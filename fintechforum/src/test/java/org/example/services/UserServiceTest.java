package org.example.services;

import org.example.entities.User;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

import java.sql.*;
import java.util.Date;
import java.util.List;

/**
 * Tests unitaires pour UserService
 * Couvre toutes les fonctionnalités CRUD et de sécurité
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {

    private static Connection testConnection;
    private static UserService userService;
    private static final String TEST_DB_URL = "jdbc:h2:mem:usertest;DB_CLOSE_DELAY=-1;MODE=MySQL";
    private static final String TEST_DB_USER = "sa";
    private static final String TEST_DB_PASSWORD = "";
    
    private Integer createdUserId; // Pour cleanup automatique

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        testConnection = DriverManager.getConnection(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD);
        createTables();
        userService = new UserService();
        System.out.println("[TEST] Base de données H2 initialisée");
    }

    @AfterAll
    static void tearDownDatabase() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
            System.out.println("[TEST] Base de données H2 fermée");
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        cleanDatabase();
        createdUserId = null;
    }
    
    @AfterEach
    void cleanUp() {
        if (createdUserId != null) {
            try {
                userService.deleteUser(createdUserId, User.Role.ADMIN);
                System.out.println("[CLEANUP] Utilisateur supprimé: ID " + createdUserId);
            } catch (SQLException e) {
                System.out.println("[CLEANUP] Avertissement: " + e.getMessage());
            }
            createdUserId = null;
        }
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
                "firstname VARCHAR(100), " +
                "lastname VARCHAR(100), " +
                "birthdate DATE, " +
                "points INT DEFAULT 0, " +
                "solde FLOAT DEFAULT 0, " +
                "numero_carte BIGINT, " +
                "cin_number VARCHAR(20), " +
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
        String uniqueUsername = "user_" + System.currentTimeMillis();
        String uniqueEmail = uniqueUsername + "@example.com";
        User user = new User(uniqueUsername, uniqueEmail, "password123", User.Role.USER);
        user.setFirstname("John");
        user.setLastname("Doe");
        user.setBirthdate(new Date());

        // Act
        boolean result = userService.createUser(user);

        // Assert
        assertThat(result).isTrue();
        assertThat(user.getId()).isGreaterThan(0);
        this.createdUserId = user.getId();
        
        System.out.println("[TEST] Utilisateur créé avec ID: " + createdUserId);
    }

    @Test
    @Order(2)
    @DisplayName("Devrait rejeter un username invalide")
    void createUser_WithInvalidUsername_ShouldThrowException() {
        // Arrange
        String uniqueEmail = "test_" + System.currentTimeMillis() + "@example.com";
        User user = new User("ab", uniqueEmail, "password123", User.Role.USER);

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
        String uniqueUsername = "user_" + System.currentTimeMillis();
        User user = new User(uniqueUsername, "invalid-email", "password123", User.Role.USER);

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
        String uniqueUsername = "user_" + System.currentTimeMillis();
        String uniqueEmail = uniqueUsername + "@example.com";
        User user = new User(uniqueUsername, uniqueEmail, "12345", User.Role.USER);

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
        String uniqueUsername = "user_" + System.currentTimeMillis();
        String uniqueEmail = uniqueUsername + "@example.com";
        User user = new User(uniqueUsername, uniqueEmail, "password123", User.Role.USER);
        userService.createUser(user);
        this.createdUserId = user.getId();

        // Act
        User foundUser = userService.getUserById(user.getId());

        // Assert
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo(uniqueUsername);
        assertThat(foundUser.getEmail()).isEqualTo(uniqueEmail);
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
        String uniqueUsername = "user_" + System.currentTimeMillis();
        String uniqueEmail = uniqueUsername + "@example.com";
        User user = new User(uniqueUsername, uniqueEmail, "password123", User.Role.USER);
        userService.createUser(user);
        this.createdUserId = user.getId();

        // Act
        User foundUser = userService.getUserByUsername(uniqueUsername);

        // Assert
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo(uniqueEmail);
    }

    @Test
    @Order(8)
    @DisplayName("Devrait récupérer tous les utilisateurs (ADMIN)")
    void getAllUsers_AsAdmin_ShouldReturnAllUsers() throws SQLException {
        // Arrange - Créer plusieurs utilisateurs avec des noms uniques
        long timestamp = System.currentTimeMillis();
        User user1 = new User("user1_" + timestamp, "user1_" + timestamp + "@example.com", "password123", User.Role.USER);
        User user2 = new User("user2_" + timestamp, "user2_" + timestamp + "@example.com", "password123", User.Role.USER);
        User user3 = new User("user3_" + timestamp, "user3_" + timestamp + "@example.com", "password123", User.Role.USER);
        
        userService.createUser(user1);
        userService.createUser(user2);
        userService.createUser(user3);
        this.createdUserId = user3.getId(); // Cleanup du dernier seulement

        // Act
        var users = userService.getAllUsers(User.Role.ADMIN);

        // Assert
        assertThat(users).hasSizeGreaterThanOrEqualTo(3);
        
        // Cleanup manuel des 2 premiers
        userService.deleteUser(user1.getId(), User.Role.ADMIN);
        userService.deleteUser(user2.getId(), User.Role.ADMIN);
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
        String uniqueUsername = "user_" + System.currentTimeMillis();
        String uniqueEmail = uniqueUsername + "@example.com";
        User user = new User(uniqueUsername, uniqueEmail, "password123", User.Role.USER);
        userService.createUser(user);
        this.createdUserId = user.getId();
        
        String newUsername = "updated_" + System.currentTimeMillis();
        String newEmail = newUsername + "@example.com";
        user.setUsername(newUsername);
        user.setEmail(newEmail);
        user.setFirstname("UpdatedFirst");
        user.setLastname("UpdatedLast");

        // Act
        boolean result = userService.updateUser(user, user.getId(), User.Role.USER);

        // Assert
        assertThat(result).isTrue();
        User updatedUser = userService.getUserById(user.getId());
        assertThat(updatedUser.getUsername()).isEqualTo(newUsername);
        assertThat(updatedUser.getFirstname()).isEqualTo("UpdatedFirst");
        assertThat(updatedUser.getLastname()).isEqualTo("UpdatedLast");
        
        System.out.println("[TEST] Utilisateur mis à jour: " + updatedUser.getUsername());
    }

    @Test
    @Order(11)
    @DisplayName("USER ne devrait pas pouvoir modifier un autre profil")
    void updateUser_OtherProfile_ShouldThrowSecurityException() throws SQLException {
        // Arrange
        long timestamp = System.currentTimeMillis();
        User user1 = new User("user1_" + timestamp, "user1_" + timestamp + "@example.com", "password123", User.Role.USER);
        User user2 = new User("user2_" + timestamp, "user2_" + timestamp + "@example.com", "password123", User.Role.USER);
        userService.createUser(user1);
        userService.createUser(user2);
        this.createdUserId = user2.getId();

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(user2, user1.getId(), User.Role.USER))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("propre profil");
            
        // Cleanup manuel du premier
        userService.deleteUser(user1.getId(), User.Role.ADMIN);
    }

    @Test
    @Order(12)
    @DisplayName("ADMIN devrait pouvoir modifier n'importe quel profil")
    void updateUser_AsAdmin_ShouldReturnTrue() throws SQLException {
        // Arrange
        String uniqueUsername = "user_" + System.currentTimeMillis();
        String uniqueEmail = uniqueUsername + "@example.com";
        User user = new User(uniqueUsername, uniqueEmail, "password123", User.Role.USER);
        userService.createUser(user);
        this.createdUserId = user.getId();
        
        user.setRole(User.Role.MODERATOR);

        // Act
        boolean result = userService.updateUser(user, 999, User.Role.ADMIN);

        // Assert
        assertThat(result).isTrue();
        User updatedUser = userService.getUserById(user.getId());
        assertThat(updatedUser.getRole()).isEqualTo(User.Role.MODERATOR);
    }

    @Test
    @Order(13)
    @DisplayName("MODERATOR ne devrait pas pouvoir modifier les profils")
    void updateUser_AsModerator_ShouldThrowSecurityException() throws SQLException {
        // Arrange
        String uniqueUsername = "user_" + System.currentTimeMillis();
        String uniqueEmail = uniqueUsername + "@example.com";
        User user = new User(uniqueUsername, uniqueEmail, "password123", User.Role.USER);
        userService.createUser(user);
        this.createdUserId = user.getId();

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
        String uniqueUsername = "user_" + System.currentTimeMillis();
        String uniqueEmail = uniqueUsername + "@example.com";
        User user = new User(uniqueUsername, uniqueEmail, "password123", User.Role.USER);
        userService.createUser(user);
        int userId = user.getId();

        // Act
        boolean result = userService.deleteUser(userId, User.Role.ADMIN);

        // Assert
        assertThat(result).isTrue();
        assertThat(userService.getUserById(userId)).isNull();
        
        // Pas besoin de cleanup car déjà supprimé
        this.createdUserId = null;
    }

    @Test
    @Order(15)
    @DisplayName("USER ne devrait pas pouvoir supprimer un utilisateur")
    void deleteUser_AsUser_ShouldThrowSecurityException() throws SQLException {
        // Arrange
        String uniqueUsername = "user_" + System.currentTimeMillis();
        String uniqueEmail = uniqueUsername + "@example.com";
        User user = new User(uniqueUsername, uniqueEmail, "password123", User.Role.USER);
        userService.createUser(user);
        this.createdUserId = user.getId();

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
        String uniqueUsername = "user_" + System.currentTimeMillis();
        String uniqueEmail = uniqueUsername + "@example.com";
        User user = new User(uniqueUsername, uniqueEmail, "password123", User.Role.USER);
        userService.createUser(user);
        this.createdUserId = user.getId();

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
        String uniqueUsername = "user_" + System.currentTimeMillis();
        String uniqueEmail = uniqueUsername + "@example.com";
        User user = new User(uniqueUsername, uniqueEmail, "password123", User.Role.USER);
        userService.createUser(user);
        this.createdUserId = user.getId();

        // Act & Assert
        assertThatThrownBy(() -> userService.blockUser(user.getId(), User.Role.USER))
            .isInstanceOf(SecurityException.class);
    }

    @Test
    @Order(18)
    @DisplayName("MODERATOR devrait pouvoir débloquer un utilisateur")
    void unblockUser_AsModerator_ShouldReturnTrue() throws SQLException {
        // Arrange
        String uniqueUsername = "user_" + System.currentTimeMillis();
        String uniqueEmail = uniqueUsername + "@example.com";
        User user = new User(uniqueUsername, uniqueEmail, "password123", User.Role.USER);
        userService.createUser(user);
        this.createdUserId = user.getId();
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
        String uniqueUsername = "user_" + System.currentTimeMillis();
        String uniqueEmail = uniqueUsername + "@example.com";
        User user = new User(uniqueUsername, uniqueEmail, "password123", User.Role.USER);
        userService.createUser(user);
        this.createdUserId = user.getId();

        // Act
        User authenticatedUser = userService.authenticate(uniqueUsername, "password123");

        // Assert
        assertThat(authenticatedUser).isNotNull();
        assertThat(authenticatedUser.getUsername()).isEqualTo(uniqueUsername);
    }

    @Test
    @Order(20)
    @DisplayName("Devrait rejeter des credentials invalides")
    void authenticate_WithInvalidCredentials_ShouldReturnNull() throws SQLException {
        // Arrange
        String uniqueUsername = "user_" + System.currentTimeMillis();
        String uniqueEmail = uniqueUsername + "@example.com";
        User user = new User(uniqueUsername, uniqueEmail, "password123", User.Role.USER);
        userService.createUser(user);
        this.createdUserId = user.getId();

        // Act
        User authenticatedUser = userService.authenticate(uniqueUsername, "wrongpassword");

        // Assert
        assertThat(authenticatedUser).isNull();
    }

    @Test
    @Order(21)
    @DisplayName("Devrait authentifier avec login() - méthode alternative")
    void login_WithValidCredentials_ShouldReturnUser() throws SQLException {
        // Arrange
        String uniqueUsername = "user_" + System.currentTimeMillis();
        String uniqueEmail = uniqueUsername + "@example.com";
        User user = new User(uniqueUsername, uniqueEmail, "password123", User.Role.USER);
        userService.createUser(user);
        this.createdUserId = user.getId();

        // Act - Test avec username
        User loggedInUser1 = userService.login(uniqueUsername, "password123");
        // Act - Test avec email
        User loggedInUser2 = userService.login(uniqueEmail, "password123");

        // Assert
        assertThat(loggedInUser1).isNotNull();
        assertThat(loggedInUser1.getUsername()).isEqualTo(uniqueUsername);
        assertThat(loggedInUser2).isNotNull();
        assertThat(loggedInUser2.getEmail()).isEqualTo(uniqueEmail);
        
        System.out.println("[TEST] Login réussi avec username et email");
    }
    
    @Test
    @Order(22)
    @DisplayName("Login devrait rejeter un utilisateur bloqué")
    void login_WithBlockedUser_ShouldThrowException() throws SQLException {
        // Arrange
        String uniqueUsername = "user_" + System.currentTimeMillis();
        String uniqueEmail = uniqueUsername + "@example.com";
        User user = new User(uniqueUsername, uniqueEmail, "password123", User.Role.USER);
        userService.createUser(user);
        this.createdUserId = user.getId();
        userService.blockUser(user.getId(), User.Role.ADMIN);

        // Act & Assert
        assertThatThrownBy(() -> userService.login(uniqueUsername, "password123"))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("blocked");
    }
}
