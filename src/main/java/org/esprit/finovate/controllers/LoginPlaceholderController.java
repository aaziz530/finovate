package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.esprit.finovate.utils.SceneUtils;
import org.esprit.finovate.utils.Session;
import org.esprit.finovate.utils.StubLoggedInUser;

import java.io.IOException;

/** Placeholder login for testing. Enter User ID to access dashboard. */
public class LoginPlaceholderController {

    @FXML private TextField txtUserId;
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleEnter() throws IOException {
        try {
            long id = Long.parseLong(txtUserId.getText().trim());
            Session.currentUser = new StubLoggedInUser(id);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();
            DashboardController ctrl = loader.getController();
            ctrl.setStage(stage);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Finovate - Dashboard");
            SceneUtils.applyStageSize(stage);
            stage.centerOnScreen();
        } catch (NumberFormatException e) {
            txtUserId.setStyle("-fx-border-color: #dc2626;");
        }
    }
}
