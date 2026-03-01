package org.esprit.finovate.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminAgentsController implements Initializable {

    public static class AgentRow {
        private final SimpleStringProperty name = new SimpleStringProperty();
        private final SimpleIntegerProperty ticketsAssignes = new SimpleIntegerProperty();
        private final SimpleIntegerProperty ticketsOuverts = new SimpleIntegerProperty();
        private final SimpleIntegerProperty ticketsResolus = new SimpleIntegerProperty();
        private final SimpleStringProperty performance = new SimpleStringProperty();

        public AgentRow(String name, int assignes, int ouverts, int resolus, String performance) {
            this.name.set(name);
            this.ticketsAssignes.set(assignes);
            this.ticketsOuverts.set(ouverts);
            this.ticketsResolus.set(resolus);
            this.performance.set(performance);
        }

        public String getName() { return name.get(); }
        public int getTicketsAssignes() { return ticketsAssignes.get(); }
        public int getTicketsOuverts() { return ticketsOuverts.get(); }
        public int getTicketsResolus() { return ticketsResolus.get(); }
        public String getPerformance() { return performance.get(); }
    }

    @FXML
    private TableView<AgentRow> agentsTable;
    @FXML
    private TableColumn<AgentRow, String> colAgentNom;
    @FXML
    private TableColumn<AgentRow, Number> colTicketsAssignes;
    @FXML
    private TableColumn<AgentRow, Number> colTicketsOuverts;
    @FXML
    private TableColumn<AgentRow, Number> colTicketsResolus;
    @FXML
    private TableColumn<AgentRow, String> colPerformance;

    private final ObservableList<AgentRow> rows = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colAgentNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colTicketsAssignes.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getTicketsAssignes()));
        colTicketsOuverts.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getTicketsOuverts()));
        colTicketsResolus.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getTicketsResolus()));
        colPerformance.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPerformance()));

        // Données placeholder (on pourrait un jour les calculer à partir des tickets)
        rows.addAll(
                new AgentRow("Agent 1", 12, 3, 9, "Très bon"),
                new AgentRow("Agent 2", 8, 5, 3, "Moyen"),
                new AgentRow("Agent 3", 5, 1, 4, "Excellent")
        );
        agentsTable.setItems(rows);
    }
}

