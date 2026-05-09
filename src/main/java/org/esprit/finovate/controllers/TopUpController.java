package org.esprit.finovate.controllers;

import com.stripe.exception.StripeException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.esprit.finovate.services.StripeService;
import org.esprit.finovate.services.TransactionService;
import org.esprit.finovate.services.UserService;
import org.esprit.finovate.utils.Session;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class TopUpController implements Initializable {

    @FXML
    private Label currentBalanceLabel;

    @FXML
    private TextField amountField;

    @FXML
    private Button payButton;

    @FXML
    private Label errorLabel;

    @FXML
    private VBox statusBox;

    @FXML
    private Label statusLabel;

    @FXML
    private ProgressBar progressBar;

    private final StripeService stripeService = StripeService.getInstance();
    private final UserService userService = new UserService();
    private final TransactionService transactionService = new TransactionService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadCurrentBalance();
        
        // Add listener to amount field for numeric input only
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                amountField.setText(oldValue);
            }
        });
    }

    private void loadCurrentBalance() {
        if (Session.currentUser != null) {
            float balance = Session.currentUser.getSolde();
            currentBalanceLabel.setText(String.format("%.3f TND", balance));
        }
    }

    @FXML
    private void setAmount10() {
        amountField.setText("10");
    }

    @FXML
    private void setAmount50() {
        amountField.setText("50");
    }

    @FXML
    private void setAmount100() {
        amountField.setText("100");
    }

    @FXML
    private void setAmount500() {
        amountField.setText("500");
    }

    @FXML
    private void handlePay() {
        hideError();

        // Validate amount
        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            showError("Veuillez entrer un montant");
            return;
        }

        float amount;
        try {
            amount = Float.parseFloat(amountText);
            if (amount <= 0) {
                showError("Le montant doit être supérieur à 0");
                return;
            }
            if (amount > 10000) {
                showError("Le montant maximum est de 10,000 TND");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Montant invalide");
            return;
        }

        // Check if Stripe is initialized
        if (!stripeService.isInitialized()) {
            showError("Stripe n'est pas configuré. Vérifiez le fichier .env");
            return;
        }

        // Disable button and show progress
        payButton.setDisable(true);
        showStatus("Création de la session de paiement...", true);

        // Create checkout session in background
        Task<StripeService.CheckoutSessionResult> createSessionTask = new Task<>() {
            @Override
            protected StripeService.CheckoutSessionResult call() throws StripeException {
                return stripeService.createCheckoutSession(amount, Session.currentUser.getId());
            }
        };

        createSessionTask.setOnSucceeded(event -> {
            StripeService.CheckoutSessionResult result = createSessionTask.getValue();
            Platform.runLater(() -> {
                updateStatus("Ouverture de la page de paiement...");
                openPaymentInWebView(result.checkoutUrl(), result.sessionId(), amount);
            });
        });

        createSessionTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                hideStatus();
                payButton.setDisable(false);
                showError("Erreur lors de la création de la session: " + createSessionTask.getException().getMessage());
            });
        });

        Thread thread = new Thread(createSessionTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void openPaymentInWebView(String checkoutUrl, String sessionId, float amount) {
        try {
            // Create a new stage for WebView
            Stage paymentStage = new Stage();
            paymentStage.setTitle("Paiement Stripe - Finovate");

            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();
            
            // Enable JavaScript
            webView.getEngine().setJavaScriptEnabled(true);

            // Track URL changes to detect success/cancel
            webEngine.locationProperty().addListener((observable, oldUrl, newUrl) -> {
                System.out.println("[TopUp] WebView navigating to: " + newUrl);
                
                if (newUrl.contains("/success")) {
                    // Payment successful
                    Platform.runLater(() -> {
                        paymentStage.close();
                        handlePaymentSuccess(sessionId, amount);
                    });
                } else if (newUrl.contains("/cancel")) {
                    // Payment cancelled
                    Platform.runLater(() -> {
                        paymentStage.close();
                        handlePaymentCancel();
                    });
                }
            });

            // Load the checkout URL
            webEngine.load(checkoutUrl);

            // Create scene and show
            javafx.scene.Scene scene = new javafx.scene.Scene(webView, 800, 600);
            paymentStage.setScene(scene);
            paymentStage.show();

            // Handle window close
            paymentStage.setOnCloseRequest(event -> {
                hideStatus();
                payButton.setDisable(false);
            });

        } catch (Exception e) {
            hideStatus();
            payButton.setDisable(false);
            showError("Erreur lors de l'ouverture de la page de paiement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handlePaymentSuccess(String sessionId, float amount) {
        showStatus("Vérification du paiement...", true);

        Task<Boolean> verifyTask = new Task<>() {
            @Override
            protected Boolean call() {
                StripeService.PaymentVerificationResult result = stripeService.verifyPayment(sessionId);
                if (result.success()) {
                    // Update user balance
                    try {
                        float newBalance = Session.currentUser.getSolde() + result.amount();
                        Session.currentUser.setSolde(newBalance);
                        userService.updateUser(Session.currentUser);
                        
                        // Log transaction
                        transactionService.logTopUp(Session.currentUser.getId(), result.amount());
                        
                        return true;
                    } catch (SQLException e) {
                        System.err.println("[TopUp] Error updating balance: " + e.getMessage());
                        return false;
                    }
                }
                return false;
            }
        };

        verifyTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                if (verifyTask.getValue()) {
                    hideStatus();
                    payButton.setDisable(false);
                    loadCurrentBalance();
                    showSuccess("Paiement réussi! Votre solde a été mis à jour.");
                    amountField.clear();
                } else {
                    hideStatus();
                    payButton.setDisable(false);
                    showError("Le paiement n'a pas pu être vérifié");
                }
            });
        });

        verifyTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                hideStatus();
                payButton.setDisable(false);
                showError("Erreur lors de la vérification: " + verifyTask.getException().getMessage());
            });
        });

        Thread thread = new Thread(verifyTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void handlePaymentCancel() {
        hideStatus();
        payButton.setDisable(false);
        showError("Paiement annulé");
    }

    private void showStatus(String message, boolean showProgress) {
        statusBox.setVisible(true);
        statusLabel.setText(message);
        progressBar.setVisible(showProgress);
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private void hideStatus() {
        statusBox.setVisible(false);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setStyle("-fx-text-fill: #dc3545;");
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setStyle("-fx-text-fill: #237f4e;");
    }

    private void hideError() {
        errorLabel.setVisible(false);
    }
}
