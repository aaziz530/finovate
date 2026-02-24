package org.example.services;

import org.example.entities.Bill;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

import java.sql.*;
import java.util.List;

/**
 * Tests unitaires pour BillService
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BillServiceTest {

    private static Connection testConnection;
    private static BillService billService;
    private static final String TEST_DB_URL = "jdbc:h2:mem:billtest;DB_CLOSE_DELAY=-1;MODE=MySQL";

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        testConnection = DriverManager.getConnection(TEST_DB_URL, "sa", "");
        createTables();
        billService = new BillService();
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
                "CREATE TABLE IF NOT EXISTS bills (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "id_user INT NOT NULL, " +
                "reference VARCHAR(100) NOT NULL, " +
                "amount DOUBLE NOT NULL, " +
                "date_paiement DATE NOT NULL)"
            );
        }
    }

    private void cleanDatabase() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM bills");
            stmt.execute("DELETE FROM users");
        }
    }

    private void insertTestUser() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("INSERT INTO users (id, username, email, solde) VALUES (1, 'testuser', 'test@example.com', 1000.0)");
        }
    }

    // ==================== TESTS PAY BILL ====================

    @Test
    @Order(1)
    @DisplayName("Devrait payer une facture avec un solde suffisant")
    void payBill_WithSufficientBalance_ShouldSucceed() throws SQLException {
        // Act
        billService.payBill(1, "ELEC-2024-001", 150.0);

        // Assert
        List<Bill> bills = billService.getBillsByUserId(1);
        assertThat(bills).hasSize(1);
        assertThat(bills.get(0).getReference()).isEqualTo("ELEC-2024-001");
        assertThat(bills.get(0).getAmount()).isEqualTo(150.0);
        
        // Vérifier que le solde a été débité
        try (Statement stmt = testConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT solde FROM users WHERE id = 1")) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getFloat("solde")).isEqualTo(850.0f);
        }
    }

    @Test
    @Order(2)
    @DisplayName("Devrait rejeter un paiement avec un solde insuffisant")
    void payBill_WithInsufficientBalance_ShouldThrowException() {
        // Act & Assert
        assertThatThrownBy(() -> billService.payBill(1, "ELEC-2024-001", 1500.0))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("Insufficient funds");
    }

    @Test
    @Order(3)
    @DisplayName("Devrait rejeter un montant négatif")
    void payBill_WithNegativeAmount_ShouldThrowException() {
        // Act & Assert
        assertThatThrownBy(() -> billService.payBill(1, "ELEC-2024-001", -50.0))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("must be positive");
    }

    @Test
    @Order(4)
    @DisplayName("Devrait rejeter un montant zéro")
    void payBill_WithZeroAmount_ShouldThrowException() {
        // Act & Assert
        assertThatThrownBy(() -> billService.payBill(1, "ELEC-2024-001", 0.0))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("must be positive");
    }

    @Test
    @Order(5)
    @DisplayName("Devrait rejeter pour un utilisateur inexistant")
    void payBill_WithNonExistentUser_ShouldThrowException() {
        // Act & Assert
        assertThatThrownBy(() -> billService.payBill(999, "ELEC-2024-001", 50.0))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("User not found");
    }

    // ==================== TESTS GET BILLS ====================

    @Test
    @Order(6)
    @DisplayName("Devrait récupérer toutes les factures d'un utilisateur")
    void getBillsByUserId_ShouldReturnAllBills() throws SQLException {
        // Arrange
        billService.payBill(1, "ELEC-2024-001", 100.0);
        billService.payBill(1, "WATER-2024-001", 50.0);
        billService.payBill(1, "GAS-2024-001", 75.0);

        // Act
        List<Bill> bills = billService.getBillsByUserId(1);

        // Assert
        assertThat(bills).hasSize(3);
        assertThat(bills).extracting(Bill::getReference)
            .containsExactlyInAnyOrder("ELEC-2024-001", "WATER-2024-001", "GAS-2024-001");
    }

    @Test
    @Order(7)
    @DisplayName("Devrait retourner une liste vide pour un utilisateur sans factures")
    void getBillsByUserId_WithNoBills_ShouldReturnEmptyList() throws SQLException {
        // Act
        List<Bill> bills = billService.getBillsByUserId(1);

        // Assert
        assertThat(bills).isEmpty();
    }

    @Test
    @Order(8)
    @DisplayName("Devrait retourner les factures triées par date décroissante")
    void getBillsByUserId_ShouldReturnBillsSortedByDateDesc() throws SQLException, InterruptedException {
        // Arrange
        billService.payBill(1, "BILL-001", 100.0);
        Thread.sleep(10); // Petit délai pour garantir des timestamps différents
        billService.payBill(1, "BILL-002", 50.0);
        Thread.sleep(10);
        billService.payBill(1, "BILL-003", 75.0);

        // Act
        List<Bill> bills = billService.getBillsByUserId(1);

        // Assert
        assertThat(bills).hasSize(3);
        // La plus récente devrait être en premier
        assertThat(bills.get(0).getReference()).isEqualTo("BILL-003");
    }

    // ==================== TESTS TRANSACTION ====================

    @Test
    @Order(9)
    @DisplayName("Devrait effectuer un rollback en cas d'erreur")
    void payBill_OnError_ShouldRollback() throws SQLException {
        // Arrange
        float initialBalance;
        try (Statement stmt = testConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT solde FROM users WHERE id = 1")) {
            rs.next();
            initialBalance = rs.getFloat("solde");
        }

        // Act - Essayer de payer plus que le solde disponible
        try {
            billService.payBill(1, "ELEC-2024-001", 2000.0);
        } catch (SQLException e) {
            // Exception attendue
        }

        // Assert - Le solde ne devrait pas avoir changé
        try (Statement stmt = testConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT solde FROM users WHERE id = 1")) {
            rs.next();
            assertThat(rs.getFloat("solde")).isEqualTo(initialBalance);
        }
        
        // Aucune facture ne devrait avoir été créée
        List<Bill> bills = billService.getBillsByUserId(1);
        assertThat(bills).isEmpty();
    }
}
