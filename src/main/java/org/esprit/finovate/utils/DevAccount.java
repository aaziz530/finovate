package org.esprit.finovate.utils;

import org.esprit.finovate.entities.User;

import java.util.Date;

public class DevAccount {
    public static final String EMAIL = "dev@finovate.tn";
    public static final String PASSWORD = "dev123";

    private DevAccount() {
    }

    public static User createUser() {
        User u = new User(EMAIL, PASSWORD, "Dev", "User", (Date) null, "00000000");
        u.setId(-1L);
        u.setRole("ADMIN");
        u.setNumeroCarte(5348036137095877L);
        return u;
    }
}
