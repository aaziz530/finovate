package org.esprit.finovate.services;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * AI Image Generator Service - Generate images with AI
 * Uses multiple fallback services for reliability
 */
public class AIImageGeneratorService {
    
    // Service 1: Picsum (photos aléatoires de qualité)
    private static final String PICSUM_API = "https://picsum.photos/800/400";
    
    // Service 2: PlaceHolder avec texte personnalisé
    private static final String PLACEHOLDER_API = "https://via.placeholder.com/800x400/";
    
    /**
     * Générer une image avec fallback multiple
     * @param description Description de l'image en anglais
     * @param filename Nom du fichier de destination
     * @return Chemin local de l'image ou null
     */
    /**
     * Générer une image avec fallback multiple
     * @param description Description de l'image en anglais
     * @param filename Nom du fichier de destination
     * @return Chemin local de l'image ou null
     */
    public static String generateImage(String description, String filename) {
        System.out.println("🎨 === GÉNÉRATION IMAGE ===");
        System.out.println("   Description: " + description);
        System.out.println("   Filename: " + filename);

        try {
            // Créer le dossier uploads s'il n'existe pas
            Path uploadsDir = Paths.get("uploads");
            if (!Files.exists(uploadsDir)) {
                Files.createDirectories(uploadsDir);
                System.out.println("📁 Dossier uploads créé");
            }

            // Extraire le mot-clé principal
            String keyword = description.split("[,\\s]+")[0];

            // Essayer Service 1: Hugging Face Stable Diffusion (AI sur mesure)
            try {
                System.out.println("📡 Tentative Hugging Face Stable Diffusion...");
                String prompt = HuggingFaceImageService.createOptimizedPrompt(keyword);
                String imagePath = HuggingFaceImageService.generateImage(prompt, filename);

                if (imagePath != null) {
                    System.out.println("✅ Image AI Stable Diffusion générée: " + imagePath);
                    return imagePath;
                }

                System.out.println("⚠️ Hugging Face non configuré ou échoué, essai Picsum...");

            } catch (Exception e1) {
                System.out.println("⚠️ Hugging Face échoué: " + e1.getMessage());
            }

            // Essayer Service 2: Picsum (photos de qualité)
            try {
                System.out.println("📡 Tentative Picsum...");
                String imageUrl = PICSUM_API + "?random=" + System.currentTimeMillis();

                URL url = new URL(imageUrl);
                InputStream in = url.openStream();

                Path outputPath = uploadsDir.resolve(filename);
                Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
                in.close();

                System.out.println("✅ Image Picsum générée: " + outputPath);
                return outputPath.toString();

            } catch (Exception e2) {
                System.out.println("⚠️ Picsum échoué, essai placeholder...");

                // Essayer Service 3: Placeholder avec couleur thématique
                try {
                    String color = getColorForKeyword(keyword);
                    String textColor = "FFFFFF";
                    String text = URLEncoder.encode(keyword.toUpperCase(), StandardCharsets.UTF_8);

                    String imageUrl = PLACEHOLDER_API + color + "/" + textColor + 
                                    "?text=" + text;

                    System.out.println("📡 URL Placeholder: " + imageUrl);

                    URL url = new URL(imageUrl);
                    InputStream in = url.openStream();

                    Path outputPath = uploadsDir.resolve(filename);
                    Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
                    in.close();

                    System.out.println("✅ Image Placeholder générée: " + outputPath);
                    return outputPath.toString();

                } catch (Exception e3) {
                    System.err.println("❌ Tous les services ont échoué");
                    e3.printStackTrace();
                    return null;
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur génération image: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    
    /**
     * Obtenir une couleur thématique selon le mot-clé
     * @param keyword Mot-clé
     * @return Code couleur hexadécimal (sans #)
     */
    private static String getColorForKeyword(String keyword) {
        switch (keyword.toLowerCase()) {
            case "blockchain": return "667eea"; // Violet
            case "bitcoin": return "f7931a"; // Orange Bitcoin
            case "cryptocurrency": return "00d4aa"; // Vert crypto
            case "ethereum": return "627eea"; // Bleu Ethereum
            case "nft": return "ff6b9d"; // Rose
            case "defi": return "00d395"; // Vert DeFi
            case "trading": return "26a69a"; // Teal
            case "finance": return "0079d3"; // Bleu finance
            case "banking": return "1e88e5"; // Bleu bancaire
            case "investment": return "43a047"; // Vert investissement
            case "stock": return "e53935"; // Rouge bourse
            case "payment": return "00acc1"; // Cyan
            case "technology": return "5e35b1"; // Violet tech
            case "digital": return "3949ab"; // Indigo
            case "security": return "d32f2f"; // Rouge sécurité
            case "data": return "1976d2"; // Bleu data
            case "future": return "7b1fa2"; // Violet futur
            case "innovation": return "f57c00"; // Orange innovation
            default: return "0079d3"; // Bleu par défaut
        }
    }
    
    /**
     * Générer une description d'image détaillée pour l'IA
     * @param theme Thème du post
     * @param title Titre du post
     * @return Description optimisée pour la génération d'image
     */
    public static String createImageDescription(String theme, String title) {
        String description = String.format("%s digital illustration", theme);
        System.out.println("📝 Description image créée: " + description);
        return description;
    }
    
    /**
     * Générer une description d'image basée UNIQUEMENT sur le titre
     * Extrait le concept principal du titre pour simplifier
     * @param title Titre du post
     * @return Description optimisée pour la génération d'image
     */
    public static String createImageDescriptionFromTitle(String title) {
        // Extraire le mot-clé principal du titre (premier mot significatif)
        String mainKeyword = extractMainKeyword(title);
        
        // Créer une description TRÈS SIMPLE
        String description = String.format("%s digital illustration", mainKeyword);
        
        System.out.println("📝 Mot-clé extrait: " + mainKeyword);
        System.out.println("📝 Description image: " + description);
        return description;
    }
    
    /**
     * Extraire le mot-clé principal d'un titre
     * @param title Titre complet
     * @return Mot-clé principal
     */
    private static String extractMainKeyword(String title) {
        // Liste de mots à ignorer (articles, prépositions, etc.)
        String[] stopWords = {"le", "la", "les", "un", "une", "des", "de", "du", "et", "ou", 
                              "dans", "pour", "avec", "sur", "par", "en", "à", "au", "aux",
                              "comment", "pourquoi", "quand", "guide", "introduction"};
        
        // Nettoyer le titre
        String cleanTitle = title.toLowerCase()
            .replaceAll("[^a-zàâäéèêëïîôùûüÿæœç\\s]", "") // Garder lettres et espaces
            .trim();
        
        // Séparer en mots
        String[] words = cleanTitle.split("\\s+");
        
        // Trouver le premier mot significatif
        for (String word : words) {
            if (word.length() > 3) { // Mot de plus de 3 lettres
                boolean isStopWord = false;
                for (String stopWord : stopWords) {
                    if (word.equals(stopWord)) {
                        isStopWord = true;
                        break;
                    }
                }
                if (!isStopWord) {
                    // Traduire en anglais si nécessaire (mots courants)
                    return translateToEnglish(word);
                }
            }
        }
        
        // Si aucun mot trouvé, retourner le premier mot
        return words.length > 0 ? translateToEnglish(words[0]) : "technology";
    }
    
    /**
     * Traduire les mots-clés courants en anglais
     * @param word Mot en français
     * @return Mot en anglais
     */
    private static String translateToEnglish(String word) {
        // Dictionnaire simple des termes fintech courants
        switch (word.toLowerCase()) {
            // Crypto & Blockchain
            case "blockchain": return "blockchain";
            case "bitcoin": return "bitcoin";
            case "crypto": case "cryptomonnaie": case "cryptomonnaies": return "cryptocurrency";
            case "ethereum": return "ethereum";
            case "nft": return "nft";
            case "defi": return "defi";
            case "token": return "token";
            case "mining": return "mining";
            case "wallet": case "portefeuille": return "wallet";
            
            // Finance
            case "trading": return "trading";
            case "finance": case "financier": case "financière": return "finance";
            case "banque": case "bancaire": return "banking";
            case "investissement": case "investir": return "investment";
            case "bourse": return "stock";
            case "monnaie": return "currency";
            case "paiement": return "payment";
            case "marché": case "market": return "market";
            
            // Technologie
            case "technologie": return "technology";
            case "numérique": case "digital": return "digital";
            case "innovation": return "innovation";
            case "sécurité": return "security";
            case "données": case "data": return "data";
            case "intelligence": return "intelligence";
            case "artificielle": return "artificial";
            case "smart": case "intelligent": return "smart";
            case "contract": case "contrat": return "contract";
            
            // Concepts généraux
            case "avenir": case "futur": case "future": return "future";
            case "découvrez": case "découvrir": return "discover";
            case "guide": return "guide";
            case "stratégie": case "stratégies": return "strategy";
            case "analyse": return "analysis";
            case "tendance": case "tendances": return "trend";
            case "révolution": return "revolution";
            case "économie": return "economy";
            
            default: 
                // Si le mot n'est pas dans le dictionnaire, utiliser un terme générique
                if (word.length() > 3) {
                    return "fintech"; // Fallback sûr
                }
                return "technology";
        }
    }
    
    /**
     * Générer une image basée sur le thème et le titre
     * @param theme Thème du post
     * @param title Titre du post
     * @param filename Nom du fichier
     * @return Chemin local de l'image ou null
     */
    public static String generateImageFromTheme(String theme, String title, String filename) {
        String description = createImageDescription(theme, title);
        return generateImage(description, filename);
    }
    
    /**
     * Générer une image basée UNIQUEMENT sur le titre (plus précis)
     * @param title Titre du post
     * @param filename Nom du fichier
     * @return Chemin local de l'image ou null
     */
    public static String generateImageFromTitle(String title, String filename) {
        String description = createImageDescriptionFromTitle(title);
        return generateImage(description, filename);
    }
}
