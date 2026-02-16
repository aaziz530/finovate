package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.esprit.finovate.controllers.InvestissementController;
import org.esprit.finovate.controllers.ProjectController;
import org.esprit.finovate.models.Project;
import org.esprit.finovate.utils.Session;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class MyProjectsController implements Initializable {

    @FXML private VBox projectsContainer;

    private Stage stage;
    private final ProjectController projectController = new ProjectController();
    private final InvestissementController investissementController = new InvestissementController();

    public void setStage(Stage stage) { this.stage = stage; }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadProjects();
    }

    private void loadProjects() {
        projectsContainer.getChildren().clear();
        if (Session.currentUser == null) return;

        try {
            List<Project> projects = projectController.getProjectsByOwnerId(Session.currentUser.getId());
            if (projects.isEmpty()) {
                Label empty = new Label("You have no projects yet. Add one from the dashboard!");
                empty.getStyleClass().add("project-meta");
                projectsContainer.getChildren().add(empty);
            } else {
                for (Project p : projects) {
                    VBox card = createProjectCard(p);
                    projectsContainer.getChildren().add(card);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Label err = new Label("Error loading projects.");
            err.getStyleClass().add("error-label");
            projectsContainer.getChildren().add(err);
        }
    }

    private VBox createProjectCard(Project p) {
        VBox card = new VBox(10);
        card.getStyleClass().add("project-card");
        card.setPrefWidth(800);

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
    }
}
