package org.example.controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Gestionnaire de notifications en temps réel
 * Affiche des alertes quand un nouveau post est créé dans un forum rejoint
 */
public class NotificationManager {
    
    private VBox notificationContainer;
    private int currentUserId;
    private ScheduledExecutorService scheduler;
    private Set<Integer> shownNotifications = new HashSet<>();
    private Timestamp lastCheckTime;
    
    public NotificationManager(VBox notificationContainer, int currentUserId) {
        this.notificationContainer = notificationContainer;
        this.currentUserId = currentUserId;
        this.lastCheckTime = new Timestamp(System.currentTimeMillis());
        
        // Configurer le conteneur
        notificationContainer.setAlignment(Pos.TOP_RIGHT);
        notificationContainer.setPadding(new Insets(20));
        notificationContainer.setSpacing(10);
        notificationContainer.setMouseTransparent(true);
        notificationContainer.setPickOnBounds(false);
    }
    
    /**
     * Démarre la vérification périodique des nouvelles notifications
     */
    public void startMonitoring() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });
        
        // Vérifier toutes les 5 secondes
        scheduler.scheduleAtFixedRate(this::checkForNewPosts, 5, 5, TimeUnit.SECONDS);
    }
    
    /**
     * Arrête la vérification
     */
    public void stopMonitoring() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
    
    /**
     * Vérifie s'il y a de nouveaux posts dans les forums rejoints
     */
    private void checkForNewPosts() {
        String query = "SELECT p.id, p.title, p.created_at, u.username, f.name as forum_name, f.id as forum_id " +
                "FROM posts p " +
                "INNER JOIN forums f ON p.forum_id = f.id " +
                "INNER JOIN users u ON p.author_id = u.id " +
                "INNER JOIN user_forum uf ON f.id = uf.forum_id " +
                "WHERE uf.user_id = ? " +
                "AND p.author_id != ? " +
                "AND p.created_at > ? " +
                "ORDER BY p.created_at DESC " +
                "LIMIT 5";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, currentUserId);
            stmt.setInt(2, currentUserId);
            stmt.setTimestamp(3, lastCheckTime);
            
            ResultSet rs = stmt.executeQuery();
            
            List<NotificationData> newNotifications = new ArrayList<>();
            
            while (rs.next()) {
                int postId = rs.getInt("id");
                
                // Éviter les doublons
                if (!shownNotifications.contains(postId)) {
                    NotificationData notification = new NotificationData(
                        postId,
                        rs.getString("title"),
                        rs.getString("username"),
                        rs.getString("forum_name"),
                        rs.getInt("forum_id"),
                        rs.getTimestamp("created_at")
                    );
                    newNotifications.add(notification);
                    shownNotifications.add(postId);
                }
            }
            
            // Afficher les notifications sur le thread JavaFX
            if (!newNotifications.isEmpty()) {
                Platform.runLater(() -> {
                    for (NotificationData notification : newNotifications) {
                        showNotification(notification);
                    }
                });
            }
            
            // Mettre à jour le timestamp
            lastCheckTime = new Timestamp(System.currentTimeMillis());
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Affiche une notification
     */
    private void showNotification(NotificationData data) {
        VBox notificationBox = new VBox(8);
        notificationBox.setPadding(new Insets(15));
        notificationBox.setMaxWidth(350);
        notificationBox.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #1877F2;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 15, 0, 0, 5);"
        );
        notificationBox.setMouseTransparent(false);
        notificationBox.setPickOnBounds(true);
        
        // Header avec icône
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label("🔔");
        iconLabel.setStyle("-fx-font-size: 24px;");
        
        VBox textBox = new VBox(3);
        
        Label titleLabel = new Label("Nouveau Post");
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1877F2;");
        
        Label forumLabel = new Label("📁 " + data.forumName);
        forumLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        textBox.getChildren().addAll(titleLabel, forumLabel);
        header.getChildren().addAll(iconLabel, textBox);
        
        // Contenu
        Label postTitleLabel = new Label(data.postTitle);
        postTitleLabel.setWrapText(true);
        postTitleLabel.setMaxWidth(320);
        postTitleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        
        Label authorLabel = new Label("Par: " + data.authorName);
        authorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");
        
        // Bouton fermer
        Label closeBtn = new Label("✕");
        closeBtn.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-text-fill: #999;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 5;"
        );
        closeBtn.setOnMouseClicked(e -> removeNotification(notificationBox));
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(closeBtn.getStyle() + "-fx-text-fill: #F44336;"));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle(closeBtn.getStyle().replace("-fx-text-fill: #F44336;", "-fx-text-fill: #999;")));
        
        // Positionner le bouton fermer en haut à droite
        StackPane topBar = new StackPane();
        topBar.getChildren().addAll(header, closeBtn);
        StackPane.setAlignment(header, Pos.CENTER_LEFT);
        StackPane.setAlignment(closeBtn, Pos.TOP_RIGHT);
        
        notificationBox.getChildren().addAll(topBar, postTitleLabel, authorLabel);
        
        // Clic sur la notification pour ouvrir le post (optionnel)
        notificationBox.setOnMouseClicked(e -> {
            if (e.getTarget() != closeBtn) {
                System.out.println("Ouvrir post ID: " + data.postId);
                // TODO: Intégrer avec OverlayManager pour ouvrir le post
                removeNotification(notificationBox);
            }
        });
        
        // Effet hover
        notificationBox.setOnMouseEntered(e -> {
            notificationBox.setStyle(notificationBox.getStyle() + "-fx-cursor: hand;");
        });
        
        // Ajouter au conteneur
        notificationContainer.getChildren().add(notificationBox);
        
        // Animation d'entrée
        animateIn(notificationBox);
        
        // Auto-suppression après 10 secondes
        Timeline autoRemove = new Timeline(new KeyFrame(Duration.seconds(10), e -> {
            removeNotification(notificationBox);
        }));
        autoRemove.play();
    }
    
    /**
     * Animation d'apparition
     */
    private void animateIn(VBox notification) {
        notification.setTranslateX(400);
        notification.setOpacity(0);
        
        TranslateTransition slide = new TranslateTransition(Duration.millis(300), notification);
        slide.setFromX(400);
        slide.setToX(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        
        FadeTransition fade = new FadeTransition(Duration.millis(300), notification);
        fade.setFromValue(0);
        fade.setToValue(1);
        
        ParallelTransition parallel = new ParallelTransition(slide, fade);
        parallel.play();
    }
    
    /**
     * Supprime une notification avec animation
     */
    private void removeNotification(VBox notification) {
        TranslateTransition slide = new TranslateTransition(Duration.millis(250), notification);
        slide.setToX(400);
        slide.setInterpolator(Interpolator.EASE_IN);
        
        FadeTransition fade = new FadeTransition(Duration.millis(250), notification);
        fade.setToValue(0);
        
        ParallelTransition parallel = new ParallelTransition(slide, fade);
        parallel.setOnFinished(e -> notificationContainer.getChildren().remove(notification));
        parallel.play();
    }
    
    /**
     * Nettoie les anciennes notifications de la mémoire
     */
    public void cleanupOldNotifications() {
        // Garder seulement les 100 dernières notifications en mémoire
        if (shownNotifications.size() > 100) {
            shownNotifications.clear();
        }
    }
    
    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/fintechforum";
        String user = "root";
        String password = "";
        return DriverManager.getConnection(url, user, password);
    }
    
    /**
     * Classe pour stocker les données d'une notification
     */
    private static class NotificationData {
        int postId;
        String postTitle;
        String authorName;
        String forumName;
        int forumId;
        Timestamp createdAt;
        
        NotificationData(int postId, String postTitle, String authorName, 
                        String forumName, int forumId, Timestamp createdAt) {
            this.postId = postId;
            this.postTitle = postTitle;
            this.authorName = authorName;
            this.forumName = forumName;
            this.forumId = forumId;
            this.createdAt = createdAt;
        }
    }
}
