package org.esprit.finovate.utils;

import org.esprit.finovate.entities.User;

public class Session {

    private static User currentUser = null;

    private Session() {}

    public static void login(User user) {
        currentUser = user;
        System.out.println("[DEV] Session démarrée → "
                + user.getFirstName() + " | role=" + user.getRole());
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isActive() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return isActive()
                && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }

    public static void clear() {
        currentUser = null;
    }
}