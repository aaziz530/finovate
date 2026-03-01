package org.esprit.finovate.services;

import org.json.JSONObject;

/**
 * AI Post Generator Service - Generate posts with images
 */
public class AIPostGeneratorService {
    
    public static class GeneratedPost {
        public String title;
        public String content;
        public String imagePath;
        public String imageKeyword;
        
        public GeneratedPost(String title, String content, String imagePath, String imageKeyword) {
            this.title = title;
            this.content = content;
            this.imagePath = imagePath;
            this.imageKeyword = imageKeyword;
        }
    }
    
    /**
     * Générer un post complet SANS image (l'utilisateur choisira l'image)
     * @param theme Thème du post (ex: "Blockchain et Finance")
     * @param tone Ton du post (ex: "Professionnel", "Casual", "Éducatif")
     * @param length Longueur (ex: "Court", "Moyen", "Long")
     * @return Post généré sans image
     */
    public static GeneratedPost generatePost(String theme, String tone, String length) {
        System.out.println("🤖 === GÉNÉRATION POST AI ===");
        System.out.println("   Thème: " + theme);
        System.out.println("   Ton: " + tone);
        System.out.println("   Longueur: " + length);
        
        // Générer le contenu du post avec Groq
        String prompt = buildPrompt(theme, tone, length);
        System.out.println("📝 Appel Groq API...");
        JSONObject response = GroqService.callGroqJSON(prompt);
        
        String title = "Post sur " + theme;
        String content = "Contenu généré...";
        String imageKeyword = theme;
        
        try {
            if (response.has("title")) {
                title = response.getString("title");
                System.out.println("   ✅ Titre: " + title);
            }
            if (response.has("content")) {
                content = response.getString("content");
                System.out.println("   ✅ Contenu: " + content.length() + " caractères");
            }
            if (response.has("image_keyword")) {
                imageKeyword = response.getString("image_keyword");
                System.out.println("   ✅ Mot-clé image: " + imageKeyword);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur parsing réponse AI: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("✅ === POST GÉNÉRÉ (sans image) ===");
        // Retourner le post SANS image (imagePath = null)
        return new GeneratedPost(title, content, null, imageKeyword);
    }
    
    private static String buildPrompt(String theme, String tone, String length) {
        int wordCount = getWordCount(length);
        
        return String.format(
            "Génère un post de forum sur le thème: %s\n" +
            "Ton: %s\n" +
            "Longueur: environ %d mots\n\n" +
            "Réponds UNIQUEMENT avec un JSON valide dans ce format:\n" +
            "{\n" +
            "  \"title\": \"Titre accrocheur du post\",\n" +
            "  \"content\": \"Contenu détaillé du post\",\n" +
            "  \"image_keyword\": \"mot-clé TRÈS SPÉCIFIQUE en anglais\"\n" +
            "}\n\n" +
            "Règles:\n" +
            "- Le titre doit être accrocheur et pertinent\n" +
            "- Le contenu doit être informatif et bien structuré\n" +
            "- Le image_keyword doit être TRÈS SPÉCIFIQUE et directement lié au thème\n" +
            "- Exemples de bons keywords:\n" +
            "  * Pour \"Blockchain\": \"blockchain technology network\"\n" +
            "  * Pour \"Bitcoin\": \"bitcoin cryptocurrency coins\"\n" +
            "  * Pour \"Finance\": \"financial charts graphs\"\n" +
            "  * Pour \"NFT\": \"nft digital art\"\n" +
            "  * Pour \"Trading\": \"stock market trading\"\n" +
            "  * Pour \"DeFi\": \"decentralized finance crypto\"\n" +
            "- Utilise 2-3 mots en anglais pour être précis\n" +
            "- Pas de markdown, juste du texte\n" +
            "- Réponds UNIQUEMENT avec le JSON, rien d'autre",
            theme, tone, wordCount
        );
    }
    
    private static int getWordCount(String length) {
        switch (length.toLowerCase()) {
            case "court": return 100;
            case "long": return 400;
            default: return 200; // Moyen
        }
    }
    
    // Méthodes de génération d'images supprimées - l'utilisateur choisit l'image manuellement
}
