package org.esprit.finovate.controllers;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.esprit.finovate.controllers.TicketDetailController;
import org.esprit.finovate.dao.TicketDAO;
import org.esprit.finovate.model.Ticket;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class TicketsListController implements Initializable {

    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private ComboBox<String> priorityFilter;
    @FXML
    private DatePicker dateFilter;
    @FXML
    private TextField searchField;

    @FXML
    private TableView<Ticket> ticketsTable;

    @FXML
    private TableColumn<Ticket, String> colTitre;
    @FXML
    private TableColumn<Ticket, String> colDescription;
    @FXML
    private TableColumn<Ticket, String> colClient;
    @FXML
    private TableColumn<Ticket, String> colPriorite;
    @FXML
    private TableColumn<Ticket, String> colStatut;
    @FXML
    private TableColumn<Ticket, String> colDateCreation;
    @FXML
    private TableColumn<Ticket, String> colAssigneA;
    @FXML
    private TableColumn<Ticket, Void> colActions;

    private final TicketDAO ticketDAO = new TicketDAO();
    private final ObservableList<Ticket> allTickets = FXCollections.observableArrayList();
    private FilteredList<Ticket> filteredTickets;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFilters();
        setupTable();
        loadData();
    }

    private void setupFilters() {
        statusFilter.getItems().addAll("Tous", "NOUVEAU", "EN_COURS", "RESOLU", "FERME");
        statusFilter.setValue("Tous");

        priorityFilter.getItems().addAll("Toutes", "HAUTE", "MOYENNE", "BASSE");
        priorityFilter.setValue("Toutes");
    }

    private void setupTable() {

        colTitre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType()));
        colDescription.setCellValueFactory(c -> {
            String desc = c.getValue().getDescription();
            if (desc == null || desc.isEmpty()) {
                return new SimpleStringProperty("");
            }
            // Truncate long descriptions for table display
            String truncated = desc.length() > 50 ? desc.substring(0, 47) + "..." : desc;
            return new SimpleStringProperty(truncated);
        });
        colClient.setCellValueFactory(c -> new SimpleStringProperty("Client X")); // placeholder
        colPriorite.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPriorite()));
        colStatut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatut()));
        colDateCreation.setCellValueFactory(c -> new SimpleStringProperty("N/A"));
        colAssigneA.setCellValueFactory(c -> new SimpleStringProperty("Non assigné"));

        // Badge couleur pour priorité
        colPriorite.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String lower = item.toUpperCase();
                    if (lower.contains("HAUTE") || lower.contains("HIGH")) {
                        setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-font-weight: bold;");
                    } else if (lower.contains("MOYENNE") || lower.contains("MEDIUM")) {
                        setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Badge couleur pour statut
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String lower = item.toUpperCase();
                    if (lower.contains("NOUVEAU") || lower.contains("OPEN")) {
                        setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8; -fx-font-weight: bold;");
                    } else if (lower.contains("EN_COURS") || lower.contains("PENDING")) {
                        setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Colonne actions avec bouton "Voir"
        Callback<TableColumn<Ticket, Void>, TableCell<Ticket, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btn = new Button("Voir");

            {
                btn.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #111827;");
                btn.setOnAction(event -> {
                    Ticket t = getTableView().getItems().get(getIndex());
                    if (t != null && t.getId() != null) {
                        showTicketDetail(t.getId());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        };
        colActions.setCellFactory(cellFactory);
    }

    private void loadData() {
        List<Ticket> list = ticketDAO.findAll();
        allTickets.setAll(list);
        if (filteredTickets == null) {
            filteredTickets = new FilteredList<>(allTickets, t -> true);
            ticketsTable.setItems(filteredTickets);
        }
        applyFilters();
    }

    @FXML
    private void onSearch() {
        applyFilters();
    }

    @FXML
    private void onResetFilters() {
        statusFilter.setValue("Tous");
        priorityFilter.setValue("Toutes");
        dateFilter.setValue((LocalDate) null);
        searchField.clear();
        applyFilters();
    }

    private void applyFilters() {
        String search = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String status = statusFilter.getValue();
        String priority = priorityFilter.getValue();

        filteredTickets.setPredicate(ticket -> {
            if (ticket == null) return false;

            boolean matchesStatus = status == null || "Tous".equals(status) ||
                    (ticket.getStatut() != null && ticket.getStatut().equalsIgnoreCase(status));

            boolean matchesPriority = priority == null || "Toutes".equals(priority) ||
                    (ticket.getPriorite() != null && ticket.getPriorite().equalsIgnoreCase(priority));

            boolean matchesSearch = search.isEmpty() ||
                    (ticket.getType() != null && ticket.getType().toLowerCase().contains(search)) ||
                    (ticket.getDescription() != null && ticket.getDescription().toLowerCase().contains(search)) ||
                    (String.valueOf(ticket.getId()).contains(search));

            return matchesStatus && matchesPriority && matchesSearch;
        });
    }

    private void showTicketDetail(Long ticketId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ticket-detail.fxml"));
            Parent detailView = loader.load();
            TicketDetailController controller = loader.getController();
            controller.loadTicket(ticketId);

            Stage stage = new Stage();
            stage.setTitle("Détail du ticket #" + ticketId);
            stage.setScene(new Scene(detailView, 900, 600));
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible d'ouvrir la page de détail: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }
}

