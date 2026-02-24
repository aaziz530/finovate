package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger la page de connexion
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();

        // Créer et afficher la scène
        Scene scene = new Scene(root, 1200, 800);

        // Appliquer le CSS moderne
        scene.getStylesheets().add(getClass().getResource("/css/modern-style.css").toExternalForm());

        primaryStage.setTitle("Finovate - Sign In");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args); // Appelle initialize() et MainController.initialize()
    }
}
