package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.esprit.finovate.entities.Goal;
import org.esprit.finovate.services.GoalService;
import org.esprit.finovate.services.IGoalService;
import org.esprit.finovate.utils.Session;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class GoalsController implements Initializable {

    @FXML
    private FlowPane goalsContainer;
    @FXML
    private Label balanceLabel;
    @FXML
    private Label activeGoalsLabel;
    @FXML
    private Button addGoalButton;

    private final IGoalService goalService;

    public GoalsController() {
        this.goalService = new GoalService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (Session.currentUser == null) {
            System.err.println("No user logged in. Redirecting...");
            return;
        }
        refreshDashboard();
    }

    private void refreshDashboard() {
        try {
            // Update Balance
            float balance = goalService.getCurrentBalance(Session.currentUser.getId().intValue());
            balanceLabel.setText(String.format("%.2f TND", balance));

            // Get Goals
            List<Goal> goals = goalService.getGoalsByUserId(Session.currentUser.getId().intValue());
            activeGoalsLabel.setText(String.valueOf(goals.size()));

            // Populate Container
            goalsContainer.getChildren().clear();
            for (Goal goal : goals) {
                goalsContainer.getChildren().add(createGoalCard(goal));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Failed to load data: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private VBox createGoalCard(Goal goal) {
        VBox card = new VBox(15);
        card.setPrefWidth(280);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);");

        // Header
        Label title = new Label(goal.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #32325d;");

        Label status = new Label(goal.getStatus());
        status.setStyle("-fx-font-size: 10px; -fx-text-fill: white; -fx-background-color: "
                + getStatusColor(goal.getStatus()) + "; -fx-background-radius: 10; -fx-padding: 3 8 3 8;");

        HBox header = new HBox(title, status);
        header.setSpacing(10);

        // Progress
        double progress = goal.getProgress();
        ProgressBar pb = new ProgressBar(progress);
        pb.setPrefWidth(240);
        pb.setStyle("-fx-accent: #237f4e;");

        Label progressText = new Label(String.format("%.0f%%", progress * 100));
        progressText.setStyle("-fx-font-size: 12px; -fx-text-fill: #8898aa;");

        HBox progressInfo = new HBox(pb);

        // Amounts
        Label amounts = new Label(String.format("%.2f / %.2f TND", goal.getCurrentAmount(), goal.getTargetAmount()));
        amounts.setStyle("-fx-font-weight: bold; -fx-text-fill: #525f7f;");

        // Deadline
        Label deadline = new Label(
                "Deadline: " + (goal.getDeadline() != null ? goal.getDeadline().toString() : "None"));
        deadline.setStyle("-fx-font-size: 11px; -fx-text-fill: #8898aa;");

        // Actions
        Button addFundsBtn = new Button("+ Add Funds");
        addFundsBtn.setMaxWidth(Double.MAX_VALUE);
        addFundsBtn.setStyle(
                "-fx-background-color: #f0fdf4; -fx-text-fill: #237f4e; -fx-background-radius: 5; -fx-cursor: hand;");
        addFundsBtn.setOnAction(e -> handleAddFunds(goal));

        Button editBtn = new Button("Edit");
        editBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #237f4e; -fx-cursor: hand; -fx-font-size: 11px;");
        editBtn.setOnAction(e -> handleEditGoal(goal));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #dc3545; -fx-cursor: hand; -fx-font-size: 11px;");
        deleteBtn.setOnAction(e -> handleDeleteGoal(goal));

        HBox actions = new HBox(addFundsBtn);
        HBox bottomActions = new HBox(10, editBtn, deleteBtn);
        bottomActions.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(header, amounts, progressInfo, progressText, deadline, actions, bottomActions);
        return card;
    }

    private String getStatusColor(String status) {
        if ("Achieved".equalsIgnoreCase(status))
            return "#2dce89"; // Green
        if ("Failed".equalsIgnoreCase(status))
            return "#f5365c"; // Red
        return "#11cdef"; // Blue
    }

    @FXML
    private void handleAddGoal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GoalDialog.fxml"));
            Parent root = loader.load();

            GoalDialogController controller = loader.getController();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("New Goal");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isSaveClicked() && controller.getGoal() != null) {
                Goal newGoal = controller.getGoal();
                newGoal.setIdUser(Session.currentUser.getId().intValue());
                goalService.addGoal(newGoal);
                refreshDashboard();
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error creating goal: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void handleAddFunds(Goal goal) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Funds");
        dialog.setHeaderText("Add funds to '" + goal.getTitle() + "'");
        dialog.setContentText("Enter amount to transfer (TND):");

        dialog.showAndWait().ifPresent(amountStr -> {
            try {
                float amount = Float.parseFloat(amountStr);
                if (amount <= 0) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("Amount must be positive.");
                    alert.showAndWait();
                    return;
                }

                goalService.addFundsToGoal(Session.currentUser.getId().intValue(), goal.getId(), amount);
                refreshDashboard();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setContentText("Funds added successfully!");
                alert.showAndWait();

            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Invalid amount format.");
                alert.showAndWait();
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Transfer failed: " + e.getMessage());
                alert.showAndWait();
            }
        });
    }

    private void handleEditGoal(Goal goal) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GoalDialog.fxml"));
            Parent root = loader.load();

            GoalDialogController controller = loader.getController();
            controller.setGoal(goal);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Goal");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isSaveClicked() && controller.getGoal() != null) {
                goalService.updateGoal(controller.getGoal());
                refreshDashboard();
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error updating goal: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void handleDeleteGoal(Goal goal) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Goal");
        alert.setHeaderText("Are you sure you want to delete this goal?");
        alert.setContentText(goal.getTitle());

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    goalService.deleteGoal(goal.getId());
                    refreshDashboard();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
