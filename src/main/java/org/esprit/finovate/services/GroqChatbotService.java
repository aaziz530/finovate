package org.esprit.finovate.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.cdimascio.dotenv.Dotenv;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class GroqChatbotService implements ChatbotService {

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.1-8b-instant";

    private static final String SYSTEM_PROMPT = "Tu es un assistant spécialisé STRICTEMENT en comptabilité et finance au sens large. " +
            "Ton périmètre autorisé inclut notamment : comptabilité (bilan, compte de résultat, journaux, TVA), finance d'entreprise (cashflow, ratios, budget), banque (comptes, virements, cartes, crédits, intérêts, RIB/IBAN), " +
            "monnaie et devises (taux de change), épargne, assurance-vie, bourse et marchés financiers (actions, obligations, indices, ETF), gestion de portefeuille, analyse financière et fiscalité. " +
            "Tu dois répondre uniquement aux questions liées à l'argent et à ces domaines. " +
            "Si la question est hors sujet (programmation, sport, santé, politique, cuisine, etc.), refuse poliment en disant que tu ne peux répondre qu'aux sujets d'argent/comptabilité/finance/banque/bourse. " +
            "Réponds de manière claire, professionnelle et concise, avec des exemples chiffrés si nécessaire.";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public GroqChatbotService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.objectMapper = new ObjectMapper();

        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
        this.apiKey = dotenv.get("GROQ_API_KEY");
    }

    @Override
    public String ask(String userMessage) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GROQ_API_KEY is missing. Please add it to your .env file.");
        }
        if (userMessage == null || userMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("Message is empty.");
        }

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", MODEL);
        payload.put("temperature", 0.2);
        payload.put("max_tokens", 512);

        ArrayNode messages = payload.putArray("messages");
        ObjectNode sys = objectMapper.createObjectNode();
        sys.put("role", "system");
        sys.put("content", SYSTEM_PROMPT);
        messages.add(sys);

        ObjectNode user = objectMapper.createObjectNode();
        user.put("role", "user");
        user.put("content", userMessage.trim());
        messages.add(user);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String body = response.body() == null ? "" : response.body();
            throw new RuntimeException("Groq API error (HTTP " + response.statusCode() + "): " + body);
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw new RuntimeException("Groq API: empty choices");
        }

        JsonNode content = choices.get(0).path("message").path("content");
        if (content.isMissingNode() || content.asText().isBlank()) {
            throw new RuntimeException("Groq API: empty message content");
        }

        return content.asText().trim();
    }
}
