package org.esprit.finovate.controllers;

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

import java.net.URL;
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

    public TransferController() {
        this.transactionService = new TransactionService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        refreshData();
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
            if (t.getSenderId() == Session.currentUser.getId().intValue()) {
                return new SimpleStringProperty("To: " + t.getReceiverName());
            } else {
                return new SimpleStringProperty("From: " + t.getSenderName());
            }
        });

        transactionTable.setItems(transactionList);
    }

    private void refreshData() {
        try {
            int userId = Session.currentUser.getId().intValue();
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
            transactionService.transferMoney(Session.currentUser.getId().intValue(), card, cin, amount,
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
    }
}
