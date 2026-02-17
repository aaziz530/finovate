# Guide d'Intégration - Navigation par Overlays (Style Reddit/Facebook)

## 🎯 Concept

Au lieu de changer de page, tout s'ouvre dans des overlays/modals superposés avec animations fluides.

## 📦 Composants Créés

### 1. `OverlayManager.java`
Gestionnaire principal des overlays avec animations

### 2. `PostOverlayController.java`
Affiche un post complet avec commentaires dans un overlay

### 3. `CreatePostOverlayController.java`
Formulaire de création de post dans un overlay

## 🔧 Intégration dans MainController

### Étape 1: Ajouter l'OverlayManager

```java
public class MainController {
    @FXML private StackPane rootContainer;  // Conteneur principal
    @FXML private BorderPane mainContent;   // Contenu principal
    
    private OverlayManager overlayManager;
    
    @FXML
    public void initialize() {
        // Initialiser l'overlay manager
        overlayManager = new OverlayManager(rootContainer, mainContent);
    }
}
```

### Étape 2: Modifier la structure FXML

Votre fichier FXML principal doit avoir cette structure:

```xml
<StackPane fx:id="rootContainer" xmlns:fx="http://javafx.com/fxml">
    <BorderPane fx:id="mainContent">
        <!-- Votre contenu actuel ici -->
        <top>
            <!-- Header -->
        </top>
        <center>
            <!-- Contenu central -->
        </center>
    </BorderPane>
</StackPane>
```

### Étape 3: Ouvrir un Post dans un Overlay

Au lieu de changer de page, utilisez:

```java
// Dans ForumsController ou PostsController
private void openPost(int postId) {
    PostOverlayController postOverlay = new PostOverlayController(
        overlayManager,
        postId,
        currentUserId
    );
    
    overlayManager.openOverlay(
        postOverlay.getView(),
        OverlayManager.OverlayType.FULL_POST
    );
}
```

### Étape 4: Créer un Post dans un Overlay

```java
private void openCreatePostDialog() {
    CreatePostOverlayController createOverlay = new CreatePostOverlayController(
        overlayManager,
        forumId,
        currentUserId,
        () -> refreshPosts()  // Callback après création
    );
    
    overlayManager.openOverlay(
        createOverlay.getView(),
        OverlayManager.OverlayType.LARGE
    );
}
```

## 🎨 Modifications dans les Contrôleurs Existants

### ForumsController.java

Remplacer:
```java
private void openForum(ForumItem forum) {
    mainController.showPostsView(forum.getId(), forum.getName());
}
```

Par:
```java
private void openForum(ForumItem forum) {
    // Ouvrir la liste des posts dans un overlay
    PostsListOverlayController postsOverlay = new PostsListOverlayController(
        overlayManager,
        forum.getId(),
        forum.getName(),
        currentUserId
    );
    
    overlayManager.openOverlay(
        postsOverlay.getView(),
        OverlayManager.OverlayType.XLARGE
    );
}
```

### Dans les ListCell

Remplacer les double-clics par des clics simples sur des boutons:

```java
Button openBtn = new Button("👁️ Voir");
openBtn.setOnAction(e -> openPost(post.getId()));
```

## 🎭 Types d'Overlays Disponibles

```java
OverlayType.SMALL      // 400x300  - Petites confirmations
OverlayType.MEDIUM     // 600x500  - Formulaires simples
OverlayType.LARGE      // 800x600  - Formulaires complexes
OverlayType.XLARGE     // 1000x700 - Listes, tableaux
OverlayType.FULL_POST  // 900x800  - Affichage de post complet
```

## ⚡ Fonctionnalités

### ✅ Animations Fluides
- Fade in/out du backdrop
- Scale animation de l'overlay
- Blur du contenu en arrière-plan

### ✅ Gestion de la Pile
- Plusieurs overlays peuvent être empilés
- Fermeture par ordre LIFO (Last In First Out)
- Clic sur le backdrop pour fermer

### ✅ Navigation Intuitive
- Bouton ✕ pour fermer
- ESC pour fermer (à implémenter)
- Pas de changement de page

## 🚀 Exemple Complet d'Utilisation

```java
public class PostsController {
    private OverlayManager overlayManager;
    
    public void setOverlayManager(OverlayManager manager) {
        this.overlayManager = manager;
    }
    
    // Ouvrir un post
    private void viewPost(int postId) {
        PostOverlayController overlay = new PostOverlayController(
            overlayManager, postId, currentUserId
        );
        overlayManager.openOverlay(
            overlay.getView(),
            OverlayManager.OverlayType.FULL_POST
        );
    }
    
    // Créer un post
    @FXML
    private void createNewPost() {
        CreatePostOverlayController overlay = new CreatePostOverlayController(
            overlayManager,
            currentForumId,
            currentUserId,
            this::refreshPostsList
        );
        overlayManager.openOverlay(
            overlay.getView(),
            OverlayManager.OverlayType.LARGE
        );
    }
}
```

## 🎯 Avantages

1. **Pas de perte de contexte** - L'utilisateur reste sur la même page
2. **Navigation fluide** - Animations modernes et professionnelles
3. **Empilable** - Plusieurs overlays peuvent coexister
4. **Performant** - Pas de rechargement de page
5. **UX moderne** - Comme Reddit, Facebook, Twitter

## 📝 TODO pour Compléter l'Intégration

1. Créer `PostsListOverlayController` pour afficher la liste des posts d'un forum
2. Ajouter support ESC pour fermer les overlays
3. Ajouter des overlays pour:
   - Édition de post
   - Édition de forum
   - Confirmation de suppression
4. Implémenter le système de votes dans les overlays
5. Ajouter des transitions entre overlays

## 🐛 Gestion des Erreurs

L'OverlayManager gère automatiquement:
- Fermeture de tous les overlays
- Nettoyage du blur
- Gestion de la pile d'overlays

Pour fermer tous les overlays:
```java
overlayManager.closeAllOverlays();
```

## 🎨 Personnalisation

Vous pouvez personnaliser les animations dans `OverlayManager.java`:
- Durée des animations
- Type d'interpolation
- Intensité du blur
- Couleur du backdrop
