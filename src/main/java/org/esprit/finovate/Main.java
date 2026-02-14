package org.esprit.finovate;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.esprit.finovate.entities.User;
import org.esprit.finovate.services.IUserService;
import org.esprit.finovate.services.UserService;
import org.esprit.finovate.utils.Session;

import java.io.IOException;

public class Main extends Application {
    /*public static void main(String[] args) {
        IUserService userService = new UserService();

        try {
//            System.out.println("=== REGISTER ===");
//            User created = userService.register("test@finovate.com", "test123", "Test", "User", java.sql.Date.valueOf(LocalDate.of(2003, 11, 30)));
//            System.out.println("User created: id=" + created.getId() + " email=" + created.getEmail());
//            System.out.println("Defaults: points=" + created.getPoints() + " solde=" + created.getSolde() + " carte=" + created.getNumeroCarte());

            System.out.println("=== LOGIN ===");
            User logged = userService.login("dev@finovate.tn", "dev123");
            if (logged == null) {
                System.out.println("Login failed");
            } else {
                System.out.println("Login OK: " + logged.getEmail());
                System.out.println("Session.currentUser: " + (Session.currentUser != null ? Session.currentUser.getEmail() : null));
            }

            System.out.println("=== LOGOUT ===");
            userService.logout();
            System.out.println("Session.currentUser after logout: " + Session.currentUser);

        } catch (Exception e) {
            System.out.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    }*/



    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}