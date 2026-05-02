package org.esprit.finovate.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import io.github.cdimascio.dotenv.Dotenv;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for sending SMS notifications via Twilio.
 * Used to notify beneficiaries when they receive a money transfer.
 * Credentials are loaded from .env file for security.
 */
public class TwilioSmsService {

    // Twilio credentials loaded from .env
    private final String accountSid;
    private final String authToken;
    private final String twilioPhoneNumber;

    // Country code for Tunisia
    private static final String COUNTRY_CODE = "+216";

    private static TwilioSmsService instance;
    private boolean initialized = false;

    private TwilioSmsService() {
        // Load credentials from .env file
        Dotenv dotenv = Dotenv.load();
        this.accountSid = dotenv.get("TWILIO_ACCOUNT_SID");
        this.authToken = dotenv.get("TWILIO_AUTH_TOKEN");
        this.twilioPhoneNumber = dotenv.get("TWILIO_PHONE_NUMBER");

        init();
    }

    public static synchronized TwilioSmsService getInstance() {
        if (instance == null) {
            instance = new TwilioSmsService();
        }
        return instance;
    }

    private void init() {
        if (!initialized) {
            if (accountSid == null || authToken == null || twilioPhoneNumber == null) {
                System.err.println("[TwilioSmsService] Missing Twilio credentials in .env file");
                System.err.println("[TwilioSmsService] Required: TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, TWILIO_PHONE_NUMBER");
                return;
            }
            try {
                Twilio.init(accountSid, authToken);
                initialized = true;
                System.out.println("[TwilioSmsService] Twilio SDK initialized successfully");
            } catch (Exception e) {
                System.err.println("[TwilioSmsService] Failed to initialize Twilio: " + e.getMessage());
            }
        }
    }

    /**
     * Sends an SMS notification to the beneficiary about a received transfer.
     *
     * @param beneficiaryName The full name of the beneficiary
     * @param phoneNumber     The 8-digit local phone number (will be converted to +216XXXXXXXX)
     * @param amount          The amount received in TND
     * @param senderName      The full name of the sender
     * @param date            The date/time of the transaction
     * @return true if SMS was sent successfully, false otherwise
     */
    public boolean sendTransferNotification(String beneficiaryName, int phoneNumber, float amount, String senderName, LocalDateTime date) {
        if (!initialized) {
            System.err.println("[TwilioSmsService] Twilio not initialized, cannot send SMS");
            return false;
        }

        // Convert 8-digit local number to E.164 format (+216XXXXXXXX)
        String e164PhoneNumber = formatToE164(phoneNumber);

        // Format amount with 3 decimal places
        String formattedAmount = String.format("%.3f TND", amount);

        // Format date as dd/MM/yyyy HH:mm
        String formattedDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        // Build the message
        String messageBody = String.format(
                "Bonjour %s, vous avez reçu %s de %s le %s.",
                beneficiaryName,
                formattedAmount,
                senderName,
                formattedDate
        );

        try {
            Message message = Message.creator(
                    new PhoneNumber(e164PhoneNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    messageBody
            ).create();

            String sid = message.getSid();
            String status = message.getStatus().toString();
            System.out.println("[TwilioSmsService] SMS sent successfully! SID: " + sid + ", Status: " + status);
            System.out.println("[TwilioSmsService] To: " + e164PhoneNumber + ", Message: " + messageBody);
            return true;

        } catch (Exception e) {
            System.err.println("[TwilioSmsService] Failed to send SMS: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Converts an 8-digit local phone number to E.164 format.
     * Example: 12345678 -> +21612345678
     *
     * @param localNumber The 8-digit phone number
     * @return The E.164 formatted phone number
     */
    private String formatToE164(int localNumber) {
        return COUNTRY_CODE + String.format("%08d", localNumber);
    }

    /**
     * Sends a test SMS to verify Twilio configuration.
     * Note: In trial mode, the recipient number must be verified in Twilio.
     *
     * @param phoneNumber The 8-digit local phone number to send test to
     * @return true if test SMS was sent successfully
     */
    public boolean sendTestSms(int phoneNumber) {
        return sendTransferNotification(
                "Test User",
                phoneNumber,
                0.001f,
                "System Test",
                LocalDateTime.now()
        );
    }
}
