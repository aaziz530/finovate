package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.esprit.finovate.entities.User;
import org.esprit.finovate.services.IUserService;
import org.esprit.finovate.services.UserService;
import org.esprit.finovate.utils.Session;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

public class UserProfileController implements Initializable {

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField cinField;
    @FXML
    private TextField cardNumberField;
    @FXML
    private DatePicker birthDatePicker;
    @FXML
    private Label errorLabel;

    private final IUserService userService = new UserService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUserData();
    }

    private void loadUserData() {
        User user = Session.currentUser;
        if (user != null) {
            firstNameField.setText(user.getFirstName());
            lastNameField.setText(user.getLastName());
            emailField.setText(user.getEmail());
            cinField.setText(user.getCinNumber());
            cardNumberField.setText(user.getCardNumber());

            if (user.getBirthdate() != null) {
                // Wrap in java.util.Date to avoid UnsupportedOperationException from
                // java.sql.Date.toInstant()
                Date birthdate = new Date(user.getBirthdate().getTime());
                birthDatePicker.setValue(birthdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
        }
    }

    @FXML
    private void handleSave() {
        errorLabel.setVisible(false);

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        LocalDate birthDateLocal = birthDatePicker.getValue();

        if (firstName.isEmpty() || lastName.isEmpty() || birthDateLocal == null) {
            showError("First name, Last name and Birth Date are required.");
            return;
        }

        try {
            User user = Session.currentUser;
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setBirthdate(Date.from(birthDateLocal.atStartOfDay(ZoneId.systemDefault()).toInstant()));

            userService.updateUser(user);

            // Refresh dashboard UI
            if (UserDashboardController.instance != null) {
                UserDashboardController.instance.refreshUserInfo();
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Profile updated successfully!");
            alert.showAndWait();

        } catch (SQLException e) {
            showError("Failed to update profile: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
