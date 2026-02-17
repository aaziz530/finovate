# Guide du Système de Badges - Gamification

## 🎯 Vue d'Ensemble

Le système de badges gamifie l'expérience utilisateur en récompensant les actions et l'engagement dans le forum.

## 🏆 Types de Badges Disponibles

### Badges de Vote (Engagement)
- **⭐ Fan du Forum** - Votez sur 5 posts dans le même forum
- **🌟 Super Fan** - Votez sur 10 posts dans le même forum
- **💫 Mega Fan** - Votez sur 25 posts dans le même forum
- **👍 Voteur Actif** - Votez sur 50 posts au total (tous forums)

### Badges de Création de Contenu
- **📝 Premier Post** - Créez votre premier post
- **✍️ Auteur Régulier** - Créez 10 posts
- **📚 Auteur Prolifique** - Créez 50 posts

### Badges d'Interaction
- **💬 Commentateur** - Postez 10 commentaires
- **🗨️ Conversateur** - Postez 50 commentaires

### Badges de Popularité
- **🔥 Post Populaire** - Recevez 10 upvotes sur un post
- **🚀 Post Viral** - Recevez 50 upvotes sur un post

### Badges de Communauté
- **👥 Membre Actif** - Rejoignez 5 forums
- **🗺️ Explorateur** - Rejoignez 10 forums

### Badges de Partage
- **📤 Partageur** - Partagez 5 posts
- **📢 Influenceur** - Partagez 20 posts

## 📊 Structure de la Base de Données

### Table `badge_types`
Contient tous les types de badges disponibles avec leurs critères.

### Table `user_badges`
Stocke les badges gagnés par chaque utilisateur.

### Table `badge_progress`
Suit la progression des utilisateurs vers l'obtention des badges.

## 🔧 Installation

### 1. Exécuter le Script SQL

```sql
-- Exécutez le fichier BADGE_SYSTEM_SQL.sql
source .idea/BADGE_SYSTEM_SQL.sql;
```

### 2. Vérifier les Tables

```sql
-- Vérifier que les tables sont créées
SHOW TABLES LIKE 'badge%';

-- Vérifier les types de badges
SELECT * FROM badge_types;
```

## 💻 Utilisation dans le Code

### Vérifier les Badges après un Vote

```java
import org.example.badge.BadgeManager;

// Dans votre méthode de vote
private void votePost(int postId, String voteType) {
    // ... votre code de vote ...
    
    // Vérifier les badges
    BadgeManager.checkVoteBadges(currentUserId, currentForumId);
}
```

### Vérifier les Badges après Création de Post

```java
// Dans votre méthode de création de post
private void createPost() {
    // ... votre code de création ...
    
    // Vérifier les badges
    BadgeManager.checkPostBadges(currentUserId);
}
```

### Vérifier les Badges après Commentaire

```java
// Dans votre méthode de création de commentaire
private void addComment() {
    // ... votre code de commentaire ...
    
    // Vérifier les badges
    BadgeManager.checkCommentBadges(currentUserId);
}
```

### Vérifier les Badges après Partage

```java
// Dans votre méthode de partage
private void sharePost(int postId) {
    // ... votre code de partage ...
    
    // Vérifier les badges
    BadgeManager.checkShareBadges(currentUserId);
}
```

### Récupérer les Badges d'un Utilisateur

```java
import org.example.badge.BadgeManager;
import org.example.badge.BadgeManager.Badge;
import java.util.List;

// Récupérer tous les badges
List<Badge> badges = BadgeManager.getUserBadges(userId);

// Compter les badges
int badgeCount = BadgeManager.getUserBadgeCount(userId);

// Afficher les badges
for (Badge badge : badges) {
    System.out.println(badge.getIcon() + " " + badge.getName());
    System.out.println("Description: " + badge.getDescription());
    if (badge.getForumName() != null) {
        System.out.println("Forum: " + badge.getForumName());
    }
}
```

## 🎨 Notification de Badge

Quand un utilisateur gagne un badge, une fenêtre popup s'affiche automatiquement avec :
- 🎉 Titre "Nouveau Badge Gagné !"
- Grande icône du badge (72px)
- Nom du badge
- Description
- Nom du forum (si applicable)
- Fond dégradé doré

### Personnaliser la Notification

Modifiez la méthode `showBadgeNotification()` dans `BadgeManager.java` :

```java
private static void showBadgeNotification(int userId, String badgeName, Integer forumId) {
    // Personnalisez le style, les couleurs, les animations, etc.
}
```

## 📈 Suivi de Progression

### Voir la Progression vers les Badges

```sql
-- Voir la progression d'un utilisateur
SELECT * FROM badge_progress_view WHERE user_id = 1;

-- Voir les badges prêts à être gagnés
SELECT * FROM badge_progress_view 
WHERE user_id = 1 AND status = 'READY';
```

## 🎮 Intégrations Recommandées

### 1. Afficher les Badges dans le Profil Utilisateur

Créez une section "Mes Badges" dans le profil :

```java
public void loadUserProfile(int userId) {
    // ... autres infos du profil ...
    
    // Charger les badges
    List<Badge> badges = BadgeManager.getUserBadges(userId);
    int badgeCount = BadgeManager.getUserBadgeCount(userId);
    
    badgeCountLabel.setText(badgeCount + " badges");
    
    // Afficher les badges dans une grille
    for (Badge badge : badges) {
        Label badgeLabel = new Label(badge.getIcon());
        badgeLabel.setTooltip(new Tooltip(badge.getName() + "\n" + badge.getDescription()));
        badgesContainer.getChildren().add(badgeLabel);
    }
}
```

### 2. Afficher le Badge le Plus Récent dans le Header

```java
public void updateHeader(int userId) {
    List<Badge> badges = BadgeManager.getUserBadges(userId);
    
    if (!badges.isEmpty()) {
        Badge latestBadge = badges.get(0); // Le plus récent
        latestBadgeLabel.setText(latestBadge.getIcon() + " " + latestBadge.getName());
    }
}
```

### 3. Leaderboard des Badges

```sql
-- Top 10 utilisateurs avec le plus de badges
SELECT 
    u.username,
    COUNT(ub.id) as badge_count
FROM users u
LEFT JOIN user_badges ub ON u.id = ub.user_id
GROUP BY u.id
ORDER BY badge_count DESC
LIMIT 10;
```

### 4. Badges Spéciaux par Forum

```sql
-- Voir les fans d'un forum spécifique
SELECT 
    u.username,
    bt.name as badge_name,
    bt.icon
FROM user_badges ub
INNER JOIN users u ON ub.user_id = u.id
INNER JOIN badge_types bt ON ub.badge_type_id = bt.id
WHERE ub.forum_id = 1
ORDER BY ub.earned_at DESC;
```

## 🔮 Fonctionnalités Futures

### Badges Avancés à Ajouter

1. **Badges Temporels**
   - "Lève-tôt" - Premier post de la journée
   - "Noctambule" - Post après minuit
   - "Régulier" - Connexion 7 jours consécutifs

2. **Badges Sociaux**
   - "Mentor" - Aidez 10 nouveaux utilisateurs
   - "Populaire" - 100 followers
   - "Influent" - Vos posts ont 1000+ vues

3. **Badges de Qualité**
   - "Expert" - 10 posts avec 20+ upvotes
   - "Contributeur de Qualité" - Ratio upvotes/downvotes > 10

4. **Badges Secrets**
   - Badges cachés à découvrir
   - Conditions spéciales

### Système de Niveaux

Ajoutez un système de niveaux basé sur les badges :

```sql
-- Calculer le niveau d'un utilisateur
SELECT 
    user_id,
    COUNT(*) as badge_count,
    CASE 
        WHEN COUNT(*) >= 20 THEN 'Légende'
        WHEN COUNT(*) >= 15 THEN 'Expert'
        WHEN COUNT(*) >= 10 THEN 'Avancé'
        WHEN COUNT(*) >= 5 THEN 'Intermédiaire'
        ELSE 'Débutant'
    END as level
FROM user_badges
GROUP BY user_id;
```

## 🎯 Exemple Complet : Badge "Fan du Forum"

### Scénario
1. Utilisateur vote sur le 1er post du forum → Progression 1/5
2. Utilisateur vote sur le 2ème post du forum → Progression 2/5
3. Utilisateur vote sur le 3ème post du forum → Progression 3/5
4. Utilisateur vote sur le 4ème post du forum → Progression 4/5
5. Utilisateur vote sur le 5ème post du forum → 🎉 Badge gagné !

### Code Complet

```java
// Dans PostsController.java
private void votePost(int postId, String voteType) {
    // ... code de vote ...
    
    if (isNewVote) {
        // Vérifier les badges
        BadgeManager.checkVoteBadges(currentUserId, currentForumId);
        
        // La notification s'affiche automatiquement si badge gagné
    }
}
```

## 📝 Notes Importantes

1. **Performance** - Les vérifications de badges sont optimisées avec des requêtes SQL efficaces
2. **Thread Safety** - Les notifications utilisent `Platform.runLater()` pour la sécurité des threads
3. **Unicité** - Un utilisateur ne peut gagner le même badge qu'une seule fois par forum
4. **Extensibilité** - Facile d'ajouter de nouveaux types de badges

## 🐛 Dépannage

### Le badge ne s'affiche pas
- Vérifiez que les tables sont créées
- Vérifiez que les types de badges sont insérés
- Vérifiez les logs pour les erreurs SQL

### Notification ne s'affiche pas
- Vérifiez que JavaFX Application Thread est utilisé
- Vérifiez les permissions de la base de données

### Badge attribué plusieurs fois
- Vérifiez la contrainte UNIQUE dans `user_badges`
- Vérifiez la méthode `userHasBadge()`

## 🎊 Félicitations !

Votre système de badges est maintenant opérationnel ! Les utilisateurs seront récompensés pour leur engagement et leur participation active dans le forum.
