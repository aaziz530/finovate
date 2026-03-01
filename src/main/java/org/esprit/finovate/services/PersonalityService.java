package org.esprit.finovate.services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PersonalityService {

    public static class PersonalityAnalysis {
        public String personalityType;
        public String emoji;
        public String description;
        public double positiveScore;
        public double neutralScore;
        public double negativeScore;
        public List<String> traits;
        public String title;

        public PersonalityAnalysis(String type, String emoji, String description, 
                                  double positive, double neutral, double negative, String title) {
            this.personalityType = type;
            this.emoji = emoji;
            this.description = description;
            this.positiveScore = positive;
            this.neutralScore = neutral;
            this.negativeScore = negative;
            this.title = title;
            this.traits = new ArrayList<>();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(emoji).append(" ").append(personalityType).append("\n\n");
            sb.append(description).append("\n\n");
            sb.append("🏆 Titre: ").append(title).append("\n\n");
            sb.append("📊 Sentiment:\n");
            sb.append(String.format("• Positif: %.0f%%\n", positiveScore * 100));
            sb.append(String.format("• Neutre: %.0f%%\n", neutralScore * 100));
            sb.append(String.format("• Négatif: %.0f%%\n", negativeScore * 100));
            
            if (!traits.isEmpty()) {
                sb.append("\n✨ Traits de personnalité:\n");
                for (String trait : traits) {
                    sb.append("• ").append(trait).append("\n");
                }
            }
            
            return sb.toString();
        }
    }

    /**
     * Analyze user personality based on their posts and comments
     * @param userContent Combined text from user's posts and comments
     * @return PersonalityAnalysis
     */
    public static PersonalityAnalysis analyzePersonality(String userContent) {
        String prompt = String.format(
            "Analyze the personality of a user based on their forum posts and comments. " +
            "Provide a detailed personality analysis.\n\n" +
            "Respond ONLY with valid JSON in this exact format:\n" +
            "{\n" +
            "  \"personalityType\": \"Type name in French\",\n" +
            "  \"emoji\": \"Single emoji\",\n" +
            "  \"description\": \"Detailed description in French (2-3 sentences)\",\n" +
            "  \"positiveScore\": 0.0,\n" +
            "  \"neutralScore\": 0.0,\n" +
            "  \"negativeScore\": 0.0,\n" +
            "  \"title\": \"Creative title in French\",\n" +
            "  \"traits\": [\"trait1\", \"trait2\", \"trait3\"]\n" +
            "}\n\n" +
            "User's content:\n%s",
            userContent.length() > 2000 ? userContent.substring(0, 2000) + "..." : userContent
        );

        try {
            JSONObject response = GroqService.callGroqJSON(prompt);
            
            if (response.has("error")) {
                return new PersonalityAnalysis("Non analysé", "❓", 
                    "Impossible d'analyser la personnalité pour le moment.", 
                    0, 0, 0, "Utilisateur Mystérieux");
            }

            String type = response.optString("personalityType", "Contributeur");
            String emoji = response.optString("emoji", "😊");
            String description = response.optString("description", "Utilisateur actif de la communauté");
            double positive = response.optDouble("positiveScore", 0.5);
            double neutral = response.optDouble("neutralScore", 0.3);
            double negative = response.optDouble("negativeScore", 0.2);
            String title = response.optString("title", "Membre de la Communauté");

            PersonalityAnalysis analysis = new PersonalityAnalysis(
                type, emoji, description, positive, neutral, negative, title
            );

            // Add traits
            if (response.has("traits")) {
                JSONArray traitsArray = response.getJSONArray("traits");
                for (int i = 0; i < traitsArray.length(); i++) {
                    analysis.traits.add(traitsArray.getString(i));
                }
            }

            return analysis;
        } catch (Exception e) {
            System.err.println("Personality analysis error: " + e.getMessage());
            return new PersonalityAnalysis("Erreur", "❌", 
                "Erreur lors de l'analyse: " + e.getMessage(), 
                0, 0, 0, "Utilisateur");
        }
    }

    /**
     * Generate a creative title based on personality
     */
    public static String generatePersonalityTitle(PersonalityAnalysis analysis) {
        return analysis.title;
    }

    /**
     * Quick personality summary (for badges/profiles)
     */
    public static String getQuickSummary(String userContent) {
        String prompt = String.format(
            "Based on this user's forum activity, give them a creative title in French (max 5 words). " +
            "Be positive and encouraging. Respond with ONLY the title, nothing else.\n\n" +
            "User content:\n%s",
            userContent.length() > 1000 ? userContent.substring(0, 1000) + "..." : userContent
        );

        return GroqService.callGroq(prompt).trim();
    }
}
