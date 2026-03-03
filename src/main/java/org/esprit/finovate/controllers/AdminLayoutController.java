package org.esprit.finovate.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.esprit.finovate.dao.TicketDAO;
import org.esprit.finovate.dao.UserDAO;
import org.esprit.finovate.entities.User;
import org.esprit.finovate.model.Ticket;
import org.esprit.finovate.utils.Session;

import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class AdminLayoutController implements Initializable {

    // --- Sidebar & Views ---
    @FXML
    private Label lblWelcome;
    @FXML
    private Button menuDashboard;
    @FXML
    private Button menuTickets;

    @FXML
    private ScrollPane viewDashboard;
    @FXML
    private VBox viewTickets;
    @FXML
    private VBox viewAgents;

    // --- Quick Actions ---
    @FXML
    private Button btnQuickAdd;
    @FXML
    private Button btnQuickEdit;
    @FXML
    private Button btnQuickDelete;

    // --- Dashboard Stats ---
    @FXML
    private Label lblTotalUsers;
    @FXML
    private Label lblOpenTickets;
    @FXML
    private Label lblInProgressTickets;
    @FXML
    private Label lblResolvedTickets;
    @FXML
    private Label lblHighPriorityTickets;
    @FXML
    private BarChart<String, Number> ticketsByStatusChart;
    @FXML
    private PieChart openVsClosedChart;

    // --- Tickets View ---
    @FXML
    private TableView<Ticket> latestTicketsTable;
    @FXML
    private TableColumn<Ticket, String> colLatestType;
    @FXML
    private TableColumn<Ticket, String> colLatestPriorite;
    @FXML
    private TableColumn<Ticket, String> colLatestStatut;
    @FXML
    private TableColumn<Ticket, Timestamp> colLatestDate;
    @FXML
    private TableColumn<Ticket, String> colLatestDescription;
    @FXML
    private TableColumn<Ticket, Void> colLatestActions;

    // --- Agents View ---
    @FXML
    private TableView<AdminAgentsController.AgentRow> agentsTable;
    @FXML
    private TableColumn<AdminAgentsController.AgentRow, String> colAgentNom;
    @FXML
    private TableColumn<AdminAgentsController.AgentRow, Number> colTicketsAssignes;
    @FXML
    private TableColumn<AdminAgentsController.AgentRow, Number> colTicketsOuverts;
    @FXML
    private TableColumn<AdminAgentsController.AgentRow, Number> colTicketsResolus;
    @FXML
    private TableColumn<AdminAgentsController.AgentRow, String> colPerformance;

    // --- Modals ---
    @FXML
    private StackPane modalAddTicket;
    @FXML
    private ComboBox<String> cmbAddType;
    @FXML
    private ComboBox<String> cmbAddPriority;
    @FXML
    private TextArea txtAddDescription;

    @FXML
    private StackPane modalEditTicket;
    @FXML
    private ComboBox<String> cmbEditType;
    @FXML
    private ComboBox<String> cmbEditPriority;
    @FXML
    private ComboBox<String> cmbEditStatus;
    @FXML
    private TextArea txtEditDescription;

    @FXML
    private StackPane modalDeleteTicket;

    // --- Toast container ---
    @FXML
    private VBox toastContainer;

    // --- Data ---
    private final UserDAO userDAO = new UserDAO();
    private final TicketDAO ticketDAO = new TicketDAO();
    private final ObservableList<Ticket> ticketList = FXCollections.observableArrayList();
    private final ObservableList<AdminAgentsController.AgentRow> agentRows = FXCollections.observableArrayList();

    private Ticket selectedTicket = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (Session.isActive()) {
            lblWelcome.setText("Welcome, " + Session.getCurrentUser().getFirstName());
        }

        setupNavigation();
        setupTicketsTable();
        setupAgentsTable();
        setupModals();

        // Listen for table selection
        latestTicketsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            selectedTicket = newV;
            boolean hasSelection = (newV != null);
            btnQuickEdit.setDisable(!hasSelection);
            btnQuickDelete.setDisable(!hasSelection);
        });

        loadAllData();
        showDashboard(); // default view
    }

    // ================== LAYOUT & NAVIGATION ==================
    private void setupNavigation() {
        // Handled via FXML onAction bindings, but we abstract the styling here
    }

    @FXML
    private void showDashboard() {
        switchView(viewDashboard, menuDashboard);
    }

    @FXML
    private void showTickets() {
        switchView(viewTickets, menuTickets);
    }

    private void switchView(javafx.scene.Node viewToShow, Button activeButton) {
        viewDashboard.setVisible(false);
        viewDashboard.setManaged(false);
        viewTickets.setVisible(false);
        viewTickets.setManaged(false);
        viewAgents.setVisible(false);
        viewAgents.setManaged(false);

        viewToShow.setVisible(true);
        viewToShow.setManaged(true);

        menuDashboard.getStyleClass().remove("sidebar-button-active");
        menuTickets.getStyleClass().remove("sidebar-button-active");

        if (!activeButton.getStyleClass().contains("sidebar-button-active")) {
            activeButton.getStyleClass().add("sidebar-button-active");
        }
    }

    @FXML
    private void onLogout() {
        Session.clear();
        Platform.exit();
    }

    // ================== DATA LOADING ==================
    private void loadAllData() {
        List<Ticket> all = ticketDAO.findAll();
        List<User> users = userDAO.findAll();

        long open = all.stream().filter(t -> "NOUVEAU".equalsIgnoreCase(t.getStatut())).count();
        long inProgress = all.stream().filter(t -> "EN_COURS".equalsIgnoreCase(t.getStatut())).count();
        long resolved = all.stream().filter(t -> "RESOLU".equalsIgnoreCase(t.getStatut())).count();
        long highPriority = all.stream()
                .filter(t -> "HAUTE".equalsIgnoreCase(t.getPriorite()) || "HIGH".equalsIgnoreCase(t.getPriorite()))
                .count();

        lblTotalUsers.setText(String.valueOf(users.size()));
        lblOpenTickets.setText(String.valueOf(open));
        lblInProgressTickets.setText(String.valueOf(inProgress));
        lblResolvedTickets.setText(String.valueOf(resolved));
        lblHighPriorityTickets.setText(String.valueOf(highPriority));

        ticketsByStatusChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tickets");
        series.getData().add(new XYChart.Data<>("Ouverts", open));
        series.getData().add(new XYChart.Data<>("En cours", inProgress));
        series.getData().add(new XYChart.Data<>("Résolus", resolved));
        series.getData().add(new XYChart.Data<>("Priorité", highPriority));
        ticketsByStatusChart.getData().add(series);

        openVsClosedChart.getData().clear();
        long opened = open + inProgress;
        if (opened > 0)
            openVsClosedChart.getData().add(new PieChart.Data("Ouverts (" + opened + ")", opened));
        if (resolved > 0)
            openVsClosedChart.getData().add(new PieChart.Data("Fermés (" + resolved + ")", resolved));

        ticketList.setAll(all);
    }

    // ================== TABLES ==================
    private void setupTicketsTable() {
        colLatestType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType()));
        colLatestPriorite.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPriorite()));
        colLatestStatut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatut()));
        colLatestDescription.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));

        colLatestDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colLatestDate.setCellFactory(col -> new TableCell<>() {
            private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "-" : fmt.format(item));
            }
        });

        colLatestStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                switch (item.toUpperCase()) {
                    case "NOUVEAU":
                        setStyle("-fx-text-fill: #8b5cf6; -fx-font-weight: bold;");
                        break;
                    case "EN_COURS":
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                        break;
                    case "RESOLU":
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                        break;
                    default:
                        setStyle("-fx-text-fill: #94A3B8;");
                        break;
                }
            }
        });

        colLatestPriorite.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                switch (item.toUpperCase()) {
                    case "HAUTE":
                    case "HIGH":
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                        break;
                    case "MOYENNE":
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                        break;
                    case "BASSE":
                    case "LOW":
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                        break;
                    default:
                        setStyle("-fx-text-fill: #94A3B8;");
                        break;
                }
            }
        });

        colLatestActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            {
                btnEdit.getStyleClass().add("btn-edit");
                btnEdit.setStyle("-fx-padding: 4 10; -fx-font-size: 11px;");
                btnDelete.getStyleClass().add("btn-delete");
                btnDelete.setStyle("-fx-padding: 4 10; -fx-font-size: 11px;");

                btnEdit.setOnAction(e -> {
                    Ticket t = getTableView().getItems().get(getIndex());
                    if (t != null) {
                        selectedTicket = t;
                        onEditTicket();
                    }
                });
                btnDelete.setOnAction(e -> {
                    Ticket t = getTableView().getItems().get(getIndex());
                    if (t != null) {
                        selectedTicket = t;
                        onDeleteTicket();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8, btnEdit, btnDelete);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                }
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
                new AdminAgentsController.AgentRow("Sophie Dupont", 12, 3, 9, "Excellent"),
                new AdminAgentsController.AgentRow("Marc Leblanc", 8, 5, 3, "Moyen"),
                new AdminAgentsController.AgentRow("Julie Martin", 5, 1, 4, "Très bon"));
        agentsTable.setItems(agentRows);
    }

    // ================== MODALS ==================
    private void setupModals() {
        cmbAddType.getItems().addAll("BUG", "FEATURE", "SUPPORT", "QUESTION");
        cmbAddPriority.getItems().addAll("BASSE", "MOYENNE", "HAUTE");
        cmbAddType.setValue("SUPPORT");
        cmbAddPriority.setValue("MOYENNE");

        cmbEditType.getItems().addAll("BUG", "FEATURE", "SUPPORT", "QUESTION");
        cmbEditPriority.getItems().addAll("BASSE", "MOYENNE", "HAUTE");
        cmbEditStatus.getItems().addAll("NOUVEAU", "EN_COURS", "RESOLU");
    }

    @FXML
    private void closeModals() {
        modalAddTicket.setVisible(false);
        modalAddTicket.setManaged(false);
        modalEditTicket.setVisible(false);
        modalEditTicket.setManaged(false);
        modalDeleteTicket.setVisible(false);
        modalDeleteTicket.setManaged(false);

        // Reset Add Form
        txtAddDescription.clear();
        cmbAddType.setValue("SUPPORT");
        cmbAddPriority.setValue("MOYENNE");
    }

    @FXML
    private void onAddTicket() {
        closeModals();
        modalAddTicket.setVisible(true);
        modalAddTicket.setManaged(true);
        fadeIn(modalAddTicket);
    }

    @FXML
    private void onEditTicket() {
        if (selectedTicket == null) {
            showToast("Veuillez sélectionner un ticket à modifier.", "error");
            return;
        }
        cmbEditType.setValue(selectedTicket.getType());
        cmbEditPriority.setValue(selectedTicket.getPriorite());
        cmbEditStatus.setValue(selectedTicket.getStatut());
        txtEditDescription.setText(selectedTicket.getDescription());

        closeModals();
        modalEditTicket.setVisible(true);
        modalEditTicket.setManaged(true);
        fadeIn(modalEditTicket);
    }

    @FXML
    private void onDeleteTicket() {
        if (selectedTicket == null) {
            showToast("Veuillez sélectionner un ticket à supprimer.", "error");
            return;
        }
        closeModals();
        modalDeleteTicket.setVisible(true);
        modalDeleteTicket.setManaged(true);
        fadeIn(modalDeleteTicket);
    }

    // ================== CRUD ACTIONS ==================
    @FXML
    private void saveNewTicket() {
        String type = cmbAddType.getValue();
        String priority = cmbAddPriority.getValue();
        String desc = txtAddDescription.getText();

        if (desc == null || desc.trim().isEmpty()) {
            showToast("La description ne peut pas être vide.", "error");
            return;
        }

        Ticket t = new Ticket();
        t.setType(type);
        t.setPriorite(priority);
        t.setDescription(desc);
        t.setStatut("NOUVEAU");
        t.setDateCreation(new Timestamp(new Date().getTime()));

        // t.setCreatorId is not available in Ticket model

        if (ticketDAO.create(t)) {
            closeModals();
            loadAllData();
            showToast("Ticket créé avec succès ! 🚀", "success");
        } else {
            showToast("Erreur lors de la création du ticket.", "error");
        }
    }

    @FXML
    private void updateTicket() {
        if (selectedTicket == null)
            return;

        String desc = txtEditDescription.getText();
        if (desc == null || desc.trim().isEmpty()) {
            showToast("La description ne peut pas être vide.", "error");
            return;
        }

        selectedTicket.setType(cmbEditType.getValue());
        selectedTicket.setPriorite(cmbEditPriority.getValue());
        selectedTicket.setStatut(cmbEditStatus.getValue());
        selectedTicket.setDescription(desc);

        if (ticketDAO.update(selectedTicket)) {
            closeModals();
            loadAllData();
            showToast("Ticket mis à jour avec succès !", "success");
        } else {
            showToast("Erreur lors de la mise à jour.", "error");
        }
    }

    @FXML
    private void confirmDeleteTicket() {
        if (selectedTicket == null)
            return;

        if (ticketDAO.delete(selectedTicket.getId())) {
            closeModals();
            selectedTicket = null;
            btnQuickEdit.setDisable(true);
            btnQuickDelete.setDisable(true);
            loadAllData();
            showToast("Ticket supprimé définitivement.", "success");
        } else {
            showToast("Erreur lors de la suppression.", "error");
            closeModals();
        }
    }

    // ================== ANIMATIONS & TOASTS ==================
    private void fadeIn(javafx.scene.Node node) {
        node.setOpacity(0.0);
        FadeTransition ft = new FadeTransition(Duration.millis(200), node);
        ft.setToValue(1.0);
        ft.play();
    }

    private void showToast(String message, String type) {
        Label lblToast = new Label(message);
        lblToast.getStyleClass().add("toast-label");

        VBox toastBox = new VBox(lblToast);
        toastBox.getStyleClass().add(type.equals("success") ? "toast-success" : "toast-error");
        toastBox.setStyle("-fx-padding: 12 24;");

        toastContainer.getChildren().add(toastBox);

        // Animate
        toastBox.setOpacity(0.0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toastBox);
        fadeIn.setToValue(1.0);

        PauseTransition stay = new PauseTransition(Duration.millis(3000));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toastBox);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> toastContainer.getChildren().remove(toastBox));

        SequentialTransition seq = new SequentialTransition(fadeIn, stay, fadeOut);
        seq.play();
    }
}