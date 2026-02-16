package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.esprit.finovate.entities.User;
import org.esprit.finovate.services.EmailService;
import org.esprit.finovate.services.IUserService;
import org.esprit.finovate.services.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Date;

/**
 * Controller for authentication operations (Login and Register)
 */
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
    private TextField cardNumberField;

    @FXML
    private PasswordField registerPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label registerErrorLabel;

    @FXML
    private Button registerButton;

    @FXML
    private Button backToLoginButton;

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

    // Service
    private final IUserService userService;
    private final EmailService emailService;

    // Session-like variables for reset process
    private static String currentResetEmail;
    private static String currentResetCode;

    public AuthController() {
        this.userService = new UserService();
        this.emailService = new EmailService();
    }

    /**
     * Handle login action
     */
    @FXML
    private void handleLogin() {
        clearError();

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validation
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

            // Check if user is admin
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                loadAdminDashboard();
            } else {
                showError("Access denied. Admin privileges required.");
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
        String password = registerPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String cardNumber = cardNumberField.getText().trim();

        // Validation
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty() || cardNumber.isEmpty()) {
            showRegisterError("All fields are required");
            return;
        }

        if (!cardNumber.matches("\\d{20}")) {
            showRegisterError("Card Number must be exactly 20 digits");
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

            User user = userService.register(email, password, firstName, lastName, birthdate, cardNumber);

            if (user != null) {
                showSuccessAndNavigateToLogin();
            }

        } catch (IllegalStateException e) {
            showRegisterError("Email already exists. Please use a different email.");
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
        navigateToPage("/Register.fxml", "Register - Finovate");
    }

    /**
     * Navigate to login page
     */
    @FXML
    private void handleLoginLink() {
        navigateToPage("/Login.fxml", "Login - Finovate");
    }

    /**
     * Load admin dashboard
     */
    private void loadAdminDashboard() {
        navigateToPage("/AdminDashboard.fxml", "Admin Dashboard - Finovate");
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
        navigateToPage("/ForgotPassword.fxml", "Forgot Password - Finovate");
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
            // Check if user exists
            if (!userService.emailExists(email)) {
                showResetError("This email address is not registered");
                return;
            }

            // Generate 6-digit code
            currentResetCode = String.valueOf((int) (Math.random() * 900000) + 100000);
            currentResetEmail = email;

            // Send Email (in a real app, do this in a thread)
            emailService.sendEmail(email, "Finovate - Password Reset Code",
                    "Your password reset code is: " + currentResetCode);

            navigateToPage("/ResetPassword.fxml", "Reset Password - Finovate");

        } catch (Exception e) {
            showResetError("Error sending email: " + e.getMessage() + ". Please check your configuration.");
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
            userService.updatePassword(currentResetEmail, newPassword);

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

    /**
     * Navigate to a different FXML page
     */
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

    /**
     * Show error message on login page
     */
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }

    /**
     * Clear error message on login page
     */
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
