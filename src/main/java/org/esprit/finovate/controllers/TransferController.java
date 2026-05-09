package org.esprit.finovate.controllers;

import javafx.concurrent.Task;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.esprit.finovate.entities.Transaction;
import org.esprit.finovate.services.ITransactionService;
import org.esprit.finovate.services.TransactionService;
import org.esprit.finovate.utils.Session;
import org.esprit.finovate.utils.MyDataBase;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

public class TransferController implements Initializable {

    @FXML
    private TextField recipientCardField;
    @FXML
    private TextField recipientCinField;
    @FXML
    private TextField amountField;
    @FXML
    private TextField descriptionField;
    @FXML
    private Label balanceLabel;
    @FXML
    private Label errorLabel;
    @FXML
    private TableView<Transaction> transactionTable;
    @FXML
    private TableColumn<Transaction, String> colDate;
    @FXML
    private TableColumn<Transaction, String> colType;
    @FXML
    private TableColumn<Transaction, String> colRecipient;
    @FXML
    private TableColumn<Transaction, Float> colAmount;
    @FXML
    private TableColumn<Transaction, String> colDescription;

    private final ITransactionService transactionService;
    private final ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private final Connection connection = MyDataBase.getInstance().getConnection();

    public TransferController() {
        this.transactionService = new TransactionService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        refreshData();
        recipientCardField.setEditable(false);
        recipientCardField.setDisable(true);
        setupCinAutoFill();
    }

    private void setupCinAutoFill() {
        recipientCinField.textProperty().addListener((obs, oldVal, newVal) -> {
            String cin = newVal != null ? newVal.trim() : "";

            if (cin.length() != 8 || !cin.matches("\\d{8}")) {
                recipientCardField.clear();
                return;
            }

            Task<String> task = new Task<>() {
                @Override
                protected String call() throws Exception {
                    return findCardNumberByCin(cin);
                }
            };

            task.setOnSucceeded(e -> {
                String card = task.getValue();
                if (card != null && !card.isBlank()) {
                    recipientCardField.setText(card);
                } else {
                    recipientCardField.clear();
                }
            });

            task.setOnFailed(e -> recipientCardField.clear());

            Thread t = new Thread(task);
            t.setDaemon(true);
            t.start();
        });
    }

    private String findCardNumberByCin(String cin) throws SQLException {
        String sql = "SELECT numero_carte FROM user WHERE cin = ? LIMIT 1";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, cin);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return String.valueOf(rs.getLong("numero_carte"));
                }
            }
        }
        return null;
    }

    private void setupTable() {
        colDate.setCellValueFactory(cellData -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return new SimpleStringProperty(sdf.format(cellData.getValue().getDate()));
        });
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        colRecipient.setCellValueFactory(cellData -> {
            Transaction t = cellData.getValue();
            if (t.getSenderId().equals(Session.currentUser.getId())) {
                return new SimpleStringProperty("To: " + t.getReceiverName());
            } else {
                return new SimpleStringProperty("From: " + t.getSenderName());
            }
        });

        transactionTable.setItems(transactionList);
    }

    private void refreshData() {
        try {
            Long userId = Session.currentUser.getId();
            float balance = transactionService.getUserBalance(userId);
            balanceLabel.setText(String.format("%.2f TND", balance));

            List<Transaction> transactions = transactionService.getTransactionsByUserId(userId);
            transactionList.setAll(transactions);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSend() {
        errorLabel.setVisible(false);
        String card = recipientCardField.getText().trim();
        String cin = recipientCinField.getText().trim();
        String amountStr = amountField.getText().trim();
        String description = descriptionField.getText().trim();

        if (card.isEmpty() || cin.isEmpty() || amountStr.isEmpty()) {
            showError("All fields except description are required");
            return;
        }

        try {
            float amount = Float.parseFloat(amountStr);
            Long numeroCarte = Long.parseLong(card);
            transactionService.transferMoney(Session.currentUser.getId(), numeroCarte, cin, amount,
                    description);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setContentText("Transfer successful!");
            alert.showAndWait();

            recipientCardField.clear();
            recipientCinField.clear();
            amountField.clear();
            descriptionField.clear();
            refreshData();

        } catch (NumberFormatException e) {
            showError("Invalid amount format");
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
    }
}
