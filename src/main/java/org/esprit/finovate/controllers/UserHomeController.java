package org.esprit.finovate.controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
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
    @FXML
    private Button aiAdviceButton;
    @FXML
    private VBox aiAdvicePopup;
    @FXML
    private Label aiAdviceLabel;

    private final ITransactionService transactionService = new TransactionService();
    private final IGoalService goalService = new GoalService();
    private final FinancialAdviceService financialAdviceService = new FinancialAdviceService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (Session.currentUser == null)
            return;

        welcomeLabel.setText("Welcome back, " + Session.currentUser.getFirstName() + "!");
        refreshDashboard();
    }

    private void refreshDashboard() {
        try {
            Long userId = Session.currentUser.getId();

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
                        String.format("%s / %s TND", topGoal.getCurrentAmount(), topGoal.getTargetAmount()));
            }

            // 3. Transactions
            List<Transaction> transactions = transactionService.getTransactionsByUserId(userId);
            recentTransactionsList.getItems().clear();
            for (int i = 0; i < Math.min(transactions.size(), 5); i++) {
                Transaction t = transactions.get(i);
                boolean isSent = t.getSenderId().equals(userId);
                String sign = isSent ? "-" : "+";
                String target = isSent ? "To: " + t.getReceiverName() : "From: " + t.getSenderName();
                recentTransactionsList.getItems().add(String.format("%s %s %.2f TND - %s",
                        target, sign, Float.parseFloat(t.getAmount()), t.getDescription()));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAiAdvice() {
        if (aiAdvicePopup.isVisible()) {
            closeAiPopup();
            return;
        }

        // Reset and show popup with loading state
        aiAdviceLabel.setText("🤖 Réflexion en cours...");
        aiAdviceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #718096; -fx-font-style: italic;");
        aiAdvicePopup.setVisible(true);
        aiAdvicePopup.setManaged(true);

        // Async task to generate advice
        Task<String> adviceTask = new Task<>() {
            @Override
            protected String call() {
                return financialAdviceService.generateAdvice();
            }
        };

        adviceTask.setOnSucceeded(e -> {
            String advice = adviceTask.getValue();
            aiAdviceLabel.setText(advice);
            aiAdviceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2d3748; -fx-font-style: normal;");
        });

        adviceTask.setOnFailed(e -> {
            aiAdviceLabel.setText("⚠️ Désolé, je n'ai pas pu générer de conseil pour le moment.");
            aiAdviceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e53e3e; -fx-font-style: normal;");
        });

        Thread thread = new Thread(adviceTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void closeAiPopup() {
        aiAdvicePopup.setVisible(false);
        aiAdvicePopup.setManaged(false);
    }
}
