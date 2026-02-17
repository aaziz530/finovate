package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.esprit.finovate.entities.User;
import org.esprit.finovate.services.IUserService;
import org.esprit.finovate.services.UserService;
import org.esprit.finovate.utils.PasswordUtils;
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
    private PasswordField oldPasswordField;
    @FXML
    private TextField oldPasswordTextField;
    @FXML
    private Button oldPasswordToggle;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private TextField newPasswordTextField;
    @FXML
    private Button newPasswordToggle;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private TextField confirmPasswordTextField;
    @FXML
    private Button confirmPasswordToggle;
    @FXML
    private TextField cinField;

    @FXML
    private TextField numeroCarteField;
    @FXML
    private DatePicker birthDatePicker;
    @FXML
    private Label errorLabel;

    private final IUserService userService = new UserService();

    private final BooleanProperty oldPwdVisible = new SimpleBooleanProperty(false);
    private final BooleanProperty newPwdVisible = new SimpleBooleanProperty(false);
    private final BooleanProperty confirmPwdVisible = new SimpleBooleanProperty(false);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupPasswordToggle(oldPasswordField, oldPasswordTextField, oldPasswordToggle, oldPwdVisible);
        setupPasswordToggle(newPasswordField, newPasswordTextField, newPasswordToggle, newPwdVisible);
        setupPasswordToggle(confirmPasswordField, confirmPasswordTextField, confirmPasswordToggle, confirmPwdVisible);
        loadUserData();
    }

    private void setupPasswordToggle(PasswordField pf, TextField tf, Button toggleBtn, BooleanProperty visibleProp) {
        if (pf == null || tf == null || toggleBtn == null) {
            return;
        }

        tf.textProperty().bindBidirectional(pf.textProperty());

        pf.visibleProperty().bind(visibleProp.not());
        pf.managedProperty().bind(visibleProp.not());

        tf.visibleProperty().bind(visibleProp);
        tf.managedProperty().bind(visibleProp);

        toggleBtn.setOnAction(e -> visibleProp.set(!visibleProp.get()));
        visibleProp.addListener((obs, oldV, newV) -> toggleBtn.setText(newV ? "Hide" : "Show"));
        toggleBtn.setText("Show");
    }

    private void loadUserData() {
        User user = Session.currentUser;
        if (user != null) {
            firstNameField.setText(user.getFirstName());
            lastNameField.setText(user.getLastName());
            emailField.setText(user.getEmail());

            cinField.setText(user.getCinNumber());
            numeroCarteField.setText(user.getNumeroCarte().toString());

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
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        LocalDate birthDateLocal = birthDatePicker.getValue();

        String oldPwd = oldPasswordField.isVisible() ? oldPasswordField.getText() : oldPasswordTextField.getText();
        String newPwd = newPasswordField.isVisible() ? newPasswordField.getText() : newPasswordTextField.getText();
        String confirmPwd = confirmPasswordField.isVisible() ? confirmPasswordField.getText() : confirmPasswordTextField.getText();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || birthDateLocal == null) {
            showError("First name, Last name, Email and Birth Date are required.");
            return;
        }

        try {
            User user = Session.currentUser;
            if (user == null) {
                showError("No user in session.");
                return;
            }

            boolean wantsPasswordChange = !oldPwd.isBlank() || !newPwd.isBlank() || !confirmPwd.isBlank();

            if (wantsPasswordChange) {
                if (oldPwd.isBlank()) {
                    showError("Old password is required.");
                    return;
                }

                if (newPwd.isBlank() || confirmPwd.isBlank()) {
                    showError("New password and confirmation are required.");
                    return;
                }
                if (!newPwd.equals(confirmPwd)) {
                    showError("New password and confirmation do not match.");
                    return;
                }

                userService.changePassword(user.getId(), oldPwd, newPwd);
                user.setPassword(PasswordUtils.sha256(newPwd));

                oldPasswordField.clear();
                oldPasswordTextField.clear();
                newPasswordField.clear();
                newPasswordTextField.clear();
                confirmPasswordField.clear();
                confirmPasswordTextField.clear();
            }

            if (!email.equalsIgnoreCase(user.getEmail())) {
                if (userService.emailExists(email)) {
                    showError("Email already exists.");
                    return;
                }
                user.setEmail(email);
            }

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

        } catch (IllegalStateException | IllegalArgumentException e) {
            showError(e.getMessage());

        } catch (SQLException e) {
            showError("Failed to update profile: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setTextFill(Color.web("#dc3545"));
        errorLabel.setStyle("-fx-text-fill: #dc3545;");
        errorLabel.setVisible(true);
    }
}