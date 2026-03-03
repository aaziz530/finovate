package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.esprit.finovate.utils.Session;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML private TextField     fieldFirstName;
    @FXML private TextField     fieldLastName;
    @FXML private TextField     fieldEmail;
    @FXML private PasswordField fieldOldPassword;
    @FXML private PasswordField fieldNewPassword;
    @FXML private PasswordField fieldConfirmPassword;
    @FXML private TextField     fieldCIN;
    @FXML private TextField     fieldCardNumber;
    @FXML private DatePicker    fieldBirthDate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUser();
    }

    private void loadUser() {
        // ✅ CORRECTION : Session.currentUser -> Session.isActive()
        if (!Session.isActive()) {
            fieldFirstName.setPromptText("First name");
            fieldLastName.setPromptText("Last name");
            fieldEmail.setPromptText("Email");
            return;
        }
        // ✅ CORRECTION : Session.currentUser.getX() -> Session.getCurrentUser().getX()
        fieldFirstName.setText(Session.getCurrentUser().getFirstName());
        fieldLastName.setText(Session.getCurrentUser().getLastName());
        fieldEmail.setText(Session.getCurrentUser().getEmail());
        fieldCIN.setText("");

        if (Session.getCurrentUser().getNumeroCarte() != null) {
            fieldCardNumber.setText(String.valueOf(Session.getCurrentUser().getNumeroCarte()));
        }
        if (Session.getCurrentUser().getBirthdate() != null) {
            LocalDate ld = Instant.ofEpochMilli(Session.getCurrentUser().getBirthdate().getTime())
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            fieldBirthDate.setValue(ld);
        }
    }

    @FXML
    private void onSaveChanges() {
        // ✅ CORRECTION : Session.currentUser -> Session.isActive()
        if (!Session.isActive()) {
            showAlert(Alert.AlertType.INFORMATION, "Profile", "No user logged in. Data not saved.");
            return;
        }
        // ✅ CORRECTION : Session.currentUser.setX() -> Session.getCurrentUser().setX()
        Session.getCurrentUser().setFirstName(fieldFirstName.getText());
        Session.getCurrentUser().setLastName(fieldLastName.getText());
        Session.getCurrentUser().setEmail(fieldEmail.getText());

        if (fieldCardNumber.getText() != null && !fieldCardNumber.getText().isEmpty()) {
            try {
                Session.getCurrentUser().setNumeroCarte(
                        Long.parseLong(fieldCardNumber.getText().trim()));
            } catch (NumberFormatException ignored) {}
        }
        if (fieldBirthDate.getValue() != null) {
            Date d = Date.from(fieldBirthDate.getValue()
                    .atStartOfDay(ZoneId.systemDefault()).toInstant());
            Session.getCurrentUser().setBirthdate(d);
        }
        showAlert(Alert.AlertType.INFORMATION, "Profile", "Changes saved successfully.");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}