package org.esprit.finovate.utils;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class ValidationUtils {

    // Validation Email
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Valide un email
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Valide un username (3-50 caractères, alphanumériques et underscore)
     */
    public static boolean isValidUsername(String username) {
        return username != null &&
                username.matches("^[a-zA-Z0-9_]{3,50}$");
    }

    /**
     * Valide un mot de passe (minimum 6 caractères)
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Valide un nom de forum (3-100 caractères)
     */
    public static boolean isValidForumName(String name) {
        return name != null &&
                name.trim().length() >= 3 &&
                name.trim().length() <= 100;
    }

    /**
     * Valide une description (10-1000 caractères)
     */
    public static boolean isValidDescription(String description) {
        return description != null &&
                description.trim().length() >= 10 &&
                description.trim().length() <= 1000;
    }

    /**
     * Valide un titre de post (5-200 caractères)
     */
    public static boolean isValidPostTitle(String title) {
        return title != null &&
                title.trim().length() >= 5 &&
                title.trim().length() <= 200;
    }

    /**
     * Valide un contenu de post (10-5000 caractères)
     */
    public static boolean isValidPostContent(String content) {
        return content != null &&
                content.trim().length() >= 10 &&
                content.trim().length() <= 5000;
    }

    /**
     * Valide un commentaire (1-1000 caractères)
     */
    public static boolean isValidCommentContent(String content) {
        return content != null &&
                content.trim().length() >= 1 &&
                content.trim().length() <= 1000;
    }

    /**
     * Nettoie les espaces d'une chaîne
     */
    public static String sanitize(String input) {
        return input == null ? "" : input.trim();
    }

    public static String validateInvestmentAmount(String value) {
        return validateAmount(value, CONFIG.getMinAmount(), CONFIG.getMaxAmount(), "Amount");
    }

    /**
     * Validates investment amount with optional max (e.g. remaining to fund).
     */
    public static String validateInvestmentAmount(String value, Double maxAmount) {
        String err = validateInvestmentAmount(value);
        if (err != null) return err;
        if (maxAmount != null && maxAmount > 0) {
            double v = parseAmount(value.trim());
            if (v > maxAmount) return "Amount cannot exceed " + String.format("%.2f", maxAmount) + " TND (remaining to fund).";
        }
        return null;
    }

    public static double parseAmount(String value) {
        return Double.parseDouble(value.trim().replace(",", "."));
    }

    /**
     * Validates amount (double) with configurable min/max.
     */
    public static String validateAmount(String value, double min, double max, String fieldName) {
        String err = validateRequired(value, fieldName);
        if (err != null) return err;
        try {
            double v = parseAmount(value);
            if (v < min) return fieldName + " must be at least " + min + " TND.";
            if (v > max) return fieldName + " must not exceed " + max + " TND.";
            return null;
        } catch (NumberFormatException e) {
            return "Invalid " + fieldName + ". Use numbers (e.g. 100 or 100.50).";
        }
    }

    public static String validateRequired(String value, String fieldName) {
        if (value == null || (value = value.trim()).isEmpty()) {
            return fieldName + " is required.";
        }
        return null;
    }

    /** Configurable validation limits */
    public static final ValidationConfig CONFIG = new ValidationConfig();

    // ========== Investment module validation methods ==========

    public static String validateTitle(String title) {
        return validateLength(title, 1, 150, "Title");
    }

    public static String validateDescription(String desc) {
        return validateLength(desc, 1, 5000, "Description");
    }

    public static String validateGoalAmount(String value) {
        return validateAmount(value, CONFIG.getMinAmount(), CONFIG.getMaxAmount(), "Goal amount");
    }

    public static String validateDeadline(LocalDate date, String fieldName) {
        if (date == null) return fieldName + " is required.";
        if (date.isBefore(LocalDate.now())) {
            return fieldName + " must be today or in the future.";
        }
        return null;
    }

    public static String validateLength(String value, int minLen, int maxLen, String fieldName) {
        String err = validateRequired(value, fieldName);
        if (err != null) return err;
        value = value.trim();
        if (value.length() < minLen) return fieldName + " must be at least " + minLen + " characters.";
        if (maxLen > 0 && value.length() > maxLen) return fieldName + " must not exceed " + maxLen + " characters.";
        return null;
    }

    /**
     * Configurable limits for validation.
     */
    public static class ValidationConfig {
        private double minAmount = 0.01;
        private double maxAmount = 999_999_999.99;

        public double getMinAmount() { return minAmount; }
        public double getMaxAmount() { return maxAmount; }
    }
}