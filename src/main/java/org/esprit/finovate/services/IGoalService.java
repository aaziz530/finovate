package org.esprit.finovate.services;

import org.esprit.finovate.entities.Goal;
import java.sql.SQLException;
import java.util.List;

public interface IGoalService {
    void addGoal(Goal goal) throws SQLException;

    List<Goal> getGoalsByUserId(int userId) throws SQLException;

    void updateGoal(Goal goal) throws SQLException;

    void deleteGoal(int goalId) throws SQLException;

    void addFundsToGoal(int userId, int goalId, float amount) throws SQLException;

    float getCurrentBalance(int userId) throws SQLException;
}
