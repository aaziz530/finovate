package org.esprit.finovate.services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Pixabay API Service - Free stock photos
 */
public class PixabayService {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String PIXABAY_API_URL = "https://pixabay.com/api/";
    // Clé API gratuite - Obtenez la vôtre sur https://pixabay.com/api/docs/
    private static final String API_KEY = "47581431-310e8a70dca9c0c8c4e9e4e4e"; // Clé API valide
    
    private static int currentImageIndex = 0; // Pour alterner entre les images
    
    /**
     * Rechercher une image sur Pixabay
     * @param query Mot-clé de recherche
     * @return URL de l'image ou null
     */
    public static String searchImage(String query) {
        return searchImage(query, 0);
    }
    
    /**
     * Rechercher une image sur Pixabay avec index
     * @param query Mot-clé de recherche
     * @param imageIndex Index de l'image à récupérer (0-4)
     * @return URL de l'image ou null
     */
    public static String searchImage(String query, int imageIndex) {
        System.out.println("🔍 Recherche image Pixabay: " + query + " (index: " + imageIndex + ")");
        
        try {
            // Nettoyer et optimiser le query
            String cleanQuery = query.trim()
                .replace(" ", "+")
                .toLowerCase();
            
            String url = PIXABAY_API_URL + 
                "?key=" + API_KEY + 
                "&q=" + cleanQuery +
                "&image_type=photo" +
                "&orientation=horizontal" +
                "&safesearch=true" +
                "&per_page=5" +  // Augmenter pour avoir plus de choix
                "&order=popular" +  // Images populaires = meilleure qualité
                "&min_width=800" +  // Minimum 800px de largeur
                "&min_height=400";  // Minimum 400px de hauteur
            
            System.out.println("📡 URL: " + url);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("📥 Status: " + response.statusCode());
            
            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                JSONArray hits = jsonResponse.getJSONArray("hits");
                
                System.out.println("📊 Images trouvées: " + hits.length());
                
                if (hits.length() > 0) {
                    // Utiliser l'index pour varier les images
                    int index = Math.min(imageIndex, hits.length() - 1);
                    JSONObject selectedImage = hits.getJSONObject(index);
                    String imageUrl = selectedImage.getString("webformatURL");
                    String tags = selectedImage.optString("tags", "");
                    
                    System.out.println("✅ Image #" + index + " URL: " + imageUrl);
                    System.out.println("   Tags: " + tags);
                    return imageUrl;
                } else {
                    System.out.println("⚠️ Aucune image trouvée pour: " + query);
                }
            } else {
                System.err.println("❌ Erreur Pixabay: " + response.statusCode());
                System.err.println("   Body: " + response.body());
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur Pixabay: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Télécharger une image depuis une URL
     * @param imageUrl URL de l'image
     * @param filename Nom du fichier de destination
     * @return Chemin local de l'image ou null
     */
    public static String downloadImage(String imageUrl, String filename) {
        System.out.println("⬇️ Téléchargement image: " + filename);
        
        try {
            // Créer le dossier uploads s'il n'existe pas
            Path uploadsDir = Paths.get("uploads");
            if (!Files.exists(uploadsDir)) {
                Files.createDirectories(uploadsDir);
                System.out.println("📁 Dossier uploads créé");
            }
            
            // Télécharger l'image
            URL url = new URL(imageUrl);
            InputStream in = url.openStream();
            
            Path outputPath = uploadsDir.resolve(filename);
            Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
            in.close();
            
            System.out.println("✅ Image téléchargée: " + outputPath);
            return outputPath.toString();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur téléchargement: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Rechercher et télécharger une image
     * @param query Mot-clé de recherche
     * @param filename Nom du fichier
     * @return Chemin local de l'image ou null
     */
    public static String searchAndDownload(String query, String filename) {
        System.out.println("🎨 === DÉBUT RECHERCHE IMAGE ===");
        System.out.println("   Query: " + query);
        System.out.println("   Filename: " + filename);
        
        String imageUrl = searchImage(query, currentImageIndex);
        if (imageUrl != null) {
            String localPath = downloadImage(imageUrl, filename);
            if (localPath != null) {
                System.out.println("✅ === IMAGE PRÊTE ===");
                return localPath;
            }
        }
        
        // FALLBACK: Utiliser une image placeholder si Pixabay échoue
        System.out.println("⚠️ Pixabay a échoué, utilisation d'une image placeholder...");
        String placeholderUrl = "https://picsum.photos/800/400";
        String localPath = downloadImage(placeholderUrl, filename);
        
        if (localPath != null) {
            System.out.println("✅ === IMAGE PLACEHOLDER PRÊTE ===");
            return localPath;
        }
        
        System.out.println("❌ === ÉCHEC TOTAL RECHERCHE IMAGE ===");
        return null;
    }
    
    /**
     * Rechercher et télécharger une image différente (pour régénération)
     * @param query Mot-clé de recherche
     * @param filename Nom du fichier
     * @return Chemin local de l'image ou null
     */
    public static String searchAndDownloadNext(String query, String filename) {
        // Incrémenter l'index pour obtenir une image différente
        currentImageIndex = (currentImageIndex + 1) % 5;
        System.out.println("🔄 Recherche image suivante (index: " + currentImageIndex + ")");
        return searchAndDownload(query, filename);
    }
    
    /**
     * Réinitialiser l'index des images
     */
    public static void resetImageIndex() {
        currentImageIndex = 0;
    }
}
