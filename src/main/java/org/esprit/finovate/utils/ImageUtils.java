package org.esprit.finovate.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/** Saves project images and resolves paths for display. */
public final class ImageUtils {

    private static final String UPLOAD_DIR = "uploads/projects";

    private ImageUtils() {}

    /**
     * Copies the selected file to uploads/projects/ and returns the relative path to store in DB.
     * @param sourcePath absolute path of the selected file
     * @return relative path like "uploads/projects/1234_image.png", or null on failure
     */
    public static String saveProjectImage(String sourcePath) {
        if (sourcePath == null || sourcePath.isBlank()) return null;
        try {
            Path source = Paths.get(sourcePath);
            if (!Files.exists(source)) return null;

            Path uploadDir = Paths.get(UPLOAD_DIR);
            Files.createDirectories(uploadDir);

            String ext = "";
            String name = source.getFileName().toString();
            int dot = name.lastIndexOf('.');
            if (dot > 0) ext = name.substring(dot);

            String filename = System.currentTimeMillis() + ext;
            Path target = uploadDir.resolve(filename);
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

            return UPLOAD_DIR + "/" + filename;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Resolves the stored path/URL for loading in ImageView.
     * Supports: file paths, Unsplash URLs (http/https).
     * @param storedPath path from DB or URL (e.g. "uploads/projects/1234.png" or "https://images.unsplash.com/...")
     * @return absolute file path, or URL string for remote images
     */
    public static String resolveImagePath(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) return null;
        if (storedPath.startsWith("http://") || storedPath.startsWith("https://")) {
            return storedPath;
        }
        Path p = Paths.get(storedPath);
        if (!p.isAbsolute()) {
            p = Paths.get(System.getProperty("user.dir")).resolve(storedPath);
        }
        return Files.exists(p) ? p.toAbsolutePath().toString() : null;
    }

    /** Returns URL string for Image constructor (file:path or http(s) URL). */
    public static String toImageUrl(String storedPath) {
        String resolved = resolveImagePath(storedPath);
        if (resolved == null) return null;
        if (resolved.startsWith("http://") || resolved.startsWith("https://")) return resolved;
        return "file:" + resolved;
    }
}
