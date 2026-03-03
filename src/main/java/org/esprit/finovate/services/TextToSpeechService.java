package org.esprit.finovate.services;

import javazoom.jl.player.Player;

import java.io.FileInputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.Arrays;
import java.util.List;

public class TextToSpeechService {

    private Player currentPlayer;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Reads text aloud using Google TTS. Stops any previous audio automatically.
     * 
     * @param text The text to read.
     * @param lang The language code ("fr" or "en"). Use "auto" to let the service
     *             detect.
     * @return A CompletableFuture with true if successful, false if network error
     *         or failure.
     */
    public CompletableFuture<Boolean> readText(String text, String lang) {
        return CompletableFuture.supplyAsync(() -> {
            Path tempFile = null;
            try {
                // Stop any currently playing audio
                stopPlayback();

                // Clean the text slightly to avoid URL breaks and limit length per request if
                // needed
                // Google TTS has a limit of ~200 chars per request, for long messages we could
                // chunk,
                // but for simple chat messages it's usually enough.
                String safeText = text.length() > 200 ? text.substring(0, 200) : text;
                // Remove emojis and weird symbols that might break the API
                String cleanText = safeText.replaceAll("[^a-zA-Z0-9àâéèêëîïôùûüçÀÂÉÈÊËÎÏÔÙÛÜÇ\\s.,!?'’-]", "");

                if (cleanText.trim().isEmpty()) {
                    return false;
                }

                String detectLang = ("auto".equalsIgnoreCase(lang) || lang == null) ? guessLanguage(cleanText) : lang;

                String query = URLEncoder.encode(cleanText, StandardCharsets.UTF_8);
                String url = "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=" + detectLang + "&q="
                        + query;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                // Download MP3 to a temporary file
                tempFile = Files.createTempFile("tts_audio_", ".mp3");
                HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(tempFile));

                if (response.statusCode() == 200) {
                    try (FileInputStream fis = new FileInputStream(tempFile.toFile())) {
                        currentPlayer = new Player(fis);
                        currentPlayer.play(); // This blocks, but we are inside supplyAsync (background thread)
                    } catch (Exception e) {
                        System.err.println("Error playing TTS Media: " + e.getMessage());
                        return false;
                    } finally {
                        try {
                            Files.deleteIfExists(tempFile);
                        } catch (Exception ignored) {
                        }
                    }
                    return true;
                } else {
                    try {
                        Files.deleteIfExists(tempFile);
                    } catch (Exception ignored) {
                    }
                    System.err.println("TTS API returned status: " + response.statusCode());
                    return false;
                }
            } catch (Exception e) {
                if (tempFile != null) {
                    try {
                        Files.deleteIfExists(tempFile);
                    } catch (Exception ignored) {
                    }
                }
                System.err.println("TTS Network Error: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Stops the playback of the current audio.
     */
    public void stopPlayback() {
        if (currentPlayer != null) {
            try {
                currentPlayer.close();
            } catch (Exception ignored) {
            }
            currentPlayer = null;
        }
    }

    /**
     * A very simple heuristic to guess if text is French or English based on common
     * stop words.
     */
    private String guessLanguage(String text) {
        String lowerText = text.toLowerCase();
        List<String> frWords = Arrays.asList(" et ", " ou ", " dans ", " pour ", " avec ", " le ", " la ", " les ",
                " un ", " une ", " des ", " je ", " tu ", " il ", " nous ", " vous ");
        int frCount = 0;
        for (String w : frWords) {
            if (lowerText.contains(w))
                frCount++;
        }

        List<String> enWords = Arrays.asList(" and ", " or ", " in ", " for ", " with ", " the ", " a ", " an ", " i ",
                " you ", " he ", " we ", " they ", " she ");
        int enCount = 0;
        for (String w : enWords) {
            if (lowerText.contains(w))
                enCount++;
        }

        return frCount > enCount ? "fr" : "en";
    }
}
