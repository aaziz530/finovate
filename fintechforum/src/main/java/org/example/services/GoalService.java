package org.example.services;

import org.example.entities.Goal;
import org.example.utils.Databaseconnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GoalService implements IGoalService {

    private Connection connection;

    public GoalService() {
        this.connection = Databaseconnection.getConnection();
    }

    @Override
    public void addGoal(Goal goal) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        String sql = "INSERT INTO goals (id_user, title, target_amount, current_amount, deadline, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
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
    public List<Goal> getGoalsByUserId(Long userId) throws SQLException {
        List<Goal> goals = new ArrayList<>();
        String sql = "SELECT * FROM goals WHERE id_user = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setLong(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Goal goal = new Goal(
                            rs.getInt("id"),
                            rs.getInt("id_user"),
                            rs.getString("title"),
                            rs.getFloat("target_amount"),
                            rs.getFloat("current_amount"),
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
        String sql = "UPDATE goals SET title = ?, target_amount = ?, deadline = ?, status = ? WHERE id = ?";
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
        String sql = "DELETE FROM goals WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, goalId);
            pst.executeUpdate();
        }
    }

    @Override
    public void addFundsToGoal(Long userId, int goalId, float amount) throws SQLException {
        try {
            connection.setAutoCommit(false);

            // Check balance
            float currentBalance = getCurrentBalance(userId);
            if (currentBalance < amount) {
                throw new SQLException("Insufficient funds. Current balance: " + currentBalance);
            }

            // Update User Balance
            String updateUserSql = "UPDATE users SET solde = solde - ? WHERE id = ?";
            try (PreparedStatement pstUser = connection.prepareStatement(updateUserSql)) {
                pstUser.setFloat(1, amount);
                pstUser.setLong(2, userId);
                int updated = pstUser.executeUpdate();
                if (updated == 0)
                    throw new SQLException("User not found or update failed");
            }

            // Update Goal Amount
            String updateGoalSql = "UPDATE goals SET current_amount = current_amount + ? WHERE id = ?";
            try (PreparedStatement pstGoal = connection.prepareStatement(updateGoalSql)) {
                pstGoal.setFloat(1, amount);
                pstGoal.setInt(2, goalId);
                int updated = pstGoal.executeUpdate();
                if (updated == 0)
                    throw new SQLException("Goal not found or update failed");
            }

            // Check if goal reached
            String checkGoalSql = "SELECT current_amount, target_amount FROM goals WHERE id = ?";
            try (PreparedStatement pstCheck = connection.prepareStatement(checkGoalSql)) {
                pstCheck.setInt(1, goalId);
                try (ResultSet rs = pstCheck.executeQuery()) {
                    if (rs.next()) {
                        float current = rs.getFloat("current_amount");
                        float target = rs.getFloat("target_amount");
                        if (current >= target) {
                            String markDoneSql = "UPDATE goals SET status = 'Achieved' WHERE id = ?";
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
    public float getCurrentBalance(Long userId) throws SQLException {
        String sql = "SELECT solde FROM users WHERE id = ?";
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
