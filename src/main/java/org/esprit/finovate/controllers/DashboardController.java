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
import org.esprit.finovate.models.Project;
import org.esprit.finovate.utils.Session;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private VBox projectsContainer;

    private Stage stage;
    private final ProjectController projectController = new ProjectController();
    private final InvestissementController investissementController = new InvestissementController();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadProjects();
    }

    public void refresh() {
        loadProjects();
    }

    private void loadProjects() {
        projectsContainer.getChildren().clear();
        try {
            List<Project> projects = projectController.getAllProjects();
            if (projects.isEmpty()) {
                Label empty = new Label("No projects yet. Add one!");
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

        double progress = p.getGoal_amount() > 0 ? Math.min(1.0, p.getCurrent_amount() / p.getGoal_amount()) : 0;
        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.getStyleClass().add("project-progress-bar");
        progressBar.setMaxWidth(Double.MAX_VALUE);

        Label meta = new Label(String.format("%.2f TND / %.2f TND  •  %s", p.getCurrent_amount(), p.getGoal_amount(), p.getStatus()));
        meta.getStyleClass().add("project-meta");

        card.getChildren().add(progressBar);
        card.getChildren().add(meta);

        return card;
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
        stage.setMinWidth(500);
        stage.setMinHeight(400);
        stage.centerOnScreen();
    }
}
