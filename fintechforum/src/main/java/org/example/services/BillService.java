package org.example.services;

import org.example.entities.Bill;
import org.example.utils.Databaseconnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillService implements IBillService {

    private Connection connection;

    public BillService() {
        this.connection = Databaseconnection.getConnection();
    }

    @Override
    public void payBill(Long userId, String reference, double amount) throws SQLException {
        if (amount <= 0) {
            throw new SQLException("Amount must be positive");
        }

        try {
            connection.setAutoCommit(false);

            // 1. Check User Balance
            float currentBalance = 0;
            String checkBalanceSql = "SELECT solde FROM users WHERE id = ?";
            try (PreparedStatement pstCheck = connection.prepareStatement(checkBalanceSql)) {
                pstCheck.setLong(1, userId);
                try (ResultSet rs = pstCheck.executeQuery()) {
                    if (rs.next()) {
                        currentBalance = rs.getFloat("solde");
                    } else {
                        throw new SQLException("User not found");
                    }
                }
            }

            if (currentBalance < amount) {
                throw new SQLException("Insufficient funds. Current balance: " + currentBalance);
            }

            // 2. Update User Balance
            String updateBalanceSql = "UPDATE users SET solde = solde - ? WHERE id = ?";
            try (PreparedStatement pstUpdate = connection.prepareStatement(updateBalanceSql)) {
                pstUpdate.setDouble(1, amount);
                pstUpdate.setLong(2, userId);
                pstUpdate.executeUpdate();
            }

            // 3. Log Bill Payment
            String logBillSql = "INSERT INTO bills (id_user, reference, amount, date_paiement) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstBill = connection.prepareStatement(logBillSql)) {
                pstBill.setLong(1, userId);
                pstBill.setString(2, reference);
                pstBill.setDouble(3, amount);
                pstBill.setDate(4, new java.sql.Date(System.currentTimeMillis()));
                pstBill.executeUpdate();
            }

            connection.commit();

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    @Override
    public List<Bill> getBillsByUserId(Long userId) throws SQLException {
        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT * FROM bills WHERE id_user = ? ORDER BY date_paiement DESC";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setLong(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Bill bill = new Bill(
                            rs.getInt("id"),
                            rs.getInt("id_user"),
                            rs.getString("reference"),
                            rs.getDouble("amount"),
                            rs.getDate("date_paiement"));
                    bills.add(bill);
                }
            }
        }
        return bills;
    }
}
