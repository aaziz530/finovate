package org.example.services;

import org.example.entities.User;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

import java.sql.*;

/**
 * Tests unitaires pour UserService
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {

    private static Connection testConnection;
    private static UserService userService;
    private static final String TEST_DB_URL = "jdbc:h2:mem:usertest;DB_CLOSE_DELAY=-1;MODE=MySQL";
    private static final String TEST_DB_USER = "sa";
    private static final String TEST_DB_PASSWORD = "";

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        testConnection = DriverManager.getConnection(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD);
        createTables();
        userService = new UserService();
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
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) NOT NULL UNIQUE, " +
                "email VARCHAR(100) NOT NULL UNIQUE, " +
                "password VARCHAR(255) NOT NULL, " +
                "role VARCHAR(20) NOT NULL, " +
                "is_blocked BOOLEAN DEFAULT FALSE, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );
        }
    }

    private void cleanDatabase() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM users");
            stmt.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
        }
    }

    // ==================== TESTS CREATE ====================

    @Test
    @Order(1)
    @DisplayName("Devrait créer un utilisateur avec des données valides")
    void createUser_WithValidData_ShouldReturnTrue() throws SQLException {
        // Arrange
        User user = new User("john_doe", "john@example.com", "password123", User.Role.USER);

        // Act
        boolean result = userService.createUser(user);

        // Assert
        assertThat(result).isTrue();
        assertThat(user.getId()).isGreaterThan(0);
    }

    @Test
    @Order(2)
    @DisplayName("Devrait rejeter un username invalide")
    void createUser_WithInvalidUsername_ShouldThrowException() {
        // Arrange
        User user = new User("ab", "test@example.com", "password123", User.Role.USER);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(user))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Username invalide");
    }

    @Test
    @Order(3)
    @DisplayName("Devrait rejeter un email invalide")
    void createUser_WithInvalidEmail_ShouldThrowException() {
        // Arrange
        User user = new User("john_doe", "invalid-email", "password123", User.Role.USER);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(user))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email invalide");
    }

    @Test
    @Order(4)
    @DisplayName("Devrait rejeter un mot de passe trop court")
    void createUser_WithShortPassword_ShouldThrowException() {
        // Arrange
        User user = new User("john_doe", "john@example.com", "12345", User.Role.USER);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(user))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Mot de passe invalide");
    }

    // ==================== TESTS READ ====================

    @Test
    @Order(5)
    @DisplayName("Devrait récupérer un utilisateur par ID")
    void getUserById_WithValidId_ShouldReturnUser() throws SQLException {
        // Arrange
        User user = new User("john_doe", "john@example.com", "password123", User.Role.USER);
        userService.createUser(user);

        // Act
        User foundUser = userService.getUserById(user.getId());

        // Assert
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("john_doe");
        assertThat(foundUser.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @Order(6)
    @DisplayName("Devrait retourner null pour un ID inexistant")
    void getUserById_WithInvalidId_ShouldReturnNull() throws SQLException {
        // Act
        User user = userService.getUserById(999);

        // Assert
        assertThat(user).isNull();
    }

    @Test
    @Order(7)
    @DisplayName("Devrait récupérer un utilisateur par username")
    void getUserByUsername_WithValidUsername_ShouldReturnUser() throws SQLException {
        // Arrange
        User user = new User("john_doe", "john@example.com", "password123", User.Role.USER);
        userService.createUser(user);

        // Act
        User foundUser = userService.getUserByUsername("john_doe");

        // Assert
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @Order(8)
    @DisplayName("Devrait récupérer tous les utilisateurs (ADMIN)")
    void getAllUsers_AsAdmin_ShouldReturnAllUsers() throws SQLException {
        // Arrange
        userService.createUser(new User("user1", "user1@example.com", "password123", User.Role.USER));
        userService.createUser(new User("user2", "user2@example.com", "password123", User.Role.USER));
        userService.createUser(new User("user3", "user3@example.com", "password123", User.Role.USER));

        // Act
        var users = userService.getAllUsers(User.Role.ADMIN);

        // Assert
        assertThat(users).hasSize(3);
    }

    @Test
    @Order(9)
    @DisplayName("Devrait refuser l'accès à un USER")
    void getAllUsers_AsUser_ShouldThrowSecurityException() {
        // Act & Assert
        assertThatThrownBy(() -> userService.getAllUsers(User.Role.USER))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("Accès refusé");
    }

    // ==================== TESTS UPDATE ====================

    @Test
    @Order(10)
    @DisplayName("USER devrait pouvoir modifier son propre profil")
    void updateUser_OwnProfile_ShouldReturnTrue() throws SQLException {
        // Arrange
        User user = new User("john_doe", "john@example.com", "password123", User.Role.USER);
        userService.createUser(user);
        
        user.setUsername("john_updated");
        user.setEmail("john_updated@example.com");

        // Act
        boolean result = userService.updateUser(user, user.getId(), User.Role.USER);

        // Assert
        assertThat(result).isTrue();
        User updatedUser = userService.getUserById(user.getId());
        assertThat(updatedUser.getUsername()).isEqualTo("john_updated");
    }

    @Test
    @Order(11)
    @DisplayName("USER ne devrait pas pouvoir modifier un autre profil")
    void updateUser_OtherProfile_ShouldThrowSecurityException() throws SQLException {
        // Arrange
        User user1 = new User("user1", "user1@example.com", "password123", User.Role.USER);
        User user2 = new User("user2", "user2@example.com", "password123", User.Role.USER);
        userService.createUser(user1);
        userService.createUser(user2);

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(user2, user1.getId(), User.Role.USER))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("propre profil");
    }

    @Test
    @Order(12)
    @DisplayName("ADMIN devrait pouvoir modifier n'importe quel profil")
    void updateUser_AsAdmin_ShouldReturnTrue() throws SQLException {
        // Arrange
        User user = new User("john_doe", "john@example.com", "password123", User.Role.USER);
        userService.createUser(user);
        
        user.setRole(User.Role.MODERATOR);

        // Act
        boolean result = userService.updateUser(user, 999, User.Role.ADMIN);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @Order(13)
    @DisplayName("MODERATOR ne devrait pas pouvoir modifier les profils")
    void updateUser_AsModerator_ShouldThrowSecurityException() throws SQLException {
        // Arrange
        User user = new User("john_doe", "john@example.com", "password123", User.Role.USER);
        userService.createUser(user);

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(user, 999, User.Role.MODERATOR))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("modérateurs ne peuvent pas modifier");
    }

    // ==================== TESTS DELETE ====================

    @Test
    @Order(14)
    @DisplayName("ADMIN devrait pouvoir supprimer un utilisateur")
    void deleteUser_AsAdmin_ShouldReturnTrue() throws SQLException {
        // Arrange
        User user = new User("john_doe", "john@example.com", "password123", User.Role.USER);
        userService.createUser(user);

        // Act
        boolean result = userService.deleteUser(user.getId(), User.Role.ADMIN);

        // Assert
        assertThat(result).isTrue();
        assertThat(userService.getUserById(user.getId())).isNull();
    }

    @Test
    @Order(15)
    @DisplayName("USER ne devrait pas pouvoir supprimer un utilisateur")
    void deleteUser_AsUser_ShouldThrowSecurityException() throws SQLException {
        // Arrange
        User user = new User("john_doe", "john@example.com", "password123", User.Role.USER);
        userService.createUser(user);

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser(user.getId(), User.Role.USER))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("administrateurs");
    }

    // ==================== TESTS BLOCK/UNBLOCK ====================

    @Test
    @Order(16)
    @DisplayName("MODERATOR devrait pouvoir bloquer un utilisateur")
    void blockUser_AsModerator_ShouldReturnTrue() throws SQLException {
        // Arrange
        User user = new User("john_doe", "john@example.com", "password123", User.Role.USER);
        userService.createUser(user);

        // Act
        boolean result = userService.blockUser(user.getId(), User.Role.MODERATOR);

        // Assert
        assertThat(result).isTrue();
        User blockedUser = userService.getUserById(user.getId());
        assertThat(blockedUser.isBlocked()).isTrue();
    }

    @Test
    @Order(17)
    @DisplayName("USER ne devrait pas pouvoir bloquer un utilisateur")
    void blockUser_AsUser_ShouldThrowSecurityException() throws SQLException {
        // Arrange
        User user = new User("john_doe", "john@example.com", "password123", User.Role.USER);
        userService.createUser(user);

        // Act & Assert
        assertThatThrownBy(() -> userService.blockUser(user.getId(), User.Role.USER))
            .isInstanceOf(SecurityException.class);
    }

    @Test
    @Order(18)
    @DisplayName("MODERATOR devrait pouvoir débloquer un utilisateur")
    void unblockUser_AsModerator_ShouldReturnTrue() throws SQLException {
        // Arrange
        User user = new User("john_doe", "john@example.com", "password123", User.Role.USER);
        userService.createUser(user);
        userService.blockUser(user.getId(), User.Role.MODERATOR);

        // Act
        boolean result = userService.unblockUser(user.getId(), User.Role.MODERATOR);

        // Assert
        assertThat(result).isTrue();
        User unblockedUser = userService.getUserById(user.getId());
        assertThat(unblockedUser.isBlocked()).isFalse();
    }

    // ==================== TESTS AUTHENTICATION ====================

    @Test
    @Order(19)
    @DisplayName("Devrait authentifier avec des credentials valides")
    void authenticate_WithValidCredentials_ShouldReturnUser() throws SQLException {
        // Arrange
        User user = new User("john_doe", "john@example.com", "password123", User.Role.USER);
        userService.createUser(user);

        // Act
        User authenticatedUser = userService.authenticate("john_doe", "password123");

        // Assert
        assertThat(authenticatedUser).isNotNull();
        assertThat(authenticatedUser.getUsername()).isEqualTo("john_doe");
    }

    @Test
    @Order(20)
    @DisplayName("Devrait rejeter des credentials invalides")
    void authenticate_WithInvalidCredentials_ShouldReturnNull() throws SQLException {
        // Arrange
        User user = new User("john_doe", "john@example.com", "password123", User.Role.USER);
        userService.createUser(user);

        // Act
        User authenticatedUser = userService.authenticate("john_doe", "wrongpassword");

        // Assert
        assertThat(authenticatedUser).isNull();
    }
}
