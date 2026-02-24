package org.example.dao;

import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

import java.sql.*;
import java.util.List;

/**
 * Tests unitaires pour ForumDAO
 * Utilise une base de données H2 en mémoire pour les tests
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ForumDAOTest {

    private static Connection testConnection;
    private static ForumDAO forumDAO;
    private static final String TEST_DB_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL";
    private static final String TEST_DB_USER = "sa";
    private static final String TEST_DB_PASSWORD = "";

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        // Créer la connexion à la base de données de test
        testConnection = DriverManager.getConnection(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD);
        
        // Créer les tables nécessaires
        createTables();
        
        // Créer le DAO avec la base de test
        forumDAO = new ForumDAO(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD);
    }

    @AfterAll
    static void tearDownDatabase() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        // Nettoyer les données avant chaque test
        cleanDatabase();
        
        // Insérer des données de test
        insertTestData();
    }

    private static void createTables() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            // Table users
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) NOT NULL, " +
                "email VARCHAR(100) NOT NULL, " +
                "password VARCHAR(255) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );

            // Table forums
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS forums (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "description TEXT, " +
                "creator_id INT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE)"
            );

            // Table user_forum
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS user_forum (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "forum_id INT NOT NULL, " +
                "user_id INT NOT NULL, " +
                "joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (forum_id) REFERENCES forums(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)"
            );
        }
    }

    private void cleanDatabase() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM user_forum");
            stmt.execute("DELETE FROM forums");
            stmt.execute("DELETE FROM users");
            stmt.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE forums ALTER COLUMN id RESTART WITH 1");
        }
    }

    private void insertTestData() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            // Insérer des utilisateurs de test
            stmt.execute("INSERT INTO users (username, email, password) VALUES " +
                "('testuser1', 'test1@example.com', 'password123'), " +
                "('testuser2', 'test2@example.com', 'password456')");
        }
    }

    // ==================== TESTS CREATE ====================

    @Test
    @Order(1)
    @DisplayName("Devrait créer un forum avec des données valides")
    void createForum_WithValidData_ShouldReturnForumId() throws SQLException {
        // Arrange
        String name = "Crypto Trading";
        String description = "Forum pour discuter de trading crypto";
        int creatorId = 1;

        // Act
        int forumId = forumDAO.createForum(name, description, creatorId);

        // Assert
        assertThat(forumId).isGreaterThan(0);
        
        // Vérifier que le forum existe dans la DB
        ForumDAO.Forum forum = forumDAO.getForumById(forumId);
        assertThat(forum).isNotNull();
        assertThat(forum.getName()).isEqualTo(name);
        assertThat(forum.getDescription()).isEqualTo(description);
        assertThat(forum.getCreatorId()).isEqualTo(creatorId);
    }

    @Test
    @Order(2)
    @DisplayName("Devrait lancer une exception avec un creatorId invalide")
    void createForum_WithInvalidCreatorId_ShouldThrowException() {
        // Arrange
        String name = "Test Forum";
        String description = "Test Description";
        int invalidCreatorId = 999; // N'existe pas

        // Act & Assert
        assertThatThrownBy(() -> forumDAO.createForum(name, description, invalidCreatorId))
            .isInstanceOf(SQLException.class);
    }

    @Test
    @Order(3)
    @DisplayName("Devrait créer plusieurs forums")
    void createForum_MultipleForums_ShouldReturnDifferentIds() throws SQLException {
        // Arrange & Act
        int forumId1 = forumDAO.createForum("Forum 1", "Description 1", 1);
        int forumId2 = forumDAO.createForum("Forum 2", "Description 2", 1);
        int forumId3 = forumDAO.createForum("Forum 3", "Description 3", 2);

        // Assert
        assertThat(forumId1).isNotEqualTo(forumId2);
        assertThat(forumId2).isNotEqualTo(forumId3);
        assertThat(forumId1).isNotEqualTo(forumId3);
    }

    // ==================== TESTS READ ====================

    @Test
    @Order(4)
    @DisplayName("Devrait récupérer un forum par son ID")
    void getForumById_WithValidId_ShouldReturnForum() throws SQLException {
        // Arrange
        int forumId = forumDAO.createForum("Test Forum", "Test Description", 1);

        // Act
        ForumDAO.Forum forum = forumDAO.getForumById(forumId);

        // Assert
        assertThat(forum).isNotNull();
        assertThat(forum.getId()).isEqualTo(forumId);
        assertThat(forum.getName()).isEqualTo("Test Forum");
        assertThat(forum.getDescription()).isEqualTo("Test Description");
        assertThat(forum.getCreatorId()).isEqualTo(1);
        assertThat(forum.getCreatedAt()).isNotNull();
    }

    @Test
    @Order(5)
    @DisplayName("Devrait retourner null pour un ID inexistant")
    void getForumById_WithInvalidId_ShouldReturnNull() throws SQLException {
        // Act
        ForumDAO.Forum forum = forumDAO.getForumById(999);

        // Assert
        assertThat(forum).isNull();
    }

    @Test
    @Order(6)
    @DisplayName("Devrait récupérer tous les forums")
    void getAllForums_ShouldReturnAllForums() throws SQLException {
        // Arrange
        forumDAO.createForum("Forum 1", "Description 1", 1);
        forumDAO.createForum("Forum 2", "Description 2", 1);
        forumDAO.createForum("Forum 3", "Description 3", 2);

        // Act
        List<ForumDAO.Forum> forums = forumDAO.getAllForums();

        // Assert
        assertThat(forums).hasSize(3);
        assertThat(forums)
            .extracting(ForumDAO.Forum::getName)
            .containsExactlyInAnyOrder("Forum 1", "Forum 2", "Forum 3");
    }

    @Test
    @Order(7)
    @DisplayName("Devrait retourner une liste vide quand aucun forum")
    void getAllForums_WhenNoForums_ShouldReturnEmptyList() throws SQLException {
        // Act
        List<ForumDAO.Forum> forums = forumDAO.getAllForums();

        // Assert
        assertThat(forums).isEmpty();
    }

    @Test
    @Order(8)
    @DisplayName("Devrait récupérer les forums d'un créateur")
    void getForumsByCreator_ShouldReturnCreatorForums() throws SQLException {
        // Arrange
        forumDAO.createForum("Forum 1", "Description 1", 1);
        forumDAO.createForum("Forum 2", "Description 2", 1);
        forumDAO.createForum("Forum 3", "Description 3", 2);

        // Act
        List<ForumDAO.Forum> user1Forums = forumDAO.getForumsByCreator(1);
        List<ForumDAO.Forum> user2Forums = forumDAO.getForumsByCreator(2);

        // Assert
        assertThat(user1Forums).hasSize(2);
        assertThat(user2Forums).hasSize(1);
        assertThat(user1Forums)
            .extracting(ForumDAO.Forum::getName)
            .containsExactlyInAnyOrder("Forum 1", "Forum 2");
    }

    // ==================== TESTS UPDATE ====================

    @Test
    @Order(9)
    @DisplayName("Devrait mettre à jour un forum")
    void updateForum_WithValidData_ShouldReturnTrue() throws SQLException {
        // Arrange
        int forumId = forumDAO.createForum("Old Name", "Old Description", 1);
        String newName = "New Name";
        String newDescription = "New Description";

        // Act
        boolean updated = forumDAO.updateForum(forumId, newName, newDescription);

        // Assert
        assertThat(updated).isTrue();
        
        ForumDAO.Forum forum = forumDAO.getForumById(forumId);
        assertThat(forum.getName()).isEqualTo(newName);
        assertThat(forum.getDescription()).isEqualTo(newDescription);
    }

    @Test
    @Order(10)
    @DisplayName("Devrait retourner false pour un ID inexistant")
    void updateForum_WithInvalidId_ShouldReturnFalse() throws SQLException {
        // Act
        boolean updated = forumDAO.updateForum(999, "New Name", "New Description");

        // Assert
        assertThat(updated).isFalse();
    }

    // ==================== TESTS DELETE ====================

    @Test
    @Order(11)
    @DisplayName("Devrait supprimer un forum")
    void deleteForum_WithValidId_ShouldReturnTrue() throws SQLException {
        // Arrange
        int forumId = forumDAO.createForum("Test Forum", "Test Description", 1);

        // Act
        boolean deleted = forumDAO.deleteForum(forumId, 1);

        // Assert
        assertThat(deleted).isTrue();
        assertThat(forumDAO.getForumById(forumId)).isNull();
    }

    @Test
    @Order(12)
    @DisplayName("Devrait retourner false si le créateur ne correspond pas")
    void deleteForum_WithWrongCreator_ShouldReturnFalse() throws SQLException {
        // Arrange
        int forumId = forumDAO.createForum("Test Forum", "Test Description", 1);

        // Act
        boolean deleted = forumDAO.deleteForum(forumId, 2); // Mauvais créateur

        // Assert
        assertThat(deleted).isFalse();
        assertThat(forumDAO.getForumById(forumId)).isNotNull(); // Forum toujours là
    }

    @Test
    @Order(13)
    @DisplayName("Devrait retourner false pour un ID inexistant")
    void deleteForum_WithInvalidId_ShouldReturnFalse() throws SQLException {
        // Act
        boolean deleted = forumDAO.deleteForum(999, 1);

        // Assert
        assertThat(deleted).isFalse();
    }

    // ==================== TESTS UTILITAIRES ====================

    @Test
    @Order(14)
    @DisplayName("Devrait vérifier si un forum existe")
    void forumExists_WithValidId_ShouldReturnTrue() throws SQLException {
        // Arrange
        int forumId = forumDAO.createForum("Test Forum", "Test Description", 1);

        // Act & Assert
        assertThat(forumDAO.forumExists(forumId)).isTrue();
        assertThat(forumDAO.forumExists(999)).isFalse();
    }

    @Test
    @Order(15)
    @DisplayName("Devrait compter les membres d'un forum")
    void getMemberCount_ShouldReturnCorrectCount() throws SQLException {
        // Arrange
        int forumId = forumDAO.createForum("Test Forum", "Test Description", 1);
        
        // Ajouter des membres
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("INSERT INTO user_forum (forum_id, user_id) VALUES " +
                "(" + forumId + ", 1), (" + forumId + ", 2)");
        }

        // Act
        int memberCount = forumDAO.getMemberCount(forumId);

        // Assert
        assertThat(memberCount).isEqualTo(2);
    }

    @Test
    @Order(16)
    @DisplayName("Devrait retourner 0 pour un forum sans membres")
    void getMemberCount_WithNoMembers_ShouldReturnZero() throws SQLException {
        // Arrange
        int forumId = forumDAO.createForum("Test Forum", "Test Description", 1);

        // Act
        int memberCount = forumDAO.getMemberCount(forumId);

        // Assert
        assertThat(memberCount).isEqualTo(0);
    }
}
