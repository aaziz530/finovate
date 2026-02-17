package org.esprit.finovate;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class FinovateApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Login is handled by another module. For testing, use stub user ID 1. Replace this with your login when integrating.
        org.esprit.finovate.utils.Session.currentUser = new org.esprit.finovate.utils.StubLoggedInUser(1L);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
        Parent root = loader.load();
        org.esprit.finovate.controllers.DashboardController ctrl = loader.getController();
        ctrl.setStage(stage);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Finovate - Dashboard");
        stage.setMinWidth(1000);
        stage.setMinHeight(600);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
