package org.esprit.finovate.services;

import org.esprit.finovate.entities.Bill;
import org.esprit.finovate.utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillService implements IBillService {

    private Connection connection;

    public BillService() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void payBill(int userId, String reference, double amount) throws SQLException {
        if (amount <= 0) {
            throw new SQLException("Amount must be positive");
        }

        try {
            connection.setAutoCommit(false);

            // 1. Check User Balance
            float currentBalance = 0;
            String checkBalanceSql = "SELECT solde FROM user WHERE id = ?";
            try (PreparedStatement pstCheck = connection.prepareStatement(checkBalanceSql)) {
                pstCheck.setInt(1, userId);
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
            String updateBalanceSql = "UPDATE user SET solde = solde - ? WHERE id = ?";
            try (PreparedStatement pstUpdate = connection.prepareStatement(updateBalanceSql)) {
                pstUpdate.setDouble(1, amount);
                pstUpdate.setInt(2, userId);
                pstUpdate.executeUpdate();
            }

            // 3. Log Bill Payment
            String logBillSql = "INSERT INTO bill (idUser, reference, amount, datePaiement) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstBill = connection.prepareStatement(logBillSql)) {
                pstBill.setInt(1, userId);
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
    public List<Bill> getBillsByUserId(int userId) throws SQLException {
        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT * FROM bill WHERE idUser = ? ORDER BY datePaiement DESC";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Bill bill = new Bill(
                            rs.getInt("id"),
                            rs.getInt("idUser"),
                            rs.getString("reference"),
                            rs.getDouble("amount"),
                            rs.getDate("datePaiement"));
                    bills.add(bill);
                }
            }
        }
        return bills;
    }
}
