package org.esprit.finovate.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.esprit.finovate.entities.Bill;
import org.esprit.finovate.services.BillService;
import org.esprit.finovate.services.IBillService;
import org.esprit.finovate.services.ITransactionService;
import org.esprit.finovate.services.TransactionService;
import org.esprit.finovate.utils.Session;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

public class BillController implements Initializable {

    @FXML
    private TextField referenceField;
    @FXML
    private TextField amountField;
    @FXML
    private Label balanceLabel;
    @FXML
    private Label errorLabel;
    @FXML
    private TableView<Bill> billTable;
    @FXML
    private TableColumn<Bill, String> colDate;
    @FXML
    private TableColumn<Bill, String> colReference;
    @FXML
    private TableColumn<Bill, Double> colAmount;

    private final IBillService billService;
    private final ITransactionService transactionService;
    private final ObservableList<Bill> billList = FXCollections.observableArrayList();

    public BillController() {
        this.billService = new BillService();
        this.transactionService = new TransactionService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        refreshData();
    }

    private void setupTable() {
        colDate.setCellValueFactory(cellData -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return new SimpleStringProperty(sdf.format(cellData.getValue().getDatePaiement()));
        });
        colReference.setCellValueFactory(new PropertyValueFactory<>("reference"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

        billTable.setItems(billList);
    }

    private void refreshData() {
        try {
            int userId = Session.currentUser.getId().intValue();
            float balance = transactionService.getUserBalance(userId);
            balanceLabel.setText(String.format("%.2f TND", balance));

            List<Bill> bills = billService.getBillsByUserId(userId);
            billList.setAll(bills);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePay() {
        errorLabel.setVisible(false);
        String reference = referenceField.getText().trim();
        String amountStr = amountField.getText().trim();

        if (reference.isEmpty() || amountStr.isEmpty()) {
            showError("Reference and amount are required");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            billService.payBill(Session.currentUser.getId().intValue(), reference, amount);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Payment Successful");
            alert.setContentText("Bill paid successfully!");
            alert.showAndWait();

            referenceField.clear();
            amountField.clear();
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
