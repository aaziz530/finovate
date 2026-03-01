package org.esprit.finovate.services;

import org.esprit.finovate.config.AIConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Groq API Service - Ultra-fast AI inference
 * Handles: Translation, Summarization, Moderation, Personality Analysis
 */
public class GroqService {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    
    // Groq models (all free and very fast)
    private static final String[] MODELS = {
        "llama-3.3-70b-versatile",  // Best quality
        "llama-3.1-70b-versatile",  // Fast and good
        "mixtral-8x7b-32768",       // Good for long context
        "gemma2-9b-it"              // Lightweight
    };
    
    private static String workingModel = MODELS[0]; // Start with best model

    /**
     * Send a prompt to Groq API
     * @param prompt The instruction/question
     * @return AI response text
     */
    public static String callGroq(String prompt) {
        String apiKey = AIConfig.getGroqApiKey();
        if (apiKey.isEmpty() || apiKey.contains("YOUR_")) {
            return "⚠️ Groq API key not configured";
        }

        // Try with working model first
        String result = tryModel(workingModel, prompt, apiKey);
        if (!result.startsWith("❌")) {
            return result;
        }

        // If failed, try other models
        for (String model : MODELS) {
            if (model.equals(workingModel)) continue;
            
            System.out.println("🔄 Trying Groq model: " + model);
            result = tryModel(model, prompt, apiKey);
            
            if (!result.startsWith("❌")) {
                workingModel = model;
                System.out.println("✅ Success with model: " + model);
                return result;
            }
        }

        return "❌ All Groq models failed. Check your API key.";
    }

    /**
     * Try a specific Groq model
     */
    private static String tryModel(String modelName, String prompt, String apiKey) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", modelName);
            
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);
            messages.put(message);
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GROQ_API_URL))
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
                System.err.println("Groq API error (" + modelName + "): " + response.statusCode() + " - " + response.body());
                return "❌ Error " + response.statusCode();
            }
        } catch (Exception e) {
            System.err.println("Groq error (" + modelName + "): " + e.getMessage());
            return "❌ Exception: " + e.getMessage();
        }
        return "❌ No response";
    }

    /**
     * Call Groq with JSON response
     */
    public static JSONObject callGroqJSON(String prompt) {
        String response = callGroq(prompt + "\n\nIMPORTANT: Respond ONLY with valid JSON, no markdown, no explanation.");
        try {
            response = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            return new JSONObject(response);
        } catch (Exception e) {
            System.err.println("Failed to parse JSON: " + response);
            JSONObject error = new JSONObject();
            error.put("error", "Failed to parse response");
            error.put("raw", response);
            return error;
        }
    }
    
    public static String getWorkingModel() {
        return workingModel;
    }
}
