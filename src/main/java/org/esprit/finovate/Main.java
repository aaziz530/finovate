package org.esprit.finovate;

import org.esprit.finovate.entities.User;
import org.esprit.finovate.services.IUserService;
import org.esprit.finovate.services.UserService;
import org.esprit.finovate.utils.Session;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
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
                // ✅ CORRECTION : Session.currentUser -> Session.getCurrentUser()
                System.out.println("Session.currentUser: " +
                        (Session.isActive() ? Session.getCurrentUser().getEmail() : null));
            }

            System.out.println("=== LOGOUT ===");
            userService.logout();
            // ✅ CORRECTION : Session.currentUser -> Session.isActive()
            System.out.println("Session.currentUser after logout: " +
                    (Session.isActive() ? Session.getCurrentUser().getEmail() : null));

        } catch (Exception e) {
            System.out.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}