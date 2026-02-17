package org.example.services;

import org.example.entities.Forum;
import org.example.entities.User;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

import java.sql.*;
import java.util.List;

/**
 * Tests unitaires pour ForumService
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ForumServiceTest {

    private static Connection testConnection;
    private static ForumService forumService;
    private static final String TEST_DB_URL = "jdbc:h2:mem:forumtest;DB_CLOSE_DELAY=-1;MODE=MySQL";

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        testConnection = DriverManager.getConnection(TEST_DB_URL, "sa", "");
        createTables();
        forumService = new ForumService();
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
    }

    private static void createTables() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS forums (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "title VARCHAR(100) NOT NULL, " +
                "description TEXT, " +
                "creator_id BIGINT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );
        }
    }

    private void cleanDatabase() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM forums");
            stmt.execute("ALTER TABLE forums ALTER COLUMN id RESTART WITH 1");
        }
    }

    // ==================== TESTS CREATE ====================

    @Test
    @Order(1)
    @DisplayName("Devrait créer un forum avec des données valides")
    void createForum_WithValidData_ShouldReturnTrue() throws SQLException {
        // Arrange
        Forum forum = new Forum(1L, "Crypto Trading", "Forum about cryptocurrency trading");

        // Act
        boolean result = forumService.createForum(forum, false);

        // Assert
        assertThat(result).isTrue();
        assertThat(forum.getId()).isNotNull();
        assertThat(forum.getId()).isGreaterThan(0L);
    }

    @Test
    @Order(2)
    @DisplayName("Devrait rejeter si utilisateur bloqué")
    void createForum_WhenUserBlocked_ShouldThrowSecurityException() {
        // Arrange
        Forum forum = new Forum(1L, "Test Forum", "Test Description");

        // Act & Assert
        assertThatThrownBy(() -> forumService.createForum(forum, true))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("bloqué");
    }

    @Test
    @Order(3)
    @DisplayName("Devrait rejeter un titre invalide")
    void createForum_WithInvalidTitle_ShouldThrowException() {
        // Arrange
        Forum forum = new Forum(1L, "AB", "Valid description here");

        // Act & Assert
        assertThatThrownBy(() -> forumService.createForum(forum, false))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Titre invalide");
    }

    @Test
    @Order(4)
    @DisplayName("Devrait rejeter une description invalide")
    void createForum_WithInvalidDescription_ShouldThrowException() {
        // Arrange
        Forum forum = new Forum(1L, "Valid Title", "Short");

        // Act & Assert
        assertThatThrownBy(() -> forumService.createForum(forum, false))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Description invalide");
    }

    // ==================== TESTS READ ====================

    @Test
    @Order(5)
    @DisplayName("Devrait récupérer tous les forums")
    void getAllForums_ShouldReturnAllForums() throws SQLException {
        // Arrange
        forumService.createForum(new Forum(1L, "Forum 1", "Description 1 with enough characters"), false);
        forumService.createForum(new Forum(1L, "Forum 2", "Description 2 with enough characters"), false);
        forumService.createForum(new Forum(2L, "Forum 3", "Description 3 with enough characters"), false);

        // Act
        List<Forum> forums = forumService.getAllForums();

        // Assert
        assertThat(forums).hasSize(3);
    }

    @Test
    @Order(6)
    @DisplayName("Devrait récupérer un forum par ID")
    void getForumById_WithValidId_ShouldReturnForum() throws SQLException {
        // Arrange
        Forum forum = new Forum(1L, "Test Forum", "Test Description with enough characters");
        forumService.createForum(forum, false);

        // Act
        Forum foundForum = forumService.getForumById(forum.getId());

        // Assert
        assertThat(foundForum).isNotNull();
        assertThat(foundForum.getTitle()).isEqualTo("Test Forum");
    }

    @Test
    @Order(7)
    @DisplayName("Devrait retourner null pour un ID inexistant")
    void getForumById_WithInvalidId_ShouldReturnNull() throws SQLException {
        // Act
        Forum forum = forumService.getForumById(999L);

        // Assert
        assertThat(forum).isNull();
    }

    // ==================== TESTS UPDATE ====================

    @Test
    @Order(8)
    @DisplayName("USER devrait pouvoir modifier son propre forum")
    void updateForum_OwnForum_ShouldReturnTrue() throws SQLException {
        // Arrange
        Forum forum = new Forum(1L, "Original Title", "Original description with enough characters");
        forumService.createForum(forum, false);
        
        forum.setTitle("Updated Title");
        forum.setDescription("Updated description with enough characters");

        // Act
        boolean result = forumService.updateForum(forum, 1L, User.Role.USER, false);

        // Assert
        assertThat(result).isTrue();
        Forum updatedForum = forumService.getForumById(forum.getId());
        assertThat(updatedForum.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    @Order(9)
    @DisplayName("USER ne devrait pas pouvoir modifier le forum d'un autre")
    void updateForum_OtherUserForum_ShouldThrowSecurityException() throws SQLException {
        // Arrange
        Forum forum = new Forum(1L, "Test Forum", "Description with enough characters");
        forumService.createForum(forum, false);
        
        forum.setTitle("Hacked Title");

        // Act & Assert
        assertThatThrownBy(() -> forumService.updateForum(forum, 2L, User.Role.USER, false))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("vos forums");
    }

    @Test
    @Order(10)
    @DisplayName("ADMIN devrait pouvoir modifier n'importe quel forum")
    void updateForum_AsAdmin_ShouldReturnTrue() throws SQLException {
        // Arrange
        Forum forum = new Forum(1L, "Test Forum", "Description with enough characters");
        forumService.createForum(forum, false);
        
        forum.setTitle("Admin Updated");
        forum.setDescription("Admin updated description with enough characters");

        // Act
        boolean result = forumService.updateForum(forum, 999L, User.Role.ADMIN, false);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @Order(11)
    @DisplayName("Devrait rejeter la modification si utilisateur bloqué")
    void updateForum_WhenUserBlocked_ShouldThrowSecurityException() throws SQLException {
        // Arrange
        Forum forum = new Forum(1L, "Test Forum", "Description with enough characters");
        forumService.createForum(forum, false);

        // Act & Assert
        assertThatThrownBy(() -> forumService.updateForum(forum, 1L, User.Role.USER, true))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("bloqué");
    }

    // ==================== TESTS DELETE ====================

    @Test
    @Order(12)
    @DisplayName("USER devrait pouvoir supprimer son propre forum")
    void deleteForum_OwnForum_ShouldReturnTrue() throws SQLException {
        // Arrange
        Forum forum = new Forum(1L, "Test Forum", "Description with enough characters");
        forumService.createForum(forum, false);

        // Act
        boolean result = forumService.deleteForum(forum.getId(), 1L, User.Role.USER);

        // Assert
        assertThat(result).isTrue();
        assertThat(forumService.getForumById(forum.getId())).isNull();
    }

    @Test
    @Order(13)
    @DisplayName("USER ne devrait pas pouvoir supprimer le forum d'un autre")
    void deleteForum_OtherUserForum_ShouldThrowSecurityException() throws SQLException {
        // Arrange
        Forum forum = new Forum(1L, "Test Forum", "Description with enough characters");
        forumService.createForum(forum, false);

        // Act & Assert
        assertThatThrownBy(() -> forumService.deleteForum(forum.getId(), 2L, User.Role.USER))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("vos forums");
    }

    @Test
    @Order(14)
    @DisplayName("ADMIN devrait pouvoir supprimer n'importe quel forum")
    void deleteForum_AsAdmin_ShouldReturnTrue() throws SQLException {
        // Arrange
        Forum forum = new Forum(1L, "Test Forum", "Description with enough characters");
        forumService.createForum(forum, false);

        // Act
        boolean result = forumService.deleteForum(forum.getId(), 999L, User.Role.ADMIN);

        // Assert
        assertThat(result).isTrue();
    }
}
