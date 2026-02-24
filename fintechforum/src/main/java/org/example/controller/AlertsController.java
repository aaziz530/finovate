package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

/**
 * Contrôleur pour la page des alertes/notifications
 */
public class AlertsController {

    @FXML private Label titleLabel;
    @FXML private Label emptyLabel;
    @FXML private ListView<AlertItem> alertsList;
    @FXML private Button clearAllBtn;

    private MainController mainController;
    private int currentUserId;

    public static class AlertItem {
        private int postId;
        private String postTitle;
        private String forumName;
        private String authorName;
        private Timestamp createdAt;
        private boolean isRead;

        public AlertItem(int postId, String postTitle, String forumName, 
                        String authorName, Timestamp createdAt, boolean isRead) {
            this.postId = postId;
            this.postTitle = postTitle;
            this.forumName = forumName;
            this.authorName = authorName;
            this.createdAt = createdAt;
            this.isRead = isRead;
        }

        public int getPostId() { return postId; }
        public String getPostTitle() { return postTitle; }
        public String getForumName() { return forumName; }
        public String getAuthorName() { return authorName; }
        public Timestamp getCreatedAt() { return createdAt; }
        public boolean isRead() { return isRead; }
        public void setRead(boolean read) { this.isRead = read; }
    }

    @FXML
    public void initialize() {
        alertsList.setCellFactory(param -> new AlertCell());
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void loadAlerts(int userId) {
        this.currentUserId = userId;
        titleLabel.setText("🔔 Mes Alertes");
        loadAlertsFromDB();
    }

    private void loadAlertsFromDB() {
        ObservableList<AlertItem> alerts = FXCollections.observableArrayList();

        // Récupérer les nouveaux posts dans les forums rejoints
        String query = "SELECT p.id, p.title, p.created_at, u.username, f.name as forum_name " +
                "FROM posts p " +
                "INNER JOIN forums f ON p.forum_id = f.id " +
                "INNER JOIN users u ON p.author_id = u.id " +
                "INNER JOIN user_forum uf ON f.id = uf.forum_id " +
                "WHERE uf.user_id = ? " +
                "AND p.author_id != ? " +
                "AND p.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                "ORDER BY p.created_at DESC " +
                "LIMIT 50";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentUserId);
            stmt.setInt(2, currentUserId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                alerts.add(new AlertItem(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("forum_name"),
                        rs.getString("username"),
                        rs.getTimestamp("created_at"),
                        false
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de chargement des alertes");
        }

        alertsList.setItems(alerts);
        emptyLabel.setVisible(alerts.isEmpty());
        emptyLabel.setManaged(alerts.isEmpty());
        
        // Afficher/masquer le bouton "Tout effacer"
        clearAllBtn.setVisible(!alerts.isEmpty());
        clearAllBtn.setManaged(!alerts.isEmpty());
    }

    @FXML
    private void clearAllAlerts() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Effacer toutes les alertes ?");
        confirmAlert.setContentText("Cette action ne peut pas être annulée.");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            alertsList.getItems().clear();
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            clearAllBtn.setVisible(false);
            clearAllBtn.setManaged(false);
            showInfo("Toutes les alertes ont été effacées");
        }
    }

    private void openPost(int postId) {
        // Ouvrir le post (à implémenter selon votre navigation)
        mainController.showPostDetails(postId, currentUserId);
    }

    private void deleteAlert(AlertItem alert) {
        alertsList.getItems().remove(alert);
        
        if (alertsList.getItems().isEmpty()) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            clearAllBtn.setVisible(false);
            clearAllBtn.setManaged(false);
        }
    }

    private String getTimeAgo(Timestamp timestamp) {
        long diff = System.currentTimeMillis() - timestamp.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return "Il y a " + days + " jour" + (days > 1 ? "s" : "");
        } else if (hours > 0) {
            return "Il y a " + hours + " heure" + (hours > 1 ? "s" : "");
        } else if (minutes > 0) {
            return "Il y a " + minutes + " minute" + (minutes > 1 ? "s" : "");
        } else {
            return "À l'instant";
        }
    }

    private class AlertCell extends ListCell<AlertItem> {
        @Override
        protected void updateItem(AlertItem alert, boolean empty) {
            super.updateItem(alert, empty);

            if (empty || alert == null) {
                setGraphic(null);
                setText(null);
            } else {
                VBox card = new VBox(10);
                card.setPadding(new Insets(15));
                
                // Style différent si non lu
                if (!alert.isRead()) {
                    card.setStyle(
                        "-fx-background-color: #E3F2FD;" +
                        "-fx-border-color: #1877F2;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
                    );
                } else {
                    card.setStyle(
                        "-fx-background-color: white;" +
                        "-fx-border-color: #ddd;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
                    );
                }

                // Header avec icône et temps
                HBox headerBox = new HBox(10);
                headerBox.setAlignment(Pos.CENTER_LEFT);

                Label iconLabel = new Label("🔔");
                iconLabel.setStyle("-fx-font-size: 20px;");

                VBox infoBox = new VBox(3);
                
                Label forumLabel = new Label("📁 " + alert.getForumName());
                forumLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1877F2;");

                Label timeLabel = new Label(getTimeAgo(alert.getCreatedAt()));
                timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");

                infoBox.getChildren().addAll(forumLabel, timeLabel);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                headerBox.getChildren().addAll(iconLabel, infoBox, spacer);

                // Message de l'alerte
                Label messageLabel = new Label("Nouveau post : " + alert.getPostTitle());
                messageLabel.setWrapText(true);
                messageLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

                Label authorLabel = new Label("Par : " + alert.getAuthorName());
                authorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

                // Boutons d'action
                HBox actionBox = new HBox(10);
                actionBox.setAlignment(Pos.CENTER_LEFT);

                Button deleteBtn = new Button("🗑️");
                deleteBtn.setStyle(
                    "-fx-background-color: #F44336;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 12px;" +
                    "-fx-padding: 8 12;" +
                    "-fx-background-radius: 5;" +
                    "-fx-cursor: hand;"
                );
                deleteBtn.setOnAction(e -> {
                    deleteAlert(alert);
                    e.consume();
                });

                actionBox.getChildren().add(deleteBtn);

                card.getChildren().addAll(headerBox, messageLabel, authorLabel, actionBox);
                
                // Rendre la carte cliquable pour ouvrir le post
                card.setOnMouseClicked(e -> {
                    alert.setRead(true);
                    openPost(alert.getPostId());
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
