package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class PostsController {

    @FXML private Label forumNameLabel;
    @FXML private Button createPostBtn;
    @FXML private Label emptyLabel;
    @FXML private ListView<PostItem> postList;

    private MainController mainController;
    private int currentForumId;
    private String currentForumName;
    private int currentUserId;
    private boolean isForumCreator;

    // Classe interne pour représenter un post
    public static class PostItem {
        private int id;
        private String title;
        private String content;
        private int authorId;
        private String authorName;
        private int commentCount;
        private int upvotes;
        private int downvotes;
        private int score;
        private Timestamp createdAt;

        public PostItem(int id, String title, String content, int authorId, String authorName,
                        int commentCount, Timestamp createdAt) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.authorId = authorId;
            this.authorName = authorName;
            this.commentCount = commentCount;
            this.createdAt = createdAt;
            this.upvotes = 0;
            this.downvotes = 0;
            this.score = 0;
        }

        // Getters
        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public int getAuthorId() { return authorId; }
        public String getAuthorName() { return authorName; }
        public int getCommentCount() { return commentCount; }
        public Timestamp getCreatedAt() { return createdAt; }
        public int getUpvotes() { return upvotes; }
        public int getDownvotes() { return downvotes; }
        public int getScore() { return score; }
        
        public void setUpvotes(int upvotes) { 
            this.upvotes = upvotes;
            this.score = upvotes - downvotes;
        }
        public void setDownvotes(int downvotes) { 
            this.downvotes = downvotes;
            this.score = upvotes - downvotes;
        }
    }

    @FXML
    public void initialize() {
        postList.setCellFactory(param -> new PostCell());
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void loadPosts(int forumId, String forumName, int userId) {
        this.currentForumId = forumId;
        this.currentForumName = forumName;
        this.currentUserId = userId;

        forumNameLabel.setText(forumName);

        // Vérifier si l'utilisateur est le créateur du forum
        checkIfForumCreator();

        // Charger les posts
        loadPostsFromDB();
    }

    private void checkIfForumCreator() {
        String query = "SELECT creator_id FROM forums WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentForumId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                isForumCreator = (rs.getInt("creator_id") == currentUserId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Afficher le bouton créer post seulement si membre du forum
        createPostBtn.setVisible(isMemberOfForum());
        createPostBtn.setManaged(isMemberOfForum());
    }

    private boolean isMemberOfForum() {
        if (isForumCreator) return true;

        String query = "SELECT 1 FROM user_forum WHERE forum_id = ? AND user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentForumId);
            stmt.setInt(2, currentUserId);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void loadPostsFromDB() {
        ObservableList<PostItem> posts = FXCollections.observableArrayList();

        String query = "SELECT p.id, p.title, p.content, p.author_id, u.username as author_name, " +
                "p.created_at, " +
                "(SELECT COUNT(*) FROM comments WHERE post_id = p.id) as comment_count, " +
                "(SELECT COUNT(*) FROM votes WHERE post_id = p.id AND vote_type = 'UPVOTE') as upvotes, " +
                "(SELECT COUNT(*) FROM votes WHERE post_id = p.id AND vote_type = 'DOWNVOTE') as downvotes " +
                "FROM posts p " +
                "INNER JOIN users u ON p.author_id = u.id " +
                "WHERE p.forum_id = ? " +
                "ORDER BY p.created_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentForumId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PostItem post = new PostItem(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getInt("author_id"),
                        rs.getString("author_name"),
                        rs.getInt("comment_count"),
                        rs.getTimestamp("created_at")
                );
                post.setUpvotes(rs.getInt("upvotes"));
                post.setDownvotes(rs.getInt("downvotes"));
                posts.add(post);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de chargement des posts");
        }

        postList.setItems(posts);
        emptyLabel.setVisible(posts.isEmpty());
        emptyLabel.setManaged(posts.isEmpty());
    }

    @FXML
    private void goBack() {
        mainController.goBackToForums();
    }

    @FXML
    private void openCreatePostDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/create-post-dialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Créer un Post");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));

            CreatePostDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setPostsController(this);
            controller.setForumId(currentForumId);
            controller.setAuthorId(currentUserId);

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur d'ouverture du dialogue");
        }
    }

    public void refreshPosts() {
        loadPostsFromDB();
    }

    private void deletePost(int postId) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer ce post ?");
        confirmAlert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String query = "DELETE FROM posts WHERE id = ? AND author_id = ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, postId);
                stmt.setInt(2, currentUserId);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    showInfo("Post supprimé avec succès");
                    refreshPosts();
                } else {
                    showError("Impossible de supprimer ce post");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur de suppression du post");
            }
        }
    }

    private void openPostDetails(PostItem post) {
        mainController.showPostDetailsView(post.getId(), post.getTitle());
    }

    private void sharePost(int postId) {
        String query = "INSERT INTO shared_posts (post_id, user_id, shared_at) VALUES (?, ?, NOW())";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, postId);
            stmt.setInt(2, currentUserId);
            stmt.executeUpdate();

            // Vérifier les badges de partage
            org.example.badge.BadgeManager.checkShareBadges(currentUserId);

            showInfo("Post partagé avec succès !");
            refreshPosts();

        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                showError("Vous avez déjà partagé ce post");
            } else {
                e.printStackTrace();
                showError("Erreur lors du partage: " + (e.getMessage() != null ? e.getMessage() : "Erreur inconnue"));
            }
        }
    }

    private void votePost(int postId, String voteType) {
        // Vérifier si l'utilisateur a déjà voté
        String checkQuery = "SELECT vote_type FROM votes WHERE post_id = ? AND user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

            checkStmt.setInt(1, postId);
            checkStmt.setInt(2, currentUserId);
            ResultSet rs = checkStmt.executeQuery();

            boolean isNewVote = false;

            if (rs.next()) {
                String existingVote = rs.getString("vote_type");
                
                if (existingVote.equals(voteType)) {
                    // Même vote: on le retire (toggle)
                    String deleteQuery = "DELETE FROM votes WHERE post_id = ? AND user_id = ?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                        deleteStmt.setInt(1, postId);
                        deleteStmt.setInt(2, currentUserId);
                        deleteStmt.executeUpdate();
                    }
                } else {
                    // Vote différent: on le change
                    String updateQuery = "UPDATE votes SET vote_type = ? WHERE post_id = ? AND user_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, voteType);
                        updateStmt.setInt(2, postId);
                        updateStmt.setInt(3, currentUserId);
                        updateStmt.executeUpdate();
                    }
                }
            } else {
                // Nouveau vote
                String insertQuery = "INSERT INTO votes (post_id, user_id, vote_type) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setInt(1, postId);
                    insertStmt.setInt(2, currentUserId);
                    insertStmt.setString(3, voteType);
                    insertStmt.executeUpdate();
                }
                isNewVote = true;
            }

            // Vérifier les badges si c'est un nouveau vote
            if (isNewVote) {
                org.example.badge.BadgeManager.checkVoteBadges(currentUserId, currentForumId);
            }

            refreshPosts();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors du vote");
        }
    }

    // Cellule personnalisée pour afficher un post
    private class PostCell extends ListCell<PostItem> {
        @Override
        protected void updateItem(PostItem post, boolean empty) {
            super.updateItem(post, empty);

            if (empty || post == null) {
                setGraphic(null);
                setText(null);
            } else {
                VBox card = new VBox(10);
                card.setPadding(new Insets(15));
                card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; " +
                        "-fx-border-radius: 5; -fx-background-radius: 5;");

                // Titre du post
                Label titleLabel = new Label(post.getTitle());
                titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                // Aperçu du contenu
                String preview = post.getContent().length() > 150
                        ? post.getContent().substring(0, 150) + "..."
                        : post.getContent();
                Label contentLabel = new Label(preview);
                contentLabel.setWrapText(true);
                contentLabel.setStyle("-fx-text-fill: #666;");

                // Info (auteur, date, commentaires)
                HBox infoBox = new HBox(15);
                Label authorLabel = new Label("👤 " + post.getAuthorName());
                Label dateLabel = new Label("📅 " + post.getCreatedAt().toString().substring(0, 16));
                Label commentsLabel = new Label("💬 " + post.getCommentCount() + " commentaires");
                infoBox.getChildren().addAll(authorLabel, dateLabel, commentsLabel);
                infoBox.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

                // Boutons d'action
                HBox actionBox = new HBox(10);
                actionBox.setAlignment(Pos.CENTER_LEFT);

                // Votes (style Facebook - horizontal)
                HBox voteBox = new HBox(15);
                voteBox.setAlignment(Pos.CENTER);
                voteBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 8 15 8 15; -fx-border-radius: 20; -fx-background-radius: 20;");

                Button likeBtn = new Button("👍");
                likeBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-cursor: hand; -fx-border: none;");
                likeBtn.setOnAction(e -> {
                    votePost(post.getId(), "UPVOTE");
                    e.consume();
                });

                Label likeCountLabel = new Label(String.valueOf(post.getUpvotes()));
                likeCountLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1877F2;");

                Label separatorLabel = new Label("|");
                separatorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ccc;");

                Button dislikeBtn = new Button("👎");
                dislikeBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-cursor: hand; -fx-border: none;");
                dislikeBtn.setOnAction(e -> {
                    votePost(post.getId(), "DOWNVOTE");
                    e.consume();
                });

                Label dislikeCountLabel = new Label(String.valueOf(post.getDownvotes()));
                dislikeCountLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #F02849;");

                voteBox.getChildren().addAll(likeBtn, likeCountLabel, separatorLabel, dislikeBtn, dislikeCountLabel);

                Button shareBtn = new Button("📤 Partager");
                shareBtn.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white;");
                shareBtn.setOnAction(e -> {
                    sharePost(post.getId());
                    e.consume();
                });

                actionBox.getChildren().addAll(voteBox, shareBtn);

                // Si c'est le post de l'utilisateur, ajouter modifier/supprimer
                if (post.getAuthorId() == currentUserId) {
                    Button editBtn = new Button("✏️ Modifier");
                    editBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
                    editBtn.setOnAction(e -> e.consume());

                    Button deleteBtn = new Button("🗑️ Supprimer");
                    deleteBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
                    deleteBtn.setOnAction(e -> {
                        deletePost(post.getId());
                        e.consume();
                    });

                    actionBox.getChildren().addAll(editBtn, deleteBtn);
                }

                card.getChildren().addAll(titleLabel, contentLabel, infoBox, actionBox);
                
                // Rendre la carte cliquable pour ouvrir le post
                card.setOnMouseClicked(e -> {
                    openPostDetails(post);
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