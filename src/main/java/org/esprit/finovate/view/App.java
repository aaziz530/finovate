package org.esprit.finovate.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.esprit.finovate.utils.DevAccount;  // ← ajout
import org.esprit.finovate.utils.Session;      // ← ajout

import java.io.IOException;
import java.net.URL;

public class App extends Application {

    private static final String MAIN_LAYOUT = "main-layout.fxml";

    @Override
    public void start(Stage stage) throws IOException {

        // 🔧 Simulation session — change ADMIN/USER pour tester
         //DevAccount.createUser();  retourne ton User ADMIN existant
        Session.login(DevAccount.createUser()); // ← ajout

        URL fxml = getClass().getResource("/" + MAIN_LAYOUT);
        if (fxml == null) {
            throw new IOException("FXML non trouvé. Vérifiez que src/main/resources/" + MAIN_LAYOUT + " existe.");
        }
        Parent root = FXMLLoader.load(fxml);
        Scene scene = new Scene(root, 1200, 750);
        stage.setTitle("User Dashboard - Finovate");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}