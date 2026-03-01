package org.esprit.finovate.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class ImageUtils {
    
    // Dossier pour stocker les images uploadées
    private static final String UPLOAD_DIR = "uploads/images/";
    
    static {
        // Créer le dossier uploads s'il n'existe pas
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Ouvre un dialogue pour sélectionner une image
     */
    public static File selectImageFile(Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        
        // Filtres pour les types d'images
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
            new FileChooser.ExtensionFilter("PNG", "*.png"),
            new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("GIF", "*.gif"),
            new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        
        return fileChooser.showOpenDialog(ownerWindow);
    }
    
    /**
     * Sauvegarde une image dans le dossier uploads et retourne le chemin relatif
     */
    public static String saveImage(File sourceFile) throws IOException {
        if (sourceFile == null || !sourceFile.exists()) {
            return null;
        }
        
        // Générer un nom unique pour l'image
        String extension = getFileExtension(sourceFile.getName());
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        
        // Chemin de destination
        Path destinationPath = Paths.get(UPLOAD_DIR + uniqueFileName);
        
        // Copier le fichier
        Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Retourner le chemin relatif
        return UPLOAD_DIR + uniqueFileName;
    }
    
    /**
     * Charge une image depuis un chemin et retourne un ImageView
     */
    public static ImageView loadImageView(String imagePath, double width, double height) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                System.out.println("Image non trouvée: " + imagePath);
                return null;
            }
            
            String imageUrl = imageFile.toURI().toString();
            Image image = new Image(imageUrl, width, height, true, true);
            
            if (image.isError()) {
                System.out.println("Erreur de chargement de l'image: " + imagePath);
                return null;
            }
            
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            
            return imageView;
        } catch (Exception e) {
            System.out.println("Exception lors du chargement de l'image: " + imagePath);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Charge une image depuis un chemin
     */
    public static Image loadImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                System.out.println("Image non trouvée: " + imagePath);
                return null;
            }
            
            String imageUrl = imageFile.toURI().toString();
            Image image = new Image(imageUrl);
            
            if (image.isError()) {
                System.out.println("Erreur de chargement de l'image: " + imagePath);
                return null;
            }
            
            return image;
        } catch (Exception e) {
            System.out.println("Exception lors du chargement de l'image: " + imagePath);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Supprime une image du système de fichiers
     */
    public static boolean deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return false;
        }
        
        try {
            File imageFile = new File(imagePath);
            return imageFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Obtient l'extension d'un fichier
     */
    private static String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // Pas d'extension
        }
        return fileName.substring(lastIndexOf);
    }
    
    /**
     * Valide si un fichier est une image valide
     */
    public static boolean isValidImage(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".png") || 
               fileName.endsWith(".jpg") || 
               fileName.endsWith(".jpeg") || 
               fileName.endsWith(".gif") || 
               fileName.endsWith(".bmp");
    }
    
    /**
     * Obtient la taille d'un fichier en MB
     */
    public static double getFileSizeMB(File file) {
        if (file == null || !file.exists()) {
            return 0;
        }
        return file.length() / (1024.0 * 1024.0);
    }
    
    /**
     * Valide la taille maximale d'une image (5MB par défaut)
     */
    public static boolean isValidImageSize(File file, double maxSizeMB) {
        return getFileSizeMB(file) <= maxSizeMB;
    }
}
