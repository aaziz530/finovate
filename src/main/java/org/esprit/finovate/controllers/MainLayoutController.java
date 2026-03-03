package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.esprit.finovate.utils.Session; // ← ajout

import java.io.IOException;

public class MainLayoutController {

    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnTickets;
    @FXML
    private Button btnCreateTicket;
    @FXML
    private Button btnAdmin;
    @FXML
    private Button btnMessagerie;
    @FXML
    private Label lblUserName;
    @FXML
    private StackPane contentPane;

    @FXML
    public void initialize() {

        // ── Affiche le nom de l'utilisateur ──────────────────
        if (Session.isActive()) {
            lblUserName.setText(Session.getCurrentUser().getFirstName()); // ← ajout
        }

        // ── Cache le bouton Admin si pas ADMIN ────────────────
        btnAdmin.setVisible(Session.isAdmin()); // ← ajout
        btnAdmin.setManaged(Session.isAdmin()); // ← ajout

        // ── Vue par défaut — ton code intact ──────────────────
        showTickets();
    }

    // ── Toutes tes méthodes existantes — RIEN CHANGE ─────────

    @FXML
    private void showDashboard() {
        loadView("/dashboard.fxml");
    }

    @FXML
    private void showTickets() {
        loadView("/tickets-list.fxml");
    }

    @FXML
    private void showCreateTicket() {
        loadView("/ticket-create.fxml");
    }

    @FXML
    private void showMessagerie() {
        loadView("/messagerie.fxml");
    }

    @FXML
    private void showAdminDashboard() {
        if (!Session.isAdmin()) {
            System.out.println("[SÉCURITÉ] Accès refusé.");
            return;
        }
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/admin-layout.fxml"));
            btnAdmin.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur chargement admin-layout");
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Erreur chargement vue : " + fxmlPath);
            e.printStackTrace();
        }
    }
}