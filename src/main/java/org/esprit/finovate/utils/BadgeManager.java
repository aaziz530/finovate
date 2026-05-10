package org.esprit.finovate.utils;

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

    private static final String DB_URL = "jdbc:mysql://localhost:3306/finovate";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    /**
     * Vérifie et attribue les badges après un vote
     */
    public static void checkVoteBadges(long userId, long forumId) {
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
    private static int countUserVotesInForum(long userId, long forumId) throws SQLException {
        String query = "SELECT COUNT(DISTINCT v.post_id) as vote_count " +
                "FROM votes v " +
                "INNER JOIN posts p ON v.post_id = p.id " +
                "WHERE v.user_id = ? AND p.forum_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setLong(1, userId);
            stmt.setLong(2, forumId);
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
    private static int countUserTotalVotes(long userId) throws SQLException {
        String query = "SELECT COUNT(DISTINCT post_id) as vote_count FROM votes WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setLong(1, userId);
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
    private static void awardBadge(long userId, String badgeName, Long forumId) throws SQLException {
        System.out.println("=== awardBadge DEBUT ===");
        System.out.println("userId: " + userId + ", badgeName: " + badgeName + ", forumId: " + forumId);
        
        // Récupérer l'ID du type de badge
        int badgeTypeId = getBadgeTypeId(badgeName);
        System.out.println("badgeTypeId récupéré: " + badgeTypeId);
        
        if (badgeTypeId == -1) {
            System.out.println("ERREUR: Badge type '" + badgeName + "' non trouvé dans badge_types!");
            System.out.println("Vérifiez que le badge existe avec: SELECT * FROM badge_types WHERE name = '" + badgeName + "'");
            return;
        }
        
        // Vérifier si l'utilisateur a déjà ce badge
        boolean hasBadge = userHasBadge(userId, badgeTypeId, forumId);
        System.out.println("L'utilisateur a déjà ce badge? " + hasBadge);
        
        if (hasBadge) {
            System.out.println("Badge déjà attribué, on skip");
            return;
        }
        
        // Attribuer le badge
        String insertQuery = "INSERT INTO user_badges (user_id, badge_type_id, forum_id) VALUES (?, ?, ?)";
        System.out.println("Tentative d'insertion dans user_badges...");
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, userId);
            stmt.setInt(2, badgeTypeId);
            if (forumId != null) {
                stmt.setLong(3, forumId);
            } else {
                stmt.setNull(3, Types.BIGINT);
            }
            
            System.out.println("Exécution de la requête INSERT...");
            int rows = stmt.executeUpdate();
            System.out.println("✓ Badge attribué avec succès! Lignes insérées: " + rows);
            
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                System.out.println("ID du badge attribué: " + generatedKeys.getInt(1));
            }
            
            // Afficher la notification de badge gagné
            showBadgeNotification(userId, badgeName, forumId);
            
        } catch (SQLException e) {
            System.out.println("ERREUR SQL lors de l'insertion du badge:");
            System.out.println("Message: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("ErrorCode: " + e.getErrorCode());
            e.printStackTrace();
            throw e;
        }
        
        System.out.println("=== awardBadge FIN ===");
    }

    /**
     * Récupère l'ID d'un type de badge par son nom
     */
    private static int getBadgeTypeId(String badgeName) throws SQLException {
        String query = "SELECT id FROM badge_types WHERE name = ?";
        
        System.out.println("  → getBadgeTypeId pour: '" + badgeName + "'");
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, badgeName);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int id = rs.getInt("id");
                System.out.println("  → Badge trouvé avec ID: " + id);
                return id;
            } else {
                System.out.println("  → AUCUN badge trouvé avec ce nom!");
                System.out.println("  → Requête SQL: " + query);
                System.out.println("  → Paramètre: '" + badgeName + "'");
            }
        } catch (SQLException e) {
            System.out.println("  → ERREUR SQL dans getBadgeTypeId: " + e.getMessage());
            throw e;
        }
        return -1;
    }

    /**
     * Vérifie si un utilisateur possède déjà un badge
     */
    private static boolean userHasBadge(long userId, int badgeTypeId, Long forumId) throws SQLException {
        String query;
        if (forumId != null) {
            query = "SELECT 1 FROM user_badges WHERE user_id = ? AND badge_type_id = ? AND forum_id = ?";
        } else {
            query = "SELECT 1 FROM user_badges WHERE user_id = ? AND badge_type_id = ? AND forum_id IS NULL";
        }
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setLong(1, userId);
            stmt.setInt(2, badgeTypeId);
            if (forumId != null) {
                stmt.setLong(3, forumId);
            }
            
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    /**
     * Affiche une notification de badge gagné
     */
    private static void showBadgeNotification(long userId, String badgeName, Long forumId) {
        try {
            // Récupérer les détails du badge
            String query = "SELECT bt.icon, bt.description, f.title as forum_name " +
                    "FROM badge_types bt " +
                    "LEFT JOIN forums f ON f.id = ? " +
                    "WHERE bt.name = ?";
            
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                if (forumId != null) {
                    stmt.setLong(1, forumId);
                } else {
                    stmt.setNull(1, Types.BIGINT);
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
                "f.title as forum_name, ub.earned_at " +
                "FROM user_badges ub " +
                "INNER JOIN badge_types bt ON ub.badge_type_id = bt.id " +
                "LEFT JOIN forums f ON ub.forum_id = f.id " +
                "WHERE ub.user_id = ? " +
                "ORDER BY ub.earned_at DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setLong(1, userId);
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
            
            stmt.setLong(1, userId);
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
    public static void checkPostBadges(long userId) {
        try {
            int postCount = countUserPosts(userId);
            
            System.out.println("=== checkPostBadges ===");
            System.out.println("userId: " + userId);
            System.out.println("postCount: " + postCount);
            
            // Bronze: 3 posts
            if (postCount >= 3) {
                System.out.println("Attribution Bronze Badge");
                awardBadge(userId, "Bronze Badge", null);
            }
            // Silver: 5 posts
            if (postCount >= 5) {
                System.out.println("Attribution Silver Badge");
                awardBadge(userId, "Silver Badge", null);
            }
            // Gold: 10 posts
            if (postCount >= 10) {
                System.out.println("Attribution Gold Badge");
                awardBadge(userId, "Gold Badge", null);
            }
            // Diamond: 15 posts
            if (postCount >= 15) {
                System.out.println("Attribution Diamond Badge");
                awardBadge(userId, "Diamond Badge", null);
            }
        } catch (SQLException e) {
            System.out.println("ERREUR dans checkPostBadges:");
            e.printStackTrace();
        }
    }

    /**
     * Vérifie les badges après création de commentaire
     */
    public static void checkCommentBadges(long userId) {
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
    public static void checkShareBadges(long userId) {
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

    private static int countUserPosts(long userId) throws SQLException {
        String query = "SELECT COUNT(*) as post_count FROM posts WHERE author_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("post_count") : 0;
        }
    }

    private static int countUserComments(long userId) throws SQLException {
        String query = "SELECT COUNT(*) as comment_count FROM comments WHERE author_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("comment_count") : 0;
        }
    }

    private static int countUserShares(long userId) throws SQLException {
        String query = "SELECT COUNT(*) as share_count FROM shared_posts WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, userId);
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
