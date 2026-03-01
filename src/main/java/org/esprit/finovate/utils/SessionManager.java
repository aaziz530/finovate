package org.esprit.finovate.utils;

import org.esprit.finovate.entities.User;

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

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static long getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }
}
