# Exemples d'Implémentation des Animations

## Comment Utiliser AnimationUtils dans Vos Contrôleurs

### 1. Animer les Cartes lors du Chargement (ForumsController)

```java
import org.example.util.AnimationUtils;

private class ForumCell extends ListCell<ForumItem> {
    @Override
    protected void updateItem(ForumItem forum, boolean empty) {
        super.updateItem(forum, empty);

        if (empty || forum == null) {
            setGraphic(null);
            setText(null);
        } else {
            VBox card = new VBox(10);
            // ... votre code existant pour créer la carte ...
            
            // NOUVEAU: Animer l'entrée de la carte
            card.setOpacity(0); // Commencer invisible
            AnimationUtils.animateCardEntry(card);
            
            // NOUVEAU: Ajouter effet de survol
            AnimationUtils.addHoverEffect(card, 1.02);
            
            setGraphic(card);
        }
    }
}
```

### 2. Animer les Boutons (PostsController)

```java
import org.example.util.AnimationUtils;

// Dans votre méthode de création de boutons
Button shareBtn = new Button("📤 Partager");
shareBtn.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white;");

// NOUVEAU: Ajouter effet de clic
AnimationUtils.addPressEffect(shareBtn);

shareBtn.setOnAction(e -> {
    // NOUVEAU: Animation de succès après le partage
    sharePost(post.getId());
    AnimationUtils.successAnimation(shareBtn);
    e.consume();
});
```

### 3. Animer les Notifications (AlertsController)

```java
import org.example.util.AnimationUtils;

private class AlertCell extends ListCell<AlertItem> {
    @Override
    protected void updateItem(AlertItem alert, boolean empty) {
        super.updateItem(alert, empty);

        if (empty || alert == null) {
            setGraphic(null);
            setText(null);
        } else {
            VBox card = new VBox(10);
            // ... votre code existant ...
            
            // NOUVEAU: Animation différente pour les alertes non lues
            if (!alert.isRead()) {
                card.setOpacity(0);
                AnimationUtils.animateCardEntry(card);
                AnimationUtils.pulse(card); // Attirer l'attention
            }
            
            Button deleteBtn = new Button("🗑️");
            deleteBtn.setOnAction(e -> {
                // NOUVEAU: Animation de disparition avant suppression
                AnimationUtils.fadeOutAndRemove(card, () -> {
                    deleteAlert(alert);
                });
                e.consume();
            });
            
            setGraphic(card);
        }
    }
}
```

### 4. Animer le Chargement des Posts (PostsController)

```java
import org.example.util.AnimationUtils;
import javafx.application.Platform;

private void loadPostsFromDB() {
    ObservableList<PostItem> posts = FXCollections.observableArrayList();

    // ... votre code de chargement depuis la DB ...

    postsList.setItems(posts);
    
    // NOUVEAU: Animer chaque post avec un délai progressif
    Platform.runLater(() -> {
        int delay = 0;
        for (int i = 0; i < postsList.getItems().size(); i++) {
            ListCell<PostItem> cell = (ListCell<PostItem>) postsList.lookup(".list-cell:nth-child(" + (i+1) + ")");
            if (cell != null && cell.getGraphic() != null) {
                cell.getGraphic().setOpacity(0);
                AnimationUtils.animateWithDelay(cell.getGraphic(), delay);
                delay += 50; // 50ms entre chaque carte
            }
        }
    });
}
```

### 5. Animer les Votes (PostsController)

```java
import org.example.util.AnimationUtils;

Button likeBtn = new Button("👍");
likeBtn.setOnAction(e -> {
    votePost(post.getId(), "UPVOTE");
    
    // NOUVEAU: Animation de succès sur le bouton
    AnimationUtils.bounce(likeBtn);
    
    // NOUVEAU: Animer le compteur
    AnimationUtils.pulse(likeCountLabel);
    
    e.consume();
});
```

### 6. Animer les Erreurs (Validation de Formulaire)

```java
import org.example.util.AnimationUtils;

@FXML
private void handleSubmit() {
    if (titleField.getText().isEmpty()) {
        // NOUVEAU: Animation d'erreur
        AnimationUtils.shake(titleField);
        AnimationUtils.errorAnimation(titleField);
        showError("Le titre est requis");
        return;
    }
    
    // ... reste du code ...
}
```

### 7. Animer l'Ouverture d'un Overlay (CreatePostOverlayController)

```java
import org.example.util.AnimationUtils;

public void show() {
    overlay.setVisible(true);
    overlay.setOpacity(0);
    
    // NOUVEAU: Animation d'entrée de l'overlay
    AnimationUtils.fadeIn(overlay, 300);
    AnimationUtils.scaleIn(contentBox, 300);
}

public void hide() {
    // NOUVEAU: Animation de sortie de l'overlay
    AnimationUtils.fadeOut(overlay, 300);
    
    // Cacher après l'animation
    PauseTransition pause = new PauseTransition(Duration.millis(300));
    pause.setOnFinished(e -> overlay.setVisible(false));
    pause.play();
}
```

### 8. Animer les Recommandations AI (RecommendationsController)

```java
import org.example.util.AnimationUtils;

private class RecommendationCell extends ListCell<RecommendedForum> {
    @Override
    protected void updateItem(RecommendedForum forum, boolean empty) {
        super.updateItem(forum, empty);

        if (empty || forum == null) {
            setGraphic(null);
            setText(null);
        } else {
            VBox card = new VBox(12);
            // ... votre code existant ...
            
            // NOUVEAU: Animation spéciale pour les recommandations AI
            card.setOpacity(0);
            AnimationUtils.slideInFromRight(card, 500);
            AnimationUtils.fadeIn(card, 500);
            
            // NOUVEAU: Effet de glow sur le badge AI
            Label aiLabel = new Label("🤖 AI");
            AnimationUtils.pulse(aiLabel);
            
            setGraphic(card);
        }
    }
}
```

### 9. Animer le Refresh (ForumsController)

```java
import org.example.util.AnimationUtils;

@FXML
private void refreshForums() {
    // NOUVEAU: Animer le bouton de refresh
    Button refreshBtn = (Button) event.getSource();
    AnimationUtils.rotate(refreshBtn, 500);
    
    // Recharger les forums
    loadForums(currentViewType, currentUserId);
}
```

### 10. Animer les Transitions entre Vues (MainController)

```java
import org.example.util.AnimationUtils;

public void showPostsView(int forumId, String forumName) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/posts.fxml"));
        Parent postsView = loader.load();
        
        // NOUVEAU: Animer la transition
        postsView.setOpacity(0);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(postsView);
        
        AnimationUtils.fadeIn(postsView, 300);
        AnimationUtils.slideInFromRight(postsView, 300);
        
        // ... reste du code ...
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

---

## Animations Recommandées par Type d'Action

### Actions de Création
- `animateCardEntry()` - Pour les nouveaux éléments
- `successAnimation()` - Pour confirmer la création
- `fadeIn()` - Pour l'apparition douce

### Actions de Suppression
- `fadeOutAndRemove()` - Pour supprimer avec animation
- `slideOutRight()` - Pour faire glisser hors de l'écran
- `scaleOut()` - Pour réduire puis supprimer

### Actions de Modification
- `pulse()` - Pour attirer l'attention
- `bounce()` - Pour confirmer la modification
- `shake()` - Pour indiquer une erreur

### Interactions Utilisateur
- `addHoverEffect()` - Pour tous les éléments cliquables
- `addPressEffect()` - Pour tous les boutons
- `rotate()` - Pour les boutons de refresh/reload

### Chargement de Données
- `animateList()` - Pour animer une liste d'éléments
- `animateWithDelay()` - Pour créer un effet de cascade
- `fadeIn()` - Pour l'apparition progressive

---

## Conseils d'Utilisation

1. **Ne pas en abuser** - Trop d'animations peuvent ralentir l'interface
2. **Cohérence** - Utilisez les mêmes animations pour les mêmes actions
3. **Durée** - Gardez les animations courtes (200-500ms)
4. **Performance** - Testez sur différentes machines
5. **Accessibilité** - Permettez de désactiver les animations si nécessaire

---

## Mode Sombre (Bonus)

Pour implémenter un mode sombre avec transition animée:

```java
public void toggleDarkMode() {
    Scene scene = root.getScene();
    
    if (scene.getRoot().getStyleClass().contains("dark-mode")) {
        // Passer en mode clair
        AnimationUtils.fadeOut(scene.getRoot(), 200);
        scene.getRoot().getStyleClass().remove("dark-mode");
        AnimationUtils.fadeIn(scene.getRoot(), 200);
    } else {
        // Passer en mode sombre
        AnimationUtils.fadeOut(scene.getRoot(), 200);
        scene.getRoot().getStyleClass().add("dark-mode");
        AnimationUtils.fadeIn(scene.getRoot(), 200);
    }
}
```

Voulez-vous que j'implémente ces animations dans un de vos contrôleurs spécifiques ?
