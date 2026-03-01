package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.esprit.finovate.dao.TicketDAO;
import org.esprit.finovate.model.Ticket;

import java.net.URL;
import java.util.ResourceBundle;

public class TicketDetailController implements Initializable {

    @FXML
    private TextField titreField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField clientField;
    @FXML
    private ComboBox<String> categorieCombo;
    @FXML
    private ComboBox<String> prioriteCombo;
    @FXML
    private ComboBox<String> statutCombo;
    @FXML
    private ComboBox<String> agentCombo;

    @FXML
    private ListView<String> conversationList;
    @FXML
    private TextArea reponseArea;

    @FXML
    private TableView<String> historyTable;
    @FXML
    private TableColumn<String, String> colHistoryDate;
    @FXML
    private TableColumn<String, String> colHistoryAction;

    private TicketDAO ticketDAO = new TicketDAO();
    private Ticket currentTicket;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Remplir quelques valeurs par défaut (placeholder)
        categorieCombo.getItems().addAll("Technique", "Facturation", "Compte", "Autre");
        prioriteCombo.getItems().addAll("BASSE", "MOYENNE", "HAUTE");
        statutCombo.getItems().addAll("NOUVEAU", "EN_COURS", "RESOLU", "FERME");
        agentCombo.getItems().addAll("Agent 1", "Agent 2", "Agent 3");
    }

    public void loadTicket(Long ticketId) {
        if (ticketId == null) return;
        
        currentTicket = ticketDAO.findById(ticketId);
        if (currentTicket != null) {
            titreField.setText(currentTicket.getType() != null ? currentTicket.getType() : "");
            descriptionArea.setText(currentTicket.getDescription() != null ? currentTicket.getDescription() : "");
            clientField.setText("Client X"); // placeholder
            
            if (currentTicket.getPriorite() != null) {
                prioriteCombo.setValue(currentTicket.getPriorite());
            }
            if (currentTicket.getStatut() != null) {
                statutCombo.setValue(currentTicket.getStatut());
            }
            
            // Load initial conversation with ticket description
            if (currentTicket.getDescription() != null && !currentTicket.getDescription().isEmpty()) {
                conversationList.getItems().clear();
                conversationList.getItems().add("Client: " + currentTicket.getDescription());
            }
        }
    }

    @FXML
    private void onSaveChanges() {
        if (currentTicket == null) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Aucun ticket chargé.");
            return;
        }
        
        // Update ticket from form
        currentTicket.setType(titreField.getText());
        currentTicket.setDescription(descriptionArea.getText());
        if (prioriteCombo.getValue() != null) {
            currentTicket.setPriorite(prioriteCombo.getValue());
        }
        if (statutCombo.getValue() != null) {
            currentTicket.setStatut(statutCombo.getValue());
        }
        
        if (ticketDAO.update(currentTicket)) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Ticket mis à jour avec succès.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de mettre à jour le ticket.");
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onAttachFile() {
        // TODO: gestion des pièces jointes
        System.out.println("Attach file (placeholder)");
    }

    @FXML
    private void onSendReply() {
        String reply = reponseArea.getText();
        if (reply != null && !reply.isBlank()) {
            conversationList.getItems().add("Support: " + reply);
            reponseArea.clear();
        }
    }
}

