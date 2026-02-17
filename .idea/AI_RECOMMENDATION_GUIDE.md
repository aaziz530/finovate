# 🤖 Guide du Système de Recommandation AI

## Vue d'ensemble
Le système de recommandation AI suggère intelligemment des forums aux utilisateurs basé sur leur comportement et leurs interactions.

## Algorithme de Recommandation

### 1. Types d'Interactions Trackées
- **VIEW** (poids: 1.0) - Voir un forum
- **CLICK** (poids: 2.0) - Cliquer sur un forum
- **LIKE** (poids: 3.0) - Liker un post
- **SHARE** (poids: 5.0) - Partager un post
- **COMMENT** (poids: 7.0) - Commenter dans un forum
- **POST** (poids: 10.0) - Créer un post dans un forum

### 2. Facteurs de Scoring
L'algorithme calcule un score basé sur:

1. **Interactions Directes** - Vos propres interactions avec les forums
2. **Filtrage Collaboratif** - "Les utilisateurs qui aiment X aiment aussi Y"
3. **Popularité** - Nombre de membres (score logarithmique)
4. **Activité Récente** - Posts des 7 derniers jours

### 3. Exclusions
- Forums déjà rejoints (exclus automatiquement)
- Forums avec score < 1 (non pertinents)
- Maximum 20 recommandations affichées

## Installation

### Étape 1: Exécuter le Script SQL
```sql
-- Copier et exécuter le contenu de:
src/main/resources/sql/ai_recommendation_tables.sql
```

Ce script crée 3 tables:
- `user_interactions` - Historique des interactions
- `forum_recommendations` - Scores calculés
- `user_preferences` - Préférences utilisateur (future)

### Étape 2: Tester le Système
1. Cliquez sur plusieurs forums différents
2. Créez des posts, commentaires
3. Cliquez sur "🤖 Recommandations" dans le header
4. Les recommandations se mettent à jour automatiquement

## Tracking Automatique

### Actuellement Implémenté
- ✅ **CLICK** - Quand vous ouvrez un forum (ForumsController.openForum)

### À Implémenter (Optionnel)
Pour améliorer les recommandations, ajoutez le tracking dans:

```java
// Dans PostsController - quand on voit les posts
RecommendationEngine.trackInteraction(userId, forumId, InteractionType.VIEW);

// Dans PostDetailsController - quand on crée un post
RecommendationEngine.trackInteraction(userId, forumId, InteractionType.POST);

// Dans PostDetailsController - quand on commente
RecommendationEngine.trackInteraction(userId, forumId, InteractionType.COMMENT);

// Dans PostDetailsController - quand on like
RecommendationEngine.trackInteraction(userId, forumId, InteractionType.LIKE);
```

## Interface Utilisateur

### Bouton dans le Header
- **Icône**: 🤖 Recommandations
- **Couleur**: Vert (rgba(76, 175, 80, 0.3))
- **Position**: Entre les onglets et le bouton Alertes

### Page de Recommandations
Chaque recommandation affiche:
- Badge "🤖 AI" et score
- Nom et description du forum
- Raison de la recommandation (💡)
- Statistiques (membres, posts récents)
- Boutons: "👁️ Voir le forum" et "➕ Rejoindre"

## Recalcul Automatique
Les recommandations sont recalculées automatiquement:
- Après chaque interaction trackée
- Quand vous cliquez sur "🔄 Actualiser"
- Quand vous rejoignez un nouveau forum

## Base de Données

### Configuration
```java
DB_URL = "jdbc:mysql://localhost:3306/fintechforum"
DB_USER = "root"
DB_PASSWORD = ""
```

### Tables Créées
1. **user_interactions**
   - Stocke chaque interaction avec son type et compteur
   - Clé unique: (user_id, forum_id, interaction_type)

2. **forum_recommendations**
   - Stocke les scores calculés pour chaque utilisateur
   - Clé unique: (user_id, forum_id)

3. **user_preferences**
   - Pour futures améliorations (tags, catégories)

## Exemple de Flux

1. **Utilisateur clique sur "Forum Crypto"**
   → `trackInteraction(1, 5, CLICK)`
   → Score +2 pour Forum Crypto

2. **Utilisateur crée un post dans "Forum Crypto"**
   → `trackInteraction(1, 5, POST)`
   → Score +10 pour Forum Crypto

3. **Système calcule les recommandations**
   → Trouve que d'autres utilisateurs qui aiment "Forum Crypto" aiment aussi "Forum Blockchain"
   → Recommande "Forum Blockchain" avec raison: "Utilisateurs similaires aiment ce forum"

4. **Utilisateur voit les recommandations**
   → "Forum Blockchain" apparaît avec score élevé
   → Peut rejoindre directement ou voir le forum

## Personnalisation

### Ajuster les Poids
Dans `RecommendationEngine.getInteractionWeight()`:
```java
case "post": return 10.0;      // Très engagé
case "comment": return 7.0;    // Engagé
case "like": return 3.0;       // Intéressé
case "share": return 5.0;      // Très intéressé
case "click": return 2.0;      // Curieux
case "view": return 1.0;       // Passif
```

### Ajuster le Nombre de Recommandations
Dans `RecommendationsController.loadRecommendationsFromEngine()`:
```java
List<RecommendedForum> recommendations = RecommendationEngine.getRecommendations(currentUserId, 10);
// Changer 10 à 20, 30, etc.
```

## Dépannage

### Aucune recommandation affichée
- Vérifiez que les tables SQL sont créées
- Interagissez avec plusieurs forums différents
- Vérifiez qu'il existe des forums non rejoints

### Erreurs SQL
- Vérifiez la connexion MySQL (localhost:3306)
- Vérifiez que l'utilisateur root a les permissions
- Vérifiez que la base `fintechforum` existe

### Recommandations non pertinentes
- Augmentez le nombre d'interactions
- Ajustez les poids dans `getInteractionWeight()`
- Ajoutez plus de types de tracking (VIEW, POST, COMMENT)

## Améliorations Futures

1. **Tags et Catégories** - Utiliser `user_preferences` pour les intérêts
2. **Machine Learning** - Modèle prédictif plus avancé
3. **Tendances** - Recommander les forums en tendance
4. **Diversité** - Éviter de recommander uniquement des forums similaires
5. **Feedback** - Permettre aux utilisateurs de dire "pas intéressé"
