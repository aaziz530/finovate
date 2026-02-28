package org.esprit.finovate.utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

public class CaptchaService {

    private static final int WIDTH = 200;
    private static final int HEIGHT = 60;
    private static final int CODE_LENGTH = 5;
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private String currentCode;
    private final Random random = new Random();

    public CaptchaService() {
        generateNewCaptcha();
    }

    public void generateNewCaptcha() {
        currentCode = generateRandomCode();
    }

    public String getCurrentCode() {
        return currentCode;
    }

    public boolean verify(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return currentCode.equalsIgnoreCase(input.trim().toUpperCase());
    }

    public Image generateCaptchaImage() {
        BufferedImage bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        // Background with gradient
        GradientPaint gradient = new GradientPaint(0, 0, new Color(240, 253, 244), WIDTH, HEIGHT, new Color(220, 252, 231));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Add noise lines
        g2d.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < 8; i++) {
            g2d.setColor(new Color(random.nextInt(100, 200), random.nextInt(100, 200), random.nextInt(100, 200), 100));
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Add noise dots
        for (int i = 0; i < 50; i++) {
            g2d.setColor(new Color(random.nextInt(50, 200), random.nextInt(50, 200), random.nextInt(50, 200)));
            g2d.fillOval(random.nextInt(WIDTH), random.nextInt(HEIGHT), 3, 3);
        }

        // Draw distorted text
        Font font = new Font("Arial", Font.BOLD, 32);
        g2d.setFont(font);

        FontRenderContext frc = g2d.getFontRenderContext();
        TextLayout layout = new TextLayout(currentCode, font, frc);

        // Calculate centered position
        float textWidth = layout.getAdvance();
        float textHeight = layout.getAscent() + layout.getDescent();
        float startX = (WIDTH - textWidth) / 2;
        float startY = (HEIGHT + textHeight / 2) / 2;

        // Apply distortion and draw each character
        float charX = startX;
        for (int i = 0; i < currentCode.length(); i++) {
            String ch = String.valueOf(currentCode.charAt(i));
            AffineTransform transform = new AffineTransform();

            // Random rotation
            double angle = (random.nextDouble() - 0.5) * 0.4;
            transform.rotate(angle, charX + 12, startY);

            // Random scale
            double scale = 0.9 + random.nextDouble() * 0.3;
            transform.scale(scale, scale);

            g2d.setTransform(transform);

            // Draw shadow
            g2d.setColor(new Color(150, 150, 150, 100));
            g2d.drawString(ch, charX + 2, startY + 2);

            // Draw character with random color
            g2d.setColor(new Color(35, 127, 78)); // Finovate green
            g2d.drawString(ch, charX, startY);

            Rectangle2D bounds = font.getStringBounds(ch, frc);
            charX += bounds.getWidth() + 2;
        }

        g2d.dispose();

        // Convert BufferedImage to JavaFX Image
        return convertToFxImage(bufferedImage);
    }

    private String generateRandomCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    private Image convertToFxImage(BufferedImage bufferedImage) {
        WritableImage fxImage = new WritableImage(WIDTH, HEIGHT);
        PixelWriter pixelWriter = fxImage.getPixelWriter();

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                int rgb = bufferedImage.getRGB(x, y);
                java.awt.Color awtColor = new java.awt.Color(rgb, true);
                javafx.scene.paint.Color fxColor = javafx.scene.paint.Color.rgb(
                        awtColor.getRed(),
                        awtColor.getGreen(),
                        awtColor.getBlue(),
                        awtColor.getAlpha() / 255.0
                );
                pixelWriter.setColor(x, y, fxColor);
            }
        }

        return fxImage;
    }
}
