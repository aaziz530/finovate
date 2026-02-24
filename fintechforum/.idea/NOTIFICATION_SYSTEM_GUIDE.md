# Guide d'Intégration - Système de Notifications

## 🔔 Concept

Affiche des notifications en temps réel quand un nouveau post est créé dans un forum que l'utilisateur a rejoint.

## ✨ Fonctionnalités

- ✅ Vérification automatique toutes les 5 secondes
- ✅ Notifications animées (slide + fade)
- ✅ Auto-suppression après 10 secondes
- ✅ Clic pour fermer manuellement
- ✅ Affichage en haut à droite
- ✅ Design moderne avec bordure bleue
- ✅ Évite les doublons

## 🔧 Intégration dans MainController

### Étape 1: Modifier le FXML

Ajouter un conteneur pour les notifications dans votre fichier FXML principal:

```xml
<StackPane fx:id="rootContainer" xmlns:fx="http://javafx.com/fxml">
    <BorderPane fx:id="mainContent">
        <!-- Votre contenu actuel -->
    </BorderPane>
    
    <!-- Conteneur de notifications (en haut à droite) -->
    <VBox fx:id="notificationContainer" 
          StackPane.alignment="TOP_RIGHT"
          mouseTransparent="true"
          pickOnBounds="false"/>
</StackPane>
```

### Étape 2: Initialiser dans MainController

```java
public class MainController {
    @FXML private VBox notificationContainer;
    
    private NotificationManager notificationManager;
    private int currentUserId;
    
    @FXML
    public void initialize() {
        // Initialiser après la connexion de l'utilisateur
    }
    
    public void onUserLoggedIn(int userId) {
        this.currentUserId = userId;
        
        // Créer et démarrer le gestionnaire de notifications
        notificationManager = new NotificationManager(notificationContainer, userId);
        notificationManager.startMonitoring();
    }
    
    // Arrêter les notifications à la déconnexion
    public void onUserLogout() {
        if (notificationManager != null) {
            notificationManager.stopMonitoring();
        }
    }
}
```

### Étape 3: Nettoyer à la fermeture de l'application

```java
@Override
public void stop() {
    if (notificationManager != null) {
        notificationManager.stopMonitoring();
    }
}
```

## 🎨 Personnalisation

### Modifier l'intervalle de vérification

Dans `NotificationManager.java`, ligne 47:

```java
// Vérifier toutes les 10 secondes au lieu de 5
scheduler.scheduleAtFixedRate(this::checkForNewPosts, 10, 10, TimeUnit.SECONDS);
```

### Modifier la durée d'affichage

Dans `NotificationManager.java`, ligne 195:

```java
// Afficher pendant 15 secondes au lieu de 10
Timeline autoRemove = new Timeline(new KeyFrame(Duration.seconds(15), e -> {
    removeNotification(notificationBox);
}));
```

### Modifier le style des notifications

Dans `NotificationManager.java`, ligne 118-125:

```java
notificationBox.setStyle(
    "-fx-background-color: white;" +
    "-fx-background-radius: 12;" +
    "-fx-border-color: #4CAF50;" +  // Vert au lieu de bleu
    "-fx-border-width: 3;" +         // Bordure plus épaisse
    "-fx-border-radius: 12;" +
    "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 20, 0, 0, 5);"
);
```

## 🔗 Intégration avec OverlayManager

Pour ouvrir le post quand on clique sur la notification:

```java
// Dans NotificationManager.java, ligne 175
notificationBox.setOnMouseClicked(e -> {
    if (e.getTarget() != closeBtn) {
        // Ouvrir le post dans un overlay
        if (overlayManager != null) {
            PostOverlayController postOverlay = new PostOverlayController(
                overlayManager,
                data.postId,
                currentUserId
            );
            overlayManager.openOverlay(
                postOverlay.getView(),
                OverlayManager.OverlayType.FULL_POST
            );
        }
        removeNotification(notificationBox);
    }
});
```

Pour cela, passer l'overlayManager au NotificationManager:

```java
public NotificationManager(VBox notificationContainer, int currentUserId, OverlayManager overlayManager) {
    this.notificationContainer = notificationContainer;
    this.currentUserId = currentUserId;
    this.overlayManager = overlayManager;
    // ...
}
```

## 📊 Requête SQL

Le système vérifie les nouveaux posts avec cette requête:

```sql
SELECT p.id, p.title, p.created_at, u.username, f.name as forum_name, f.id as forum_id
FROM posts p
INNER JOIN forums f ON p.forum_id = f.id
INNER JOIN users u ON p.author_id = u.id
INNER JOIN user_forum uf ON f.id = uf.forum_id
WHERE uf.user_id = ?           -- Forums rejoints par l'utilisateur
AND p.author_id != ?           -- Pas ses propres posts
AND p.created_at > ?           -- Nouveaux posts depuis la dernière vérification
ORDER BY p.created_at DESC
LIMIT 5
```

## 🎯 Exemple de Notification

```
┌─────────────────────────────────────┐
│ 🔔 Nouveau Post                     │ ✕
│ 📁 Forum Crypto                     │
│                                     │
│ Bitcoin atteint 50k$ !              │
│ Par: JohnDoe                        │
└─────────────────────────────────────┘
```

## 🐛 Gestion des Erreurs

Le système gère automatiquement:
- Connexions SQL échouées (log dans console)
- Doublons de notifications (Set de IDs)
- Nettoyage de la mémoire (après 100 notifications)
- Thread daemon (ne bloque pas la fermeture de l'app)

## ⚡ Performance

- **Thread séparé** pour les vérifications SQL
- **Platform.runLater()** pour les mises à jour UI
- **Daemon thread** qui ne bloque pas l'application
- **Limite de 5 notifications** par vérification
- **Nettoyage automatique** de la mémoire

## 🎨 Animations

### Entrée (300ms)
- Slide de droite vers gauche
- Fade in (0 → 1)
- Interpolation EASE_OUT

### Sortie (250ms)
- Slide vers la droite
- Fade out (1 → 0)
- Interpolation EASE_IN

## 📝 TODO Optionnel

1. Ajouter un son de notification
2. Ajouter un compteur de notifications non lues
3. Ajouter un historique des notifications
4. Ajouter des filtres (par forum, par type)
5. Ajouter des notifications pour:
   - Nouveaux commentaires sur vos posts
   - Réponses à vos commentaires
   - Mentions (@username)
   - Nouveaux membres dans vos forums

## 🔊 Ajouter un Son (Optionnel)

```java
import javafx.scene.media.AudioClip;

private AudioClip notificationSound;

public NotificationManager(...) {
    // ...
    try {
        notificationSound = new AudioClip(
            getClass().getResource("/sounds/notification.mp3").toString()
        );
    } catch (Exception e) {
        // Son non disponible
    }
}

private void showNotification(NotificationData data) {
    // ...
    if (notificationSound != null) {
        notificationSound.play();
    }
}
```

## 🎯 Avantages

1. **Temps réel** - Notifications instantanées (5s de délai)
2. **Non intrusif** - Coin supérieur droit, auto-suppression
3. **Performant** - Thread séparé, pas de blocage UI
4. **Élégant** - Animations fluides, design moderne
5. **Intelligent** - Évite les doublons, nettoie la mémoire
