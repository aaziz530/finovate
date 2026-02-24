package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.ai.RecommendationEngine;
import org.example.ai.RecommendationEngine.RecommendedForum;

import java.sql.*;
import java.util.List;

/**
 * Contrôleur pour afficher les recommandations AI de forums
 */
public class RecommendationsController {

    @FXML private Label titleLabel;
    @FXML private Label emptyLabel;
    @FXML private ListView<RecommendedForum> recommendationsList;
    @FXML private Button refreshBtn;

    private MainController mainController;
    private int currentUserId;

    @FXML
    public void initialize() {
        recommendationsList.setCellFactory(param -> new RecommendationCell());
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void loadRecommendations(int userId) {
        this.currentUserId = userId;
        titleLabel.setText("🤖 Recommandations AI pour vous");
        
        // Calculer les recommandations
        RecommendationEngine.calculateRecommendations(userId);
        
        // Charger les recommandations
        loadRecommendationsFromEngine();
    }

    @FXML
    private void refreshRecommendations() {
        // Recalculer et recharger
        RecommendationEngine.calculateRecommendations(currentUserId);
        loadRecommendationsFromEngine();
        showInfo("Recommandations mises à jour !");
    }

    @FXML
    private void clearAllRecommendations() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer toutes les recommandations ?");
        confirmAlert.setContentText("Cela supprimera toutes les recommandations calculées. Vous pouvez les recalculer à tout moment.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                RecommendationEngine.clearAllRecommendations(currentUserId);
                loadRecommendationsFromEngine();
                showInfo("Toutes les recommandations ont été supprimées !");
            }
        });
    }

    private void loadRecommendationsFromEngine() {
        List<RecommendedForum> recommendations = RecommendationEngine.getRecommendations(currentUserId, 10);
        
        ObservableList<RecommendedForum> items = FXCollections.observableArrayList(recommendations);
        recommendationsList.setItems(items);
        
        emptyLabel.setVisible(items.isEmpty());
        emptyLabel.setManaged(items.isEmpty());
    }

    private void joinForum(int forumId) {
        String query = "INSERT INTO user_forum (forum_id, user_id, joined_at) VALUES (?, ?, NOW())";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, forumId);
            stmt.setInt(2, currentUserId);
            stmt.executeUpdate();

            // Tracker l'interaction
            RecommendationEngine.trackInteraction(currentUserId, forumId, 
                RecommendationEngine.InteractionType.CLICK);

            showInfo("Vous avez rejoint le forum !");
            loadRecommendationsFromEngine(); // Recharger

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                showError("Vous avez déjà rejoint ce forum");
            } else {
                e.printStackTrace();
                showError("Erreur lors de l'adhésion au forum");
            }
        }
    }

    private void viewForum(int forumId) {
        // Tracker l'interaction
        RecommendationEngine.trackInteraction(currentUserId, forumId, 
            RecommendationEngine.InteractionType.VIEW);
        
        // Ouvrir le forum
        mainController.showPostsView(forumId, "");
    }

    private class RecommendationCell extends ListCell<RecommendedForum> {
        @Override
        protected void updateItem(RecommendedForum forum, boolean empty) {
            super.updateItem(forum, empty);

            if (empty || forum == null) {
                setGraphic(null);
                setText(null);
            } else {
                VBox card = new VBox(12);
                card.setPadding(new Insets(20));
                card.setStyle(
                    "-fx-background-color: linear-gradient(to right, #E3F2FD, #F3E5F5);" +
                    "-fx-border-color: #1877F2;" +
                    "-fx-border-width: 2;" +
                    "-fx-border-radius: 12;" +
                    "-fx-background-radius: 12;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 10, 0, 0, 3);"
                );

                // Badge AI
                HBox headerBox = new HBox(10);
                headerBox.setAlignment(Pos.CENTER_LEFT);
                
                Label aiLabel = new Label("🤖 AI");
                aiLabel.setStyle(
                    "-fx-background-color: #1877F2;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 11px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 4 10;" +
                    "-fx-background-radius: 10;"
                );
                
                Label scoreLabel = new Label(String.format("Score: %.0f", forum.score));
                scoreLabel.setStyle(
                    "-fx-background-color: #4CAF50;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 11px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 4 10;" +
                    "-fx-background-radius: 10;"
                );
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                headerBox.getChildren().addAll(aiLabel, scoreLabel, spacer);

                // Nom du forum
                Label nameLabel = new Label(forum.name);
                nameLabel.setStyle(
                    "-fx-font-size: 20px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #1877F2;"
                );

                // Description
                Label descLabel = new Label(forum.description);
                descLabel.setWrapText(true);
                descLabel.setStyle(
                    "-fx-font-size: 14px;" +
                    "-fx-text-fill: #555;"
                );

                // Raison de la recommandation
                VBox reasonBox = new VBox(5);
                reasonBox.setPadding(new Insets(10));
                reasonBox.setStyle(
                    "-fx-background-color: rgba(255, 255, 255, 0.7);" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-color: #E1E8ED;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 8;"
                );
                
                Label reasonTitle = new Label("💡 Pourquoi cette recommandation ?");
                reasonTitle.setStyle(
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #666;"
                );
                
                Label reasonText = new Label(forum.reason);
                reasonText.setWrapText(true);
                reasonText.setStyle(
                    "-fx-font-size: 12px;" +
                    "-fx-text-fill: #888;"
                );
                
                reasonBox.getChildren().addAll(reasonTitle, reasonText);

                // Stats
                HBox statsBox = new HBox(20);
                statsBox.setAlignment(Pos.CENTER_LEFT);
                
                Label membersLabel = new Label("👥 " + forum.memberCount + " membres");
                membersLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
                
                Label postsLabel = new Label("📝 " + forum.recentPosts + " posts récents");
                postsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
                
                statsBox.getChildren().addAll(membersLabel, postsLabel);

                // Boutons d'action
                HBox actionBox = new HBox(10);
                actionBox.setAlignment(Pos.CENTER_LEFT);

                Button joinBtn = new Button("➕ Rejoindre");
                joinBtn.setStyle(
                    "-fx-background-color: #4CAF50;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 10 20;" +
                    "-fx-background-radius: 8;" +
                    "-fx-cursor: hand;"
                );
                joinBtn.setOnAction(e -> {
                    joinForum(forum.id);
                    e.consume();
                });

                actionBox.getChildren().add(joinBtn);

                card.getChildren().addAll(
                    headerBox,
                    nameLabel,
                    descLabel,
                    reasonBox,
                    statsBox,
                    actionBox
                );
                
                // Rendre la carte cliquable pour voir le forum
                card.setOnMouseClicked(e -> {
                    viewForum(forum.id);
                    e.consume();
                });
                card.setStyle(card.getStyle() + "; -fx-cursor: hand;");

                setGraphic(card);
            }
        }
    }

    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/fintechforum";
        String user = "root";
        String password = "";
        return DriverManager.getConnection(url, user, password);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
