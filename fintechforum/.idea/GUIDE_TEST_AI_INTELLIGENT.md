# 🧪 Guide de Test - Système AI Intelligent

## Étape par Étape pour Tester les Recommandations

---

## ÉTAPE 1: Créer les Tables AI (Si pas encore fait)

### Ouvrir MySQL Workbench ou phpMyAdmin

1. Connectez-vous à votre base de données
2. Sélectionnez la base `fintechforum`
3. Copiez et exécutez ce SQL:

```sql
USE fintechforum;

-- Tables pour le système AI
CREATE TABLE IF NOT EXISTS user_interactions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    forum_id INT NOT NULL,
    interaction_type ENUM('view', 'click', 'post', 'comment', 'like', 'share') NOT NULL,
    interaction_count INT DEFAULT 1,
    last_interaction TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (forum_id) REFERENCES forums(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_forum_type (user_id, forum_id, interaction_type)
);

CREATE TABLE IF NOT EXISTS forum_recommendations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    forum_id INT NOT NULL,
    score DECIMAL(10, 2) DEFAULT 0,
    reason VARCHAR(255),
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (forum_id) REFERENCES forums(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_forum (user_id, forum_id)
);

CREATE TABLE IF NOT EXISTS user_preferences (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    preference_key VARCHAR(100) NOT NULL,
    preference_value VARCHAR(255) NOT NULL,
    weight DECIMAL(5, 2) DEFAULT 1.0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_preference (user_id, preference_key)
);

CREATE INDEX idx_interactions_user ON user_interactions(user_id);
CREATE INDEX idx_interactions_forum ON user_interactions(forum_id);
CREATE INDEX idx_recommendations_user ON forum_recommendations(user_id);
CREATE INDEX idx_recommendations_score ON forum_recommendations(score DESC);

SELECT 'Tables AI créées avec succès!' as Status;
```

✅ **Résultat attendu**: Message "Tables AI créées avec succès!"

---

## ÉTAPE 2: Créer les Forums de Test avec Descriptions Similaires

### Exécuter le SQL de test

Copiez et exécutez ce SQL dans MySQL:

```sql
USE fintechforum;

-- ============================================
-- CRÉER DES FORUMS AVEC DESCRIPTIONS SIMILAIRES
-- ============================================

-- Groupe 1: Forums sur la Crypto (TRÈS SIMILAIRES)
INSERT INTO forums (name, description, creator_id, created_at) VALUES
('Bitcoin et Cryptomonnaies', 'Discussions sur Bitcoin, Ethereum, et autres cryptomonnaies. Trading crypto, analyse technique, et stratégies d''investissement blockchain.', 2, NOW()),
('Blockchain et DeFi', 'Finance décentralisée, smart contracts, NFT, et technologies blockchain. Investissement dans les projets DeFi et crypto.', 2, NOW()),
('Trading Crypto Avancé', 'Stratégies de trading crypto, analyse des marchés, bots de trading, et gestion de portefeuille cryptomonnaies.', 2, NOW());

-- Groupe 2: Forums sur l'Investissement (SIMILAIRES)
INSERT INTO forums (name, description, creator_id, created_at) VALUES
('Bourse et Actions', 'Investissement en bourse, analyse d''actions, dividendes, et stratégies de trading sur les marchés financiers.', 2, NOW()),
('Investissement Long Terme', 'Stratégies d''investissement à long terme, portefeuille diversifié, actions à dividendes, et planification financière.', 2, NOW());

-- Groupe 3: Forums Différents (PEU SIMILAIRES)
INSERT INTO forums (name, description, creator_id, created_at) VALUES
('Immobilier Locatif', 'Gestion locative, recherche de locataires, optimisation fiscale, et rentabilité des investissements immobiliers.', 2, NOW()),
('Épargne et Budget', 'Gestion de budget personnel, épargne mensuelle, livrets d''épargne, et conseils pour économiser.', 2, NOW());

-- Ajouter des membres et posts pour la popularité
INSERT INTO user_forum (user_id, forum_id, joined_at) VALUES
(2, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), NOW()),
(3, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), NOW()),
(4, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), NOW());

INSERT INTO posts (forum_id, author_id, title, content, created_at) VALUES
((SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), 2, 'Bitcoin Bull Run 2026', 'Analyse', NOW()),
((SELECT id FROM forums WHERE name = 'Blockchain et DeFi'), 3, 'Meilleurs protocoles DeFi', 'Guide', NOW());

SELECT 'Forums de test créés!' as Status;
```

✅ **Résultat attendu**: 7 nouveaux forums créés

---

## ÉTAPE 3: Simuler VOS Interactions (Utilisateur ID = 1)

### Exécuter ce SQL pour simuler que VOUS aimez les forums crypto:

```sql
USE fintechforum;

-- ============================================
-- VOUS (utilisateur 1) interagissez avec "Bitcoin et Cryptomonnaies"
-- ============================================

-- Vous avez cliqué 10 fois sur ce forum
INSERT INTO user_interactions (user_id, forum_id, interaction_type, interaction_count) VALUES
(1, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), 'click', 10);

-- Vous avez vu ce forum 15 fois
INSERT INTO user_interactions (user_id, forum_id, interaction_type, interaction_count) VALUES
(1, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), 'view', 15);

-- Vous avez créé 3 posts dans ce forum
INSERT INTO user_interactions (user_id, forum_id, interaction_type, interaction_count) VALUES
(1, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), 'post', 3);

-- Vous avez UPVOTÉ ce forum (important pour l'AI!)
INSERT INTO votes (forum_id, user_id, vote_type) VALUES
((SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), 1, 'upvote');

-- ============================================
-- Simuler le filtrage collaboratif
-- D'autres utilisateurs qui aiment "Bitcoin" aiment aussi "Blockchain et DeFi"
-- ============================================

INSERT INTO user_interactions (user_id, forum_id, interaction_type, interaction_count) VALUES
(2, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), 'click', 12),
(2, (SELECT id FROM forums WHERE name = 'Blockchain et DeFi'), 'click', 10),
(3, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), 'click', 9),
(3, (SELECT id FROM forums WHERE name = 'Blockchain et DeFi'), 'click', 7);

SELECT 'Interactions simulées!' as Status;
```

✅ **Résultat attendu**: Vos interactions sont enregistrées

---

## ÉTAPE 4: Vérifier les Données

### Exécuter ce SQL pour voir vos interactions:

```sql
USE fintechforum;

-- Voir VOS interactions
SELECT 'VOS INTERACTIONS:' as Info;
SELECT f.name as Forum, ui.interaction_type as Type, ui.interaction_count as Nombre
FROM user_interactions ui
JOIN forums f ON ui.forum_id = f.id
WHERE ui.user_id = 1
ORDER BY ui.interaction_count DESC;

-- Voir vos votes
SELECT 'VOS VOTES:' as Info;
SELECT f.name as Forum, v.vote_type as Vote
FROM votes v
JOIN forums f ON v.forum_id = f.id
WHERE v.user_id = 1;

-- Voir tous les forums disponibles
SELECT 'FORUMS DISPONIBLES:' as Info;
SELECT id, name, LEFT(description, 60) as description_preview
FROM forums
ORDER BY id DESC
LIMIT 10;
```

✅ **Résultat attendu**: 
- Vous avez 3 types d'interactions avec "Bitcoin et Cryptomonnaies"
- Vous avez upvoté ce forum
- 7+ forums sont disponibles

---

## ÉTAPE 5: Lancer l'Application et Tester

### 1. Compiler et lancer l'application

```bash
mvn clean javafx:run
```

### 2. Cliquer sur "🤖 Recommandations" dans le header

L'application va automatiquement:
1. Calculer les recommandations basées sur vos interactions
2. Analyser la similarité textuelle des descriptions
3. Appliquer le filtrage collaboratif
4. Afficher les résultats

---

## ÉTAPE 6: Résultats Attendus

### Vous devriez voir ces forums recommandés (dans cet ordre):

#### 🥇 1. "Blockchain et DeFi" (Score: ~70-80 points)
**Raisons affichées:**
- ✅ "Similaire à: Bitcoin et Cryptomonnaies" (Similarité textuelle: ~60%)
- ✅ "Utilisateurs similaires aiment ce forum" (Filtrage collaboratif)
- ✅ "Forum actif" (Posts récents)
- ✅ "Basé sur vos votes positifs"

**Pourquoi en premier?**
- Mots communs: crypto, blockchain, investissement, finance, trading
- Autres utilisateurs qui aiment Bitcoin aiment aussi ce forum
- Vous avez upvoté Bitcoin

#### 🥈 2. "Trading Crypto Avancé" (Score: ~50-60 points)
**Raisons affichées:**
- ✅ "Similaire à: Bitcoin et Cryptomonnaies" (Similarité: ~50%)
- ✅ "Basé sur vos votes positifs"

**Pourquoi en deuxième?**
- Mots communs: crypto, trading, stratégies, analyse

#### 🥉 3. "Bourse et Actions" (Score: ~30-40 points)
**Raisons affichées:**
- ✅ "Similaire à: Bitcoin et Cryptomonnaies" (Similarité: ~30%)
- ✅ "Forum populaire"

**Pourquoi en troisième?**
- Mots communs: investissement, trading, stratégies, analyse
- Moins similaire que les forums crypto

#### 4. "Investissement Long Terme" (Score: ~25-35 points)
**Raisons affichées:**
- ✅ "Similaire à: Bitcoin et Cryptomonnaies" (Similarité: ~25%)

#### 5. Autres forums avec moins de similarité

---

## ÉTAPE 7: Tester l'Évolution des Recommandations

### Test 1: Cliquer sur un forum recommandé

1. Cliquez sur "Blockchain et DeFi" (le premier recommandé)
2. Retournez à "🤖 Recommandations"
3. Cliquez sur "🔄 Actualiser"

**Résultat attendu:**
- "Trading Crypto Avancé" monte en première position
- Nouvelles raisons: "Vos interactions: X click"
- Les scores changent

### Test 2: Upvoter un nouveau forum

1. Allez sur "Accueil"
2. Trouvez "Bourse et Actions"
3. Upvotez-le (si vous avez implémenté les votes dans l'UI)
4. Retournez aux recommandations
5. Cliquez sur "🔄 Actualiser"

**Résultat attendu:**
- "Investissement Long Terme" monte dans le classement
- Raison: "Basé sur vos votes positifs"

### Test 3: Supprimer toutes les recommandations

1. Cliquez sur "🗑️ Tout supprimer"
2. Confirmez
3. La liste se vide
4. Cliquez sur "🔄 Actualiser"
5. Les recommandations sont recalculées

---

## ÉTAPE 8: Comprendre les Scores

### Exemple de calcul pour "Blockchain et DeFi":

```
1. Interactions directes: 0 points
   (Vous n'avez pas encore interagi avec ce forum)

2. Filtrage collaboratif: 10 points
   (2 utilisateurs similaires × 5 points)

3. Popularité: 0 points
   (Peu de membres pour l'instant)

4. Activité récente: 3 points
   (1 post récent × 3 points)

5. Similarité textuelle: 30 points ⭐ NOUVEAU
   (60% de similarité × 50 points)

6. Votes: 5 points ⭐ NOUVEAU
   (Similaire à un forum upvoté)

TOTAL: 48 points
```

---

## ÉTAPE 9: Tester avec Vos Propres Données

### Créer vos propres forums de test:

```sql
-- Créer un forum sur un sujet que vous aimez
INSERT INTO forums (name, description, creator_id, created_at) VALUES
('Votre Sujet', 'Description avec mots-clés importants...', 2, NOW());

-- Interagir avec ce forum
INSERT INTO user_interactions (user_id, forum_id, interaction_type, interaction_count) VALUES
(1, (SELECT id FROM forums WHERE name = 'Votre Sujet'), 'click', 5);

-- Upvoter ce forum
INSERT INTO votes (forum_id, user_id, vote_type) VALUES
((SELECT id FROM forums WHERE name = 'Votre Sujet'), 1, 'upvote');

-- Créer un forum similaire
INSERT INTO forums (name, description, creator_id, created_at) VALUES
('Sujet Similaire', 'Description avec mots-clés similaires...', 2, NOW());
```

Puis testez dans l'application!

---

## ÉTAPE 10: Déboguer si Problème

### Si aucune recommandation n'apparaît:

1. **Vérifier les tables:**
```sql
SELECT COUNT(*) FROM user_interactions WHERE user_id = 1;
SELECT COUNT(*) FROM forum_recommendations WHERE user_id = 1;
```

2. **Vérifier les forums:**
```sql
SELECT COUNT(*) FROM forums;
SELECT COUNT(*) FROM user_forum WHERE user_id = 1;
```

3. **Forcer le recalcul:**
- Cliquez sur "🔄 Actualiser" dans l'application
- Ou exécutez en SQL:
```sql
DELETE FROM forum_recommendations WHERE user_id = 1;
```
Puis relancez l'application

4. **Vérifier les logs:**
- Regardez la console de l'application
- Cherchez les erreurs SQL

---

## Résumé du Test

### Ce que vous devez voir:

✅ Forums crypto recommandés en premier (similarité textuelle)
✅ Raisons détaillées pour chaque recommandation
✅ Scores AI affichés
✅ Recommandations qui évoluent quand vous interagissez
✅ Bouton "Supprimer" qui fonctionne

### Ce qui prouve que l'AI est intelligente:

1. **Similarité textuelle**: Forums avec descriptions similaires sont recommandés
2. **Filtrage collaboratif**: "Les utilisateurs qui aiment X aiment aussi Y"
3. **Votes**: Forums similaires à ceux que vous upvotez sont recommandés
4. **Évolution**: Les recommandations changent avec vos interactions
5. **Transparence**: Les raisons sont affichées clairement

---

## 🎉 Félicitations!

Vous avez maintenant un système de recommandation AI intelligent qui:
- Comprend le contenu des forums
- Apprend de vos interactions
- S'adapte à vos préférences
- Explique ses décisions

**Amusez-vous à tester!** 🚀
