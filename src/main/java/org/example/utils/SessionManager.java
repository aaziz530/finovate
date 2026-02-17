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

    public static int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }

    public static User.Role getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == User.Role.ADMIN;
    }

    public static boolean isModerator() {
        return currentUser != null && currentUser.getRole() == User.Role.MODERATOR;
    }

    public static boolean isUser() {
        return currentUser != null && currentUser.getRole() == User.Role.USER;
    }

    public static boolean isBlocked() {
        return currentUser != null && currentUser.isBlocked();
    }
}
