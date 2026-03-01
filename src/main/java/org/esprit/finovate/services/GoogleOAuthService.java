package org.esprit.finovate.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class GoogleOAuthService {
    private static final String AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_ENDPOINT = "https://openidconnect.googleapis.com/v1/userinfo";

    private final GoogleOAuthConfig config;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GoogleOAuthService(GoogleOAuthConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    public GoogleUserInfo authenticate() throws Exception {
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = codeChallenge(codeVerifier);
        int port = findFreePort();
        String redirectUri = "http://127.0.0.1:" + port + "/callback";
        String state = randomUrlSafeString(24);

        BlockingQueue<Map<String, String>> callbackParamsQueue = new ArrayBlockingQueue<>(1);
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/callback", exchange -> {
            try {
                String query = exchange.getRequestURI().getRawQuery();
                Map<String, String> params = parseQuery(query);

                String responseHtml;
                if (params.containsKey("error")) {
                    responseHtml = "<html><body><h3>Google login failed</h3><p>You can close this window and return to the app.</p></body></html>";
                } else {
                    responseHtml = "<html><body><h3>Login successful</h3><p>You can close this window and return to the app.</p></body></html>";
                }

                byte[] bytes = responseHtml.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }

                callbackParamsQueue.offer(params);
            } finally {
                exchange.close();
            }
        });
        server.start();

        try {
            String authUrl = buildAuthUrl(config.getClientId(), redirectUri, state, codeChallenge);
            openBrowser(authUrl);

            Map<String, String> params = callbackParamsQueue.take();

            if (params.containsKey("error")) {
                throw new IllegalStateException("Google authentication error: " + params.get("error"));
            }

            String returnedState = params.get("state");
            if (returnedState == null || !returnedState.equals(state)) {
                throw new IllegalStateException("Invalid OAuth state");
            }

            String code = params.get("code");
            if (code == null || code.isBlank()) {
                throw new IllegalStateException("Missing authorization code");
            }

            String accessToken = exchangeCodeForAccessToken(code, redirectUri, codeVerifier);
            return fetchUserInfo(accessToken);

        } finally {
            server.stop(0);
        }
    }

    private String buildAuthUrl(String clientId, String redirectUri, String state, String codeChallenge) {
        String scope = urlEncode("openid email profile");
        return AUTH_ENDPOINT +
                "?client_id=" + urlEncode(clientId) +
                "&redirect_uri=" + urlEncode(redirectUri) +
                "&response_type=code" +
                "&scope=" + scope +
                "&state=" + urlEncode(state) +
                "&code_challenge=" + urlEncode(codeChallenge) +
                "&code_challenge_method=S256" +
                "&access_type=offline";
    }

    private String exchangeCodeForAccessToken(String code, String redirectUri, String codeVerifier) throws Exception {
        String form = "code=" + urlEncode(code) +
                "&client_id=" + urlEncode(config.getClientId()) +
                "&client_secret=" + urlEncode(config.getClientSecret()) +
                "&redirect_uri=" + urlEncode(redirectUri) +
                "&grant_type=authorization_code" +
                "&code_verifier=" + urlEncode(codeVerifier);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_ENDPOINT))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Token endpoint error: HTTP " + response.statusCode() + " - " + response.body());
        }

        JsonNode json = objectMapper.readTree(response.body());
        JsonNode accessTokenNode = json.get("access_token");
        if (accessTokenNode == null || accessTokenNode.asText().isBlank()) {
            throw new IllegalStateException("Missing access_token in token response");
        }
        return accessTokenNode.asText();
    }

    private GoogleUserInfo fetchUserInfo(String accessToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(USERINFO_ENDPOINT))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("UserInfo endpoint error: HTTP " + response.statusCode() + " - " + response.body());
        }

        JsonNode json = objectMapper.readTree(response.body());
        String email = json.hasNonNull("email") ? json.get("email").asText() : null;
        String givenName = json.hasNonNull("given_name") ? json.get("given_name").asText() : null;
        String familyName = json.hasNonNull("family_name") ? json.get("family_name").asText() : null;

        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Google did not return an email");
        }

        return new GoogleUserInfo(email, givenName, familyName);
    }

    private static void openBrowser(String url) throws Exception {
        if (!Desktop.isDesktopSupported()) {
            throw new IllegalStateException("Desktop browsing is not supported on this system");
        }
        Desktop.getDesktop().browse(URI.create(url));
    }

    private static int findFreePort() throws IOException {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        }
    }

    private static String generateCodeVerifier() {
        return randomUrlSafeString(64);
    }

    private static String codeChallenge(String codeVerifier) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }

    private static String randomUrlSafeString(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String urlEncode(String v) {
        return URLEncoder.encode(v, StandardCharsets.UTF_8);
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> map = new HashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) {
            return map;
        }

        String[] parts = rawQuery.split("&");
        for (String part : parts) {
            int idx = part.indexOf('=');
            if (idx <= 0) {
                continue;
            }
            String key = URLDecoder.decode(part.substring(0, idx), StandardCharsets.UTF_8);
            String val = URLDecoder.decode(part.substring(idx + 1), StandardCharsets.UTF_8);
            map.put(key, val);
        }
        return map;
    }

    public record GoogleUserInfo(String email, String givenName, String familyName) {
    }
}
