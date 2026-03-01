package org.esprit.finovate.services;

import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONArray;
import org.json.JSONObject;
import org.esprit.finovate.entities.Goal;
import org.esprit.finovate.entities.Transaction;
import org.esprit.finovate.utils.Session;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Service for generating personalized financial advice using Grok AI.
 * Aggregates user data (transactions, goals, bills) and generates actionable advice.
 */
public class FinancialAdviceService {

    private static final String GROK_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROK_MODEL = "llama-3.3-70b-versatile";
    private static final HttpClient client = HttpClient.newHttpClient();

    private final String apiKey;
    private final ITransactionService transactionService;
    private final IGoalService goalService;

    public FinancialAdviceService() {
        Dotenv dotenv = Dotenv.load();
        this.apiKey = dotenv.get("GROQ_API_KEY");
        this.transactionService = new TransactionService();
        this.goalService = new GoalService();
    }

    /**
     * Generate a personalized financial advice message for the current user.
     * @return AI-generated advice string
     */
    public String generateAdvice() {
        if (Session.currentUser == null) {
            return "⚠️ Veuillez vous connecter pour obtenir des conseils personnalisés.";
        }

        if (apiKey == null || apiKey.isEmpty() || apiKey.contains("YOUR_")) {
            return "⚠️ Clé API Grok non configurée. Veuillez ajouter GROQ_API_KEY dans .env";
        }

        try {
            int userId = Session.currentUser.getId().intValue();
            String context = buildUserContext(userId);
            String prompt = buildPrompt(context);
            String advice = callGrokAPI(prompt);

            if (advice == null || advice.isEmpty()) {
                return "⚠️ Impossible de générer des conseils. Veuillez réessayer.";
            }

            return advice;
        } catch (SQLException e) {
            System.err.println("[FinancialAdviceService] Erreur lors du chargement des données: " + e.getMessage());
            return "⚠️ Erreur lors du chargement de vos données financières.";
        } catch (Exception e) {
            System.err.println("[FinancialAdviceService] Erreur: " + e.getMessage());
            return "⚠️ Erreur lors de la génération des conseils: " + e.getMessage();
        }
    }

    /**
     * Build a text summary of the user's financial situation.
     */
    private String buildUserContext(int userId) throws SQLException {
        StringBuilder sb = new StringBuilder();

        // User info
        sb.append("=== PROFIL UTILISATEUR ===\n");
        sb.append(String.format("Nom: %s %s\n", Session.currentUser.getFirstName(), Session.currentUser.getLastName()));
        float balance = transactionService.getUserBalance(userId);
        sb.append(String.format("Solde actuel: %.2f TND\n", balance));
        sb.append(String.format("FinoPoints: %d\n", Session.currentUser.getPoints()));

        // Recent transactions
        sb.append("\n=== TRANSACTIONS RÉCENTES ===\n");
        List<Transaction> transactions = transactionService.getTransactionsByUserId(userId);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        int txCount = Math.min(transactions.size(), 5);
        for (int i = 0; i < txCount; i++) {
            Transaction t = transactions.get(i);
            boolean isSent = t.getSenderId() == userId;
            String type = isSent ? "ENVOYÉ" : "REÇU";
            sb.append(String.format("- [%s] %s: %.2f TND (%s) - %s\n",
                    sdf.format(t.getDate()),
                    type,
                    t.getAmount(),
                    isSent ? "À: " + t.getReceiverName() : "De: " + t.getSenderName(),
                    t.getDescription() != null ? t.getDescription() : "Sans description"));
        }
        if (transactions.isEmpty()) {
            sb.append("- Aucune transaction récente\n");
        }

        // Daily transfer usage
        sb.append("\n=== LIMITES DE TRANSFERT ===\n");
        float dailyTotal = transactionService.getDailyTransferTotal(userId);
        sb.append(String.format("Transferts du jour: %.2f / 3000 TND\n", dailyTotal));
        sb.append(String.format("Limite restante: %.2f TND\n", Math.max(0, 3000 - dailyTotal)));

        // Goals
        sb.append("\n=== OBJECTIFS FINANCIERS ===\n");
        List<Goal> goals = goalService.getGoalsByUserId(userId);
        for (Goal g : goals) {
            sb.append(String.format("- %s: %.2f / %.2f TND (%.0f%%) - Statut: %s\n",
                    g.getTitle(),
                    g.getCurrentAmount(),
                    g.getTargetAmount(),
                    g.getProgress() * 100,
                    g.getStatus()));
            if (g.getDeadline() != null && !"Achieved".equalsIgnoreCase(g.getStatus())) {
                float suggested = g.getSuggestedMonthlySaving();
                sb.append(String.format("  → Épargne mensuelle suggérée: %.2f TND\n", suggested));
            }
        }
        if (goals.isEmpty()) {
            sb.append("- Aucun objectif actif\n");
        }

        return sb.toString();
    }

    /**
     * Build the AI prompt with user context.
     */
    private String buildPrompt(String context) {
        return """
            Tu es un conseiller financier amical et professionnel pour une application fintech appelée "Finovate".
            
            Basé sur les données financières de l'utilisateur ci-dessous, génère un message de conseil financier personnalisé et court.
            
            Règles:
            - Maximum 3-4 phrases
            - Sois encourageant et donne des conseils actionnables
            - Si l'utilisateur a des objectifs, mentionne la progression ou des suggestions
            - Si le solde est bas, donne un avertissement doux
            - Si la limite de transfert quotidien est presque atteinte, rappelle-le
            - Écris en français (l'utilisateur est tunisien)
            - Utilise modérément des emojis pour rendre le message amical
            
            DONNÉES UTILISATEUR:
            %s
            
            Génère le conseil maintenant:
            """.formatted(context);
    }

    /**
     * Call Grok API directly.
     */
    private String callGrokAPI(String prompt) throws Exception {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", GROK_MODEL);

        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        messages.put(message);

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 500);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROK_API_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());
            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject messageObj = choice.getJSONObject("message");
                return messageObj.getString("content");
            }
        } else {
            System.err.println("[FinancialAdviceService] Grok API error: " + response.statusCode() + " - " + response.body());
            throw new Exception("Erreur API Grok: " + response.statusCode());
        }

        return null;
    }
}
