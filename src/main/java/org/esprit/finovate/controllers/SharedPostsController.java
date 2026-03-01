package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class SharedPostsController {

    @FXML private Label titleLabel;
    @FXML private Label emptyLabel;
    @FXML private ListView<SharedPostItem> sharedPostsList;

    private MainController mainController;
    private int currentUserId;

    // Classe interne pour représenter un post partagé
    public static class SharedPostItem {
        private int id;
        private int postId;
        private String postTitle;
        private String postContent;
        private String forumName;
        private String authorName;
        private Timestamp sharedAt;

        public SharedPostItem(int id, int postId, String postTitle, String postContent, 
                            String forumName, String authorName, Timestamp sharedAt) {
            this.id = id;
            this.postId = postId;
            this.postTitle = postTitle;
            this.postContent = postContent;
            this.forumName = forumName;
            this.authorName = authorName;
            this.sharedAt = sharedAt;
        }

        public int getId() { return id; }
        public int getPostId() { return postId; }
        public String getPostTitle() { return postTitle; }
        public String getPostContent() { return postContent; }
        public String getForumName() { return forumName; }
        public String getAuthorName() { return authorName; }
        public Timestamp getSharedAt() { return sharedAt; }
    }

    @FXML
    public void initialize() {
        sharedPostsList.setCellFactory(param -> new SharedPostCell());
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void loadSharedPosts(int userId) {
        this.currentUserId = userId;
        titleLabel.setText("Postes Partagés");
        loadSharedPostsFromDB();
    }

    private void loadSharedPostsFromDB() {
        ObservableList<SharedPostItem> posts = FXCollections.observableArrayList();
        
        System.out.println("\n=== CHARGEMENT DES POSTES PARTAGÉS ===");
        System.out.println("currentUserId: " + currentUserId);

        String query = "SELECT sp.id, sp.post_id, p.title, p.content, f.title as forum_name, " +
                "CONCAT(u.firstname, ' ', u.lastname) as author_name, sp.shared_at " +
                "FROM shared_posts sp " +
                "INNER JOIN posts p ON sp.post_id = p.id " +
                "INNER JOIN forums f ON p.forum_id = f.id " +
                "INNER JOIN user u ON p.author_id = u.id " +
                "WHERE sp.user_id = ? " +
                "ORDER BY sp.shared_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count++;
                posts.add(new SharedPostItem(
                        rs.getInt("id"),
                        rs.getInt("post_id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("forum_name"),
                        rs.getString("author_name"),
                        rs.getTimestamp("shared_at")
                ));
            }
            
            System.out.println("Total postes partagés: " + count);

        } catch (SQLException e) {
            System.out.println("ERREUR lors du chargement des postes partagés:");
            e.printStackTrace();
            showError("Erreur de chargement des postes partagés");
        }

        sharedPostsList.setItems(posts);
        emptyLabel.setVisible(posts.isEmpty());
        emptyLabel.setManaged(posts.isEmpty());
    }

    private void unsharePost(int sharedPostId) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Retirer ce post des partagés ?");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            String query = "DELETE FROM shared_posts WHERE id = ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, sharedPostId);
                stmt.executeUpdate();

                showInfo("Post retiré des partagés");
                loadSharedPostsFromDB();

            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur lors du retrait");
            }
        }
    }

    // Cellule personnalisée pour afficher un post partagé
    private class SharedPostCell extends ListCell<SharedPostItem> {
        @Override
        protected void updateItem(SharedPostItem post, boolean empty) {
            super.updateItem(post, empty);

            if (empty || post == null) {
                setGraphic(null);
                setText(null);
            } else {
                VBox card = new VBox(10);
                card.setPadding(new Insets(15));
                card.setStyle(
                    "-fx-background-color: white; " +
                    "-fx-border-color: #E0E0E0; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 12; " +
                    "-fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);"
                );

                Label titleLabel = new Label(post.getPostTitle());
                titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");

                String preview = post.getPostContent().length() > 150
                        ? post.getPostContent().substring(0, 150) + "..."
                        : post.getPostContent();
                Label contentLabel = new Label(preview);
                contentLabel.setWrapText(true);
                contentLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 14px;");

                HBox infoBox = new HBox(15);
                Label forumLabel = new Label("📁 " + post.getForumName());
                Label authorLabel = new Label("👤 " + post.getAuthorName());
                Label dateLabel = new Label("📤 " + post.getSharedAt().toString().substring(0, 16));
                infoBox.getChildren().addAll(forumLabel, authorLabel, dateLabel);
                infoBox.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

                HBox actionBox = new HBox(10);
                actionBox.setAlignment(Pos.CENTER_LEFT);

                Button unshareBtn = new Button("🗑️ Retirer");
                unshareBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
                unshareBtn.setOnAction(e -> {
                    unsharePost(post.getId());
                    e.consume();
                });

                actionBox.getChildren().add(unshareBtn);

                card.getChildren().addAll(titleLabel, contentLabel, infoBox, actionBox);
                
                // Rendre la carte cliquable pour ouvrir le post
                card.setOnMouseClicked(e -> {
                    if (mainController != null) {
                        mainController.showPostDetailsView(post.getPostId(), post.getPostTitle());
                    }
                    e.consume();
                });
                card.setStyle(card.getStyle() + "; -fx-cursor: hand;");
                
                setGraphic(card);
            }
        }
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

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
