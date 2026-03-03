package org.esprit.finovate.services;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

/**
 * Service for external third-party API Integrations.
 * Features:
 * 1. MyMemory Translation API (Free, no auth needed).
 * 2. Twilio WhatsApp API.
 */
public class ExternalApiService {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    // ==========================================
    // 1. TRANSLATION API (MyMemory)
    // ==========================================

    /**
     * Translates text using MyMemory API with auto-detection.
     * 
     * @param text       The text to translate.
     * @param targetLang Example: "fr" or "en"
     * @return CompletableFuture containing the translated text.
     */
    public static CompletableFuture<String> translateText(String text, String targetLang) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
                // MyMemory supports 'autodetect' as a source language
                String langPair = "autodetect|" + targetLang;
                String encodedLang = URLEncoder.encode(langPair, StandardCharsets.UTF_8);
                String url = "https://api.mymemory.translated.net/get?q=" + encodedText + "&langpair=" + encodedLang;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

                // Simple manual JSON parsing to avoid pulling in Gson/Jackson just for this
                // Example response: {"responseData":{"translatedText":"Bonjour!"}, ...}
                String body = response.body();
                String key = "\"translatedText\":\"";
                int start = body.indexOf(key);
                if (start != -1) {
                    start += key.length();
                    int end = body.indexOf("\"", start);
                    if (end != -1) {
                        return body.substring(start, end).replace("\\u0027", "'").replace("\\\"", "\"");
                    }
                }
                return "[Erreur de traduction]";
            } catch (Exception e) {
                e.printStackTrace();
                return "[Erreur réseau]";
            }
        });
    }

    // ==========================================
    // 2. WHATSAPP API (Twilio)
    // ==========================================

    // TODO: REPLACE THESE PLACEHOLDERS WITH YOUR ACTUAL TWILIO CREDENTIALS
    private static final String TWILIO_ACCOUNT_SID = "YOUR_TWILIO_SID";
    private static final String TWILIO_AUTH_TOKEN = "YOUR_TWILIO_AUTH_TOKEN";
    private static final String TWILIO_WHATSAPP_NUMBER = "whatsapp:+14155238886"; // Default Twilio Sandbox number

    /**
     * Sends a WhatsApp message via Twilio.
     * 
     * @param toPhoneNumber The destination number (e.g. "whatsapp:+21612345678")
     * @param messageBody   The message to send.
     */
    public static void sendWhatsAppMessage(String toPhoneNumber, String messageBody) {
        final String finalNumber = toPhoneNumber.startsWith("whatsapp:") ? toPhoneNumber : "whatsapp:" + toPhoneNumber;
        CompletableFuture.runAsync(() -> {
            try {
                String url = "https://api.twilio.com/2010-04-01/Accounts/" + TWILIO_ACCOUNT_SID + "/Messages.json";

                String formData = "From=" + URLEncoder.encode(TWILIO_WHATSAPP_NUMBER, StandardCharsets.UTF_8)
                        + "&To=" + URLEncoder.encode(finalNumber, StandardCharsets.UTF_8)
                        + "&Body=" + URLEncoder.encode(messageBody, StandardCharsets.UTF_8);

                String auth = TWILIO_ACCOUNT_SID + ":" + TWILIO_AUTH_TOKEN;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", "Basic " + encodedAuth)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(formData))
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                System.out
                        .println("[TWILIO WHATSAPP] Status: " + response.statusCode() + " | Body: " + response.body());

            } catch (Exception e) {
                System.err.println("[TWILIO WHATSAPP ERROR] Failed to send message: " + e.getMessage());
            }
        });
    }
}
