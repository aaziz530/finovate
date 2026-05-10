package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.esprit.finovate.utils.ImageUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateForumDialogController {

    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private Button selectImageBtn;
    @FXML private Label imageNameLabel;
    @FXML private ImageView imagePreview;

    private Stage dialogStage;
    private ForumsController forumsController;
    private long currentUserId;
    private Long forumIdToEdit = null; // null = création, sinon = modification
    private File selectedImageFile = null;
    private String existingImagePath = null;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setForumsController(ForumsController forumsController) {
        this.forumsController = forumsController;
    }

    public void setCurrentUserId(long userId) {
        this.currentUserId = userId;
    }

    public void setForumToEdit(long forumId, String name, String description) {
        this.forumIdToEdit = forumId;
        nameField.setText(name);
        descriptionArea.setText(description);
    }

    public void setForumToEdit(long forumId, String name, String description, String imagePath) {
        this.forumIdToEdit = forumId;
        this.existingImagePath = imagePath;
        nameField.setText(name);
        descriptionArea.setText(description);
        
        // Afficher l'image existante si elle existe
        if (imagePath != null && !imagePath.isEmpty()) {
            Image image = ImageUtils.loadImage(imagePath);
            if (image != null) {
                imagePreview.setImage(image);
                imagePreview.setVisible(true);
                imagePreview.setManaged(true);
                imageNameLabel.setText("Image actuelle");
            }
        }
    }

    @FXML
    private void handleSelectImage() {
        File imageFile = ImageUtils.selectImageFile(dialogStage);
        
        if (imageFile != null) {
            // Valider l'image
            if (!ImageUtils.isValidImage(imageFile)) {
                showError("Le fichier sélectionné n'est pas une image valide");
                return;
            }
            
            // Vérifier la taille (max 5MB)
            if (!ImageUtils.isValidImageSize(imageFile, 5.0)) {
                showError("L'image est trop grande. Taille maximale: 5MB");
                return;
            }
            
            selectedImageFile = imageFile;
            imageNameLabel.setText(imageFile.getName());
            
            // Afficher l'aperçu
            Image image = new Image(imageFile.toURI().toString());
            imagePreview.setImage(image);
            imagePreview.setVisible(true);
            imagePreview.setManaged(true);
        }
    }

    @FXML
    private void handleCreate() {
        String name = nameField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (name.isEmpty()) {
            showError("Le nom du forum est obligatoire");
            return;
        }

        if (forumIdToEdit != null) {
            // Mode modification
            updateForum(name, description);
        } else {
            // Mode création
            createForum(name, description);
        }
    }

    private void createForum(String name, String description) {
        String imagePath = null;
        
        // Sauvegarder l'image si sélectionnée
        if (selectedImageFile != null) {
            System.out.println("=== CRÉATION FORUM AVEC IMAGE ===");
            System.out.println("Fichier sélectionné: " + selectedImageFile.getAbsolutePath());
            try {
                imagePath = ImageUtils.saveImage(selectedImageFile);
                System.out.println("Image sauvegardée à: " + imagePath);
            } catch (Exception e) {
                System.out.println("ERREUR lors de la sauvegarde de l'image:");
                e.printStackTrace();
                showError("Erreur lors de la sauvegarde de l'image");
                return;
            }
        } else {
            System.out.println("=== CRÉATION FORUM SANS IMAGE ===");
        }
        
        // Essayer d'abord avec image_url
        String queryWithImage = "INSERT INTO forums (title, description, creator_id, image_url, created_at) VALUES (?, ?, ?, ?, NOW())";
        String queryWithoutImage = "INSERT INTO forums (title, description, creator_id, created_at) VALUES (?, ?, ?, NOW())";

        try (Connection conn = getConnection()) {
            // Essayer d'abord avec la colonne image_url
            try (PreparedStatement stmt = conn.prepareStatement(queryWithImage)) {
                stmt.setString(1, name);
                stmt.setString(2, description);
                stmt.setLong(3, currentUserId);
                stmt.setString(4, imagePath);
                
                System.out.println("Tentative d'insertion avec image_url...");
                System.out.println("Query: " + queryWithImage);
                System.out.println("Valeurs: name=" + name + ", desc=" + description + ", userId=" + currentUserId + ", imagePath=" + imagePath);
                
                stmt.executeUpdate();
                
                System.out.println("✓ Forum créé avec succès AVEC colonne image_url");
                forumsController.refreshForums();
                dialogStage.close();
                return;
            } catch (SQLException e) {
                // Si la colonne image_url n'existe pas, essayer sans
                System.out.println("✗ Échec avec image_url: " + e.getMessage());
                System.out.println("Tentative sans colonne image_url...");
            }
            
            // Essayer sans la colonne image_url
            try (PreparedStatement stmt = conn.prepareStatement(queryWithoutImage)) {
                stmt.setString(1, name);
                stmt.setString(2, description);
                stmt.setLong(3, currentUserId);
                
                System.out.println("Query: " + queryWithoutImage);
                stmt.executeUpdate();
                
                System.out.println("✓ Forum créé avec succès SANS colonne image_url");
                System.out.println("⚠ ATTENTION: L'image n'a pas été sauvegardée en base!");
                forumsController.refreshForums();
                dialogStage.close();
            }

        } catch (SQLException e) {
            System.out.println("✗✗ ERREUR CRITIQUE lors de la création du forum:");
            e.printStackTrace();
            showError("Erreur lors de la création du forum: " + e.getMessage());
        }
    }

    private void updateForum(String name, String description) {
        String imagePath = existingImagePath;
        
        // Si une nouvelle image est sélectionnée
        if (selectedImageFile != null) {
            try {
                // Supprimer l'ancienne image si elle existe
                if (existingImagePath != null && !existingImagePath.isEmpty()) {
                    ImageUtils.deleteImage(existingImagePath);
                }
                // Sauvegarder la nouvelle image
                imagePath = ImageUtils.saveImage(selectedImageFile);
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur lors de la sauvegarde de l'image");
                return;
            }
        }
        
        // Essayer d'abord avec image_url
        String queryWithImage = "UPDATE forums SET title = ?, description = ?, image_url = ? WHERE id = ? AND creator_id = ?";
        String queryWithoutImage = "UPDATE forums SET title = ?, description = ? WHERE id = ? AND creator_id = ?";

        try (Connection conn = getConnection()) {
            int rowsAffected = 0;
            
            // Essayer d'abord avec la colonne image_url
            try (PreparedStatement stmt = conn.prepareStatement(queryWithImage)) {
                stmt.setString(1, name);
                stmt.setString(2, description);
                stmt.setString(3, imagePath);
                stmt.setLong(4, forumIdToEdit);
                stmt.setLong(5, currentUserId);
                rowsAffected = stmt.executeUpdate();
            } catch (SQLException e) {
                // Si la colonne image_url n'existe pas, essayer sans
                System.out.println("Colonne image_url non trouvée, modification sans image...");
                
                try (PreparedStatement stmt = conn.prepareStatement(queryWithoutImage)) {
                    stmt.setString(1, name);
                    stmt.setString(2, description);
                    stmt.setLong(3, forumIdToEdit);
                    stmt.setLong(4, currentUserId);
                    rowsAffected = stmt.executeUpdate();
                }
            }

            if (rowsAffected > 0) {
                forumsController.refreshForums();
                dialogStage.close();
            } else {
                showError("Impossible de modifier ce forum");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la modification du forum: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private Connection getConnection() throws SQLException {
        return org.esprit.finovate.utils.DatabaseConfig.getConnection();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


