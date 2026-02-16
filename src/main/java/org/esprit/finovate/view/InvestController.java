package org.esprit.finovate.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.esprit.finovate.controllers.InvestissementController;
import org.esprit.finovate.controllers.ProjectController;
import org.esprit.finovate.entities.Project;
import org.esprit.finovate.utils.ValidationUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class InvestController implements Initializable {

    @FXML private ListView<Project> listProjects;
    @FXML private TextField txtAmount;
    @FXML private Label lblError;
    @FXML private Label lblSuccess;

    private Stage stage;
    private final ProjectController projectController = new ProjectController();
    private final InvestissementController investissementController = new InvestissementController();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadProjects();
        listProjects.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Project item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%s  |  %.2f / %.2f TND  |  %s",
                            item.getTitle(), item.getCurrent_amount(), item.getGoal_amount(), item.getStatus()));
                }
            }
        });
    }

    private void loadProjects() {
        try {
            List<Project> all = projectController.getAllProjects();
            listProjects.getItems().clear();
            if (org.esprit.finovate.utils.Session.currentUser == null) {
                listProjects.getItems().addAll(all);
            } else {
                Long myId = org.esprit.finovate.utils.Session.currentUser.getId();
                for (Project p : all) {
                    if (p.getOwner_id() == null || !p.getOwner_id().equals(myId)) {
                        listProjects.getItems().add(p);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleInvest() {
        lblError.setVisible(false);
        lblError.setManaged(false);
        lblSuccess.setVisible(false);
        lblSuccess.setManaged(false);

        Project selected = listProjects.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a project.");
            return;
        }

        String amountStr = txtAmount.getText() == null ? "" : txtAmount.getText().trim();
        String err = ValidationUtils.validateInvestmentAmount(amountStr);
        if (err != null) { showError(err); return; }

        double amount = ValidationUtils.parseAmount(amountStr);

        try {
            investissementController.addInvestissement(selected.getProject_id(), amount);
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Success");
            success.setHeaderText("Investment Request Sent");
            success.setContentText(String.format("%.2f TND — Request sent to project owner. You'll see it in My Investments until they accept or decline.", amount));
            success.showAndWait();
            lblSuccess.setText("Investment successful! Project total updated.");
            lblSuccess.setVisible(true);
            lblSuccess.setManaged(true);
            txtAmount.clear();
            loadProjects();
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() != null) msg = e.getCause().getMessage();
            if (msg == null || msg.isEmpty()) msg = e.getClass().getSimpleName();
            showError("Error: " + msg);
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
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
