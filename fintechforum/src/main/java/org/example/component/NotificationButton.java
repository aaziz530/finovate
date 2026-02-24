package org.example.component;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.example.controller.NotificationCenterController;
import org.example.controller.OverlayManager;

import java.sql.*;

/**
 * Bouton de notification avec badge de compteur
 */
public class NotificationButton extends StackPane {
    
    private Label iconLabel;
    private Circle badge;
    private Label badgeLabel;
    private int currentUserId;
    private OverlayManager overlayManager;
    private int unreadCount = 0;
    
    public NotificationButton(int currentUserId, OverlayManager overlayManager) {
        this.currentUserId = currentUserId;
        this.overlayManager = overlayManager;
        
        buildUI();
        updateUnreadCount();
        startPeriodicUpdate();
    }
    
    private void buildUI() {
        // Icône de notification
        iconLabel = new Label("🔔");
        iconLabel.setStyle(
            "-fx-font-size: 24px;" +
            "-fx-cursor: hand;"
        );
        
        // Badge rouge avec compteur
        badge = new Circle(10);
        badge.setFill(Color.web("#F44336"));
        badge.setStroke(Color.WHITE);
        badge.setStrokeWidth(2);
        badge.setVisible(false);
        
        badgeLabel = new Label("0");
        badgeLabel.setStyle(
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;"
        );
        badgeLabel.setVisible(false);
        
        // Positionner le badge en haut à droite
        StackPane.setAlignment(iconLabel, Pos.CENTER);
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setAlignment(badgeLabel, Pos.TOP_RIGHT);
        
        getChildren().addAll(iconLabel, badge, badgeLabel);
        
        // Style du conteneur
        setStyle(
            "-fx-padding: 8;" +
            "-fx-background-color: rgba(255, 255, 255, 0.2);" +
            "-fx-background-radius: 25;" +
            "-fx-cursor: hand;"
        );
        
        // Effet hover
        setOnMouseEntered(e -> {
            setStyle(
                "-fx-padding: 8;" +
                "-fx-background-color: rgba(255, 255, 255, 0.3);" +
                "-fx-background-radius: 25;" +
                "-fx-cursor: hand;"
            );
            animateIcon();
        });
        
        setOnMouseExited(e -> {
            setStyle(
                "-fx-padding: 8;" +
                "-fx-background-color: rgba(255, 255, 255, 0.2);" +
                "-fx-background-radius: 25;" +
                "-fx-cursor: hand;"
            );
        });
        
        // Clic pour ouvrir le centre de notifications
        setOnMouseClicked(e -> openNotificationCenter());
    }
    
    /**
     * Met à jour le compteur de notifications non lues
     */
    public void updateUnreadCount() {
        String query = "SELECT COUNT(*) as count " +
                "FROM posts p " +
                "INNER JOIN user_forum uf ON p.forum_id = uf.forum_id " +
                "WHERE uf.user_id = ? " +
                "AND p.author_id != ? " +
                "AND p.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, currentUserId);
            stmt.setInt(2, currentUserId);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int newCount = rs.getInt("count");
                
                if (newCount != unreadCount && newCount > 0) {
                    // Nouvelle notification - animer
                    animateBadge();
                }
                
                unreadCount = newCount;
                updateBadgeDisplay();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Met à jour l'affichage du badge
     */
    private void updateBadgeDisplay() {
        if (unreadCount > 0) {
            badge.setVisible(true);
            badgeLabel.setVisible(true);
            
            // Limiter l'affichage à 99+
            if (unreadCount > 99) {
                badgeLabel.setText("99+");
                badge.setRadius(12);
            } else {
                badgeLabel.setText(String.valueOf(unreadCount));
                badge.setRadius(10);
            }
        } else {
            badge.setVisible(false);
            badgeLabel.setVisible(false);
        }
    }
    
    /**
     * Animation de l'icône au survol
     */
    private void animateIcon() {
        RotateTransition rotate = new RotateTransition(Duration.millis(200), iconLabel);
        rotate.setByAngle(15);
        rotate.setCycleCount(2);
        rotate.setAutoReverse(true);
        rotate.play();
    }
    
    /**
     * Animation du badge pour nouvelle notification
     */
    private void animateBadge() {
        // Scale animation
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), badge);
        scale.setFromX(0.5);
        scale.setFromY(0.5);
        scale.setToX(1.2);
        scale.setToY(1.2);
        scale.setCycleCount(2);
        scale.setAutoReverse(true);
        
        ScaleTransition scaleLabel = new ScaleTransition(Duration.millis(300), badgeLabel);
        scaleLabel.setFromX(0.5);
        scaleLabel.setFromY(0.5);
        scaleLabel.setToX(1.2);
        scaleLabel.setToY(1.2);
        scaleLabel.setCycleCount(2);
        scaleLabel.setAutoReverse(true);
        
        ParallelTransition parallel = new ParallelTransition(scale, scaleLabel);
        parallel.play();
        
        // Shake animation de l'icône
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), iconLabel);
        shake.setFromX(0);
        shake.setByX(5);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }
    
    /**
     * Ouvre le centre de notifications
     */
    private void openNotificationCenter() {
        NotificationCenterController notificationCenter = new NotificationCenterController(
            overlayManager,
            currentUserId
        );
        
        overlayManager.openOverlay(
            notificationCenter.getView(),
            OverlayManager.OverlayType.LARGE
        );
        
        // Réinitialiser le compteur après ouverture
        // (optionnel - vous pouvez garder le compteur)
        // unreadCount = 0;
        // updateBadgeDisplay();
    }
    
    /**
     * Démarre la mise à jour périodique du compteur
     */
    private void startPeriodicUpdate() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(10), e -> {
            updateUnreadCount();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/fintechforum";
        String user = "root";
        String password = "";
        return DriverManager.getConnection(url, user, password);
    }
}
