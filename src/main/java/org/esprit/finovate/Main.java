package org.esprit.finovate;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.esprit.finovate.controllers.DashboardController;
import org.esprit.finovate.utils.Session;
import org.esprit.finovate.utils.StubLoggedInUser;

import java.io.IOException;

/**
 * Main entry point for Finovate JavaFX Application
 */
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Set a default user to bypass login for now
            Session.currentUser = new StubLoggedInUser(1L);

            var url = getClass().getResource("/fxml/dashboard.fxml");
            if (url == null) {
                throw new IllegalStateException("Missing resource: /fxml/dashboard.fxml (check src/main/resources)");
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            // Get controller and set stage
            DashboardController ctrl = loader.getController();
            ctrl.setStage(primaryStage);

            // Create scene
            Scene scene = new Scene(root);

            // Configure primary stage
            primaryStage.setTitle("Finovate - Dashboard");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(800);
            primaryStage.centerOnScreen();

            // Show the stage
            primaryStage.show();

            System.out.println("Finovate application started successfully!");

        } catch (IOException e) {
            System.err.println("Failed to load fxml/dashboard.fxml");
            e.printStackTrace();
            showErrorAndExit("Failed to load the dashboard page. Please check if fxml/dashboard.fxml exists under src/main/resources.");
        } catch (Exception e) {
            System.err.println("Unexpected error during application startup");
            e.printStackTrace();
            showErrorAndExit("Unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Display error message and exit application
     */
    private void showErrorAndExit(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Application Error");
        alert.setHeaderText("Failed to start Finovate");
        alert.setContentText(message);
        alert.showAndWait();
        System.exit(1);
    }

    @Override
    public void stop() {
        System.out.println("Finovate application stopped.");
    }
}