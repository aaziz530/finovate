package org.esprit.finovate.services;

import io.github.cdimascio.dotenv.Dotenv;

public class GoogleOAuthConfig {
    private final String clientId;
    private final String clientSecret;

    public GoogleOAuthConfig(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public static GoogleOAuthConfig load() {
        Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
        
        String clientId = dotenv.get("GOOGLE_CLIENT_ID");
        String clientSecret = dotenv.get("GOOGLE_CLIENT_SECRET");

        if (clientId == null || clientSecret == null) {
            clientId = System.getenv("GOOGLE_CLIENT_ID");
            clientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
        }

        if (clientId == null || clientSecret == null) {
            throw new RuntimeException("Google OAuth credentials not found in .env or system environment");
        }

        return new GoogleOAuthConfig(clientId.trim(), clientSecret.trim());
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
