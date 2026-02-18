package org.esprit.finovate.utils;

import javafx.stage.Stage;

/** Shared scene/window dimensions for consistent layout. */
public final class SceneUtils {
    public static final int WIDTH = 1000;
    public static final int HEIGHT = 600;

    private SceneUtils() {}

    public static void applyStageSize(Stage stage) {
        stage.setMinWidth(WIDTH);
        stage.setMinHeight(HEIGHT);
    }
}
