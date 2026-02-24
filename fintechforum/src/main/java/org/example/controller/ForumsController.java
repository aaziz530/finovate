package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class ForumsController {

    @FXML private Label titleLabel;
    @FXML private Button createForumBtn;
    @FXML private Label emptyLabel;
    @FXML private ListView<ForumItem> forumsList;

    private MainController mainController;
    private int currentUserId;
    private String currentViewType; // "accueil", "myForums", "joinedForums"
    private ObservableList<ForumItem> allForums = FXCollections.observableArrayList(); // Liste complète
    private ObservableList<ForumItem> filteredForums = FXCollections.observableArrayList(); // Liste filtrée

    // Classe interne pour représenter un forum
    public static class ForumItem {
        private int id;
        private String name;
        private String description;
        private int creatorId;
        private int memberCount;
        private int upvotes;
        private int downvotes;
        private int score;
        private Timestamp createdAt;

        public ForumItem(int id, String name, String description, int creatorId, int memberCount, Timestamp createdAt) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.creatorId = creatorId;
            this.memberCount = memberCount;
            this.createdAt = createdAt;
            this.upvotes = 0;
            this.downvotes = 0;
            this.score = 0;
        }

        // Getters
        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getCreatorId() { return creatorId; }
        public int getMemberCount() { return memberCount; }
        public Timestamp getCreatedAt() { return createdAt; }
        public int getUpvotes() { return upvotes; }
        public int getDownvotes() { return downvotes; }
        public int getScore() { return score; }
        
        public void setUpvotes(int upvotes) { 
            this.upvotes = upvotes;
            this.score = upvotes - downvotes;
        }
        public void setDownvotes(int downvotes) { 
            this.downvotes = downvotes;
            this.score = upvotes - downvotes;
        }
    }

    @FXML
    public void initialize() {
        // Configurer la cellule personnalisée pour afficher les forums
        forumsList.setCellFactory(param -> new ForumCell());
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void loadForums(String viewType, int userId) {
        this.currentViewType = viewType;
        this.currentUserId = userId;

        if (viewType.equals("accueil")) {
            titleLabel.setText("Accueil - Tous les Forums");
            createForumBtn.setVisible(false);
            createForumBtn.setManaged(false);
            loadAllPublicForums();
        } else if (viewType.equals("myForums")) {
            titleLabel.setText("Mes Forums");
            createForumBtn.setVisible(true);
            createForumBtn.setManaged(true);
            loadMyForums();
        } else {
            titleLabel.setText("Forums Rejoints");
            createForumBtn.setVisible(false);
            createForumBtn.setManaged(false);
            loadJoinedForums();
        }
    }

    private void loadAllPublicForums() {
        allForums.clear();

        String query = "SELECT f.id, f.name, f.description, f.creator_id, f.created_at, " +
                "(SELECT COUNT(*) FROM user_forum WHERE forum_id = f.id) as member_count " +
                "FROM forums f " +
                "ORDER BY f.created_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                allForums.add(new ForumItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("creator_id"),
                        rs.getInt("member_count"),
                        rs.getTimestamp("created_at")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de chargement des forums");
        }

        filteredForums = FXCollections.observableArrayList(allForums);
        forumsList.setItems(filteredForums);
        emptyLabel.setVisible(filteredForums.isEmpty());
        emptyLabel.setManaged(filteredForums.isEmpty());
    }

    private void loadMyForums() {
        allForums.clear();

        String query = "SELECT f.id, f.name, f.description, f.creator_id, f.created_at, " +
                "(SELECT COUNT(*) FROM user_forum WHERE forum_id = f.id) as member_count " +
                "FROM forums f WHERE f.creator_id = ? ORDER BY f.created_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                allForums.add(new ForumItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("creator_id"),
                        rs.getInt("member_count"),
                        rs.getTimestamp("created_at")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de chargement des forums");
        }

        filteredForums = FXCollections.observableArrayList(allForums);
        forumsList.setItems(filteredForums);
        emptyLabel.setVisible(filteredForums.isEmpty());
        emptyLabel.setManaged(filteredForums.isEmpty());
    }

    private void loadJoinedForums() {
        allForums.clear();

        String query = "SELECT f.id, f.name, f.description, f.creator_id, f.created_at, " +
                "(SELECT COUNT(*) FROM user_forum WHERE forum_id = f.id) as member_count " +
                "FROM forums f " +
                "INNER JOIN user_forum fm ON f.id = fm.forum_id " +
                "WHERE fm.user_id = ? AND f.creator_id != ? " +
                "ORDER BY fm.joined_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentUserId);
            stmt.setInt(2, currentUserId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                allForums.add(new ForumItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("creator_id"),
                        rs.getInt("member_count"),
                        rs.getTimestamp("created_at")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de chargement des forums rejoints");
        }

        filteredForums = FXCollections.observableArrayList(allForums);
        forumsList.setItems(filteredForums);
        emptyLabel.setVisible(filteredForums.isEmpty());
        emptyLabel.setManaged(filteredForums.isEmpty());
    }

    // Méthode de recherche
    public void applySearch(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredForums = FXCollections.observableArrayList(allForums);
        } else {
            String search = searchText.toLowerCase();
            filteredForums = allForums.filtered(forum -> 
                forum.getName().toLowerCase().contains(search) || 
                forum.getDescription().toLowerCase().contains(search)
            );
        }
        forumsList.setItems(filteredForums);
        emptyLabel.setVisible(filteredForums.isEmpty());
        emptyLabel.setManaged(filteredForums.isEmpty());
    }

    // Méthode de tri
    public void applySort(String sortType) {
        if (sortType == null) return;

        switch (sortType) {
            case "Plus récents":
                filteredForums.sort((f1, f2) -> f2.getCreatedAt().compareTo(f1.getCreatedAt()));
                break;
            case "Plus anciens":
                filteredForums.sort((f1, f2) -> f1.getCreatedAt().compareTo(f2.getCreatedAt()));
                break;
            case "Plus populaires":
                filteredForums.sort((f1, f2) -> Integer.compare(f2.getMemberCount(), f1.getMemberCount()));
                break;
            case "A-Z":
                filteredForums.sort((f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));
                break;
            case "Z-A":
                filteredForums.sort((f1, f2) -> f2.getName().compareToIgnoreCase(f1.getName()));
                break;
        }
        forumsList.refresh();
    }

    @FXML
    private void openCreateForumDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/create-forum-dialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Créer un Forum");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));

            CreateForumDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setForumsController(this);
            controller.setCurrentUserId(currentUserId);

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur d'ouverture du dialogue");
        }
    }

    public void refreshForums() {
        loadForums(currentViewType, currentUserId);
    }

    private void deleteForum(int forumId) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer ce forum ?");
        confirmAlert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String query = "DELETE FROM forums WHERE id = ? AND creator_id = ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, forumId);
                stmt.setInt(2, currentUserId);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    showInfo("Forum supprimé avec succès");
                    refreshForums();
                } else {
                    showError("Impossible de supprimer ce forum");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur de suppression du forum");
            }
        }
    }

    private void joinForum(int forumId) {
        String query = "INSERT INTO user_forum (forum_id, user_id, joined_at) VALUES (?, ?, NOW())";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, forumId);
            stmt.setInt(2, currentUserId);
            stmt.executeUpdate();

            showInfo("Vous avez rejoint le forum !");
            refreshForums();

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                showError("Vous avez déjà rejoint ce forum");
            } else {
                e.printStackTrace();
                showError("Erreur lors de l'adhésion au forum");
            }
        }
    }

    private void leaveForum(int forumId) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Quitter ce forum ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String query = "DELETE FROM user_forum WHERE forum_id = ? AND user_id = ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, forumId);
                stmt.setInt(2, currentUserId);
                stmt.executeUpdate();

                showInfo("Vous avez quitté le forum");
                refreshForums();

            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur lors de la sortie du forum");
            }
        }
    }

    private boolean checkIfMember(int forumId) {
        String query = "SELECT 1 FROM user_forum WHERE forum_id = ? AND user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, forumId);
            stmt.setInt(2, currentUserId);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void openForum(ForumItem forum) {
        // Tracker l'interaction CLICK pour le système de recommandation AI
        try {
            Class<?> engineClass = Class.forName("org.example.ai.RecommendationEngine");
            Class<?> interactionTypeClass = Class.forName("org.example.ai.RecommendationEngine$InteractionType");
            
            java.lang.reflect.Method trackMethod = engineClass.getMethod("trackInteraction", int.class, int.class, interactionTypeClass);
            Object clickType = Enum.valueOf((Class<Enum>) interactionTypeClass, "CLICK");
            
            trackMethod.invoke(null, currentUserId, forum.getId(), clickType);
        } catch (Exception e) {
            // Silently fail if AI engine not available
        }
        
        mainController.showPostsView(forum.getId(), forum.getName());
    }

    private void voteForum(int forumId, String voteType) {
        String checkQuery = "SELECT vote_type FROM votes WHERE forum_id = ? AND user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

            checkStmt.setInt(1, forumId);
            checkStmt.setInt(2, currentUserId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String existingVote = rs.getString("vote_type");
                
                if (existingVote.equals(voteType)) {
                    String deleteQuery = "DELETE FROM votes WHERE forum_id = ? AND user_id = ?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                        deleteStmt.setInt(1, forumId);
                        deleteStmt.setInt(2, currentUserId);
                        deleteStmt.executeUpdate();
                    }
                } else {
                    String updateQuery = "UPDATE votes SET vote_type = ? WHERE forum_id = ? AND user_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, voteType);
                        updateStmt.setInt(2, forumId);
                        updateStmt.setInt(3, currentUserId);
                        updateStmt.executeUpdate();
                    }
                }
            } else {
                String insertQuery = "INSERT INTO votes (forum_id, user_id, vote_type) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setInt(1, forumId);
                    insertStmt.setInt(2, currentUserId);
                    insertStmt.setString(3, voteType);
                    insertStmt.executeUpdate();
                }
            }

            refreshForums();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors du vote: " + e.getMessage());
        }
    }

    // Cellule personnalisée pour afficher un forum
    private class ForumCell extends ListCell<ForumItem> {
        @Override
        protected void updateItem(ForumItem forum, boolean empty) {
            super.updateItem(forum, empty);

            if (empty || forum == null) {
                setGraphic(null);
                setText(null);
            } else {
                VBox card = new VBox(10);
                card.setPadding(new Insets(15));
                card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; " +
                        "-fx-border-radius: 5; -fx-background-radius: 5;");

                // Titre du forum
                Label nameLabel = new Label(forum.getName());
                nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

                // Description
                Label descLabel = new Label(forum.getDescription());
                descLabel.setWrapText(true);
                descLabel.setStyle("-fx-text-fill: #666;");

                // Info (membres, date)
                HBox infoBox = new HBox(15);
                Label membersLabel = new Label("👥 " + forum.getMemberCount() + " membres");
                Label dateLabel = new Label("📅 " + forum.getCreatedAt().toString().substring(0, 10));
                infoBox.getChildren().addAll(membersLabel, dateLabel);
                infoBox.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

                // Boutons d'action
                HBox actionBox = new HBox(10);
                actionBox.setAlignment(Pos.CENTER_LEFT);

                if (currentViewType.equals("myForums")) {
                    // Mes forums: boutons Modifier et Supprimer
                    Button editBtn = new Button("✏️ Modifier");
                    editBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");

                    Button deleteBtn = new Button("🗑️ Supprimer");
                    deleteBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
                    deleteBtn.setOnAction(e -> deleteForum(forum.getId()));

                    actionBox.getChildren().addAll(editBtn, deleteBtn);
                } else if (currentViewType.equals("joinedForums")) {
                    // Forums rejoints: bouton Quitter
                    Button leaveBtn = new Button("Quitter");
                    leaveBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
                    leaveBtn.setOnAction(e -> leaveForum(forum.getId()));

                    actionBox.getChildren().add(leaveBtn);
                } else if (currentViewType.equals("accueil")) {
                    // Accueil: bouton Rejoindre si pas déjà membre
                    if (forum.getCreatorId() != currentUserId) {
                        // Vérifier si déjà membre
                        boolean isMember = checkIfMember(forum.getId());
                        
                        if (isMember) {
                            Button leaveBtn = new Button("Se désinscrire");
                            leaveBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
                            leaveBtn.setOnAction(e -> leaveForum(forum.getId()));
                            actionBox.getChildren().add(leaveBtn);
                        } else {
                            Button joinBtn = new Button("➕ Rejoindre");
                            joinBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                            joinBtn.setOnAction(e -> joinForum(forum.getId()));
                            actionBox.getChildren().add(joinBtn);
                        }
                    }
                }

                card.getChildren().addAll(nameLabel, descLabel, infoBox, actionBox);
                
                // Rendre la carte cliquable pour ouvrir le forum
                card.setOnMouseClicked(e -> {
                    openForum(forum);
                    e.consume();
                });
                card.setStyle(card.getStyle() + "; -fx-cursor: hand;");
                
                setGraphic(card);
            }
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