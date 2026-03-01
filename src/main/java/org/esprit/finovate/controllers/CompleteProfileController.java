package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.Period;

public class CompleteProfileController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField cinField;

    @FXML
    private DatePicker birthdatePicker;

    @FXML
    private Label errorLabel;

    @FXML
    private Button cancelButton;

    @FXML
    private Button saveButton;

    private final org.esprit.finovate.services.IUserService userService = new org.esprit.finovate.services.UserService();
    private CompleteProfileResult result;

    public void presetName(String firstName, String lastName) {
        if (firstNameField != null && firstName != null) {
            firstNameField.setText(firstName);
        }
        if (lastNameField != null && lastName != null) {
            lastNameField.setText(lastName);
        }
    }

    public CompleteProfileResult getResult() {
        return result;
    }

    @FXML
    private void handleSave() {
        hideError();

        String firstName = firstNameField == null ? "" : firstNameField.getText().trim();
        String lastName = lastNameField == null ? "" : lastNameField.getText().trim();
        String cin = cinField == null ? "" : cinField.getText().trim();
        LocalDate birthdate = birthdatePicker == null ? null : birthdatePicker.getValue();

        if (firstName.isEmpty() || lastName.isEmpty() || cin.isEmpty() || birthdate == null) {
            showError("All fields are required.");
            return;
        }

        if (firstName.length() < 3 || lastName.length() < 3) {
            showError("First name and last name must be at least 3 characters.");
            return;
        }

        if (!firstName.matches("[A-Za-zÀ-ÖØ-öø-ÿ]+([ '\\-][A-Za-zÀ-ÖØ-öø-ÿ]+)*") ||
                !lastName.matches("[A-Za-zÀ-ÖØ-öø-ÿ]+([ '\\-][A-Za-zÀ-ÖØ-öø-ÿ]+)*")) {
            showError("First name and last name must contain only letters.");
            return;
        }

        if (!cin.matches("\\d{8}")) {
            showError("CIN Number must be exactly 8 digits.");
            return;
        }

        try {
            if (userService.cinExists(cin)) {
                showError("This CIN is already registered.");
                return;
            }
        } catch (java.sql.SQLException e) {
            showError("Database error checking CIN: " + e.getMessage());
            return;
        }

        int age = Period.between(birthdate, LocalDate.now()).getYears();
        if (age < 18) {
            showError("You must be at least 18 years old.");
            return;
        }

        result = new CompleteProfileResult(firstName, lastName, cin, birthdate);
        close();
    }

    @FXML
    private void handleCancel() {
        result = null;
        close();
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setTextFill(Color.web("#dc3545"));
            errorLabel.setVisible(true);
        }
    }

    private void hideError() {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        }
    }

    private void close() {
        if (cancelButton != null && cancelButton.getScene() != null) {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        } else if (saveButton != null && saveButton.getScene() != null) {
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        }
    }

    public record CompleteProfileResult(String firstName, String lastName, String cin, LocalDate birthdate) {
    }
}
