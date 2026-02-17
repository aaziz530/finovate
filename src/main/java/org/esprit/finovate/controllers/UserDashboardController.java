package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.esprit.finovate.utils.Session;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class UserDashboardController implements Initializable {

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userRoleLabel;

    @FXML
    private StackPane contentArea;

    @FXML
    private Button btnHome;

    @FXML
    private Button btnGoals;

    @FXML
    private Button btnTransfer;

    @FXML
    private Button btnBills;

    @FXML
    private Button btnProfile;

    @FXML
    private Button btnLogout;

    public static UserDashboardController instance;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        refreshUserInfo();

        // Load default view (User Home)
        loadView("/UserHome.fxml");
        updateButtonStyles(btnHome);
    }

    @FXML
    private void handleHome() {
        loadView("/UserHome.fxml");
        updateButtonStyles(btnHome);
    }

    @FXML
    private void handleGoals() {
        loadView("/Goals.fxml");
        updateButtonStyles(btnGoals);
    }

    @FXML
    private void handleTransfer() {
        loadView("/Transfer.fxml");
        updateButtonStyles(btnTransfer);
    }

    @FXML
    private void handleBills() {
        loadView("/Bills.fxml");
        updateButtonStyles(btnBills);
    }

    @FXML
    private void handleProfile() {
        loadView("/UserProfile.fxml");
        updateButtonStyles(btnProfile);
    }

    public void refreshUserInfo() {
        if (Session.currentUser != null) {
            userNameLabel.setText(Session.currentUser.getFirstName() + " " + Session.currentUser.getLastName());
            userRoleLabel.setText(Session.currentUser.getRole());
        }
    }

    @FXML
    private void handleLogout() {
        Session.currentUser = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login - Finovate");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load view: " + fxmlPath);
        }
    }

    private void updateButtonStyles(Button activeBtn) {
        Button[] buttons = { btnHome, btnGoals, btnTransfer, btnBills, btnProfile };
        for (Button btn : buttons) {
            if (btn == null)
                continue;
            if (btn == activeBtn) {
                btn.setStyle(
                        "-fx-background-color: #f0fdf4; -fx-text-fill: #237f4e; -fx-cursor: hand; -fx-background-radius: 8; -fx-font-weight: bold;");
            } else {
                btn.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: #525f7f; -fx-cursor: hand; -fx-font-weight: normal;");
            }
        }
    }
}
