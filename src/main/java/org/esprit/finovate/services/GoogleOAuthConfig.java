package org.esprit.finovate.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GoogleOAuthConfig {
    private final String clientId;
    private final String clientSecret;

    public GoogleOAuthConfig(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public static GoogleOAuthConfig load() throws IOException {
        Properties props = new Properties();
        try (InputStream is = GoogleOAuthConfig.class.getResourceAsStream("/google_oauth.properties")) {
            if (is == null) {
                throw new IOException("Missing resource: /google_oauth.properties");
            }
            props.load(is);
        }

        String clientId = props.getProperty("client_id");
        String clientSecret = props.getProperty("client_secret");

        if (clientId == null || clientId.isBlank()) {
            throw new IOException("google_oauth.properties: client_id is missing");
        }

        if (clientSecret == null) {
            clientSecret = "";
        }

        return new GoogleOAuthConfig(clientId.trim(), clientSecret.trim());
    }
}
