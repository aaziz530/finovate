package org.esprit.finovate.utils;

import java.util.regex.Pattern;

/**
 * Input validation utilities (contrôle de saisie)
 */
public final class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int TITLE_MAX_LENGTH = 150;
    private static final int DESCRIPTION_MAX_LENGTH = 5000;
    private static final double MIN_AMOUNT = 0.01;
    private static final double MAX_AMOUNT = 999_999_999.99;

    private ValidationUtils() {}

    public static String validateEmail(String email) {
        if (email == null || (email = email.trim()).isEmpty()) {
            return "Email is required.";
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Invalid email format.";
        }
        return null;
    }

    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required.";
        }
        if (password.length() < 3) {
            return "Password must be at least 3 characters.";
        }
        return null;
    }

    public static String validateTitle(String title) {
        if (title == null || (title = title.trim()).isEmpty()) {
            return "Title is required.";
        }
        if (title.length() > TITLE_MAX_LENGTH) {
            return "Title must not exceed " + TITLE_MAX_LENGTH + " characters.";
        }
        return null;
    }

    public static String validateDescription(String desc) {
        if (desc == null || (desc = desc.trim()).isEmpty()) {
            return "Description is required.";
        }
        if (desc.length() > DESCRIPTION_MAX_LENGTH) {
            return "Description must not exceed " + DESCRIPTION_MAX_LENGTH + " characters.";
        }
        return null;
    }

    public static String validateGoalAmount(String value) {
        if (value == null || (value = value.trim()).isEmpty()) {
            return "Goal amount is required.";
        }
        try {
            double v = Double.parseDouble(value.replace(",", "."));
            if (v < MIN_AMOUNT) return "Amount must be at least " + MIN_AMOUNT + " TND.";
            if (v > MAX_AMOUNT) return "Amount too large.";
            return null;
        } catch (NumberFormatException e) {
            return "Invalid amount. Use numbers only (e.g. 10000 or 10000.50).";
        }
    }

    public static String validateInvestmentAmount(String value) {
        if (value == null || (value = value.trim()).isEmpty()) {
            return "Amount is required.";
        }
        try {
            double v = Double.parseDouble(value.replace(",", "."));
            if (v < MIN_AMOUNT) return "Amount must be at least " + MIN_AMOUNT + " TND.";
            if (v > MAX_AMOUNT) return "Amount too large.";
            return null;
        } catch (NumberFormatException e) {
            return "Invalid amount. Use numbers only (e.g. 100 or 100.50).";
        }
    }

    public static double parseAmount(String value) {
        return Double.parseDouble(value.trim().replace(",", "."));
    }
}
