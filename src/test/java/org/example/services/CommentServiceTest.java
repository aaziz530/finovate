package org.example.services;

import org.example.entities.Comment;
import org.example.entities.User;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

import java.sql.*;
import java.util.List;

/**
 * Tests unitaires pour CommentService
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommentServiceTest {

    private static Connection testConnection;
    private static CommentService commentService;
    private static final String TEST_DB_URL = "jdbc:h2:mem:commenttest;DB_CLOSE_DELAY=-1;MODE=MySQL";

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        testConnection = DriverManager.getConnection(TEST_DB_URL, "sa", "");
        createTables();
        commentService = new CommentService();
    }

    @AfterAll
    static void tearDownDatabase() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        cleanDatabase();
        insertTestData();
    }

    private static void createTables() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS posts (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "title VARCHAR(200) NOT NULL, " +
                "content TEXT NOT NULL, " +
                "author_id INT NOT NULL)"
            );
            
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS comments (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "post_id INT NOT NULL, " +
                "author_id INT NOT NULL, " +
                "content TEXT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );
        }
    }

    private void cleanDatabase() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM comments");
            stmt.execute("DELETE FROM posts");
        }
    }

    private void insertTestData() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("INSERT INTO posts (id, title, content, author_id) VALUES (1, 'Test Post', 'Content', 1)");
        }
    }

    // ==================== TESTS CREATE ====================

    @Test
    @Order(1)
    @DisplayName("Devrait créer un commentaire avec des données valides")
    void createComment_WithValidData_ShouldReturnTrue() throws SQLException {
        // Arrange
        Comment comment = new Comment(1, 1, "This is a valid comment");

        // Act
        boolean result = commentService.createComment(comment, false);

        // Assert
        assertThat(result).isTrue();
        assertThat(comment.getId()).isGreaterThan(0);
    }

    @Test
    @Order(2)
    @DisplayName("Devrait rejeter si utilisateur bloqué")
    void createComment_WhenUserBlocked_ShouldThrowSecurityException() {
        // Arrange
        Comment comment = new Comment(1, 1, "Valid comment");

        // Act & Assert
        assertThatThrownBy(() -> commentService.createComment(comment, true))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("bloqué");
    }

    @Test
    @Order(3)
    @DisplayName("Devrait rejeter un contenu invalide")
    void createComment_WithInvalidContent_ShouldThrowException() {
        // Arrange
        Comment comment = new Comment(1, 1, "");

        // Act & Assert
        assertThatThrownBy(() -> commentService.createComment(comment, false))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Contenu invalide");
    }

    // ==================== TESTS READ ====================

    @Test
    @Order(4)
    @DisplayName("Devrait récupérer un commentaire par ID")
    void getCommentById_WithValidId_ShouldReturnComment() throws SQLException {
        // Arrange
        Comment comment = new Comment(1, 1, "Test comment");
        commentService.createComment(comment, false);

        // Act
        Comment foundComment = commentService.getCommentById(comment.getId());

        // Assert
        assertThat(foundComment).isNotNull();
        assertThat(foundComment.getContent()).isEqualTo("Test comment");
    }

    @Test
    @Order(5)
    @DisplayName("Devrait récupérer les commentaires d'un post")
    void getCommentsByPost_ShouldReturnComments() throws SQLException {
        // Arrange
        commentService.createComment(new Comment(1, 1, "Comment 1"), false);
        commentService.createComment(new Comment(1, 1, "Comment 2"), false);
        commentService.createComment(new Comment(1, 2, "Comment 3"), false);

        // Act
        List<Comment> comments = commentService.getCommentsByPost(1);

        // Assert
        assertThat(comments).hasSize(3);
    }

    @Test
    @Order(6)
    @DisplayName("Devrait récupérer les commentaires d'un auteur")
    void getCommentsByAuthor_ShouldReturnComments() throws SQLException {
        // Arrange
        commentService.createComment(new Comment(1, 1, "Comment 1"), false);
        commentService.createComment(new Comment(1, 1, "Comment 2"), false);
        commentService.createComment(new Comment(1, 2, "Comment 3"), false);

        // Act
        List<Comment> comments = commentService.getCommentsByAuthor(1);

        // Assert
        assertThat(comments).hasSize(2);
    }

    // ==================== TESTS UPDATE ====================

    @Test
    @Order(7)
    @DisplayName("USER devrait pouvoir modifier son propre commentaire")
    void updateComment_OwnComment_ShouldReturnTrue() throws SQLException {
        // Arrange
        Comment comment = new Comment(1, 1, "Original comment");
        commentService.createComment(comment, false);
        
        comment.setContent("Updated comment");

        // Act
        boolean result = commentService.updateComment(comment, 1, User.Role.USER, false);

        // Assert
        assertThat(result).isTrue();
        Comment updatedComment = commentService.getCommentById(comment.getId());
        assertThat(updatedComment.getContent()).isEqualTo("Updated comment");
    }

    @Test
    @Order(8)
    @DisplayName("USER ne devrait pas pouvoir modifier le commentaire d'un autre")
    void updateComment_OtherUserComment_ShouldThrowSecurityException() throws SQLException {
        // Arrange
        Comment comment = new Comment(1, 1, "Test comment");
        commentService.createComment(comment, false);
        
        comment.setContent("Hacked comment");

        // Act & Assert
        assertThatThrownBy(() -> commentService.updateComment(comment, 2, User.Role.USER, false))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("propres commentaires");
    }

    @Test
    @Order(9)
    @DisplayName("ADMIN devrait pouvoir modifier n'importe quel commentaire")
    void updateComment_AsAdmin_ShouldReturnTrue() throws SQLException {
        // Arrange
        Comment comment = new Comment(1, 1, "Test comment");
        commentService.createComment(comment, false);
        
        comment.setContent("Admin updated");

        // Act
        boolean result = commentService.updateComment(comment, 999, User.Role.ADMIN, false);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @Order(10)
    @DisplayName("Devrait rejeter la modification si utilisateur bloqué")
    void updateComment_WhenUserBlocked_ShouldThrowSecurityException() throws SQLException {
        // Arrange
        Comment comment = new Comment(1, 1, "Test comment");
        commentService.createComment(comment, false);

        // Act & Assert
        assertThatThrownBy(() -> commentService.updateComment(comment, 1, User.Role.USER, true))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("bloqué");
    }

    // ==================== TESTS DELETE ====================

    @Test
    @Order(11)
    @DisplayName("USER devrait pouvoir supprimer son propre commentaire")
    void deleteComment_OwnComment_ShouldReturnTrue() throws SQLException {
        // Arrange
        Comment comment = new Comment(1, 1, "Test comment");
        commentService.createComment(comment, false);

        // Act
        boolean result = commentService.deleteComment(comment.getId(), 1, User.Role.USER);

        // Assert
        assertThat(result).isTrue();
        assertThat(commentService.getCommentById(comment.getId())).isNull();
    }

    @Test
    @Order(12)
    @DisplayName("USER ne devrait pas pouvoir supprimer le commentaire d'un autre")
    void deleteComment_OtherUserComment_ShouldThrowSecurityException() throws SQLException {
        // Arrange
        Comment comment = new Comment(1, 1, "Test comment");
        commentService.createComment(comment, false);

        // Act & Assert
        assertThatThrownBy(() -> commentService.deleteComment(comment.getId(), 2, User.Role.USER))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("propres commentaires");
    }

    @Test
    @Order(13)
    @DisplayName("MODERATOR ne devrait pas pouvoir supprimer des commentaires")
    void deleteComment_AsModerator_ShouldThrowSecurityException() throws SQLException {
        // Arrange
        Comment comment = new Comment(1, 1, "Test comment");
        commentService.createComment(comment, false);

        // Act & Assert
        assertThatThrownBy(() -> commentService.deleteComment(comment.getId(), 999, User.Role.MODERATOR))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("modérateurs");
    }

    @Test
    @Order(14)
    @DisplayName("ADMIN devrait pouvoir supprimer n'importe quel commentaire")
    void deleteComment_AsAdmin_ShouldReturnTrue() throws SQLException {
        // Arrange
        Comment comment = new Comment(1, 1, "Test comment");
        commentService.createComment(comment, false);

        // Act
        boolean result = commentService.deleteComment(comment.getId(), 999, User.Role.ADMIN);

        // Assert
        assertThat(result).isTrue();
    }
}
