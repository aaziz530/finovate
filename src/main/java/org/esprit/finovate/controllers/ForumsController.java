package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import org.esprit.finovate.utils.ImageUtils;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class ForumsController {

    @FXML private Label titleLabel;
    @FXML private Button createForumBtn;
    @FXML private Label emptyLabel;
    @FXML private ListView<ForumItem> forumsList;

    private MainController mainController; // Optional - for backward compatibility
    private ForumsPageController forumsPageController; // New - for direct navigation
    private StackPane parentContentArea; // For loading posts view
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
        private String imageUrl;

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
        public String getImageUrl() { return imageUrl; }
        
        public void setUpvotes(int upvotes) { 
            this.upvotes = upvotes;
            this.score = upvotes - downvotes;
        }
        public void setDownvotes(int downvotes) { 
            this.downvotes = downvotes;
            this.score = upvotes - downvotes;
        }
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }

    @FXML
    public void initialize() {
        // Configurer la cellule personnalisée pour afficher les forums
        forumsList.setCellFactory(param -> new ForumCell());
        
        // Ajouter un espacement entre les cartes
        forumsList.setStyle(
            forumsList.getStyle() + 
            "-fx-background-color: transparent; " +
            "-fx-cell-size: 10; " +
            "-fx-fixed-cell-size: -1;"
        );
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        System.out.println("ForumsController: mainController défini = " + (mainController != null));
    }
    
    /**
     * Set the parent ForumsPageController and its content area for direct navigation
     */
    public void setForumsPageController(ForumsPageController forumsPageController, StackPane contentArea) {
        this.forumsPageController = forumsPageController;
        this.parentContentArea = contentArea;
        System.out.println("ForumsController: forumsPageController défini = " + (forumsPageController != null));
    }

    public void loadForums(String viewType, int userId) {
        this.currentViewType = viewType;
        this.currentUserId = userId;
        
        System.out.println("=== ForumsController.loadForums ===");
        System.out.println("viewType: " + viewType);
        System.out.println("userId: " + userId);
        System.out.println("mainController: " + (mainController != null ? "present" : "null"));
        System.out.println("forumsPageController: " + (forumsPageController != null ? "present" : "null"));
        System.out.println("parentContentArea: " + (parentContentArea != null ? "present" : "null"));

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
        
        System.out.println("\n=== CHARGEMENT DES FORUMS PUBLICS ===");

        String query = "SELECT f.id, f.title, f.description, f.creator_id, f.created_at, f.image_url, " +
                "(SELECT COUNT(*) FROM user_forum WHERE forum_id = f.id) as member_count " +
                "FROM forums f " +
                "ORDER BY f.created_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();
            int count = 0;

            while (rs.next()) {
                count++;
                ForumItem forum = new ForumItem(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("creator_id"),
                        rs.getInt("member_count"),
                        rs.getTimestamp("created_at")
                );
                
                // Charger l'image_url
                String imageUrl = rs.getString("image_url");
                forum.setImageUrl(imageUrl);
                System.out.println("Forum #" + count + " '" + forum.getName() + "' - image_url: " + (imageUrl != null ? imageUrl : "NULL"));
                
                allForums.add(forum);
            }
            
            System.out.println("Total forums chargés: " + count);

        } catch (SQLException e) {
            System.out.println("ERREUR lors du chargement des forums:");
            e.printStackTrace();
            showError("Erreur de chargement des forums");
        }

        filteredForums = FXCollections.observableArrayList(allForums);
        forumsList.setItems(filteredForums);
        emptyLabel.setVisible(filteredForums.isEmpty());
        emptyLabel.setManaged(filteredForums.isEmpty());
        
        System.out.println("=== FIN CHARGEMENT ===\n");
    }

    private void loadMyForums() {
        allForums.clear();

        String query = "SELECT f.id, f.title, f.description, f.creator_id, f.created_at, f.image_url, " +
                "(SELECT COUNT(*) FROM user_forum WHERE forum_id = f.id) as member_count " +
                "FROM forums f WHERE f.creator_id = ? ORDER BY f.created_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ForumItem forum = new ForumItem(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("creator_id"),
                        rs.getInt("member_count"),
                        rs.getTimestamp("created_at")
                );
                
                // Charger l'image_url
                String imageUrl = rs.getString("image_url");
                forum.setImageUrl(imageUrl);
                
                allForums.add(forum);
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
        
        System.out.println("\n=== CHARGEMENT DES FORUMS REJOINTS ===");
        System.out.println("currentUserId: " + currentUserId);

        // Requête simplifiée - afficher TOUS les forums rejoints (même ceux créés par l'utilisateur)
        String query = "SELECT f.id, f.title, f.description, f.creator_id, f.created_at, f.image_url, " +
                "(SELECT COUNT(*) FROM user_forum WHERE forum_id = f.id) as member_count " +
                "FROM forums f " +
                "INNER JOIN user_forum uf ON f.id = uf.forum_id " +
                "WHERE uf.user_id = ? " +
                "ORDER BY uf.joined_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentUserId);
            
            System.out.println("Exécution de la requête SQL...");
            ResultSet rs = stmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count++;
                int forumId = rs.getInt("id");
                int creatorId = rs.getInt("creator_id");
                
                ForumItem forum = new ForumItem(
                        forumId,
                        rs.getString("title"),
                        rs.getString("description"),
                        creatorId,
                        rs.getInt("member_count"),
                        rs.getTimestamp("created_at")
                );
                
                // Charger l'image_url
                String imageUrl = rs.getString("image_url");
                forum.setImageUrl(imageUrl);
                
                System.out.println("Forum rejoint #" + count + ": ID=" + forumId + ", Nom=" + forum.getName() + ", Créateur=" + creatorId);
                allForums.add(forum);
            }
            
            System.out.println("Total forums rejoints trouvés: " + count);
            
            // Si aucun forum trouvé, vérifier la table user_forum
            if (count == 0) {
                System.out.println("\n=== VÉRIFICATION TABLE user_forum ===");
                String checkQuery = "SELECT * FROM user_forum WHERE user_id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                    checkStmt.setInt(1, currentUserId);
                    ResultSet checkRs = checkStmt.executeQuery();
                    int userForumCount = 0;
                    while (checkRs.next()) {
                        userForumCount++;
                        System.out.println("user_forum entry: user_id=" + checkRs.getInt("user_id") + 
                                         ", forum_id=" + checkRs.getInt("forum_id") + 
                                         ", joined_at=" + checkRs.getTimestamp("joined_at"));
                    }
                    System.out.println("Total entrées user_forum pour userId " + currentUserId + ": " + userForumCount);
                }
            }

        } catch (SQLException e) {
            System.out.println("ERREUR SQL lors du chargement des forums rejoints:");
            e.printStackTrace();
            showError("Erreur de chargement des forums rejoints: " + e.getMessage());
        }

        filteredForums = FXCollections.observableArrayList(allForums);
        forumsList.setItems(filteredForums);
        emptyLabel.setVisible(filteredForums.isEmpty());
        emptyLabel.setManaged(filteredForums.isEmpty());
        
        if (filteredForums.isEmpty()) {
            System.out.println("⚠️ Aucun forum rejoint à afficher");
        }
        
        System.out.println("=== FIN CHARGEMENT FORUMS REJOINTS ===\n");
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
        // Open create forum dialog directly (works with or without MainController)
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/create-forum-dialog.fxml"));
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
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du dialogue de création");
        }
    }

    public void refreshForums() {
        loadForums(currentViewType, currentUserId);
    }

    private void editForum(ForumItem forum) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/create-forum-dialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier le Forum");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));

            CreateForumDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setForumsController(this);
            controller.setCurrentUserId(currentUserId);
            // Passer l'imageUrl au dialogue pour afficher l'image existante
            controller.setForumToEdit(forum.getId(), forum.getName(), forum.getDescription(), forum.getImageUrl());

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur d'ouverture du dialogue de modification");
        }
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
        System.out.println("\n=== REJOINDRE FORUM ===");
        System.out.println("forumId: " + forumId);
        System.out.println("currentUserId: " + currentUserId);
        
        String query = "INSERT INTO user_forum (forum_id, user_id, joined_at) VALUES (?, ?, NOW())";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, forumId);
            stmt.setInt(2, currentUserId);
            int rows = stmt.executeUpdate();
            
            System.out.println("Lignes insérées: " + rows);
            showInfo("Vous avez rejoint le forum !");
            refreshForums();

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                System.out.println("Forum déjà rejoint");
                showError("Vous avez déjà rejoint ce forum");
            } else {
                System.out.println("ERREUR lors de l'adhésion:");
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
        
        // Try to load posts view - use ForumsPageController if available, otherwise MainController
        if (parentContentArea != null) {
            // Direct navigation through ForumsPageController
            loadPostsViewDirect(forum.getId(), forum.getName());
        } else if (mainController != null) {
            // Legacy navigation through MainController
            mainController.showPostsView(forum.getId(), forum.getName());
        } else {
            System.err.println("ERREUR: Aucun contrôleur parent disponible pour ouvrir le forum!");
            showError("Erreur: Impossible d'ouvrir le forum. Veuillez réessayer.");
        }
    }
    
    /**
     * Load posts view directly into parent content area (without MainController)
     */
    private void loadPostsViewDirect(int forumId, String forumName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/posts.fxml"));
            Parent postsView = loader.load();

            PostsController controller = loader.getController();
            // Pass mainController if available (optional)
            if (mainController != null) {
                controller.setMainController(mainController);
            }
            // Set parent content area for navigation
            if (forumsPageController != null && parentContentArea != null) {
                controller.setForumsPageController(forumsPageController, parentContentArea);
            }
            controller.loadPosts(forumId, forumName, currentUserId);

            parentContentArea.getChildren().clear();
            parentContentArea.getChildren().add(postsView);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement des posts");
        }
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

    // Cellule personnalisée pour afficher un forum (style Reddit/Twitter moderne)
    private class ForumCell extends ListCell<ForumItem> {
        @Override
        protected void updateItem(ForumItem forum, boolean empty) {
            super.updateItem(forum, empty);

            if (empty || forum == null) {
                setGraphic(null);
                setText(null);
                setStyle("-fx-background-color: transparent; -fx-padding: 0;");
            } else {
                // Wrapper pour ajouter un espacement en bas
                VBox wrapper = new VBox();
                wrapper.setStyle("-fx-background-color: transparent; -fx-padding: 0 0 15 0;");
                
                // Card principale avec layout horizontal
                HBox card = new HBox(0);
                card.setPrefHeight(150);
                card.setStyle(
                    "-fx-background-color: white; " +
                    "-fx-border-color: #E0E0E0; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 12; " +
                    "-fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2); " +
                    "-fx-cursor: hand;"
                );

                // Image du forum sur toute la hauteur à gauche (120px de largeur)
                if (forum.getImageUrl() != null && !forum.getImageUrl().isEmpty()) {
                    System.out.println("Chargement de l'image: " + forum.getImageUrl());
                    ImageView forumImage = ImageUtils.loadImageView(forum.getImageUrl(), 120, 150);
                    if (forumImage != null) {
                        forumImage.setFitWidth(120);
                        forumImage.setFitHeight(150);
                        forumImage.setPreserveRatio(false);
                        
                        // Rendre l'image plus claire
                        javafx.scene.effect.ColorAdjust colorAdjust = new javafx.scene.effect.ColorAdjust();
                        colorAdjust.setBrightness(0.2); // Augmenter la luminosité
                        forumImage.setEffect(colorAdjust);
                        
                        // Clip pour arrondir les coins à gauche seulement
                        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(120, 150);
                        clip.setArcWidth(12);
                        clip.setArcHeight(12);
                        forumImage.setClip(clip);
                        
                        card.getChildren().add(forumImage);
                        System.out.println("Image chargée avec succès");
                    } else {
                        System.out.println("Échec du chargement de l'image, utilisation de l'icône par défaut");
                        VBox iconBox = createDefaultForumIconBox();
                        card.getChildren().add(iconBox);
                    }
                } else {
                    System.out.println("Pas d'image URL pour ce forum");
                    VBox iconBox = createDefaultForumIconBox();
                    card.getChildren().add(iconBox);
                }

                // Contenu à droite de l'image
                VBox contentBox = new VBox(8);
                contentBox.setPadding(new Insets(15, 20, 15, 20));
                HBox.setHgrow(contentBox, Priority.ALWAYS);
                
                // Titre du forum en gras
                Label nameLabel = new Label(forum.getName());
                nameLabel.setStyle(
                    "-fx-font-size: 22px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-text-fill: #2E7D32;"
                );
                nameLabel.setWrapText(true);

                // Métadonnées (membres + date)
                HBox metaBox = new HBox(15);
                Label membersLabel = new Label("👥 " + forum.getMemberCount() + " membres");
                membersLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666; -fx-font-weight: 600;");
                
                Label dateLabel = new Label("📅 " + forum.getCreatedAt().toString().substring(0, 10));
                dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
                
                metaBox.getChildren().addAll(membersLabel, dateLabel);

                // Description
                Label descLabel = new Label(forum.getDescription());
                descLabel.setWrapText(true);
                descLabel.setMaxWidth(Double.MAX_VALUE);
                descLabel.setStyle(
                    "-fx-text-fill: #555; " +
                    "-fx-font-size: 14px; " +
                    "-fx-line-spacing: 2px;"
                );
                
                // Limiter la description à 2 lignes
                descLabel.setMaxHeight(40);

                // Spacer pour pousser les boutons en bas
                Region spacer = new Region();
                VBox.setVgrow(spacer, Priority.ALWAYS);

                // Boutons d'action en bas (ICÔNES SEULEMENT avec tooltips)
                HBox actionBox = new HBox(15);
                actionBox.setAlignment(Pos.CENTER_LEFT);

                if (currentViewType.equals("myForums")) {
                    // Mes forums: boutons Modifier et Supprimer (icônes seulement)
                    Button editBtn = new Button("✏");
                    editBtn.setStyle(
                        "-fx-background-color: transparent; " +
                        "-fx-text-fill: #666; " +
                        "-fx-font-size: 18px; " +
                        "-fx-padding: 4 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-width: 0;"
                    );
                    Tooltip editTooltip = new Tooltip("Modifier");
                    editTooltip.setStyle("-fx-font-size: 12px;");
                    Tooltip.install(editBtn, editTooltip);
                    
                    editBtn.setOnMouseEntered(ev -> editBtn.setStyle(
                        "-fx-background-color: #FFF3E0; " +
                        "-fx-text-fill: #FF9800; " +
                        "-fx-font-size: 18px; " +
                        "-fx-padding: 4 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-width: 0; " +
                        "-fx-background-radius: 4;"
                    ));
                    editBtn.setOnMouseExited(ev -> editBtn.setStyle(
                        "-fx-background-color: transparent; " +
                        "-fx-text-fill: #666; " +
                        "-fx-font-size: 18px; " +
                        "-fx-padding: 4 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-width: 0;"
                    ));
                    editBtn.setOnAction(ev -> {
                        ev.consume();
                        editForum(forum);
                    });

                    Button deleteBtn = new Button("🗑");
                    deleteBtn.setStyle(
                        "-fx-background-color: transparent; " +
                        "-fx-text-fill: #666; " +
                        "-fx-font-size: 18px; " +
                        "-fx-padding: 4 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-width: 0;"
                    );
                    Tooltip deleteTooltip = new Tooltip("Supprimer");
                    deleteTooltip.setStyle("-fx-font-size: 12px;");
                    Tooltip.install(deleteBtn, deleteTooltip);
                    
                    deleteBtn.setOnMouseEntered(ev -> deleteBtn.setStyle(
                        "-fx-background-color: #FFEBEE; " +
                        "-fx-text-fill: #F44336; " +
                        "-fx-font-size: 18px; " +
                        "-fx-padding: 4 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-width: 0; " +
                        "-fx-background-radius: 4;"
                    ));
                    deleteBtn.setOnMouseExited(ev -> deleteBtn.setStyle(
                        "-fx-background-color: transparent; " +
                        "-fx-text-fill: #666; " +
                        "-fx-font-size: 18px; " +
                        "-fx-padding: 4 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-width: 0;"
                    ));
                    deleteBtn.setOnAction(ev -> {
                        ev.consume();
                        deleteForum(forum.getId());
                    });

                    actionBox.getChildren().addAll(editBtn, deleteBtn);
                    
                } else if (currentViewType.equals("joinedForums")) {
                    // Forums rejoints: bouton Quitter (icône seulement)
                    Button leaveBtn = new Button("👋");
                    leaveBtn.setStyle(
                        "-fx-background-color: transparent; " +
                        "-fx-text-fill: #666; " +
                        "-fx-font-size: 18px; " +
                        "-fx-padding: 4 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-width: 0;"
                    );
                    Tooltip leaveTooltip = new Tooltip("Quitter");
                    leaveTooltip.setStyle("-fx-font-size: 12px;");
                    Tooltip.install(leaveBtn, leaveTooltip);
                    
                    leaveBtn.setOnMouseEntered(ev -> leaveBtn.setStyle(
                        "-fx-background-color: #FFEBEE; " +
                        "-fx-text-fill: #F44336; " +
                        "-fx-font-size: 18px; " +
                        "-fx-padding: 4 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-width: 0; " +
                        "-fx-background-radius: 4;"
                    ));
                    leaveBtn.setOnMouseExited(ev -> leaveBtn.setStyle(
                        "-fx-background-color: transparent; " +
                        "-fx-text-fill: #666; " +
                        "-fx-font-size: 18px; " +
                        "-fx-padding: 4 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-width: 0;"
                    ));
                    leaveBtn.setOnAction(ev -> {
                        ev.consume();
                        leaveForum(forum.getId());
                    });
                    actionBox.getChildren().add(leaveBtn);
                    
                } else if (currentViewType.equals("accueil")) {
                    // Accueil: bouton Rejoindre si pas déjà membre (icône seulement)
                    if (forum.getCreatorId() != currentUserId) {
                        boolean isMember = checkIfMember(forum.getId());
                        
                        if (isMember) {
                            Button memberBtn = new Button("✓");
                            memberBtn.setStyle(
                                "-fx-background-color: transparent; " +
                                "-fx-text-fill: #666; " +
                                "-fx-font-size: 18px; " +
                                "-fx-padding: 4 8; " +
                                "-fx-cursor: hand; " +
                                "-fx-border-width: 0;"
                            );
                            Tooltip memberTooltip = new Tooltip("Membre - Cliquer pour quitter");
                            memberTooltip.setStyle("-fx-font-size: 12px;");
                            Tooltip.install(memberBtn, memberTooltip);
                            
                            memberBtn.setOnMouseEntered(ev -> memberBtn.setStyle(
                                "-fx-background-color: #FFEBEE; " +
                                "-fx-text-fill: #F44336; " +
                                "-fx-font-size: 18px; " +
                                "-fx-padding: 4 8; " +
                                "-fx-cursor: hand; " +
                                "-fx-border-width: 0; " +
                                "-fx-background-radius: 4;"
                            ));
                            memberBtn.setOnMouseExited(ev -> memberBtn.setStyle(
                                "-fx-background-color: transparent; " +
                                "-fx-text-fill: #666; " +
                                "-fx-font-size: 18px; " +
                                "-fx-padding: 4 8; " +
                                "-fx-cursor: hand; " +
                                "-fx-border-width: 0;"
                            ));
                            memberBtn.setOnAction(ev -> {
                                ev.consume();
                                leaveForum(forum.getId());
                            });
                            actionBox.getChildren().add(memberBtn);
                        } else {
                            Button joinBtn = new Button("+");
                            joinBtn.setStyle(
                                "-fx-background-color: transparent; " +
                                "-fx-text-fill: #666; " +
                                "-fx-font-size: 18px; " +
                                "-fx-padding: 4 8; " +
                                "-fx-cursor: hand; " +
                                "-fx-border-width: 0;"
                            );
                            Tooltip joinTooltip = new Tooltip("Rejoindre");
                            joinTooltip.setStyle("-fx-font-size: 12px;");
                            Tooltip.install(joinBtn, joinTooltip);
                            
                            joinBtn.setOnMouseEntered(ev -> joinBtn.setStyle(
                                "-fx-background-color: #E8F5E9; " +
                                "-fx-text-fill: #4CAF50; " +
                                "-fx-font-size: 18px; " +
                                "-fx-padding: 4 8; " +
                                "-fx-cursor: hand; " +
                                "-fx-border-width: 0; " +
                                "-fx-background-radius: 4;"
                            ));
                            joinBtn.setOnMouseExited(ev -> joinBtn.setStyle(
                                "-fx-background-color: transparent; " +
                                "-fx-text-fill: #666; " +
                                "-fx-font-size: 18px; " +
                                "-fx-padding: 4 8; " +
                                "-fx-cursor: hand; " +
                                "-fx-border-width: 0;"
                            ));
                            joinBtn.setOnAction(ev -> {
                                ev.consume();
                                joinForum(forum.getId());
                            });
                            actionBox.getChildren().add(joinBtn);
                        }
                    }
                }

                contentBox.getChildren().addAll(nameLabel, metaBox, descLabel, spacer, actionBox);
                card.getChildren().add(contentBox);
                
                // Effet hover
                card.setOnMouseEntered(e -> {
                    card.setStyle(
                        "-fx-background-color: #F5F5F5; " +
                        "-fx-border-color: #4CAF50; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 3); " +
                        "-fx-cursor: hand;"
                    );
                });
                
                card.setOnMouseExited(e -> {
                    card.setStyle(
                        "-fx-background-color: white; " +
                        "-fx-border-color: #E0E0E0; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2); " +
                        "-fx-cursor: hand;"
                    );
                });
                
                // Rendre toute la carte cliquable pour ouvrir le forum
                card.setOnMouseClicked(e -> {
                    openForum(forum);
                    e.consume();
                });
                
                wrapper.getChildren().add(card);
                setGraphic(wrapper);
                setStyle("-fx-background-color: transparent; -fx-padding: 0;");
            }
        }
    }

    private Connection getConnection() throws SQLException {
        return org.esprit.finovate.utils.DatabaseConfig.getConnection();
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

    // Méthodes utilitaires pour créer des boutons stylisés
    private Label createDefaultForumIcon() {
        Label iconLabel = new Label("🏛️");
        iconLabel.setStyle(
            "-fx-font-size: 32px; " +
            "-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2); " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 14; " +
            "-fx-min-width: 60; -fx-min-height: 60; " +
            "-fx-alignment: center;"
        );
        return iconLabel;
    }

    private VBox createDefaultForumIconBox() {
        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setPrefSize(120, 150);
        iconBox.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4CAF50, #2E7D32);"
        );
        
        Label iconLabel = new Label("🏛️");
        iconLabel.setStyle(
            "-fx-font-size: 48px; " +
            "-fx-text-fill: white;"
        );
        
        iconBox.getChildren().add(iconLabel);
        return iconBox;
    }
    
    private Button createStyledButton(String text, String bgColor, String hoverColor) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: " + bgColor + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 8 16; " +
            "-fx-background-radius: 20; " +
            "-fx-cursor: hand; " +
            "-fx-border-width: 0;"
        );
        
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + hoverColor + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 8 16; " +
            "-fx-background-radius: 20; " +
            "-fx-cursor: hand; " +
            "-fx-border-width: 0;"
        ));
        
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + bgColor + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 8 16; " +
            "-fx-background-radius: 20; " +
            "-fx-cursor: hand; " +
            "-fx-border-width: 0;"
        ));
        
        return btn;
    }
    
    private Button createOutlinedButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #0079D3; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 8 16; " +
            "-fx-border-color: #0079D3; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 20; " +
            "-fx-background-radius: 20; " +
            "-fx-cursor: hand;"
        );
        
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: #0079D3; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 8 16; " +
            "-fx-border-color: #0079D3; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 20; " +
            "-fx-background-radius: 20; " +
            "-fx-cursor: hand;"
        ));
        
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #0079D3; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 8 16; " +
            "-fx-border-color: #0079D3; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 20; " +
            "-fx-background-radius: 20; " +
            "-fx-cursor: hand;"
        ));
        
        return btn;
    }
}