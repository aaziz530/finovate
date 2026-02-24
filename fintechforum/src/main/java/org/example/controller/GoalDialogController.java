package org.example.controller;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.*;
import org.example.entities.Goal;

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

    private Goal createdGoal;
    public boolean saveClicked = false;

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
            float amount = Float.parseFloat(amountField.getText().trim());
            Date deadline = Date.from(deadlinePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

            if (createdGoal == null) {
                createdGoal = new Goal();
                createdGoal.setCreatedAt(new Date());
                createdGoal.setStatus("In Progress");
                createdGoal.setCurrentAmount(0);
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
