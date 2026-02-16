package org.esprit.finovate.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.esprit.finovate.services.UserService;
import org.esprit.finovate.utils.ValidationUtils;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    private final UserService userService = new UserService();
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleLogin() {
        lblError.setVisible(false);
        lblError.setManaged(false);

        String email = txtEmail.getText() == null ? "" : txtEmail.getText().trim();
        String password = txtPassword.getText() == null ? "" : txtPassword.getText();

        String err = ValidationUtils.validateEmail(email);
        if (err != null) { showError(err); return; }
        err = ValidationUtils.validatePassword(password);
        if (err != null) { showError(err); return; }

        try {
            if (userService.login(email, password) != null) {
                openDashboard();
            } else {
                showError("Invalid email or password.");
            }
        } catch (SQLException e) {
            showError("Connection error. Try again.");
            e.printStackTrace();
        } catch (IOException e) {
            showError("Failed to load dashboard.");
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void openDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
        Parent root = loader.load();
        DashboardController ctrl = loader.getController();
        ctrl.setStage(stage);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Finovate - Dashboard");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.centerOnScreen();
    }
}
