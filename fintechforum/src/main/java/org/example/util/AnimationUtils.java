package org.example.util;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Classe utilitaire pour les animations JavaFX
 * Fournit des animations prêtes à l'emploi pour améliorer l'UX
 */
public class AnimationUtils {

    /**
     * Animation de fondu entrant (Fade In)
     */
    public static void fadeIn(Node node) {
        fadeIn(node, 500);
    }

    public static void fadeIn(Node node, int durationMs) {
        FadeTransition fade = new FadeTransition(Duration.millis(durationMs), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    /**
     * Animation de fondu sortant (Fade Out)
     */
    public static void fadeOut(Node node) {
        fadeOut(node, 500);
    }

    public static void fadeOut(Node node, int durationMs) {
        FadeTransition fade = new FadeTransition(Duration.millis(durationMs), node);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.play();
    }

    /**
     * Animation de glissement depuis la gauche
     */
    public static void slideInFromLeft(Node node) {
        slideInFromLeft(node, 400);
    }

    public static void slideInFromLeft(Node node, int durationMs) {
        TranslateTransition slide = new TranslateTransition(Duration.millis(durationMs), node);
        slide.setFromX(-300);
        slide.setToX(0);
        slide.play();
    }

    /**
     * Animation de glissement depuis la droite
     */
    public static void slideInFromRight(Node node) {
        slideInFromRight(node, 400);
    }

    public static void slideInFromRight(Node node, int durationMs) {
        TranslateTransition slide = new TranslateTransition(Duration.millis(durationMs), node);
        slide.setFromX(300);
        slide.setToX(0);
        slide.play();
    }

    /**
     * Animation de glissement vers le haut
     */
    public static void slideInFromBottom(Node node) {
        slideInFromBottom(node, 400);
    }

    public static void slideInFromBottom(Node node, int durationMs) {
        TranslateTransition slide = new TranslateTransition(Duration.millis(durationMs), node);
        slide.setFromY(50);
        slide.setToY(0);
        slide.play();
    }

    /**
     * Animation d'échelle (zoom in)
     */
    public static void scaleIn(Node node) {
        scaleIn(node, 300);
    }

    public static void scaleIn(Node node, int durationMs) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(durationMs), node);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.play();
    }

    /**
     * Animation de pulsation (pulse)
     */
    public static void pulse(Node node) {
        pulse(node, 500);
    }

    public static void pulse(Node node, int durationMs) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(durationMs), node);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.05);
        scale.setToY(1.05);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.play();
    }

    /**
     * Animation de secousse (shake)
     */
    public static void shake(Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), node);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }

    /**
     * Animation de rotation
     */
    public static void rotate(Node node) {
        rotate(node, 500);
    }

    public static void rotate(Node node, int durationMs) {
        RotateTransition rotate = new RotateTransition(Duration.millis(durationMs), node);
        rotate.setByAngle(360);
        rotate.play();
    }

    /**
     * Animation d'entrée de carte (combinée: fade + slide + scale)
     */
    public static void animateCardEntry(Node card) {
        animateCardEntry(card, 500);
    }

    public static void animateCardEntry(Node card, int durationMs) {
        // Fade in
        FadeTransition fade = new FadeTransition(Duration.millis(durationMs), card);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        
        // Slide up
        TranslateTransition slide = new TranslateTransition(Duration.millis(durationMs), card);
        slide.setFromY(30);
        slide.setToY(0);
        
        // Scale
        ScaleTransition scale = new ScaleTransition(Duration.millis(durationMs), card);
        scale.setFromX(0.95);
        scale.setFromY(0.95);
        scale.setToX(1.0);
        scale.setToY(1.0);
        
        // Jouer toutes les animations en parallèle
        ParallelTransition parallel = new ParallelTransition(fade, slide, scale);
        parallel.play();
    }

    /**
     * Animation de survol (hover) - agrandir légèrement
     */
    public static void addHoverEffect(Node node) {
        addHoverEffect(node, 1.02);
    }

    public static void addHoverEffect(Node node, double scaleFactor) {
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), node);
        scaleUp.setToX(scaleFactor);
        scaleUp.setToY(scaleFactor);
        
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), node);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        
        node.setOnMouseEntered(e -> scaleUp.play());
        node.setOnMouseExited(e -> scaleDown.play());
    }

    /**
     * Animation de clic (press effect)
     */
    public static void addPressEffect(Node node) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), node);
        scaleDown.setToX(0.95);
        scaleDown.setToY(0.95);
        
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), node);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);
        
        node.setOnMousePressed(e -> scaleDown.play());
        node.setOnMouseReleased(e -> scaleUp.play());
    }

    /**
     * Animation de rebond (bounce)
     */
    public static void bounce(Node node) {
        TranslateTransition bounce1 = new TranslateTransition(Duration.millis(100), node);
        bounce1.setToY(-10);
        
        TranslateTransition bounce2 = new TranslateTransition(Duration.millis(100), node);
        bounce2.setToY(0);
        
        TranslateTransition bounce3 = new TranslateTransition(Duration.millis(80), node);
        bounce3.setToY(-5);
        
        TranslateTransition bounce4 = new TranslateTransition(Duration.millis(80), node);
        bounce4.setToY(0);
        
        SequentialTransition sequence = new SequentialTransition(bounce1, bounce2, bounce3, bounce4);
        sequence.play();
    }

    /**
     * Animation de chargement avec délai
     */
    public static void animateWithDelay(Node node, int delayMs) {
        PauseTransition pause = new PauseTransition(Duration.millis(delayMs));
        pause.setOnFinished(e -> animateCardEntry(node));
        pause.play();
    }

    /**
     * Animation de liste (anime chaque élément avec un délai)
     */
    public static void animateList(javafx.collections.ObservableList<Node> nodes) {
        animateList(nodes, 50);
    }

    public static void animateList(javafx.collections.ObservableList<Node> nodes, int delayBetweenMs) {
        int delay = 0;
        for (Node node : nodes) {
            animateWithDelay(node, delay);
            delay += delayBetweenMs;
        }
    }

    /**
     * Animation de disparition (fade out puis suppression)
     */
    public static void fadeOutAndRemove(Node node, Runnable onFinished) {
        FadeTransition fade = new FadeTransition(Duration.millis(300), node);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });
        fade.play();
    }

    /**
     * Animation de succès (scale + glow effect)
     */
    public static void successAnimation(Node node) {
        ScaleTransition scale1 = new ScaleTransition(Duration.millis(150), node);
        scale1.setToX(1.1);
        scale1.setToY(1.1);
        
        ScaleTransition scale2 = new ScaleTransition(Duration.millis(150), node);
        scale2.setToX(1.0);
        scale2.setToY(1.0);
        
        SequentialTransition sequence = new SequentialTransition(scale1, scale2);
        sequence.play();
    }

    /**
     * Animation d'erreur (shake + couleur rouge temporaire)
     */
    public static void errorAnimation(Node node) {
        String originalStyle = node.getStyle();
        
        // Shake
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), node);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        
        // Restaurer le style original après l'animation
        shake.setOnFinished(e -> {
            PauseTransition pause = new PauseTransition(Duration.millis(500));
            pause.setOnFinished(ev -> node.setStyle(originalStyle));
            pause.play();
        });
        
        shake.play();
    }
}
