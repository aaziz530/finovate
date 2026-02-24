package org.example.services;

import org.example.entities.Goal;
import java.sql.SQLException;
import java.util.List;

public interface IGoalService {
    void addGoal(Goal goal) throws SQLException;

    List<Goal> getGoalsByUserId(Long userId) throws SQLException;

    void updateGoal(Goal goal) throws SQLException;

    void deleteGoal(int goalId) throws SQLException;

    void addFundsToGoal(Long userId, int goalId, float amount) throws SQLException;

    float getCurrentBalance(Long userId) throws SQLException;
}
