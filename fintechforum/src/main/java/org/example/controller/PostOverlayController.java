package org.example.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.sql.*;

/**
 * Contrôleur pour afficher un post dans un overlay (style Reddit/Facebook)
 */
public class PostOverlayController {
    
    private VBox rootContainer;
    private OverlayManager overlayManager;
    private int postId;
    private int currentUserId;
    
    public PostOverlayController(OverlayManager overlayManager, int postId, int currentUserId) {
        this.overlayManager = overlayManager;
        this.postId = postId;
        this.currentUserId = currentUserId;
        this.rootContainer = new VBox(0);
        buildUI();
        loadPostData();
    }
    
    public VBox getView() {
        return rootContainer;
    }
    
    private void buildUI() {
        rootContainer.setStyle("-fx-background-color: white;");
        
        // Header avec bouton fermer
        HBox header = createHeader();
        
        // Contenu scrollable
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: white; -fx-border-color: transparent;");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setId("postContent");
        
        scrollPane.setContent(content);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        rootContainer.getChildren().addAll(header, scrollPane);
    }
    
    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
            "-fx-background-color: linear-gradient(to right, #1877F2, #4A9AFF);" +
            "-fx-border-color: transparent transparent #E1E8ED transparent;" +
            "-fx-border-width: 0 0 1 0;"
        );
        
        Label titleLabel = new Label("📄 Détails du Post");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.2);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 5 12;" +
            "-fx-background-radius: 20;" +
            "-fx-cursor: hand;"
        );
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(
            closeBtn.getStyle() + "-fx-background-color: rgba(255, 255, 255, 0.3);"
        ));
        closeBtn.setOnAction(e -> overlayManager.closeTopOverlay());
        
        header.getChildren().addAll(titleLabel, spacer, closeBtn);
        return header;
    }
    
    private void loadPostData() {
        String query = "SELECT p.title, p.content, p.created_at, u.username, f.name as forum_name, " +
                "(SELECT COUNT(*) FROM comments WHERE post_id = p.id) as comment_count " +
                "FROM posts p " +
                "INNER JOIN users u ON p.author_id = u.id " +
                "INNER JOIN forums f ON p.forum_id = f.id " +
                "WHERE p.id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                VBox content = (VBox) ((ScrollPane) rootContainer.getChildren().get(1)).getContent();
                content.getChildren().clear();
                
                // Titre du post
                Label titleLabel = new Label(rs.getString("title"));
                titleLabel.setWrapText(true);
                titleLabel.setStyle(
                    "-fx-font-size: 24px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #2C3E50;"
                );
                
                // Info auteur et forum
                HBox infoBox = new HBox(15);
                infoBox.setAlignment(Pos.CENTER_LEFT);
                infoBox.setPadding(new Insets(10, 0, 10, 0));
                
                Label authorLabel = new Label("👤 " + rs.getString("username"));
                authorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #1877F2; -fx-font-weight: bold;");
                
                Label forumLabel = new Label("📁 " + rs.getString("forum_name"));
                forumLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
                
                Label dateLabel = new Label("📅 " + rs.getTimestamp("created_at").toString().substring(0, 16));
                dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #999;");
                
                Label commentsLabel = new Label("💬 " + rs.getInt("comment_count") + " commentaires");
                commentsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #999;");
                
                infoBox.getChildren().addAll(authorLabel, forumLabel, dateLabel, commentsLabel);
                
                // Séparateur
                Separator sep1 = new Separator();
                sep1.setStyle("-fx-background-color: #E1E8ED;");
                
                // Contenu du post
                TextFlow contentFlow = new TextFlow();
                Text contentText = new Text(rs.getString("content"));
                contentText.setStyle("-fx-font-size: 15px; -fx-fill: #333; -fx-line-spacing: 5px;");
                contentFlow.getChildren().add(contentText);
                contentFlow.setPadding(new Insets(15, 0, 15, 0));
                
                // Séparateur
                Separator sep2 = new Separator();
                sep2.setStyle("-fx-background-color: #E1E8ED;");
                
                // Actions
                HBox actionsBox = createActionsBox();
                
                // Section commentaires
                VBox commentsSection = createCommentsSection();
                
                content.getChildren().addAll(
                    titleLabel,
                    infoBox,
                    sep1,
                    contentFlow,
                    sep2,
                    actionsBox,
                    commentsSection
                );
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de chargement du post");
        }
    }
    
    private HBox createActionsBox() {
        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER_LEFT);
        actionsBox.setPadding(new Insets(10, 0, 15, 0));
        
        Button likeBtn = new Button("👍 J'aime");
        likeBtn.setStyle(
            "-fx-background-color: #E3F2FD;" +
            "-fx-text-fill: #1877F2;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 20;" +
            "-fx-background-radius: 20;" +
            "-fx-cursor: hand;"
        );
        
        Button shareBtn = new Button("📤 Partager");
        shareBtn.setStyle(
            "-fx-background-color: #E8F5E9;" +
            "-fx-text-fill: #4CAF50;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 20;" +
            "-fx-background-radius: 20;" +
            "-fx-cursor: hand;"
        );
        shareBtn.setOnAction(e -> sharePost());
        
        actionsBox.getChildren().addAll(likeBtn, shareBtn);
        return actionsBox;
    }
    
    private VBox createCommentsSection() {
        VBox commentsSection = new VBox(15);
        
        Label commentsTitle = new Label("💬 Commentaires");
        commentsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        
        // Zone d'ajout de commentaire
        VBox addCommentBox = new VBox(10);
        addCommentBox.setPadding(new Insets(15));
        addCommentBox.setStyle(
            "-fx-background-color: #F8F9FA;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #E1E8ED;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 10;"
        );
        
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Ajouter un commentaire...");
        commentArea.setPrefRowCount(3);
        commentArea.setWrapText(true);
        commentArea.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #E1E8ED;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-font-size: 14px;"
        );
        
        Button addCommentBtn = new Button("💬 Publier le commentaire");
        addCommentBtn.setStyle(
            "-fx-background-color: #1877F2;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 20;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        addCommentBtn.setOnAction(e -> addComment(commentArea));
        
        addCommentBox.getChildren().addAll(commentArea, addCommentBtn);
        
        // Liste des commentaires
        VBox commentsList = new VBox(10);
        commentsList.setId("commentsList");
        loadComments(commentsList);
        
        commentsSection.getChildren().addAll(commentsTitle, addCommentBox, commentsList);
        return commentsSection;
    }
    
    private void loadComments(VBox commentsList) {
        commentsList.getChildren().clear();
        
        String query = "SELECT c.id, c.content, c.created_at, u.username, c.author_id " +
                "FROM comments c " +
                "INNER JOIN users u ON c.author_id = u.id " +
                "WHERE c.post_id = ? " +
                "ORDER BY c.created_at DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                VBox commentCard = createCommentCard(
                    rs.getInt("id"),
                    rs.getString("content"),
                    rs.getString("username"),
                    rs.getTimestamp("created_at").toString().substring(0, 16),
                    rs.getInt("author_id")
                );
                commentsList.getChildren().add(commentCard);
            }
            
            if (commentsList.getChildren().isEmpty()) {
                Label emptyLabel = new Label("Aucun commentaire pour le moment");
                emptyLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-padding: 20;");
                commentsList.getChildren().add(emptyLabel);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private VBox createCommentCard(int commentId, String content, String author, String date, int authorId) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(
            "-fx-background-color: #F8F9FA;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #E1E8ED;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 10;"
        );
        
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label authorLabel = new Label("👤 " + author);
        authorLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1877F2;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label dateLabel = new Label(date);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");
        
        headerBox.getChildren().addAll(authorLabel, spacer, dateLabel);
        
        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
        
        card.getChildren().addAll(headerBox, contentLabel);
        
        // Boutons si c'est le commentaire de l'utilisateur
        if (authorId == currentUserId) {
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            
            Button deleteBtn = new Button("🗑️ Supprimer");
            deleteBtn.setStyle(
                "-fx-background-color: #FFEBEE;" +
                "-fx-text-fill: #F44336;" +
                "-fx-font-size: 12px;" +
                "-fx-padding: 5 15;" +
                "-fx-background-radius: 15;" +
                "-fx-cursor: hand;"
            );
            deleteBtn.setOnAction(e -> deleteComment(commentId));
            
            buttonBox.getChildren().add(deleteBtn);
            card.getChildren().add(buttonBox);
        }
        
        return card;
    }
    
    private void addComment(TextArea commentArea) {
        String commentContent = commentArea.getText().trim();
        
        if (commentContent.isEmpty()) {
            showError("Le commentaire ne peut pas être vide");
            return;
        }
        
        String query = "INSERT INTO comments (post_id, author_id, content, created_at) VALUES (?, ?, ?, NOW())";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, postId);
            stmt.setInt(2, currentUserId);
            stmt.setString(3, commentContent);
            stmt.executeUpdate();
            
            commentArea.clear();
            
            // Recharger les commentaires
            VBox postContent = (VBox) ((ScrollPane) rootContainer.getChildren().get(1)).getContent();
            VBox commentsSection = (VBox) postContent.getChildren().get(postContent.getChildren().size() - 1);
            VBox commentsList = (VBox) commentsSection.getChildren().get(2);
            loadComments(commentsList);
            
            showInfo("Commentaire ajouté !");
            
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
                
                // Recharger les commentaires
                VBox content = (VBox) ((ScrollPane) rootContainer.getChildren().get(1)).getContent();
                VBox commentsSection = (VBox) content.getChildren().get(content.getChildren().size() - 1);
                VBox commentsList = (VBox) commentsSection.getChildren().get(2);
                loadComments(commentsList);
                
                showInfo("Commentaire supprimé");
                
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur de suppression");
            }
        }
    }
    
    private void sharePost() {
        String query = "INSERT INTO shared_posts (user_id, post_id, shared_at) VALUES (?, ?, NOW())";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, currentUserId);
            stmt.setInt(2, postId);
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
