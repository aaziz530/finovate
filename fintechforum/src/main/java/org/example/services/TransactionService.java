package org.example.services;

import org.example.entities.Transaction;
import org.example.utils.Databaseconnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionService implements ITransactionService {

    private Connection connection;

    public TransactionService() {
        this.connection = Databaseconnection.getConnection();
    }

    @Override
    public void transferMoney(Long senderId, String numeroCarte, String cin, float amount, String description)
            throws SQLException {
        if (amount <= 0) {
            throw new SQLException("Amount must be positive");
        }

        try {
            connection.setAutoCommit(false);

            // 1. Get Receiver ID
            Long receiverId = null;
            String getReceiverSql = "SELECT id FROM users WHERE numero_carte = ? AND cin_number = ?";
            try (PreparedStatement pstReceiver = connection.prepareStatement(getReceiverSql)) {
                pstReceiver.setString(1, numeroCarte);
                pstReceiver.setString(2, cin);
                try (ResultSet rs = pstReceiver.executeQuery()) {
                    if (rs.next()) {
                        receiverId = rs.getLong("id");
                    } else {
                        throw new SQLException("Receiver not found with specified Card Number and CIN.");
                    }
                }
            }

            if (senderId.equals(receiverId)) {
                throw new SQLException("You cannot transfer money to yourself");
            }

            // 2. Check Sender Balance
            float senderBalance = getUserBalance(senderId);
            if (senderBalance < amount) {
                throw new SQLException("Insufficient funds. Current balance: " + senderBalance);
            }

            // 3. Subtract from Sender
            String subtractSql = "UPDATE users SET solde = solde - ? WHERE id = ?";
            try (PreparedStatement pstSubtract = connection.prepareStatement(subtractSql)) {
                pstSubtract.setFloat(1, amount);
                pstSubtract.setLong(2, senderId);
                pstSubtract.executeUpdate();
            }

            // 4. Add to Receiver
            String addSql = "UPDATE users SET solde = solde + ? WHERE id = ?";
            try (PreparedStatement pstAdd = connection.prepareStatement(addSql)) {
                pstAdd.setFloat(1, amount);
                pstAdd.setLong(2, receiverId);
                pstAdd.executeUpdate();
            }

            // 5. Log Transaction
            String logSql = "INSERT INTO transactions (sender_id, receiver_id, amount, type, description) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstLog = connection.prepareStatement(logSql)) {
                pstLog.setLong(1, senderId);
                pstLog.setLong(2, receiverId);
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
    public List<Transaction> getTransactionsByUserId(Long userId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, " +
                "s.firstname as sender_fname, s.lastname as sender_lname, " +
                "r.firstname as receiver_fname, r.lastname as receiver_lname " +
                "FROM transactions t " +
                "LEFT JOIN users s ON t.sender_id = s.id " +
                "LEFT JOIN users r ON t.receiver_id = r.id " +
                "WHERE (t.sender_id = ? OR t.receiver_id = ?) AND t.type = 'TRANSFER' " +
                "ORDER BY t.date DESC";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setLong(1, userId);
            pst.setLong(2, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    // Récupérer receiver_id en tant que Long puis convertir en Integer
                    Long receiverIdLong = rs.getObject("receiver_id", Long.class);
                    Integer receiverId = (receiverIdLong != null) ? receiverIdLong.intValue() : null;
                    
                    Transaction t = new Transaction(
                            rs.getInt("id"),
                            rs.getInt("sender_id"),
                            receiverId,
                            rs.getFloat("amount"),
                            rs.getString("type"),
                            rs.getString("description"),
                            rs.getTimestamp("date"));

                    String senderFname = rs.getString("sender_fname");
                    String senderLname = rs.getString("sender_lname");
                    if (senderFname != null && senderLname != null) {
                        t.setSenderName(senderFname + " " + senderLname);
                    } else {
                        t.setSenderName("Unknown");
                    }
                    
                    String receiverFname = rs.getString("receiver_fname");
                    String receiverLname = rs.getString("receiver_lname");
                    if (receiverFname != null && receiverLname != null) {
                        t.setReceiverName(receiverFname + " " + receiverLname);
                    } else {
                        t.setReceiverName("Unknown");
                    }
                    
                    transactions.add(t);
                }
            }
        }
        return transactions;
    }

    @Override
    public float getUserBalance(Long userId) throws SQLException {
        String sql = "SELECT solde FROM users WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setLong(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getFloat("solde");
                }
            }
        }
        throw new SQLException("User not found");
    }
}
