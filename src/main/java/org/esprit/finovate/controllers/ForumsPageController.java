package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import org.esprit.finovate.services.PersonalityService;
import org.esprit.finovate.utils.Session;

import java.io.IOException;

public class ForumsPageController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortBox;
    @FXML private Button accueilBtn;
    @FXML private Button mesForumsBtn;
    @FXML private Button forumsRejointsBtn;
    @FXML private Button postesPartagesBtn;
    @FXML private Button badgesBtn;
    @FXML private Button aiGeneratorBtn;
    @FXML private Button personalityBtn;
    @FXML private Button recommendationsBtn;
    @FXML private Button alertesBtn;
    @FXML private StackPane contentArea;

    private MainController mainController; // Optional - for backward compatibility
    private String currentView = "accueil";
    private int currentUserId;
    private ForumsController currentForumsController;

    @FXML
    public void initialize() {
        // Get current user ID from Session
        if (Session.currentUser != null) {
            this.currentUserId = Session.currentUser.getId().intValue();
        } else {
            this.currentUserId = 1; // Fallback
        }
        
        // Initialiser le ComboBox de tri
        sortBox.setItems(FXCollections.observableArrayList(
                "Plus récents",
                "Plus anciens",
                "Plus populaires",
                "A-Z",
                "Z-A"
        ));
        sortBox.setValue("Plus récents");

        // Gestionnaire de recherche
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            onSearch(newVal);
        });

        // Gestionnaire de tri
        sortBox.setOnAction(e -> onSortChange());

        // Charger la vue par défaut immédiatement
        showAccueil();
    }

    /**
     * Optional method for backward compatibility with MainController
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    
    public void goBackToForums() {
        loadForumsView("accueil");
    }

    @FXML
    private void showAccueil() {
        currentView = "accueil";
        updateNavButtonStyles(accueilBtn);
        loadForumsView("accueil");
    }

    @FXML
    private void showMyForums() {
        currentView = "myForums";
        updateNavButtonStyles(mesForumsBtn);
        loadForumsView("myForums");
    }

    @FXML
    private void showJoinedForums() {
        currentView = "joinedForums";
        updateNavButtonStyles(forumsRejointsBtn);
        loadForumsView("joinedForums");
    }

    @FXML
    private void showSharedPosts() {
        currentView = "sharedPosts";
        updateNavButtonStyles(postesPartagesBtn);
        loadSharedPostsView();
    }

    @FXML
    private void showBadges() {
        currentView = "badges";
        updateNavButtonStyles(badgesBtn);
        loadBadgesView();
    }

    @FXML
    private void showAIGenerator() {
        currentView = "aiGenerator";
        updateNavButtonStyles(aiGeneratorBtn);
        loadAIGeneratorView();
    }

    @FXML
    private void showPersonality() {
        currentView = "personality";
        updateNavButtonStyles(personalityBtn);
        loadPersonalityView();
    }

    @FXML
    private void showRecommendations() {
        currentView = "recommendations";
        updateNavButtonStyles(recommendationsBtn);
        loadRecommendationsView();
    }

    @FXML
    private void showAlerts() {
        currentView = "alerts";
        updateNavButtonStyles(alertesBtn);
        loadAlertsView();
    }

    private void loadForumsView(String viewType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forums.fxml"));
            Parent forumsView = loader.load();

            ForumsController controller = loader.getController();
            // Pass mainController if available (optional)
            if (mainController != null) {
                controller.setMainController(mainController);
            }
            // Pass this controller and contentArea for direct navigation
            controller.setForumsPageController(this, contentArea);
            controller.loadForums(viewType, currentUserId);
            
            currentForumsController = controller;

            contentArea.getChildren().clear();
            contentArea.getChildren().add(forumsView);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement de la vue forums");
        }
    }

    private void loadSharedPostsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/shared-posts.fxml"));
            Parent sharedPostsView = loader.load();

            SharedPostsController controller = loader.getController();
            // Pass mainController if available (optional)
            if (mainController != null) {
                controller.setMainController(mainController);
            }
            controller.loadSharedPosts(currentUserId);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(sharedPostsView);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement des postes partagés");
        }
    }

    private void loadBadgesView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/badges-view.fxml"));
            Parent badgesView = loader.load();

            BadgesViewController controller = loader.getController();
            // Pass mainController if available (optional)
            if (mainController != null) {
                controller.setMainController(mainController);
            }
            controller.loadBadges(currentUserId);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(badgesView);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement des badges");
        }
    }

    private void loadAIGeneratorView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ai-post-generator-view.fxml"));
            Parent aiGeneratorView = loader.load();

            AIPostGeneratorController controller = loader.getController();
            // Pass mainController if available (optional)
            if (mainController != null) {
                controller.setMainController(mainController);
            }
            controller.loadUserForums(); // Charger les forums après avoir défini le mainController

            contentArea.getChildren().clear();
            contentArea.getChildren().add(aiGeneratorView);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement du générateur IA");
        }
    }

    private void loadPersonalityView() {
        try {
            // Show loading in contentArea
            VBox loadingBox = new VBox(20);
            loadingBox.setAlignment(javafx.geometry.Pos.CENTER);
            loadingBox.setStyle("-fx-background-color: #F8F9FA; -fx-padding: 50;");
            
            Label loadingLabel = new Label("🎭 Analyse de Personnalité");
            loadingLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #9C27B0;");
            
            Label loadingText = new Label("⏳ Analyse en cours...\nCela peut prendre quelques secondes.");
            loadingText.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-text-alignment: center;");
            loadingText.setWrapText(true);
            
            loadingBox.getChildren().addAll(loadingLabel, loadingText);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(loadingBox);
            
            // Launch analysis in background
            javafx.concurrent.Task<PersonalityService.PersonalityAnalysis> analysisTask = 
                new javafx.concurrent.Task<>() {
                @Override
                protected PersonalityService.PersonalityAnalysis call() {
                    StringBuilder userContent = new StringBuilder();
                    
                    try (java.sql.Connection conn = getConnection()) {
                        // Get posts
                        String postsQuery = "SELECT title, content FROM posts WHERE author_id = ? ORDER BY created_at DESC LIMIT 20";
                        try (java.sql.PreparedStatement stmt = conn.prepareStatement(postsQuery)) {
                            stmt.setInt(1, currentUserId);
                            java.sql.ResultSet rs = stmt.executeQuery();
                            while (rs.next()) {
                                userContent.append(rs.getString("title")).append("\n");
                                userContent.append(rs.getString("content")).append("\n\n");
                            }
                        }
                        
                        // Get comments
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
                try {
                    PersonalityService.PersonalityAnalysis analysis = analysisTask.getValue();
                    String username = getUsernameById(currentUserId);
                    
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/personality-view.fxml"));
                    Parent personalityView = loader.load();
                    
                    PersonalityViewController controller = loader.getController();
                    controller.setMainController(mainController);
                    controller.displayAnalysis(analysis, username);
                    
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(personalityView);
                    
                } catch (IOException ex) {
                    ex.printStackTrace();
                    showError("Erreur de chargement de la vue personnalité");
                }
            });
            
            analysisTask.setOnFailed(e -> {
                showError("❌ Erreur lors de l'analyse de personnalité");
            });
            
            new Thread(analysisTask).start();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de l'analyse");
        }
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
        return org.esprit.finovate.utils.DatabaseConfig.getConnection();
    }

    private void loadRecommendationsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recommendations-view.fxml"));
            Parent recommendationsView = loader.load();

            RecommendationsController controller = loader.getController();
            // Pass mainController if available (optional)
            if (mainController != null) {
                controller.setMainController(mainController);
            }
            controller.loadRecommendations(currentUserId);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(recommendationsView);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement des recommandations");
        }
    }

    private void loadAlertsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/alerts-view.fxml"));
            Parent alertsView = loader.load();

            AlertsController controller = loader.getController();
            // Pass mainController if available (optional)
            if (mainController != null) {
                controller.setMainController(mainController);
            }
            controller.loadAlerts(currentUserId);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(alertsView);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement des alertes");
        }
    }

    private void updateNavButtonStyles(Button activeButton) {
        String inactiveStyle = "-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 600; -fx-padding: 12 24; -fx-background-radius: 20; -fx-cursor: hand;";
        String activeStyle = "-fx-background-color: white; -fx-text-fill: #4CAF50; -fx-font-size: 14px; -fx-font-weight: 700; -fx-padding: 12 24; -fx-background-radius: 20; -fx-cursor: hand;";
        String inactiveIconStyle = "-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; -fx-font-size: 20px; -fx-padding: 10; -fx-background-radius: 50%; -fx-cursor: hand; -fx-min-width: 45; -fx-min-height: 45;";
        String activeIconStyle = "-fx-background-color: white; -fx-text-fill: #4CAF50; -fx-font-size: 20px; -fx-padding: 10; -fx-background-radius: 50%; -fx-cursor: hand; -fx-min-width: 45; -fx-min-height: 45;";

        accueilBtn.setStyle(inactiveStyle);
        mesForumsBtn.setStyle(inactiveStyle);
        forumsRejointsBtn.setStyle(inactiveStyle);
        postesPartagesBtn.setStyle(inactiveStyle);
        badgesBtn.setStyle(inactiveStyle);
        aiGeneratorBtn.setStyle(inactiveStyle);
        personalityBtn.setStyle(inactiveIconStyle);
        recommendationsBtn.setStyle(inactiveIconStyle);
        alertesBtn.setStyle(inactiveIconStyle);

        // Appliquer le style actif selon le type de bouton
        if (activeButton == personalityBtn || activeButton == recommendationsBtn || activeButton == alertesBtn) {
            activeButton.setStyle(activeIconStyle);
        } else {
            activeButton.setStyle(activeStyle);
        }
    }

    private void onSearch(String searchText) {
        if (currentForumsController != null) {
            currentForumsController.applySearch(searchText);
        }
    }

    private void onSortChange() {
        String sortType = sortBox.getValue();
        if (currentForumsController != null) {
            currentForumsController.applySort(sortType);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
