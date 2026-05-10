package org.esprit.finovate.services;

import org.esprit.finovate.entities.Goal;
import org.esprit.finovate.utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GoalService implements IGoalService {

    private Connection connection;

    public GoalService() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void addGoal(Goal goal) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        // Générer l'ID manuellement (la colonne id n'est pas en AUTO_INCREMENT)
        long newId = 1;
        String maxIdSql = "SELECT MAX(id) FROM goal";
        try (PreparedStatement pstMax = connection.prepareStatement(maxIdSql);
             ResultSet rs = pstMax.executeQuery()) {
            if (rs.next() && rs.getLong(1) > 0) {
                newId = rs.getLong(1) + 1;
            }
        }

        String sql = "INSERT INTO goal (id, id_user, title, target_amount, current_amount, deadline, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setLong(1, newId);
            pst.setLong(2, goal.getIdUser());
            pst.setString(3, goal.getTitle());
            pst.setString(4, goal.getTargetAmount());
            pst.setString(5, goal.getCurrentAmount());
            pst.setDate(6, new java.sql.Date(goal.getDeadline().getTime()));
            pst.setString(7, goal.getStatus());
            pst.setDate(8, new java.sql.Date(goal.getCreatedAt().getTime()));
            pst.executeUpdate();

            goal.setId(newId);
        }
    }

    @Override
    public List<Goal> getGoalsByUserId(Long userId) throws SQLException {
        List<Goal> goals = new ArrayList<>();
        String sql = "SELECT * FROM goal WHERE id_user = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setLong(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Goal goal = new Goal(
                            rs.getLong("id"),
                            rs.getLong("id_user"),
                            rs.getString("title"),
                            rs.getString("target_amount"),
                            rs.getString("current_amount"),
                            rs.getDate("deadline"),
                            rs.getString("status"),
                            rs.getDate("created_at"));
                    goals.add(goal);
                }
            }
        }
        return goals;
    }

    @Override
    public void updateGoal(Goal goal) throws SQLException {
        String sql = "UPDATE goal SET title = ?, target_amount = ?, deadline = ?, status = ? WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, goal.getTitle());
            pst.setString(2, goal.getTargetAmount());
            pst.setDate(3, new java.sql.Date(goal.getDeadline().getTime()));
            pst.setString(4, goal.getStatus());
            pst.setLong(5, goal.getId());
            pst.executeUpdate();
        }
    }

    @Override
    public void deleteGoal(Long goalId) throws SQLException {
        try {
            connection.setAutoCommit(false);

            Long userId;
            float goalAmount;
            String selectSql = "SELECT id_user, current_amount FROM goal WHERE id = ? FOR UPDATE";
            try (PreparedStatement pst = connection.prepareStatement(selectSql)) {
                pst.setLong(1, goalId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Goal not found with ID: " + goalId);
                    }
                    userId = rs.getLong("id_user");
                    goalAmount = Float.parseFloat(rs.getString("current_amount").isEmpty() ? "0" : rs.getString("current_amount"));
                }
            }

            if (goalAmount > 0) {
                String refundSql = "UPDATE user SET solde = solde + ? WHERE id = ?";
                try (PreparedStatement pstRefund = connection.prepareStatement(refundSql)) {
                    pstRefund.setFloat(1, goalAmount);
                    pstRefund.setLong(2, userId);
                    int updated = pstRefund.executeUpdate();
                    if (updated == 0) {
                        throw new SQLException("User not found for refund. User ID: " + userId);
                    }
                }
            }

            String deleteSql = "DELETE FROM goal WHERE id = ?";
            try (PreparedStatement pstDelete = connection.prepareStatement(deleteSql)) {
                pstDelete.setLong(1, goalId);
                int deleted = pstDelete.executeUpdate();
                if (deleted == 0) {
                    throw new SQLException("Delete failed. Goal not found with ID: " + goalId);
                }
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                e.addSuppressed(ex);
            }
            throw e;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void addFundsToGoal(Long userId, Long goalId, float amount) throws SQLException {
        // This operation requires a transaction
        // 1. Check user balance
        // 2. Check goal exists and not completed
        // 3. Subtract from user balance
        // 4. Add to goal balance
        // 5. Update goal status if target reached

        try {
            connection.setAutoCommit(false);

            // Check balance
            float currentBalance = getCurrentBalance(userId);
            if (currentBalance < amount) {
                throw new SQLException("Insufficient funds. Current balance: " + currentBalance);
            }

            // Update User Balance
            String updateUserSql = "UPDATE user SET solde = solde - ? WHERE id = ?";
            try (PreparedStatement pstUser = connection.prepareStatement(updateUserSql)) {
                pstUser.setFloat(1, amount);
                pstUser.setLong(2, userId);
                int updated = pstUser.executeUpdate();
                if (updated == 0)
                    throw new SQLException("User not found or update failed");
            }

            // Update Goal Amount
            // Get current amount and add new amount
            String getCurrentSql = "SELECT current_amount FROM goal WHERE id = ?";
            float newAmount = 0;
            try (PreparedStatement pstGetCurrent = connection.prepareStatement(getCurrentSql)) {
                pstGetCurrent.setLong(1, goalId);
                try (ResultSet rs = pstGetCurrent.executeQuery()) {
                    if (rs.next()) {
                        float current = Float.parseFloat(rs.getString("current_amount").isEmpty() ? "0" : rs.getString("current_amount"));
                        newAmount = current + amount;
                    }
                }
            }
            
            String updateGoalSql = "UPDATE goal SET current_amount = ? WHERE id = ?";
            try (PreparedStatement pstGoal = connection.prepareStatement(updateGoalSql)) {
                pstGoal.setString(1, String.valueOf(newAmount));
                pstGoal.setLong(2, goalId);
                int updated = pstGoal.executeUpdate();
                if (updated == 0)
                    throw new SQLException("Goal not found or update failed");
            }

            // Check if goal reached (This could be optimized but let's fetch first)
            String checkGoalSql = "SELECT current_amount, target_amount FROM goal WHERE id = ?";
            try (PreparedStatement pstCheck = connection.prepareStatement(checkGoalSql)) {
                pstCheck.setLong(1, goalId);
                try (ResultSet rs = pstCheck.executeQuery()) {
                    if (rs.next()) {
                        float current = Float.parseFloat(rs.getString("current_amount").isEmpty() ? "0" : rs.getString("current_amount"));
                        float target = Float.parseFloat(rs.getString("target_amount").isEmpty() ? "0" : rs.getString("target_amount"));
                        if (current >= target) {
                            String markDoneSql = "UPDATE goal SET status = 'Achieved' WHERE id = ?";
                            try (PreparedStatement pstDone = connection.prepareStatement(markDoneSql)) {
                                pstDone.setLong(1, goalId);
                                pstDone.executeUpdate();
                            }
                        }
                    }
                }
            }

            connection.commit();

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                e.addSuppressed(ex);
            }
            throw e;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public float getCurrentBalance(Long userId) throws SQLException {
        String sql = "SELECT solde FROM user WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setLong(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getFloat("solde");
                }
            }
        }
        throw new SQLException("User not found with ID: " + userId);
    }
}
