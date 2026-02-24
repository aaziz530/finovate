package org.example.services;

import org.example.entities.Transaction;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

import java.sql.*;
import java.util.List;

/**
 * Tests unitaires pour TransactionService
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransactionServiceTest {

    private static Connection testConnection;
    private static TransactionService transactionService;
    private static final String TEST_DB_URL = "jdbc:h2:mem:transactiontest;DB_CLOSE_DELAY=-1;MODE=MySQL";

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        testConnection = DriverManager.getConnection(TEST_DB_URL, "sa", "");
        createTables();
        transactionService = new TransactionService();
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
        insertTestUsers();
    }

    private static void createTables() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) NOT NULL, " +
                "email VARCHAR(100) NOT NULL, " +
                "firstname VARCHAR(100), " +
                "lastname VARCHAR(100), " +
                "solde FLOAT DEFAULT 0, " +
                "numero_carte BIGINT, " +
                "cin_number VARCHAR(20))"
            );
            
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "sender_id INT NOT NULL, " +
                "receiver_id INT, " +
                "amount FLOAT NOT NULL, " +
                "type VARCHAR(50) NOT NULL, " +
                "description TEXT, " +
                "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );
        }
    }

    private void cleanDatabase() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM transactions");
            stmt.execute("DELETE FROM users");
        }
    }

    private void insertTestUsers() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("INSERT INTO users (id, username, email, firstname, lastname, solde, numero_carte, cin_number) " +
                "VALUES (1, 'user1', 'user1@example.com', 'John', 'Doe', 1000.0, 5123456789012345, '12345678')");
            stmt.execute("INSERT INTO users (id, username, email, firstname, lastname, solde, numero_carte, cin_number) " +
                "VALUES (2, 'user2', 'user2@example.com', 'Jane', 'Smith', 500.0, 5198765432109876, '87654321')");
        }
    }

    // ==================== TESTS TRANSFER MONEY ====================

    @Test
    @Order(1)
    @DisplayName("Devrait transférer de l'argent avec succès")
    void transferMoney_WithValidData_ShouldSucceed() throws SQLException {
        // Act
        transactionService.transferMoney(1, "5198765432109876", "87654321", 200.0f, "Test transfer");

        // Assert
        List<Transaction> transactions = transactionService.getTransactionsByUserId(1);
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getAmount()).isEqualTo(200.0f);
        
        // Vérifier les soldes
        assertThat(transactionService.getUserBalance(1)).isEqualTo(800.0f);
        assertThat(transactionService.getUserBalance(2)).isEqualTo(700.0f);
    }

    @Test
    @Order(2)
    @DisplayName("Devrait rejeter un transfert avec solde insuffisant")
    void transferMoney_WithInsufficientBalance_ShouldThrowException() {
        // Act & Assert
        assertThatThrownBy(() -> 
            transactionService.transferMoney(1, "5198765432109876", "87654321", 1500.0f, "Too much"))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("Insufficient");
    }

    @Test
    @Order(3)
    @DisplayName("Devrait rejeter un montant négatif")
    void transferMoney_WithNegativeAmount_ShouldThrowException() {
        // Act & Assert
        assertThatThrownBy(() -> 
            transactionService.transferMoney(1, "5198765432109876", "87654321", -100.0f, "Negative"))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("positive");
    }

    @Test
    @Order(4)
    @DisplayName("Devrait rejeter un transfert vers un destinataire inexistant")
    void transferMoney_WithInvalidReceiver_ShouldThrowException() {
        // Act & Assert
        assertThatThrownBy(() -> 
            transactionService.transferMoney(1, "9999999999999999", "99999999", 100.0f, "Invalid receiver"))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("not found");
    }

    @Test
    @Order(5)
    @DisplayName("Devrait rejeter un transfert avec CIN incorrect")
    void transferMoney_WithWrongCIN_ShouldThrowException() {
        // Act & Assert
        assertThatThrownBy(() -> 
            transactionService.transferMoney(1, "5198765432109876", "00000000", 100.0f, "Wrong CIN"))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("CIN");
    }

    // ==================== TESTS GET TRANSACTIONS ====================

    @Test
    @Order(6)
    @DisplayName("Devrait récupérer toutes les transactions d'un utilisateur")
    void getTransactionsByUserId_ShouldReturnAllTransactions() throws SQLException {
        // Arrange
        transactionService.transferMoney(1, "5198765432109876", "87654321", 100.0f, "Transfer 1");
        transactionService.transferMoney(1, "5198765432109876", "87654321", 50.0f, "Transfer 2");

        // Act
        List<Transaction> transactions = transactionService.getTransactionsByUserId(1);

        // Assert
        assertThat(transactions).hasSize(2);
    }

    @Test
    @Order(7)
    @DisplayName("Devrait retourner une liste vide pour un utilisateur sans transactions")
    void getTransactionsByUserId_WithNoTransactions_ShouldReturnEmptyList() throws SQLException {
        // Act
        List<Transaction> transactions = transactionService.getTransactionsByUserId(1);

        // Assert
        assertThat(transactions).isEmpty();
    }

    // ==================== TESTS GET BALANCE ====================

    @Test
    @Order(8)
    @DisplayName("Devrait récupérer le solde d'un utilisateur")
    void getUserBalance_ShouldReturnCorrectBalance() throws SQLException {
        // Act
        float balance = transactionService.getUserBalance(1);

        // Assert
        assertThat(balance).isEqualTo(1000.0f);
    }

    @Test
    @Order(9)
    @DisplayName("Devrait retourner 0 pour un utilisateur inexistant")
    void getUserBalance_WithInvalidUser_ShouldReturnZero() throws SQLException {
        // Act
        float balance = transactionService.getUserBalance(999);

        // Assert
        assertThat(balance).isEqualTo(0.0f);
    }

    // ==================== TESTS TRANSACTION INTEGRITY ====================

    @Test
    @Order(10)
    @DisplayName("Devrait effectuer un rollback en cas d'erreur")
    void transferMoney_OnError_ShouldRollback() throws SQLException {
        // Arrange
        float initialBalance1 = transactionService.getUserBalance(1);
        float initialBalance2 = transactionService.getUserBalance(2);

        // Act - Essayer un transfert invalide
        try {
            transactionService.transferMoney(1, "5198765432109876", "87654321", 2000.0f, "Too much");
        } catch (SQLException e) {
            // Exception attendue
        }

        // Assert - Les soldes ne devraient pas avoir changé
        assertThat(transactionService.getUserBalance(1)).isEqualTo(initialBalance1);
        assertThat(transactionService.getUserBalance(2)).isEqualTo(initialBalance2);
        
        // Aucune transaction ne devrait avoir été créée
        List<Transaction> transactions = transactionService.getTransactionsByUserId(1);
        assertThat(transactions).isEmpty();
    }
}
