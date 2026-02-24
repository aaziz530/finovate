package org.example.services;

import org.example.entities.Forum;
import org.example.entities.User;
import org.example.utils.Databaseconnection;
import org.example.utils.ValidationUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ForumService {

    // CREATE
    public boolean createForum(Forum forum, boolean isUserBlocked) throws SQLException {
        if (isUserBlocked) throw new SecurityException("Utilisateur bloqué");

        if (!ValidationUtils.isValidForumName(forum.getTitle()))
            throw new IllegalArgumentException("Titre invalide (3-100 caractères)");

        if (!ValidationUtils.isValidDescription(forum.getDescription()))
            throw new IllegalArgumentException("Description invalide (10-1000 caractères)");

        String query = "INSERT INTO forums (title, description, creator_id, created_at) VALUES (?, ?, ?, ?)";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, ValidationUtils.sanitize(forum.getTitle()));
            stmt.setString(2, ValidationUtils.sanitize(forum.getDescription()));
            stmt.setLong(3, forum.getIdCreator());
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) forum.setId(keys.getLong(1));
                return true;
            }
            return false;
        }
    }

    // READ - all forums
    public List<Forum> getAllForums() throws SQLException {
        List<Forum> forums = new ArrayList<>();
        String query = "SELECT * FROM forums ORDER BY created_at DESC";

        try (Connection conn = Databaseconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                forums.add(mapResultSetToForum(rs));
            }
        }
        return forums;
    }

    // READ - by ID
    public Forum getForumById(Long id) throws SQLException {
        String query = "SELECT * FROM forums WHERE id = ?";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapResultSetToForum(rs);
            return null;
        }
    }

    // UPDATE
    public boolean updateForum(Forum forum, Long userId, String role, boolean isBlocked) throws SQLException {
        if (isBlocked) throw new SecurityException("Utilisateur bloqué");

        Forum existing = getForumById(forum.getId());
        if (existing == null) return false;

        if ("USER".equals(role) && !existing.getIdCreator().equals(userId))
            throw new SecurityException("Vous ne pouvez modifier que vos forums");

        String query = "UPDATE forums SET title = ?, description = ? WHERE id = ?";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, ValidationUtils.sanitize(forum.getTitle()));
            stmt.setString(2, ValidationUtils.sanitize(forum.getDescription()));
            stmt.setLong(3, forum.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    // DELETE
    public boolean deleteForum(Long forumId, Long userId, String role) throws SQLException {
        Forum forum = getForumById(forumId);
        if (forum == null) return false;

        if ("USER".equals(role) && !forum.getIdCreator().equals(userId))
            throw new SecurityException("Vous ne pouvez supprimer que vos forums");

        String query = "DELETE FROM forums WHERE id = ?";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, forumId);
            return stmt.executeUpdate() > 0;
        }
    }

    // MAP ResultSet → Forum
    private Forum mapResultSetToForum(ResultSet rs) throws SQLException {
        Forum f = new Forum(rs.getLong("creator_id"), rs.getString("title"), rs.getString("description"));
        f.setId(rs.getLong("id"));
        f.setCreatedAt(rs.getTimestamp("created_at"));
        return f;
    }
}
