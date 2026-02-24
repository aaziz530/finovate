package org.example.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.entities.Transaction;
import org.example.services.ITransactionService;
import org.example.services.TransactionService;
import org.example.utils.SessionManager;

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
            Long currentUserId = SessionManager.getCurrentUserId();
            if (currentUserId != null && t.getSenderId() == currentUserId.intValue()) {
                return new SimpleStringProperty("To: " + t.getReceiverName());
            } else {
                return new SimpleStringProperty("From: " + t.getSenderName());
            }
        });

        transactionTable.setItems(transactionList);
    }

    private void refreshData() {
        try {
            Long userId = SessionManager.getCurrentUserId();
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

        // Validation des champs
        if (card.isEmpty() || cin.isEmpty() || amountStr.isEmpty()) {
            showError("All fields except description are required");
            return;
        }

        try {
            // Valider que le numéro de carte contient seulement des chiffres
            if (!card.matches("\\d+")) {
                showError("Card number must contain only digits");
                return;
            }
            
            // Remplacer virgule par point pour le montant
            amountStr = amountStr.replace(',', '.');
            
            // Parser le montant
            float amount = Float.parseFloat(amountStr);
            
            // Vérifier que le montant est positif
            if (amount <= 0) {
                showError("Amount must be greater than 0");
                return;
            }
            
            // Effectuer le transfert avec String au lieu de Long
            transactionService.transferMoney(SessionManager.getCurrentUserId(), card, cin, amount, description);

            // Succès
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Transfer successful!");
            alert.showAndWait();

            // Nettoyer les champs
            recipientCardField.clear();
            recipientCinField.clear();
            amountField.clear();
            descriptionField.clear();
            
            // Rafraîchir les données
            refreshData();

        } catch (NumberFormatException e) {
            showError("Invalid amount format. Please enter a valid number (e.g., 100 or 100.50)");
            e.printStackTrace();
        } catch (SQLException e) {
            showError(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
