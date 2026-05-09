package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.*;
import org.esprit.finovate.entities.Goal;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class GoalDialogController {

    @FXML
    private TextField titleField;
    @FXML
    private TextField amountField;
    @FXML
    private DatePicker deadlinePicker;
    @FXML
    private Label errorLabel;
    @FXML
    private Button cancelButton;

    @FXML
    private Label suggestedLabel;

    // Result
    private Goal createdGoal;
    public boolean saveClicked = false;

    @FXML
    public void initialize() {
        // Ajouter des écouteurs pour mettre à jour la suggestion en temps réel
        amountField.textProperty().addListener((obs, oldVal, newVal) -> updateSuggestion());
        deadlinePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateSuggestion());
    }

    private void updateSuggestion() {
        try {
            String amountStr = amountField.getText().trim();
            LocalDate deadlineDate = deadlinePicker.getValue();

            if (!amountStr.isEmpty() && deadlineDate != null) {
                String targetAmount = amountStr;
                Date deadline = Date.from(deadlineDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                
                // Calcul temporaire pour l'affichage
                Goal tempGoal = new Goal();
                tempGoal.setTargetAmount(targetAmount);
                tempGoal.setCurrentAmount(createdGoal != null ? createdGoal.getCurrentAmount() : "0");
                tempGoal.setDeadline(deadline);
                
                float suggested = tempGoal.getSuggestedMonthlySaving();
                if (suggested > 0) {
                    suggestedLabel.setText(String.format("Suggested: %.2f TND / month", suggested));
                    suggestedLabel.setVisible(true);
                } else {
                    suggestedLabel.setVisible(false);
                }
            } else {
                suggestedLabel.setVisible(false);
            }
        } catch (Exception e) {
            suggestedLabel.setVisible(false);
        }
    }

    public void setGoal(Goal goal) {
        this.createdGoal = goal;
        if (goal != null) {
            titleField.setText(goal.getTitle());
            amountField.setText(String.valueOf(goal.getTargetAmount()));
            if (goal.getDeadline() != null)
                deadlinePicker.setValue(Instant.ofEpochMilli(goal.getDeadline().getTime())
                        .atZone(ZoneId.systemDefault()).toLocalDate());
        }
    }

    public Goal getGoal() {
        return createdGoal;
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleSave() {
        if (isValid()) {
            String title = titleField.getText().trim();
            String amount = amountField.getText().trim();
            Date deadline = Date.from(deadlinePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

            if (createdGoal == null) {
                createdGoal = new Goal(); // IDUser will be set by caller
                createdGoal.setCreatedAt(new Date());
                createdGoal.setStatus("In Progress");
                createdGoal.setCurrentAmount("0");
            }
            createdGoal.setTitle(title);
            createdGoal.setTargetAmount(amount);
            createdGoal.setDeadline(deadline);

            saveClicked = true;
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        }
    }

    private boolean isValid() {
        String msg = "";
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            msg += "Title is required. ";
        }
        if (amountField.getText() == null || amountField.getText().trim().isEmpty()) {
            msg += "Amount is required. ";
        } else {
            try {
                float v = Float.parseFloat(amountField.getText());
                if (v <= 0)
                    msg += "Amount must be positive. ";
            } catch (NumberFormatException e) {
                msg += "Invalid Amount format. ";
            }
        }
        if (deadlinePicker.getValue() == null) {
            msg += "Deadline is required. ";
        } else if (deadlinePicker.getValue().isBefore(LocalDate.now())) {
            msg += "Deadline must be in the future. ";
        }

        if (msg.isEmpty()) {
            return true;
        } else {
            errorLabel.setText(msg);
            errorLabel.setVisible(true);
            return false;
        }
    }
}
