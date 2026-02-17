package org.example.services;

import org.example.entities.Post;
import org.example.entities.User;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

import java.sql.*;
import java.util.List;

/**
 * Tests unitaires pour PostService
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostServiceTest {

    private static Connection testConnection;
    private static PostService postService;
    private static final String TEST_DB_URL = "jdbc:h2:mem:posttest;DB_CLOSE_DELAY=-1;MODE=MySQL";

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        testConnection = DriverManager.getConnection(TEST_DB_URL, "sa", "");
        createTables();
        postService = new PostService();
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
                "CREATE TABLE IF NOT EXISTS forums (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "title VARCHAR(100) NOT NULL, " +
                "description TEXT, " +
                "creator_id INT NOT NULL)"
            );
            
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS posts (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "forum_id INT NOT NULL, " +
                "title VARCHAR(200) NOT NULL, " +
                "content TEXT NOT NULL, " +
                "author_id INT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );
            
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS comments (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "post_id INT NOT NULL, " +
                "author_id INT NOT NULL, " +
                "content TEXT NOT NULL)"
            );
        }
    }

    private void cleanDatabase() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM comments");
            stmt.execute("DELETE FROM posts");
            stmt.execute("DELETE FROM forums");
        }
    }

    private void insertTestData() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("INSERT INTO forums (id, title, description, creator_id) VALUES (1, 'Test Forum', 'Description', 1)");
        }
    }

    // ==================== TESTS CREATE ====================

    @Test
    @Order(1)
    @DisplayName("Devrait créer un post avec des données valides")
    void createPost_WithValidData_ShouldReturnTrue() throws SQLException {
        // Arrange
        Post post = new Post(1, "Test Post Title", "This is a valid post content with enough characters", 1);

        // Act
        boolean result = postService.createPost(post, false);

        // Assert
        assertThat(result).isTrue();
        assertThat(post.getId()).isGreaterThan(0);
    }

    @Test
    @Order(2)
    @DisplayName("Devrait rejeter si utilisateur bloqué")
    void createPost_WhenUserBlocked_ShouldThrowSecurityException() {
        // Arrange
        Post post = new Post(1, "Test Post", "Valid content here", 1);

        // Act & Assert
        assertThatThrownBy(() -> postService.createPost(post, true))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("bloqué");
    }

    @Test
    @Order(3)
    @DisplayName("Devrait rejeter un titre invalide")
    void createPost_WithInvalidTitle_ShouldThrowException() {
        // Arrange
        Post post = new Post(1, "Bad", "Valid content here with enough characters", 1);

        // Act & Assert
        assertThatThrownBy(() -> postService.createPost(post, false))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Titre invalide");
    }

    @Test
    @Order(4)
    @DisplayName("Devrait rejeter un contenu invalide")
    void createPost_WithInvalidContent_ShouldThrowException() {
        // Arrange
        Post post = new Post(1, "Valid Title Here", "Short", 1);

        // Act & Assert
        assertThatThrownBy(() -> postService.createPost(post, false))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Contenu invalide");
    }

    // ==================== TESTS READ ====================

    @Test
    @Order(5)
    @DisplayName("Devrait récupérer un post par ID")
    void getPostById_WithValidId_ShouldReturnPost() throws SQLException {
        // Arrange
        Post post = new Post(1, "Test Post", "Valid content here", 1);
        postService.createPost(post, false);

        // Act
        Post foundPost = postService.getPostById(post.getId());

        // Assert
        assertThat(foundPost).isNotNull();
        assertThat(foundPost.getTitle()).isEqualTo("Test Post");
    }

    @Test
    @Order(6)
    @DisplayName("Devrait récupérer les posts d'un forum")
    void getPostsByForum_ShouldReturnPosts() throws SQLException {
        // Arrange
        postService.createPost(new Post(1, "Post 1", "Content 1 with enough characters", 1), false);
        postService.createPost(new Post(1, "Post 2", "Content 2 with enough characters", 1), false);
        postService.createPost(new Post(1, "Post 3", "Content 3 with enough characters", 1), false);

        // Act
        List<Post> posts = postService.getPostsByForum(1);

        // Assert
        assertThat(posts).hasSize(3);
    }

    @Test
    @Order(7)
    @DisplayName("Devrait récupérer les posts d'un auteur")
    void getPostsByAuthor_ShouldReturnPosts() throws SQLException {
        // Arrange
        postService.createPost(new Post(1, "Post 1", "Content 1 with enough characters", 1), false);
        postService.createPost(new Post(1, "Post 2", "Content 2 with enough characters", 1), false);
        postService.createPost(new Post(1, "Post 3", "Content 3 with enough characters", 2), false);

        // Act
        List<Post> posts = postService.getPostsByAuthor(1);

        // Assert
        assertThat(posts).hasSize(2);
    }

    // ==================== TESTS UPDATE ====================

    @Test
    @Order(8)
    @DisplayName("USER devrait pouvoir modifier son propre post")
    void updatePost_OwnPost_ShouldReturnTrue() throws SQLException {
        // Arrange
        Post post = new Post(1, "Original Title", "Original content with enough characters", 1);
        postService.createPost(post, false);
        
        post.setTitle("Updated Title");
        post.setContent("Updated content with enough characters");

        // Act
        boolean result = postService.updatePost(post, 1, User.Role.USER, false);

        // Assert
        assertThat(result).isTrue();
        Post updatedPost = postService.getPostById(post.getId());
        assertThat(updatedPost.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    @Order(9)
    @DisplayName("USER ne devrait pas pouvoir modifier le post d'un autre")
    void updatePost_OtherUserPost_ShouldThrowSecurityException() throws SQLException {
        // Arrange
        Post post = new Post(1, "Test Post", "Content with enough characters", 1);
        postService.createPost(post, false);
        
        post.setTitle("Hacked Title");

        // Act & Assert
        assertThatThrownBy(() -> postService.updatePost(post, 2, User.Role.USER, false))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("propres posts");
    }

    @Test
    @Order(10)
    @DisplayName("ADMIN devrait pouvoir modifier n'importe quel post")
    void updatePost_AsAdmin_ShouldReturnTrue() throws SQLException {
        // Arrange
        Post post = new Post(1, "Test Post", "Content with enough characters", 1);
        postService.createPost(post, false);
        
        post.setTitle("Admin Updated Title");
        post.setContent("Admin updated content with enough characters");

        // Act
        boolean result = postService.updatePost(post, 999, User.Role.ADMIN, false);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @Order(11)
    @DisplayName("Devrait rejeter la modification si utilisateur bloqué")
    void updatePost_WhenUserBlocked_ShouldThrowSecurityException() throws SQLException {
        // Arrange
        Post post = new Post(1, "Test Post", "Content with enough characters", 1);
        postService.createPost(post, false);

        // Act & Assert
        assertThatThrownBy(() -> postService.updatePost(post, 1, User.Role.USER, true))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("bloqué");
    }

    // ==================== TESTS DELETE ====================

    @Test
    @Order(12)
    @DisplayName("USER devrait pouvoir supprimer son propre post")
    void deletePost_OwnPost_ShouldReturnTrue() throws SQLException {
        // Arrange
        Post post = new Post(1, "Test Post", "Content with enough characters", 1);
        postService.createPost(post, false);

        // Act
        boolean result = postService.deletePost(post.getId(), 1, User.Role.USER);

        // Assert
        assertThat(result).isTrue();
        assertThat(postService.getPostById(post.getId())).isNull();
    }

    @Test
    @Order(13)
    @DisplayName("USER ne devrait pas pouvoir supprimer le post d'un autre")
    void deletePost_OtherUserPost_ShouldThrowSecurityException() throws SQLException {
        // Arrange
        Post post = new Post(1, "Test Post", "Content with enough characters", 1);
        postService.createPost(post, false);

        // Act & Assert
        assertThatThrownBy(() -> postService.deletePost(post.getId(), 2, User.Role.USER))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("propres posts");
    }

    @Test
    @Order(14)
    @DisplayName("MODERATOR ne devrait pas pouvoir supprimer des posts")
    void deletePost_AsModerator_ShouldThrowSecurityException() throws SQLException {
        // Arrange
        Post post = new Post(1, "Test Post", "Content with enough characters", 1);
        postService.createPost(post, false);

        // Act & Assert
        assertThatThrownBy(() -> postService.deletePost(post.getId(), 999, User.Role.MODERATOR))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("modérateurs");
    }

    @Test
    @Order(15)
    @DisplayName("ADMIN devrait pouvoir supprimer n'importe quel post")
    void deletePost_AsAdmin_ShouldReturnTrue() throws SQLException {
        // Arrange
        Post post = new Post(1, "Test Post", "Content with enough characters", 1);
        postService.createPost(post, false);

        // Act
        boolean result = postService.deletePost(post.getId(), 999, User.Role.ADMIN);

        // Assert
        assertThat(result).isTrue();
    }

    // ==================== TESTS UTILS ====================

    @Test
    @Order(16)
    @DisplayName("Devrait compter les commentaires d'un post")
    void getCommentCount_ShouldReturnCorrectCount() throws SQLException {
        // Arrange
        Post post = new Post(1, "Test Post", "Content with enough characters", 1);
        postService.createPost(post, false);
        
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("INSERT INTO comments (post_id, author_id, content) VALUES " +
                "(" + post.getId() + ", 1, 'Comment 1'), " +
                "(" + post.getId() + ", 1, 'Comment 2'), " +
                "(" + post.getId() + ", 2, 'Comment 3')");
        }

        // Act
        int count = postService.getCommentCount(post.getId());

        // Assert
        assertThat(count).isEqualTo(3);
    }
}
