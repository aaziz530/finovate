package org.example.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object pour les opérations CRUD sur les Forums
 */
public class ForumDAO {

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public ForumDAO() {
        this("jdbc:mysql://localhost:3306/fintechforum", "root", "");
    }

    public ForumDAO(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    /**
     * Crée un nouveau forum
     * @return ID du forum créé, ou -1 en cas d'erreur
     */
    public int createForum(String name, String description, int creatorId) throws SQLException {
        String query = "INSERT INTO forums (name, description, creator_id, created_at) VALUES (?, ?, ?, NOW())";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setInt(3, creatorId);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating forum failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating forum failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Récupère un forum par son ID
     */
    public Forum getForumById(int forumId) throws SQLException {
        String query = "SELECT id, name, description, creator_id, created_at FROM forums WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, forumId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new Forum(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getInt("creator_id"),
                    rs.getTimestamp("created_at")
                );
            }
            return null;
        }
    }

    /**
     * Récupère tous les forums
     */
    public List<Forum> getAllForums() throws SQLException {
        List<Forum> forums = new ArrayList<>();
        String query = "SELECT id, name, description, creator_id, created_at FROM forums ORDER BY created_at DESC";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                forums.add(new Forum(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getInt("creator_id"),
                    rs.getTimestamp("created_at")
                ));
            }
        }
        return forums;
    }

    /**
     * Récupère les forums créés par un utilisateur
     */
    public List<Forum> getForumsByCreator(int creatorId) throws SQLException {
        List<Forum> forums = new ArrayList<>();
        String query = "SELECT id, name, description, creator_id, created_at FROM forums WHERE creator_id = ? ORDER BY created_at DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, creatorId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                forums.add(new Forum(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getInt("creator_id"),
                    rs.getTimestamp("created_at")
                ));
            }
        }
        return forums;
    }

    /**
     * Met à jour un forum
     */
    public boolean updateForum(int forumId, String name, String description) throws SQLException {
        String query = "UPDATE forums SET name = ?, description = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setInt(3, forumId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Supprime un forum
     */
    public boolean deleteForum(int forumId, int creatorId) throws SQLException {
        String query = "DELETE FROM forums WHERE id = ? AND creator_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, forumId);
            stmt.setInt(2, creatorId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Compte le nombre de membres d'un forum
     */
    public int getMemberCount(int forumId) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM user_forum WHERE forum_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, forumId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
            return 0;
        }
    }

    /**
     * Vérifie si un forum existe
     */
    public boolean forumExists(int forumId) throws SQLException {
        String query = "SELECT 1 FROM forums WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, forumId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    /**
     * Classe interne représentant un Forum
     */
    public static class Forum {
        private int id;
        private String name;
        private String description;
        private int creatorId;
        private Timestamp createdAt;

        public Forum(int id, String name, String description, int creatorId, Timestamp createdAt) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.creatorId = creatorId;
            this.createdAt = createdAt;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getCreatorId() { return creatorId; }
        public Timestamp getCreatedAt() { return createdAt; }
    }
}
