package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import org.esprit.finovate.dao.TicketDAO;
import org.esprit.finovate.model.Ticket;
import org.esprit.finovate.utils.Session; // ← ajout

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label lblOpenTickets;
    @FXML private Label lblInProgressTickets;
    @FXML private Label lblResolvedTickets;
    @FXML private Label lblHighPriorityTickets;
    @FXML private BarChart<String, Number> ticketsByMonthChart;

    private final TicketDAO ticketDAO = new TicketDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!Session.isAdmin()) {          // ← ajout
            return;                        // ← ajout : bloque si pas admin
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
                        || "HIGH".equalsIgnoreCase(t.getPriorite())).count();

        lblOpenTickets.setText(String.valueOf(open));
        lblInProgressTickets.setText(String.valueOf(inProgress));
        lblResolvedTickets.setText(String.valueOf(resolved));
        lblHighPriorityTickets.setText(String.valueOf(highPriority));

        ticketsByMonthChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tickets");
        series.getData().add(new XYChart.Data<>("Tous", all.size()));
        ticketsByMonthChart.getData().add(series);
    }
}