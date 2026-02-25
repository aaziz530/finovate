package org.esprit.finovate.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/** Exchange Rate API - TND to EUR conversion */
public class ExchangeRateService {
    private static final String BASE = "https://v6.exchangerate-api.com/v6";
    private final HttpClient client = HttpClient.newHttpClient();
    private Double tndToEurRate = null;
    private long lastFetch = 0;
    private static final long CACHE_MS = TimeUnit.HOURS.toMillis(6);

    public Double getTndToEurRate() {
        if (tndToEurRate != null && System.currentTimeMillis() - lastFetch < CACHE_MS) {
            return tndToEurRate;
        }
        String key = ApiConfig.getExchangeRateApiKey();
        if (key.isEmpty()) return null;
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + "/" + key + "/latest/TND"))
                    .GET()
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) return tndToEurRate;

            JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();
            if (!"success".equals(root.get("result").getAsString())) return tndToEurRate;
            JsonObject rates = root.getAsJsonObject("conversion_rates");
            tndToEurRate = rates.get("EUR").getAsDouble();
            lastFetch = System.currentTimeMillis();
            return tndToEurRate;
        } catch (Exception e) {
            e.printStackTrace();
            return tndToEurRate;
        }
    }

    public Double tndToEur(double tnd) {
        Double rate = getTndToEurRate();
        return rate != null ? tnd * rate : null;
    }

    public String formatTndAndEur(double tnd) {
        Double eur = tndToEur(tnd);
        if (eur != null) {
            return String.format("%.2f TND (≈ %.2f €)", tnd, eur);
        }
        return String.format("%.2f TND", tnd);
    }
}
