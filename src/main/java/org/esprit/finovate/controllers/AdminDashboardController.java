package org.esprit.finovate.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.esprit.finovate.models.Investissement;
import org.esprit.finovate.models.Project;
import org.esprit.finovate.utils.PdfExportUtil;
import org.esprit.finovate.utils.SceneUtils;
import org.esprit.finovate.utils.Session;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class AdminDashboardController implements Initializable {

    private static final int PROJECTS_PER_PAGE = 3;
    private static final int INVEST_PER_PAGE = 5;

    @FXML private TableView<Project> tableProjects;
    @FXML private TableView<Investissement> tableInvestissements;
    @FXML private TextField txtSearchProjects;
    @FXML private TextField txtSearchInvest;
    @FXML private ComboBox<String> comboSortProjects;
    @FXML private ComboBox<String> comboSortInvest;
    @FXML private Label lblProjectsPage;
    @FXML private Label lblInvestPage;

    private Stage stage;
    private final ProjectController projectController = new ProjectController();
    private final InvestissementController investissementController = new InvestissementController();

    private List<Project> allProjects = new ArrayList<>();
    private List<Investissement> allInvestissements = new ArrayList<>();
    private int projectsPage = 1;
    private int investPage = 1;
    private int totalProjectsPages = 1;
    private int totalInvestPages = 1;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupProjectsTable();
        setupInvestissementsTable();
        comboSortProjects.setItems(FXCollections.observableArrayList("Titre A-Z", "Titre Z-A", "Montant ↑", "Montant ↓", "Date récent", "Date ancien"));
        comboSortInvest.setItems(FXCollections.observableArrayList("Montant ↑", "Montant ↓", "Date récent", "Date ancien", "Status"));

        txtSearchProjects.textProperty().addListener((o, ov, nv) -> applyFiltersAndRender());
        txtSearchInvest.textProperty().addListener((o, ov, nv) -> applyFiltersAndRender());
        comboSortProjects.valueProperty().addListener((o, ov, nv) -> applyFiltersAndRender());
        comboSortInvest.valueProperty().addListener((o, ov, nv) -> applyFiltersAndRender());

        loadData();
    }

    private void setupProjectsTable() {
        tableProjects.getColumns().clear();
        TableColumn<Project, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("project_id"));
        colId.setPrefWidth(50);
        TableColumn<Project, String> colTitle = new TableColumn<>("Title");
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTitle.setPrefWidth(180);
        TableColumn<Project, Long> colOwner = new TableColumn<>("Owner");
        colOwner.setCellValueFactory(new PropertyValueFactory<>("owner_id"));
        colOwner.setPrefWidth(60);
        TableColumn<Project, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setPrefWidth(70);
        TableColumn<Project, Double> colGoal = new TableColumn<>("Goal");
        colGoal.setCellValueFactory(new PropertyValueFactory<>("goal_amount"));
        colGoal.setPrefWidth(70);
        TableColumn<Project, Double> colCurrent = new TableColumn<>("Current");
        colCurrent.setCellValueFactory(new PropertyValueFactory<>("current_amount"));
        colCurrent.setPrefWidth(70);
        tableProjects.getColumns().addAll(colId, colTitle, colOwner, colStatus, colGoal, colCurrent);
    }

    private void setupInvestissementsTable() {
        tableInvestissements.getColumns().clear();
        TableColumn<Investissement, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("investissement_id"));
        colId.setPrefWidth(45);
        TableColumn<Investissement, Long> colProject = new TableColumn<>("Project");
        colProject.setCellValueFactory(new PropertyValueFactory<>("project_id"));
        colProject.setPrefWidth(70);
        TableColumn<Investissement, Long> colInvestor = new TableColumn<>("Investor");
        colInvestor.setCellValueFactory(new PropertyValueFactory<>("investor_id"));
        colInvestor.setPrefWidth(70);
        TableColumn<Investissement, Double> colAmount = new TableColumn<>("Amount");
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setPrefWidth(80);
        TableColumn<Investissement, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setPrefWidth(85);
        tableInvestissements.getColumns().addAll(colId, colProject, colInvestor, colAmount, colStatus);
    }

    private void loadData() {
        try {
            allProjects = projectController.getAllProjects();
            allInvestissements = investissementController.getAllInvestissements();
            projectsPage = 1;
            investPage = 1;
            applyFiltersAndRender();
        } catch (SQLException e) {
            showError("Error loading data: " + e.getMessage());
        }
    }

    private void applyFiltersAndRender() {
        String searchP = txtSearchProjects != null && txtSearchProjects.getText() != null ? txtSearchProjects.getText().trim().toLowerCase() : "";
        String searchI = txtSearchInvest != null && txtSearchInvest.getText() != null ? txtSearchInvest.getText().trim().toLowerCase() : "";
        String sortP = comboSortProjects != null && comboSortProjects.getValue() != null ? comboSortProjects.getValue() : "";
        String sortI = comboSortInvest != null && comboSortInvest.getValue() != null ? comboSortInvest.getValue() : "";

        List<Project> filteredP = allProjects.stream()
                .filter(p -> searchP.isEmpty() || (p.getTitle() != null && p.getTitle().toLowerCase().contains(searchP))
                        || (p.getDescription() != null && p.getDescription().toLowerCase().contains(searchP))
                        || String.valueOf(p.getProject_id()).contains(searchP)
                        || String.valueOf(p.getOwner_id()).contains(searchP))
                .sorted(projectComparator(sortP))
                .collect(Collectors.toList());

        List<Investissement> filteredI = allInvestissements.stream()
                .filter(inv -> searchI.isEmpty()
                        || String.valueOf(inv.getInvestissement_id()).contains(searchI)
                        || String.valueOf(inv.getProject_id()).contains(searchI)
                        || String.valueOf(inv.getInvestor_id()).contains(searchI)
                        || String.valueOf(inv.getAmount()).contains(searchI)
                        || (inv.getStatus() != null && inv.getStatus().toLowerCase().contains(searchI)))
                .sorted(investComparator(sortI))
                .collect(Collectors.toList());

        totalProjectsPages = Math.max(1, (int) Math.ceil((double) filteredP.size() / PROJECTS_PER_PAGE));
        totalInvestPages = Math.max(1, (int) Math.ceil((double) filteredI.size() / INVEST_PER_PAGE));
        projectsPage = Math.min(Math.max(1, projectsPage), totalProjectsPages);
        investPage = Math.min(Math.max(1, investPage), totalInvestPages);

        int fromP = (projectsPage - 1) * PROJECTS_PER_PAGE;
        int toP = Math.min(fromP + PROJECTS_PER_PAGE, filteredP.size());
        int fromI = (investPage - 1) * INVEST_PER_PAGE;
        int toI = Math.min(fromI + INVEST_PER_PAGE, filteredI.size());

        tableProjects.getItems().setAll(filteredP.subList(fromP, toP));
        tableInvestissements.getItems().setAll(filteredI.subList(fromI, toI));

        if (lblProjectsPage != null) lblProjectsPage.setText("Page " + projectsPage + " / " + totalProjectsPages + " (" + filteredP.size() + " total)");
        if (lblInvestPage != null) lblInvestPage.setText("Page " + investPage + " / " + totalInvestPages + " (" + filteredI.size() + " total)");
    }

    private Comparator<Project> projectComparator(String sort) {
        return switch (sort) {
            case "Titre A-Z" -> Comparator.comparing(p -> p.getTitle() != null ? p.getTitle().toLowerCase() : "", String.CASE_INSENSITIVE_ORDER);
            case "Titre Z-A" -> Comparator.comparing(p -> p.getTitle() != null ? p.getTitle().toLowerCase() : "", String.CASE_INSENSITIVE_ORDER.reversed());
            case "Montant ↑" -> Comparator.comparingDouble(Project::getCurrent_amount);
            case "Montant ↓" -> Comparator.comparingDouble(Project::getCurrent_amount).reversed();
            case "Date récent" -> Comparator.comparing(p -> p.getCreated_at() != null ? p.getCreated_at() : new Date(0), Comparator.nullsLast(Comparator.reverseOrder()));
            case "Date ancien" -> Comparator.comparing(p -> p.getCreated_at() != null ? p.getCreated_at() : new Date(0), Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(p -> p.getCreated_at() != null ? p.getCreated_at() : new Date(0), Comparator.nullsLast(Comparator.reverseOrder()));
        };
    }

    private Comparator<Investissement> investComparator(String sort) {
        return switch (sort) {
            case "Montant ↑" -> Comparator.comparingDouble(Investissement::getAmount);
            case "Montant ↓" -> Comparator.comparingDouble(Investissement::getAmount).reversed();
            case "Date récent" -> Comparator.comparing(inv -> inv.getInvestment_date() != null ? inv.getInvestment_date() : new Date(0), Comparator.nullsLast(Comparator.reverseOrder()));
            case "Date ancien" -> Comparator.comparing(inv -> inv.getInvestment_date() != null ? inv.getInvestment_date() : new Date(0), Comparator.nullsLast(Comparator.naturalOrder()));
            case "Status" -> Comparator.comparing(inv -> inv.getStatus() != null ? inv.getStatus() : "");
            default -> Comparator.comparing(inv -> inv.getInvestment_date() != null ? inv.getInvestment_date() : new Date(0), Comparator.nullsLast(Comparator.reverseOrder()));
        };
    }

    @FXML
    private void handleProjectsPrev() {
        if (projectsPage > 1) {
            projectsPage--;
            applyFiltersAndRender();
        }
    }

    @FXML
    private void handleProjectsNext() {
        if (projectsPage < totalProjectsPages) {
            projectsPage++;
            applyFiltersAndRender();
        }
    }

    @FXML
    private void handleInvestPrev() {
        if (investPage > 1) {
            investPage--;
            applyFiltersAndRender();
        }
    }

    @FXML
    private void handleInvestNext() {
        if (investPage < totalInvestPages) {
            investPage++;
            applyFiltersAndRender();
        }
    }

    @FXML
    private void handleExportPdf() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save PDF");
        fc.setInitialFileName("finovate_admin_report.pdf");
        File chosen = fc.showSaveDialog(stage);
        if (chosen == null) return;
        try {
            PdfExportUtil.exportAdminReport(allProjects, allInvestissements, chosen);
            showInfo("PDF exported to: " + chosen.getAbsolutePath());
        } catch (Exception e) {
            showError("PDF export failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddProject() {
        TextInputDialog titleDlg = new TextInputDialog();
        titleDlg.setTitle("Add Project");
        titleDlg.setHeaderText("New Project");
        titleDlg.setContentText("Title:");
        Optional<String> titleOpt = titleDlg.showAndWait();
        if (titleOpt.isEmpty() || titleOpt.get().isBlank()) return;

        TextInputDialog descDlg = new TextInputDialog();
        descDlg.setTitle("Add Project");
        descDlg.setHeaderText("Description");
        descDlg.setContentText("Description:");
        Optional<String> descOpt = descDlg.showAndWait();
        String desc = descOpt.orElse("");

        TextInputDialog goalDlg = new TextInputDialog("10000");
        goalDlg.setTitle("Add Project");
        goalDlg.setHeaderText("Goal Amount (TND)");
        goalDlg.setContentText("Amount:");
        Optional<String> goalOpt = goalDlg.showAndWait();
        if (goalOpt.isEmpty()) return;
        double goal;
        try {
            goal = Double.parseDouble(goalOpt.get().trim());
        } catch (NumberFormatException e) {
            showError("Invalid amount.");
            return;
        }

        TextInputDialog ownerDlg = new TextInputDialog("1");
        ownerDlg.setTitle("Add Project");
        ownerDlg.setHeaderText("Owner ID");
        ownerDlg.setContentText("User ID:");
        Optional<String> ownerOpt = ownerDlg.showAndWait();
        if (ownerOpt.isEmpty()) return;
        long ownerId;
        try {
            ownerId = Long.parseLong(ownerOpt.get().trim());
        } catch (NumberFormatException e) {
            showError("Invalid owner ID.");
            return;
        }

        try {
            Project p = new Project();
            p.setTitle(titleOpt.get().trim());
            p.setDescription(desc);
            p.setGoal_amount(goal);
            p.setCurrent_amount(0);
            p.setCreated_at(new Date());
            p.setDeadline(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000));
            p.setStatus("OPEN");
            p.setOwner_id(ownerId);
            projectController.addProjectAsAdmin(p);
            loadData();
            showInfo("Project added.");
        } catch (Exception e) {
            showError("Failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditProject() {
        Project p = tableProjects.getSelectionModel().getSelectedItem();
        if (p == null) {
            showError("Select a project first.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_project.fxml"));
            Parent root = loader.load();
            EditProjectController ctrl = loader.getController();
            ctrl.setStage(stage);
            ctrl.setProject(p);
            ctrl.setReturnToMyProjects(null);
            ctrl.setAdminReturnCallback(() -> {
                loadData();
                goToAdminDashboard();
            });
            stage.setScene(new Scene(root));
            stage.setTitle("Finovate - Edit Project (Admin)");
            SceneUtils.applyStageSize(stage);
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteProject() {
        Project p = tableProjects.getSelectionModel().getSelectedItem();
        if (p == null) {
            showError("Select a project first.");
            return;
        }
        if (new Alert(Alert.AlertType.CONFIRMATION, "Delete \"" + p.getTitle() + "\"?", ButtonType.YES, ButtonType.NO).showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
        try {
            projectController.deleteProject(p.getProject_id());
            loadData();
            showInfo("Project deleted.");
        } catch (Exception e) {
            showError("Failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddInvestment() {
        TextInputDialog projectDlg = new TextInputDialog();
        projectDlg.setTitle("Add Investment");
        projectDlg.setHeaderText("Project ID");
        projectDlg.setContentText("Project ID:");
        Optional<String> projectOpt = projectDlg.showAndWait();
        if (projectOpt.isEmpty()) return;
        long projectId;
        try {
            projectId = Long.parseLong(projectOpt.get().trim());
        } catch (NumberFormatException e) {
            showError("Invalid project ID.");
            return;
        }

        TextInputDialog investorDlg = new TextInputDialog();
        investorDlg.setTitle("Add Investment");
        investorDlg.setHeaderText("Investor ID");
        investorDlg.setContentText("Investor ID:");
        Optional<String> investorOpt = investorDlg.showAndWait();
        if (investorOpt.isEmpty()) return;
        long investorId;
        try {
            investorId = Long.parseLong(investorOpt.get().trim());
        } catch (NumberFormatException e) {
            showError("Invalid investor ID.");
            return;
        }

        TextInputDialog amountDlg = new TextInputDialog("100");
        amountDlg.setTitle("Add Investment");
        amountDlg.setHeaderText("Amount (TND)");
        amountDlg.setContentText("Amount:");
        Optional<String> amountOpt = amountDlg.showAndWait();
        if (amountOpt.isEmpty()) return;
        double amount;
        try {
            amount = Double.parseDouble(amountOpt.get().trim());
        } catch (NumberFormatException e) {
            showError("Invalid amount.");
            return;
        }

        ChoiceDialog<String> statusDlg = new ChoiceDialog<>("PENDING", "PENDING", "CONFIRMED", "DECLINED");
        statusDlg.setTitle("Add Investment");
        statusDlg.setHeaderText("Status");
        statusDlg.setContentText("Status:");
        Optional<String> statusOpt = statusDlg.showAndWait();
        if (statusOpt.isEmpty()) return;

        try {
            Investissement inv = new Investissement();
            inv.setProject_id(projectId);
            inv.setInvestor_id(investorId);
            inv.setAmount(amount);
            inv.setStatus(statusOpt.get());
            inv.setInvestment_date(new Date());
            investissementController.addInvestissementAsAdmin(inv);
            loadData();
            showInfo("Investment added.");
        } catch (Exception e) {
            showError("Failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditInvestment() {
        Investissement inv = tableInvestissements.getSelectionModel().getSelectedItem();
        if (inv == null) {
            showError("Select an investment first.");
            return;
        }
        TextInputDialog amountDlg = new TextInputDialog(String.valueOf(inv.getAmount()));
        amountDlg.setTitle("Edit Investment");
        amountDlg.setHeaderText("Amount (TND)");
        amountDlg.setContentText("Amount:");
        Optional<String> amountOpt = amountDlg.showAndWait();
        if (amountOpt.isEmpty()) return;
        double amount;
        try {
            amount = Double.parseDouble(amountOpt.get().trim());
        } catch (NumberFormatException e) {
            showError("Invalid amount.");
            return;
        }

        ChoiceDialog<String> statusDlg = new ChoiceDialog<>(inv.getStatus(), "PENDING", "CONFIRMED", "DECLINED");
        statusDlg.setTitle("Edit Investment");
        statusDlg.setHeaderText("Status");
        statusDlg.setContentText("Status:");
        Optional<String> statusOpt = statusDlg.showAndWait();
        if (statusOpt.isEmpty()) return;

        try {
            inv.setAmount(amount);
            inv.setStatus(statusOpt.get());
            investissementController.updateInvestissementAsAdmin(inv);
            loadData();
            showInfo("Investment updated.");
        } catch (Exception e) {
            showError("Failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteInvestment() {
        Investissement inv = tableInvestissements.getSelectionModel().getSelectedItem();
        if (inv == null) {
            showError("Select an investment first.");
            return;
        }
        if (new Alert(Alert.AlertType.CONFIRMATION, "Delete this investment?", ButtonType.YES, ButtonType.NO).showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
        try {
            investissementController.deleteInvestissement(inv.getInvestissement_id());
            loadData();
            showInfo("Investment deleted.");
        } catch (Exception e) {
            showError("Failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToDashboard() throws IOException {
        goToDashboard();
    }

    @FXML
    private void handleSignOut() throws IOException {
        Session.currentUser = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/logged_out.fxml"));
        Parent root = loader.load();
        LoggedOutController ctrl = loader.getController();
        ctrl.setStage(stage);
        stage.setScene(new Scene(root));
        stage.setTitle("Finovate - Logged out");
        SceneUtils.applyStageSize(stage);
    }

    private void goToDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
        Parent root = loader.load();
        DashboardController ctrl = loader.getController();
        ctrl.setStage(stage);
        ctrl.refresh();
        stage.setScene(new Scene(root));
        stage.setTitle("Finovate - Dashboard");
        SceneUtils.applyStageSize(stage);
    }

    private void goToAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_dashboard.fxml"));
            Parent root = loader.load();
            AdminDashboardController ctrl = loader.getController();
            ctrl.setStage(stage);
            stage.setScene(new Scene(root));
            stage.setTitle("Finovate - Admin Dashboard");
            SceneUtils.applyStageSize(stage);
        } catch (IOException e) {
            showError("Error: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
}
