package org.esprit.finovate.utils;

import org.esprit.finovate.entities.User;

/** Holds current logged-in user. Set currentUser after login. */
public class Session {
    public static User currentUser;

    private Session() {
    }
}
