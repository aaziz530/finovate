package org.esprit.finovate.services;

import org.json.JSONObject;

public class ModerationService {

    public static class ModerationResult {
        public boolean isToxic;
        public double toxicityScore;
        public double threatScore;
        public double insultScore;
        public double profanityScore;
        public String message;
        public String explanation;

        public ModerationResult(boolean isToxic, double toxicityScore, double threatScore, 
                               double insultScore, double profanityScore, String message, String explanation) {
            this.isToxic = isToxic;
            this.toxicityScore = toxicityScore;
            this.threatScore = threatScore;
            this.insultScore = insultScore;
            this.profanityScore = profanityScore;
            this.message = message;
            this.explanation = explanation;
        }

        @Override
        public String toString() {
            return String.format("%s\n\nScores:\n• Toxicité: %.0f%%\n• Menaces: %.0f%%\n• Insultes: %.0f%%\n• Vulgarité: %.0f%%\n\n%s",
                    message, toxicityScore * 100, threatScore * 100, insultScore * 100, profanityScore * 100, explanation);
        }
    }

    /**
     * Analyze content for toxicity using Groq API
     * @param text Text to analyze
     * @return ModerationResult with scores
     */
    public static ModerationResult analyzeContent(String text) {
        String prompt = String.format(
            "You are a content moderation AI. Analyze the following text for inappropriate content in French or English. " +
            "Be STRICT and sensitive to:\n" +
            "- Personal attacks (\"tu es méchant\", \"you are mean\", \"idiot\", etc.)\n" +
            "- Insults and name-calling\n" +
            "- Threats or violence\n" +
            "- Profanity and vulgar language\n" +
            "- Harassment or bullying\n\n" +
            "Rate each category from 0.0 (completely safe) to 1.0 (very inappropriate):\n" +
            "- toxicity: Overall toxicity level (be strict, even mild insults should score 0.5+)\n" +
            "- threat: Contains threats or violence\n" +
            "- insult: Contains insults or personal attacks (\"méchant\", \"stupide\", \"idiot\" = 0.6+)\n" +
            "- profanity: Contains profanity or vulgar language\n\n" +
            "IMPORTANT: Even mild negative personal comments should score at least 0.5 in insult category.\n\n" +
            "Respond ONLY with valid JSON in this exact format:\n" +
            "{\n" +
            "  \"toxicity\": 0.0,\n" +
            "  \"threat\": 0.0,\n" +
            "  \"insult\": 0.0,\n" +
            "  \"profanity\": 0.0,\n" +
            "  \"explanation\": \"Brief explanation in French\"\n" +
            "}\n\n" +
            "Text to analyze:\n%s",
            text
        );

        try {
            JSONObject response = GroqService.callGroqJSON(prompt);
            
            if (response.has("error")) {
                return new ModerationResult(false, 0, 0, 0, 0, 
                    "⚠️ Unable to analyze content", response.getString("raw"));
            }

            double toxicity = response.optDouble("toxicity", 0.0);
            double threat = response.optDouble("threat", 0.0);
            double insult = response.optDouble("insult", 0.0);
            double profanity = response.optDouble("profanity", 0.0);
            String explanation = response.optString("explanation", "Aucune explication fournie");

            // Lower threshold to 0.5 (50%) to catch milder insults
            boolean isToxic = toxicity > 0.5 || threat > 0.5 || insult > 0.5 || profanity > 0.6;
            String message = isToxic ? "⚠️ ATTENTION: Ce contenu peut être inapproprié" : "✅ Contenu approprié";

            return new ModerationResult(isToxic, toxicity, threat, insult, profanity, message, explanation);
        } catch (Exception e) {
            System.err.println("Moderation error: " + e.getMessage());
            return new ModerationResult(false, 0, 0, 0, 0, 
                "❌ Erreur d'analyse", e.getMessage());
        }
    }

    /**
     * Quick check if content is safe to post
     */
    public static boolean isSafeContent(String text) {
        ModerationResult result = analyzeContent(text);
        return !result.isToxic;
    }

    /**
     * Get moderation warning message
     */
    public static String getModerationWarning(String text) {
        ModerationResult result = analyzeContent(text);
        if (result.isToxic) {
            return result.toString() + "\n\nVeuillez réviser votre message avant de publier.";
        }
        return null;
    }

    /**
     * Get detailed moderation report
     */
    public static String getModerationReport(String text) {
        ModerationResult result = analyzeContent(text);
        return result.toString();
    }
}
