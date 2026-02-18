package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.esprit.finovate.models.Investissement;
import org.esprit.finovate.models.Project;
import org.esprit.finovate.utils.SceneUtils;
import org.esprit.finovate.utils.Session;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MyInvestmentsController implements Initializable {

    @FXML private VBox investmentsContainer;
    @FXML private TextField txtSearchDynamic;

    private Stage stage;
    private List<Investissement> allInvestments = List.of();
    private final InvestissementController investissementController = new InvestissementController();
    private final ProjectController projectController = new ProjectController();

    public void setStage(Stage stage) { this.stage = stage; }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (txtSearchDynamic != null) txtSearchDynamic.textProperty().addListener((o, ov, nv) -> applyFiltersAndRender());
        loadInvestments();
    }

    public void refresh() {
        loadInvestments();
    }

    private void loadInvestments() {
        if (Session.currentUser == null) return;
        try {
            allInvestments = investissementController.getInvestissementsByInvestorId(Session.currentUser.getId());
            applyFiltersAndRender();
        } catch (SQLException e) {
            e.printStackTrace();
            allInvestments = List.of();
            showError();
        }
    }

    private void applyFiltersAndRender() {
        investmentsContainer.getChildren().clear();
        String search = txtSearchDynamic != null && txtSearchDynamic.getText() != null
                ? txtSearchDynamic.getText().trim().toLowerCase() : "";
        Map<Long, String> projectTitles = new HashMap<>();
        try {
            for (Project p : projectController.getAllProjects()) {
                projectTitles.put(p.getProject_id(), p.getTitle());
            }
        } catch (SQLException e) {
            // ignore
        }

        List<Investissement> filtered = allInvestments.stream()
                .filter(inv -> search.isEmpty() || matchesSearch(inv, projectTitles, search))
                .collect(Collectors.toList());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        if (filtered.isEmpty()) {
            Label empty = new Label("No investments match your search.");
            empty.getStyleClass().add("project-meta");
            investmentsContainer.getChildren().add(empty);
        } else {
            for (Investissement inv : filtered) {
                investmentsContainer.getChildren().add(createInvestmentCard(inv, projectTitles, sdf));
            }
        }
    }

    private boolean matchesSearch(Investissement inv, Map<Long, String> titles, String search) {
        String t = titles.getOrDefault(inv.getProject_id(), "").toLowerCase();
        return t.contains(search);
    }

    private void showError() {
        Label err = new Label("Error loading investments.");
        err.getStyleClass().add("error-label");
        investmentsContainer.getChildren().add(err);
    }

    private HBox createInvestmentCard(Investissement inv, Map<Long, String> projectTitles, SimpleDateFormat sdf) {
        HBox card = new HBox(16);
        card.getStyleClass().add("investment-card");
        card.setPrefWidth(680);

        VBox info = new VBox(6);
        String title = projectTitles.getOrDefault(inv.getProject_id(), "Project #" + inv.getProject_id());
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("project-title");
        titleLabel.setWrapText(true);

        String date = inv.getInvestment_date() != null ? sdf.format(inv.getInvestment_date()) : "—";
        Label meta = new Label(String.format("%.2f TND  •  %s  •  %s", inv.getAmount(), inv.getStatus(), date));
        meta.getStyleClass().add("project-meta");

        info.getChildren().addAll(titleLabel, meta);
        HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

        card.getChildren().add(info);

        boolean isPending = "PENDING".equals(inv.getStatus());
        if (isPending) {
            Button btnCancel = new Button("Cancel");
            btnCancel.getStyleClass().addAll("btn-danger", "btn-small");
            btnCancel.setOnAction(e -> handleDeleteInvestment(inv));
            card.getChildren().add(btnCancel);
        }

        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return card;
    }

    private void handleDeleteInvestment(Investissement inv) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Investment");
        confirm.setHeaderText("Cancel this investment request?");
        confirm.setContentText("Your " + inv.getAmount() + " TND request will be withdrawn.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                investissementController.deleteInvestissement(inv.getInvestissement_id());
                new Alert(Alert.AlertType.INFORMATION, "Investment withdrawn.").showAndWait();
                loadInvestments();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Failed: " + ex.getMessage()).showAndWait();
            }
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
