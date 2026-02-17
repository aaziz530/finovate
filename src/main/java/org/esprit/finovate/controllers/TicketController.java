package org.esprit.finovate.controllers;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.esprit.finovate.dao.TicketDAO;
import org.esprit.finovate.model.Ticket;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TicketController implements Initializable {

    @FXML
    private TableView<Ticket> ticketTable;
    @FXML
    private TableColumn<Ticket, Number> colId;
    @FXML
    private TableColumn<Ticket, String> colType;
    @FXML
    private TableColumn<Ticket, String> colDescription;
    @FXML
    private TableColumn<Ticket, String> colPriorite;
    @FXML
    private TableColumn<Ticket, String> colStatut;

    @FXML
    private TextField fieldSearch;
    @FXML
    private TextField fieldId;
    @FXML
    private TextField fieldType;
    @FXML
    private TextField fieldDescription;
    @FXML
    private TextField fieldPriorite;
    @FXML
    private TextField fieldStatut;

    private final TicketDAO ticketDAO = new TicketDAO();
    private final ObservableList<Ticket> allTickets = FXCollections.observableArrayList();
    private FilteredList<Ticket> filteredTickets;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadData();
        ticketTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> selectionToForm(newVal));
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(cell -> new SimpleLongProperty(cell.getValue().getId() != null ? cell.getValue().getId() : 0L));
        colType.setCellValueFactory(cell -> new SimpleStringProperty(nullToEmpty(cell.getValue().getType())));
        colDescription.setCellValueFactory(cell -> new SimpleStringProperty(nullToEmpty(cell.getValue().getDescription())));
        colPriorite.setCellValueFactory(cell -> new SimpleStringProperty(nullToEmpty(cell.getValue().getPriorite())));
        colStatut.setCellValueFactory(cell -> new SimpleStringProperty(nullToEmpty(cell.getValue().getStatut())));
    }

    private void loadData() {
        List<Ticket> list = ticketDAO.findAll();
        allTickets.clear();
        allTickets.addAll(list);
        if (filteredTickets == null) {
            filteredTickets = new FilteredList<>(allTickets, p -> true);
            ticketTable.setItems(filteredTickets);
        }
    }

    private void selectionToForm(Ticket ticket) {
        if (ticket == null) {
            clearForm();
            return;
        }
        fieldId.setText(ticket.getId() != null ? ticket.getId().toString() : "");
        fieldType.setText(nullToEmpty(ticket.getType()));
        fieldDescription.setText(nullToEmpty(ticket.getDescription()));
        fieldPriorite.setText(nullToEmpty(ticket.getPriorite()));
        fieldStatut.setText(nullToEmpty(ticket.getStatut()));
    }

    private void clearForm() {
        fieldId.clear();
        fieldType.clear();
        fieldDescription.clear();
        fieldPriorite.clear();
        fieldStatut.clear();
        ticketTable.getSelectionModel().clearSelection();
    }

    private Ticket formToTicket() {
        Ticket t = new Ticket();
        String idStr = fieldId.getText();
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                t.setId(Long.parseLong(idStr.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        t.setType(fieldType.getText());
        t.setDescription(fieldDescription.getText());
        t.setPriorite(fieldPriorite.getText());
        t.setStatut(fieldStatut.getText());
        return t;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onAdd() {
        String type = fieldType.getText();
        String description = fieldDescription.getText();
        String priorite = fieldPriorite.getText();
        String statut = fieldStatut.getText();
        if (type == null || type.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le type est obligatoire.");
            return;
        }
        Ticket t = new Ticket(type, description != null ? description : "", priorite != null ? priorite : "", statut != null ? statut : "");
        if (ticketDAO.create(t)) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Réclamation créée.");
            loadData();
            clearForm();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de créer la réclamation.");
        }
    }

    @FXML
    private void onUpdate() {
        Ticket t = formToTicket();
        if (t.getId() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Sélectionnez une réclamation à modifier (ou saisissez un ID).");
            return;
        }
        if (ticketDAO.update(t)) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Réclamation modifiée.");
            loadData();
            clearForm();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier la réclamation.");
        }
    }

    @FXML
    private void onDelete() {
        String idStr = fieldId.getText();
        if (idStr == null || idStr.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Sélectionnez une réclamation à supprimer.");
            return;
        }
        try {
            Long id = Long.parseLong(idStr.trim());
            if (ticketDAO.delete(id)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Réclamation supprimée.");
                loadData();
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer la réclamation.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "ID invalide.");
        }
    }

    @FXML
    private void onSearch() {
        if (filteredTickets == null) {
            filteredTickets = new FilteredList<>(allTickets, p -> true);
            ticketTable.setItems(filteredTickets);
        }
        String query = fieldSearch.getText();
        if (query == null) query = "";
        final String q = query.trim().toLowerCase();
        if (q.isEmpty()) {
            filteredTickets.setPredicate(p -> true);
        } else {
            filteredTickets.setPredicate(ticket ->
                    (ticket.getType() != null && ticket.getType().toLowerCase().contains(q)) ||
                            (ticket.getDescription() != null && ticket.getDescription().toLowerCase().contains(q)) ||
                            (ticket.getPriorite() != null && ticket.getPriorite().toLowerCase().contains(q)) ||
                            (ticket.getStatut() != null && ticket.getStatut().toLowerCase().contains(q)));
        }
    }

    @FXML
    private void onClear() {
        if (fieldSearch != null) fieldSearch.clear();
        if (filteredTickets != null) filteredTickets.setPredicate(p -> true);
        clearForm();
        loadData();
    }
}
