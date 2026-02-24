# Test du Système de Badges

## 🚀 Étapes d'Installation et de Test

### Étape 1 : Créer les Tables

Exécutez le script SQL dans votre base de données MySQL :

```bash
# Ouvrez MySQL
mysql -u root -p fintechforum

# Exécutez le script
source C:/Users/MSI/IdeaProjects/fintechforum/.idea/BADGE_SYSTEM_SQL.sql
```

Ou copiez-collez le contenu du fichier `BADGE_SYSTEM_SQL.sql` dans votre client MySQL.

### Étape 2 : Vérifier les Tables

```sql
-- Vérifier que les tables sont créées
SHOW TABLES LIKE 'badge%';

-- Devrait afficher :
-- badge_progress
-- badge_types
-- user_badges

-- Vérifier les types de badges
SELECT * FROM badge_types;

-- Devrait afficher 14 badges
```

### Étape 3 : Compiler et Lancer l'Application

```bash
# Dans le terminal
mvn clean compile
mvn javafx:run
```

### Étape 4 : Tester le Badge "Fan du Forum"

1. **Connectez-vous** avec un utilisateur
2. **Ouvrez un forum** (n'importe lequel)
3. **Votez sur 5 posts différents** dans ce forum
   - Cliquez sur 👍 ou 👎 sur 5 posts
4. **Au 5ème vote**, une fenêtre popup devrait apparaître :
   ```
   🎉 Nouveau Badge Gagné !
   
   ⭐
   Fan du Forum
   Votez sur 5 posts dans le même forum
   Forum: [Nom du Forum]
   ```

### Étape 5 : Tester d'Autres Badges

#### Badge "Super Fan" (10 votes)
- Continuez à voter dans le même forum
- Au 10ème vote → Badge "Super Fan" 🌟

#### Badge "Partageur" (5 partages)
- Partagez 5 posts différents
- Au 5ème partage → Badge "Partageur" 📤

#### Badge "Premier Post"
- Créez votre premier post
- Immédiatement → Badge "Premier Post" 📝

## 🔍 Vérifications SQL

### Voir les Badges d'un Utilisateur

```sql
-- Remplacez 1 par l'ID de votre utilisateur
SELECT * FROM user_badges_view WHERE user_id = 1;
```

### Voir la Progression

```sql
-- Voir combien de votes dans chaque forum
SELECT 
    p.forum_id,
    f.name as forum_name,
    COUNT(DISTINCT v.post_id) as vote_count
FROM votes v
INNER JOIN posts p ON v.post_id = p.id
INNER JOIN forums f ON p.forum_id = f.id
WHERE v.user_id = 1
GROUP BY p.forum_id;
```

### Compter les Badges

```sql
-- Nombre total de badges gagnés
SELECT COUNT(*) as total_badges FROM user_badges WHERE user_id = 1;

-- Badges par catégorie
SELECT 
    bt.category,
    COUNT(*) as badge_count
FROM user_badges ub
INNER JOIN badge_types bt ON ub.badge_type_id = bt.id
WHERE ub.user_id = 1
GROUP BY bt.category;
```

## 🎯 Scénarios de Test

### Scénario 1 : Badge "Fan du Forum"

**Objectif** : Gagner le badge en votant 5 fois dans le même forum

**Étapes** :
1. Ouvrir le forum "Crypto Trading"
2. Voter sur le post 1 → Aucun badge
3. Voter sur le post 2 → Aucun badge
4. Voter sur le post 3 → Aucun badge
5. Voter sur le post 4 → Aucun badge
6. Voter sur le post 5 → 🎉 Badge "Fan du Forum" !

**Vérification SQL** :
```sql
SELECT * FROM user_badges WHERE user_id = 1 AND badge_type_id = 1;
```

### Scénario 2 : Badge "Super Fan"

**Objectif** : Gagner le badge en votant 10 fois dans le même forum

**Étapes** :
1. Continuer dans le même forum
2. Voter sur 5 posts supplémentaires
3. Au 10ème vote → 🎉 Badge "Super Fan" !

### Scénario 3 : Badges Multiples

**Objectif** : Gagner plusieurs badges en une session

**Étapes** :
1. Voter sur 5 posts dans Forum A → Badge "Fan du Forum" (Forum A)
2. Voter sur 5 posts dans Forum B → Badge "Fan du Forum" (Forum B)
3. Créer 1 post → Badge "Premier Post"
4. Partager 5 posts → Badge "Partageur"

**Résultat** : 4 badges gagnés !

## 🐛 Problèmes Courants

### Problème 1 : Badge ne s'affiche pas

**Causes possibles** :
- Tables non créées
- Types de badges non insérés
- Erreur SQL

**Solution** :
```sql
-- Vérifier les tables
SHOW TABLES LIKE 'badge%';

-- Vérifier les types de badges
SELECT COUNT(*) FROM badge_types;
-- Devrait retourner 14

-- Vérifier les erreurs
SHOW WARNINGS;
```

### Problème 2 : Notification ne s'affiche pas

**Causes possibles** :
- JavaFX Thread issue
- Erreur dans BadgeManager

**Solution** :
- Vérifier les logs de la console
- Vérifier que `Platform.runLater()` est utilisé

### Problème 3 : Badge attribué plusieurs fois

**Causes possibles** :
- Contrainte UNIQUE manquante

**Solution** :
```sql
-- Vérifier la contrainte
SHOW CREATE TABLE user_badges;

-- Devrait contenir :
-- UNIQUE KEY unique_user_badge (user_id, badge_type_id, forum_id)
```

## 📊 Statistiques de Test

### Après les Tests, Vérifiez :

```sql
-- Nombre total de badges attribués
SELECT COUNT(*) as total_badges_awarded FROM user_badges;

-- Badge le plus populaire
SELECT 
    bt.name,
    bt.icon,
    COUNT(*) as times_awarded
FROM user_badges ub
INNER JOIN badge_types bt ON ub.badge_type_id = bt.id
GROUP BY bt.id
ORDER BY times_awarded DESC;

-- Utilisateur avec le plus de badges
SELECT 
    u.username,
    COUNT(ub.id) as badge_count
FROM users u
LEFT JOIN user_badges ub ON u.id = ub.user_id
GROUP BY u.id
ORDER BY badge_count DESC
LIMIT 5;
```

## ✅ Checklist de Test

- [ ] Tables créées (badge_types, user_badges, badge_progress)
- [ ] 14 types de badges insérés
- [ ] Badge "Fan du Forum" fonctionne (5 votes)
- [ ] Badge "Super Fan" fonctionne (10 votes)
- [ ] Badge "Mega Fan" fonctionne (25 votes)
- [ ] Badge "Partageur" fonctionne (5 partages)
- [ ] Badge "Premier Post" fonctionne (1 post)
- [ ] Notification popup s'affiche correctement
- [ ] Pas de badges en double
- [ ] Badges spécifiques au forum fonctionnent
- [ ] Badges globaux fonctionnent

## 🎉 Résultat Attendu

Après avoir voté 5 fois dans un forum, vous devriez voir cette fenêtre :

```
┌─────────────────────────────────────┐
│   🎉 Nouveau Badge Gagné !          │
├─────────────────────────────────────┤
│                                     │
│              ⭐                      │
│                                     │
│         Fan du Forum                │
│                                     │
│  Votez sur 5 posts dans le même    │
│           forum                     │
│                                     │
│      Forum: Crypto Trading          │
│                                     │
│           [ OK ]                    │
└─────────────────────────────────────┘
```

Avec un fond dégradé doré (FFD700 → FFA500) et des animations !

## 📝 Notes

- Les badges sont vérifiés automatiquement après chaque action
- Un utilisateur peut gagner le même badge dans différents forums
- Les badges globaux ne sont gagnés qu'une seule fois
- La notification s'affiche immédiatement après l'obtention du badge

Bon test ! 🚀
