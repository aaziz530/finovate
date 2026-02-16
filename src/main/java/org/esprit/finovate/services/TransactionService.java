package org.esprit.finovate.services;

import org.esprit.finovate.entities.Transaction;
import org.esprit.finovate.utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionService implements ITransactionService {

    private Connection connection;

    public TransactionService() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void transferMoney(int senderId, String receiverEmail, float amount, String description)
            throws SQLException {
        if (amount <= 0) {
            throw new SQLException("Amount must be positive");
        }

        try {
            connection.setAutoCommit(false);

            // 1. Get Receiver ID
            int receiverId = -1;
            String getReceiverSql = "SELECT id FROM user WHERE email = ?";
            try (PreparedStatement pstReceiver = connection.prepareStatement(getReceiverSql)) {
                pstReceiver.setString(1, receiverEmail);
                try (ResultSet rs = pstReceiver.executeQuery()) {
                    if (rs.next()) {
                        receiverId = rs.getInt("id");
                    } else {
                        throw new SQLException("Receiver not found");
                    }
                }
            }

            if (senderId == receiverId) {
                throw new SQLException("You cannot transfer money to yourself");
            }

            // 2. Check Sender Balance
            float senderBalance = getUserBalance(senderId);
            if (senderBalance < amount) {
                throw new SQLException("Insufficient funds. Current balance: " + senderBalance);
            }

            // 3. Subtract from Sender
            String subtractSql = "UPDATE user SET solde = solde - ? WHERE id = ?";
            try (PreparedStatement pstSubtract = connection.prepareStatement(subtractSql)) {
                pstSubtract.setFloat(1, amount);
                pstSubtract.setInt(2, senderId);
                pstSubtract.executeUpdate();
            }

            // 4. Add to Receiver
            String addSql = "UPDATE user SET solde = solde + ? WHERE id = ?";
            try (PreparedStatement pstAdd = connection.prepareStatement(addSql)) {
                pstAdd.setFloat(1, amount);
                pstAdd.setInt(2, receiverId);
                pstAdd.executeUpdate();
            }

            // 5. Log Transaction
            String logSql = "INSERT INTO transaction (senderId, receiverId, amount, type, description) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstLog = connection.prepareStatement(logSql)) {
                pstLog.setInt(1, senderId);
                pstLog.setInt(2, receiverId);
                pstLog.setFloat(3, amount);
                pstLog.setString(4, "TRANSFER");
                pstLog.setString(5, description);
                pstLog.executeUpdate();
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
    public List<Transaction> getTransactionsByUserId(int userId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE senderId = ? OR receiverId = ? ORDER BY date DESC";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, userId);
            pst.setInt(2, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Transaction t = new Transaction(
                            rs.getInt("id"),
                            rs.getInt("senderId"),
                            (Integer) rs.getObject("receiverId"),
                            rs.getFloat("amount"),
                            rs.getString("type"),
                            rs.getString("description"),
                            rs.getTimestamp("date"));
                    transactions.add(t);
                }
            }
        }
        return transactions;
    }

    @Override
    public float getUserBalance(int userId) throws SQLException {
        String sql = "SELECT solde FROM user WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getFloat("solde");
                }
            }
        }
        throw new SQLException("User not found");
    }
}
