package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.esprit.finovate.entities.Goal;
import org.esprit.finovate.entities.Transaction;
import org.esprit.finovate.services.*;
import org.esprit.finovate.utils.Session;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class UserHomeController implements Initializable {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label balanceLabel;
    @FXML
    private Label pointsLabel;
    @FXML
    private Label goalsCountLabel;
    @FXML
    private ListView<String> recentTransactionsList;
    @FXML
    private Label featuredGoalTitle;
    @FXML
    private ProgressBar featuredGoalProgress;
    @FXML
    private Label featuredGoalAmount;
    @FXML
    private VBox featuredGoalContainer;

    private final ITransactionService transactionService = new TransactionService();
    private final IGoalService goalService = new GoalService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (Session.currentUser == null)
            return;

        welcomeLabel.setText("Welcome back, " + Session.currentUser.getFirstName() + "!");
        refreshDashboard();
    }

    private void refreshDashboard() {
        try {
            int userId = Session.currentUser.getId().intValue();

            // 1. Balance & Points
            float balance = transactionService.getUserBalance(userId);
            balanceLabel.setText(String.format("%.2f TND", balance));
            pointsLabel.setText(Session.currentUser.getPoints() + " pts");

            // 2. Goals
            List<Goal> goals = goalService.getGoalsByUserId(userId);
            goalsCountLabel.setText(String.valueOf(goals.size()));

            if (!goals.isEmpty()) {
                Goal topGoal = goals.get(0); // Simplistic: just take first
                featuredGoalTitle.setText(topGoal.getTitle());
                featuredGoalProgress.setProgress(topGoal.getProgress());
                featuredGoalAmount.setText(
                        String.format("%.2f / %.2f TND", topGoal.getCurrentAmount(), topGoal.getTargetAmount()));
            }

            // 3. Transactions
            List<Transaction> transactions = transactionService.getTransactionsByUserId(userId);
            recentTransactionsList.getItems().clear();
            for (int i = 0; i < Math.min(transactions.size(), 5); i++) {
                Transaction t = transactions.get(i);
                boolean isSent = t.getSenderId() == userId;
                String sign = isSent ? "-" : "+";
                String target = isSent ? "To: " + t.getReceiverName() : "From: " + t.getSenderName();
                recentTransactionsList.getItems().add(String.format("%s %s %.2f TND - %s",
                        target, sign, t.getAmount(), t.getDescription()));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
