package org.esprit.finovate;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
            // Load Login FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();

            // Create scene
            Scene scene = new Scene(root);

            // Configure primary stage
            primaryStage.setTitle("Finovate - Login");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(800);
            primaryStage.centerOnScreen();

            // Show the stage
            primaryStage.show();

            System.out.println("Finovate application started successfully!");

        } catch (IOException e) {
            System.err.println("Failed to load Login.fxml");
            e.printStackTrace();
            showErrorAndExit("Failed to load the login page. Please check if Login.fxml exists in resources folder.");
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