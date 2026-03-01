package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.esprit.finovate.entities.User;
import org.esprit.finovate.services.EmailService;
import org.esprit.finovate.services.GitHubOAuthConfig;
import org.esprit.finovate.services.GitHubOAuthService;
import org.esprit.finovate.services.GoogleOAuthConfig;
import org.esprit.finovate.services.GoogleOAuthService;
import org.esprit.finovate.services.IUserService;
import org.esprit.finovate.services.UserService;
import org.esprit.finovate.utils.Session;
import org.esprit.finovate.utils.RememberMeService;
import org.esprit.finovate.utils.CaptchaService;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
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
    private Button googleLoginButton;

    @FXML
    private Button githubLoginButton;

    @FXML
    private CheckBox rememberMeCheckbox;

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
    private Button backToLoginButton;

    @FXML
    private Hyperlink loginLink;

    // Captcha FXML fields
    @FXML
    private ImageView captchaImageView;

    @FXML
    private Button refreshCaptchaButton;

    @FXML
    private TextField captchaField;

    private CaptchaService captchaService;

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
        this.captchaService = new CaptchaService();
    }

    @FXML
    public void initialize() {
        // Initialize captcha on register page
        if (captchaImageView != null && captchaService != null) {
            refreshCaptcha();
        }

        if (usernameField != null && passwordField != null && rememberMeCheckbox != null) {
            if (RememberMeService.isRememberMeEnabled()) {
                String savedUser = RememberMeService.getSavedUsername();
                String savedPass = RememberMeService.getSavedPassword();
                
                usernameField.setText(savedUser);
                passwordField.setText(savedPass);
                rememberMeCheckbox.setSelected(true);

                // Auto-login
                Platform.runLater(() -> {
                    try {
                        User user = userService.login(savedUser, savedPass);
                        if (user != null) {
                            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                                loadAdminDashboard();
                            } else {
                                navigateToPage("/UserDashboard.fxml", "User Dashboard - Finovate");
                            }
                        }
                    } catch (SQLException e) {
                        // If auto-login fails (e.g. password changed), just stay on login page
                        System.err.println("Auto-login failed: " + e.getMessage());
                    }
                });
            }
        }
    }

    @FXML
    private void handleGoogleLogin() {
        clearError();

        setLoginControlsDisabled(true);

        Thread t = new Thread(() -> {
            try {
                GoogleOAuthConfig config = GoogleOAuthConfig.load();
                GoogleOAuthService oauth = new GoogleOAuthService(config);
                GoogleOAuthService.GoogleUserInfo googleUser = oauth.authenticate();

                Platform.runLater(() -> handleGoogleUserAuthenticated(googleUser));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Google login failed: " + e.getMessage());
                    setLoginControlsDisabled(false);
                });
            }
        });

        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void handleGitHubLogin() {
        clearError();

        setLoginControlsDisabled(true);

        Thread t = new Thread(() -> {
            try {
                GitHubOAuthConfig config = GitHubOAuthConfig.load();
                GitHubOAuthService oauth = new GitHubOAuthService(config);
                GitHubOAuthService.GitHubUserInfo githubUser = oauth.authenticate();

                Platform.runLater(() -> handleGitHubUserAuthenticated(githubUser));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("GitHub login failed: " + e.getMessage());
                    setLoginControlsDisabled(false);
                });
            }
        });

        t.setDaemon(true);
        t.start();
    }

    private void handleGitHubUserAuthenticated(GitHubOAuthService.GitHubUserInfo githubUser) {
        try {
            String email = githubUser.email();
            if (email == null || email.isBlank()) {
                showError("GitHub did not return an email");
                return;
            }

            User existing = ((UserService) userService).findByEmail(email);
            if (existing != null) {
                org.esprit.finovate.utils.Session.currentUser = existing;
                if ("ADMIN".equalsIgnoreCase(existing.getRole())) {
                    loadAdminDashboard();
                } else {
                    navigateToPage("/UserDashboard.fxml", "User Dashboard - Finovate");
                }
                return;
            }

            // New user - show complete profile dialog
            CompleteProfileController.CompleteProfileResult result = showCompleteProfileDialogForGitHub(githubUser);
            if (result == null) {
                return;
            }

            Date birthdate = Date.from(result.birthdate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            User created = ((UserService) userService).registerGoogleUser(email, result.firstName(), result.lastName(), birthdate, result.cin());

            if (created != null) {
                Session.currentUser = created;
                navigateToPage("/UserDashboard.fxml", "User Dashboard - Finovate");
            }
        } catch (IllegalStateException e) {
            showError(e.getMessage());
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        } finally {
            setLoginControlsDisabled(false);
        }
    }

    private CompleteProfileController.CompleteProfileResult showCompleteProfileDialogForGitHub(GitHubOAuthService.GitHubUserInfo githubUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CompleteProfileDialog.fxml"));
            Parent root = loader.load();
            CompleteProfileController controller = loader.getController();

            controller.presetName(
                    githubUser.firstName() == null ? "" : githubUser.firstName(),
                    githubUser.lastName() == null ? "" : githubUser.lastName());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            Stage owner = getCurrentStage();
            if (owner != null) {
                stage.initOwner(owner);
            }
            stage.setTitle("Complete Profile");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.showAndWait();

            return controller.getResult();
        } catch (IOException e) {
            showError("Failed to open profile form: " + e.getMessage());
            return null;
        }
    }

    private void handleGoogleUserAuthenticated(GoogleOAuthService.GoogleUserInfo googleUser) {
        try {
            String email = googleUser.email();
            if (email == null || email.isBlank()) {
                showError("Google did not return an email");
                return;
            }

            User existing = ((UserService) userService).findByEmail(email);
            if (existing != null) {
                org.esprit.finovate.utils.Session.currentUser = existing;
                if ("ADMIN".equalsIgnoreCase(existing.getRole())) {
                    loadAdminDashboard();
                } else {
                    navigateToPage("/UserDashboard.fxml", "User Dashboard - Finovate");
                }
                return;
            }

            CompleteProfileController.CompleteProfileResult result = showCompleteProfileDialog(googleUser);
            if (result == null) {
                return;
            }

            Date birthdate = Date.from(result.birthdate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            User created = ((UserService) userService).registerGoogleUser(email, result.firstName(), result.lastName(), birthdate, result.cin());

            if (created != null) {
                Session.currentUser = created;
                navigateToPage("/UserDashboard.fxml", "User Dashboard - Finovate");
            }
        } catch (IllegalStateException e) {
            showError(e.getMessage());
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        } finally {
            setLoginControlsDisabled(false);
        }
    }

    private CompleteProfileController.CompleteProfileResult showCompleteProfileDialog(GoogleOAuthService.GoogleUserInfo googleUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CompleteProfileDialog.fxml"));
            Parent root = loader.load();
            CompleteProfileController controller = loader.getController();

            controller.presetName(
                    googleUser.givenName() == null ? "" : googleUser.givenName(),
                    googleUser.familyName() == null ? "" : googleUser.familyName());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            Stage owner = getCurrentStage();
            if (owner != null) {
                stage.initOwner(owner);
            }
            stage.setTitle("Complete Profile");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.showAndWait();

            return controller.getResult();
        } catch (IOException e) {
            showError("Failed to open profile form: " + e.getMessage());
            return null;
        }
    }

    private void setLoginControlsDisabled(boolean disabled) {
        if (loginButton != null) {
            loginButton.setDisable(disabled);
        }
        if (googleLoginButton != null) {
            googleLoginButton.setDisable(disabled);
        }
        if (githubLoginButton != null) {
            githubLoginButton.setDisable(disabled);
        }
        if (usernameField != null) {
            usernameField.setDisable(disabled);
        }
        if (passwordField != null) {
            passwordField.setDisable(disabled);
        }
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

            // Save or clear credentials based on Remember Me checkbox
            if (rememberMeCheckbox.isSelected()) {
                RememberMeService.saveCredentials(username, password);
            } else {
                RememberMeService.clearCredentials();
            }

            // Check if user is admin
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                loadAdminDashboard();
            } else {
                navigateToPage("/UserDashboard.fxml", "User Dashboard - Finovate");
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

        if (firstName.length() < 3 || lastName.length() < 3) {
            showRegisterError("First name and last name must be at least 3 characters");
            return;
        }

        if (!firstName.matches("[A-Za-zÀ-ÖØ-öø-ÿ]+([ '\\-][A-Za-zÀ-ÖØ-öø-ÿ]+)*") ||
                !lastName.matches("[A-Za-zÀ-ÖØ-öø-ÿ]+([ '\\-][A-Za-zÀ-ÖØ-öø-ÿ]+)*")) {
            showRegisterError("First name and last name must contain only letters");
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

        LocalDate birthdateLocal = birthdatePicker.getValue();
        int age = Period.between(birthdateLocal, LocalDate.now()).getYears();
        if (age < 18) {
            showRegisterError("You must be at least 18 years old to create an account");
            return;
        }

        // Captcha validation
        String captchaInput = captchaField != null ? captchaField.getText().trim() : "";
        if (captchaInput.isEmpty()) {
            showRegisterError("Please enter the verification code");
            return;
        }
        if (!captchaService.verify(captchaInput)) {
            showRegisterError("Invalid verification code. Please try again.");
            refreshCaptcha();
            if (captchaField != null) {
                captchaField.clear();
            }
            return;
        }

        try {
            Date birthdate = Date.from(birthdatePicker.getValue()
                    .atStartOfDay(ZoneId.systemDefault()).toInstant());

            User user = userService.register(email, password, firstName, lastName, birthdate, cinNumber);

            if (user != null) {
                showSuccessAndNavigateToLogin();
            }

        } catch (IllegalStateException e) {
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
        navigateToPage("/Register.fxml", "Register - Finovate");
    }

    /**
     * Refresh captcha image
     */
    @FXML
    private void handleRefreshCaptcha() {
        refreshCaptcha();
        if (captchaField != null) {
            captchaField.clear();
        }
    }

    private void refreshCaptcha() {
        if (captchaService != null && captchaImageView != null) {
            captchaService.generateNewCaptcha();
            Image captchaImage = captchaService.generateCaptchaImage();
            captchaImageView.setImage(captchaImage);
        }
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
