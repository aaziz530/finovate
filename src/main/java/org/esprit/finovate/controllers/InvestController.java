package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.esprit.finovate.models.Project;
import org.esprit.finovate.utils.ImageUtils;
import org.esprit.finovate.utils.SceneUtils;
import org.esprit.finovate.utils.ValidationUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class InvestController implements Initializable {

    @FXML private VBox projectsContainer;
    @FXML private TextField txtSearchDynamic;

    private Stage stage;
    private List<Project> allProjects = List.of();
    private final ProjectController projectController = new ProjectController();
    private final InvestissementController investissementController = new InvestissementController();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (txtSearchDynamic != null) {
            txtSearchDynamic.textProperty().addListener((o, ov, nv) -> applyFiltersAndRender());
        }
        loadProjects();
    }

    private void loadProjects() {
        try {
            List<Project> all = projectController.getAllProjects();
            if (org.esprit.finovate.utils.Session.currentUser != null) {
                Long myId = org.esprit.finovate.utils.Session.currentUser.getId();
                allProjects = all.stream()
                        .filter(p -> p.getOwner_id() == null || !p.getOwner_id().equals(myId))
                        .collect(Collectors.toList());
            } else {
                allProjects = all;
            }
            applyFiltersAndRender();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void applyFiltersAndRender() {
        projectsContainer.getChildren().clear();
        String search = txtSearchDynamic != null && txtSearchDynamic.getText() != null
                ? txtSearchDynamic.getText().trim().toLowerCase() : "";
        List<Project> filtered = allProjects.stream()
                .filter(p -> search.isEmpty() || matchesSearch(p, search))
                .collect(Collectors.toList());

        double remaining;
        for (Project p : filtered) {
            remaining = p.getGoal_amount() - p.getCurrent_amount();
            boolean canInvest = "OPEN".equals(p.getStatus()) && remaining > 0;
            VBox card = createProjectCard(p, canInvest);
            projectsContainer.getChildren().add(card);
        }

        if (filtered.isEmpty()) {
            Label empty = new Label("No projects to invest in.");
            empty.getStyleClass().add("project-meta");
            projectsContainer.getChildren().add(empty);
        }
    }

    private boolean matchesSearch(Project p, String search) {
        String t = p.getTitle() != null ? p.getTitle().toLowerCase() : "";
        String d = p.getDescription() != null ? p.getDescription().toLowerCase() : "";
        return t.contains(search) || d.contains(search);
    }

    private VBox createProjectCard(Project p, boolean canInvest) {
        VBox card = new VBox(10);
        card.getStyleClass().add("project-card");
        card.setPrefWidth(900);

        String resolved = ImageUtils.resolveImagePath(p.getImagePath());
        if (resolved != null) {
            try {
                ImageView iv = new ImageView(new Image("file:" + resolved));
                iv.setFitWidth(120);
                iv.setFitHeight(80);
                iv.setPreserveRatio(true);
                card.getChildren().add(iv);
            } catch (Exception ignored) {}
        }

        Label title = new Label(p.getTitle());
        title.getStyleClass().add("project-title");
        title.setWrapText(true);
        card.getChildren().add(title);

        String desc = p.getDescription();
        if (desc != null && !desc.isEmpty()) {
            String shortDesc = desc.length() > 120 ? desc.substring(0, 120) + "..." : desc;
            Label descLabel = new Label(shortDesc);
            descLabel.getStyleClass().add("project-desc");
            descLabel.setWrapText(true);
            card.getChildren().add(descLabel);
        }

        double progress = p.getGoal_amount() > 0 ? Math.min(1.0, p.getCurrent_amount() / p.getGoal_amount()) : 0;
        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.getStyleClass().add("project-progress-bar");
        progressBar.setMaxWidth(Double.MAX_VALUE);
        Label meta = new Label(String.format("%.2f TND / %.2f TND  •  %s", p.getCurrent_amount(), p.getGoal_amount(), p.getStatus()));
        meta.getStyleClass().add("project-meta");
        card.getChildren().add(progressBar);
        card.getChildren().add(meta);

        if (canInvest) {
            TextField txtAmount = new TextField();
            txtAmount.setPromptText("Amount (TND)...");
            txtAmount.setPrefWidth(120);
            Button btnInvest = new Button("Invest");
            btnInvest.getStyleClass().addAll("btn-primary", "btn-small");
            btnInvest.setOnAction(e -> handleInvest(p, txtAmount));
            HBox investRow = new HBox(8);
            investRow.getChildren().addAll(txtAmount, btnInvest);
            investRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            card.getChildren().add(investRow);
        } else {
            Label status = new Label("Fully funded or closed.");
            status.getStyleClass().add("project-meta");
            card.getChildren().add(status);
        }

        return card;
    }

    private void handleInvest(Project p, TextField txtAmount) {
        String amountStr = txtAmount.getText() == null ? "" : txtAmount.getText().trim();
        double remaining = p.getGoal_amount() - p.getCurrent_amount();
        String err = ValidationUtils.validateInvestmentAmount(amountStr, remaining > 0 ? remaining : null);
        if (err != null) {
            new Alert(Alert.AlertType.WARNING, err).showAndWait();
            return;
        }
        double amount = ValidationUtils.parseAmount(amountStr);
        try {
            investissementController.addInvestissement(p.getProject_id(), amount);
            new Alert(Alert.AlertType.INFORMATION, String.format("%.2f TND — Request sent. Check My Investments for status.", amount)).showAndWait();
            txtAmount.clear();
            loadProjects();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Failed: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML
    private void handleBack() throws IOException {
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
