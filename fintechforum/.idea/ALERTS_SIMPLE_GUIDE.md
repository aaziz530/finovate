# Guide Simple - Page des Alertes

## 🎯 Concept Simple

Une page qui affiche toutes les alertes quand un nouveau post est créé dans un forum que tu as rejoint.

## 📁 Fichiers Créés

1. `AlertsController.java` - Contrôleur de la page
2. `alerts-view.fxml` - Interface de la page

## 🔧 Intégration dans MainController

### Étape 1: Ajouter une icône dans le header

Dans ton fichier FXML principal (main-view.fxml), ajoute un bouton dans le header:

```xml
<HBox alignment="CENTER_LEFT" spacing="15">
    <Label text="Mon Application" style="-fx-font-size: 20px;"/>
    
    <Region HBox.hgrow="ALWAYS"/>
    
    <!-- NOUVEAU: Bouton Alertes -->
    <Button text="🔔" 
            onAction="#showAlerts"
            style="-fx-font-size: 24px; -fx-background-color: transparent; -fx-cursor: hand;"/>
    
    <Button text="Profil"/>
    <Button text="Déconnexion"/>
</HBox>
```

### Étape 2: Ajouter la méthode dans MainController

```java
public class MainController {
    
    @FXML
    private void showAlerts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/alerts-view.fxml"));
            Parent alertsView = loader.load();
            
            AlertsController controller = loader.getController();
            controller.setMainController(this);
            controller.loadAlerts(currentUserId);
            
            // Changer la vue centrale
            centerPane.setCenter(alertsView);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

### Étape 3: Ajouter la navigation vers les posts

Dans MainController, ajoute cette méthode:

```java
public void showPostDetails(int postId, int userId) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/post-details.fxml"));
        Parent postView = loader.load();
        
        PostDetailsController controller = loader.getController();
        controller.setMainController(this);
        controller.loadPostDetails(postId, userId);
        
        centerPane.setCenter(postView);
        
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

## 🎨 Exemple Visuel

### Header avec icône:
```
┌────────────────────────────────────┐
│  Mon App      🔔    Profil  Logout │
│                ↑                   │
│         Clic ici pour voir alertes │
└────────────────────────────────────┘
```

### Page des alertes:
```
┌─────────────────────────────────────────┐
│ 🔔 Mes Alertes          🗑️ Tout effacer │
├─────────────────────────────────────────┤
│                                         │
│  🔔 📁 Forum Crypto                     │
│     Il y a 2 heures                     │
│                                         │
│     Nouveau post : Bitcoin à 50k$ !    │
│     Par : JohnDoe                       │
│                                         │
│     [👁️ Voir le post]  [🗑️]            │
│                                         │
├─────────────────────────────────────────┤
│  🔔 📁 Forum Trading                    │
│     Il y a 5 heures                     │
│                                         │
│     Nouveau post : Stratégie gagnante  │
│     Par : Alice                         │
│                                         │
│     [👁️ Voir le post]  [🗑️]            │
│                                         │
└─────────────────────────────────────────┘
```

## 📊 Comment ça marche

1. **Détection automatique**
   - Quand un nouveau post est créé dans un forum que tu as rejoint
   - Il apparaît dans la page des alertes

2. **Affichage**
   - Fond bleu clair pour les alertes non lues
   - Fond blanc pour les alertes lues
   - Temps écoulé ("Il y a 2 heures")

3. **Actions**
   - Clic sur "Voir le post" → Ouvre le post
   - Clic sur 🗑️ → Supprime l'alerte
   - Clic sur "Tout effacer" → Supprime toutes les alertes

## 🔍 Requête SQL

Les alertes sont récupérées avec:

```sql
SELECT p.id, p.title, p.created_at, u.username, f.name as forum_name
FROM posts p
INNER JOIN forums f ON p.forum_id = f.id
INNER JOIN users u ON p.author_id = u.id
INNER JOIN user_forum uf ON f.id = uf.forum_id
WHERE uf.user_id = ?              -- Tes forums rejoints
AND p.author_id != ?              -- Pas tes propres posts
AND p.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)  -- 7 derniers jours
ORDER BY p.created_at DESC
LIMIT 50
```

## 🎨 Personnalisation

### Changer la période (7 jours)

Dans `AlertsController.java`, ligne 68:

```java
"AND p.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +  // 30 jours
```

### Changer le nombre max d'alertes

Dans `AlertsController.java`, ligne 71:

```java
"LIMIT 100";  // 100 au lieu de 50
```

### Changer les couleurs

Dans `AlertsController.java`, ligne 149-156:

```java
// Alerte non lue
card.setStyle(
    "-fx-background-color: #E8F5E9;" +  // Vert clair
    "-fx-border-color: #4CAF50;" +      // Vert
    // ...
);
```

## ✅ C'est tout!

Maintenant tu as:
- ✅ Une icône 🔔 dans le header
- ✅ Une page qui liste toutes les alertes
- ✅ Détection automatique des nouveaux posts
- ✅ Navigation vers les posts
- ✅ Suppression des alertes

Simple et efficace! 🎉
