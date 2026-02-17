package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.esprit.finovate.utils.LiveValidationHelper;
import org.esprit.finovate.utils.ValidationUtils;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class AddProjectController implements Initializable {

    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtGoalAmount;
    @FXML private DatePicker dateDeadline;
    @FXML private Label lblError;

    private Stage stage;
    private DashboardController dashboardController;
    private final ProjectController projectController = new ProjectController();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setDashboardController(DashboardController ctrl) {
        this.dashboardController = ctrl;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LiveValidationHelper.bind(txtTitle, s -> ValidationUtils.validateTitle(s));
        LiveValidationHelper.bind(txtDescription, s -> ValidationUtils.validateDescription(s));
        LiveValidationHelper.bind(txtGoalAmount, s -> ValidationUtils.validateGoalAmount(s));
        LiveValidationHelper.bind(dateDeadline, d -> d == null ? null : ValidationUtils.validateDeadline(d, "Deadline"));
    }

    @FXML
    private void handleCreate() {
        lblError.setVisible(false);
        lblError.setManaged(false);

        String title = txtTitle.getText() == null ? "" : txtTitle.getText().trim();
        String desc = txtDescription.getText() == null ? "" : txtDescription.getText().trim();
        String goalStr = txtGoalAmount.getText() == null ? "" : txtGoalAmount.getText().trim();

        String err = ValidationUtils.validateTitle(title);
        if (err != null) { showError(err); return; }
        err = ValidationUtils.validateDescription(desc);
        if (err != null) { showError(err); return; }
        err = ValidationUtils.validateGoalAmount(goalStr);
        if (err != null) { showError(err); return; }

        LocalDate deadlineDate = dateDeadline.getValue();
        if (deadlineDate != null) {
            err = ValidationUtils.validateDeadline(deadlineDate, "Deadline");
            if (err != null) { showError(err); return; }
        }

        double goalAmount = ValidationUtils.parseAmount(goalStr);
        Date deadline = deadlineDate != null
                ? Date.from(deadlineDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
                : new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30));

        try {
            projectController.addProject(title, desc, goalAmount, deadline);
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Success");
            success.setHeaderText("Project Created");
            success.setContentText("Your project has been added successfully and saved to the database.");
            success.showAndWait();
            handleCancel();
        } catch (Exception e) {
            showError("Failed to create project. Check database connection.");
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
        goBackToDashboard();
    }

    private void goBackToDashboard() throws IOException {
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
