package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.sql.*;

public class PostDetailsController {

    @FXML private Label titleLabel;
    @FXML private Label authorLabel;
    @FXML private Label dateLabel;
    @FXML private TextArea contentArea;
    @FXML private VBox commentsBox;
    @FXML private TextArea newCommentArea;
    @FXML private Button backBtn;
    @FXML private Button addCommentBtn;

    private MainController mainController;
    private int currentPostId;
    private int currentUserId;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void loadPostDetails(int postId, int userId) {
        this.currentPostId = postId;
        this.currentUserId = userId;
        
        loadPostInfo();
        loadComments();
    }

    private void loadPostInfo() {
        String query = "SELECT p.title, p.content, p.created_at, u.username " +
                "FROM posts p " +
                "INNER JOIN users u ON p.author_id = u.id " +
                "WHERE p.id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentPostId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                titleLabel.setText(rs.getString("title"));
                authorLabel.setText("Par: " + rs.getString("username"));
                dateLabel.setText("Publié le: " + rs.getTimestamp("created_at").toString().substring(0, 16));
                contentArea.setText(rs.getString("content"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de chargement du post");
        }
    }

    private void loadComments() {
        commentsBox.getChildren().clear();

        String query = "SELECT c.id, c.content, c.created_at, u.username, c.author_id " +
                "FROM comments c " +
                "INNER JOIN users u ON c.author_id = u.id " +
                "WHERE c.post_id = ? " +
                "ORDER BY c.created_at ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentPostId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                VBox commentCard = createCommentCard(
                    rs.getInt("id"),
                    rs.getString("content"),
                    rs.getString("username"),
                    rs.getTimestamp("created_at").toString().substring(0, 16),
                    rs.getInt("author_id")
                );
                commentsBox.getChildren().add(commentCard);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de chargement des commentaires");
        }
    }

    private VBox createCommentCard(int commentId, String content, String author, String date, int authorId) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-color: #2196F3; " +
                "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Header avec auteur et date
        javafx.scene.layout.HBox headerBox = new javafx.scene.layout.HBox(10);
        headerBox.setStyle("-fx-background-color: #E3F2FD; -fx-padding: 8; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        Label authorLabel = new Label("👤 " + author);
        authorLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1976D2;");
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Label dateLabel = new Label("📅 " + date);
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        
        headerBox.getChildren().addAll(authorLabel, spacer, dateLabel);

        // Contenu du commentaire
        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333; -fx-padding: 10 0 10 0;");

        card.getChildren().addAll(headerBox, contentLabel);

        // Si c'est le commentaire de l'utilisateur, ajouter boutons modifier/supprimer
        if (authorId == currentUserId) {
            javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(10);
            buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            buttonBox.setStyle("-fx-padding: 5 0 0 0;");
            
            Button editBtn = new Button("✏️ Modifier");
            editBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                    "-fx-font-size: 11px; -fx-padding: 5 15 5 15; -fx-border-radius: 5; -fx-background-radius: 5;");
            editBtn.setOnAction(e -> editComment(commentId, content, card));
            
            Button deleteBtn = new Button("🗑️ Supprimer");
            deleteBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; " +
                    "-fx-font-size: 11px; -fx-padding: 5 15 5 15; -fx-border-radius: 5; -fx-background-radius: 5;");
            deleteBtn.setOnAction(e -> deleteComment(commentId));
            
            buttonBox.getChildren().addAll(editBtn, deleteBtn);
            card.getChildren().add(buttonBox);
        }

        return card;
    }

    @FXML
    private void addComment() {
        String content = newCommentArea.getText().trim();

        if (content.isEmpty()) {
            showError("Le commentaire ne peut pas être vide");
            return;
        }

        String query = "INSERT INTO comments (post_id, author_id, content, created_at) VALUES (?, ?, ?, NOW())";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentPostId);
            stmt.setInt(2, currentUserId);
            stmt.setString(3, content);
            stmt.executeUpdate();

            newCommentArea.clear();
            loadComments();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ajout du commentaire");
        }
    }

    private void deleteComment(int commentId) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer ce commentaire ?");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            String query = "DELETE FROM comments WHERE id = ? AND author_id = ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, commentId);
                stmt.setInt(2, currentUserId);
                stmt.executeUpdate();

                loadComments();

            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur de suppression du commentaire");
            }
        }
    }

    private void editComment(int commentId, String currentContent, VBox card) {
        // Remplacer le contenu par un TextArea éditable
        card.getChildren().clear();
        card.setStyle("-fx-background-color: #FFF3E0; -fx-border-color: #FF9800; " +
                "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Label headerLabel = new Label("✏️ Modification en cours...");
        headerLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #E65100; -fx-padding: 0 0 10 0;");

        TextArea editArea = new TextArea(currentContent);
        editArea.setWrapText(true);
        editArea.setPrefRowCount(4);
        editArea.setStyle("-fx-font-size: 14px; -fx-border-color: #FF9800; -fx-border-width: 1; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;");

        javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        buttonBox.setStyle("-fx-padding: 10 0 0 0;");
        
        Button saveBtn = new Button("💾 Enregistrer");
        saveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-padding: 8 20 8 20; -fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-font-weight: bold;");
        saveBtn.setOnAction(e -> {
            String newContent = editArea.getText().trim();
            if (newContent.isEmpty()) {
                showError("Le commentaire ne peut pas être vide");
                return;
            }
            saveEditedComment(commentId, newContent);
        });

        Button cancelBtn = new Button("❌ Annuler");
        cancelBtn.setStyle("-fx-background-color: #757575; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-padding: 8 20 8 20; -fx-border-radius: 5; -fx-background-radius: 5;");
        cancelBtn.setOnAction(e -> loadComments());

        buttonBox.getChildren().addAll(saveBtn, cancelBtn);
        card.getChildren().addAll(headerLabel, editArea, buttonBox);
    }

    private void saveEditedComment(int commentId, String newContent) {
        String query = "UPDATE comments SET content = ?, updated_at = NOW() WHERE id = ? AND author_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newContent);
            stmt.setInt(2, commentId);
            stmt.setInt(3, currentUserId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                showInfo("Commentaire modifié avec succès");
                loadComments();
            } else {
                showError("Impossible de modifier ce commentaire");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la modification du commentaire");
        }
    }

    @FXML
    private void goBack() {
        mainController.goBackToForums();
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

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
