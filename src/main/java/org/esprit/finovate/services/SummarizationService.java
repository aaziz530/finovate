package org.esprit.finovate.services;

public class SummarizationService {

    /**
     * Summarize text using Groq API
     * @param text Text to summarize
     * @param maxWords Maximum words in summary
     * @return Summarized text
     */
    public static String summarize(String text, int maxWords) {
        String prompt = String.format(
            "Summarize the following text in approximately %d words. " +
            "Keep the key points and main ideas. Be concise and clear.\n\n" +
            "Text to summarize:\n%s",
            maxWords, text
        );
        
        return GroqService.callGroq(prompt).trim();
    }

    /**
     * Summarize with default length (100 words)
     */
    public static String summarize(String text) {
        return summarize(text, 100);
    }

    /**
     * Generate a short summary (30-50 words)
     */
    public static String summarizeShort(String text) {
        return summarize(text, 40);
    }

    /**
     * Generate a detailed summary (150-200 words)
     */
    public static String summarizeLong(String text) {
        return summarize(text, 175);
    }

    /**
     * Summarize forum description
     */
    public static String summarizeForum(String title, String description) {
        String prompt = String.format(
            "Summarize this forum in 50 words:\n\n" +
            "Title: %s\n" +
            "Description: %s",
            title, description
        );
        
        return GroqService.callGroq(prompt).trim();
    }

    /**
     * Summarize post content
     */
    public static String summarizePost(String title, String content) {
        String prompt = String.format(
            "Summarize this post in 80 words:\n\n" +
            "Title: %s\n" +
            "Content: %s",
            title, content
        );
        
        return GroqService.callGroq(prompt).trim();
    }

    /**
     * Generate bullet points summary
     */
    public static String summarizeBulletPoints(String text) {
        String prompt = String.format(
            "Summarize the following text as 3-5 bullet points. " +
            "Each bullet should be one concise sentence.\n\n" +
            "Text:\n%s",
            text
        );
        
        return GroqService.callGroq(prompt).trim();
    }
}
