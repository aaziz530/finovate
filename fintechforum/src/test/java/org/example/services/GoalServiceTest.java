package org.example.services;

import org.example.entities.Goal;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

import java.sql.*;
import java.util.Date;
import java.util.List;

/**
 * Tests unitaires pour GoalService
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GoalServiceTest {

    private static Connection testConnection;
    private static GoalService goalService;
    private static final String TEST_DB_URL = "jdbc:h2:mem:goaltest;DB_CLOSE_DELAY=-1;MODE=MySQL";

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        testConnection = DriverManager.getConnection(TEST_DB_URL, "sa", "");
        createTables();
        goalService = new GoalService();
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
        insertTestUser();
    }

    private static void createTables() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) NOT NULL, " +
                "email VARCHAR(100) NOT NULL, " +
                "solde FLOAT DEFAULT 0)"
            );
            
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS goals (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "id_user INT NOT NULL, " +
                "title VARCHAR(200) NOT NULL, " +
                "target_amount FLOAT NOT NULL, " +
                "current_amount FLOAT DEFAULT 0, " +
                "deadline DATE NOT NULL, " +
                "status VARCHAR(50) DEFAULT 'In Progress', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );
        }
    }

    private void cleanDatabase() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM goals");
            stmt.execute("DELETE FROM users");
        }
    }

    private void insertTestUser() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("INSERT INTO users (id, username, email, solde) VALUES (1, 'testuser', 'test@example.com', 1000.0)");
        }
    }

    // ==================== TESTS ADD GOAL ====================

    @Test
    @Order(1)
    @DisplayName("Devrait ajouter un objectif avec des données valides")
    void addGoal_WithValidData_ShouldSucceed() throws SQLException {
        // Arrange
        Date deadline = new Date(System.currentTimeMillis() + 86400000L * 30); // 30 jours
        Goal goal = new Goal(1, "Vacation Fund", 2000.0f, deadline);

        // Act
        goalService.addGoal(goal);

        // Assert
        List<Goal> goals = goalService.getGoalsByUserId(1);
        assertThat(goals).hasSize(1);
        assertThat(goals.get(0).getTitle()).isEqualTo("Vacation Fund");
        assertThat(goals.get(0).getTargetAmount()).isEqualTo(2000.0f);
        assertThat(goals.get(0).getCurrentAmount()).isEqualTo(0.0f);
    }

    @Test
    @Order(2)
    @DisplayName("Devrait rejeter un objectif avec un montant négatif")
    void addGoal_WithNegativeAmount_ShouldThrowException() {
        // Arrange
        Date deadline = new Date(System.currentTimeMillis() + 86400000L * 30);
        Goal goal = new Goal(1, "Invalid Goal", -500.0f, deadline);

        // Act & Assert
        assertThatThrownBy(() -> goalService.addGoal(goal))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("positive");
    }

    @Test
    @Order(3)
    @DisplayName("Devrait rejeter un objectif avec une date passée")
    void addGoal_WithPastDeadline_ShouldThrowException() {
        // Arrange
        Date pastDate = new Date(System.currentTimeMillis() - 86400000L); // Hier
        Goal goal = new Goal(1, "Past Goal", 1000.0f, pastDate);

        // Act & Assert
        assertThatThrownBy(() -> goalService.addGoal(goal))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("future");
    }

    // ==================== TESTS GET GOALS ====================

    @Test
    @Order(4)
    @DisplayName("Devrait récupérer tous les objectifs d'un utilisateur")
    void getGoalsByUserId_ShouldReturnAllGoals() throws SQLException {
        // Arrange
        Date deadline = new Date(System.currentTimeMillis() + 86400000L * 30);
        goalService.addGoal(new Goal(1, "Goal 1", 1000.0f, deadline));
        goalService.addGoal(new Goal(1, "Goal 2", 2000.0f, deadline));
        goalService.addGoal(new Goal(1, "Goal 3", 3000.0f, deadline));

        // Act
        List<Goal> goals = goalService.getGoalsByUserId(1);

        // Assert
        assertThat(goals).hasSize(3);
        assertThat(goals).extracting(Goal::getTitle)
            .containsExactlyInAnyOrder("Goal 1", "Goal 2", "Goal 3");
    }

    @Test
    @Order(5)
    @DisplayName("Devrait retourner une liste vide pour un utilisateur sans objectifs")
    void getGoalsByUserId_WithNoGoals_ShouldReturnEmptyList() throws SQLException {
        // Act
        List<Goal> goals = goalService.getGoalsByUserId(1);

        // Assert
        assertThat(goals).isEmpty();
    }

    // ==================== TESTS UPDATE GOAL ====================

    @Test
    @Order(6)
    @DisplayName("Devrait mettre à jour un objectif")
    void updateGoal_WithValidData_ShouldSucceed() throws SQLException {
        // Arrange
        Date deadline = new Date(System.currentTimeMillis() + 86400000L * 30);
        Goal goal = new Goal(1, "Original Title", 1000.0f, deadline);
        goalService.addGoal(goal);
        
        List<Goal> goals = goalService.getGoalsByUserId(1);
        Goal savedGoal = goals.get(0);
        savedGoal.setTitle("Updated Title");
        savedGoal.setTargetAmount(1500.0f);

        // Act
        goalService.updateGoal(savedGoal);

        // Assert
        goals = goalService.getGoalsByUserId(1);
        assertThat(goals.get(0).getTitle()).isEqualTo("Updated Title");
        assertThat(goals.get(0).getTargetAmount()).isEqualTo(1500.0f);
    }

    // ==================== TESTS DELETE GOAL ====================

    @Test
    @Order(7)
    @DisplayName("Devrait supprimer un objectif")
    void deleteGoal_WithValidId_ShouldSucceed() throws SQLException {
        // Arrange
        Date deadline = new Date(System.currentTimeMillis() + 86400000L * 30);
        Goal goal = new Goal(1, "To Delete", 1000.0f, deadline);
        goalService.addGoal(goal);
        
        List<Goal> goals = goalService.getGoalsByUserId(1);
        int goalId = goals.get(0).getId();

        // Act
        goalService.deleteGoal(goalId);

        // Assert
        goals = goalService.getGoalsByUserId(1);
        assertThat(goals).isEmpty();
    }

    // ==================== TESTS ADD FUNDS ====================

    @Test
    @Order(8)
    @DisplayName("Devrait ajouter des fonds à un objectif")
    void addFundsToGoal_WithSufficientBalance_ShouldSucceed() throws SQLException {
        // Arrange
        Date deadline = new Date(System.currentTimeMillis() + 86400000L * 30);
        Goal goal = new Goal(1, "Savings", 1000.0f, deadline);
        goalService.addGoal(goal);
        
        List<Goal> goals = goalService.getGoalsByUserId(1);
        int goalId = goals.get(0).getId();

        // Act
        goalService.addFundsToGoal(1, goalId, 300.0f);

        // Assert
        goals = goalService.getGoalsByUserId(1);
        assertThat(goals.get(0).getCurrentAmount()).isEqualTo(300.0f);
        assertThat(goalService.getCurrentBalance(1)).isEqualTo(700.0f);
    }

    @Test
    @Order(9)
    @DisplayName("Devrait rejeter l'ajout de fonds avec solde insuffisant")
    void addFundsToGoal_WithInsufficientBalance_ShouldThrowException() throws SQLException {
        // Arrange
        Date deadline = new Date(System.currentTimeMillis() + 86400000L * 30);
        Goal goal = new Goal(1, "Savings", 2000.0f, deadline);
        goalService.addGoal(goal);
        
        List<Goal> goals = goalService.getGoalsByUserId(1);
        int goalId = goals.get(0).getId();

        // Act & Assert
        assertThatThrownBy(() -> goalService.addFundsToGoal(1, goalId, 1500.0f))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("Insufficient");
    }

    @Test
    @Order(10)
    @DisplayName("Devrait marquer l'objectif comme atteint quand le montant cible est atteint")
    void addFundsToGoal_WhenTargetReached_ShouldMarkAsCompleted() throws SQLException {
        // Arrange
        Date deadline = new Date(System.currentTimeMillis() + 86400000L * 30);
        Goal goal = new Goal(1, "Savings", 500.0f, deadline);
        goalService.addGoal(goal);
        
        List<Goal> goals = goalService.getGoalsByUserId(1);
        int goalId = goals.get(0).getId();

        // Act
        goalService.addFundsToGoal(1, goalId, 500.0f);

        // Assert
        goals = goalService.getGoalsByUserId(1);
        assertThat(goals.get(0).getCurrentAmount()).isEqualTo(500.0f);
        assertThat(goals.get(0).getStatus()).isEqualTo("Completed");
    }

    // ==================== TESTS GET BALANCE ====================

    @Test
    @Order(11)
    @DisplayName("Devrait récupérer le solde actuel d'un utilisateur")
    void getCurrentBalance_ShouldReturnCorrectBalance() throws SQLException {
        // Act
        float balance = goalService.getCurrentBalance(1);

        // Assert
        assertThat(balance).isEqualTo(1000.0f);
    }

    @Test
    @Order(12)
    @DisplayName("Devrait retourner 0 pour un utilisateur inexistant")
    void getCurrentBalance_WithInvalidUser_ShouldReturnZero() throws SQLException {
        // Act
        float balance = goalService.getCurrentBalance(999);

        // Assert
        assertThat(balance).isEqualTo(0.0f);
    }
}
