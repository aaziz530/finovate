package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.esprit.finovate.utils.Session;

import java.io.IOException;

public class MainLayoutController {

    @FXML
    private StackPane contentPane;
    @FXML
    private Button btnProfile;
    @FXML
    private Label lblUserName;

    private static final String SIDEBAR_BTN = "-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 10 12; -fx-cursor: hand; -fx-font-size: 14px;";
    private static final String SIDEBAR_BTN_ACTIVE = "-fx-background-color: rgba(46, 139, 87, 0.25); -fx-text-fill: #2e8b57; -fx-alignment: CENTER_LEFT; -fx-padding: 10 12; -fx-cursor: hand; -fx-font-size: 14px; -fx-background-radius: 0 6 6 0;";

    @FXML
    public void initialize() {
        updateUserName();
        showView("/dashboard.fxml");
        setActiveButton(null);
    }

    private void updateUserName() {
        if (lblUserName != null && Session.currentUser != null) {
            String name = Session.currentUser.getFirstName() + " " + Session.currentUser.getLastName();
            lblUserName.setText(name != null ? name.trim() : Session.currentUser.getEmail());
        } else if (lblUserName != null) {
            lblUserName.setText("Guest");
        }
    }

    private void setActiveButton(String active) {
        if (btnProfile == null) return;
        boolean isProfile = "profile".equals(active);
        btnProfile.setStyle(isProfile ? SIDEBAR_BTN_ACTIVE : SIDEBAR_BTN);
    }

    @FXML
    private void showDashboard() {
        setActiveButton(null);
        showView("/dashboard.fxml");
    }

    @FXML
    private void showTickets() {
        setActiveButton(null);
        showView("/tickets-list.fxml");
    }

    @FXML
    private void showCreateTicket() {
        setActiveButton(null);
        showView("/ticket-create.fxml");
    }

    @FXML
    private void showProfile() {
        setActiveButton("profile");
        showView("/profile.fxml");
    }

    @FXML
    private void showAgents() {
        setActiveButton(null);
        showView("/admin-agents.fxml");
    }

    @FXML
    private void showMessagerie() {
        setActiveButton(null);
        showView("/messagerie.fxml");
    }

    @FXML
    private void showAdminDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/admin-layout.fxml"));
            Stage stage = new Stage();
            stage.setTitle("FINOVATE Admin Dashboard");
            stage.setScene(new Scene(root, 1100, 700));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showForgotPassword() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/forgot-password.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Forgot Password - Finovate");
            stage.setScene(new Scene(root, 420, 480));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void signOut() {
        Session.currentUser = null;
        javafx.application.Platform.exit();
    }

    private void showView(String fxmlPath) {
        try {
            Node view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}