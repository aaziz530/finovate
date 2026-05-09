package org.esprit.finovate.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/** Unsplash API - search photos for project images */
public class UnsplashService {
    private static final String BASE = "https://api.unsplash.com";
    private final HttpClient client = HttpClient.newHttpClient();

    public record UnsplashPhoto(String id, String urlSmall, String urlRegular, String alt) {}

    public List<UnsplashPhoto> searchPhotos(String query) {
        return searchPhotos(query, 10);
    }

    public List<UnsplashPhoto> searchPhotos(String query, int perPage) {
        if (!ApiConfig.hasUnsplashKey()) return List.of();
        if (query == null || query.isBlank()) query = "project";
        try {
            String q = java.net.URLEncoder.encode(query.trim(), java.nio.charset.StandardCharsets.UTF_8);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + "/search/photos?query=" + q + "&per_page=" + perPage))
                    .header("Authorization", "Client-ID " + ApiConfig.getUnsplashAccessKey())
                    .GET()
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) return List.of();

            JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();
            JsonArray results = root.getAsJsonArray("results");
            if (results == null) return List.of();

            List<UnsplashPhoto> photos = new ArrayList<>();
            for (JsonElement el : results) {
                JsonObject r = el.getAsJsonObject();
                String id = getStr(r, "id");
                JsonObject urls = r.getAsJsonObject("urls");
                String small = urls != null ? getStr(urls, "small") : "";
                String regular = urls != null ? getStr(urls, "regular") : "";
                String altStr = getStr(r, "alt_description");
                if (altStr == null) altStr = getStr(r, "description");
                if (altStr == null) altStr = "";
                photos.add(new UnsplashPhoto(id, small, regular, altStr));
            }
            return photos;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private static String getStr(JsonObject o, String key) {
        JsonElement e = o.get(key);
        return e != null && !e.isJsonNull() ? e.getAsString() : null;
    }
}
