package org.esprit.finovate.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Loads API keys from api_config.properties */
public final class ApiConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream is = ApiConfig.class.getResourceAsStream("/api_config.properties")) {
            if (is != null) props.load(is);
        } catch (IOException ignored) {}
    }

    public static String getUnsplashAccessKey() {
        return props.getProperty("unsplash.access.key", "").trim();
    }

    public static String getGoogleMapsApiKey() {
        return props.getProperty("google.maps.api.key", "").trim();
    }

    public static String getExchangeRateApiKey() {
        return props.getProperty("exchangerate.api.key", "").trim();
    }

    public static boolean hasUnsplashKey() { return !getUnsplashAccessKey().isEmpty(); }
    public static boolean hasGoogleMapsKey() { return !getGoogleMapsApiKey().isEmpty(); }
    public static boolean hasExchangeRateKey() { return !getExchangeRateApiKey().isEmpty(); }
}
