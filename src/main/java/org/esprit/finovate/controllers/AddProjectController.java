package org.esprit.finovate.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.esprit.finovate.api.ApiConfig;
import org.esprit.finovate.api.MapPicker;
import org.esprit.finovate.api.UnsplashService;
import org.esprit.finovate.utils.ImageUtils;
import org.esprit.finovate.utils.LiveValidationHelper;
import org.esprit.finovate.utils.SceneUtils;
import org.esprit.finovate.utils.ValidationUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class AddProjectController implements Initializable {

    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtGoalAmount;
    @FXML private DatePicker dateDeadline;
    @FXML private ComboBox<String> comboCategory;
    @FXML private Label lblImagePath;
    @FXML private Label lblError;
    @FXML private TextField txtUnsplashSearch;
    @FXML private FlowPane flowUnsplashResults;
    @FXML private Label lblLocation;

    private Stage stage;
    private String selectedImagePath;
    private Double selectedLat;
    private Double selectedLng;

    private DashboardController dashboardController;
    private final ProjectController projectController = new ProjectController();
    private final UnsplashService unsplashService = new UnsplashService();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setDashboardController(DashboardController ctrl) {
        this.dashboardController = ctrl;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LiveValidationHelper.bind(txtTitle, s -> ValidationUtils.validateTitle(s));
        LiveValidationHelper.bind(txtDescription, s -> ValidationUtils.validateDescription(s));
        LiveValidationHelper.bind(txtGoalAmount, s -> ValidationUtils.validateGoalAmount(s));
        LiveValidationHelper.bind(dateDeadline, d -> d == null ? null : ValidationUtils.validateDeadline(d, "Deadline"));
        if (comboCategory != null) {
            comboCategory.getItems().addAll("Technology", "Agriculture", "Commerce", "Education", "Health", "Transport", "Other");
        }
    }

    @FXML
    private void handleSearchUnsplash() {
        if (flowUnsplashResults == null || txtUnsplashSearch == null) return;
        if (!ApiConfig.hasUnsplashKey()) {
            new Alert(Alert.AlertType.WARNING, "Add unsplash.access.key in src/main/resources/api_config.properties").showAndWait();
            return;
        }
        String q = txtUnsplashSearch.getText() != null ? txtUnsplashSearch.getText().trim() : "";
        if (q.isEmpty()) q = txtTitle.getText() != null ? txtTitle.getText().trim() : "";
        if (q.isEmpty()) q = "project";
        final String searchQuery = q;
        flowUnsplashResults.getChildren().clear();
        flowUnsplashResults.getChildren().add(new Label("Searching..."));
        new Thread(() -> {
            List<UnsplashService.UnsplashPhoto> photos = unsplashService.searchPhotos(searchQuery, 8);
            Platform.runLater(() -> {
                flowUnsplashResults.getChildren().clear();
                for (UnsplashService.UnsplashPhoto ph : photos) {
                    final UnsplashService.UnsplashPhoto photo = ph;
                    try {
                        ImageView iv = new ImageView(new Image(photo.urlSmall(), true));
                        iv.setFitWidth(100);
                        iv.setFitHeight(80);
                        iv.setPreserveRatio(true);
                        iv.setOnMouseClicked(e -> {
                            selectedImagePath = photo.urlRegular();
                            String alt = photo.alt();
                            lblImagePath.setText("Unsplash: " + (alt.length() > 30 ? alt.substring(0, 30) + "..." : alt));
                        });
                        flowUnsplashResults.getChildren().add(iv);
                    } catch (Exception ignored) {}
                }
            });
        }).start();
    }

    @FXML
    private void handlePickLocation() {
        Stage mapStage = new Stage();
        mapStage.initModality(Modality.APPLICATION_MODAL);
        mapStage.setTitle("Pick Project Location");
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        javafx.scene.layout.VBox pickerPanel = MapPicker.createPickerWebView(
            (lat, lng) -> {
                selectedLat = lat;
                selectedLng = lng;
                if (lblLocation != null) lblLocation.setText(String.format("%.4f, %.4f", lat, lng));
                mapStage.close();
            },
            mapStage::close
        );
        root.getChildren().add(pickerPanel);
        mapStage.setScene(new Scene(root, 640, 520));
        mapStage.showAndWait();
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Project Image");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File f = fc.showOpenDialog(stage);
        if (f != null) {
            selectedImagePath = ImageUtils.saveProjectImage(f.getAbsolutePath());
            lblImagePath.setText(selectedImagePath != null ? f.getName() : "Failed to save image");
        }
    }

    @FXML
    private void handleCreate() {
        lblError.setVisible(false);
        lblError.setManaged(false);

        String title = txtTitle.getText() == null ? "" : txtTitle.getText().trim();
        String desc = txtDescription.getText() == null ? "" : txtDescription.getText().trim();
        String goalStr = txtGoalAmount.getText() == null ? "" : txtGoalAmount.getText().trim();

        String err = ValidationUtils.validateTitle(title);
        if (err != null) { showError(err); return; }
        err = ValidationUtils.validateDescription(desc);
        if (err != null) { showError(err); return; }
        err = ValidationUtils.validateGoalAmount(goalStr);
        if (err != null) { showError(err); return; }

        LocalDate deadlineDate = dateDeadline.getValue();
        if (deadlineDate != null) {
            err = ValidationUtils.validateDeadline(deadlineDate, "Deadline");
            if (err != null) { showError(err); return; }
        }

        double goalAmount = ValidationUtils.parseAmount(goalStr);
        Date deadline = deadlineDate != null
                ? Date.from(deadlineDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
                : new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30));

        String category = comboCategory != null && comboCategory.getValue() != null ? comboCategory.getValue().trim() : null;
        if (category != null && category.isEmpty()) category = null;

        try {
            projectController.addProject(title, desc, goalAmount, deadline, selectedImagePath, selectedLat, selectedLng, category);
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Success");
            success.setHeaderText("Project Created");
            success.setContentText("Your project has been added successfully and saved to the database.");
            success.showAndWait();
            handleCancel();
        } catch (Exception e) {
            showError("Failed to create project. Check database connection.");
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    @FXML
    private void handleCancel() throws IOException {
        goBackToDashboard();
    }

    private void goBackToDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
        Parent root = loader.load();
        DashboardController ctrl = loader.getController();
        ctrl.setStage(stage);
        ctrl.refresh();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Finovate - Dashboard");
        SceneUtils.applyStageSize(stage);
    }
}
