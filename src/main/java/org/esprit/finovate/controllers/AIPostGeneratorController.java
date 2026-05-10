package org.esprit.finovate.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.esprit.finovate.services.AIPostGeneratorService;
import org.esprit.finovate.utils.BadgeManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AIPostGeneratorController {

    @FXML private TextField themeField;
    @FXML private ComboBox<String> forumComboBox;
    @FXML private ComboBox<String> toneComboBox;
    @FXML private ComboBox<String> lengthComboBox;
    @FXML private Button generateButton;
    @FXML private VBox previewBox;
    @FXML private ImageView previewImage;
    @FXML private Label titleLabel;
    @FXML private TextArea contentArea;
    @FXML private Button publishButton;
    @FXML private Button chooseImageButton;
    @FXML private Label statusLabel;

    private MainController mainController;
    private long currentUserId;
    private long selectedForumId = -1;
    private AIPostGeneratorService.GeneratedPost currentPost;
    private String selectedImagePath = null;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        if (mainController != null) {
            this.currentUserId = mainController.getCurrentUserId();
            System.out.println("🔍 AIPostGenerator - User ID depuis MainController: " + currentUserId);
        } else {
            // Fallback: utiliser Session si mainController est null
            if (org.esprit.finovate.utils.Session.currentUser != null) {
                this.currentUserId = org.esprit.finovate.utils.Session.currentUser.getId();
                System.out.println("🔍 AIPostGenerator - User ID depuis Session: " + currentUserId);
            } else {
                System.err.println("❌ ERREUR: Impossible de récupérer currentUserId!");
            }
        }
    }

    @FXML
    public void initialize() {
        System.out.println("🔧 AIPostGenerator - initialize() appelé");
        
        // Initialiser currentUserId depuis Session si disponible
        if (org.esprit.finovate.utils.Session.currentUser != null) {
            this.currentUserId = org.esprit.finovate.utils.Session.currentUser.getId();
            System.out.println("🔍 AIPostGenerator - User ID depuis Session dans initialize: " + currentUserId);
        }
        
        // Initialiser les ComboBox
        toneComboBox.getItems().addAll("Professionnel", "Casual", "Éducatif", "Inspirant");
        toneComboBox.setValue("Professionnel");
        
        lengthComboBox.getItems().addAll("Court", "Moyen", "Long");
        lengthComboBox.setValue("Moyen");
        
        // Cacher l'aperçu au début
        previewBox.setVisible(false);
        previewBox.setManaged(false);
        
        // Charger les forums immédiatement si currentUserId est disponible
        if (currentUserId > 0) {
            System.out.println("🔄 Chargement des forums depuis initialize()");
            loadForums();
        } else {
            System.out.println("⚠️ currentUserId non disponible dans initialize(), attente de setMainController()");
        }
    }
    
    public void loadUserForums() {
        System.out.println("🔄 loadUserForums() appelé - currentUserId: " + currentUserId);
        // Appeler cette méthode après que currentUserId soit défini
        if (currentUserId > 0) {
            loadForums();
        } else {
            System.err.println("❌ ERREUR: currentUserId est 0 ou négatif!");
        }
    }

    private void loadForums() {
        System.out.println("\n=== 🔍 CHARGEMENT DES FORUMS ===");
        System.out.println("currentUserId: " + currentUserId);
        
        if (currentUserId <= 0) {
            System.err.println("❌ ERREUR: currentUserId invalide (" + currentUserId + ")");
            statusLabel.setText("⚠️ Erreur: Utilisateur non identifié");
            statusLabel.setStyle("-fx-text-fill: #F44336;");
            generateButton.setDisable(true);
            return;
        }
        
        // Vider le ComboBox avant de charger
        forumComboBox.getItems().clear();
        selectedForumId = -1;
        
        // Charger seulement les forums créés par l'utilisateur ou qu'il a rejoints
        // Utiliser UNION pour combiner les deux sources
        String query = "SELECT DISTINCT f.id, f.title FROM forums f " +
                      "WHERE f.creator_id = ? " +
                      "UNION " +
                      "SELECT DISTINCT f.id, f.title FROM forums f " +
                      "INNER JOIN user_forum uf ON f.id = uf.forum_id " +
                      "WHERE uf.user_id = ? " +
                      "ORDER BY title";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            System.out.println("📡 Connexion DB établie");
            System.out.println("🔍 Exécution requête SQL...");
            
            stmt.setLong(1, currentUserId);
            stmt.setLong(2, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                long forumId = rs.getLong("id");
                String forumTitle = rs.getString("title");
                forumComboBox.getItems().add(forumTitle);
                count++;
                
                System.out.println("  ✅ Forum #" + count + ": " + forumTitle + " (ID: " + forumId + ")");
                
                // Stocker l'ID du premier forum comme défaut
                if (selectedForumId == -1) {
                    selectedForumId = forumId;
                }
            }
            
            System.out.println("📊 Total forums chargés: " + count);
            
            if (!forumComboBox.getItems().isEmpty()) {
                forumComboBox.setValue(forumComboBox.getItems().get(0));
                System.out.println("✅ Forum par défaut sélectionné: " + forumComboBox.getValue());
                statusLabel.setText("✅ Prêt à générer");
                statusLabel.setStyle("-fx-text-fill: #4CAF50;");
                generateButton.setDisable(false);
            } else {
                System.out.println("⚠️ Aucun forum trouvé pour cet utilisateur");
                statusLabel.setText("⚠️ Vous devez créer ou rejoindre un forum d'abord");
                statusLabel.setStyle("-fx-text-fill: #FF9800;");
                generateButton.setDisable(true);
                
                // Vérifier si des forums existent dans la DB
                String checkQuery = "SELECT COUNT(*) as total FROM forums";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                    ResultSet checkRs = checkStmt.executeQuery();
                    if (checkRs.next()) {
                        int totalForums = checkRs.getInt("total");
                        System.out.println("ℹ️ Total forums dans la DB: " + totalForums);
                    }
                }
                
                // Vérifier les forums créés par l'utilisateur
                String myForumsQuery = "SELECT COUNT(*) as total FROM forums WHERE creator_id = ?";
                try (PreparedStatement myStmt = conn.prepareStatement(myForumsQuery)) {
                    myStmt.setLong(1, currentUserId);
                    ResultSet myRs = myStmt.executeQuery();
                    if (myRs.next()) {
                        int myForums = myRs.getInt("total");
                        System.out.println("ℹ️ Forums créés par userId " + currentUserId + ": " + myForums);
                    }
                }
                
                // Vérifier les forums rejoints
                String joinedQuery = "SELECT COUNT(*) as total FROM user_forum WHERE user_id = ?";
                try (PreparedStatement joinedStmt = conn.prepareStatement(joinedQuery)) {
                    joinedStmt.setLong(1, currentUserId);
                    ResultSet joinedRs = joinedStmt.executeQuery();
                    if (joinedRs.next()) {
                        int joinedForums = joinedRs.getInt("total");
                        System.out.println("ℹ️ Forums rejoints par userId " + currentUserId + ": " + joinedForums);
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR lors du chargement des forums:");
            e.printStackTrace();
            showError("Erreur lors du chargement des forums: " + e.getMessage());
            statusLabel.setText("❌ Erreur de chargement");
            statusLabel.setStyle("-fx-text-fill: #F44336;");
            generateButton.setDisable(true);
        }
        
        System.out.println("=== FIN CHARGEMENT FORUMS ===\n");
        
        // Listener pour mettre à jour l'ID du forum sélectionné
        forumComboBox.setOnAction(e -> updateSelectedForum());
    }

    private void updateSelectedForum() {
        String selectedForumTitle = forumComboBox.getValue();
        if (selectedForumTitle == null) return;
        
        // Vérifier que le forum sélectionné appartient bien à l'utilisateur (créé ou rejoint)
        // Utiliser UNION pour combiner les deux sources
        String query = "SELECT f.id FROM forums f " +
                      "WHERE f.title = ? AND f.creator_id = ? " +
                      "UNION " +
                      "SELECT f.id FROM forums f " +
                      "INNER JOIN user_forum uf ON f.id = uf.forum_id " +
                      "WHERE f.title = ? AND uf.user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, selectedForumTitle);
            stmt.setLong(2, currentUserId);
            stmt.setString(3, selectedForumTitle);
            stmt.setLong(4, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                selectedForumId = rs.getLong("id");
                System.out.println("✅ Forum sélectionné: " + selectedForumTitle + " (ID: " + selectedForumId + ")");
            } else {
                System.err.println("⚠️ Forum non autorisé: " + selectedForumTitle);
                selectedForumId = -1;
                showError("Vous n'avez pas accès à ce forum");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour du forum sélectionné:");
            e.printStackTrace();
        }
    }

    @FXML
    private void generatePost() {
        String theme = themeField.getText().trim();
        if (theme.isEmpty()) {
            showError("Veuillez entrer un thème");
            return;
        }
        
        if (selectedForumId == -1) {
            showError("Veuillez sélectionner un forum");
            return;
        }
        
        String tone = toneComboBox.getValue();
        String length = lengthComboBox.getValue();
        
        // Désactiver le bouton
        generateButton.setDisable(true);
        statusLabel.setText("⏳ Génération du post...");
        statusLabel.setStyle("-fx-text-fill: #0079D3;");
        
        // Générer en arrière-plan
        new Thread(() -> {
            System.out.println("🎨 === GÉNÉRATION POST ===");
            System.out.println("   Thème: " + theme);
            System.out.println("   Ton: " + tone);
            System.out.println("   Longueur: " + length);
            
            AIPostGeneratorService.GeneratedPost post = 
                AIPostGeneratorService.generatePost(theme, tone, length);
            
            System.out.println("📝 Post généré:");
            System.out.println("   Titre: " + post.title);
            System.out.println("   Contenu: " + post.content.substring(0, Math.min(100, post.content.length())) + "...");
            
            Platform.runLater(() -> {
                currentPost = post;
                displayPost(post);
                generateButton.setDisable(false);
                statusLabel.setText("✅ Post généré ! Choisissez une image.");
                statusLabel.setStyle("-fx-text-fill: #4CAF50;");
            });
        }).start();
    }

    private void displayPost(AIPostGeneratorService.GeneratedPost post) {
        System.out.println("🖼️ === AFFICHAGE PREVIEW ===");
        
        // Afficher l'aperçu
        previewBox.setVisible(true);
        previewBox.setManaged(true);
        
        // Titre
        titleLabel.setText(post.title);
        System.out.println("   Titre: " + post.title);
        
        // Contenu
        contentArea.setText(post.content);
        System.out.println("   Contenu: " + post.content.length() + " caractères");
        
        // Pas d'image automatique - l'utilisateur doit choisir
        previewImage.setVisible(false);
        previewImage.setManaged(false);
        selectedImagePath = null;
        
        System.out.println("   ⚠️ Aucune image - l'utilisateur doit choisir");
        System.out.println("✅ === PREVIEW AFFICHÉE ===");
    }

    @FXML
    private void chooseImage() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
            new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        java.io.File selectedFile = fileChooser.showOpenDialog(chooseImageButton.getScene().getWindow());
        
        if (selectedFile != null) {
            try {
                // Copier l'image dans le dossier uploads
                java.nio.file.Path uploadsDir = java.nio.file.Paths.get("uploads");
                if (!java.nio.file.Files.exists(uploadsDir)) {
                    java.nio.file.Files.createDirectories(uploadsDir);
                }
                
                String filename = "post_" + System.currentTimeMillis() + "_" + selectedFile.getName();
                java.nio.file.Path targetPath = uploadsDir.resolve(filename);
                java.nio.file.Files.copy(selectedFile.toPath(), targetPath, 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                selectedImagePath = targetPath.toString();
                
                // Afficher l'image dans le preview
                Image image = new Image("file:" + selectedImagePath);
                previewImage.setImage(image);
                previewImage.setVisible(true);
                previewImage.setManaged(true);
                
                statusLabel.setText("✅ Image choisie !");
                statusLabel.setStyle("-fx-text-fill: #4CAF50;");
                
                System.out.println("✅ Image choisie: " + selectedImagePath);
                
            } catch (Exception e) {
                System.err.println("❌ Erreur lors du choix de l'image: " + e.getMessage());
                e.printStackTrace();
                statusLabel.setText("❌ Erreur lors du choix de l'image");
                statusLabel.setStyle("-fx-text-fill: #F44336;");
            }
        }
    }

    @FXML
    private void publishPost() {
        if (currentPost == null) return;
        
        String title = titleLabel.getText();
        String content = contentArea.getText();
        
        publishButton.setDisable(true);
        statusLabel.setText("⏳ Publication en cours...");
        
        new Thread(() -> {
            // Utiliser l'image choisie par l'utilisateur (peut être null)
            boolean success = savePostToDatabase(title, content, selectedImagePath);
            
            Platform.runLater(() -> {
                if (success) {
                    statusLabel.setText("✅ Post publié avec succès !");
                    statusLabel.setStyle("-fx-text-fill: #4CAF50;");
                    
                    // Réinitialiser après 2 secondes
                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                            Platform.runLater(this::resetForm);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                } else {
                    statusLabel.setText("❌ Erreur lors de la publication");
                    statusLabel.setStyle("-fx-text-fill: #F44336;");
                    publishButton.setDisable(false);
                }
            });
        }).start();
    }

    private boolean savePostToDatabase(String title, String content, String imagePath) {
        System.out.println("💾 === DÉBUT ENREGISTREMENT POST ===");
        System.out.println("  Forum ID: " + selectedForumId);
        System.out.println("  Title: " + title);
        System.out.println("  Author ID: " + currentUserId);
        System.out.println("  Image: " + imagePath);
        
        // VÉRIFICATION DE SÉCURITÉ: L'utilisateur doit avoir créé ou rejoint le forum
        // Utiliser UNION pour combiner les deux sources
        String checkPermissionQuery = "SELECT 1 FROM forums f " +
                                     "WHERE f.id = ? AND f.creator_id = ? " +
                                     "UNION " +
                                     "SELECT 1 FROM forums f " +
                                     "INNER JOIN user_forum uf ON f.id = uf.forum_id " +
                                     "WHERE f.id = ? AND uf.user_id = ?";
        
        try (Connection conn = getConnection()) {
            System.out.println("✅ Connexion DB établie");
            
            // Vérifier les permissions
            try (PreparedStatement checkStmt = conn.prepareStatement(checkPermissionQuery)) {
                checkStmt.setLong(1, selectedForumId);
                checkStmt.setLong(2, currentUserId);
                checkStmt.setLong(3, selectedForumId);
                checkStmt.setLong(4, currentUserId);
                ResultSet permRs = checkStmt.executeQuery();
                
                if (!permRs.next()) {
                    System.err.println("❌ ERREUR: Utilisateur non autorisé à poster dans ce forum !");
                    showError("Vous n'avez pas la permission de poster dans ce forum");
                    return false;
                }
                System.out.println("✅ Permission vérifiée");
            }
            
            // Essayer d'abord avec image_url
            String queryWithImage = "INSERT INTO posts (forum_id, title, content, author_id, image_url, created_at) " +
                                   "VALUES (?, ?, ?, ?, ?, NOW())";
            String queryWithoutImage = "INSERT INTO posts (forum_id, title, content, author_id, created_at) " +
                                      "VALUES (?, ?, ?, ?, NOW())";
            
            // Essayer avec image_url
            try (PreparedStatement stmt = conn.prepareStatement(queryWithImage)) {
                stmt.setLong(1, selectedForumId);
                stmt.setString(2, title);
                stmt.setString(3, content);
                stmt.setLong(4, currentUserId);
                stmt.setString(5, imagePath);
                
                System.out.println("🔄 Exécution INSERT avec image_url...");
                int rows = stmt.executeUpdate();
                System.out.println("✅ INSERT réussi ! Lignes affectées: " + rows);
                
                if (rows > 0) {
                    // Vérifier que le post existe vraiment
                    String checkQuery = "SELECT id FROM posts WHERE title = ? AND author_id = ? ORDER BY created_at DESC LIMIT 1";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                        checkStmt.setString(1, title);
                        checkStmt.setLong(2, currentUserId);
                        ResultSet rs = checkStmt.executeQuery();
                        if (rs.next()) {
                            long postId = rs.getLong("id");
                            System.out.println("✅ Post vérifié dans la DB ! ID: " + postId);
                            
                            // Vérifier et attribuer les badges
                            BadgeManager.checkPostBadges(currentUserId);
                            
                            return true;
                        } else {
                            System.err.println("❌ ERREUR: Post non trouvé après insertion !");
                            return false;
                        }
                    }
                } else {
                    System.err.println("❌ ERREUR: Aucune ligne insérée !");
                    return false;
                }
                
            } catch (Exception e) {
                System.out.println("⚠️ Colonne image_url non trouvée, essai sans image...");
                System.out.println("   Erreur: " + e.getMessage());
                
                // Essayer sans image_url
                try (PreparedStatement stmt = conn.prepareStatement(queryWithoutImage)) {
                    stmt.setLong(1, selectedForumId);
                    stmt.setString(2, title);
                    stmt.setString(3, content);
                    stmt.setLong(4, currentUserId);
                    
                    System.out.println("🔄 Exécution INSERT sans image_url...");
                    int rows = stmt.executeUpdate();
                    System.out.println("✅ INSERT réussi ! Lignes affectées: " + rows);
                    
                    if (rows > 0) {
                        // Vérifier que le post existe vraiment
                        String checkQuery = "SELECT id FROM posts WHERE title = ? AND author_id = ? ORDER BY created_at DESC LIMIT 1";
                        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                            checkStmt.setString(1, title);
                            checkStmt.setLong(2, currentUserId);
                            ResultSet rs = checkStmt.executeQuery();
                            if (rs.next()) {
                                long postId = rs.getLong("id");
                                System.out.println("✅ Post vérifié dans la DB ! ID: " + postId);
                                
                                // Vérifier et attribuer les badges
                                BadgeManager.checkPostBadges(currentUserId);
                                
                                return true;
                            } else {
                                System.err.println("❌ ERREUR: Post non trouvé après insertion !");
                                return false;
                            }
                        }
                    } else {
                        System.err.println("❌ ERREUR: Aucune ligne insérée !");
                        return false;
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR CRITIQUE lors de la création du post:");
            e.printStackTrace();
            return false;
        }
    }

    private void resetForm() {
        themeField.clear();
        previewBox.setVisible(false);
        previewBox.setManaged(false);
        currentPost = null;
        selectedImagePath = null;
        statusLabel.setText("");
        publishButton.setDisable(false);
    }

    private Connection getConnection() throws Exception {
        String url = "jdbc:mysql://localhost:3306/finovate";
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
