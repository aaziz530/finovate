package org.esprit.finovate.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.esprit.finovate.api.ExchangeRateService;
import org.esprit.finovate.models.DailyRevenue;
import org.esprit.finovate.entities.Investissement;
import org.esprit.finovate.entities.Project;
import org.esprit.finovate.services.DailyRevenueService;
import org.esprit.finovate.services.InvestissementService;
import org.esprit.finovate.utils.SceneUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ProjectRevenueController implements Initializable {

    @FXML
    private Label lblProjectTitle;
    @FXML
    private Label lblTotalRevenue;
    @FXML
    private Label lblTotalToInvestors;
    @FXML
    private Label lblNetEarnings;
    @FXML
    private VBox vboxMissingDays;

    @FXML
    private LineChart<String, Number> revenueChart;
    @FXML
    private PieChart distributionChart;

    private Stage stage;
    private Project currentProject;

    private final DailyRevenueService dailyRevenueService = new DailyRevenueService();
    private final InvestissementService investissementService = new InvestissementService();
    private final ExchangeRateService exchangeRateService = new ExchangeRateService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialization handled in setProject
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setProject(Project project) {
        this.currentProject = project;
        lblProjectTitle.setText("Revenus : " + project.getTitle());
        loadDashboardData();
    }

    private void loadDashboardData() {
        if (currentProject == null)
            return;

        try {
            Long projectId = currentProject.getProject_id();

            // 1. Calculate KPI totals and update labels
            List<DailyRevenue> allRevenues = dailyRevenueService.getRevenuesByProject(projectId);
            double totalRevenue = allRevenues.stream().mapToDouble(DailyRevenue::getAmount).sum();

            // Calculate total % given away
            List<Investissement> investments = investissementService.getInvestissementsByProjectId(projectId);
            double totalPercentageToInvestors = 0;
            for (Investissement inv : investments) {
                if ("CONFIRMED".equals(inv.getStatus())) {
                    totalPercentageToInvestors += inv.getRevenuePercentage();
                }
            }

            double totalToInvestors = totalRevenue * (totalPercentageToInvestors / 100.0);
            double netEarnings = totalRevenue - totalToInvestors;

            lblTotalRevenue.setText(exchangeRateService.formatTndAndEur(totalRevenue));
            lblTotalToInvestors.setText(exchangeRateService.formatTndAndEur(totalToInvestors));
            lblNetEarnings.setText(exchangeRateService.formatTndAndEur(netEarnings));

            // 2. Load Missing Days
            vboxMissingDays.getChildren().clear();
            List<LocalDate> missingDates = dailyRevenueService.getMissingRevenueDates(projectId);
            if (missingDates.isEmpty()) {
                Label lblOk = new Label("Tous les revenus sont à jour ! ✅");
                lblOk.setStyle("-fx-text-fill: #43A047; -fx-font-weight: bold;");
                vboxMissingDays.getChildren().add(lblOk);
            } else {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (LocalDate date : missingDates) {
                    HBox row = new HBox(15);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.getStyleClass().add("investment-card");

                    Label lblDate = new Label("Revenu du " + date.format(dtf));
                    lblDate.getStyleClass().add("field-label");
                    lblDate.setPrefWidth(200);

                    TextField txtAmount = new TextField();
                    txtAmount.setPromptText("Montant en TND");
                    txtAmount.setPrefWidth(150);

                    Button btnSubmit = new Button("Valider");
                    btnSubmit.getStyleClass().addAll("btn-primary", "btn-small");

                    btnSubmit.setOnAction(e -> {
                        try {
                            double amount = Double.parseDouble(txtAmount.getText());
                            if (amount < 0)
                                throw new NumberFormatException();
                            dailyRevenueService.addRevenue(projectId, java.sql.Date.valueOf(date), amount);
                            loadDashboardData(); // Refresh everything
                        } catch (NumberFormatException ex) {
                            new Alert(Alert.AlertType.WARNING, "Veuillez entrer un montant valide positif.")
                                    .showAndWait();
                        } catch (SQLException ex) {
                            new Alert(Alert.AlertType.ERROR, "Erreur base de données : " + ex.getMessage())
                                    .showAndWait();
                        }
                    });

                    row.getChildren().addAll(lblDate, txtAmount, btnSubmit);
                    vboxMissingDays.getChildren().add(row);
                }
            }

            // 3. Load Line Chart (Revenue Evolution)
            revenueChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Chiffre d'Affaires Quotidien");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
            for (DailyRevenue dr : allRevenues) {
                if (dr.getRevenue_date() != null) {
                    series.getData().add(new XYChart.Data<>(sdf.format(dr.getRevenue_date()), dr.getAmount()));
                }
            }
            revenueChart.getData().add(series);

            // 4. Load Pie Chart (Distribution)
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Votre Part (" + String.format("%.1f", 100 - totalPercentageToInvestors) + "%)",
                            netEarnings),
                    new PieChart.Data("Part Investisseurs (" + String.format("%.1f", totalPercentageToInvestors) + "%)",
                            totalToInvestors));
            distributionChart.setData(pieChartData);

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Error loading statistics: " + e.getMessage()).showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/my_projects.fxml"));
            Parent root = loader.load();
            MyProjectsController ctrl = loader.getController();
            ctrl.setStage(stage);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Finovate - My Projects");
            SceneUtils.applyStageSize(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
