package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import org.esprit.finovate.utils.ImageUtils;
import org.esprit.finovate.services.ModerationService;
import javafx.concurrent.Task;

import java.sql.*;

public class PostDetailsController {

    @FXML private Label titleLabel;
    @FXML private Label authorLabel;
    @FXML private Label dateLabel;
    @FXML private Label contentLabel;
    @FXML private Label commentCountLabel;
    @FXML private ImageView postImageView;
    @FXML private VBox imageContainer;
    @FXML private Button likeBtn;
    @FXML private Button dislikeBtn;
    @FXML private Button shareBtn;
    @FXML private VBox commentsBox;
    @FXML private TextArea newCommentArea;
    @FXML private Button backBtn;
    @FXML private Button addCommentBtn;

    private MainController mainController; // Optional - for backward compatibility
    private ForumsPageController forumsPageController; // New - for direct navigation
    private StackPane parentContentArea; // For navigation back
    private int currentPostId;
    private int currentUserId;
    private int currentForumId; // Store forum ID for back navigation
    private String currentForumName; // Store forum name for back navigation
    private int upvotes = 0;
    private int downvotes = 0;

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

    public void loadPostDetails(int postId, int userId) {
        this.currentPostId = postId;
        this.currentUserId = userId;
        
        loadPostInfo();
        loadComments();
        setupActionButtons();
    }

    private void loadPostInfo() {
        String query = "SELECT p.title, p.content, p.created_at, p.image_url, p.forum_id, f.title as forum_name, CONCAT(u.firstname, ' ', u.lastname) as username, " +
                "(SELECT COUNT(*) FROM comments WHERE post_id = p.id) as comment_count, " +
                "(SELECT COUNT(*) FROM votes WHERE post_id = p.id AND vote_type = 'UPVOTE') as upvotes, " +
                "(SELECT COUNT(*) FROM votes WHERE post_id = p.id AND vote_type = 'DOWNVOTE') as downvotes " +
                "FROM posts p " +
                "INNER JOIN user u ON p.author_id = u.id " +
                "INNER JOIN forums f ON p.forum_id = f.id " +
                "WHERE p.id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentPostId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                titleLabel.setText(rs.getString("title"));
                authorLabel.setText("👤 " + rs.getString("username"));
                
                Timestamp createdAt = rs.getTimestamp("created_at");
                dateLabel.setText("📅 " + getTimeAgo(createdAt));
                
                contentLabel.setText(rs.getString("content"));
                
                // Store forum info for back navigation
                currentForumId = rs.getInt("forum_id");
                currentForumName = rs.getString("forum_name");
                
                int commentCount = rs.getInt("comment_count");
                commentCountLabel.setText("💬 " + commentCount + " commentaire" + (commentCount > 1 ? "s" : ""));
                
                upvotes = rs.getInt("upvotes");
                downvotes = rs.getInt("downvotes");
                likeBtn.setText("👍 J'aime (" + upvotes + ")");
                dislikeBtn.setText("👎 Je n'aime pas (" + downvotes + ")");
                
                // Charger l'image
                String imageUrl = rs.getString("image_url");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    ImageView loadedImage = ImageUtils.loadImageView(imageUrl, 120, 200);
                    if (loadedImage != null) {
                        postImageView.setImage(loadedImage.getImage());
                        postImageView.setVisible(true);
                        
                        // Rendre l'image plus claire
                        javafx.scene.effect.ColorAdjust colorAdjust = new javafx.scene.effect.ColorAdjust();
                        colorAdjust.setBrightness(0.2);
                        postImageView.setEffect(colorAdjust);
                    } else {
                        showDefaultIcon();
                    }
                } else {
                    showDefaultIcon();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de chargement du post");
        }
    }
    
    private void showDefaultIcon() {
        // Cacher l'ImageView et afficher l'icône par défaut
        postImageView.setVisible(false);
        Label iconLabel = new Label("📝");
        iconLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: white;");
        imageContainer.getChildren().clear();
        imageContainer.getChildren().add(iconLabel);
    }
    
    private void setupActionButtons() {
        // Bouton J'aime (icône + compteur)
        likeBtn.setText("👍 " + upvotes);
        likeBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #666; " +
            "-fx-font-size: 16px; " +
            "-fx-padding: 6 12; " +
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
            "-fx-padding: 6 12; " +
            "-fx-cursor: hand; " +
            "-fx-border-width: 0; " +
            "-fx-background-radius: 6;"
        ));
        likeBtn.setOnMouseExited(e -> likeBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #666; " +
            "-fx-font-size: 16px; " +
            "-fx-padding: 6 12; " +
            "-fx-cursor: hand; " +
            "-fx-border-width: 0;"
        ));
        likeBtn.setOnAction(e -> votePost("UPVOTE"));
        
        // Bouton Je n'aime pas (icône + compteur)
        dislikeBtn.setText("👎 " + downvotes);
        dislikeBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #666; " +
            "-fx-font-size: 16px; " +
            "-fx-padding: 6 12; " +
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
            "-fx-padding: 6 12; " +
            "-fx-cursor: hand; " +
            "-fx-border-width: 0; " +
            "-fx-background-radius: 6;"
        ));
        dislikeBtn.setOnMouseExited(e -> dislikeBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #666; " +
            "-fx-font-size: 16px; " +
            "-fx-padding: 6 12; " +
            "-fx-cursor: hand; " +
            "-fx-border-width: 0;"
        ));
        dislikeBtn.setOnAction(e -> votePost("DOWNVOTE"));
        
        // Bouton Partager (icône seulement)
        shareBtn.setText("📤");
        shareBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #666; " +
            "-fx-font-size: 16px; " +
            "-fx-padding: 6 12; " +
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
            "-fx-padding: 6 12; " +
            "-fx-cursor: hand; " +
            "-fx-border-width: 0; " +
            "-fx-background-radius: 6;"
        ));
        shareBtn.setOnMouseExited(e -> shareBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #666; " +
            "-fx-font-size: 16px; " +
            "-fx-padding: 6 12; " +
            "-fx-cursor: hand; " +
            "-fx-border-width: 0;"
        ));
        shareBtn.setOnAction(e -> sharePost());
    }
    
    private void votePost(String voteType) {
        String checkQuery = "SELECT vote_type FROM votes WHERE post_id = ? AND user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

            checkStmt.setInt(1, currentPostId);
            checkStmt.setInt(2, currentUserId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String existingVote = rs.getString("vote_type");
                
                if (existingVote.equals(voteType)) {
                    // Retirer le vote
                    String deleteQuery = "DELETE FROM votes WHERE post_id = ? AND user_id = ?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                        deleteStmt.setInt(1, currentPostId);
                        deleteStmt.setInt(2, currentUserId);
                        deleteStmt.executeUpdate();
                    }
                } else {
                    // Changer le vote
                    String updateQuery = "UPDATE votes SET vote_type = ? WHERE post_id = ? AND user_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, voteType);
                        updateStmt.setInt(2, currentPostId);
                        updateStmt.setInt(3, currentUserId);
                        updateStmt.executeUpdate();
                    }
                }
            } else {
                // Ajouter un nouveau vote
                String insertQuery = "INSERT INTO votes (post_id, user_id, vote_type) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setInt(1, currentPostId);
                    insertStmt.setInt(2, currentUserId);
                    insertStmt.setString(3, voteType);
                    insertStmt.executeUpdate();
                }
            }

            loadPostInfo(); // Recharger pour mettre à jour les compteurs

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors du vote");
        }
    }
    
    private void sharePost() {
        String query = "INSERT INTO shared_posts (post_id, user_id, shared_at) VALUES (?, ?, NOW())";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentPostId);
            stmt.setInt(2, currentUserId);
            stmt.executeUpdate();

            showInfo("Post partagé avec succès !");

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                showError("Vous avez déjà partagé ce post");
            } else {
                e.printStackTrace();
                showError("Erreur lors du partage");
            }
        }
    }
    
    private String getTimeAgo(Timestamp timestamp) {
        long diff = System.currentTimeMillis() - timestamp.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) return "il y a " + days + " jour" + (days > 1 ? "s" : "");
        if (hours > 0) return "il y a " + hours + " heure" + (hours > 1 ? "s" : "");
        if (minutes > 0) return "il y a " + minutes + " minute" + (minutes > 1 ? "s" : "");
        return "à l'instant";
    }

    private void loadComments() {
        commentsBox.getChildren().clear();

        String query = "SELECT c.id, c.content, c.created_at, CONCAT(u.firstname, ' ', u.lastname) as username, c.author_id " +
                "FROM comments c " +
                "INNER JOIN user u ON c.author_id = u.id " +
                "WHERE c.post_id = ? " +
                "ORDER BY c.created_at ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentPostId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                VBox commentCard = createCommentCard(
                    rs.getInt("id"),
                    rs.getString("content"),
                    rs.getString("username"),
                    rs.getTimestamp("created_at"),
                    rs.getInt("author_id")
                );
                commentsBox.getChildren().add(commentCard);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de chargement des commentaires");
        }
    }

    private VBox createCommentCard(int commentId, String content, String author, Timestamp createdAt, int authorId) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #E0E0E0; " +
                "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Header avec auteur et date
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label authorLabel = new Label("👤 " + author);
        authorLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #2E7D32;");
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Label dateLabel = new Label("📅 " + getTimeAgo(createdAt));
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        headerBox.getChildren().addAll(authorLabel, spacer, dateLabel);

        // Contenu du commentaire
        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

        card.getChildren().addAll(headerBox, contentLabel);

        // Barre d'actions (toujours visible mais discrète)
        HBox actionBox = new HBox(15);
        actionBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        actionBox.setStyle("-fx-padding: 5 0 0 0;");
        
        // Bouton Répondre (pour tous)
        Button replyBtn = new Button("↩");
        replyBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #666; " +
            "-fx-font-size: 16px; " +
            "-fx-padding: 4 8; " +
            "-fx-cursor: hand; " +
            "-fx-border-width: 0;"
        );
        Tooltip replyTooltip = new Tooltip("Répondre");
        replyTooltip.setStyle("-fx-font-size: 12px;");
        Tooltip.install(replyBtn, replyTooltip);
        
        replyBtn.setOnMouseEntered(e -> replyBtn.setStyle(
            "-fx-background-color: #E8F5E9; " +
            "-fx-text-fill: #4CAF50; " +
            "-fx-font-size: 16px; " +
            "-fx-padding: 4 8; " +
            "-fx-cursor: hand; " +
            "-fx-border-width: 0; " +
            "-fx-background-radius: 4;"
        ));
        replyBtn.setOnMouseExited(e -> replyBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #666; " +
            "-fx-font-size: 16px; " +
            "-fx-padding: 4 8; " +
            "-fx-cursor: hand; " +
            "-fx-border-width: 0;"
        ));
        replyBtn.setOnAction(e -> replyToComment(author, card));
        
        actionBox.getChildren().add(replyBtn);

        // Si c'est le commentaire de l'utilisateur, ajouter modifier/supprimer
        if (authorId == currentUserId) {
            // Bouton Modifier (icône seulement)
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
            
            editBtn.setOnMouseEntered(e -> editBtn.setStyle(
                "-fx-background-color: #FFF3E0; " +
                "-fx-text-fill: #FF9800; " +
                "-fx-font-size: 16px; " +
                "-fx-padding: 4 8; " +
                "-fx-cursor: hand; " +
                "-fx-border-width: 0; " +
                "-fx-background-radius: 4;"
            ));
            editBtn.setOnMouseExited(e -> editBtn.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-text-fill: #666; " +
                "-fx-font-size: 16px; " +
                "-fx-padding: 4 8; " +
                "-fx-cursor: hand; " +
                "-fx-border-width: 0;"
            ));
            editBtn.setOnAction(e -> editComment(commentId, content, card));
            
            // Bouton Supprimer (icône seulement)
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
            
            deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(
                "-fx-background-color: #FFEBEE; " +
                "-fx-text-fill: #F44336; " +
                "-fx-font-size: 16px; " +
                "-fx-padding: 4 8; " +
                "-fx-cursor: hand; " +
                "-fx-border-width: 0; " +
                "-fx-background-radius: 4;"
            ));
            deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-text-fill: #666; " +
                "-fx-font-size: 16px; " +
                "-fx-padding: 4 8; " +
                "-fx-cursor: hand; " +
                "-fx-border-width: 0;"
            ));
            deleteBtn.setOnAction(e -> deleteComment(commentId));
            
            actionBox.getChildren().addAll(editBtn, deleteBtn);
        }
        
        card.getChildren().add(actionBox);

        return card;
    }
    
    private void replyToComment(String authorName, VBox commentCard) {
        // Créer une zone de réponse sous le commentaire
        VBox replyBox = new VBox(10);
        replyBox.setPadding(new Insets(10, 0, 0, 30)); // Indentation à gauche
        replyBox.setStyle("-fx-background-color: transparent;");
        
        Label replyLabel = new Label("↩ Répondre à " + authorName);
        replyLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #4CAF50;");
        
        TextArea replyArea = new TextArea();
        replyArea.setPromptText("Écrivez votre réponse...");
        replyArea.setWrapText(true);
        replyArea.setPrefRowCount(2);
        replyArea.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #4CAF50; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-font-size: 13px;"
        );
        
        HBox replyButtonBox = new HBox(10);
        replyButtonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        Button sendReplyBtn = new Button("Envoyer");
        sendReplyBtn.setStyle(
            "-fx-background-color: #4CAF50; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 6 16; " +
            "-fx-background-radius: 15; " +
            "-fx-cursor: hand;"
        );
        sendReplyBtn.setOnAction(e -> {
            String replyContent = replyArea.getText().trim();
            if (!replyContent.isEmpty()) {
                addReply("@" + authorName + " " + replyContent);
                // Retirer la zone de réponse après envoi
                int index = commentsBox.getChildren().indexOf(commentCard);
                if (index >= 0 && index + 1 < commentsBox.getChildren().size()) {
                    if (commentsBox.getChildren().get(index + 1) == replyBox) {
                        commentsBox.getChildren().remove(replyBox);
                    }
                }
            }
        });
        
        Button cancelReplyBtn = new Button("Annuler");
        cancelReplyBtn.setStyle(
            "-fx-background-color: #757575; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 6 16; " +
            "-fx-background-radius: 15; " +
            "-fx-cursor: hand;"
        );
        cancelReplyBtn.setOnAction(e -> {
            commentsBox.getChildren().remove(replyBox);
        });
        
        replyButtonBox.getChildren().addAll(sendReplyBtn, cancelReplyBtn);
        replyBox.getChildren().addAll(replyLabel, replyArea, replyButtonBox);
        
        // Vérifier si une zone de réponse existe déjà
        int commentIndex = commentsBox.getChildren().indexOf(commentCard);
        if (commentIndex >= 0) {
            // Supprimer toute zone de réponse existante
            if (commentIndex + 1 < commentsBox.getChildren().size()) {
                javafx.scene.Node nextNode = commentsBox.getChildren().get(commentIndex + 1);
                if (nextNode instanceof VBox && ((VBox) nextNode).getStyle().contains("transparent")) {
                    commentsBox.getChildren().remove(nextNode);
                }
            }
            // Ajouter la nouvelle zone de réponse
            commentsBox.getChildren().add(commentIndex + 1, replyBox);
        }
    }
    
    private void addReply(String content) {
        String query = "INSERT INTO comments (post_id, author_id, content, created_at) VALUES (?, ?, ?, NOW())";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentPostId);
            stmt.setInt(2, currentUserId);
            stmt.setString(3, content);
            stmt.executeUpdate();

            loadComments();
            loadPostInfo();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ajout de la réponse");
        }
    }

    @FXML
    private void addComment() {
        String content = newCommentArea.getText().trim();

        if (content.isEmpty()) {
            showError("Le commentaire ne peut pas être vide");
            return;
        }

        // AI MODERATION: Check content before posting
        addCommentBtn.setDisable(true);
        addCommentBtn.setText("⏳ Vérification...");
        
        Task<ModerationService.ModerationResult> moderationTask = new Task<>() {
            @Override
            protected ModerationService.ModerationResult call() {
                return ModerationService.analyzeContent(content);
            }
        };
        
        moderationTask.setOnSucceeded(e -> {
            ModerationService.ModerationResult result = moderationTask.getValue();
            
            addCommentBtn.setDisable(false);
            addCommentBtn.setText("💬 Ajouter un commentaire");
            
            if (result.isToxic) {
                // Show warning dialog
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("⚠️ Contenu Inapproprié Détecté");
                alert.setHeaderText("Attention : Ce commentaire peut être inapproprié");
                alert.setContentText(result.toString());
                alert.getDialogPane().setMinWidth(500);
                
                ButtonType revise = new ButtonType("✏️ Réviser", ButtonBar.ButtonData.CANCEL_CLOSE);
                ButtonType postAnyway = new ButtonType("📤 Publier quand même", ButtonBar.ButtonData.OK_DONE);
                alert.getButtonTypes().setAll(revise, postAnyway);
                
                alert.showAndWait().ifPresent(response -> {
                    if (response == postAnyway) {
                        // User chose to post anyway
                        submitComment(content);
                    }
                    // If revise, do nothing (keep text in textarea)
                });
            } else {
                // Content is safe, post directly
                submitComment(content);
            }
        });
        
        moderationTask.setOnFailed(e -> {
            addCommentBtn.setDisable(false);
            addCommentBtn.setText("💬 Ajouter un commentaire");
            
            // If moderation fails, ask user
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("⚠️ Vérification Impossible");
            alert.setHeaderText("Impossible de vérifier le contenu");
            alert.setContentText("Erreur de modération. Voulez-vous publier quand même ?");
            
            ButtonType yes = new ButtonType("Oui", ButtonBar.ButtonData.OK_DONE);
            ButtonType no = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(yes, no);
            
            alert.showAndWait().ifPresent(response -> {
                if (response == yes) {
                    submitComment(content);
                }
            });
        });
        
        new Thread(moderationTask).start();
    }
    
    /**
     * Submit comment to database (after moderation check)
     */
    private void submitComment(String content) {
        String query = "INSERT INTO comments (post_id, author_id, content, created_at) VALUES (?, ?, ?, NOW())";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentPostId);
            stmt.setInt(2, currentUserId);
            stmt.setString(3, content);
            stmt.executeUpdate();

            newCommentArea.clear();
            loadComments();
            loadPostInfo(); // Recharger pour mettre à jour le compteur de commentaires
            showInfo("✅ Commentaire publié avec succès");

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ajout du commentaire");
        }
    }

    private void deleteComment(int commentId) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer ce commentaire ?");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            String query = "DELETE FROM comments WHERE id = ? AND author_id = ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, commentId);
                stmt.setInt(2, currentUserId);
                stmt.executeUpdate();

                loadComments();

            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur de suppression du commentaire");
            }
        }
    }

    private void editComment(int commentId, String currentContent, VBox card) {
        // Remplacer le contenu par un TextArea éditable
        card.getChildren().clear();
        card.setStyle("-fx-background-color: #FFF3E0; -fx-border-color: #FF9800; " +
                "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label headerLabel = new Label("✏️ Modification en cours...");
        headerLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #E65100; -fx-padding: 0 0 10 0;");

        TextArea editArea = new TextArea(currentContent);
        editArea.setWrapText(true);
        editArea.setPrefRowCount(4);
        editArea.setStyle("-fx-font-size: 14px; -fx-border-color: #FF9800; -fx-border-width: 1; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        buttonBox.setStyle("-fx-padding: 10 0 0 0;");
        
        Button saveBtn = new Button("💾 Enregistrer");
        saveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-font-weight: 600; -fx-padding: 8 20; -fx-border-radius: 15; -fx-background-radius: 15; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            String newContent = editArea.getText().trim();
            if (newContent.isEmpty()) {
                showError("Le commentaire ne peut pas être vide");
                return;
            }
            saveEditedComment(commentId, newContent);
        });

        Button cancelBtn = new Button("❌ Annuler");
        cancelBtn.setStyle("-fx-background-color: #757575; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-font-weight: 600; -fx-padding: 8 20; -fx-border-radius: 15; -fx-background-radius: 15; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> loadComments());

        buttonBox.getChildren().addAll(saveBtn, cancelBtn);
        card.getChildren().addAll(headerLabel, editArea, buttonBox);
    }

    private void saveEditedComment(int commentId, String newContent) {
        String query = "UPDATE comments SET content = ?, updated_at = NOW() WHERE id = ? AND author_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newContent);
            stmt.setInt(2, commentId);
            stmt.setInt(3, currentUserId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                showInfo("Commentaire modifié avec succès");
                loadComments();
            } else {
                showError("Impossible de modifier ce commentaire");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la modification du commentaire");
        }
    }

    @FXML
    private void goBack() {
        // Try to navigate back - use ForumsPageController if available, otherwise MainController
        if (parentContentArea != null && forumsPageController != null) {
            // Direct navigation - load posts view for the current forum
            loadPostsViewForBack();
        } else if (mainController != null) {
            // Legacy navigation through MainController
            mainController.goBackToForums();
        } else {
            System.err.println("ERREUR: Aucun contrôleur parent disponible pour retourner!");
            showError("Erreur: Impossible de retourner. Veuillez réessayer.");
        }
    }
    
    /**
     * Load posts view when going back (without MainController)
     */
    private void loadPostsViewForBack() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/posts.fxml"));
            javafx.scene.Parent postsView = loader.load();

            PostsController controller = loader.getController();
            // Pass mainController if available (optional)
            if (mainController != null) {
                controller.setMainController(mainController);
            }
            // Set parent content area for navigation
            if (forumsPageController != null && parentContentArea != null) {
                controller.setForumsPageController(forumsPageController, parentContentArea);
            }
            controller.loadPosts(currentForumId, currentForumName, currentUserId);

            parentContentArea.getChildren().clear();
            parentContentArea.getChildren().add(postsView);

        } catch (java.io.IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement des posts");
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
