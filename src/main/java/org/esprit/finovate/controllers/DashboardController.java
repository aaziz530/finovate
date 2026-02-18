package org.esprit.finovate.controllers;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.esprit.finovate.models.Project;
import org.esprit.finovate.utils.ImageUtils;
import org.esprit.finovate.utils.ValidationUtils;
import org.esprit.finovate.utils.SceneUtils;
import org.esprit.finovate.utils.Session;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DashboardController implements Initializable {

    @FXML private VBox projectsContainer;
    @FXML private TextField txtSearchDynamic;
    @FXML private Button btnAdminDashboard;
    @FXML private ComboBox<String> comboStatus;
    @FXML private ComboBox<String> comboTri;

    private Stage stage;
    private List<Project> allProjects = List.of();
    private final ProjectController projectController = new ProjectController();
    private final InvestissementController investissementController = new InvestissementController();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboStatus.setItems(FXCollections.observableArrayList("Tous", "OPEN", "FUNDED", "CLOSED"));
        comboTri.setItems(FXCollections.observableArrayList("Titre (A-Z)", "Titre (Z-A)", "Montant ↑", "Montant ↓", "Date (récent)", "Date (ancien)"));
        ChangeListener<String> dynamicListener = (o, ov, nv) -> applyFiltersAndRender();
        txtSearchDynamic.textProperty().addListener(dynamicListener);
        comboTri.valueProperty().addListener((o, ov, nv) -> applyFiltersAndRender());
        if (btnAdminDashboard != null && Session.currentUser != null && "ADMIN".equals(Session.currentUser.getRole())) {
            btnAdminDashboard.setVisible(true);
            btnAdminDashboard.setManaged(true);
        }
        loadProjects();
    }

    public void refresh() {
        loadProjects();
    }

    private void loadProjects() {
        try {
            allProjects = projectController.getAllProjects();
            applyFiltersAndRender();
        } catch (SQLException e) {
            e.printStackTrace();
            allProjects = List.of();
            showError("Error loading projects.");
        }
    }

    @FXML
    private void handleStaticSearch() {
        applyFiltersAndRender();
    }

    private void applyFiltersAndRender() {
        projectsContainer.getChildren().clear();
        String searchText = txtSearchDynamic != null && txtSearchDynamic.getText() != null
                ? txtSearchDynamic.getText().trim().toLowerCase() : "";
        String statusFilter = comboStatus != null && comboStatus.getValue() != null
                ? comboStatus.getValue() : "Tous";
        String sortOption = comboTri != null && comboTri.getValue() != null
                ? comboTri.getValue() : null;

        List<Project> filtered = allProjects.stream()
                .filter(p -> (searchText.isEmpty() || matchesSearch(p, searchText)))
                .filter(p -> "Tous".equals(statusFilter) || statusFilter.equals(p.getStatus()))
                .collect(Collectors.toList());

        Comparator<Project> cmp = switch (sortOption != null ? sortOption : "") {
            case "Titre (A-Z)" -> Comparator.comparing(p -> p.getTitle() != null ? p.getTitle().toLowerCase() : "", String.CASE_INSENSITIVE_ORDER);
            case "Titre (Z-A)" -> Comparator.comparing(p -> p.getTitle() != null ? p.getTitle().toLowerCase() : "", String.CASE_INSENSITIVE_ORDER.reversed());
            case "Montant ↑" -> Comparator.comparingDouble(Project::getCurrent_amount);
            case "Montant ↓" -> Comparator.comparingDouble(Project::getCurrent_amount).reversed();
            case "Date (récent)" -> Comparator.comparing(p -> p.getCreated_at() != null ? p.getCreated_at() : new java.util.Date(0), Comparator.nullsLast(Comparator.reverseOrder()));
            case "Date (ancien)" -> Comparator.comparing(p -> p.getCreated_at() != null ? p.getCreated_at() : new java.util.Date(0), Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(p -> p.getCreated_at() != null ? p.getCreated_at() : new java.util.Date(0), Comparator.nullsLast(Comparator.reverseOrder()));
        };
        filtered.sort(cmp);

        if (filtered.isEmpty()) {
            Label empty = new Label("No projects match your filters.");
            empty.getStyleClass().add("project-meta");
            projectsContainer.getChildren().add(empty);
        } else {
            for (Project p : filtered) {
                projectsContainer.getChildren().add(createProjectCard(p));
            }
        }
    }

    private boolean matchesSearch(Project p, String search) {
        String t = p.getTitle() != null ? p.getTitle().toLowerCase() : "";
        String d = p.getDescription() != null ? p.getDescription().toLowerCase() : "";
        return t.contains(search) || d.contains(search);
    }

    private void showError(String msg) {
        Label err = new Label(msg);
        err.getStyleClass().add("error-label");
        projectsContainer.getChildren().add(err);
    }

    private VBox createProjectCard(Project p) {
        VBox card = new VBox(10);
        card.getStyleClass().add("project-card");
        card.setPrefWidth(800);

        String resolved = ImageUtils.resolveImagePath(p.getImagePath());
        if (resolved != null) {
            try {
                ImageView iv = new ImageView(new Image("file:" + resolved));
                iv.setFitWidth(120);
                iv.setFitHeight(80);
                iv.setPreserveRatio(true);
                card.getChildren().add(iv);
            } catch (Exception ignored) {}
        }

        Label title = new Label(p.getTitle());
        title.getStyleClass().add("project-title");
        title.setWrapText(true);
        card.getChildren().add(title);

        String desc = p.getDescription();
        if (desc != null && !desc.isEmpty()) {
            String shortDesc = desc.length() > 120 ? desc.substring(0, 120) + "..." : desc;
            Label descLabel = new Label(shortDesc);
            descLabel.getStyleClass().add("project-desc");
            descLabel.setWrapText(true);
            card.getChildren().add(descLabel);
        }

        double progress = p.getGoal_amount() > 0 ? Math.min(1.0, p.getCurrent_amount() / p.getGoal_amount()) : 0;
        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.getStyleClass().add("project-progress-bar");
        progressBar.setMaxWidth(Double.MAX_VALUE);

        Label meta = new Label(String.format("%.2f TND / %.2f TND  •  %s", p.getCurrent_amount(), p.getGoal_amount(), p.getStatus()));
        meta.getStyleClass().add("project-meta");

        card.getChildren().add(progressBar);
        card.getChildren().add(meta);

        double remaining = p.getGoal_amount() - p.getCurrent_amount();
        boolean canInvest = Session.currentUser != null
                && (p.getOwner_id() == null || !p.getOwner_id().equals(Session.currentUser.getId()))
                && "OPEN".equals(p.getStatus())
                && remaining > 0;

        if (canInvest) {
            TextField txtAmount = new TextField();
            txtAmount.setPromptText("Amount (TND)...");
            txtAmount.setPrefWidth(120);
            Button btnInvest = new Button("Invest");
            btnInvest.getStyleClass().addAll("btn-primary", "btn-small");
            btnInvest.setOnAction(e -> handleInvestFromCard(p, txtAmount));
            HBox investRow = new HBox(8);
            investRow.getChildren().addAll(txtAmount, btnInvest);
            investRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            card.getChildren().add(investRow);
        }

        return card;
    }

    private void handleInvestFromCard(Project p, TextField txtAmount) {
        String amountStr = txtAmount.getText() == null ? "" : txtAmount.getText().trim();
        double remaining = p.getGoal_amount() - p.getCurrent_amount();
        String err = ValidationUtils.validateInvestmentAmount(amountStr, remaining > 0 ? remaining : null);
        if (err != null) {
            new Alert(Alert.AlertType.WARNING, err).showAndWait();
            return;
        }
        double amount = ValidationUtils.parseAmount(amountStr);
        try {
            investissementController.addInvestissement(p.getProject_id(), amount);
            new Alert(Alert.AlertType.INFORMATION, String.format("%.2f TND — Request sent. Check My Investments for status.", amount)).showAndWait();
            txtAmount.clear();
            loadProjects();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Failed: " + ex.getMessage()).showAndWait();
        }
    }

    void handleEditProject(Project p) {
        try {
            if (investissementController.hasInvestments(p.getProject_id())) {
                new Alert(Alert.AlertType.WARNING, "Cannot edit: this project has investments.").showAndWait();
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_project.fxml"));
            Parent root = loader.load();
            EditProjectController ctrl = loader.getController();
            ctrl.setStage(stage);
            ctrl.setProject(p);
            ctrl.setDashboardController(this);

            Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Finovate - Edit Project");
        SceneUtils.applyStageSize(stage);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).showAndWait();
            e.printStackTrace();
        }
    }

    void handleDeleteProject(Project p) {
        try {
            if (investissementController.hasInvestments(p.getProject_id())) {
                new Alert(Alert.AlertType.WARNING, "Cannot delete: this project has investments.").showAndWait();
                return;
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).showAndWait();
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Project");
        confirm.setHeaderText("Delete \"" + p.getTitle() + "\"?");
        confirm.setContentText("This cannot be undone.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                projectController.deleteProject(p.getProject_id());
                loadProjects();
                new Alert(Alert.AlertType.INFORMATION, "Project deleted.").showAndWait();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Failed to delete: " + e.getMessage()).showAndWait();
            }
        }
    }

    @FXML
    private void handleAddProject() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_project.fxml"));
        Parent root = loader.load();
        AddProjectController ctrl = loader.getController();
        ctrl.setStage(stage);
        ctrl.setDashboardController(this);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Finovate - Add Project");
        SceneUtils.applyStageSize(stage);
    }

    @FXML
    private void handleMyProjects() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/my_projects.fxml"));
        Parent root = loader.load();
        MyProjectsController ctrl = loader.getController();
        ctrl.setStage(stage);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Finovate - My Projects");
        SceneUtils.applyStageSize(stage);
    }

    @FXML
    private void handleInvest() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/invest.fxml"));
        Parent root = loader.load();
        InvestController ctrl = loader.getController();
        ctrl.setStage(stage);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Finovate - Invest");
        SceneUtils.applyStageSize(stage);
    }

    @FXML
    private void handlePendingInvestments() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pending_investments.fxml"));
        Parent root = loader.load();
        PendingInvestmentsController ctrl = loader.getController();
        ctrl.setStage(stage);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Finovate - Investment Requests");
        SceneUtils.applyStageSize(stage);
    }

    @FXML
    private void handleMyInvestments() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/my_investments.fxml"));
        Parent root = loader.load();
        MyInvestmentsController ctrl = loader.getController();
        ctrl.setStage(stage);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Finovate - My Investments");
        SceneUtils.applyStageSize(stage);
    }

    @FXML
    private void handleAdminDashboard() throws IOException {
        if (Session.currentUser == null || !"ADMIN".equals(Session.currentUser.getRole())) {
            new Alert(Alert.AlertType.WARNING, "Access denied. Admin only.").showAndWait();
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_dashboard.fxml"));
        Parent root = loader.load();
        AdminDashboardController ctrl = loader.getController();
        ctrl.setStage(stage);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Finovate - Admin Dashboard");
        SceneUtils.applyStageSize(stage);
    }

    @FXML
    private void handleSwitchUser() throws IOException {
        Session.currentUser = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/logged_out.fxml"));
        Parent root = loader.load();
        LoggedOutController ctrl = loader.getController();
        ctrl.setStage(stage);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Finovate - Logged out");
        SceneUtils.applyStageSize(stage);
    }
}
