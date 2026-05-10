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
import org.esprit.finovate.utils.BadgeManager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreatePostDialogController {

    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private Button selectImageBtn;
    @FXML private Label imageNameLabel;
    @FXML private ImageView imagePreview;

    private Stage dialogStage;
    private PostsController postsController;
    private long forumId;
    private long authorId;
    private Long postIdToEdit = null; // null = création, sinon = modification
    private File selectedImageFile = null;
    private String existingImagePath = null;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setPostsController(PostsController postsController) {
        this.postsController = postsController;
    }

    public void setForumId(long forumId) {
        this.forumId = forumId;
    }

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }
    
    public void setPostToEdit(long postId, String title, String content, String imagePath) {
        this.postIdToEdit = postId;
        this.existingImagePath = imagePath;
        titleField.setText(title);
        contentArea.setText(content);
        
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
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            showError("Le titre et le contenu sont obligatoires");
            return;
        }

        if (postIdToEdit != null) {
            // Mode modification
            updatePost(title, content);
        } else {
            // Mode création
            createPost(title, content);
        }
    }
    
    private void createPost(String title, String content) {
        System.out.println("=== CREATE POST DIALOG ===");
        System.out.println("Forum ID: " + forumId);
        System.out.println("Author ID: " + authorId);
        System.out.println("Title: " + title);
        
        String imagePath = null;
        
        // Sauvegarder l'image si sélectionnée
        if (selectedImageFile != null) {
            try {
                imagePath = ImageUtils.saveImage(selectedImageFile);
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur lors de la sauvegarde de l'image");
                return;
            }
        }

        try (Connection conn = getConnection()) {
            System.out.println("Connection established");
            
            // VÉRIFICATION DE SÉCURITÉ: L'utilisateur doit avoir créé ou rejoint le forum
            // Utiliser UNION pour combiner les deux sources
            String checkPermissionQuery = "SELECT 1 FROM forums f " +
                                         "WHERE f.id = ? AND f.creator_id = ? " +
                                         "UNION " +
                                         "SELECT 1 FROM forums f " +
                                         "INNER JOIN user_forum uf ON f.id = uf.forum_id " +
                                         "WHERE f.id = ? AND uf.user_id = ?";
            
            try (PreparedStatement checkStmt = conn.prepareStatement(checkPermissionQuery)) {
                checkStmt.setLong(1, forumId);
                checkStmt.setLong(2, authorId);
                checkStmt.setLong(3, forumId);
                checkStmt.setLong(4, authorId);
                java.sql.ResultSet permRs = checkStmt.executeQuery();
                
                if (!permRs.next()) {
                    System.out.println("Permission denied - not a member");
                    showError("Vous devez être membre du forum pour créer un post");
                    return;
                }
                System.out.println("Permission OK");
            }
            
            // Essayer d'abord avec image_url
            String queryWithImage = "INSERT INTO posts (forum_id, title, content, author_id, image_url, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
            String queryWithoutImage = "INSERT INTO posts (forum_id, title, content, author_id, created_at, updated_at) VALUES (?, ?, ?, ?, NOW(), NOW())";

            // Essayer d'abord avec la colonne image_url
            try (PreparedStatement stmt = conn.prepareStatement(queryWithImage)) {
                stmt.setLong(1, forumId);
                stmt.setString(2, title);
                stmt.setString(3, content);
                stmt.setLong(4, authorId);
                stmt.setString(5, imagePath);
                int rows = stmt.executeUpdate();
                
                System.out.println("Post created with image! Rows: " + rows);
                
                // Vérifier et attribuer les badges
                System.out.println("Calling BadgeManager.checkPostBadges...");
                BadgeManager.checkPostBadges(authorId);
                System.out.println("BadgeManager.checkPostBadges completed");
                
                postsController.refreshPosts();
                dialogStage.close();
                System.out.println("=== END CREATE POST DIALOG ===");
                return;
            } catch (SQLException e) {
                // Si la colonne image_url n'existe pas, essayer sans
                System.out.println("Colonne image_url non trouvée, création sans image...");
            }
            
            // Essayer sans la colonne image_url
            try (PreparedStatement stmt = conn.prepareStatement(queryWithoutImage)) {
                stmt.setLong(1, forumId);
                stmt.setString(2, title);
                stmt.setString(3, content);
                stmt.setLong(4, authorId);
                int rows = stmt.executeUpdate();
                
                System.out.println("Post created without image! Rows: " + rows);
                
                // Vérifier et attribuer les badges
                System.out.println("Calling BadgeManager.checkPostBadges...");
                BadgeManager.checkPostBadges(authorId);
                System.out.println("BadgeManager.checkPostBadges completed");
                
                postsController.refreshPosts();
                dialogStage.close();
            }

        } catch (SQLException e) {
            System.out.println("SQL ERROR: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur lors de la création du post: " + e.getMessage());
        }
        
        System.out.println("=== END CREATE POST DIALOG ===");
    }
    
    private void updatePost(String title, String content) {
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
        
        String query = "UPDATE posts SET title = ?, content = ?, image_url = ?, updated_at = NOW() WHERE id = ? AND author_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, title);
            stmt.setString(2, content);
            stmt.setString(3, imagePath);
            stmt.setLong(4, postIdToEdit);
            stmt.setLong(5, authorId);
            
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                postsController.refreshPosts();
                dialogStage.close();
            } else {
                showError("Impossible de modifier ce post");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la modification du post: " + e.getMessage());
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