package org.esprit.finovate.controllers;

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
import org.esprit.finovate.models.Investissement;
import org.esprit.finovate.models.Project;

import java.text.SimpleDateFormat;
import org.esprit.finovate.utils.ImageUtils;
import org.esprit.finovate.utils.SceneUtils;
import org.esprit.finovate.utils.Session;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MyProjectsController implements Initializable {

    @FXML private VBox projectsContainer;
    @FXML private TextField txtSearchDynamic;
    @FXML private ComboBox<String> comboStatus;
    @FXML private ComboBox<String> comboTri;

    private Stage stage;
    private List<Project> allProjects = List.of();
    private final ProjectController projectController = new ProjectController();
    private final InvestissementController investissementController = new InvestissementController();

    public void setStage(Stage stage) { this.stage = stage; }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (comboStatus != null) comboStatus.setItems(FXCollections.observableArrayList("Tous", "OPEN", "FUNDED", "CLOSED"));
        if (comboTri != null) comboTri.setItems(FXCollections.observableArrayList("Titre (A-Z)", "Titre (Z-A)", "Montant ↑", "Montant ↓", "Date (récent)", "Date (ancien)"));
        if (txtSearchDynamic != null) txtSearchDynamic.textProperty().addListener((o, ov, nv) -> applyFiltersAndRender());
        if (comboTri != null) comboTri.valueProperty().addListener((o, ov, nv) -> applyFiltersAndRender());
        loadProjects();
    }

    @FXML
    private void handleStaticSearch() {
        applyFiltersAndRender();
    }

    private void loadProjects() {
        if (Session.currentUser == null) return;
        try {
            allProjects = projectController.getProjectsByOwnerId(Session.currentUser.getId());
            applyFiltersAndRender();
        } catch (SQLException e) {
            e.printStackTrace();
            allProjects = List.of();
            showError();
        }
    }

    private void applyFiltersAndRender() {
        projectsContainer.getChildren().clear();
        String searchText = txtSearchDynamic != null && txtSearchDynamic.getText() != null ? txtSearchDynamic.getText().trim().toLowerCase() : "";
        String statusFilter = comboStatus != null && comboStatus.getValue() != null ? comboStatus.getValue() : "Tous";
        String sortOption = comboTri != null && comboTri.getValue() != null ? comboTri.getValue() : null;

        List<Project> filtered = allProjects.stream()
                .filter(p -> searchText.isEmpty() || matchesSearch(p, searchText))
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

    private void showError() {
        Label err = new Label("Error loading projects.");
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

        javafx.scene.control.ProgressBar progressBar = new javafx.scene.control.ProgressBar(
                p.getGoal_amount() > 0 ? Math.min(1.0, p.getCurrent_amount() / p.getGoal_amount()) : 0);
        progressBar.getStyleClass().add("project-progress-bar");
        progressBar.setMaxWidth(Double.MAX_VALUE);

        Label meta = new Label(String.format("%.2f TND / %.2f TND  •  %s", p.getCurrent_amount(), p.getGoal_amount(), p.getStatus()));
        meta.getStyleClass().add("project-meta");

        card.getChildren().add(progressBar);
        card.getChildren().add(meta);

        try {
            boolean hasInvestments = investissementController.hasInvestments(p.getProject_id());
            List<Investissement> pending = investissementController.getInvestissementsByProjectId(p.getProject_id()).stream()
                    .filter(inv -> "PENDING".equals(inv.getStatus()))
                    .collect(Collectors.toList());

            if (!pending.isEmpty()) {
                Label pendingLabel = new Label("Pending investment requests:");
                pendingLabel.getStyleClass().add("field-label");
                card.getChildren().add(pendingLabel);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                for (Investissement inv : pending) {
                    HBox invRow = new HBox(12);
                    invRow.getStyleClass().add("investment-card");
                    invRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    String dateStr = inv.getInvestment_date() != null ? sdf.format(inv.getInvestment_date()) : "—";
                    Label invInfo = new Label(String.format("Investor #%d  •  %.2f TND  •  %s", inv.getInvestor_id(), inv.getAmount(), dateStr));
                    invInfo.getStyleClass().add("project-meta");
                    Button btnAccept = new Button("Accept");
                    btnAccept.getStyleClass().addAll("btn-primary", "btn-small");
                    btnAccept.setOnAction(e -> handleAccept(inv));
                    Button btnDecline = new Button("Decline");
                    btnDecline.getStyleClass().addAll("btn-danger", "btn-small");
                    btnDecline.setOnAction(e -> handleDecline(inv));
                    invRow.getChildren().addAll(invInfo, btnAccept, btnDecline);
                    HBox.setHgrow(invInfo, javafx.scene.layout.Priority.ALWAYS);
                    card.getChildren().add(invRow);
                }
            }

            if (!hasInvestments) {
                HBox actions = new HBox(8);
                Button btnEdit = new Button("Edit");
                btnEdit.getStyleClass().addAll("btn-edit", "btn-small");
                btnEdit.setOnAction(e -> openEdit(p));
                Button btnDelete = new Button("Delete");
                btnDelete.getStyleClass().addAll("btn-danger", "btn-small");
                btnDelete.setOnAction(e -> deleteProject(p));
                actions.getChildren().addAll(btnEdit, btnDelete);
                card.getChildren().add(actions);
            } else {
                Label locked = new Label("Edit/Delete disabled — this project has investments.");
                locked.getStyleClass().add("project-meta");
                card.getChildren().add(locked);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return card;
    }

    private void handleAccept(Investissement inv) {
        try {
            investissementController.acceptInvestissement(inv.getInvestissement_id());
            new Alert(Alert.AlertType.INFORMATION, "Investment accepted. Amount added to project.").showAndWait();
            loadProjects();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed: " + e.getMessage()).showAndWait();
        }
    }

    private void handleDecline(Investissement inv) {
        try {
            investissementController.declineInvestissement(inv.getInvestissement_id());
            new Alert(Alert.AlertType.INFORMATION, "Investment declined.").showAndWait();
            loadProjects();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed: " + e.getMessage()).showAndWait();
        }
    }

    private void openEdit(Project p) {
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
            ctrl.setReturnToMyProjects(this);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Finovate - Edit Project");
        SceneUtils.applyStageSize(stage);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).showAndWait();
        }
    }

    private void deleteProject(Project p) {
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
    private void handleBack() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
        Parent root = loader.load();
        DashboardController ctrl = loader.getController();
        ctrl.setStage(stage);
        ctrl.refresh();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Finovate - Dashboard");
        SceneUtils.applyStageSize(stage);
    }
}
