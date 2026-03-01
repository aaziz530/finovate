package org.esprit.finovate.services;

import org.esprit.finovate.entities.Post;
import org.esprit.finovate.entities.User;
import org.esprit.finovate.utils.Databaseconnection;
import org.esprit.finovate.utils.ValidationUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostService {

    /**
     * CREATE - Créer un post
     * Contrôles:
     * - Utilisateur ne doit pas être bloqué
     * - Utilisateur doit être membre du forum (créateur ou a rejoint)
     * - Validation titre et contenu
     */
    public boolean createPost(Post post, boolean isUserBlocked) throws SQLException {
        if (isUserBlocked) {
            throw new SecurityException("Utilisateur bloqué: impossible de créer un post");
        }

        // Validation
        if (!ValidationUtils.isValidPostTitle(post.getTitle())) {
            throw new IllegalArgumentException("Titre invalide (5-200 caractères)");
        }
        if (!ValidationUtils.isValidPostContent(post.getContent())) {
            throw new IllegalArgumentException("Contenu invalide (10-5000 caractères)");
        }

        try (Connection conn = Databaseconnection.getConnection()) {
            // VÉRIFICATION DE SÉCURITÉ: L'utilisateur doit avoir créé ou rejoint le forum
            // Utiliser UNION pour combiner les deux sources
            String checkPermissionQuery = "SELECT 1 FROM forums f " +
                                         "WHERE f.id = ? AND f.creator_id = ? " +
                                         "UNION " +
                                         "SELECT 1 FROM forums f " +
                                         "INNER JOIN user_forum uf ON f.id = uf.forum_id " +
                                         "WHERE f.id = ? AND uf.user_id = ?";
            
            try (PreparedStatement checkStmt = conn.prepareStatement(checkPermissionQuery)) {
                checkStmt.setInt(1, post.getForumId());
                checkStmt.setInt(2, post.getAuthorId());
                checkStmt.setInt(3, post.getForumId());
                checkStmt.setInt(4, post.getAuthorId());
                ResultSet permRs = checkStmt.executeQuery();
                
                if (!permRs.next()) {
                    throw new SecurityException("Vous devez être membre du forum pour créer un post");
                }
            }
            
            // Créer le post
            String query = "INSERT INTO posts (forum_id, title, content, author_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, post.getForumId());
                stmt.setString(2, ValidationUtils.sanitize(post.getTitle()));
                stmt.setString(3, ValidationUtils.sanitize(post.getContent()));
                stmt.setInt(4, post.getAuthorId());

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        post.setId(generatedKeys.getInt(1));
                    }
                    
                    // Vérifier les badges après création du post
                    org.esprit.finovate.utils.BadgeManager.checkPostBadges(post.getAuthorId());
                    
                    return true;
                }
                return false;
            }
        }
    }

    /**
     * READ - Récupérer un post par ID
     */
    public Post getPostById(int id) throws SQLException {
        String query = "SELECT * FROM posts WHERE id = ?";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToPost(rs);
            }
            return null;
        }
    }

    /**
     * READ - Récupérer tous les posts d'un forum
     */
    public List<Post> getPostsByForum(int forumId) throws SQLException {
        List<Post> posts = new ArrayList<>();
        String query = "SELECT * FROM posts WHERE forum_id = ? ORDER BY created_at DESC";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, forumId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                posts.add(mapResultSetToPost(rs));
            }
        }
        return posts;
    }

    /**
     * READ - Récupérer les posts d'un auteur
     */
    public List<Post> getPostsByAuthor(int authorId) throws SQLException {
        List<Post> posts = new ArrayList<>();
        String query = "SELECT * FROM posts WHERE author_id = ? ORDER BY created_at DESC";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, authorId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                posts.add(mapResultSetToPost(rs));
            }
        }
        return posts;
    }

    /**
     * UPDATE - Modifier un post
     * Contrôles:
     * - Utilisateur peut modifier seulement ses propres posts
     * - Utilisateur ne doit pas être bloqué
     */
    public boolean updatePost(Post post, int requestingUserId, boolean isUserBlocked) throws SQLException {
        if (isUserBlocked) {
            throw new SecurityException("Utilisateur bloqué: impossible de modifier un post");
        }

        // Vérifier les droits
        Post existingPost = getPostById(post.getId());
        if (existingPost == null || existingPost.getAuthorId() != requestingUserId) {
            throw new SecurityException("Vous ne pouvez modifier que vos propres posts");
        }

        // Validation
        if (!ValidationUtils.isValidPostTitle(post.getTitle())) {
            throw new IllegalArgumentException("Titre invalide");
        }
        if (!ValidationUtils.isValidPostContent(post.getContent())) {
            throw new IllegalArgumentException("Contenu invalide");
        }

        String query = "UPDATE posts SET title = ?, content = ? WHERE id = ?";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, ValidationUtils.sanitize(post.getTitle()));
            stmt.setString(2, ValidationUtils.sanitize(post.getContent()));
            stmt.setInt(3, post.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * DELETE - Supprimer un post
     * Contrôles:
     * - Utilisateur peut supprimer seulement ses propres posts
     */
    public boolean deletePost(int postId, int requestingUserId) throws SQLException {
        // Vérifier les droits
        Post post = getPostById(postId);
        if (post == null || post.getAuthorId() != requestingUserId) {
            throw new SecurityException("Vous ne pouvez supprimer que vos propres posts");
        }

        String query = "DELETE FROM posts WHERE id = ?";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, postId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Compter les commentaires d'un post
     */
    public int getCommentCount(int postId) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM comments WHERE post_id = ?";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }
            return 0;
        }
    }

    /**
     * Mapper ResultSet vers Post
     */
    private Post mapResultSetToPost(ResultSet rs) throws SQLException {
        return new Post(
                rs.getInt("id"),
                rs.getInt("forum_id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getInt("author_id"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at")
        );
    }
}