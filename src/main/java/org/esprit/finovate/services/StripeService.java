package org.esprit.finovate.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling Stripe payments.
 * Uses Stripe Checkout for secure payment processing.
 */
public class StripeService {

    private static StripeService instance;
    private final String secretKey;
    private final String publishableKey;
    private boolean initialized = false;

    // Store pending payments: sessionId -> amount
    private final Map<String, Float> pendingPayments = new HashMap<>();

    private StripeService() {
        Dotenv dotenv = Dotenv.load();
        this.secretKey = dotenv.get("STRIPE_SECRET_KEY");
        this.publishableKey = dotenv.get("STRIPE_PUBLISHABLE_KEY");

        init();
    }

    public static synchronized StripeService getInstance() {
        if (instance == null) {
            instance = new StripeService();
        }
        return instance;
    }

    private void init() {
        if (secretKey == null || publishableKey == null) {
            System.err.println("[StripeService] Missing Stripe credentials in .env file");
            System.err.println("[StripeService] Required: STRIPE_SECRET_KEY, STRIPE_PUBLISHABLE_KEY");
            return;
        }
        Stripe.apiKey = secretKey;
        initialized = true;
        System.out.println("[StripeService] Stripe SDK initialized successfully");
    }

    public String getPublishableKey() {
        return publishableKey;
    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Creates a Stripe Checkout session for card top-up.
     *
     * @param amountTND  Amount entered in the app (displayed as TND). Stripe will charge the same numeric value in EUR.
     * @param userId     User ID for tracking
     * @return CheckoutSessionResult containing session URL and session ID
     * @throws StripeException if session creation fails
     */
    public CheckoutSessionResult createCheckoutSession(float amountTND, Long userId) throws StripeException {
        if (!initialized) {
            throw new IllegalStateException("Stripe not initialized. Check .env configuration.");
        }

        // Stripe uses the smallest currency unit. For EUR that's cents.
        // IMPORTANT: no FX conversion is applied by design; the numeric amount is charged in EUR.
        long amountInCents = Math.round(amountTND * 100);

        // Create checkout session
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("https://example.com/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("https://example.com/cancel")
                .putMetadata("user_id", String.valueOf(userId))
                .putMetadata("amount_tnd", String.valueOf(amountTND))
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Alimentation carte Finovate")
                                                                .setDescription("Recharge de votre carte Finovate")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);

        // Store pending payment
        pendingPayments.put(session.getId(), amountTND);

        System.out.println("[StripeService] Checkout session created: " + session.getId());
        System.out.println("[StripeService] Amount: " + amountTND + " TND, User ID: " + userId);

        return new CheckoutSessionResult(session.getId(), session.getUrl());
    }

    /**
     * Retrieves the payment amount for a session ID.
     *
     * @param sessionId The Stripe checkout session ID
     * @return The amount in TND, or null if not found
     */
    public Float getPendingAmount(String sessionId) {
        return pendingPayments.get(sessionId);
    }

    /**
     * Removes a pending payment after processing.
     *
     * @param sessionId The Stripe checkout session ID
     */
    public void removePendingPayment(String sessionId) {
        pendingPayments.remove(sessionId);
    }

    /**
     * Verifies a payment session and returns its status.
     *
     * @param sessionId The Stripe checkout session ID
     * @return PaymentVerificationResult containing status and amount
     */
    public PaymentVerificationResult verifyPayment(String sessionId) {
        if (!initialized) {
            return new PaymentVerificationResult(false, 0, "Stripe not initialized");
        }

        try {
            Session session = Session.retrieve(sessionId);
            String paymentStatus = session.getPaymentStatus();

            if ("paid".equals(paymentStatus)) {
                // Get amount from metadata or pending payments
                String amountStr = session.getMetadata().get("amount_tnd");
                float amount = amountStr != null ? Float.parseFloat(amountStr) : pendingPayments.getOrDefault(sessionId, 0f);

                System.out.println("[StripeService] Payment verified successfully: " + sessionId);
                System.out.println("[StripeService] Amount: " + amount + " TND");

                return new PaymentVerificationResult(true, amount, null);
            } else {
                return new PaymentVerificationResult(false, 0, "Payment not completed. Status: " + paymentStatus);
            }
        } catch (StripeException e) {
            System.err.println("[StripeService] Error verifying payment: " + e.getMessage());
            return new PaymentVerificationResult(false, 0, e.getMessage());
        }
    }

    /**
     * Result of a checkout session creation.
     */
    public record CheckoutSessionResult(String sessionId, String checkoutUrl) {
    }

    /**
     * Result of payment verification.
     */
    public record PaymentVerificationResult(boolean success, float amount, String errorMessage) {
    }
}
