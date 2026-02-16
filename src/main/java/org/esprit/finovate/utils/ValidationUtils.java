package org.esprit.finovate.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Input validation utilities (contrôle de saisie dynamique).
 * Supports configurable rules and reusable validators.
 */
public final class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[\\p{L}\\p{M}\\s'-]{2,50}$", Pattern.UNICODE_CHARACTER_CLASS);

    /** Configurable validation limits (dynamique) */
    public static final ValidationConfig CONFIG = new ValidationConfig();

    private ValidationUtils() {}

    // ========== Dynamic / Configurable Validators ==========

    /**
     * Validates required (non-null, non-empty after trim).
     */
    public static String validateRequired(String value, String fieldName) {
        if (value == null || (value = value.trim()).isEmpty()) {
            return fieldName + " is required.";
        }
        return null;
    }

    /**
     * Validates string length.
     */
    public static String validateLength(String value, int minLen, int maxLen, String fieldName) {
        String err = validateRequired(value, fieldName);
        if (err != null) return err;
        value = value.trim();
        if (value.length() < minLen) return fieldName + " must be at least " + minLen + " characters.";
        if (maxLen > 0 && value.length() > maxLen) return fieldName + " must not exceed " + maxLen + " characters.";
        return null;
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

    /**
     * Validates person name (firstname, lastname) - letters, spaces, apostrophe, hyphen.
     */
    public static String validateName(String value, String fieldName) {
        String err = validateRequired(value, fieldName);
        if (err != null) return err;
        value = value.trim();
        if (value.length() < 2) return fieldName + " must be at least 2 characters.";
        if (value.length() > 50) return fieldName + " must not exceed 50 characters.";
        if (!NAME_PATTERN.matcher(value).matches()) {
            return fieldName + " must contain only letters, spaces, apostrophe or hyphen.";
        }
        return null;
    }

    /**
     * Validates deadline is today or in the future.
     */
    public static String validateDeadline(LocalDate date, String fieldName) {
        if (date == null) return fieldName + " is required.";
        if (date.isBefore(LocalDate.now())) {
            return fieldName + " must be today or in the future.";
        }
        return null;
    }

    /**
     * Validates deadline from java.util.Date.
     */
    public static String validateDeadline(Date date, String fieldName) {
        if (date == null) return fieldName + " is required.";
        LocalDate ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return validateDeadline(ld, fieldName);
    }

    /**
     * Validates birthdate - user must be at least minAge years old.
     */
    public static String validateBirthdate(Date birthdate, int minAge, String fieldName) {
        if (birthdate == null) return fieldName + " is required.";
        LocalDate ld = birthdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (ld.isAfter(LocalDate.now().minusYears(minAge))) {
            return "You must be at least " + minAge + " years old.";
        }
        return null;
    }

    /**
     * Runs multiple validators; returns first error or null.
     */
    @SafeVarargs
    public static String validateAll(Supplier<String>... validators) {
        for (Supplier<String> v : validators) {
            String err = v.get();
            if (err != null) return err;
        }
        return null;
    }

    // ========== Legacy / Convenience Methods (backward compatible) ==========

    public static String validateEmail(String email) {
        String err = validateRequired(email, "Email");
        if (err != null) return err;
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) return "Invalid email format.";
        return null;
    }

    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) return "Password is required.";
        if (password.length() < CONFIG.getPasswordMinLength()) {
            return "Password must be at least " + CONFIG.getPasswordMinLength() + " characters.";
        }
        return null;
    }

    public static String validateTitle(String title) {
        return validateLength(title, 1, CONFIG.getTitleMaxLength(), "Title");
    }

    public static String validateDescription(String desc) {
        return validateLength(desc, 1, CONFIG.getDescriptionMaxLength(), "Description");
    }

    public static String validateGoalAmount(String value) {
        return validateAmount(value, CONFIG.getMinAmount(), CONFIG.getMaxAmount(), "Goal amount");
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
     * Configurable limits for validation (contrôle de saisie dynamique).
     */
    public static class ValidationConfig {
        private int titleMaxLength = 150;
        private int descriptionMaxLength = 5000;
        private double minAmount = 0.01;
        private double maxAmount = 999_999_999.99;
        private int passwordMinLength = 3;

        public int getTitleMaxLength() { return titleMaxLength; }
        public ValidationConfig setTitleMaxLength(int v) { titleMaxLength = v; return this; }

        public int getDescriptionMaxLength() { return descriptionMaxLength; }
        public ValidationConfig setDescriptionMaxLength(int v) { descriptionMaxLength = v; return this; }

        public double getMinAmount() { return minAmount; }
        public ValidationConfig setMinAmount(double v) { minAmount = v; return this; }

        public double getMaxAmount() { return maxAmount; }
        public ValidationConfig setMaxAmount(double v) { maxAmount = v; return this; }

        public int getPasswordMinLength() { return passwordMinLength; }
        public ValidationConfig setPasswordMinLength(int v) { passwordMinLength = v; return this; }
    }
}
