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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (Session.currentUser != null) {
            userNameLabel.setText(Session.currentUser.getFirstName() + " " + Session.currentUser.getLastName());
            userRoleLabel.setText(Session.currentUser.getRole());
        }

        // Load default view (Goals for now, as requested to start with)
        loadView("/Goals.fxml");
    }

    @FXML
    private void handleHome() {
        // Placeholder for Home/Overview
        // loadView("/UserHome.fxml");
    }

    @FXML
    private void handleGoals() {
        loadView("/Goals.fxml");
    }

    @FXML
    private void handleTransfer() {
        // Placeholder
        // loadView("/Transfer.fxml");
    }

    @FXML
    private void handleBills() {
        // Placeholder
        // loadView("/Bills.fxml");
    }

    @FXML
    private void handleProfile() {
        // Placeholder
        // loadView("/UserProfile.fxml");
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
}
