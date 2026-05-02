package org.esprit.finovate.utils;

import org.esprit.finovate.entities.User;

/**
 * Legacy forum UI helpers used {@code SessionManager}; this project uses {@link Session}
 * elsewhere. Delegate so forum controllers stay in sync with the logged-in user.
 */
public final class SessionManager {

    private SessionManager() {}

    public static void login(User user) {
        Session.currentUser = user;
    }

    public static void logout() {
        Session.currentUser = null;
    }

    /** Same user as {@link Session#currentUser}. */
    public static User getCurrentUser() {
        return Session.currentUser;
    }

    public static boolean isLoggedIn() {
        return Session.currentUser != null;
    }

    public static long getCurrentUserId() {
        User u = Session.currentUser;
        return u != null && u.getId() != null ? u.getId() : -1L;
    }
}
