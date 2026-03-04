package org.esprit.finovate.utils;

import javafx.stage.Stage;

/**
 * Shared scene/window dimensions for consistent layout. Fits laptop screens.
 */
public final class SceneUtils {
    public static final int WIDTH = 1100;
    public static final int HEIGHT = 700;

    private SceneUtils() {
    }

    public static void applyStageSize(Stage stage) {
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);
        stage.setMinWidth(1000);
        stage.setMinHeight(600);
        stage.centerOnScreen();
    }
}
