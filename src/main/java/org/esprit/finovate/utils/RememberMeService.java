package org.esprit.finovate.utils;

import java.util.prefs.Preferences;

public class RememberMeService {
    private static final String PREF_NODE_NAME = "org.esprit.finovate.auth";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember_me";

    private static final Preferences prefs = Preferences.userRoot().node(PREF_NODE_NAME);

    public static void saveCredentials(String username, String password) {
        prefs.put(KEY_USERNAME, username);
        prefs.put(KEY_PASSWORD, password);
        prefs.putBoolean(KEY_REMEMBER, true);
    }

    public static void clearCredentials() {
        prefs.remove(KEY_USERNAME);
        prefs.remove(KEY_PASSWORD);
        prefs.putBoolean(KEY_REMEMBER, false);
    }

    public static String getSavedUsername() {
        return prefs.get(KEY_USERNAME, "");
    }

    public static String getSavedPassword() {
        return prefs.get(KEY_PASSWORD, "");
    }

    public static boolean isRememberMeEnabled() {
        return prefs.getBoolean(KEY_REMEMBER, false);
    }
}
