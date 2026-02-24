package org.example.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Centre de notifications - Affiche toutes les notifications dans un overlay
 */
public class NotificationCenterController {
    
    private VBox rootContainer;
    private OverlayManager overlayManager;
    private int currentUserId;
    private VBox notificationsList;
    
    public NotificationCenterController(OverlayManager overlayManager, int currentUserId) {
        this.overlayManager = overlayManager;
        this.currentUserId = currentUserId;
        this.rootContainer = new VBox(0);
        buildUI();
        loadNotifications();
    }
    
    public VBox getView() {
        return rootContainer;
    }
    
    private void buildUI() {
        rootContainer.setStyle("-fx-background-color: white;");
        
        // Header
        HBox header = createHeader();
        
        // Contenu scrollable
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: white; -fx-border-color: transparent;");
        
        notificationsList = new VBox(10);
        notificationsList.setPadding(new Insets(20));
        
        scrollPane.setContent(notificationsList);
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
        
        Label titleLabel = new Label("🔔 Centre de Notifications");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button markAllReadBtn = new Button("✓ Tout marquer lu");
        markAllReadBtn.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.2);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 8 15;" +
            "-fx-background-radius: 15;" +
            "-fx-cursor: hand;"
        );
        markAllReadBtn.setOnAction(e -> markAllAsRead());
        
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
        closeBtn.setOnAction(e -> overlayManager.closeTopOverlay());
        
        header.getChildren().addAll(titleLabel, spacer, markAllReadBtn, closeBtn);
        return header;
    }
    
    private void loadNotifications() {
        notificationsList.getChildren().clear();
        
        String query = "SELECT p.id, p.title, p.created_at, u.username, f.name as forum_name, " +
                "f.id as forum_id, " +
                "(SELECT COUNT(*) FROM comments WHERE post_id = p.id) as comment_count " +
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
            
            List<NotificationItem> notifications = new ArrayList<>();
            
            while (rs.next()) {
                notifications.add(new NotificationItem(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("username"),
                    rs.getString("forum_name"),
                    rs.getInt("forum_id"),
                    rs.getInt("comment_count"),
                    rs.getTimestamp("created_at")
                ));
            }
            
            if (notifications.isEmpty()) {
                Label emptyLabel = new Label("Aucune notification récente");
                emptyLabel.setStyle(
                    "-fx-font-size: 16px;" +
                    "-fx-text-fill: #999;" +
                    "-fx-font-style: italic;" +
                    "-fx-padding: 50;"
                );
                notificationsList.getChildren().add(emptyLabel);
            } else {
                for (NotificationItem notification : notifications) {
                    notificationsList.getChildren().add(createNotificationCard(notification));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de chargement des notifications");
        }
    }
    
    private VBox createNotificationCard(NotificationItem notification) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(
            "-fx-background-color: #F8F9FA;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #E1E8ED;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 10;" +
            "-fx-cursor: hand;"
        );
        
        // Header
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label("📄");
        iconLabel.setStyle("-fx-font-size: 20px;");
        
        VBox infoBox = new VBox(3);
        
        Label forumLabel = new Label("📁 " + notification.forumName);
        forumLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1877F2;");
        
        Label timeLabel = new Label(getTimeAgo(notification.createdAt));
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");
        
        infoBox.getChildren().addAll(forumLabel, timeLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        headerBox.getChildren().addAll(iconLabel, infoBox, spacer);
        
        // Titre du post
        Label titleLabel = new Label(notification.postTitle);
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        
        // Info auteur et commentaires
        HBox footerBox = new HBox(15);
        footerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label authorLabel = new Label("👤 " + notification.authorName);
        authorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        Label commentsLabel = new Label("💬 " + notification.commentCount + " commentaires");
        commentsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        footerBox.getChildren().addAll(authorLabel, commentsLabel);
        
        card.getChildren().addAll(headerBox, titleLabel, footerBox);
        
        // Effet hover
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: #E3F2FD;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #1877F2;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 10;" +
                "-fx-cursor: hand;"
            );
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: #F8F9FA;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #E1E8ED;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 10;" +
                "-fx-cursor: hand;"
            );
        });
        
        // Clic pour ouvrir le post
        card.setOnMouseClicked(e -> {
            openPost(notification.postId);
        });
        
        return card;
    }
    
    private void openPost(int postId) {
        // Fermer le centre de notifications
        overlayManager.closeTopOverlay();
        
        // Ouvrir le post
        PostOverlayController postOverlay = new PostOverlayController(
            overlayManager,
            postId,
            currentUserId
        );
        
        overlayManager.openOverlay(
            postOverlay.getView(),
            OverlayManager.OverlayType.FULL_POST
        );
    }
    
    private void markAllAsRead() {
        // TODO: Implémenter la logique de marquage comme lu
        showInfo("Toutes les notifications ont été marquées comme lues");
        overlayManager.closeTopOverlay();
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
    
    /**
     * Classe pour stocker les données d'une notification
     */
    private static class NotificationItem {
        int postId;
        String postTitle;
        String authorName;
        String forumName;
        int forumId;
        int commentCount;
        Timestamp createdAt;
        
        NotificationItem(int postId, String postTitle, String authorName, 
                        String forumName, int forumId, int commentCount, Timestamp createdAt) {
            this.postId = postId;
            this.postTitle = postTitle;
            this.authorName = authorName;
            this.forumName = forumName;
            this.forumId = forumId;
            this.commentCount = commentCount;
            this.createdAt = createdAt;
        }
    }
}
