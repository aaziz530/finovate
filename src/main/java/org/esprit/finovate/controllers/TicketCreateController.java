package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.esprit.finovate.dao.TicketDAO;
import org.esprit.finovate.model.Ticket;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class TicketCreateController implements Initializable {

    @FXML
    private TextField titreField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private ComboBox<String> categorieCombo;
    @FXML
    private ComboBox<String> prioriteCombo;
    @FXML
    private Label attachedFileLabel;

    private final TicketDAO ticketDAO = new TicketDAO();
    private File attachedFile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        categorieCombo.getItems().addAll("Technique", "Facturation", "Compte", "Autre");
        prioriteCombo.getItems().addAll("LOW", "MEDIUM", "HIGH");
    }

    @FXML
    private void onAttachFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une pièce jointe");
        Window window = titreField.getScene() != null ? titreField.getScene().getWindow() : null;
        File file = chooser.showOpenDialog(window);
        if (file != null) {
            attachedFile = file;
            attachedFileLabel.setText(file.getName());
        }
    }

    @FXML
    private void onCancel() {
        titreField.clear();
        descriptionArea.clear();
        categorieCombo.getSelectionModel().clearSelection();
        prioriteCombo.getSelectionModel().clearSelection();
        attachedFile = null;
        attachedFileLabel.setText("Aucun fichier sélectionné");
    }

    @FXML
    private void onSubmit() {
        String titre = titreField.getText();
        String description = descriptionArea.getText();
        String priorite = prioriteCombo.getValue();

        if (titre == null || titre.isBlank()) {
            // On pourrait afficher une Alert ici, pour l'instant console
            System.out.println("Titre obligatoire");
            return;
        }

        // Analyse de sentiment basique (mots-clés)
        boolean isNegative = false;
        if (description != null) {
            String lowerDesc = description.toLowerCase();
            if (lowerDesc.contains("furious") || lowerDesc.contains("bad service") ||
                    lowerDesc.contains("angry") || lowerDesc.contains("nul") ||
                    lowerDesc.contains("en colère") || lowerDesc.contains("mauvais service")) {
                isNegative = true;
            }
        }

        Ticket t = new Ticket();
        t.setType(titre);

        if (isNegative) {
            t.setDescription("🔥 [URGENT: Sentiment Négatif] " + (description != null ? description : ""));
            t.setPriorite("HIGH"); // Forcer la priorité haute
        } else {
            t.setDescription(description != null ? description : "");
            t.setPriorite(priorite != null ? priorite : "LOW");
        }

        t.setStatut("NOUVEAU");

        if (ticketDAO.create(t)) {
            System.out.println("Ticket créé avec succès. Sentiment négatif: " + isNegative);
            onCancel();
        } else {
            System.out.println("Erreur création ticket (page création).");
        }
    }
}
