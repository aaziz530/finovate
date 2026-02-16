package org.esprit.finovate.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.esprit.finovate.controllers.ProjectController;
import org.esprit.finovate.entities.Project;
import org.esprit.finovate.utils.ValidationUtils;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Date;

public class EditProjectController {

    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtGoalAmount;
    @FXML private DatePicker dateDeadline;
    @FXML private Label lblError;

    private Stage stage;
    private Project project;
    private DashboardController dashboardController;
    private MyProjectsController returnToMyProjects;
    private final ProjectController projectController = new ProjectController();

    public void setStage(Stage stage) { this.stage = stage; }
    public void setDashboardController(DashboardController ctrl) { this.dashboardController = ctrl; }
    public void setReturnToMyProjects(MyProjectsController ctrl) { this.returnToMyProjects = ctrl; }

    public void setProject(Project p) {
        this.project = p;
        txtTitle.setText(p.getTitle());
        txtDescription.setText(p.getDescription());
        txtGoalAmount.setText(String.valueOf(p.getGoal_amount()));
        if (p.getDeadline() != null) {
            dateDeadline.setValue(p.getDeadline().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }
    }

    @FXML
    private void handleSave() {
        lblError.setVisible(false);

        String title = txtTitle.getText() == null ? "" : txtTitle.getText().trim();
        String desc = txtDescription.getText() == null ? "" : txtDescription.getText().trim();
        String goalStr = txtGoalAmount.getText() == null ? "" : txtGoalAmount.getText().trim();

        String err = ValidationUtils.validateTitle(title);
        if (err != null) { showError(err); return; }
        err = ValidationUtils.validateDescription(desc);
        if (err != null) { showError(err); return; }
        err = ValidationUtils.validateGoalAmount(goalStr);
        if (err != null) { showError(err); return; }

        double goalAmount = ValidationUtils.parseAmount(goalStr);

        Date deadline = null;
        if (dateDeadline.getValue() != null) {
            deadline = Date.from(dateDeadline.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        project.setTitle(title);
        project.setDescription(desc);
        project.setGoal_amount(goalAmount);
        project.setDeadline(deadline);

        try {
            projectController.updateProject(project);
            new Alert(Alert.AlertType.INFORMATION, "Project updated successfully.").showAndWait();
            handleCancel();
        } catch (Exception e) {
            showError("Failed to update: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    @FXML
    private void handleCancel() throws IOException {
        if (returnToMyProjects != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/my_projects.fxml"));
            Parent root = loader.load();
            MyProjectsController ctrl = loader.getController();
            ctrl.setStage(stage);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Finovate - My Projects");
        } else {
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
}
