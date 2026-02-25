package org.esprit.finovate.utils;

import javafx.stage.Stage;

/** Shared scene/window dimensions for consistent layout. Fits laptop screens. */
public final class SceneUtils {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;

    private SceneUtils() {}

    public static void applyStageSize(Stage stage) {
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);
        stage.setMinWidth(1024);
        stage.setMinHeight(600);
    }
}
