package org.esprit.finovate.api;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/** Builds WebView with OpenStreetMap (Leaflet) for picking or viewing project location. No API key required. */
public class MapPicker {

    public interface LocationCallback {
        void onLocation(double lat, double lng);
    }

    /** Returns a VBox containing the map picker with Confirm and Cancel buttons (JavaFX buttons, no JS bridge). */
    public static VBox createPickerWebView(LocationCallback onConfirm, Runnable onCancel) {
        WebView webView = new WebView();
        webView.setPrefSize(600, 400);
        VBox.setVgrow(webView, Priority.ALWAYS);
        WebEngine engine = webView.getEngine();

        String html = loadHtmlTemplate();
        engine.loadContent(html, "text/html");

        Button btnConfirm = new Button("Confirm");
        btnConfirm.getStyleClass().addAll("btn-primary", "btn-small");
        btnConfirm.setOnAction(e -> {
            try {
                Object result = engine.executeScript("window.getSelectedLocation ? window.getSelectedLocation() : null");
                if (result instanceof String s && !s.isEmpty()) {
                    String[] parts = s.split(",");
                    if (parts.length == 2) {
                        double lat = Double.parseDouble(parts[0].trim());
                        double lng = Double.parseDouble(parts[1].trim());
                        onConfirm.onLocation(lat, lng);
                        return;
                    }
                }
                new Alert(Alert.AlertType.WARNING, "Please place a marker on the map or search for a location first.").showAndWait();
            } catch (Exception ignored) {}
        });

        Button btnCancel = new Button("Cancel");
        btnCancel.getStyleClass().addAll("btn-secondary", "btn-small");
        btnCancel.setOnAction(e -> {
            if (onCancel != null) onCancel.run();
        });

        HBox buttons = new HBox(12, btnConfirm, btnCancel);
        buttons.setAlignment(Pos.CENTER_LEFT);
        buttons.setPadding(new Insets(10));
        buttons.getStyleClass().add("form-actions");

        VBox root = new VBox(webView, buttons);
        root.setFillWidth(true);
        return root;
    }

    public static WebView createViewerWebView(double lat, double lng) {
        WebView webView = new WebView();
        webView.setPrefSize(400, 250);
        WebEngine engine = webView.getEngine();
        String html = loadHtmlTemplate();
        html = html.replace("const isPicker = true", "const isPicker = false");
        html = html.replace("var initialLat, initialLng;", "var initialLat = " + lat + ", initialLng = " + lng + ";");
        engine.loadContent(html, "text/html");
        return webView;
    }

    private static String loadHtmlTemplate() {
        try (var is = MapPicker.class.getResourceAsStream("/html/map_picker.html");
             var reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "<html><body>Map not available.</body></html>";
        }
    }
}
