package org.esprit.finovate.services;

import org.esprit.finovate.entities.Transaction;
import org.esprit.finovate.utils.MyDataBase;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionService implements ITransactionService {

    private Connection connection;

    public TransactionService() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    @Override
    public float getDailyTransferTotal(Long userId) throws SQLException {
        String sql = "SELECT SUM(CAST(amount AS DECIMAL(12,2))) as total FROM transaction " +
                     "WHERE sender_id = ? AND type = 'TRANSFER' " +
                     "AND DATE(date) = CURDATE()";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setLong(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getFloat("total");
                }
            }
        }
        return 0;
    }

    @Override
    public void transferMoney(Long senderId, Long numeroCarte, String cin, float amount, String description)
            throws SQLException {
        if (amount <= 0) {
            throw new SQLException("Amount must be positive");
        }

        // Daily limit check (3000 TND)
        float dailyTotal = getDailyTransferTotal(senderId);
        if (dailyTotal + amount > 3000) {
            throw new SQLException(String.format(
                "Limite quotidienne dépassée. Vous avez déjà transféré %.3f TND aujourd'hui. " +
                "Le maximum autorisé est de 3000 TND par jour.", dailyTotal));
        }

        // Data for SMS notification
        String receiverFirstName = null;
        String receiverLastName = null;
        int receiverPhone = 0;
        String senderFirstName = null;
        String senderLastName = null;

        try {
            connection.setAutoCommit(false);

            // 1. Get Receiver ID, name, and phone number
            Long receiverId = -1L;
            String getReceiverSql = "SELECT id, firstname, lastname, phone_number FROM user WHERE numero_carte = ? AND cin = ?";
            try (PreparedStatement pstReceiver = connection.prepareStatement(getReceiverSql)) {
                pstReceiver.setLong(1, numeroCarte);
                pstReceiver.setString(2, cin);
                try (ResultSet rs = pstReceiver.executeQuery()) {
                    if (rs.next()) {
                        receiverId = rs.getLong("id");
                        receiverFirstName = rs.getString("firstname");
                        receiverLastName = rs.getString("lastname");
                        receiverPhone = rs.getInt("phone_number");
                    } else {
                        throw new SQLException("Receiver not found with specified Card Number and CIN.");
                    }
                }
            }

            if (senderId == receiverId) {
                throw new SQLException("You cannot transfer money to yourself");
            }

            // 1b. Get Sender name
            String getSenderSql = "SELECT firstname, lastname FROM user WHERE id = ?";
            try (PreparedStatement pstSender = connection.prepareStatement(getSenderSql)) {
                pstSender.setLong(1, senderId);
                try (ResultSet rs = pstSender.executeQuery()) {
                    if (rs.next()) {
                        senderFirstName = rs.getString("firstname");
                        senderLastName = rs.getString("lastname");
                    }
                }
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
                pstSubtract.setLong(2, senderId);
                pstSubtract.executeUpdate();
            }

            // 4. Add to Receiver
            String addSql = "UPDATE user SET solde = solde + ? WHERE id = ?";
            try (PreparedStatement pstAdd = connection.prepareStatement(addSql)) {
                pstAdd.setFloat(1, amount);
                pstAdd.setLong(2, receiverId);
                pstAdd.executeUpdate();
            }

            // 5. Log Transaction
            String logSql = "INSERT INTO transaction (sender_id, receiver_id, amount, type, description, date) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstLog = connection.prepareStatement(logSql)) {
                pstLog.setLong(1, senderId);
                pstLog.setLong(2, receiverId);
                pstLog.setString(3, String.valueOf(amount));
                pstLog.setString(4, "TRANSFER");
                pstLog.setString(5, description);
                pstLog.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));
                pstLog.executeUpdate();
            }

            connection.commit();

            // 6. Send SMS notification to receiver (after successful commit)
            if (receiverPhone > 0 && receiverFirstName != null && senderFirstName != null) {
                String receiverName = receiverFirstName + " " + receiverLastName;
                String senderName = senderFirstName + " " + senderLastName;
                TwilioSmsService.getInstance().sendTransferNotification(
                        receiverName,
                        receiverPhone,
                        amount,
                        senderName,
                        LocalDateTime.now()
                );
            }

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
                "FROM transaction t " +
                "LEFT JOIN user s ON t.sender_id = s.id " +
                "LEFT JOIN user r ON t.receiver_id = r.id " +
                "WHERE (t.sender_id = ? OR t.receiver_id = ?) AND t.type = 'TRANSFER' " +
                "ORDER BY t.date DESC";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setLong(1, userId);
            pst.setLong(2, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Transaction t = new Transaction(
                            rs.getLong("id"),
                            rs.getLong("sender_id"),
                            rs.getLong("receiver_id"),
                            rs.getString("amount"),
                            rs.getString("type"),
                            rs.getString("description"),
                            rs.getTimestamp("date"));

                    t.setSenderName(rs.getString("sender_fname") + " " + rs.getString("sender_lname"));
                    if (rs.getString("receiver_fname") != null) {
                        t.setReceiverName(rs.getString("receiver_fname") + " " + rs.getString("receiver_lname"));
                    }
                    transactions.add(t);
                }
            }
        }
        return transactions;
    }

    @Override
    public float getUserBalance(Long userId) throws SQLException {
        String sql = "SELECT solde FROM user WHERE id = ?";
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

    @Override
    public void logTopUp(Long userId, float amount) throws SQLException {
        String sql = "INSERT INTO transaction (sender_id, receiver_id, amount, type, description, date) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setLong(1, userId);
            pst.setNull(2, java.sql.Types.INTEGER);
            pst.setString(3, String.valueOf(amount));
            pst.setString(4, "TOPUP");
            pst.setString(5, "Alimentation carte via Stripe");
            pst.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));
            pst.executeUpdate();
        }
    }
}
