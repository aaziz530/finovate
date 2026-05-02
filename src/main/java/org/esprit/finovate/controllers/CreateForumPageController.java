package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.esprit.finovate.entities.Forum;
import org.esprit.finovate.services.ForumService;
import org.esprit.finovate.utils.SessionManager;

public class CreateForumPageController {

    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private Label nameError;
    @FXML private Label descriptionError;
    @FXML private Button createBtn;
    @FXML private Button cancelBtn;
    @FXML private Button backBtn;

    private MainController mainController;

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

    @FXML
    private void handleCreate() {
        // Reset errors
        nameError.setVisible(false);
        nameError.setManaged(false);
        descriptionError.setVisible(false);
        descriptionError.setManaged(false);

        String name = nameField.getText().trim();
        String description = descriptionField.getText().trim();

        // Validation
        boolean hasError = false;

        if (name.isEmpty() || name.length() < 3) {
            nameError.setText("Le titre doit contenir au moins 3 caractères");
            nameError.setVisible(true);
            nameError.setManaged(true);
            hasError = true;
        }

        if (description.isEmpty() || description.length() < 10) {
            descriptionError.setText("La description doit contenir au moins 10 caractères");
            descriptionError.setVisible(true);
            descriptionError.setManaged(true);
            hasError = true;
        }

        if (hasError) {
            return;
        }

        try {
            long userId = SessionManager.getCurrentUser().getId();
            Forum forum = new Forum(userId, name, description);
            
            ForumService forumService = new ForumService();
            boolean success = forumService.createForum(forum, false);

            if (success) {
                showSuccess("Forum créé avec succès!");
                // Return to forums page
                if (mainController != null) {
                    mainController.showForums();
                }
            } else {
                showError("Erreur lors de la création du forum");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        if (mainController != null) {
            mainController.showForums();
        }
    }

    @FXML
    private void handleBack() {
        if (mainController != null) {
            mainController.showForums();
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
