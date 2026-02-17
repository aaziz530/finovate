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

public class CreatePostDialogController {

    @FXML private TextField titleField;
    @FXML private TextArea contentArea;

    private Stage dialogStage;
    private PostsController postsController;
    private int forumId;
    private int authorId;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setPostsController(PostsController postsController) {
        this.postsController = postsController;
    }

    public void setForumId(int forumId) {
        this.forumId = forumId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    @FXML
    private void handleCreate() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            showError("Le titre et le contenu sont obligatoires");
            return;
        }

        String query = "INSERT INTO posts (forum_id, title, content, author_id, created_at) " +
                "VALUES (?, ?, ?, ?, NOW())";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, forumId);
            stmt.setString(2, title);
            stmt.setString(3, content);
            stmt.setInt(4, authorId);
            stmt.executeUpdate();


            postsController.refreshPosts();
            dialogStage.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la création du post");
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