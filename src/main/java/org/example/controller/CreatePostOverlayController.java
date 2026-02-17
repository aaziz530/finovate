package org.example.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.*;

/**
 * Overlay pour créer un nouveau post (style Reddit/Facebook)
 */
public class CreatePostOverlayController {
    
    private VBox rootContainer;
    private OverlayManager overlayManager;
    private int forumId;
    private int currentUserId;
    private Runnable onPostCreated;
    
    public CreatePostOverlayController(OverlayManager overlayManager, int forumId, int currentUserId, Runnable onPostCreated) {
        this.overlayManager = overlayManager;
        this.forumId = forumId;
        this.currentUserId = currentUserId;
        this.onPostCreated = onPostCreated;
        this.rootContainer = new VBox(0);
        buildUI();
    }
    
    public VBox getView() {
        return rootContainer;
    }
    
    private void buildUI() {
        rootContainer.setStyle("-fx-background-color: white;");
        
        // Header
        HBox header = createHeader();
        
        // Formulaire
        VBox form = createForm();
        
        rootContainer.getChildren().addAll(header, form);
    }
    
    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
            "-fx-background-color: linear-gradient(to right, #1877F2, #4A9AFF);" +
            "-fx-border-color: transparent transparent #E1E8ED transparent;" +
            "-fx-border-width: 0 0 1 0;"
        );
        
        Label titleLabel = new Label("✍️ Créer un Post");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.2);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 5 12;" +
            "-fx-background-radius: 20;" +
            "-fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> overlayManager.closeTopOverlay());
        
        header.getChildren().addAll(titleLabel, spacer, closeBtn);
        return header;
    }
    
    private VBox createForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(25));
        
        // Titre
        Label titleLabel = new Label("Titre du post");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        
        TextField titleField = new TextField();
        titleField.setPromptText("Entrez un titre accrocheur...");
        titleField.setStyle(
            "-fx-font-size: 15px;" +
            "-fx-padding: 12;" +
            "-fx-background-color: white;" +
            "-fx-border-color: #E1E8ED;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );
        titleField.setOnMouseEntered(e -> titleField.setStyle(
            titleField.getStyle() + "-fx-border-color: #1877F2;"
        ));
        
        // Contenu
        Label contentLabel = new Label("Contenu");
        contentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Partagez vos idées...");
        contentArea.setPrefRowCount(8);
        contentArea.setWrapText(true);
        contentArea.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-padding: 12;" +
            "-fx-background-color: white;" +
            "-fx-border-color: #E1E8ED;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );
        
        // Boutons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle(
            "-fx-background-color: #E0E0E0;" +
            "-fx-text-fill: #666;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 12 30;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> overlayManager.closeTopOverlay());
        
        Button createBtn = new Button("📤 Publier");
        createBtn.setStyle(
            "-fx-background-color: #1877F2;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 12 30;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(24, 119, 242, 0.3), 5, 0, 0, 2);"
        );
        createBtn.setOnAction(e -> createPost(titleField.getText(), contentArea.getText()));
        
        buttonBox.getChildren().addAll(cancelBtn, createBtn);
        
        form.getChildren().addAll(
            titleLabel, titleField,
            contentLabel, contentArea,
            buttonBox
        );
        
        return form;
    }
    
    private void createPost(String title, String content) {
        if (title.trim().isEmpty() || content.trim().isEmpty()) {
            showError("Le titre et le contenu sont obligatoires");
            return;
        }
        
        String query = "INSERT INTO posts (forum_id, author_id, title, content, created_at) VALUES (?, ?, ?, ?, NOW())";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, forumId);
            stmt.setInt(2, currentUserId);
            stmt.setString(3, title.trim());
            stmt.setString(4, content.trim());
            stmt.executeUpdate();
            
            showInfo("Post créé avec succès !");
            overlayManager.closeTopOverlay();
            
            if (onPostCreated != null) {
                onPostCreated.run();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la création du post");
        }
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
