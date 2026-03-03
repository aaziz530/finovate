package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import org.esprit.finovate.dao.TicketDAO;
import org.esprit.finovate.model.Ticket;
import javafx.scene.chart.PieChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.esprit.finovate.utils.Session;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DashboardController implements Initializable {

    @FXML
    private Label lblOpenTickets;
    @FXML
    private Label lblInProgressTickets;
    @FXML
    private Label lblResolvedTickets;
    @FXML
    private Label lblHighPriorityTickets;
    @FXML
    private BarChart<String, Number> ticketsByMonthChart;
    @FXML
    private PieChart ticketsByCategoryChart;
    @FXML
    private BarChart<String, Number> avgResolutionTimeChart;

    private final TicketDAO ticketDAO = new TicketDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!Session.isAdmin()) { // ← ajout
            return; // ← ajout : bloque si pas admin
        }
        loadStatsAndData(); // ← ton code original intact
    }

    // ── Ton code original — RIEN CHANGE ──────────────────────
    private void loadStatsAndData() {
        List<Ticket> all = ticketDAO.findAll();

        long open = all.stream()
                .filter(t -> "NOUVEAU".equalsIgnoreCase(t.getStatut())).count();
        long inProgress = all.stream()
                .filter(t -> "EN_COURS".equalsIgnoreCase(t.getStatut())).count();
        long resolved = all.stream()
                .filter(t -> "RESOLU".equalsIgnoreCase(t.getStatut())).count();
        long highPriority = all.stream()
                .filter(t -> "HAUTE".equalsIgnoreCase(t.getPriorite())
                        || "HIGH".equalsIgnoreCase(t.getPriorite()))
                .count();

        lblOpenTickets.setText(String.valueOf(open));
        lblInProgressTickets.setText(String.valueOf(inProgress));
        lblResolvedTickets.setText(String.valueOf(resolved));
        lblHighPriorityTickets.setText(String.valueOf(highPriority));

        ticketsByMonthChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tickets");
        series.getData().add(new XYChart.Data<>("Tous", all.size()));
        ticketsByMonthChart.getData().add(series);

        // --- Nouveauté: PieChart (Nombre de tickets par catégorie) ---
        Map<String, Long> countByCategory = all.stream()
                .collect(Collectors.groupingBy(t -> t.getType() != null ? t.getType() : "Inconnu",
                        Collectors.counting()));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        countByCategory.forEach((category, count) -> {
            pieChartData.add(new PieChart.Data(category + " (" + count + ")", count));
        });
        ticketsByCategoryChart.setData(pieChartData);

        // --- Nouveauté: BarChart (Temps Moyen de Résolution) ---
        XYChart.Series<String, Number> resolutionSeries = new XYChart.Series<>();
        resolutionSeries.setName("Heures Moyennes");

        Map<String, Double> avgTimeByCategory = all.stream()
                .filter(t -> "RESOLU".equalsIgnoreCase(t.getStatut()) && t.getDateResolution() != null
                        && t.getDateCreation() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getType() != null ? t.getType() : "Inconnu",
                        Collectors.averagingDouble(t -> {
                            long diffInMillies = Math
                                    .abs(t.getDateResolution().getTime() - t.getDateCreation().getTime());
                            return (double) diffInMillies / (1000 * 60 * 60); // Convertir en heures
                        })));

        avgTimeByCategory.forEach((category, avgHours) -> {
            resolutionSeries.getData().add(new XYChart.Data<>(category, avgHours));
        });

        avgResolutionTimeChart.getData().clear();
        avgResolutionTimeChart.getData().add(resolutionSeries);
    }
}