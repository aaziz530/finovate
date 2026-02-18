package org.esprit.finovate.utils;

/** Contract for logged-in user. Session.currentUser must implement this. */
public interface ILoggedInUser {
    Long getId();
    /** @return "ADMIN" or "USER" */
    String getRole();
}
