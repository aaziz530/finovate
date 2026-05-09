package org.esprit.finovate.services;

import org.esprit.finovate.entities.Goal;
import java.sql.SQLException;
import java.util.List;

public interface IGoalService {
    void addGoal(Goal goal) throws SQLException;

    List<Goal> getGoalsByUserId(Long userId) throws SQLException;

    void updateGoal(Goal goal) throws SQLException;

    void deleteGoal(Long goalId) throws SQLException;

    void addFundsToGoal(Long userId, Long goalId, float amount) throws SQLException;

    float getCurrentBalance(Long userId) throws SQLException;
}
