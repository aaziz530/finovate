package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateForumDialogController {

    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;

    private Stage dialogStage;
    private ForumsController forumsController;
    private int currentUserId;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setForumsController(ForumsController forumsController) {
        this.forumsController = forumsController;
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    @FXML
    private void handleCreate() {
        String name = nameField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (name.isEmpty()) {
            showError("Le nom du forum est obligatoire");
            return;
        }

        String query = "INSERT INTO forums (name, description, creator_id, created_at) VALUES (?, ?, ?, NOW())";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setInt(3, currentUserId);
            stmt.executeUpdate();

            forumsController.refreshForums();
            dialogStage.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la création du forum");
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/fintechforum";
        String user = "root";
        String password = "";
        return DriverManager.getConnection(url, user, password);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


