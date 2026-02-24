package org.example.utils;

import org.example.entities.User;

public class SessionManager {
    private static User currentUser = null;

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static Long getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }

    public static String getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }

    public static boolean isModerator() {
        return currentUser != null && "MODERATOR".equals(currentUser.getRole());
    }

    public static boolean isUser() {
        return currentUser != null && "USER".equals(currentUser.getRole());
    }

    public static boolean isBlocked() {
        return currentUser != null && currentUser.isBlocked();
    }
}
