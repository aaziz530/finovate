package org.esprit.finovate.services;

public class TranslationService {

    /**
     * Translate text using Groq API
     * @param text Text to translate
     * @param sourceLang Source language
     * @param targetLang Target language
     * @return Translated text
     */
    public static String translate(String text, String sourceLang, String targetLang) {
        String prompt = String.format(
            "Translate the following text from %s to %s. " +
            "Provide ONLY the translation, no explanations, no additional text.\n\n" +
            "Text to translate:\n%s",
            sourceLang, targetLang, text
        );
        
        return GroqService.callGroq(prompt).trim();
    }

    /**
     * Auto-detect language and translate to target language
     */
    public static String translateAuto(String text, String targetLang) {
        String prompt = String.format(
            "Translate the following text to %s. " +
            "Detect the source language automatically. " +
            "Provide ONLY the translation, no explanations.\n\n" +
            "Text:\n%s",
            targetLang, text
        );
        
        return GroqService.callGroq(prompt).trim();
    }

    public static String translateToEnglish(String text) {
        return translateAuto(text, "English");
    }

    public static String translateToFrench(String text) {
        return translateAuto(text, "French");
    }

    public static String translateToSpanish(String text) {
        return translateAuto(text, "Spanish");
    }

    public static String translateToGerman(String text) {
        return translateAuto(text, "German");
    }

    public static String[] getSupportedLanguages() {
        return new String[]{
            "English", "French", "Spanish", "German", "Italian", "Portuguese", 
            "Russian", "Chinese", "Japanese", "Arabic", "Dutch", "Polish", 
            "Turkish", "Swedish", "Danish", "Finnish", "Norwegian", "Czech", 
            "Greek", "Hebrew", "Korean", "Hindi", "Vietnamese", "Thai"
        };
    }
}
