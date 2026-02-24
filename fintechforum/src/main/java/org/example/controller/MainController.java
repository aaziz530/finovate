package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.collections.FXCollections;

import java.io.IOException;

public class MainController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortBox;
    @FXML private Button accueilBtn;
    @FXML private Button mesForumsBtn;
    @FXML private Button forumsRejointsBtn;
    @FXML private Button postesPartagesBtn;
    @FXML private StackPane contentArea;
    @FXML private Label statusLabel;
    @FXML private Label userInfoLabel;

    private String currentView = "myForums"; // "accueil", "myForums", "joinedForums", "sharedPosts"
    private Long currentUserId = 1L; // ID de l'utilisateur connecté
    private ForumsController currentForumsController; // Référence au controller actuel

    @FXML
    public void initialize() {
        // Initialiser le ComboBox de tri
        sortBox.setItems(FXCollections.observableArrayList(
                "Plus récents",
                "Plus anciens",
                "Plus populaires",
                "A-Z",
                "Z-A"
        ));
        sortBox.setValue("Plus récents");

        // Gestionnaire de recherche
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            onSearch(newVal);
        });

        // Gestionnaire de tri
        sortBox.setOnAction(e -> onSortChange());

        // Charger la vue par défaut (Accueil)
        showAccueil();
    }

    @FXML
    private void showAccueil() {
        currentView = "accueil";
        updateNavButtonStyles(accueilBtn);
        loadForumsView("accueil"); // tous les forums publics
        statusLabel.setText("Affichage: Accueil - Tous les Forums");
    }

    @FXML
    private void showMyForums() {
        currentView = "myForums";
        updateNavButtonStyles(mesForumsBtn);
        loadForumsView("myForums"); // mes forums
        statusLabel.setText("Affichage: Mes Forums");
    }

    @FXML
    private void showJoinedForums() {
        currentView = "joinedForums";
        updateNavButtonStyles(forumsRejointsBtn);
        loadForumsView("joinedForums"); // forums rejoints
        statusLabel.setText("Affichage: Forums Rejoints");
    }

    @FXML
    private void showSharedPosts() {
        currentView = "sharedPosts";
        updateNavButtonStyles(postesPartagesBtn);
        loadSharedPostsView();
        statusLabel.setText("Affichage: Postes Partagés");
    }

    private void loadSharedPostsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/shared-posts.fxml"));
            Parent sharedPostsView = loader.load();

            SharedPostsController controller = loader.getController();
            controller.setMainController(this);
            controller.loadSharedPosts(currentUserId.intValue());

            contentArea.getChildren().clear();
            contentArea.getChildren().add(sharedPostsView);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement des postes partagés");
        }
    }

    private void loadForumsView(String viewType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/forums.fxml"));
            Parent forumsView = loader.load();

            // Passer les paramètres au controller
            ForumsController controller = loader.getController();
            controller.setMainController(this);
            controller.loadForums(viewType, currentUserId.intValue());
            
            // Garder la référence au controller
            currentForumsController = controller;

            contentArea.getChildren().clear();
            contentArea.getChildren().add(forumsView);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement de la vue forums");
        }
    }

    private void updateNavButtonStyles(Button activeButton) {
        accueilBtn.getStyleClass().remove("nav-button-active");
        mesForumsBtn.getStyleClass().remove("nav-button-active");
        forumsRejointsBtn.getStyleClass().remove("nav-button-active");
        postesPartagesBtn.getStyleClass().remove("nav-button-active");

        accueilBtn.getStyleClass().add("nav-button");
        mesForumsBtn.getStyleClass().add("nav-button");
        forumsRejointsBtn.getStyleClass().add("nav-button");
        postesPartagesBtn.getStyleClass().add("nav-button");

        activeButton.getStyleClass().remove("nav-button");
        activeButton.getStyleClass().add("nav-button-active");
    }

    private void onSearch(String searchText) {
        if (currentForumsController != null) {
            currentForumsController.applySearch(searchText);
        }
    }

    private void onSortChange() {
        String sortType = sortBox.getValue();
        if (currentForumsController != null) {
            currentForumsController.applySort(sortType);
        }
    }

    public void showPostsView(int forumId, String forumName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/posts.fxml"));
            Parent postsView = loader.load();

            PostsController controller = loader.getController();
            controller.setMainController(this);
            controller.loadPosts(forumId, forumName, currentUserId.intValue());

            contentArea.getChildren().clear();
            contentArea.getChildren().add(postsView);

            statusLabel.setText("Forum: " + forumName);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement des posts");
        }
    }

    public void showPostDetailsView(int postId, String postTitle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/post-details.fxml"));
            Parent postDetailsView = loader.load();

            PostDetailsController controller = loader.getController();
            controller.setMainController(this);
            controller.loadPostDetails(postId, currentUserId.intValue());

            contentArea.getChildren().clear();
            contentArea.getChildren().add(postDetailsView);

            statusLabel.setText("Post: " + postTitle);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement du post");
        }
    }

    public void goBackToForums() {
        if (currentView.equals("accueil")) {
            showAccueil();
        } else if (currentView.equals("myForums")) {
            showMyForums();
        } else if (currentView.equals("sharedPosts")) {
            showSharedPosts();
        } else {
            showJoinedForums();
        }
    }

    /**
     * Affiche les détails d'un post (appelé depuis AlertsController)
     */
    public void showPostDetails(int postId, int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/post-details.fxml"));
            Parent postDetailsView = loader.load();

            PostDetailsController controller = loader.getController();
            controller.setMainController(this);
            controller.loadPostDetails(postId, userId);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(postDetailsView);

            statusLabel.setText("Détails du post");

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement du post");
        }
    }

    /**
     * Affiche la page des alertes
     */
    @FXML
    private void showAlerts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/alerts-view.fxml"));
            Parent alertsView = loader.load();

            AlertsController controller = loader.getController();
            controller.setMainController(this);
            controller.loadAlerts(currentUserId.intValue());

            contentArea.getChildren().clear();
            contentArea.getChildren().add(alertsView);

            statusLabel.setText("Mes Alertes");

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement des alertes");
        }
    }

    /**
     * Affiche la page des recommandations AI
     */
    @FXML
    private void showRecommendations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/recommendations-view.fxml"));
            Parent recommendationsView = loader.load();

            RecommendationsController controller = loader.getController();
            controller.setMainController(this);
            controller.loadRecommendations(currentUserId.intValue());

            contentArea.getChildren().clear();
            contentArea.getChildren().add(recommendationsView);

            statusLabel.setText("Recommandations AI");

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement des recommandations");
        }
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}