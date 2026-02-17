package org.example.controller;

import javafx.animation.*;
import javafx.scene.layout.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.geometry.Insets;

import java.util.Stack;

/**
 * Gestionnaire d'overlays pour navigation moderne type Reddit/Facebook
 * Permet d'ouvrir des vues en superposition sans changer de page
 */
public class OverlayManager {
    
    private StackPane rootContainer;
    private Pane mainContent;
    private Stack<OverlayContext> overlayStack = new Stack<>();
    private BoxBlur backgroundBlur = new BoxBlur(10, 10, 3);
    
    public OverlayManager(StackPane rootContainer, Pane mainContent) {
        this.rootContainer = rootContainer;
        this.mainContent = mainContent;
    }
    
    /**
     * Ouvre un overlay avec animation
     */
    public void openOverlay(Pane overlayContent, OverlayType type) {
        // Créer le fond sombre semi-transparent
        Pane backdrop = new Pane();
        backdrop.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        backdrop.setOpacity(0);
        
        // Conteneur de l'overlay
        VBox overlayContainer = new VBox();
        overlayContainer.setMaxWidth(type.getWidth());
        overlayContainer.setMaxHeight(type.getHeight());
        overlayContainer.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 15;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 20, 0, 0, 5);"
        );
        overlayContainer.getChildren().add(overlayContent);
        
        // Centrer l'overlay
        StackPane overlayWrapper = new StackPane(overlayContainer);
        overlayWrapper.setStyle("-fx-background-color: transparent;");
        overlayWrapper.setOpacity(0);
        overlayWrapper.setScaleX(0.8);
        overlayWrapper.setScaleY(0.8);
        
        // Fermer au clic sur le backdrop
        backdrop.setOnMouseClicked(e -> closeTopOverlay());
        
        // Ajouter à la pile
        OverlayContext context = new OverlayContext(backdrop, overlayWrapper, overlayContent);
        overlayStack.push(context);
        
        // Ajouter au conteneur
        rootContainer.getChildren().addAll(backdrop, overlayWrapper);
        
        // Appliquer le blur au contenu principal
        if (overlayStack.size() == 1) {
            mainContent.setEffect(backgroundBlur);
        }
        
        // Animation d'ouverture
        animateOpen(backdrop, overlayWrapper);
    }
    
    /**
     * Ferme l'overlay du dessus
     */
    public void closeTopOverlay() {
        if (overlayStack.isEmpty()) return;
        
        OverlayContext context = overlayStack.pop();
        
        // Animation de fermeture
        animateClose(context.backdrop, context.wrapper, () -> {
            rootContainer.getChildren().removeAll(context.backdrop, context.wrapper);
            
            // Retirer le blur si plus d'overlays
            if (overlayStack.isEmpty()) {
                mainContent.setEffect(null);
            }
        });
    }
    
    /**
     * Ferme tous les overlays
     */
    public void closeAllOverlays() {
        while (!overlayStack.isEmpty()) {
            closeTopOverlay();
        }
    }
    
    /**
     * Vérifie si des overlays sont ouverts
     */
    public boolean hasOpenOverlays() {
        return !overlayStack.isEmpty();
    }
    
    /**
     * Animation d'ouverture
     */
    private void animateOpen(Pane backdrop, StackPane wrapper) {
        // Fade in backdrop
        FadeTransition backdropFade = new FadeTransition(Duration.millis(200), backdrop);
        backdropFade.setFromValue(0);
        backdropFade.setToValue(1);
        
        // Scale + fade in overlay
        ParallelTransition overlayAnim = new ParallelTransition();
        
        FadeTransition overlayFade = new FadeTransition(Duration.millis(200), wrapper);
        overlayFade.setFromValue(0);
        overlayFade.setToValue(1);
        
        ScaleTransition overlayScale = new ScaleTransition(Duration.millis(200), wrapper);
        overlayScale.setFromX(0.8);
        overlayScale.setFromY(0.8);
        overlayScale.setToX(1.0);
        overlayScale.setToY(1.0);
        overlayScale.setInterpolator(Interpolator.EASE_OUT);
        
        overlayAnim.getChildren().addAll(overlayFade, overlayScale);
        
        // Jouer les animations
        backdropFade.play();
        overlayAnim.play();
    }
    
    /**
     * Animation de fermeture
     */
    private void animateClose(Pane backdrop, StackPane wrapper, Runnable onFinished) {
        // Fade out backdrop
        FadeTransition backdropFade = new FadeTransition(Duration.millis(150), backdrop);
        backdropFade.setFromValue(1);
        backdropFade.setToValue(0);
        
        // Scale + fade out overlay
        ParallelTransition overlayAnim = new ParallelTransition();
        
        FadeTransition overlayFade = new FadeTransition(Duration.millis(150), wrapper);
        overlayFade.setFromValue(1);
        overlayFade.setToValue(0);
        
        ScaleTransition overlayScale = new ScaleTransition(Duration.millis(150), wrapper);
        overlayScale.setFromX(1.0);
        overlayScale.setFromY(1.0);
        overlayScale.setToX(0.9);
        overlayScale.setToY(0.9);
        overlayScale.setInterpolator(Interpolator.EASE_IN);
        
        overlayAnim.getChildren().addAll(overlayFade, overlayScale);
        overlayAnim.setOnFinished(e -> onFinished.run());
        
        // Jouer les animations
        backdropFade.play();
        overlayAnim.play();
    }
    
    /**
     * Types d'overlay prédéfinis
     */
    public enum OverlayType {
        SMALL(400, 300),
        MEDIUM(600, 500),
        LARGE(800, 600),
        XLARGE(1000, 700),
        FULL_POST(900, 800);
        
        private final double width;
        private final double height;
        
        OverlayType(double width, double height) {
            this.width = width;
            this.height = height;
        }
        
        public double getWidth() { return width; }
        public double getHeight() { return height; }
    }
    
    /**
     * Contexte d'un overlay
     */
    private static class OverlayContext {
        Pane backdrop;
        StackPane wrapper;
        Pane content;
        
        OverlayContext(Pane backdrop, StackPane wrapper, Pane content) {
            this.backdrop = backdrop;
            this.wrapper = wrapper;
            this.content = content;
        }
    }
}
