package org.esprit.finovate.services;

import org.esprit.finovate.entities.Comment;
import org.esprit.finovate.entities.User;
import org.esprit.finovate.utils.Databaseconnection;
import org.esprit.finovate.utils.ValidationUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentService {

    /**
     * CREATE - Créer un commentaire
     * Contrôles:
     * - Utilisateur ne doit pas être bloqué
     * - Validation du contenu
     */
    public boolean createComment(Comment comment, boolean isUserBlocked) throws SQLException {
        if (isUserBlocked) {
            throw new SecurityException("Utilisateur bloqué: impossible de créer un commentaire");
        }

        // Validation
        if (!ValidationUtils.isValidCommentContent(comment.getContent())) {
            throw new IllegalArgumentException("Contenu invalide (1-1000 caractères)");
        }

        String query = "INSERT INTO comments (post_id, author_id, content) VALUES (?, ?, ?)";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, comment.getPostId());
            stmt.setInt(2, comment.getAuthorId());
            stmt.setString(3, ValidationUtils.sanitize(comment.getContent()));

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    comment.setId(generatedKeys.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    /**
     * READ - Récupérer un commentaire par ID
     */
    public Comment getCommentById(int id) throws SQLException {
        String query = "SELECT * FROM comments WHERE id = ?";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToComment(rs);
            }
            return null;
        }
    }

    /**
     * READ - Récupérer tous les commentaires d'un post
     */
    public List<Comment> getCommentsByPost(int postId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT * FROM comments WHERE post_id = ? ORDER BY created_at ASC";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                comments.add(mapResultSetToComment(rs));
            }
        }
        return comments;
    }

    /**
     * READ - Récupérer les commentaires d'un auteur
     */
    public List<Comment> getCommentsByAuthor(int authorId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT * FROM comments WHERE author_id = ? ORDER BY created_at DESC";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, authorId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                comments.add(mapResultSetToComment(rs));
            }
        }
        return comments;
    }

    /**
     * UPDATE - Modifier un commentaire
     * Contrôles:
     * - Utilisateur peut modifier seulement ses propres commentaires
     * - Utilisateur ne doit pas être bloqué
     */
    public boolean updateComment(Comment comment, int requestingUserId, boolean isUserBlocked) throws SQLException {
        if (isUserBlocked) {
            throw new SecurityException("Utilisateur bloqué: impossible de modifier un commentaire");
        }

        // Vérifier les droits
        Comment existingComment = getCommentById(comment.getId());
        if (existingComment == null || existingComment.getAuthorId() != requestingUserId) {
            throw new SecurityException("Vous ne pouvez modifier que vos propres commentaires");
        }

        // Validation
        if (!ValidationUtils.isValidCommentContent(comment.getContent())) {
            throw new IllegalArgumentException("Contenu invalide");
        }

        String query = "UPDATE comments SET content = ? WHERE id = ?";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, ValidationUtils.sanitize(comment.getContent()));
            stmt.setInt(2, comment.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * DELETE - Supprimer un commentaire
     * Contrôles:
     * - Utilisateur peut supprimer seulement ses propres commentaires
     */
    public boolean deleteComment(int commentId, int requestingUserId) throws SQLException {
        // Vérifier les droits
        Comment comment = getCommentById(commentId);
        if (comment == null || comment.getAuthorId() != requestingUserId) {
            throw new SecurityException("Vous ne pouvez supprimer que vos propres commentaires");
        }

        String query = "DELETE FROM comments WHERE id = ?";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, commentId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Mapper ResultSet vers Comment
     */
    private Comment mapResultSetToComment(ResultSet rs) throws SQLException {
        return new Comment(
                rs.getInt("id"),
                rs.getInt("post_id"),
                rs.getInt("author_id"),
                rs.getString("content"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at")
        );
    }
}