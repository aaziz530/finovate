package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.esprit.finovate.api.ExchangeRateService;
import org.esprit.finovate.models.DailyRevenue;
import org.esprit.finovate.entities.Investissement;
import org.esprit.finovate.entities.Project;
import org.esprit.finovate.services.DailyRevenueService;
import org.esprit.finovate.utils.SceneUtils;
import org.esprit.finovate.utils.Session;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class InvestorStatsController implements Initializable {

    @FXML
    private Label lblTotalInvested;
    @FXML
    private Label lblTotalEarnings;
    @FXML
    private LineChart<String, Number> portfolioChart;

    private Stage stage;
    private Project currentProject;
    private Investissement currentInvestment;

    private final DailyRevenueService dailyRevenueService = new DailyRevenueService();
    private final ExchangeRateService exchangeRateService = new ExchangeRateService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Handled in setProjectAndInvestment
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setProjectAndInvestment(Project project, Investissement investment) {
        this.currentProject = project;
        this.currentInvestment = investment;
        if (Session.currentUser != null && project != null && investment != null) {
            loadStats();
        }
    }

    private void loadStats() {
        try {
            // 1. Update KPI with single investment data
            double investedAmount = currentInvestment.getAmount();
            lblTotalInvested.setText(exchangeRateService.formatTndAndEur(investedAmount) + "     ("
                    + currentInvestment.getRevenuePercentage() + "% des revenus)");

            // 2. Calculate and update Earnings for this specific project
            List<DailyRevenue> revenues = dailyRevenueService.getRevenuesByProject(currentProject.getProject_id());

            double totalEarnings = 0;
            for (DailyRevenue dr : revenues) {
                totalEarnings += dr.getAmount() * (currentInvestment.getRevenuePercentage() / 100.0);
            }
            lblTotalEarnings.setText(exchangeRateService.formatTndAndEur(totalEarnings));

            // 3. Build Chart Data (Revenue Evolution for this specific project)
            portfolioChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Vos Gains Quotidiens (" + currentInvestment.getRevenuePercentage() + "%)");

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM");
            for (DailyRevenue dr : revenues) {
                if (dr.getRevenue_date() != null) {
                    double myCut = dr.getAmount() * (currentInvestment.getRevenuePercentage() / 100.0);
                    series.getData().add(new XYChart.Data<>(sdf.format(dr.getRevenue_date()), myCut));
                }
            }

            portfolioChart.getData().add(series);

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur de chargement des statistiques: " + e.getMessage()).showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/my_investments.fxml"));
            Parent root = loader.load();
            MyInvestmentsController ctrl = loader.getController();
            ctrl.setStage(stage);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Finovate - Mes Investissements");
            SceneUtils.applyStageSize(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
