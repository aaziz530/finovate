# Guide d'Intégration - Bouton de Notification avec Badge

## 🔔 Composants Créés

### 1. `NotificationButton.java`
Bouton cliquable avec badge rouge affichant le nombre de notifications

### 2. `NotificationCenterController.java`
Centre de notifications affichant toutes les notifications récentes (7 derniers jours)

## 🎯 Fonctionnalités

### NotificationButton
- ✅ Icône 🔔 cliquable
- ✅ Badge rouge avec compteur (1, 2, 3... 99+)
- ✅ Animation au survol (rotation)
- ✅ Animation pour nouvelle notification (shake + scale)
- ✅ Mise à jour automatique toutes les 10 secondes
- ✅ Ouvre le centre de notifications au clic

### NotificationCenter
- ✅ Liste de toutes les notifications (7 derniers jours)
- ✅ Affichage du temps écoulé ("Il y a 2 heures")
- ✅ Compteur de commentaires
- ✅ Clic pour ouvrir le post
- ✅ Bouton "Tout marquer lu"
- ✅ Design moderne avec hover effects

## 🔧 Intégration dans MainController

### Étape 1: Ajouter le bouton dans le Header

```java
public class MainController {
    @FXML private HBox headerBox;  // Votre header existant
    @FXML private StackPane rootContainer;
    
    private OverlayManager overlayManager;
    private NotificationButton notificationButton;
    private int currentUserId;
    
    @FXML
    public void initialize() {
        // Initialiser l'overlay manager
        overlayManager = new OverlayManager(rootContainer, mainContent);
    }
    
    public void onUserLoggedIn(int userId) {
        this.currentUserId = userId;
        
        // Créer et ajouter le bouton de notification
        notificationButton = new NotificationButton(userId, overlayManager);
        
        // Ajouter au header (à droite)
        headerBox.getChildren().add(notificationButton);
        HBox.setMargin(notificationButton, new Insets(0, 10, 0, 0));
    }
}
```

### Étape 2: Ajouter dans le FXML (Optionnel)

Si vous préférez définir dans le FXML:

```xml
<HBox fx:id="headerBox" alignment="CENTER_LEFT" spacing="15">
    <Label text="Mon Application" style="-fx-font-size: 20px;"/>
    
    <Region HBox.hgrow="ALWAYS"/>
    
    <!-- Bouton de notification sera ajouté ici programmatiquement -->
    
    <Button text="Profil"/>
    <Button text="Déconnexion"/>
</HBox>
```

## 🎨 Exemple Visuel

```
┌─────────────────────────────────────────────┐
│  Mon App          🔔(3)    Profil  Logout   │
│                    ↑                         │
│              Badge rouge avec "3"            │
└─────────────────────────────────────────────┘
```

Quand on clique sur 🔔:

```
┌──────────────────────────────────────┐
│ 🔔 Centre de Notifications      ✓ ✕ │
├──────────────────────────────────────┤
│                                      │
│  📄 📁 Forum Crypto                  │
│     Il y a 2 heures                  │
│                                      │
│     Bitcoin atteint 50k$ !           │
│     👤 JohnDoe  💬 5 commentaires    │
│                                      │
├──────────────────────────────────────┤
│  📄 📁 Forum Trading                 │
│     Il y a 5 heures                  │
│                                      │
│     Stratégie gagnante !             │
│     👤 Alice  💬 12 commentaires     │
│                                      │
└──────────────────────────────────────┘
```

## 🎭 Animations

### Au survol du bouton
- Rotation de l'icône (±15°)
- Changement de couleur du fond

### Nouvelle notification
- **Badge**: Scale animation (0.5 → 1.2 → 1.0)
- **Icône**: Shake animation (gauche-droite)

### Ouverture du centre
- Slide + fade in (comme les autres overlays)

## 📊 Requête SQL

Le bouton compte les notifications avec:

```sql
SELECT COUNT(*) as count
FROM posts p
INNER JOIN user_forum uf ON p.forum_id = uf.forum_id
WHERE uf.user_id = ?           -- Utilisateur connecté
AND p.author_id != ?           -- Pas ses propres posts
AND p.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)  -- 7 derniers jours
```

## 🎨 Personnalisation

### Changer la couleur du badge

Dans `NotificationButton.java`, ligne 42:

```java
badge.setFill(Color.web("#4CAF50"));  // Vert au lieu de rouge
```

### Changer l'intervalle de mise à jour

Dans `NotificationButton.java`, ligne 195:

```java
Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
    updateUnreadCount();
}));
```

### Changer la période des notifications (7 jours)

Dans `NotificationCenterController.java`, ligne 91:

```java
"AND p.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +  // 30 jours
```

### Changer le nombre max de notifications

Dans `NotificationCenterController.java`, ligne 94:

```java
"LIMIT 100";  // 100 au lieu de 50
```

## 🔗 Intégration Complète

### MainController.java complet

```java
public class MainController {
    @FXML private StackPane rootContainer;
    @FXML private BorderPane mainContent;
    @FXML private HBox headerBox;
    @FXML private VBox notificationContainer;  // Pour les toasts
    
    private OverlayManager overlayManager;
    private NotificationManager notificationManager;
    private NotificationButton notificationButton;
    private int currentUserId;
    
    @FXML
    public void initialize() {
        overlayManager = new OverlayManager(rootContainer, mainContent);
    }
    
    public void onUserLoggedIn(int userId) {
        this.currentUserId = userId;
        
        // 1. Bouton de notification dans le header
        notificationButton = new NotificationButton(userId, overlayManager);
        headerBox.getChildren().add(notificationButton);
        
        // 2. Notifications toast en temps réel
        notificationManager = new NotificationManager(notificationContainer, userId);
        notificationManager.startMonitoring();
    }
    
    public void onUserLogout() {
        if (notificationManager != null) {
            notificationManager.stopMonitoring();
        }
        
        if (notificationButton != null) {
            headerBox.getChildren().remove(notificationButton);
        }
    }
}
```

## 🎯 Différence entre les 2 systèmes

### NotificationManager (Toast)
- Affiche les **nouvelles** notifications en temps réel
- Coin supérieur droit
- Auto-disparition après 10 secondes
- Vérification toutes les 5 secondes

### NotificationButton + Center
- Affiche **toutes** les notifications (7 jours)
- Bouton cliquable dans le header
- Badge avec compteur
- Centre de notifications complet

## 💡 Recommandation

Utilisez les **deux systèmes ensemble**:

1. **NotificationManager** pour alerter en temps réel
2. **NotificationButton** pour consulter l'historique

Quand une nouvelle notification arrive:
- Toast apparaît en haut à droite ✅
- Badge du bouton s'incrémente ✅
- Animation du bouton ✅

## 🐛 Troubleshooting

### Le badge ne s'affiche pas
- Vérifier que `currentUserId` est correct
- Vérifier la connexion à la base de données
- Vérifier qu'il y a des posts dans les forums rejoints

### Le bouton ne s'ajoute pas au header
- Vérifier que `headerBox` est bien injecté avec `@FXML`
- Vérifier que `onUserLoggedIn()` est appelé après la connexion

### Les animations ne fonctionnent pas
- Vérifier que JavaFX est bien configuré
- Vérifier qu'il n'y a pas d'erreurs dans la console

## 📝 TODO Optionnel

1. Ajouter un son au clic sur le bouton
2. Ajouter des filtres dans le centre (par forum, par date)
3. Ajouter la recherche dans les notifications
4. Ajouter un système de "marquer comme lu"
5. Ajouter des notifications pour:
   - Nouveaux commentaires sur vos posts
   - Réponses à vos commentaires
   - Mentions (@username)

## 🎉 Résultat Final

Vous aurez:
- 🔔 Bouton avec badge dans le header
- 📋 Centre de notifications complet
- 🎨 Animations fluides
- ⚡ Mise à jour en temps réel
- 🎯 Navigation vers les posts
