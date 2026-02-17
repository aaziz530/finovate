package org.example.ai;

import java.sql.*;
import java.util.*;

/**
 * Moteur de recommandation intelligent basé sur le comportement utilisateur
 * Analyse les interactions pour suggérer des forums pertinents
 */
public class RecommendationEngine {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/fintechforum";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    /**
     * Enregistre une interaction utilisateur
     */
    public static void trackInteraction(int userId, int forumId, InteractionType type) {
        String query = "INSERT INTO user_interactions (user_id, forum_id, interaction_type, interaction_count) " +
                "VALUES (?, ?, ?, 1) " +
                "ON DUPLICATE KEY UPDATE " +
                "interaction_count = interaction_count + 1, " +
                "last_interaction = CURRENT_TIMESTAMP";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, forumId);
            stmt.setString(3, type.name().toLowerCase());
            stmt.executeUpdate();
            
            // Recalculer les recommandations après chaque interaction
            calculateRecommendations(userId);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Calcule les recommandations pour un utilisateur
     * Algorithme basé sur:
     * 1. Fréquence d'interaction
     * 2. Type d'interaction (poids différents)
     * 3. Similarité avec d'autres utilisateurs
     * 4. Popularité du forum
     * 5. Similarité textuelle des descriptions (NOUVEAU)
     * 6. Votes (upvotes/downvotes) (NOUVEAU)
     */
    public static void calculateRecommendations(int userId) {
        try (Connection conn = getConnection()) {
            
            // Nettoyer les anciennes recommandations
            String deleteQuery = "DELETE FROM forum_recommendations WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                stmt.setInt(1, userId);
                stmt.executeUpdate();
            }
            
            // Calculer les scores pour chaque forum
            Map<Integer, RecommendationScore> scores = new HashMap<>();
            
            // 1. Score basé sur les interactions directes
            calculateDirectInteractionScore(conn, userId, scores);
            
            // 2. Score basé sur la similarité avec d'autres utilisateurs
            calculateCollaborativeFilteringScore(conn, userId, scores);
            
            // 3. Score basé sur la popularité et l'activité récente
            calculatePopularityScore(conn, userId, scores);
            
            // 4. Score basé sur la similarité textuelle des descriptions (NOUVEAU)
            calculateTextSimilarityScore(conn, userId, scores);
            
            // 5. Score basé sur les votes (NOUVEAU)
            calculateVoteScore(conn, userId, scores);
            
            // 6. Exclure les forums déjà rejoints
            excludeJoinedForums(conn, userId, scores);
            
            // Sauvegarder les recommandations
            saveRecommendations(conn, userId, scores);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Score basé sur les interactions directes de l'utilisateur
     */
    private static void calculateDirectInteractionScore(Connection conn, int userId, 
                                                        Map<Integer, RecommendationScore> scores) throws SQLException {
        String query = "SELECT forum_id, interaction_type, interaction_count " +
                "FROM user_interactions " +
                "WHERE user_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int forumId = rs.getInt("forum_id");
                String type = rs.getString("interaction_type");
                int count = rs.getInt("interaction_count");
                
                // Poids différents selon le type d'interaction
                double weight = getInteractionWeight(type);
                double score = count * weight;
                
                scores.putIfAbsent(forumId, new RecommendationScore(forumId));
                scores.get(forumId).addScore(score, "Vos interactions: " + count + " " + type);
            }
        }
    }
    
    /**
     * Score basé sur le filtrage collaboratif
     * "Les utilisateurs qui aiment X aiment aussi Y"
     */
    private static void calculateCollaborativeFilteringScore(Connection conn, int userId,
                                                             Map<Integer, RecommendationScore> scores) throws SQLException {
        // Trouver les utilisateurs similaires
        String query = "SELECT ui2.forum_id, COUNT(*) as similarity_score " +
                "FROM user_interactions ui1 " +
                "INNER JOIN user_interactions ui2 ON ui1.forum_id = ui2.forum_id " +
                "WHERE ui1.user_id = ? " +
                "AND ui2.user_id != ? " +
                "AND ui2.forum_id NOT IN (SELECT forum_id FROM user_interactions WHERE user_id = ?) " +
                "GROUP BY ui2.forum_id " +
                "HAVING similarity_score >= 2 " +
                "ORDER BY similarity_score DESC " +
                "LIMIT 10";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int forumId = rs.getInt("forum_id");
                int similarityScore = rs.getInt("similarity_score");
                
                scores.putIfAbsent(forumId, new RecommendationScore(forumId));
                scores.get(forumId).addScore(similarityScore * 5, 
                    "Utilisateurs similaires aiment ce forum");
            }
        }
    }
    
    /**
     * Score basé sur la popularité et l'activité récente
     */
    private static void calculatePopularityScore(Connection conn, int userId,
                                                 Map<Integer, RecommendationScore> scores) throws SQLException {
        String query = "SELECT f.id, " +
                "(SELECT COUNT(*) FROM user_forum WHERE forum_id = f.id) as member_count, " +
                "(SELECT COUNT(*) FROM posts WHERE forum_id = f.id AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)) as recent_posts " +
                "FROM forums f " +
                "WHERE f.id NOT IN (SELECT forum_id FROM user_forum WHERE user_id = ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int forumId = rs.getInt("id");
                int memberCount = rs.getInt("member_count");
                int recentPosts = rs.getInt("recent_posts");
                
                // Score basé sur la popularité et l'activité
                double popularityScore = Math.log(memberCount + 1) * 2;
                double activityScore = recentPosts * 3;
                
                scores.putIfAbsent(forumId, new RecommendationScore(forumId));
                scores.get(forumId).addScore(popularityScore, "Forum populaire");
                scores.get(forumId).addScore(activityScore, "Forum actif");
            }
        }
    }
    
    /**
     * Score basé sur la similarité textuelle des descriptions (NOUVEAU)
     * Analyse les mots-clés communs entre les forums que l'utilisateur aime
     * et les autres forums disponibles
     */
    private static void calculateTextSimilarityScore(Connection conn, int userId,
                                                     Map<Integer, RecommendationScore> scores) throws SQLException {
        // Récupérer les descriptions des forums avec lesquels l'utilisateur a interagi
        String userForumsQuery = "SELECT DISTINCT f.id, f.name, f.description " +
                "FROM forums f " +
                "INNER JOIN user_interactions ui ON f.id = ui.forum_id " +
                "WHERE ui.user_id = ? " +
                "ORDER BY ui.interaction_count DESC LIMIT 5";
        
        List<ForumText> userForums = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(userForumsQuery)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                userForums.add(new ForumText(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description")
                ));
            }
        }
        
        if (userForums.isEmpty()) return;
        
        // Récupérer tous les autres forums
        String otherForumsQuery = "SELECT f.id, f.name, f.description " +
                "FROM forums f " +
                "WHERE f.id NOT IN (SELECT forum_id FROM user_forum WHERE user_id = ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(otherForumsQuery)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int forumId = rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                
                // Calculer la similarité avec chaque forum de l'utilisateur
                double maxSimilarity = 0;
                String similarTo = "";
                
                for (ForumText userForum : userForums) {
                    double similarity = calculateTextSimilarity(
                        name + " " + description,
                        userForum.name + " " + userForum.description
                    );
                    
                    if (similarity > maxSimilarity) {
                        maxSimilarity = similarity;
                        similarTo = userForum.name;
                    }
                }
                
                // Si similarité significative, ajouter au score
                if (maxSimilarity > 0.1) {
                    double similarityScore = maxSimilarity * 50; // Poids important
                    scores.putIfAbsent(forumId, new RecommendationScore(forumId));
                    scores.get(forumId).addScore(similarityScore, 
                        "Similaire à: " + similarTo);
                }
            }
        }
    }
    
    /**
     * Score basé sur les votes (upvotes/downvotes) (NOUVEAU)
     * Cette méthode est optionnelle et ne cause pas d'erreur si la table votes n'existe pas
     */
    private static void calculateVoteScore(Connection conn, int userId,
                                          Map<Integer, RecommendationScore> scores) throws SQLException {
        try {
            // Vérifier si la table votes existe et a la colonne forum_id
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "votes", null);
            
            if (!tables.next()) {
                // Table votes n'existe pas, on ignore cette fonctionnalité
                return;
            }
            
            // Vérifier si la colonne forum_id existe
            ResultSet columns = metaData.getColumns(null, null, "votes", "forum_id");
            if (!columns.next()) {
                // Colonne forum_id n'existe pas, on ignore cette fonctionnalité
                return;
            }
            
            // Récupérer les forums que l'utilisateur a upvoté
            String upvotedQuery = "SELECT forum_id FROM votes WHERE user_id = ? AND vote_type = 'upvote'";
            List<Integer> upvotedForums = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(upvotedQuery)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    upvotedForums.add(rs.getInt("forum_id"));
                }
            }
            
            if (upvotedForums.isEmpty()) return;
            
            // Trouver les forums similaires aux forums upvotés
            String similarForumsQuery = "SELECT f.id, f.name, f.description " +
                    "FROM forums f " +
                    "WHERE f.id NOT IN (SELECT forum_id FROM user_forum WHERE user_id = ?) " +
                    "AND f.id NOT IN (" + String.join(",", upvotedForums.stream().map(String::valueOf).toArray(String[]::new)) + ")";
            
            try (PreparedStatement stmt = conn.prepareStatement(similarForumsQuery)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    int forumId = rs.getInt("id");
                    
                    scores.putIfAbsent(forumId, new RecommendationScore(forumId));
                    scores.get(forumId).addScore(5, "Basé sur vos votes positifs");
                }
            }
        } catch (SQLException e) {
            // Si erreur SQL (table ou colonne n'existe pas), on ignore silencieusement
            // Le système fonctionne sans cette fonctionnalité
        }
    }
    
    /**
     * Calcule la similarité textuelle entre deux textes
     * Utilise l'algorithme de Jaccard (mots communs / mots totaux)
     */
    private static double calculateTextSimilarity(String text1, String text2) {
        // Normaliser et tokeniser
        Set<String> words1 = tokenize(text1.toLowerCase());
        Set<String> words2 = tokenize(text2.toLowerCase());
        
        if (words1.isEmpty() || words2.isEmpty()) return 0;
        
        // Calculer l'intersection et l'union
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        // Coefficient de Jaccard
        return (double) intersection.size() / union.size();
    }
    
    /**
     * Tokenise un texte en mots significatifs
     */
    private static Set<String> tokenize(String text) {
        // Mots vides à ignorer (stop words)
        Set<String> stopWords = new HashSet<>(Arrays.asList(
            "le", "la", "les", "un", "une", "des", "de", "du", "et", "ou", "pour",
            "dans", "sur", "avec", "par", "en", "à", "au", "aux", "ce", "cette",
            "ces", "son", "sa", "ses", "mon", "ma", "mes", "ton", "ta", "tes",
            "the", "a", "an", "and", "or", "for", "in", "on", "with", "by", "at"
        ));
        
        Set<String> words = new HashSet<>();
        String[] tokens = text.split("[\\s\\p{Punct}]+");
        
        for (String token : tokens) {
            if (token.length() > 2 && !stopWords.contains(token)) {
                words.add(token);
            }
        }
        
        return words;
    }
    
    /**
     * Exclure les forums déjà rejoints (optionnel)
     */
    private static void excludeJoinedForums(Connection conn, int userId,
                                           Map<Integer, RecommendationScore> scores) throws SQLException {
        String query = "SELECT forum_id FROM user_forum WHERE user_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int forumId = rs.getInt("forum_id");
                scores.remove(forumId); // Retirer les forums déjà rejoints
            }
        }
    }
    
    /**
     * Sauvegarder les recommandations dans la base de données
     */
    private static void saveRecommendations(Connection conn, int userId,
                                           Map<Integer, RecommendationScore> scores) throws SQLException {
        String query = "INSERT INTO forum_recommendations (user_id, forum_id, score, reason) " +
                "VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            // Trier par score décroissant
            List<RecommendationScore> sortedScores = new ArrayList<>(scores.values());
            sortedScores.sort((a, b) -> Double.compare(b.totalScore, a.totalScore));
            
            // Sauvegarder les top 20
            int count = 0;
            for (RecommendationScore score : sortedScores) {
                if (count >= 20) break;
                if (score.totalScore < 1) continue; // Score minimum
                
                stmt.setInt(1, userId);
                stmt.setInt(2, score.forumId);
                stmt.setDouble(3, score.totalScore);
                stmt.setString(4, score.getReason());
                stmt.addBatch();
                
                count++;
            }
            
            stmt.executeBatch();
        }
    }
    
    /**
     * Récupère les forums recommandés pour un utilisateur
     */
    public static List<RecommendedForum> getRecommendations(int userId, int limit) {
        List<RecommendedForum> recommendations = new ArrayList<>();
        
        String query = "SELECT fr.forum_id, fr.score, fr.reason, f.name, f.description, " +
                "(SELECT COUNT(*) FROM user_forum WHERE forum_id = f.id) as member_count, " +
                "(SELECT COUNT(*) FROM posts WHERE forum_id = f.id AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)) as recent_posts " +
                "FROM forum_recommendations fr " +
                "INNER JOIN forums f ON fr.forum_id = f.id " +
                "WHERE fr.user_id = ? " +
                "ORDER BY fr.score DESC " +
                "LIMIT ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                recommendations.add(new RecommendedForum(
                    rs.getInt("forum_id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("score"),
                    rs.getString("reason"),
                    rs.getInt("member_count"),
                    rs.getInt("recent_posts")
                ));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return recommendations;
    }
    
    /**
     * Poids des différents types d'interaction
     */
    private static double getInteractionWeight(String type) {
        switch (type.toLowerCase()) {
            case "post": return 10.0;      // Créer un post = très engagé
            case "comment": return 7.0;    // Commenter = engagé
            case "like": return 3.0;       // Liker = intéressé
            case "share": return 5.0;      // Partager = très intéressé
            case "click": return 2.0;      // Cliquer = curieux
            case "view": return 1.0;       // Voir = passif
            default: return 1.0;
        }
    }
    
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    /**
     * Types d'interaction
     */
    public enum InteractionType {
        VIEW, CLICK, POST, COMMENT, LIKE, SHARE
    }
    
    /**
     * Classe pour stocker le score de recommandation
     */
    private static class RecommendationScore {
        int forumId;
        double totalScore = 0;
        List<String> reasons = new ArrayList<>();
        
        RecommendationScore(int forumId) {
            this.forumId = forumId;
        }
        
        void addScore(double score, String reason) {
            this.totalScore += score;
            if (!reasons.contains(reason)) {
                this.reasons.add(reason);
            }
        }
        
        String getReason() {
            return String.join(", ", reasons);
        }
    }
    
    /**
     * Classe pour représenter un forum recommandé
     */
    public static class RecommendedForum {
        public int id;
        public String name;
        public String description;
        public double score;
        public String reason;
        public int memberCount;
        public int recentPosts;
        
        public RecommendedForum(int id, String name, String description, double score, 
                               String reason, int memberCount, int recentPosts) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.score = score;
            this.reason = reason;
            this.memberCount = memberCount;
            this.recentPosts = recentPosts;
        }
    }
    
    /**
     * Classe pour stocker le texte d'un forum
     */
    private static class ForumText {
        int id;
        String name;
        String description;
        
        ForumText(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description != null ? description : "";
        }
    }
    
    /**
     * Supprime toutes les recommandations pour un utilisateur
     */
    public static void clearAllRecommendations(int userId) {
        String query = "DELETE FROM forum_recommendations WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
