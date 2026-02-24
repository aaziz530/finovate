package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.entities.User;
import org.example.services.UserService;
import org.example.utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Date;

public class AuthController {

    // Login FXML fields
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private Hyperlink registerLink;

    // Register FXML fields
    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private DatePicker birthdatePicker;

    @FXML
    private TextField cinField;

    @FXML
    private PasswordField registerPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label registerErrorLabel;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink loginLink;

    // Forgot Password FXML fields
    @FXML
    private TextField resetEmailField;

    @FXML
    private Label resetErrorLabel;

    @FXML
    private Button sendCodeButton;

    // Reset Password FXML fields
    @FXML
    private TextField codeField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmNewPasswordField;

    @FXML
    private Label resetPassErrorLabel;

    @FXML
    private Button resetPasswordButton;

    private final UserService userService;

    // Session-like variables for reset process
    private static String currentResetEmail;
    private static String currentResetCode;

    public AuthController() {
        this.userService = new UserService();
    }

    @FXML
    private void handleLogin() {
        clearError();

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password");
            return;
        }

        try {
            User user = userService.login(username, password);

            if (user == null) {
                showError("Invalid email or password");
                return;
            }

            // Set session
            SessionManager.login(user);

            // Check if user is admin
            if ("ADMIN".equals(user.getRole())) {
                navigateToPage("/fxml/admin-dashboard.fxml", "Finovate - Admin Dashboard");
            } else {
                // Navigate to user dashboard
                navigateToPage("/fxml/user-dashboard.fxml", "Finovate - Dashboard");
            }

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle register action
     */
    @FXML
    private void handleRegister() {
        clearRegisterError();

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = registerPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String cinNumber = cinField.getText().trim();

        // Validation
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty() || cinNumber.isEmpty()) {
            showRegisterError("All fields are required");
            return;
        }

        if (!cinNumber.matches("\\d{8}")) {
            showRegisterError("CIN Number must be exactly 8 digits");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showRegisterError("Invalid email format");
            return;
        }

        if (password.length() < 6) {
            showRegisterError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showRegisterError("Passwords do not match");
            return;
        }

        if (birthdatePicker.getValue() == null) {
            showRegisterError("Please select your birthdate");
            return;
        }

        try {
            Date birthdate = Date.from(birthdatePicker.getValue()
                    .atStartOfDay(ZoneId.systemDefault()).toInstant());

            // Create user using constructor
            User user = new User(email, password, firstName, lastName, birthdate, cinNumber);
            // Default values are set in constructor: role=USER, solde=500, points=0, numeroCarte generated

            boolean created = userService.createUser(user);

            if (created) {
                showSuccessAndNavigateToLogin();
            } else {
                showRegisterError("Failed to create account");
            }

        } catch (IllegalArgumentException e) {
            showRegisterError(e.getMessage());
        } catch (SQLException e) {
            showRegisterError("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigate to register page
     */
    @FXML
    private void handleRegisterLink() {
        navigateToPage("/fxml/register.fxml", "Register - Finovate");
    }

    /**
     * Navigate to login page
     */
    @FXML
    private void handleLoginLink() {
        navigateToPage("/fxml/login.fxml", "Login - Finovate");
    }

    /**
     * Show success message and navigate to login
     */
    private void showSuccessAndNavigateToLogin() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registration Successful");
        alert.setHeaderText(null);
        alert.setContentText("Account created successfully! You can now login.");
        alert.showAndWait();

        handleLoginLink();
    }

    /**
     * Handle Forgot Password Link
     */
    @FXML
    private void handleForgotPasswordLink() {
        navigateToPage("/fxml/forgot-password.fxml", "Forgot Password - Finovate");
    }

    /**
     * Handle Send Reset Code
     */
    @FXML
    private void handleSendResetCode() {
        String email = resetEmailField.getText().trim();

        if (email.isEmpty()) {
            showResetError("Please enter your email");
            return;
        }

        try {
            // Check if user exists by email
            User user = userService.getUserByEmail(email);
            if (user == null) {
                showResetError("This email address is not registered");
                return;
            }

            // Generate 6-digit code
            currentResetCode = String.valueOf((int) (Math.random() * 900000) + 100000);
            currentResetEmail = email;

            // In a real app, send email here
            // For now, show the code in console
            System.out.println("Reset code for " + email + ": " + currentResetCode);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Reset Code Sent");
            alert.setHeaderText(null);
            alert.setContentText("Reset code has been generated. Check console for code: " + currentResetCode);
            alert.showAndWait();

            navigateToPage("/fxml/reset-password.fxml", "Reset Password - Finovate");

        } catch (Exception e) {
            showResetError("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle Reset Password
     */
    @FXML
    private void handleResetPassword() {
        String code = codeField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmNewPasswordField.getText();

        if (code.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showResetPassError("All fields are required");
            return;
        }

        if (!code.equals(currentResetCode)) {
            showResetPassError("Invalid reset code");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showResetPassError("Passwords do not match");
            return;
        }

        if (newPassword.length() < 6) {
            showResetPassError("Password must be at least 6 characters");
            return;
        }

        try {
            // Get user by email and update password
            User user = userService.getUserByEmail(currentResetEmail);
            if (user == null) {
                showResetPassError("User not found");
                return;
            }

            user.setPassword(newPassword);
            userService.updateUser(user);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Your password has been reset successfully!");
            alert.showAndWait();

            handleLoginLink();

        } catch (SQLException e) {
            showResetPassError("Database error: " + e.getMessage());
        }
    }

    private void navigateToPage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = getCurrentStage();
            if (stage == null) {
                System.err.println("Error: Could not determine current stage for navigation.");
                return;
            }
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load page: " + e.getMessage());
        }
    }

    /**
     * Get current stage
     */
    private Stage getCurrentStage() {
        if (loginButton != null && loginButton.getScene() != null) {
            return (Stage) loginButton.getScene().getWindow();
        } else if (registerButton != null && registerButton.getScene() != null) {
            return (Stage) registerButton.getScene().getWindow();
        } else if (sendCodeButton != null && sendCodeButton.getScene() != null) {
            return (Stage) sendCodeButton.getScene().getWindow();
        } else if (resetPasswordButton != null && resetPasswordButton.getScene() != null) {
            return (Stage) resetPasswordButton.getScene().getWindow();
        }
        return null;
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }

    private void clearError() {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        }
    }

    /**
     * Show error message on register page
     */
    private void showRegisterError(String message) {
        if (registerErrorLabel != null) {
            registerErrorLabel.setText(message);
            registerErrorLabel.setVisible(true);
        }
    }

    /**
     * Clear error message on register page
     */
    private void clearRegisterError() {
        if (registerErrorLabel != null) {
            registerErrorLabel.setText("");
            registerErrorLabel.setVisible(false);
        }
    }

    /**
     * Show error on forgot password page
     */
    private void showResetError(String message) {
        if (resetErrorLabel != null) {
            resetErrorLabel.setText(message);
            resetErrorLabel.setVisible(true);
        }
    }

    /**
     * Show error on reset password page
     */
    private void showResetPassError(String message) {
        if (resetPassErrorLabel != null) {
            resetPassErrorLabel.setText(message);
            resetPassErrorLabel.setVisible(true);
        }
    }
}
