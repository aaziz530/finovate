package org.esprit.finovate.controllers;

import javafx.scene.control.Alert;

/**
 * Forum create flows ({@link CreateForumPageController}, {@link CreatePostPageController})
 * expect a parent controller with navigation hooks. The full multi-screen forum shell
 * (from {@code develop}) is optional here—this stub satisfies compilation and shows a
 * hint if navigation is invoked without that shell.
 */
public class MainController {

    public void showForums() {
        informForumNavigationStub("Forums");
    }

    public void showPosts(Long forumId) {
        informForumNavigationStub("Posts (forum id=" + forumId + ")");
    }

    private static void informForumNavigationStub(String context) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Forum navigation");
        a.setHeaderText(null);
        a.setContentText(
                "Forum list/posts UI is not wired in this build.\nYou can still create forums/posts; "
                        + "use the main app features to continue.\n(" + context + ")");
        a.showAndWait();
    }
}
