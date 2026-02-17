package org.example.utils;

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
}