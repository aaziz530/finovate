package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import org.esprit.finovate.utils.ImageUtils;
import org.esprit.finovate.services.TranslationService;
import org.esprit.finovate.services.SummarizationService;
import javafx.concurrent.Task;
import org.esprit.finovate.utils.BadgeManager;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class PostsController {

    @FXML private Label forumNameLabel;
    @FXML private Button createPostBtn;
    @FXML private Label emptyLabel;
    @FXML private ListView<PostItem> postList;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;

    private MainController mainController; // Optional - for backward compatibility
    private ForumsPageController forumsPageController; // New - for direct navigation
    private StackPane parentContentArea; // For loading post details view
    private long currentForumId;
    private String currentForumName;
    private long currentUserId;
    private boolean isForumCreator;
    private ObservableList<PostItem> allPosts = FXCollections.observableArrayList();
    private ObservableList<PostItem> filteredPosts = FXCollections.observableArrayList();

    // Classe interne pour représenter un post
    public static class PostItem {
        private long id;
        private String title;
        private String content;
        private long authorId;
        private String authorName;
        private int commentCount;
        private int upvotes;
        private int downvotes;
        private int score;
        private Timestamp createdAt;
        private String imageUrl;

        public PostItem(long id, String title, String content, long authorId, String authorName,
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
        public long getId() { return id; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public long getAuthorId() { return authorId; }
        public String getAuthorName() { return authorName; }
        public int getCommentCount() { return commentCount; }
        public Timestamp getCreatedAt() { return createdAt; }
        public int getUpvotes() { return upvotes; }
        public int getDownvotes() { return downvotes; }
        public int getScore() { return score; }
        public String getImageUrl() { return imageUrl; }
        
        public void setUpvotes(int upvotes) { 
            this.upvotes = upvotes;
            this.score = upvotes - downvotes;
        }
        public void setDownvotes(int downvotes) { 
            this.downvotes = downvotes;
            this.score = upvotes - downvotes;
        }
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }

    @FXML
    public void initialize() {
        postList.setCellFactory(param -> new PostCell());
        
        // Ajouter un espacement entre les cartes
        postList.setStyle(
            postList.getStyle() + 
            "-fx-background-color: transparent; " +
            "-fx-cell-size: 10; " +
            "-fx-fixed-cell-size: -1;"
        );
        
        // Configurer le ComboBox de tri
        sortComboBox.setItems(FXCollections.observableArrayList(
            "Plus récents",
            "Plus anciens",
            "Plus populaires",
            "Plus commentés",
            "A-Z",
            "Z-A"
        ));
        sortComboBox.setValue("Plus récents");
        
        // Listener pour la recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applySearchAndSort();
        });
        
        // Listener pour le tri
        sortComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            applySearchAndSort();
        });
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    
    /**
     * Set the parent ForumsPageController and its content area for direct navigation
     */
    public void setForumsPageController(ForumsPageController forumsPageController, StackPane contentArea) {
        this.forumsPageController = forumsPageController;
        this.parentContentArea = contentArea;
    }

    public void loadPosts(long forumId, String forumName, long userId) {
        this.currentForumId = forumId;
        this.currentForumName = forumName;
        this.currentUserId = userId;

        System.out.println("=== PostsController.loadPosts ===");
        System.out.println("forumId: " + forumId);
        System.out.println("forumName: " + forumName);
        System.out.println("userId: " + userId);

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

            stmt.setLong(1, currentForumId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                isForumCreator = (rs.getLong("creator_id") == currentUserId);
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

            stmt.setLong(1, currentForumId);
            stmt.setLong(2, currentUserId);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void loadPostsFromDB() {
        allPosts.clear();
        
        System.out.println("=== loadPostsFromDB ===");
        System.out.println("currentForumId: " + currentForumId);

        String query = "SELECT p.id, p.title, p.content, p.author_id, " +
                "CONCAT(u.firstname, ' ', u.lastname) as author_name, " +
                "p.created_at, p.image_url, " +
                "(SELECT COUNT(*) FROM comments WHERE post_id = p.id) as comment_count, " +
                "(SELECT COUNT(*) FROM votes WHERE post_id = p.id AND vote_type = 'UPVOTE') as upvotes, " +
                "(SELECT COUNT(*) FROM votes WHERE post_id = p.id AND vote_type = 'DOWNVOTE') as downvotes " +
                "FROM posts p " +
                "INNER JOIN user u ON p.author_id = u.id " +
                "WHERE p.forum_id = ? " +
                "ORDER BY p.created_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, currentForumId);
            ResultSet rs = stmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count++;
                PostItem post = new PostItem(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getLong("author_id"),
                        rs.getString("author_name"),
                        rs.getInt("comment_count"),
                        rs.getTimestamp("created_at")
                );
                post.setUpvotes(rs.getInt("upvotes"));
                post.setDownvotes(rs.getInt("downvotes"));
                
                // Charger l'image_url
                try {
                    String imageUrl = rs.getString("image_url");
                    post.setImageUrl(imageUrl);
                    if (imageUrl != null) {
                        System.out.println("Post #" + post.getId() + " a une image: " + imageUrl);
                    }
                } catch (SQLException e) {
                    // Colonne image_url n'existe pas encore
                    post.setImageUrl(null);
                }
                
                allPosts.add(post);
            }
            
            System.out.println("Total posts chargés: " + count);

        } catch (SQLException e) {
            System.out.println("ERREUR lors du chargement des posts:");
            e.printStackTrace();
            showError("Erreur de chargement des posts");
        }

        applySearchAndSort();
    }
    
    private void applySearchAndSort() {
        String searchText = searchField != null ? searchField.getText() : "";
        String sortType = sortComboBox != null ? sortComboBox.getValue() : "Plus récents";
        
        // Filtrer par recherche
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredPosts = FXCollections.observableArrayList(allPosts);
        } else {
            String search = searchText.toLowerCase();
            filteredPosts = allPosts.filtered(post -> 
                post.getTitle().toLowerCase().contains(search) || 
                post.getContent().toLowerCase().contains(search) ||
                post.getAuthorName().toLowerCase().contains(search)
            );
        }
        
        // Trier
        if (sortType != null) {
            switch (sortType) {
                case "Plus récents":
                    filteredPosts.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
                    break;
                case "Plus anciens":
                    filteredPosts.sort((p1, p2) -> p1.getCreatedAt().compareTo(p2.getCreatedAt()));
                    break;
                case "Plus populaires":
                    filteredPosts.sort((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()));
                    break;
                case "Plus commentés":
                    filteredPosts.sort((p1, p2) -> Integer.compare(p2.getCommentCount(), p1.getCommentCount()));
                    break;
                case "A-Z":
                    filteredPosts.sort((p1, p2) -> p1.getTitle().compareToIgnoreCase(p2.getTitle()));
                    break;
                case "Z-A":
                    filteredPosts.sort((p1, p2) -> p2.getTitle().compareToIgnoreCase(p1.getTitle()));
                    break;
            }
        }
        
        postList.setItems(filteredPosts);
        emptyLabel.setVisible(filteredPosts.isEmpty());
        emptyLabel.setManaged(filteredPosts.isEmpty());
    }

    @FXML
    private void goBack() {
        if (mainController != null) {
            mainController.goBackToForums();
        } else if (forumsPageController != null) {
            // Use forumsPageController to navigate back
            forumsPageController.goBackToForums();
        } else {
            System.err.println("Aucun contrôleur parent disponible, impossible de revenir en arrière");
            showError("Impossible de revenir en arrière");
        }
    }

    @FXML
    private void openCreatePostDialog() {
        // Open create post dialog directly (works with or without MainController)
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/create-post-dialog.fxml"));
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
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du dialogue de création");
        }
    }

    public void refreshPosts() {
        loadPostsFromDB();
    }
    
    private void editPost(PostItem post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/create-post-dialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier le Post");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));

            CreatePostDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setPostsController(this);
            controller.setForumId(currentForumId);
            controller.setAuthorId(currentUserId);
            controller.setPostToEdit(post.getId(), post.getTitle(), post.getContent(), post.getImageUrl());

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur d'ouverture du dialogue de modification");
        }
    }

    private void deletePost(long postId) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer ce post ?");
        confirmAlert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String query = "DELETE FROM posts WHERE id = ? AND author_id = ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setLong(1, postId);
                stmt.setLong(2, currentUserId);
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
        // Try to load post details - use ForumsPageController if available, otherwise MainController
        if (parentContentArea != null) {
            // Direct navigation through ForumsPageController
            loadPostDetailsViewDirect(post.getId(), post.getTitle());
        } else if (mainController != null) {
            // Legacy navigation through MainController
            mainController.showPostDetailsView(post.getId(), post.getTitle());
        } else {
            System.err.println("ERREUR: Aucun contrôleur parent disponible pour ouvrir le post!");
            showError("Erreur: Impossible d'ouvrir le post. Veuillez réessayer.");
        }
    }
    
    /**
     * Load post details view directly into parent content area (without MainController)
     */
    private void loadPostDetailsViewDirect(long postId, String postTitle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post-details.fxml"));
            Parent postDetailsView = loader.load();

            PostDetailsController controller = loader.getController();
            // Pass mainController if available (optional)
            if (mainController != null) {
                controller.setMainController(mainController);
            }
            // Set parent content area for navigation
            if (forumsPageController != null && parentContentArea != null) {
                controller.setForumsPageController(forumsPageController, parentContentArea);
            }
            controller.loadPostDetails(postId, currentUserId);

            parentContentArea.getChildren().clear();
            parentContentArea.getChildren().add(postDetailsView);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement du post");
        }
    }

    private void sharePost(long postId) {
        String query = "INSERT INTO shared_posts (post_id, user_id, shared_at) VALUES (?, ?, NOW())";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, postId);
            stmt.setLong(2, currentUserId);
            stmt.executeUpdate();

            // Vérifier les badges de partage
            BadgeManager.checkShareBadges(currentUserId);

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

    private void votePost(long postId, String voteType) {
        // Vérifier si l'utilisateur a déjà voté
        String checkQuery = "SELECT vote_type FROM votes WHERE post_id = ? AND user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

            checkStmt.setLong(1, postId);
            checkStmt.setLong(2, currentUserId);
            ResultSet rs = checkStmt.executeQuery();

            boolean isNewVote = false;

            if (rs.next()) {
                String existingVote = rs.getString("vote_type");
                
                if (existingVote.equals(voteType)) {
                    // Même vote: on le retire (toggle)
                    String deleteQuery = "DELETE FROM votes WHERE post_id = ? AND user_id = ?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                        deleteStmt.setLong(1, postId);
                        deleteStmt.setLong(2, currentUserId);
                        deleteStmt.executeUpdate();
                    }
                } else {
                    // Vote différent: on le change
                    String updateQuery = "UPDATE votes SET vote_type = ? WHERE post_id = ? AND user_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, voteType);
                        updateStmt.setLong(2, postId);
                        updateStmt.setLong(3, currentUserId);
                        updateStmt.executeUpdate();
                    }
                }
            } else {
                // Nouveau vote
                String insertQuery = "INSERT INTO votes (post_id, user_id, vote_type, created_at) VALUES (?, ?, ?, NOW())";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setLong(1, postId);
                    insertStmt.setLong(2, currentUserId);
                    insertStmt.setString(3, voteType);
                    insertStmt.executeUpdate();
                }
                isNewVote = true;
            }

            // Vérifier les badges si c'est un nouveau vote
            if (isNewVote) {
                BadgeManager.checkVoteBadges(currentUserId, currentForumId);
            }

            refreshPosts();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors du vote");
        }
    }

    // Cellule personnalisée pour afficher un post (style Reddit/Twitter moderne)
    private class PostCell extends ListCell<PostItem> {
        @Override
        protected void updateItem(PostItem post, boolean empty) {
            super.updateItem(post, empty);

            if (empty || post == null) {
                setGraphic(null);
                setText(null);
                setStyle("-fx-background-color: transparent; -fx-padding: 0;");
            } else {
                // Wrapper pour ajouter un espacement en bas
                VBox wrapper = new VBox();
                wrapper.setStyle("-fx-background-color: transparent; -fx-padding: 0 0 15 0;");
                
                // Card principale avec layout horizontal (EXACTEMENT COMME LES FORUMS)
                HBox card = new HBox(0);
                card.setPrefHeight(150);
                card.setStyle(
                    "-fx-background-color: white; " +
                    "-fx-border-color: #E0E0E0; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 12; " +
                    "-fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2); " +
                    "-fx-cursor: hand;"
                );

                // IMAGE DU POST sur toute la hauteur à gauche (120px de largeur) - TOUJOURS AFFICHÉE
                ImageView postImage = null;
                if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                    System.out.println("Chargement de l'image du post: " + post.getImageUrl());
                    postImage = ImageUtils.loadImageView(post.getImageUrl(), 120, 150);
                }
                
                if (postImage != null) {
                    postImage.setFitWidth(120);
                    postImage.setFitHeight(150);
                    postImage.setPreserveRatio(false);
                    
                    // Rendre l'image plus claire
                    javafx.scene.effect.ColorAdjust colorAdjust = new javafx.scene.effect.ColorAdjust();
                    colorAdjust.setBrightness(0.2);
                    postImage.setEffect(colorAdjust);
                    
                    // Clip pour arrondir les coins à gauche seulement
                    javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(120, 150);
                    clip.setArcWidth(12);
                    clip.setArcHeight(12);
                    postImage.setClip(clip);
                    
                    card.getChildren().add(postImage);
                    System.out.println("Image du post chargée avec succès");
                } else {
                    System.out.println("Utilisation de l'icône par défaut");
                    VBox iconBox = createDefaultPostIconBox();
                    card.getChildren().add(iconBox);
                }

                // CONTENU à droite de l'image
                VBox contentBox = new VBox(8);
                contentBox.setPadding(new Insets(15, 20, 15, 20));
                HBox.setHgrow(contentBox, Priority.ALWAYS);
                
                // Titre du post en gras (couleur verte)
                Label titleLabel = new Label(post.getTitle());
                titleLabel.setStyle(
                    "-fx-font-size: 18px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-text-fill: #2E7D32;"
                );
                titleLabel.setWrapText(true);
                titleLabel.setMaxWidth(Double.MAX_VALUE);

                // Métadonnées (auteur + date + commentaires)
                HBox metaBox = new HBox(15);
                Label authorLabel = new Label("👤 " + post.getAuthorName());
                authorLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666; -fx-font-weight: 600;");
                
                Label dateLabel = new Label("📅 " + getTimeAgo(post.getCreatedAt()));
                dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
                
                Label commentsLabel = new Label("💬 " + post.getCommentCount() + " commentaires");
                commentsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
                
                metaBox.getChildren().addAll(authorLabel, dateLabel, commentsLabel);

                // Aperçu du contenu (limité à 2 lignes)
                String preview = post.getContent().length() > 120
                        ? post.getContent().substring(0, 120) + "..."
                        : post.getContent();
                Label contentLabel = new Label(preview);
                contentLabel.setWrapText(true);
                contentLabel.setMaxWidth(Double.MAX_VALUE);
                contentLabel.setMaxHeight(40);
                contentLabel.setStyle(
                    "-fx-text-fill: #555; " +
                    "-fx-font-size: 14px; " +
                    "-fx-line-spacing: 2px;"
                );

                // Spacer pour pousser les boutons en bas
                Region spacer = new Region();
                VBox.setVgrow(spacer, Priority.ALWAYS);

                // BOUTONS D'ACTION en bas (ICÔNES SEULEMENT avec tooltips)
                HBox actionBox = new HBox(15);
                actionBox.setAlignment(Pos.CENTER_LEFT);

                // Bouton J'aime (icône seulement)
                Button likeBtn = new Button("👍 " + post.getUpvotes());
                likeBtn.setStyle(
                    "-fx-background-color: transparent; " +
                    "-fx-text-fill: #666; " +
                    "-fx-font-size: 16px; " +
                    "-fx-padding: 4 8; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0;"
                );
                Tooltip likeTooltip = new Tooltip("J'aime");
                likeTooltip.setStyle("-fx-font-size: 12px;");
                Tooltip.install(likeBtn, likeTooltip);
                
                likeBtn.setOnMouseEntered(e -> likeBtn.setStyle(
                    "-fx-background-color: #E8F5E9; " +
                    "-fx-text-fill: #4CAF50; " +
                    "-fx-font-size: 16px; " +
                    "-fx-padding: 4 8; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0; " +
                    "-fx-background-radius: 4;"
                ));
                likeBtn.setOnMouseExited(e -> likeBtn.setStyle(
                    "-fx-background-color: transparent; " +
                    "-fx-text-fill: #666; " +
                    "-fx-font-size: 16px; " +
                    "-fx-padding: 4 8; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0;"
                ));
                likeBtn.setOnAction(e -> {
                    e.consume();
                    votePost(post.getId(), "UPVOTE");
                });

                // Bouton Je n'aime pas (icône seulement)
                Button dislikeBtn = new Button("👎 " + post.getDownvotes());
                dislikeBtn.setStyle(
                    "-fx-background-color: transparent; " +
                    "-fx-text-fill: #666; " +
                    "-fx-font-size: 16px; " +
                    "-fx-padding: 4 8; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0;"
                );
                Tooltip dislikeTooltip = new Tooltip("Je n'aime pas");
                dislikeTooltip.setStyle("-fx-font-size: 12px;");
                Tooltip.install(dislikeBtn, dislikeTooltip);
                
                dislikeBtn.setOnMouseEntered(e -> dislikeBtn.setStyle(
                    "-fx-background-color: #FFEBEE; " +
                    "-fx-text-fill: #F44336; " +
                    "-fx-font-size: 16px; " +
                    "-fx-padding: 4 8; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0; " +
                    "-fx-background-radius: 4;"
                ));
                dislikeBtn.setOnMouseExited(e -> dislikeBtn.setStyle(
                    "-fx-background-color: transparent; " +
                    "-fx-text-fill: #666; " +
                    "-fx-font-size: 16px; " +
                    "-fx-padding: 4 8; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0;"
                ));
                dislikeBtn.setOnAction(e -> {
                    e.consume();
                    votePost(post.getId(), "DOWNVOTE");
                });

                // Bouton Partager (icône seulement)
                Button shareBtn = new Button("📤");
                shareBtn.setStyle(
                    "-fx-background-color: transparent; " +
                    "-fx-text-fill: #666; " +
                    "-fx-font-size: 16px; " +
                    "-fx-padding: 4 8; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0;"
                );
                Tooltip shareTooltip = new Tooltip("Partager");
                shareTooltip.setStyle("-fx-font-size: 12px;");
                Tooltip.install(shareBtn, shareTooltip);
                
                shareBtn.setOnMouseEntered(e -> shareBtn.setStyle(
                    "-fx-background-color: #E3F2FD; " +
                    "-fx-text-fill: #2196F3; " +
                    "-fx-font-size: 16px; " +
                    "-fx-padding: 4 8; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0; " +
                    "-fx-background-radius: 4;"
                ));
                shareBtn.setOnMouseExited(e -> shareBtn.setStyle(
                    "-fx-background-color: transparent; " +
                    "-fx-text-fill: #666; " +
                    "-fx-font-size: 16px; " +
                    "-fx-padding: 4 8; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0;"
                ));
                shareBtn.setOnAction(e -> {
                    e.consume();
                    sharePost(post.getId());
                });

                actionBox.getChildren().addAll(likeBtn, dislikeBtn, shareBtn);

                // AI FEATURES: Translate and Summarize buttons
                // Bouton Traduire (🌐)
                MenuButton translateBtn = new MenuButton("🌐");
                translateBtn.setStyle(
                    "-fx-background-color: transparent; " +
                    "-fx-text-fill: #666; " +
                    "-fx-font-size: 16px; " +
                    "-fx-padding: 4 8; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0;"
                );
                Tooltip translateTooltip = new Tooltip("Traduire");
                translateTooltip.setStyle("-fx-font-size: 12px;");
                Tooltip.install(translateBtn, translateTooltip);
                
                translateBtn.setOnMouseEntered(ev -> translateBtn.setStyle(
                    "-fx-background-color: #E8EAF6; " +
                    "-fx-text-fill: #3F51B5; " +
                    "-fx-font-size: 16px; " +
                    "-fx-padding: 4 8; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0; " +
                    "-fx-background-radius: 4;"
                ));
                translateBtn.setOnMouseExited(ev -> translateBtn.setStyle(
                    "-fx-background-color: transparent; " +
                    "-fx-text-fill: #666; " +
                    "-fx-font-size: 16px; " +
                    "-fx-padding: 4 8; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0;"
                ));
                
                MenuItem toEnglish = new MenuItem("🇬🇧 English");
                toEnglish.setOnAction(ev -> {
                    ev.consume();
                    translatePost(post, "English", titleLabel, contentLabel);
                });
                
                MenuItem toFrench = new MenuItem("🇫🇷 Français");
                toFrench.setOnAction(ev -> {
                    ev.consume();
                    translatePost(post, "French", titleLabel, contentLabel);
                });
                
                MenuItem toSpanish = new MenuItem("🇪🇸 Español");
                toSpanish.setOnAction(ev -> {
                    ev.consume();
                    translatePost(post, "Spanish", titleLabel, contentLabel);
                });
                
                MenuItem toGerman = new MenuItem("🇩🇪 Deutsch");
                toGerman.setOnAction(ev -> {
                    ev.consume();
                    translatePost(post, "German", titleLabel, contentLabel);
                });
                
                translateBtn.getItems().addAll(toEnglish, toFrench, toSpanish, toGerman);
                
                // Bouton Résumer (📝)
                Button summarizeBtn = new Button("📝");
                summarizeBtn.setStyle(
                    "-fx-background-color: transparent; " +
                    "-fx-text-fill: #666; " +
                    "-fx-font-size: 16px; " +
                    "-fx-padding: 4 8; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0;"
                );
                Tooltip summarizeTooltip = new Tooltip("Résumer");
                summarizeTooltip.setStyle("-fx-font-size: 12px;");
                Tooltip.install(summarizeBtn, summarizeTooltip);
                
                summarizeBtn.setOnMouseEntered(ev -> summarizeBtn.setStyle(
                    "-fx-background-color: #FFF9C4; " +
                    "-fx-text-fill: #F57C00; " +
                    "-fx-font-size: 16px; " +
                    "-fx-padding: 4 8; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0; " +
                    "-fx-background-radius: 4;"
                ));
                summarizeBtn.setOnMouseExited(ev -> summarizeBtn.setStyle(
                    "-fx-background-color: transparent; " +
                    "-fx-text-fill: #666; " +
                    "-fx-font-size: 16px; " +
                    "-fx-padding: 4 8; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0;"
                ));
                summarizeBtn.setOnAction(ev -> {
                    ev.consume();
                    toggleSummary(post, card, contentBox);
                });
                
                actionBox.getChildren().addAll(translateBtn, summarizeBtn);

                // Si c'est le post de l'utilisateur, ajouter modifier et supprimer
                if (post.getAuthorId() == currentUserId) {
                    // Bouton Modifier
                    Button editBtn = new Button("✏");
                    editBtn.setStyle(
                        "-fx-background-color: transparent; " +
                        "-fx-text-fill: #666; " +
                        "-fx-font-size: 16px; " +
                        "-fx-padding: 4 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-width: 0;"
                    );
                    Tooltip editTooltip = new Tooltip("Modifier");
                    editTooltip.setStyle("-fx-font-size: 12px;");
                    Tooltip.install(editBtn, editTooltip);
                    
                    editBtn.setOnMouseEntered(ev -> editBtn.setStyle(
                        "-fx-background-color: #FFF3E0; " +
                        "-fx-text-fill: #FF9800; " +
                        "-fx-font-size: 16px; " +
                        "-fx-padding: 4 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-width: 0; " +
                        "-fx-background-radius: 4;"
                    ));
                    editBtn.setOnMouseExited(ev -> editBtn.setStyle(
                        "-fx-background-color: transparent; " +
                        "-fx-text-fill: #666; " +
                        "-fx-font-size: 16px; " +
                        "-fx-padding: 4 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-width: 0;"
                    ));
                    editBtn.setOnAction(ev -> {
                        ev.consume();
                        editPost(post);
                    });
                    
                    // Bouton Supprimer
                    Button deleteBtn = new Button("🗑");
                    deleteBtn.setStyle(
                        "-fx-background-color: transparent; " +
                        "-fx-text-fill: #666; " +
                        "-fx-font-size: 16px; " +
                        "-fx-padding: 4 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-width: 0;"
                    );
                    Tooltip deleteTooltip = new Tooltip("Supprimer");
                    deleteTooltip.setStyle("-fx-font-size: 12px;");
                    Tooltip.install(deleteBtn, deleteTooltip);
                    
                    deleteBtn.setOnMouseEntered(ev -> deleteBtn.setStyle(
                        "-fx-background-color: #FFEBEE; " +
                        "-fx-text-fill: #F44336; " +
                        "-fx-font-size: 16px; " +
                        "-fx-padding: 4 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-width: 0; " +
                        "-fx-background-radius: 4;"
                    ));
                    deleteBtn.setOnMouseExited(ev -> deleteBtn.setStyle(
                        "-fx-background-color: transparent; " +
                        "-fx-text-fill: #666; " +
                        "-fx-font-size: 16px; " +
                        "-fx-padding: 4 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-width: 0;"
                    ));
                    deleteBtn.setOnAction(ev -> {
                        ev.consume();
                        deletePost(post.getId());
                    });
                    
                    actionBox.getChildren().addAll(editBtn, deleteBtn);
                }

                contentBox.getChildren().addAll(titleLabel, metaBox, contentLabel, spacer, actionBox);
                card.getChildren().add(contentBox);
                
                // Effet hover
                card.setOnMouseEntered(e -> {
                    card.setStyle(
                        "-fx-background-color: #F5F5F5; " +
                        "-fx-border-color: #4CAF50; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 3); " +
                        "-fx-cursor: hand;"
                    );
                });
                
                card.setOnMouseExited(e -> {
                    card.setStyle(
                        "-fx-background-color: white; " +
                        "-fx-border-color: #E0E0E0; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2); " +
                        "-fx-cursor: hand;"
                    );
                });
                
                // Rendre toute la carte cliquable pour ouvrir les détails
                card.setOnMouseClicked(e -> {
                    openPostDetails(post);
                    e.consume();
                });
                
                wrapper.getChildren().add(card);
                setGraphic(wrapper);
                setStyle("-fx-background-color: transparent; -fx-padding: 0;");
            }
        }
    }

    // ============================================
    // AI FEATURES: Translation and Summarization
    // ============================================
    
    /**
     * Translate post content to target language
     */
    private void translatePost(PostItem post, String targetLang, Label titleLabel, Label contentLabel) {
        // Show loading indicator
        contentLabel.setText("⏳ Traduction en cours...");
        
        Task<String[]> translateTask = new Task<>() {
            @Override
            protected String[] call() {
                String translatedTitle = TranslationService.translateAuto(post.getTitle(), targetLang);
                String translatedContent = TranslationService.translateAuto(post.getContent(), targetLang);
                return new String[]{translatedTitle, translatedContent};
            }
        };
        
        translateTask.setOnSucceeded(e -> {
            String[] result = translateTask.getValue();
            titleLabel.setText(result[0]);
            
            // Update preview (limit to 120 chars)
            String preview = result[1].length() > 120
                    ? result[1].substring(0, 120) + "..."
                    : result[1];
            contentLabel.setText(preview);
            
            showInfo("✅ Traduit en " + targetLang);
        });
        
        translateTask.setOnFailed(e -> {
            contentLabel.setText(post.getContent().substring(0, Math.min(120, post.getContent().length())) + "...");
            showError("❌ Erreur de traduction");
        });
        
        new Thread(translateTask).start();
    }
    
    /**
     * Toggle summary display under post
     */
    private void toggleSummary(PostItem post, HBox card, VBox contentBox) {
        // Check if summary already exists
        VBox existingSummary = null;
        for (javafx.scene.Node node : contentBox.getChildren()) {
            if (node.getUserData() != null && node.getUserData().equals("summary")) {
                existingSummary = (VBox) node;
                break;
            }
        }
        
        if (existingSummary != null) {
            // Remove summary if it exists
            contentBox.getChildren().remove(existingSummary);
            return;
        }
        
        // Create summary section
        VBox summaryBox = new VBox(12);
        summaryBox.setUserData("summary");
        summaryBox.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #E8F5E9, #C8E6C9); " +
            "-fx-padding: 20; " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: #4CAF50; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(76, 175, 80, 0.3), 8, 0, 0, 2);"
        );
        summaryBox.setMaxWidth(Double.MAX_VALUE);
        
        // Header
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label("📝");
        iconLabel.setStyle("-fx-font-size: 24px;");
        
        Label headerLabel = new Label("Résumé Intelligent");
        headerLabel.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2E7D32;"
        );
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label loadingLabel = new Label("⏳ Génération en cours...");
        loadingLabel.setStyle(
            "-fx-font-size: 13px; " +
            "-fx-text-fill: #666; " +
            "-fx-font-style: italic;"
        );
        
        headerBox.getChildren().addAll(iconLabel, headerLabel, spacer, loadingLabel);
        
        // Summary content
        Label summaryLabel = new Label();
        summaryLabel.setWrapText(true);
        summaryLabel.setStyle(
            "-fx-font-size: 15px; " +
            "-fx-text-fill: #1B5E20; " +
            "-fx-line-spacing: 4px; " +
            "-fx-padding: 10 0 0 0;"
        );
        summaryLabel.setMaxWidth(Double.MAX_VALUE);
        
        // Footer
        Label footerLabel = new Label("💡 Résumé généré automatiquement par IA");
        footerLabel.setStyle(
            "-fx-font-size: 11px; " +
            "-fx-text-fill: #4CAF50; " +
            "-fx-font-style: italic; " +
            "-fx-padding: 10 0 0 0;"
        );
        
        summaryBox.getChildren().addAll(headerBox, summaryLabel, footerLabel);
        
        // Add summary box before action buttons
        int actionBoxIndex = contentBox.getChildren().size() - 1;
        contentBox.getChildren().add(actionBoxIndex, summaryBox);
        
        // Generate summary in background
        Task<String> summarizeTask = new Task<>() {
            @Override
            protected String call() {
                return SummarizationService.summarizePost(post.getTitle(), post.getContent());
            }
        };
        
        summarizeTask.setOnSucceeded(e -> {
            String summary = summarizeTask.getValue();
            summaryLabel.setText(summary);
            loadingLabel.setVisible(false);
            loadingLabel.setManaged(false);
        });
        
        summarizeTask.setOnFailed(e -> {
            summaryLabel.setText("❌ Erreur lors de la génération du résumé. Veuillez réessayer.");
            summaryLabel.setStyle(
                "-fx-font-size: 14px; " +
                "-fx-text-fill: #D32F2F; " +
                "-fx-font-style: italic;"
            );
            loadingLabel.setVisible(false);
            loadingLabel.setManaged(false);
        });
        
        new Thread(summarizeTask).start();
    }
    
    private Connection getConnection() throws SQLException {
        return org.esprit.finovate.utils.DatabaseConfig.getConnection();
    }

    private VBox createDefaultPostIconBox() {
        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setPrefSize(120, 150);
        iconBox.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4CAF50, #2E7D32);"
        );
        
        Label iconLabel = new Label("📝");
        iconLabel.setStyle(
            "-fx-font-size: 48px; " +
            "-fx-text-fill: white;"
        );
        
        iconBox.getChildren().add(iconLabel);
        return iconBox;
    }
    
    private Button createStyledButton(String text, String bgColor, String hoverColor) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: " + bgColor + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 8 16; " +
            "-fx-background-radius: 20; " +
            "-fx-cursor: hand; " +
            "-fx-border-width: 0;"
        );
        
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + hoverColor + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 8 16; " +
            "-fx-background-radius: 20; " +
            "-fx-cursor: hand; " +
            "-fx-border-width: 0;"
        ));
        
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + bgColor + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 8 16; " +
            "-fx-background-radius: 20; " +
            "-fx-cursor: hand; " +
            "-fx-border-width: 0;"
        ));
        
        return btn;
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

    // Méthodes utilitaires pour créer des boutons stylisés
    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: " + color + "; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 6 12; " +
            "-fx-cursor: hand; " +
            "-fx-border-width: 0; " +
            "-fx-background-radius: 4;"
        );
        
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: #F6F7F8; " +
            "-fx-text-fill: " + color + "; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 6 12; " +
            "-fx-cursor: hand; " +
            "-fx-border-width: 0; " +
            "-fx-background-radius: 4;"
        ));
        
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: " + color + "; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 6 12; " +
            "-fx-cursor: hand; " +
            "-fx-border-width: 0; " +
            "-fx-background-radius: 4;"
        ));
        
        return btn;
    }
    
    private String getTimeAgo(Timestamp timestamp) {
        long diff = System.currentTimeMillis() - timestamp.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) return days + "j";
        if (hours > 0) return hours + "h";
        if (minutes > 0) return minutes + "min";
        return "maintenant";
    }
}