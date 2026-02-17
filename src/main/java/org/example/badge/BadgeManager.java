package org.example.badge;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire du système de badges
 * Gère l'attribution, le suivi et l'affichage des badges
 */
public class BadgeManager {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/fintechforum";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    /**
     * Vérifie et attribue les badges après un vote
     */
    public static void checkVoteBadges(int userId, int forumId) {
        try {
            // Compter les votes de l'utilisateur dans ce forum
            int voteCount = countUserVotesInForum(userId, forumId);
            
            // Vérifier le badge "Fan du Forum" (5 votes)
            if (voteCount == 5) {
                awardBadge(userId, "Fan du Forum", forumId);
            }
            
            // Vérifier le badge "Super Fan" (10 votes)
            if (voteCount == 10) {
                awardBadge(userId, "Super Fan", forumId);
            }
            
            // Vérifier le badge "Mega Fan" (25 votes)
            if (voteCount == 25) {
                awardBadge(userId, "Mega Fan", forumId);
            }
            
            // Vérifier les badges globaux
            int totalVotes = countUserTotalVotes(userId);
            if (totalVotes == 50) {
                awardBadge(userId, "Voteur Actif", null);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Compte les votes d'un utilisateur dans un forum spécifique
     */
    private static int countUserVotesInForum(int userId, int forumId) throws SQLException {
        String query = "SELECT COUNT(DISTINCT v.post_id) as vote_count " +
                "FROM votes v " +
                "INNER JOIN posts p ON v.post_id = p.id " +
                "WHERE v.user_id = ? AND p.forum_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, forumId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("vote_count");
            }
        }
        return 0;
    }

    /**
     * Compte le total des votes d'un utilisateur
     */
    private static int countUserTotalVotes(int userId) throws SQLException {
        String query = "SELECT COUNT(DISTINCT post_id) as vote_count FROM votes WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("vote_count");
            }
        }
        return 0;
    }

    /**
     * Attribue un badge à un utilisateur
     */
    private static void awardBadge(int userId, String badgeName, Integer forumId) throws SQLException {
        // Récupérer l'ID du type de badge
        int badgeTypeId = getBadgeTypeId(badgeName);
        if (badgeTypeId == -1) return;
        
        // Vérifier si l'utilisateur a déjà ce badge
        if (userHasBadge(userId, badgeTypeId, forumId)) {
            return;
        }
        
        // Attribuer le badge
        String insertQuery = "INSERT INTO user_badges (user_id, badge_type_id, forum_id) VALUES (?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, badgeTypeId);
            if (forumId != null) {
                stmt.setInt(3, forumId);
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            
            stmt.executeUpdate();
            
            // Afficher la notification de badge gagné
            showBadgeNotification(userId, badgeName, forumId);
        }
    }

    /**
     * Récupère l'ID d'un type de badge par son nom
     */
    private static int getBadgeTypeId(String badgeName) throws SQLException {
        String query = "SELECT id FROM badge_types WHERE name = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, badgeName);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }

    /**
     * Vérifie si un utilisateur possède déjà un badge
     */
    private static boolean userHasBadge(int userId, int badgeTypeId, Integer forumId) throws SQLException {
        String query;
        if (forumId != null) {
            query = "SELECT 1 FROM user_badges WHERE user_id = ? AND badge_type_id = ? AND forum_id = ?";
        } else {
            query = "SELECT 1 FROM user_badges WHERE user_id = ? AND badge_type_id = ? AND forum_id IS NULL";
        }
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, badgeTypeId);
            if (forumId != null) {
                stmt.setInt(3, forumId);
            }
            
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    /**
     * Affiche une notification de badge gagné
     */
    private static void showBadgeNotification(int userId, String badgeName, Integer forumId) {
        try {
            // Récupérer les détails du badge
            String query = "SELECT bt.icon, bt.description, f.name as forum_name " +
                    "FROM badge_types bt " +
                    "LEFT JOIN forums f ON f.id = ? " +
                    "WHERE bt.name = ?";
            
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                if (forumId != null) {
                    stmt.setInt(1, forumId);
                } else {
                    stmt.setNull(1, Types.INTEGER);
                }
                stmt.setString(2, badgeName);
                
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String icon = rs.getString("icon");
                    String description = rs.getString("description");
                    String forumName = rs.getString("forum_name");
                    
                    // Créer l'alerte personnalisée
                    javafx.application.Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("🎉 Nouveau Badge Gagné !");
                        alert.setHeaderText(null);
                        
                        // Créer le contenu personnalisé
                        VBox content = new VBox(15);
                        content.setAlignment(Pos.CENTER);
                        content.setPadding(new Insets(20));
                        content.setStyle("-fx-background-color: linear-gradient(to bottom, #FFD700, #FFA500); " +
                                "-fx-background-radius: 10;");
                        
                        // Icône du badge (grande taille)
                        Label iconLabel = new Label(icon);
                        iconLabel.setStyle("-fx-font-size: 72px;");
                        
                        // Nom du badge
                        Label nameLabel = new Label(badgeName);
                        nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
                        
                        // Description
                        Label descLabel = new Label(description);
                        descLabel.setWrapText(true);
                        descLabel.setMaxWidth(300);
                        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
                        descLabel.setAlignment(Pos.CENTER);
                        
                        content.getChildren().addAll(iconLabel, nameLabel, descLabel);
                        
                        // Ajouter le nom du forum si applicable
                        if (forumName != null) {
                            Label forumLabel = new Label("Forum: " + forumName);
                            forumLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; " +
                                    "-fx-text-fill: white; -fx-background-color: rgba(0,0,0,0.3); " +
                                    "-fx-padding: 8 15; -fx-background-radius: 15;");
                            content.getChildren().add(forumLabel);
                        }
                        
                        alert.getDialogPane().setContent(content);
                        alert.getDialogPane().setStyle("-fx-background-color: transparent;");
                        
                        // Personnaliser le bouton
                        alert.getButtonTypes().setAll(ButtonType.OK);
                        
                        alert.showAndWait();
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Récupère tous les badges d'un utilisateur
     */
    public static List<Badge> getUserBadges(int userId) {
        List<Badge> badges = new ArrayList<>();
        
        String query = "SELECT bt.name, bt.icon, bt.description, bt.category, " +
                "f.name as forum_name, ub.earned_at " +
                "FROM user_badges ub " +
                "INNER JOIN badge_types bt ON ub.badge_type_id = bt.id " +
                "LEFT JOIN forums f ON ub.forum_id = f.id " +
                "WHERE ub.user_id = ? " +
                "ORDER BY ub.earned_at DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                badges.add(new Badge(
                        rs.getString("name"),
                        rs.getString("icon"),
                        rs.getString("description"),
                        rs.getString("category"),
                        rs.getString("forum_name"),
                        rs.getTimestamp("earned_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return badges;
    }

    /**
     * Compte le nombre de badges d'un utilisateur
     */
    public static int getUserBadgeCount(int userId) {
        String query = "SELECT COUNT(*) as badge_count FROM user_badges WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("badge_count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }

    /**
     * Vérifie les badges après création de post
     */
    public static void checkPostBadges(int userId) {
        try {
            int postCount = countUserPosts(userId);
            
            if (postCount == 1) {
                awardBadge(userId, "Premier Post", null);
            } else if (postCount == 10) {
                awardBadge(userId, "Auteur Régulier", null);
            } else if (postCount == 50) {
                awardBadge(userId, "Auteur Prolifique", null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Vérifie les badges après création de commentaire
     */
    public static void checkCommentBadges(int userId) {
        try {
            int commentCount = countUserComments(userId);
            
            if (commentCount == 10) {
                awardBadge(userId, "Commentateur", null);
            } else if (commentCount == 50) {
                awardBadge(userId, "Conversateur", null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Vérifie les badges après partage de post
     */
    public static void checkShareBadges(int userId) {
        try {
            int shareCount = countUserShares(userId);
            
            if (shareCount == 5) {
                awardBadge(userId, "Partageur", null);
            } else if (shareCount == 20) {
                awardBadge(userId, "Influenceur", null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static int countUserPosts(int userId) throws SQLException {
        String query = "SELECT COUNT(*) as post_count FROM posts WHERE author_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("post_count") : 0;
        }
    }

    private static int countUserComments(int userId) throws SQLException {
        String query = "SELECT COUNT(*) as comment_count FROM comments WHERE author_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("comment_count") : 0;
        }
    }

    private static int countUserShares(int userId) throws SQLException {
        String query = "SELECT COUNT(*) as share_count FROM shared_posts WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("share_count") : 0;
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Classe interne représentant un badge
     */
    public static class Badge {
        private String name;
        private String icon;
        private String description;
        private String category;
        private String forumName;
        private Timestamp earnedAt;

        public Badge(String name, String icon, String description, String category, 
                    String forumName, Timestamp earnedAt) {
            this.name = name;
            this.icon = icon;
            this.description = description;
            this.category = category;
            this.forumName = forumName;
            this.earnedAt = earnedAt;
        }

        public String getName() { return name; }
        public String getIcon() { return icon; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public String getForumName() { return forumName; }
        public Timestamp getEarnedAt() { return earnedAt; }
    }
}
