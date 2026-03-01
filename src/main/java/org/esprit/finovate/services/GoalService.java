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

        String sql = "INSERT INTO goal (idUser, title, targetAmount, currentAmount, deadline, status, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, goal.getIdUser());
            pst.setString(2, goal.getTitle());
            pst.setFloat(3, goal.getTargetAmount());
            pst.setFloat(4, goal.getCurrentAmount());
            pst.setDate(5, new java.sql.Date(goal.getDeadline().getTime()));
            pst.setString(6, goal.getStatus());
            pst.setDate(7, new java.sql.Date(goal.getCreatedAt().getTime()));
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    goal.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public List<Goal> getGoalsByUserId(int userId) throws SQLException {
        List<Goal> goals = new ArrayList<>();
        String sql = "SELECT * FROM goal WHERE idUser = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Goal goal = new Goal(
                            rs.getInt("id"),
                            rs.getInt("idUser"),
                            rs.getString("title"),
                            rs.getFloat("targetAmount"),
                            rs.getFloat("currentAmount"),
                            rs.getDate("deadline"),
                            rs.getString("status"),
                            rs.getDate("createdAt"));
                    goals.add(goal);
                }
            }
        }
        return goals;
    }

    @Override
    public void updateGoal(Goal goal) throws SQLException {
        String sql = "UPDATE goal SET title = ?, targetAmount = ?, deadline = ?, status = ? WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, goal.getTitle());
            pst.setFloat(2, goal.getTargetAmount());
            pst.setDate(3, new java.sql.Date(goal.getDeadline().getTime()));
            pst.setString(4, goal.getStatus());
            pst.setInt(5, goal.getId());
            pst.executeUpdate();
        }
    }

    @Override
    public void deleteGoal(int goalId) throws SQLException {
        try {
            connection.setAutoCommit(false);

            int userId;
            float goalAmount;
            String selectSql = "SELECT idUser, currentAmount FROM goal WHERE id = ? FOR UPDATE";
            try (PreparedStatement pst = connection.prepareStatement(selectSql)) {
                pst.setInt(1, goalId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Goal not found with ID: " + goalId);
                    }
                    userId = rs.getInt("idUser");
                    goalAmount = rs.getFloat("currentAmount");
                }
            }

            if (goalAmount > 0) {
                String refundSql = "UPDATE user SET solde = solde + ? WHERE id = ?";
                try (PreparedStatement pstRefund = connection.prepareStatement(refundSql)) {
                    pstRefund.setFloat(1, goalAmount);
                    pstRefund.setInt(2, userId);
                    int updated = pstRefund.executeUpdate();
                    if (updated == 0) {
                        throw new SQLException("User not found for refund. User ID: " + userId);
                    }
                }
            }

            String deleteSql = "DELETE FROM goal WHERE id = ?";
            try (PreparedStatement pstDelete = connection.prepareStatement(deleteSql)) {
                pstDelete.setInt(1, goalId);
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
    public void addFundsToGoal(int userId, int goalId, float amount) throws SQLException {
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
                pstUser.setInt(2, userId);
                int updated = pstUser.executeUpdate();
                if (updated == 0)
                    throw new SQLException("User not found or update failed");
            }

            // Update Goal Amount
            String updateGoalSql = "UPDATE goal SET currentAmount = currentAmount + ? WHERE id = ?";
            try (PreparedStatement pstGoal = connection.prepareStatement(updateGoalSql)) {
                pstGoal.setFloat(1, amount);
                pstGoal.setInt(2, goalId);
                int updated = pstGoal.executeUpdate();
                if (updated == 0)
                    throw new SQLException("Goal not found or update failed");
            }

            // Check if goal reached (This could be optimized but let's fetch first)
            String checkGoalSql = "SELECT currentAmount, targetAmount FROM goal WHERE id = ?";
            try (PreparedStatement pstCheck = connection.prepareStatement(checkGoalSql)) {
                pstCheck.setInt(1, goalId);
                try (ResultSet rs = pstCheck.executeQuery()) {
                    if (rs.next()) {
                        float current = rs.getFloat("currentAmount");
                        float target = rs.getFloat("targetAmount");
                        if (current >= target) {
                            String markDoneSql = "UPDATE goal SET status = 'Achieved' WHERE id = ?";
                            try (PreparedStatement pstDone = connection.prepareStatement(markDoneSql)) {
                                pstDone.setInt(1, goalId);
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
    public float getCurrentBalance(int userId) throws SQLException {
        String sql = "SELECT solde FROM user WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getFloat("solde");
                }
            }
        }
        throw new SQLException("User not found with ID: " + userId);
    }
}
