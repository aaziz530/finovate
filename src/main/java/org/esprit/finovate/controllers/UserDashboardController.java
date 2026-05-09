package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
    private Button btnTopUp;

    @FXML
    private Button btnBills;

    @FXML
    private Button btnForum;

    @FXML
    private Button btnInvestissement;

    @FXML
    private VBox investSubMenu;

    @FXML
    private Button btnAddProject;

    @FXML
    private Button btnAllProjects;

    @FXML
    private Button btnMyProjects;

    @FXML
    private Button btnMyInvestments;

    @FXML
    private Button btnReclamations;

    @FXML
    private Button btnMarketplace;

    @FXML
    private Button btnChatbot;

    @FXML
    private Button btnProfile;

    @FXML
    private Button btnLogout;

    public static UserDashboardController instance;

    private Button activeButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        refreshUserInfo();

        setupHoverEffects();

        // Load default view (User Home)
        loadView("/UserHome.fxml");
        updateButtonStyles(btnHome);
    }

    private void setupHoverEffects() {
        Button[] buttons = { btnHome, btnGoals, btnTransfer, btnTopUp, btnBills, btnForum, btnInvestissement, btnReclamations, btnMarketplace, btnChatbot, btnProfile, btnLogout };
        for (Button btn : buttons) {
            if (btn == null) {
                continue;
            }

            btn.setOnMouseEntered(e -> {
                if (btn != activeButton) {
                    btn.setStyle(
                            "-fx-background-color: #f0fdf4; -fx-text-fill: #237f4e; -fx-cursor: hand; -fx-background-radius: 8; -fx-font-weight: bold;");
                }
            });

            btn.setOnMouseExited(e -> {
                if (btn != activeButton) {
                    btn.setStyle(
                            "-fx-background-color: transparent; -fx-text-fill: #525f7f; -fx-cursor: hand; -fx-font-weight: normal;");
                }
            });
        }
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
    private void handleTopUp() {
        loadView("/TopUp.fxml");
        updateButtonStyles(btnTopUp);
    }

    @FXML
    private void handleBills() {
        loadView("/Bills.fxml");
        updateButtonStyles(btnBills);
    }

    @FXML
    private void handleForum() {
        loadView("/forums-page.fxml");
        updateButtonStyles(btnForum);
    }

    @FXML
    private void handleProfile() {
        loadView("/UserProfile.fxml");
        updateButtonStyles(btnProfile);
    }

    @FXML
    private void handleChatbot() {
        loadView("/ChatbotView.fxml");
        updateButtonStyles(btnChatbot);
    }

    @FXML
    private void handleMarketplace() {
        loadView("/marketplace-view.fxml");
        updateButtonStyles(btnMarketplace);
    }

    @FXML
    private void toggleInvestMenu() {
        if (investSubMenu != null) {
            boolean isVisible = investSubMenu.isVisible();
            investSubMenu.setVisible(!isVisible);
            investSubMenu.setManaged(!isVisible);
            btnInvestissement.setText(isVisible ? "Investissement  ▼" : "Investissement  ▲");
        }
    }

    @FXML
    private void handleAddProject() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_project.fxml"));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller != null && contentArea != null && contentArea.getScene() != null) {
                Stage stage = (Stage) contentArea.getScene().getWindow();
                try {
                    controller.getClass().getMethod("setStage", Stage.class).invoke(controller, stage);
                } catch (Exception ignored) {}
                // Set callback to navigate back to all projects list after creation
                try {
                    controller.getClass().getMethod("setOnProjectCreatedCallback", Runnable.class)
                            .invoke(controller, (Runnable) this::handleAllProjects);
                } catch (Exception ignored) {}
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            updateButtonStyles(btnInvestissement);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAllProjects() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/all_projects_embed.fxml"));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller != null && contentArea != null && contentArea.getScene() != null) {
                Stage stage = (Stage) contentArea.getScene().getWindow();
                try {
                    controller.getClass().getMethod("setStage", Stage.class).invoke(controller, stage);
                } catch (Exception ignored) {}
                // Set callback for embedded navigation when editing projects
                try {
                    controller.getClass().getMethod("setOnEditReturnCallback", Runnable.class)
                            .invoke(controller, (Runnable) this::handleAllProjects);
                } catch (Exception ignored) {}
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            updateButtonStyles(btnInvestissement);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMyProjects() {
        loadView("/fxml/my_projects.fxml");
        updateButtonStyles(btnInvestissement);
    }

    @FXML
    private void handleMyInvestments() {
        loadView("/fxml/my_investments.fxml");
        updateButtonStyles(btnInvestissement);
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
        org.esprit.finovate.utils.RememberMeService.clearCredentials();
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

            Object controller = loader.getController();
            if (controller != null && contentArea != null && contentArea.getScene() != null) {
                Stage stage = (Stage) contentArea.getScene().getWindow();
                try {
                    controller.getClass().getMethod("setStage", Stage.class).invoke(controller, stage);
                } catch (Exception ignored) {
                    // Controller doesn't support setStage(Stage)
                }
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load view: " + fxmlPath);
        }
    }

    private void updateButtonStyles(Button activeBtn) {
        this.activeButton = activeBtn;
        Button[] buttons = { btnHome, btnGoals, btnTransfer, btnTopUp, btnBills, btnForum, btnInvestissement, btnReclamations, btnMarketplace, btnChatbot, btnProfile };
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

        if (btnLogout != null) {
            btnLogout.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #dc3545; -fx-cursor: hand; -fx-font-weight: normal;");
        }
    }
}
