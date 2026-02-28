package org.esprit.finovate.services;

import io.github.cdimascio.dotenv.Dotenv;

public class GitHubOAuthConfig {
    private final String clientId;
    private final String clientSecret;

    public GitHubOAuthConfig(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public static GitHubOAuthConfig load() {
        Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
        
        String clientId = dotenv.get("GITHUB_CLIENT_ID");
        String clientSecret = dotenv.get("GITHUB_CLIENT_SECRET");

        if (clientId == null || clientSecret == null) {
            clientId = System.getenv("GITHUB_CLIENT_ID");
            clientSecret = System.getenv("GITHUB_CLIENT_SECRET");
        }

        if (clientId == null || clientSecret == null) {
            throw new RuntimeException("GitHub OAuth credentials not found in .env or system environment");
        }

        return new GitHubOAuthConfig(clientId.trim(), clientSecret.trim());
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
