package org.esprit.finovate.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class App extends Application {

    private static final String VIEW_PATH = "ticket-view.fxml";

    @Override
    public void start(Stage stage) throws IOException {
        URL fxml = getClass().getResource("/" + VIEW_PATH);
        if (fxml == null) {
            throw new IOException("FXML non trouvé. Vérifiez que src/main/resources/" + VIEW_PATH + " existe.");
        }
        Parent root = FXMLLoader.load(fxml);
        Scene scene = new Scene(root, 700, 500);
        stage.setTitle("Finovate - Gestion des réclamations");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
