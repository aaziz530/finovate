package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.utils.SessionManager;

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
    private Button btnForums;

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

    private Long currentUserId = 1L; // TODO: Get from session

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Check if user is logged in
        if (!SessionManager.isLoggedIn()) {
            System.err.println("No user logged in. Please login first.");
            return;
        }
        
        currentUserId = SessionManager.getCurrentUserId();
        userNameLabel.setText(SessionManager.getCurrentUser().getEmail());
        userRoleLabel.setText(SessionManager.getCurrentUser().getRole());

        // Load default view (Home)
        handleHome();
    }

    @FXML
    private void handleHome() {
        loadView("/fxml/user-home.fxml");
        updateButtonStyles(btnHome);
    }

    @FXML
    private void handleForums() {
        loadForumsView();
        updateButtonStyles(btnForums);
    }

    @FXML
    private void handleGoals() {
        loadView("/fxml/goals.fxml");
        updateButtonStyles(btnGoals);
    }

    @FXML
    private void handleTransfer() {
        loadView("/fxml/transfer.fxml");
        updateButtonStyles(btnTransfer);
    }

    @FXML
    private void handleBills() {
        loadView("/fxml/bills.fxml");
        updateButtonStyles(btnBills);
    }

    @FXML
    private void handleProfile() {
        loadView("/fxml/user-profile.fxml");
        updateButtonStyles(btnProfile);
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Finovate - Sign In");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadForumsView() {
        try {
            // Load the complete forums interface (main.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent forumsView = loader.load();
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(forumsView);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load forums view: " + e.getMessage());
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
        Button[] buttons = { btnHome, btnForums, btnGoals, btnTransfer, btnBills, btnProfile };
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
