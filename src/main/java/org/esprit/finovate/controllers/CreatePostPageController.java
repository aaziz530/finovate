package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.esprit.finovate.entities.Post;
import org.esprit.finovate.services.PostService;
import org.esprit.finovate.utils.SessionManager;

public class CreatePostPageController {

    @FXML private TextField titleField;
    @FXML private TextArea contentField;
    @FXML private Label titleError;
    @FXML private Label contentError;
    @FXML private Button createBtn;
    @FXML private Button cancelBtn;
    @FXML private Button backBtn;

    private MainController mainController;
    private int forumId;

    @FXML
    public void initialize() {
        // Ajouter animations hover et press au bouton retour
        setupBackButtonAnimations();
    }

    private void setupBackButtonAnimations() {
        if (backBtn != null) {
            // Animation hover - déplace vers la gauche
            backBtn.setOnMouseEntered(e -> {
                javafx.animation.TranslateTransition slide = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(200), backBtn);
                slide.setToX(-5);
                slide.play();
            });
            
            backBtn.setOnMouseExited(e -> {
                javafx.animation.TranslateTransition slide = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(200), backBtn);
                slide.setToX(0);
                slide.play();
            });
            
            // Animation press - effet de rebond
            backBtn.setOnMousePressed(e -> {
                javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(100), backBtn);
                scale.setToX(0.9);
                scale.setToY(0.9);
                scale.play();
            });
            
            backBtn.setOnMouseReleased(e -> {
                javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(100), backBtn);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();
            });
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setForumId(int forumId) {
        this.forumId = forumId;
    }

    @FXML
    private void handleCreate() {
        // Reset errors
        titleError.setVisible(false);
        titleError.setManaged(false);
        contentError.setVisible(false);
        contentError.setManaged(false);

        String title = titleField.getText().trim();
        String content = contentField.getText().trim();

        // Validation
        boolean hasError = false;

        if (title.isEmpty() || title.length() < 5) {
            titleError.setText("Le titre doit contenir au moins 5 caractères");
            titleError.setVisible(true);
            titleError.setManaged(true);
            hasError = true;
        }

        if (content.isEmpty() || content.length() < 20) {
            contentError.setText("Le contenu doit contenir au moins 20 caractères");
            contentError.setVisible(true);
            contentError.setManaged(true);
            hasError = true;
        }

        if (hasError) {
            return;
        }

        try {
            Long userId = SessionManager.getCurrentUser().getId();
            Post post = new Post(forumId, title, content, userId);
            
            PostService postService = new PostService();
            boolean success = postService.createPost(post, false);

            if (success) {
                showSuccess("Post publié avec succès!");
                // Return to posts page
                if (mainController != null) {
                    mainController.showPosts(Long.valueOf(forumId));
                }
            } else {
                showError("Erreur lors de la publication du post");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        if (mainController != null) {
            mainController.showPosts(Long.valueOf(forumId));
        }
    }

    @FXML
    private void handleBack() {
        if (mainController != null) {
            mainController.showPosts(Long.valueOf(forumId));
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
