package org.esprit.finovate.utils;

import javafx.stage.Stage;

/** Consistent scene/window sizing utilities */
public final class SceneUtils {
    private SceneUtils() {}

    public static final double DEFAULT_WIDTH = 1280;
    public static final double DEFAULT_HEIGHT = 720;

    public static void applyStageSize(Stage stage) {
        if (stage != null) {
            stage.setWidth(DEFAULT_WIDTH);
            stage.setHeight(DEFAULT_HEIGHT);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
        }
    }
}
