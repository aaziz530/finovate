package org.esprit.finovate.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.esprit.finovate.dao.TicketDAO;
import org.esprit.finovate.dao.UserDAO;
import org.esprit.finovate.entities.User;
import org.esprit.finovate.model.Ticket;
import org.esprit.finovate.utils.Session;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

public class AdminLayoutController implements Initializable {

    // Top bar
    @FXML private Label lblWelcome;

    // Stats
    @FXML private Label lblTotalUsers;
    @FXML private Label lblOpenTickets;
    @FXML private Label lblInProgressTickets;
    @FXML private Label lblResolvedTickets;
    @FXML private Label lblHighPriorityTickets;

    // Charts
    @FXML private BarChart<String, Number> ticketsByStatusChart;
    @FXML private PieChart openVsClosedChart;

    // Tab Tickets
    @FXML private TableView<Ticket>              latestTicketsTable;
    @FXML private TableColumn<Ticket, String>    colLatestType;
    @FXML private TableColumn<Ticket, String>    colLatestPriorite;
    @FXML private TableColumn<Ticket, String>    colLatestStatut;
    @FXML private TableColumn<Ticket, Timestamp> colLatestDate;
    @FXML private TableColumn<Ticket, Void>      colLatestActions;

    // Tab Agents
    @FXML private TableView<AdminAgentsController.AgentRow>            agentsTable;
    @FXML private TableColumn<AdminAgentsController.AgentRow, String>  colAgentNom;
    @FXML private TableColumn<AdminAgentsController.AgentRow, Number>  colTicketsAssignes;
    @FXML private TableColumn<AdminAgentsController.AgentRow, Number>  colTicketsOuverts;
    @FXML private TableColumn<AdminAgentsController.AgentRow, Number>  colTicketsResolus;
    @FXML private TableColumn<AdminAgentsController.AgentRow, String>  colPerformance;

    private final UserDAO   userDAO   = new UserDAO();
    private final TicketDAO ticketDAO = new TicketDAO();
    private final ObservableList<Ticket> ticketList = FXCollections.observableArrayList();
    private final ObservableList<AdminAgentsController.AgentRow> agentRows = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (Session.currentUser != null)
            lblWelcome.setText("Welcome, " + Session.currentUser.getFirstName());
        setupTicketsTable();
        setupAgentsTable();
        loadAll();
    }

    private void setupTicketsTable() {
        colLatestType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType()));
        colLatestPriorite.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPriorite()));
        colLatestStatut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatut()));

        // Date formatée
        colLatestDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colLatestDate.setCellFactory(col -> new TableCell<>() {
            private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            @Override
            protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "—" : fmt.format(item));
            }
        });

        // Statut coloré
        colLatestStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item.toUpperCase()) {
                    case "NOUVEAU"  -> setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");
                    case "EN_COURS" -> setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    case "RESOLU"   -> setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                    default         -> setStyle("-fx-text-fill: #64748b;");
                }
            }
        });

        // Priorité colorée
        colLatestPriorite.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item.toUpperCase()) {
                    case "HAUTE", "HIGH" -> setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    case "MOYENNE"       -> setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    case "BASSE", "LOW"  -> setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                    default              -> setStyle("-fx-text-fill: #64748b;");
                }
            }
        });

        // Boutons Actions
        colLatestActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit   = new Button("✏ Modifier");
            private final Button btnDelete = new Button("🗑 Supprimer");
            {
                btnEdit.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8;" +
                        "-fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                btnDelete.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c;" +
                        "-fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                btnEdit.setOnAction(e -> {
                    Ticket t = getTableView().getItems().get(getIndex());
                    if (t != null && t.getId() != null) openEditTicket(t.getId());
                });
                btnDelete.setOnAction(e -> {
                    Ticket t = getTableView().getItems().get(getIndex());
                    if (t != null && t.getId() != null) deleteTicket(t.getId());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(6, btnEdit, btnDelete));
            }
        });

        latestTicketsTable.setItems(ticketList);
    }

    private void setupAgentsTable() {
        colAgentNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colTicketsAssignes.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getTicketsAssignes()));
        colTicketsOuverts.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getTicketsOuverts()));
        colTicketsResolus.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getTicketsResolus()));
        colPerformance.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPerformance()));

        agentRows.setAll(
                new AdminAgentsController.AgentRow("Agent 1", 12, 3, 9,  "⭐⭐⭐⭐⭐ Excellent"),
                new AdminAgentsController.AgentRow("Agent 2", 8,  5, 3,  "⭐⭐⭐ Moyen"),
                new AdminAgentsController.AgentRow("Agent 3", 5,  1, 4,  "⭐⭐⭐⭐ Très bon")
        );
        agentsTable.setItems(agentRows);
    }

    private void loadAll() {
        List<Ticket> all   = ticketDAO.findAll();
        List<User>   users = userDAO.findAll();

        long open         = all.stream().filter(t -> "NOUVEAU".equalsIgnoreCase(t.getStatut())).count();
        long inProgress   = all.stream().filter(t -> "EN_COURS".equalsIgnoreCase(t.getStatut())).count();
        long resolved     = all.stream().filter(t -> "RESOLU".equalsIgnoreCase(t.getStatut())).count();
        long highPriority = all.stream().filter(t -> "HAUTE".equalsIgnoreCase(t.getPriorite())
                || "HIGH".equalsIgnoreCase(t.getPriorite())).count();

        lblTotalUsers.setText(String.valueOf(users.size()));
        lblOpenTickets.setText(String.valueOf(open));
        lblInProgressTickets.setText(String.valueOf(inProgress));
        lblResolvedTickets.setText(String.valueOf(resolved));
        lblHighPriorityTickets.setText(String.valueOf(highPriority));

        // BarChart
        ticketsByStatusChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tickets");
        series.getData().add(new XYChart.Data<>("Ouverts",    open));
        series.getData().add(new XYChart.Data<>("En cours",   inProgress));
        series.getData().add(new XYChart.Data<>("Résolus",    resolved));
        series.getData().add(new XYChart.Data<>("Priorité ↑", highPriority));
        ticketsByStatusChart.getData().add(series);

        // PieChart
        openVsClosedChart.getData().clear();
        long opened = open + inProgress;
        if (opened   > 0) openVsClosedChart.getData().add(new PieChart.Data("Ouverts ("  + opened   + ")", opened));
        if (resolved  > 0) openVsClosedChart.getData().add(new PieChart.Data("Fermés ("  + resolved  + ")", resolved));

        // Tableau
        ticketList.setAll(all);
    }

    @FXML
    private void onAddTicket() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ticket-create.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Nouveau Ticket");
            stage.setScene(new Scene(root, 700, 500));
            stage.setOnHidden(e -> loadAll());
            stage.show();
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + ex.getMessage());
        }
    }

    private void openEditTicket(Long ticketId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ticket-detail.fxml"));
            Parent root = loader.load();
            TicketDetailController ctrl = loader.getController();
            ctrl.loadTicket(ticketId);
            Stage stage = new Stage();
            stage.setTitle("Modifier le ticket #" + ticketId);
            stage.setScene(new Scene(root, 900, 600));
            stage.setOnHidden(e -> loadAll());
            stage.show();
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'édition: " + ex.getMessage());
        }
    }

    private void deleteTicket(Long ticketId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer le ticket #" + ticketId + " ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (ticketDAO.delete(ticketId)) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Ticket supprimé.");
                    loadAll();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer.");
                }
            }
        });
    }

    @FXML
    private void onLogout() {
        Session.currentUser = null;
        javafx.application.Platform.exit();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}