package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.esprit.finovate.entities.User;
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

    // Service
    private final IUserService userService;

    public AuthController() {
        this.userService = new UserService();
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
        String email = emailField.getText().trim();
        String password = registerPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty()) {
            showRegisterError("All fields are required");
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

            User user = userService.register(email, password, firstName, lastName, birthdate);

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
     * Navigate to a different FXML page
     */
    private void navigateToPage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = getCurrentStage();
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
        if (loginButton != null) {
            return (Stage) loginButton.getScene().getWindow();
        } else if (registerButton != null) {
            return (Stage) registerButton.getScene().getWindow();
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
}
