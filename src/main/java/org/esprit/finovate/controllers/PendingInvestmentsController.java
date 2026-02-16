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
import org.esprit.finovate.controllers.InvestissementController;
import org.esprit.finovate.controllers.ProjectController;
import org.esprit.finovate.models.Investissement;
import org.esprit.finovate.models.Project;
import org.esprit.finovate.utils.Session;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class PendingInvestmentsController implements Initializable {

    @FXML private VBox pendingContainer;

    private Stage stage;
    private final InvestissementController investissementController = new InvestissementController();
    private final ProjectController projectController = new ProjectController();

    public void setStage(Stage stage) { this.stage = stage; }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadPending();
    }

    private void loadPending() {
        pendingContainer.getChildren().clear();
        if (Session.currentUser == null) return;

        try {
            List<Investissement> pending = investissementController.getPendingInvestmentsForOwner(Session.currentUser.getId());
            Map<Long, String> projectTitles = new HashMap<>();
            for (Project p : projectController.getAllProjects()) {
                projectTitles.put(p.getProject_id(), p.getTitle());
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            if (pending.isEmpty()) {
                Label empty = new Label("No pending investment requests.");
                empty.getStyleClass().add("project-meta");
                pendingContainer.getChildren().add(empty);
            } else {
                for (Investissement inv : pending) {
                    HBox card = createPendingCard(inv, projectTitles, sdf);
                    pendingContainer.getChildren().add(card);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Label err = new Label("Error loading requests.");
            err.getStyleClass().add("error-label");
            pendingContainer.getChildren().add(err);
        }
    }

    private HBox createPendingCard(Investissement inv, Map<Long, String> projectTitles, SimpleDateFormat sdf) {
        HBox card = new HBox(16);
        card.getStyleClass().add("investment-card");
        card.setPrefWidth(680);

        VBox info = new VBox(6);
        String title = projectTitles.getOrDefault(inv.getProject_id(), "Project #" + inv.getProject_id());
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("project-title");
        titleLabel.setWrapText(true);

        String date = inv.getInvestment_date() != null ? sdf.format(inv.getInvestment_date()) : "—";
        Label meta = new Label(String.format("%.2f TND  •  %s", inv.getAmount(), date));
        meta.getStyleClass().add("project-meta");

        info.getChildren().addAll(titleLabel, meta);
        HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

        Button btnAccept = new Button("Accept");
        btnAccept.getStyleClass().addAll("btn-primary", "btn-small");
        btnAccept.setOnAction(e -> handleAccept(inv));

        Button btnDecline = new Button("Decline");
        btnDecline.getStyleClass().addAll("btn-danger", "btn-small");
        btnDecline.setOnAction(e -> handleDecline(inv));

        card.getChildren().addAll(info, btnAccept, btnDecline);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return card;
    }

    private void handleAccept(Investissement inv) {
        try {
            investissementController.acceptInvestissement(inv.getInvestissement_id());
            new Alert(Alert.AlertType.INFORMATION, "Investment accepted. Amount added to project.").showAndWait();
            loadPending();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed: " + e.getMessage()).showAndWait();
        }
    }

    private void handleDecline(Investissement inv) {
        try {
            investissementController.declineInvestissement(inv.getInvestissement_id());
            new Alert(Alert.AlertType.INFORMATION, "Investment declined.").showAndWait();
            loadPending();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed: " + e.getMessage()).showAndWait();
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
    }
}
