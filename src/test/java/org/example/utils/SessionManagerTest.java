package org.example.utils;

import org.example.entities.User;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitaires pour SessionManager
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SessionManagerTest {

    @BeforeEach
    void setUp() {
        // S'assurer qu'aucun utilisateur n'est connecté avant chaque test
        SessionManager.logout();
    }

    // ==================== TESTS LOGIN/LOGOUT ====================

    @Test
    @Order(1)
    @DisplayName("Devrait connecter un utilisateur")
    void login_WithUser_ShouldSetCurrentUser() {
        // Arrange
        User user = new User(1, "john_doe", "john@example.com", "password123", 
                           User.Role.USER, false, null, null);

        // Act
        SessionManager.login(user);

        // Assert
        assertThat(SessionManager.isLoggedIn()).isTrue();
        assertThat(SessionManager.getCurrentUser()).isEqualTo(user);
    }

    @Test
    @Order(2)
    @DisplayName("Devrait déconnecter un utilisateur")
    void logout_ShouldClearCurrentUser() {
        // Arrange
        User user = new User(1, "john_doe", "john@example.com", "password123", 
                           User.Role.USER, false, null, null);
        SessionManager.login(user);

        // Act
        SessionManager.logout();

        // Assert
        assertThat(SessionManager.isLoggedIn()).isFalse();
        assertThat(SessionManager.getCurrentUser()).isNull();
    }

    // ==================== TESTS GETTERS ====================

    @Test
    @Order(3)
    @DisplayName("Devrait récupérer l'utilisateur courant")
    void getCurrentUser_WhenLoggedIn_ShouldReturnUser() {
        // Arrange
        User user = new User(1, "john_doe", "john@example.com", "password123", 
                           User.Role.USER, false, null, null);
        SessionManager.login(user);

        // Act
        User currentUser = SessionManager.getCurrentUser();

        // Assert
        assertThat(currentUser).isNotNull();
        assertThat(currentUser.getUsername()).isEqualTo("john_doe");
    }

    @Test
    @Order(4)
    @DisplayName("Devrait retourner null si aucun utilisateur connecté")
    void getCurrentUser_WhenNotLoggedIn_ShouldReturnNull() {
        // Act
        User currentUser = SessionManager.getCurrentUser();

        // Assert
        assertThat(currentUser).isNull();
    }

    @Test
    @Order(5)
    @DisplayName("Devrait récupérer l'ID de l'utilisateur courant")
    void getCurrentUserId_WhenLoggedIn_ShouldReturnId() {
        // Arrange
        User user = new User(42, "john_doe", "john@example.com", "password123", 
                           User.Role.USER, false, null, null);
        SessionManager.login(user);

        // Act
        int userId = SessionManager.getCurrentUserId();

        // Assert
        assertThat(userId).isEqualTo(42);
    }

    @Test
    @Order(6)
    @DisplayName("Devrait retourner -1 si aucun utilisateur connecté")
    void getCurrentUserId_WhenNotLoggedIn_ShouldReturnMinusOne() {
        // Act
        int userId = SessionManager.getCurrentUserId();

        // Assert
        assertThat(userId).isEqualTo(-1);
    }

    @Test
    @Order(7)
    @DisplayName("Devrait récupérer le rôle de l'utilisateur courant")
    void getCurrentUserRole_WhenLoggedIn_ShouldReturnRole() {
        // Arrange
        User user = new User(1, "admin", "admin@example.com", "password123", 
                           User.Role.ADMIN, false, null, null);
        SessionManager.login(user);

        // Act
        User.Role role = SessionManager.getCurrentUserRole();

        // Assert
        assertThat(role).isEqualTo(User.Role.ADMIN);
    }

    @Test
    @Order(8)
    @DisplayName("Devrait retourner null si aucun utilisateur connecté")
    void getCurrentUserRole_WhenNotLoggedIn_ShouldReturnNull() {
        // Act
        User.Role role = SessionManager.getCurrentUserRole();

        // Assert
        assertThat(role).isNull();
    }

    // ==================== TESTS ROLE CHECKS ====================

    @Test
    @Order(9)
    @DisplayName("Devrait vérifier si l'utilisateur est ADMIN")
    void isAdmin_WithAdminUser_ShouldReturnTrue() {
        // Arrange
        User admin = new User(1, "admin", "admin@example.com", "password123", 
                            User.Role.ADMIN, false, null, null);
        SessionManager.login(admin);

        // Act & Assert
        assertThat(SessionManager.isAdmin()).isTrue();
        assertThat(SessionManager.isModerator()).isFalse();
        assertThat(SessionManager.isUser()).isFalse();
    }

    @Test
    @Order(10)
    @DisplayName("Devrait vérifier si l'utilisateur est MODERATOR")
    void isModerator_WithModeratorUser_ShouldReturnTrue() {
        // Arrange
        User moderator = new User(1, "moderator", "mod@example.com", "password123", 
                                User.Role.MODERATOR, false, null, null);
        SessionManager.login(moderator);

        // Act & Assert
        assertThat(SessionManager.isModerator()).isTrue();
        assertThat(SessionManager.isAdmin()).isFalse();
        assertThat(SessionManager.isUser()).isFalse();
    }

    @Test
    @Order(11)
    @DisplayName("Devrait vérifier si l'utilisateur est USER")
    void isUser_WithRegularUser_ShouldReturnTrue() {
        // Arrange
        User user = new User(1, "user", "user@example.com", "password123", 
                           User.Role.USER, false, null, null);
        SessionManager.login(user);

        // Act & Assert
        assertThat(SessionManager.isUser()).isTrue();
        assertThat(SessionManager.isAdmin()).isFalse();
        assertThat(SessionManager.isModerator()).isFalse();
    }

    @Test
    @Order(12)
    @DisplayName("Devrait retourner false pour tous les rôles si non connecté")
    void roleChecks_WhenNotLoggedIn_ShouldReturnFalse() {
        // Act & Assert
        assertThat(SessionManager.isAdmin()).isFalse();
        assertThat(SessionManager.isModerator()).isFalse();
        assertThat(SessionManager.isUser()).isFalse();
    }

    // ==================== TESTS BLOCKED STATUS ====================

    @Test
    @Order(13)
    @DisplayName("Devrait vérifier si l'utilisateur est bloqué")
    void isBlocked_WithBlockedUser_ShouldReturnTrue() {
        // Arrange
        User blockedUser = new User(1, "blocked", "blocked@example.com", "password123", 
                                   User.Role.USER, true, null, null);
        SessionManager.login(blockedUser);

        // Act & Assert
        assertThat(SessionManager.isBlocked()).isTrue();
    }

    @Test
    @Order(14)
    @DisplayName("Devrait retourner false si l'utilisateur n'est pas bloqué")
    void isBlocked_WithNormalUser_ShouldReturnFalse() {
        // Arrange
        User user = new User(1, "user", "user@example.com", "password123", 
                           User.Role.USER, false, null, null);
        SessionManager.login(user);

        // Act & Assert
        assertThat(SessionManager.isBlocked()).isFalse();
    }

    @Test
    @Order(15)
    @DisplayName("Devrait retourner false si aucun utilisateur connecté")
    void isBlocked_WhenNotLoggedIn_ShouldReturnFalse() {
        // Act & Assert
        assertThat(SessionManager.isBlocked()).isFalse();
    }

    // ==================== TESTS LOGGED IN STATUS ====================

    @Test
    @Order(16)
    @DisplayName("Devrait retourner true si un utilisateur est connecté")
    void isLoggedIn_WithUser_ShouldReturnTrue() {
        // Arrange
        User user = new User(1, "user", "user@example.com", "password123", 
                           User.Role.USER, false, null, null);
        SessionManager.login(user);

        // Act & Assert
        assertThat(SessionManager.isLoggedIn()).isTrue();
    }

    @Test
    @Order(17)
    @DisplayName("Devrait retourner false si aucun utilisateur connecté")
    void isLoggedIn_WithoutUser_ShouldReturnFalse() {
        // Act & Assert
        assertThat(SessionManager.isLoggedIn()).isFalse();
    }
}
