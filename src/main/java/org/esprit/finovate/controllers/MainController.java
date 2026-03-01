package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import org.esprit.finovate.services.PersonalityService;
import org.esprit.finovate.utils.Session;

import java.io.IOException;

public class MainController {

    @FXML private StackPane mainContentArea;
    @FXML private Button forumsMenuBtn;
    @FXML private Button personalityMenuBtn;
    @FXML private Button recommendationsMenuBtn;
    @FXML private Button alertsMenuBtn;

    private String currentView = "forums";
    private int currentUserId;

    @FXML
    public void initialize() {
        // Get current user ID from Session
        if (Session.currentUser != null) {
            this.currentUserId = Session.currentUser.getId().intValue();
        } else {
            this.currentUserId = 1; // Fallback
        }
        
        // Charger la page Forums par défaut
        showForumsPage();
        
        // Ajouter les effets hover pour les boutons AI
        setupAIButtonsHoverEffects();
    }
    
    private void setupAIButtonsHoverEffects() {
        // Personality button hover
        if (personalityMenuBtn != null) {
            personalityMenuBtn.setOnMouseEntered(e -> {
                personalityMenuBtn.setStyle(
                    "-fx-background-color: #E1BEE7; " +
                    "-fx-text-fill: #9C27B0; " +
                    "-fx-font-size: 24px; " +
                    "-fx-padding: 12; " +
                    "-fx-background-radius: 10; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0; " +
                    "-fx-pref-width: 55; " +
                    "-fx-pref-height: 55; " +
                    "-fx-effect: dropshadow(gaussian, rgba(156, 39, 176, 0.3), 8, 0, 0, 2);"
                );
            });
            personalityMenuBtn.setOnMouseExited(e -> {
                personalityMenuBtn.setStyle(
                    "-fx-background-color: #F3E5F5; " +
                    "-fx-text-fill: #9C27B0; " +
                    "-fx-font-size: 24px; " +
                    "-fx-padding: 12; " +
                    "-fx-background-radius: 10; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0; " +
                    "-fx-pref-width: 55; " +
                    "-fx-pref-height: 55;"
                );
            });
        }
        
        // Recommendations button hover
        if (recommendationsMenuBtn != null) {
            recommendationsMenuBtn.setOnMouseEntered(e -> {
                recommendationsMenuBtn.setStyle(
                    "-fx-background-color: #FFF59D; " +
                    "-fx-text-fill: #F57C00; " +
                    "-fx-font-size: 24px; " +
                    "-fx-padding: 12; " +
                    "-fx-background-radius: 10; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0; " +
                    "-fx-pref-width: 55; " +
                    "-fx-pref-height: 55; " +
                    "-fx-effect: dropshadow(gaussian, rgba(245, 124, 0, 0.3), 8, 0, 0, 2);"
                );
            });
            recommendationsMenuBtn.setOnMouseExited(e -> {
                recommendationsMenuBtn.setStyle(
                    "-fx-background-color: #FFF9C4; " +
                    "-fx-text-fill: #F57C00; " +
                    "-fx-font-size: 24px; " +
                    "-fx-padding: 12; " +
                    "-fx-background-radius: 10; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0; " +
                    "-fx-pref-width: 55; " +
                    "-fx-pref-height: 55;"
                );
            });
        }
        
        // Alerts button hover
        if (alertsMenuBtn != null) {
            alertsMenuBtn.setOnMouseEntered(e -> {
                alertsMenuBtn.setStyle(
                    "-fx-background-color: #EF9A9A; " +
                    "-fx-text-fill: #F44336; " +
                    "-fx-font-size: 24px; " +
                    "-fx-padding: 12; " +
                    "-fx-background-radius: 10; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0; " +
                    "-fx-pref-width: 55; " +
                    "-fx-pref-height: 55; " +
                    "-fx-effect: dropshadow(gaussian, rgba(244, 67, 54, 0.3), 8, 0, 0, 2);"
                );
            });
            alertsMenuBtn.setOnMouseExited(e -> {
                alertsMenuBtn.setStyle(
                    "-fx-background-color: #FFEBEE; " +
                    "-fx-text-fill: #F44336; " +
                    "-fx-font-size: 24px; " +
                    "-fx-padding: 12; " +
                    "-fx-background-radius: 10; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0; " +
                    "-fx-pref-width: 55; " +
                    "-fx-pref-height: 55;"
                );
            });
        }
    }

    @FXML
    private void showForumsPage() {
        currentView = "forums";
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forums-page.fxml"));
            Parent forumsPage = loader.load();

            ForumsPageController controller = loader.getController();
            controller.setMainController(this);

            mainContentArea.getChildren().clear();
            mainContentArea.getChildren().add(forumsPage);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement de la page forums");
        }
    }

    @FXML
    private void showBadgesPage() {
        // Cette méthode n'est plus nécessaire car les badges sont maintenant dans la navbar
    }
    
    public void showMyPersonality() {
        currentView = "personality";
        
        // Afficher un dialogue de chargement
        Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle("Analyse en cours");
        loadingAlert.setHeaderText("🎭 Analyse de Personnalité");
        loadingAlert.setContentText("⏳ Analyse de votre personnalité en cours...\nCela peut prendre quelques secondes.");
        loadingAlert.show();
        
        // Lancer l'analyse en arrière-plan
        javafx.concurrent.Task<PersonalityService.PersonalityAnalysis> analysisTask =
            new javafx.concurrent.Task<>() {
            @Override
            protected PersonalityService.PersonalityAnalysis call() {
                // Récupérer le contenu de l'utilisateur
                StringBuilder userContent = new StringBuilder();
                
                try (java.sql.Connection conn = getConnection()) {
                    // Récupérer les posts
                    String postsQuery = "SELECT title, content FROM posts WHERE author_id = ? ORDER BY created_at DESC LIMIT 20";
                    try (java.sql.PreparedStatement stmt = conn.prepareStatement(postsQuery)) {
                        stmt.setInt(1, currentUserId);
                        java.sql.ResultSet rs = stmt.executeQuery();
                        while (rs.next()) {
                            userContent.append(rs.getString("title")).append("\n");
                            userContent.append(rs.getString("content")).append("\n\n");
                        }
                    }
                    
                    // Récupérer les commentaires
                    String commentsQuery = "SELECT content FROM comments WHERE author_id = ? ORDER BY created_at DESC LIMIT 30";
                    try (java.sql.PreparedStatement stmt = conn.prepareStatement(commentsQuery)) {
                        stmt.setInt(1, currentUserId);
                        java.sql.ResultSet rs = stmt.executeQuery();
                        while (rs.next()) {
                            userContent.append(rs.getString("content")).append("\n");
                        }
                    }
                } catch (java.sql.SQLException e) {
                    e.printStackTrace();
                }
                
                if (userContent.length() == 0) {
                    userContent.append("Utilisateur sans contenu suffisant pour l'analyse.");
                }
                
                return PersonalityService.analyzePersonality(userContent.toString());
            }
        };
        
        analysisTask.setOnSucceeded(e -> {
            loadingAlert.close();
            PersonalityService.PersonalityAnalysis analysis = analysisTask.getValue();
            
            // Récupérer le nom d'utilisateur
            String username = getUsernameById(currentUserId);
            
            // Créer le dialogue d'analyse
            showPersonalityDialog(analysis, username);
        });
        
        analysisTask.setOnFailed(e -> {
            loadingAlert.close();
            showError("❌ Erreur lors de l'analyse de personnalité");
        });
        
        new Thread(analysisTask).start();
    }
    
    private void showPersonalityDialog(PersonalityService.PersonalityAnalysis analysis, String username) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Analyse de Personnalité");
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setMinWidth(600);
        dialogPane.setMaxWidth(600);
        dialogPane.setMinHeight(500);
        dialogPane.setMaxHeight(550);
        
        javafx.scene.layout.VBox mainContainer = new javafx.scene.layout.VBox(20);
        mainContainer.setPadding(new javafx.geometry.Insets(25));
        mainContainer.setStyle("-fx-background-color: white;");
        
        // Header
        javafx.scene.layout.VBox header = new javafx.scene.layout.VBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER);
        header.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #F3E5F5, #E1BEE7); " +
            "-fx-padding: 20; " +
            "-fx-background-radius: 10;"
        );
        
        Label iconLabel = new Label(analysis.emoji);
        iconLabel.setStyle("-fx-font-size: 56px;");
        
        Label headerTitle = new Label(analysis.personalityType);
        headerTitle.setWrapText(true);
        headerTitle.setStyle(
            "-fx-font-size: 20px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #9C27B0; " +
            "-fx-text-alignment: center;"
        );
        headerTitle.setMaxWidth(Double.MAX_VALUE);
        
        Label usernameLabel = new Label("👤 " + username);
        usernameLabel.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #666;"
        );
        
        header.getChildren().addAll(iconLabel, headerTitle, usernameLabel);
        
        // Title section
        javafx.scene.layout.VBox titleSection = new javafx.scene.layout.VBox(8);
        titleSection.setStyle(
            "-fx-background-color: #FFF9C4; " +
            "-fx-padding: 12; " +
            "-fx-background-radius: 8;"
        );
        
        Label titleLabel = new Label("🏆 " + analysis.title);
        titleLabel.setWrapText(true);
        titleLabel.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #F57C00;"
        );
        
        titleSection.getChildren().add(titleLabel);
        
        // Description
        javafx.scene.layout.VBox descSection = new javafx.scene.layout.VBox(10);
        descSection.setStyle(
            "-fx-background-color: #F5F5F5; " +
            "-fx-padding: 15; " +
            "-fx-background-radius: 8;"
        );
        
        Label descLabel = new Label(analysis.description);
        descLabel.setWrapText(true);
        descLabel.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #333; " +
            "-fx-line-spacing: 3px;"
        );
        
        descSection.getChildren().add(descLabel);
        
        // Sentiment scores
        javafx.scene.layout.VBox sentimentSection = new javafx.scene.layout.VBox(10);
        sentimentSection.setStyle(
            "-fx-background-color: #E8F5E9; " +
            "-fx-padding: 15; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: #4CAF50; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 8;"
        );
        
        Label sentimentTitle = new Label("📊 Analyse de Sentiment");
        sentimentTitle.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2E7D32;"
        );
        
        Label positiveLabel = new Label(String.format("😊 Positif: %.0f%%", analysis.positiveScore * 100));
        positiveLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #4CAF50;");
        
        Label neutralLabel = new Label(String.format("😐 Neutre: %.0f%%", analysis.neutralScore * 100));
        neutralLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #FF9800;");
        
        Label negativeLabel = new Label(String.format("😔 Négatif: %.0f%%", analysis.negativeScore * 100));
        negativeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #F44336;");
        
        sentimentSection.getChildren().addAll(sentimentTitle, positiveLabel, neutralLabel, negativeLabel);
        
        // Traits
        if (!analysis.traits.isEmpty()) {
            javafx.scene.layout.VBox traitsSection = new javafx.scene.layout.VBox(8);
            traitsSection.setStyle(
                "-fx-background-color: #E3F2FD; " +
                "-fx-padding: 12; " +
                "-fx-background-radius: 8;"
            );
            
            Label traitsTitle = new Label("✨ Traits de Personnalité");
            traitsTitle.setStyle(
                "-fx-font-size: 13px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #1976D2;"
            );
            
            traitsSection.getChildren().add(traitsTitle);
            
            for (String trait : analysis.traits) {
                Label traitLabel = new Label("• " + trait);
                traitLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");
                traitsSection.getChildren().add(traitLabel);
            }
            
            mainContainer.getChildren().addAll(header, titleSection, descSection, sentimentSection, traitsSection);
        } else {
            mainContainer.getChildren().addAll(header, titleSection, descSection, sentimentSection);
        }
        
        // Footer
        Label footerInfo = new Label("💡 Analyse générée par intelligence artificielle");
        footerInfo.setStyle(
            "-fx-font-size: 11px; " +
            "-fx-text-fill: #999; " +
            "-fx-font-style: italic;"
        );
        mainContainer.getChildren().add(footerInfo);
        
        dialogPane.setContent(mainContainer);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        
        Button closeButton = (Button) dialogPane.lookupButton(ButtonType.CLOSE);
        closeButton.setText("Fermer");
        closeButton.setStyle(
            "-fx-background-color: #9C27B0; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 10 30; " +
            "-fx-background-radius: 8;"
        );
        
        dialog.showAndWait();
    }
    
    private String getUsernameById(int userId) {
        try (java.sql.Connection conn = getConnection()) {
            String query = "SELECT CONCAT(firstname, ' ', lastname) as username FROM user WHERE id = ?";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                java.sql.ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return "Utilisateur";
    }
    
    private java.sql.Connection getConnection() throws java.sql.SQLException {
        String url = "jdbc:mysql://localhost:3306/fintechforum";
        String user = "root";
        String password = "";
        return java.sql.DriverManager.getConnection(url, user, password);
    }

    // Méthodes appelées depuis ForumsPageController
    public void showRecommendations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recommendations-view.fxml"));
            Parent recommendationsView = loader.load();

            RecommendationsController controller = loader.getController();
            controller.setMainController(this);
            controller.loadRecommendations(currentUserId);

            mainContentArea.getChildren().clear();
            mainContentArea.getChildren().add(recommendationsView);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement des recommandations");
        }
    }

    public void showAlerts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/alerts-view.fxml"));
            Parent alertsView = loader.load();

            AlertsController controller = loader.getController();
            controller.setMainController(this);
            controller.loadAlerts(currentUserId);

            mainContentArea.getChildren().clear();
            mainContentArea.getChildren().add(alertsView);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement des alertes");
        }
    }

    // Méthodes appelées depuis ForumsController
    public void showPostsView(int forumId, String forumName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/posts.fxml"));
            Parent postsView = loader.load();

            PostsController controller = loader.getController();
            controller.setMainController(this);
            controller.loadPosts(forumId, forumName, currentUserId);

            mainContentArea.getChildren().clear();
            mainContentArea.getChildren().add(postsView);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement des posts");
        }
    }

    public void showPostDetailsView(int postId, String postTitle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post-details.fxml"));
            Parent postDetailsView = loader.load();

            PostDetailsController controller = loader.getController();
            controller.setMainController(this);
            controller.loadPostDetails(postId, currentUserId);

            mainContentArea.getChildren().clear();
            mainContentArea.getChildren().add(postDetailsView);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement du post");
        }
    }

    public void goBackToForums() {
        showForumsPage();
    }

    public void showPostDetails(int postId, int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post-details.fxml"));
            Parent postDetailsView = loader.load();

            PostDetailsController controller = loader.getController();
            controller.setMainController(this);
            controller.loadPostDetails(postId, userId);

            mainContentArea.getChildren().clear();
            mainContentArea.getChildren().add(postDetailsView);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement du post");
        }
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Show Create Forum Page
    public void showCreateForumPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/create-forum-page.fxml"));
            Parent createForumPage = loader.load();

            CreateForumPageController controller = loader.getController();
            controller.setMainController(this);

            mainContentArea.getChildren().clear();
            mainContentArea.getChildren().add(createForumPage);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement de la page de création de forum");
        }
    }

    // Show Create Post Page
    public void showCreatePostPage(Long forumId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/create-post-page.fxml"));
            Parent createPostPage = loader.load();

            CreatePostPageController controller = loader.getController();
            controller.setMainController(this);
            controller.setForumId(forumId.intValue());

            mainContentArea.getChildren().clear();
            mainContentArea.getChildren().add(createPostPage);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement de la page de création de post");
        }
    }

    // Show Forums (for navigation)
    public void showForums() {
        showForumsPage();
    }

    // Show Posts (for navigation)
    public void showPosts(Long forumId) {
        showPostsView(forumId.intValue(), "");
    }
}
