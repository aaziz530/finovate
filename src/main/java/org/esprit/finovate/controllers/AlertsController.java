package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class AlertsController {

    @FXML private Label titleLabel;
    @FXML private Label emptyLabel;
    @FXML private ListView<AlertItem> alertsList;
    @FXML private Button clearAllBtn;

    private MainController mainController;
    private long currentUserId;

    public static class AlertItem {
        private long postId;
        private String postTitle;
        private String forumName;
        private String authorName;
        private Timestamp createdAt;
        private boolean isRead;
        private String alertType;
        private String voteType;

        public AlertItem(long postId, String postTitle, String forumName, String authorName, 
                        Timestamp createdAt, boolean isRead, String alertType, String voteType) {
            this.postId = postId;
            this.postTitle = postTitle;
            this.forumName = forumName;
            this.authorName = authorName;
            this.createdAt = createdAt;
            this.isRead = isRead;
            this.alertType = alertType;
            this.voteType = voteType;
        }

        public long getPostId() { return postId; }
        public String getPostTitle() { return postTitle; }
        public String getForumName() { return forumName; }
        public String getAuthorName() { return authorName; }
        public Timestamp getCreatedAt() { return createdAt; }
        public boolean isRead() { return isRead; }
        public void setRead(boolean read) { this.isRead = read; }
        public String getAlertType() { return alertType; }
        public String getVoteType() { return voteType; }
    }

    @FXML
    public void initialize() {
        System.out.println("AlertsController.initialize() appelé");
        if (alertsList != null) {
            alertsList.setCellFactory(param -> new AlertCell());
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void loadAlerts(long userId) {
        this.currentUserId = userId;
        System.out.println("=== loadAlerts() appelé pour userId: " + userId + " ===");
        if (titleLabel != null) {
            titleLabel.setText("🔔 Mes Alertes");
        }
        loadAlertsFromDB();
    }

    private void loadAlertsFromDB() {
        ObservableList<AlertItem> alerts = FXCollections.observableArrayList();
        
        System.out.println("=== DÉBUT loadAlertsFromDB ===");

        try (Connection conn = getConnection()) {
            System.out.println("Connexion DB OK");
            
            // REQUÊTE VOTES
            String queryVotes = "SELECT p.id, p.title, f.title as forum_name, " +
                    "CONCAT(u.firstname, ' ', u.lastname) as username, p.created_at, v.vote_type " +
                    "FROM votes v " +
                    "INNER JOIN posts p ON v.post_id = p.id " +
                    "INNER JOIN forums f ON p.forum_id = f.id " +
                    "INNER JOIN user u ON v.user_id = u.id " +
                    "WHERE p.author_id = ? AND v.user_id != ? " +
                    "LIMIT 50";

            System.out.println("Exécution requête votes pour userId=" + currentUserId);
            try (PreparedStatement stmt = conn.prepareStatement(queryVotes)) {
                stmt.setLong(1, currentUserId);
                stmt.setLong(2, currentUserId);
                ResultSet rs = stmt.executeQuery();
                
                int count = 0;
                while (rs.next()) {
                    String title = rs.getString("title");
                    String voteType = rs.getString("vote_type");
                    System.out.println("  Vote trouvé: " + voteType + " sur '" + title + "'");
                    
                    alerts.add(new AlertItem(
                            rs.getLong("id"),
                            title,
                            rs.getString("forum_name"),
                            rs.getString("username"),
                            rs.getTimestamp("created_at"),
                            false,
                            "VOTE",
                            voteType
                    ));
                    count++;
                }
                System.out.println("Total votes trouvés: " + count);
            } catch (SQLException e) {
                System.err.println("ERREUR requête votes: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (SQLException e) {
            System.err.println("ERREUR connexion DB: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== TOTAL ALERTES: " + alerts.size() + " ===");
        
        if (alertsList != null) {
            alertsList.setItems(alerts);
            System.out.println("ListView.setItems() OK avec " + alerts.size() + " items");
        } else {
            System.err.println("ERREUR: alertsList est NULL!");
        }
        
        if (emptyLabel != null) {
            boolean isEmpty = alerts.isEmpty();
            emptyLabel.setVisible(isEmpty);
            emptyLabel.setManaged(isEmpty);
            if (isEmpty) {
                emptyLabel.setText("Aucune alerte pour le moment");
            }
            System.out.println("EmptyLabel visible: " + isEmpty);
        }
        
        if (clearAllBtn != null) {
            clearAllBtn.setVisible(!alerts.isEmpty());
            clearAllBtn.setManaged(!alerts.isEmpty());
        }
    }

    @FXML
    private void clearAllAlerts() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Effacer toutes les alertes ?");
        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            alertsList.getItems().clear();
            emptyLabel.setVisible(true);
            clearAllBtn.setVisible(false);
        }
    }

    private void openPost(long postId) {
        if (mainController != null) {
            mainController.showPostDetails(postId, currentUserId);
        }
    }

    private void deleteAlert(AlertItem alert) {
        alertsList.getItems().remove(alert);
        if (alertsList.getItems().isEmpty()) {
            emptyLabel.setVisible(true);
            clearAllBtn.setVisible(false);
        }
    }

    private String getTimeAgo(Timestamp timestamp) {
        long diff = System.currentTimeMillis() - timestamp.getTime();
        long minutes = diff / 60000;
        long hours = minutes / 60;
        long days = hours / 24;
        if (days > 0) return "Il y a " + days + " jour" + (days > 1 ? "s" : "");
        if (hours > 0) return "Il y a " + hours + " heure" + (hours > 1 ? "s" : "");
        if (minutes > 0) return "Il y a " + minutes + " min";
        return "À l'instant";
    }

    private class AlertCell extends ListCell<AlertItem> {
        @Override
        protected void updateItem(AlertItem alert, boolean empty) {
            super.updateItem(alert, empty);
            if (empty || alert == null) {
                setGraphic(null);
            } else {
                VBox card = new VBox(10);
                card.setPadding(new Insets(15));
                card.setStyle("-fx-background-color: white; -fx-border-color: #4CAF50; " +
                        "-fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12;");

                String icon = "🔔";
                String type = "";
                switch (alert.getAlertType()) {
                    case "NEW_POST": 
                        icon = "📝"; 
                        type = "Nouveau post"; 
                        break;
                    case "VOTE":
                        boolean up = alert.getVoteType() != null && alert.getVoteType().equalsIgnoreCase("UPVOTE");
                        icon = up ? "👍" : "👎";
                        type = up ? "Vote positif" : "Vote négatif";
                        break;
                    case "POST_IN_MY_FORUM": 
                        icon = "🏠"; 
                        type = "Post dans votre forum"; 
                        break;
                }

                Label iconLabel = new Label(icon);
                iconLabel.setStyle("-fx-font-size: 24px;");

                Label typeLabel = new Label(type);
                typeLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #FF9800;");

                Label forumLabel = new Label("📁 " + alert.getForumName());
                forumLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #4CAF50;");

                Label timeLabel = new Label(getTimeAgo(alert.getCreatedAt()));
                timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");

                Label messageLabel = new Label(alert.getPostTitle());
                messageLabel.setWrapText(true);
                messageLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");

                Label authorLabel = new Label("Par : " + alert.getAuthorName());
                authorLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

                Button deleteBtn = new Button("🗑️");
                deleteBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
                deleteBtn.setOnAction(e -> deleteAlert(alert));

                card.getChildren().addAll(iconLabel, typeLabel, forumLabel, timeLabel, messageLabel, authorLabel, deleteBtn);
                card.setOnMouseClicked(e -> openPost(alert.getPostId()));
                setGraphic(card);
            }
        }
    }

    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/finovate";
        String user = "root";
        String password = "";
        System.out.println("Connexion à: " + url);
        return DriverManager.getConnection(url, user, password);
    }
}
