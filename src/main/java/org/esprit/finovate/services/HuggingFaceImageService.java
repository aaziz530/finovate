package org.esprit.finovate.services;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Hugging Face Image Generation Service
 * Uses Stable Diffusion for AI-generated images
 */
public class HuggingFaceImageService {
    
    // API gratuite Hugging Face - Stable Diffusion
    private static final String HF_API_URL = "https://api-inference.huggingface.co/models/stabilityai/stable-diffusion-2-1";
    // Clé API gratuite (tu peux créer la tienne sur huggingface.co)
    private static final String HF_API_KEY = "hf_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"; // À remplacer
    
    /**
     * Générer une image avec Stable Diffusion
     * @param prompt Description de l'image en anglais
     * @param filename Nom du fichier de destination
     * @return Chemin local de l'image ou null
     */
    public static String generateImage(String prompt, String filename) {
        System.out.println("🎨 === GÉNÉRATION IMAGE STABLE DIFFUSION ===");
        System.out.println("   Prompt: " + prompt);
        System.out.println("   Filename: " + filename);
        
        try {
            // Créer le dossier uploads s'il n'existe pas
            Path uploadsDir = Paths.get("uploads");
            if (!Files.exists(uploadsDir)) {
                Files.createDirectories(uploadsDir);
                System.out.println("📁 Dossier uploads créé");
            }
            
            // Préparer la requête
            URL url = new URL(HF_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + HF_API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            // Créer le JSON avec le prompt
            JSONObject json = new JSONObject();
            json.put("inputs", prompt);
            
            // Envoyer la requête
            System.out.println("📡 Envoi requête Hugging Face...");
            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes());
            os.flush();
            os.close();
            
            int responseCode = conn.getResponseCode();
            System.out.println("📥 Response code: " + responseCode);
            
            if (responseCode == 200) {
                // Télécharger l'image générée
                InputStream in = conn.getInputStream();
                Path outputPath = uploadsDir.resolve(filename);
                Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
                in.close();
                
                System.out.println("✅ Image Stable Diffusion générée: " + outputPath);
                return outputPath.toString();
            } else {
                System.err.println("❌ Erreur Hugging Face: " + responseCode);
                InputStream errorStream = conn.getErrorStream();
                if (errorStream != null) {
                    String error = new String(errorStream.readAllBytes());
                    System.err.println("   Erreur: " + error);
                }
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur génération Stable Diffusion: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Créer un prompt optimisé pour Stable Diffusion
     * @param keyword Mot-clé principal
     * @return Prompt optimisé
     */
    public static String createOptimizedPrompt(String keyword) {
        // Créer un prompt détaillé pour de meilleures images
        String prompt = String.format(
            "professional digital illustration of %s, " +
            "modern design, technology concept, " +
            "high quality, business style, " +
            "clean background, vibrant colors, " +
            "fintech theme, 4k, detailed",
            keyword
        );
        
        System.out.println("📝 Prompt optimisé: " + prompt);
        return prompt;
    }
}
