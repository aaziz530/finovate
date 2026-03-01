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

    @FXML
    private TextField fieldFirstName;
    @FXML
    private TextField fieldLastName;
    @FXML
    private TextField fieldEmail;
    @FXML
    private PasswordField fieldOldPassword;
    @FXML
    private PasswordField fieldNewPassword;
    @FXML
    private PasswordField fieldConfirmPassword;
    @FXML
    private TextField fieldCIN;
    @FXML
    private TextField fieldCardNumber;
    @FXML
    private DatePicker fieldBirthDate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUser();
    }

    private void loadUser() {
        if (Session.currentUser == null) {
            fieldFirstName.setPromptText("First name");
            fieldLastName.setPromptText("Last name");
            fieldEmail.setPromptText("Email");
            return;
        }
        fieldFirstName.setText(Session.currentUser.getFirstName());
        fieldLastName.setText(Session.currentUser.getLastName());
        fieldEmail.setText(Session.currentUser.getEmail());
        fieldCIN.setText("");
        if (Session.currentUser.getNumeroCarte() != null) {
            fieldCardNumber.setText(String.valueOf(Session.currentUser.getNumeroCarte()));
        }
        if (Session.currentUser.getBirthdate() != null) {
            LocalDate ld = Instant.ofEpochMilli(Session.currentUser.getBirthdate().getTime())
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            fieldBirthDate.setValue(ld);
        }
    }

    @FXML
    private void onSaveChanges() {
        if (Session.currentUser == null) {
            showAlert(Alert.AlertType.INFORMATION, "Profile", "No user logged in. Data not saved.");
            return;
        }
        Session.currentUser.setFirstName(fieldFirstName.getText());
        Session.currentUser.setLastName(fieldLastName.getText());
        Session.currentUser.setEmail(fieldEmail.getText());
        if (fieldCardNumber.getText() != null && !fieldCardNumber.getText().isEmpty()) {
            try {
                Session.currentUser.setNumeroCarte(Long.parseLong(fieldCardNumber.getText().trim()));
            } catch (NumberFormatException ignored) {}
        }
        if (fieldBirthDate.getValue() != null) {
            Date d = Date.from(fieldBirthDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Session.currentUser.setBirthdate(d);
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
