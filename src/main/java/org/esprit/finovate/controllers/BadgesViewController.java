package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BadgesViewController {

    @FXML private Label totalBadgesLabel;
    @FXML private GridPane badgesGrid;

    private MainController mainController;
    private int currentUserId;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void loadBadges(int userId) {
        this.currentUserId = userId;
        
        System.out.println("=== loadBadges() appelé pour userId: " + userId + " ===");
        
        List<Badge> badges = loadBadgesFromDB();
        
        System.out.println("Total badges trouvés: " + badges.size());
        
        // Mettre à jour juste le total
        totalBadgesLabel.setText(String.valueOf(badges.size()));
        
        // Afficher les badges
        displayBadges(badges);
    }

    private List<Badge> loadBadgesFromDB() {
        List<Badge> badges = new ArrayList<>();
        
        System.out.println("=== loadBadgesFromDB() ===");
        
        String query = "SELECT bt.name, bt.description, bt.icon, bt.category, ub.earned_at, f.title as forum_name " +
                "FROM user_badges ub " +
                "INNER JOIN badge_types bt ON ub.badge_type_id = bt.id " +
                "LEFT JOIN forums f ON ub.forum_id = f.id " +
                "WHERE ub.user_id = ? " +
                "ORDER BY ub.earned_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            System.out.println("Connexion DB OK");
            System.out.println("Requête pour userId: " + currentUserId);
            
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                String name = rs.getString("name");
                System.out.println("Badge #" + count + ": " + name);
                
                badges.add(new Badge(
                        name,
                        rs.getString("description"),
                        rs.getString("icon"),
                        rs.getString("category"),
                        rs.getTimestamp("earned_at"),
                        rs.getString("forum_name")
                ));
            }
            
            System.out.println("Total badges chargés: " + count);

        } catch (SQLException e) {
            System.out.println("ERREUR SQL:");
            e.printStackTrace();
        }

        return badges;
    }

    private void displayBadges(List<Badge> badges) {
        badgesGrid.getChildren().clear();
        
        if (badges.isEmpty()) {
            VBox emptyBox = new VBox(15);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(40));
            emptyBox.setStyle("-fx-background-color: white; -fx-border-radius: 15; -fx-background-radius: 15;");
            
            Label emptyIcon = new Label("🎯");
            emptyIcon.setStyle("-fx-font-size: 64px;");
            
            Label emptyLabel = new Label("Aucun badge gagné pour le moment");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
            
            Label emptyDesc = new Label("Créez des posts pour gagner des badges !");
            emptyDesc.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            
            emptyBox.getChildren().addAll(emptyIcon, emptyLabel, emptyDesc);
            badgesGrid.add(emptyBox, 0, 0);
            return;
        }
        
        int col = 0;
        int row = 0;
        
        for (Badge badge : badges) {
            VBox badgeCard = createBadgeCard(badge);
            badgesGrid.add(badgeCard, col, row);
            
            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createBadgeCard(Badge badge) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(25));
        card.setPrefWidth(280);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: " + getBorderColor(badge.category) + "; " +
            "-fx-border-width: 3; " +
            "-fx-border-radius: 15; " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 3);"
        );

        // Icône du badge avec fond coloré
        String badgeColor = getBadgeColor(badge.category);
        VBox iconContainer = new VBox();
        iconContainer.setAlignment(Pos.CENTER);
        iconContainer.setStyle(
            "-fx-background-color: " + badgeColor + "; " +
            "-fx-background-radius: 60; " +
            "-fx-min-width: 110; -fx-min-height: 110; " +
            "-fx-max-width: 110; -fx-max-height: 110;"
        );
        
        Label iconLabel = new Label(badge.icon);
        iconLabel.setStyle("-fx-font-size: 56px;");
        iconContainer.getChildren().add(iconLabel);

        // Nom du badge
        Label nameLabel = new Label(badge.name);
        nameLabel.setStyle(
            "-fx-font-size: 20px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #1A1A1B; " +
            "-fx-wrap-text: true; " +
            "-fx-text-alignment: center;"
        );
        nameLabel.setMaxWidth(240);
        nameLabel.setWrapText(true);

        // Catégorie badge
        Label categoryLabel = new Label(getCategoryDisplayName(badge.category));
        categoryLabel.setStyle(
            "-fx-font-size: 12px; " +
            "-fx-text-fill: white; " +
            "-fx-background-color: " + getCategoryColor(badge.category) + "; " +
            "-fx-padding: 5 15; " +
            "-fx-background-radius: 12; " +
            "-fx-font-weight: bold;"
        );

        // Description
        Label descLabel = new Label(badge.description);
        descLabel.setStyle(
            "-fx-font-size: 13px; " +
            "-fx-text-fill: #666; " +
            "-fx-wrap-text: true; " +
            "-fx-text-alignment: center; " +
            "-fx-line-spacing: 2;"
        );
        descLabel.setMaxWidth(240);
        descLabel.setWrapText(true);

        card.getChildren().addAll(iconContainer, nameLabel, categoryLabel, descLabel);

        // Forum name if applicable
        if (badge.forumName != null) {
            Label forumLabel = new Label("📁 " + badge.forumName);
            forumLabel.setStyle(
                "-fx-font-size: 12px; " +
                "-fx-text-fill: #0079D3; " +
                "-fx-background-color: #E3F2FD; " +
                "-fx-padding: 6 12; " +
                "-fx-background-radius: 10; " +
                "-fx-font-weight: 600;"
            );
            forumLabel.setMaxWidth(240);
            forumLabel.setWrapText(true);
            card.getChildren().add(forumLabel);
        }

        // Date d'obtention
        Label dateLabel = new Label("🗓️ " + badge.earnedAt.toString().substring(0, 10));
        dateLabel.setStyle(
            "-fx-font-size: 12px; " +
            "-fx-text-fill: #999; " +
            "-fx-font-style: italic;"
        );

        card.getChildren().add(dateLabel);
        return card;
    }

    private String getBadgeColor(String category) {
        if (category == null) return "linear-gradient(to bottom right, #999, #666)";
        
        switch (category.toLowerCase()) {
            case "bronze": return "linear-gradient(to bottom right, #CD7F32, #8B4513)";
            case "silver": return "linear-gradient(to bottom right, #C0C0C0, #808080)";
            case "gold": return "linear-gradient(to bottom right, #FFD700, #FFA500)";
            case "diamond": return "linear-gradient(to bottom right, #B9F2FF, #00CED1)";
            default: return "linear-gradient(to bottom right, #999, #666)";
        }
    }

    private String getBorderColor(String category) {
        if (category == null) return "#999";
        
        switch (category.toLowerCase()) {
            case "bronze": return "#CD7F32";
            case "silver": return "#C0C0C0";
            case "gold": return "#FFD700";
            case "diamond": return "#00CED1";
            default: return "#999";
        }
    }

    private String getCategoryColor(String category) {
        if (category == null) return "#999";
        
        switch (category.toLowerCase()) {
            case "bronze": return "#CD7F32";
            case "silver": return "#C0C0C0";
            case "gold": return "#FFD700";
            case "diamond": return "#00CED1";
            default: return "#999";
        }
    }

    private String getCategoryDisplayName(String category) {
        if (category == null) return "BADGE";
        
        switch (category.toLowerCase()) {
            case "bronze": return "BRONZE";
            case "silver": return "SILVER";
            case "gold": return "GOLD";
            case "diamond": return "DIAMOND";
            default: return category.toUpperCase();
        }
    }

    private Connection getConnection() throws SQLException {
        return org.esprit.finovate.utils.DatabaseConfig.getConnection();
    }

    // Classe interne pour représenter un badge
    private static class Badge {
        String name;
        String description;
        String icon;
        String category;
        Timestamp earnedAt;
        String forumName;

        Badge(String name, String description, String icon, String category, Timestamp earnedAt, String forumName) {
            this.name = name;
            this.description = description;
            this.icon = icon;
            this.category = category;
            this.earnedAt = earnedAt;
            this.forumName = forumName;
        }
    }
}
