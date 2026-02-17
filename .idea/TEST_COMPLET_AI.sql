-- ============================================
-- SCRIPT COMPLET POUR TESTER L'AI INTELLIGENT
-- ============================================
-- Exécutez ce script dans MySQL pour tester le système
-- Base de données: fintechforum

USE fintechforum;

-- ============================================
-- PARTIE 1: CRÉER LES TABLES AI (si pas encore fait)
-- ============================================

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

CREATE INDEX IF NOT EXISTS idx_interactions_user ON user_interactions(user_id);
CREATE INDEX IF NOT EXISTS idx_interactions_forum ON user_interactions(forum_id);
CREATE INDEX IF NOT EXISTS idx_recommendations_user ON forum_recommendations(user_id);
CREATE INDEX IF NOT EXISTS idx_recommendations_score ON forum_recommendations(score DESC);

SELECT '✅ ÉTAPE 1: Tables AI créées' as Status;

-- ============================================
-- PARTIE 2: CRÉER DES FORUMS AVEC DESCRIPTIONS SIMILAIRES
-- ============================================

-- Groupe 1: Forums CRYPTO (très similaires entre eux)
INSERT INTO forums (name, description, creator_id, created_at) VALUES
('Bitcoin et Cryptomonnaies', 'Discussions sur Bitcoin, Ethereum, et autres cryptomonnaies. Trading crypto, analyse technique, et stratégies d''investissement blockchain.', 2, NOW()),
('Blockchain et DeFi', 'Finance décentralisée, smart contracts, NFT, et technologies blockchain. Investissement dans les projets DeFi et crypto.', 2, NOW()),
('Trading Crypto Avancé', 'Stratégies de trading crypto, analyse des marchés, bots de trading, et gestion de portefeuille cryptomonnaies.', 2, NOW());

-- Groupe 2: Forums INVESTISSEMENT (similaires entre eux)
INSERT INTO forums (name, description, creator_id, created_at) VALUES
('Bourse et Actions', 'Investissement en bourse, analyse d''actions, dividendes, et stratégies de trading sur les marchés financiers.', 2, NOW()),
('Investissement Long Terme', 'Stratégies d''investissement à long terme, portefeuille diversifié, actions à dividendes, et planification financière.', 2, NOW()),
('Trading Forex et CFD', 'Trading sur le marché des changes, analyse technique forex, stratégies de day trading et swing trading.', 2, NOW());

-- Groupe 3: Forums IMMOBILIER (similaires entre eux)
INSERT INTO forums (name, description, creator_id, created_at) VALUES
('Investissement Immobilier', 'Achat immobilier, location, SCPI, et stratégies d''investissement dans la pierre. Rentabilité locative.', 2, NOW()),
('Immobilier Locatif', 'Gestion locative, recherche de locataires, optimisation fiscale, et rentabilité des investissements immobiliers.', 2, NOW());

-- Groupe 4: Forums DIVERS (peu similaires aux autres)
INSERT INTO forums (name, description, creator_id, created_at) VALUES
('Épargne et Budget', 'Gestion de budget personnel, épargne mensuelle, livrets d''épargne, et conseils pour économiser au quotidien.', 2, NOW()),
('Fiscalité et Impôts', 'Optimisation fiscale, déclarations d''impôts, niches fiscales, et stratégies de réduction d''impôts légales.', 2, NOW());

SELECT '✅ ÉTAPE 2: Forums de test créés' as Status;

-- ============================================
-- PARTIE 3: AJOUTER MEMBRES ET ACTIVITÉ
-- ============================================

-- Ajouter des membres aux forums crypto (pour la popularité)
INSERT INTO user_forum (user_id, forum_id, joined_at) VALUES
(2, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), NOW()),
(3, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), NOW()),
(4, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), NOW()),
(2, (SELECT id FROM forums WHERE name = 'Blockchain et DeFi'), NOW()),
(3, (SELECT id FROM forums WHERE name = 'Blockchain et DeFi'), NOW());

-- Ajouter des posts récents (pour l'activité)
INSERT INTO posts (forum_id, author_id, title, content, created_at) VALUES
((SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), 2, 'Bitcoin Bull Run 2026', 'Analyse du marché crypto', NOW()),
((SELECT id FROM forums WHERE name = 'Blockchain et DeFi'), 3, 'Meilleurs protocoles DeFi', 'Mes recommandations', NOW()),
((SELECT id FROM forums WHERE name = 'Trading Crypto Avancé'), 2, 'Stratégie de trading', 'Ma méthode', DATE_SUB(NOW(), INTERVAL 1 DAY)),
((SELECT id FROM forums WHERE name = 'Bourse et Actions'), 3, 'Actions tech 2026', 'Analyse', DATE_SUB(NOW(), INTERVAL 2 DAY));

SELECT '✅ ÉTAPE 3: Membres et activité ajoutés' as Status;

-- ============================================
-- PARTIE 4: SIMULER VOS INTERACTIONS (Utilisateur ID = 1)
-- ============================================

-- VOUS avez beaucoup interagi avec "Bitcoin et Cryptomonnaies"
INSERT INTO user_interactions (user_id, forum_id, interaction_type, interaction_count) VALUES
(1, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), 'click', 10),
(1, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), 'view', 15),
(1, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), 'post', 3);

-- VOUS avez upvoté "Bitcoin et Cryptomonnaies" (important!)
INSERT INTO votes (forum_id, user_id, vote_type) VALUES
((SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), 1, 'upvote');

-- VOUS avez aussi regardé "Bourse et Actions" (moins d'interactions)
INSERT INTO user_interactions (user_id, forum_id, interaction_type, interaction_count) VALUES
(1, (SELECT id FROM forums WHERE name = 'Bourse et Actions'), 'view', 3);

SELECT '✅ ÉTAPE 4: Vos interactions simulées' as Status;

-- ============================================
-- PARTIE 5: SIMULER LE FILTRAGE COLLABORATIF
-- ============================================

-- D'autres utilisateurs qui aiment "Bitcoin" aiment aussi "Blockchain et DeFi"
INSERT INTO user_interactions (user_id, forum_id, interaction_type, interaction_count) VALUES
(2, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), 'click', 12),
(2, (SELECT id FROM forums WHERE name = 'Blockchain et DeFi'), 'click', 10),
(2, (SELECT id FROM forums WHERE name = 'Trading Crypto Avancé'), 'click', 8),
(3, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), 'click', 9),
(3, (SELECT id FROM forums WHERE name = 'Blockchain et DeFi'), 'click', 7),
(3, (SELECT id FROM forums WHERE name = 'Trading Crypto Avancé'), 'click', 6);

SELECT '✅ ÉTAPE 5: Filtrage collaboratif simulé' as Status;

-- ============================================
-- PARTIE 6: VÉRIFICATION DES DONNÉES
-- ============================================

SELECT '' as '';
SELECT '========================================' as '';
SELECT '📊 VÉRIFICATION DES DONNÉES' as '';
SELECT '========================================' as '';

SELECT '' as '';
SELECT '1️⃣ VOS INTERACTIONS (Utilisateur 1):' as Info;
SELECT f.name as Forum, ui.interaction_type as Type, ui.interaction_count as Nombre
FROM user_interactions ui
JOIN forums f ON ui.forum_id = f.id
WHERE ui.user_id = 1
ORDER BY ui.interaction_count DESC;

SELECT '' as '';
SELECT '2️⃣ VOS VOTES:' as Info;
SELECT f.name as Forum, v.vote_type as Vote
FROM votes v
JOIN forums f ON v.forum_id = f.id
WHERE v.user_id = 1;

SELECT '' as '';
SELECT '3️⃣ FORUMS DISPONIBLES (non rejoints):' as Info;
SELECT f.id, f.name, LEFT(f.description, 50) as description_preview
FROM forums f
WHERE f.id NOT IN (SELECT forum_id FROM user_forum WHERE user_id = 1)
ORDER BY f.id DESC
LIMIT 10;

SELECT '' as '';
SELECT '4️⃣ INTERACTIONS DES AUTRES UTILISATEURS:' as Info;
SELECT u.id as User_ID, f.name as Forum, ui.interaction_type as Type, ui.interaction_count as Nombre
FROM user_interactions ui
JOIN forums f ON ui.forum_id = f.id
JOIN users u ON ui.user_id = u.id
WHERE ui.user_id IN (2, 3)
ORDER BY ui.user_id, ui.interaction_count DESC
LIMIT 10;

-- ============================================
-- PARTIE 7: RÉSULTATS ATTENDUS
-- ============================================

SELECT '' as '';
SELECT '========================================' as '';
SELECT '🎯 RÉSULTATS ATTENDUS DANS L''APPLICATION' as '';
SELECT '========================================' as '';

SELECT '' as '';
SELECT 'Quand vous cliquez sur "🤖 Recommandations", vous devriez voir:' as Info;

SELECT '' as '';
SELECT '🥇 1. Blockchain et DeFi (Score: ~70-80)' as Recommandation
UNION ALL SELECT '   Raisons: Similaire à Bitcoin, Utilisateurs similaires, Forum actif, Basé sur vos votes'
UNION ALL SELECT ''
UNION ALL SELECT '🥈 2. Trading Crypto Avancé (Score: ~50-60)'
UNION ALL SELECT '   Raisons: Similaire à Bitcoin, Basé sur vos votes'
UNION ALL SELECT ''
UNION ALL SELECT '🥉 3. Bourse et Actions (Score: ~30-40)'
UNION ALL SELECT '   Raisons: Similaire à Bitcoin, Forum populaire'
UNION ALL SELECT ''
UNION ALL SELECT '4. Investissement Long Terme (Score: ~25-35)'
UNION ALL SELECT '   Raisons: Similaire à Bitcoin'
UNION ALL SELECT ''
UNION ALL SELECT '5. Autres forums avec moins de similarité';

SELECT '' as '';
SELECT '========================================' as '';
SELECT '🚀 PROCHAINES ÉTAPES' as '';
SELECT '========================================' as '';

SELECT '' as '';
SELECT '1. Lancez l''application: mvn clean javafx:run' as Etape
UNION ALL SELECT '2. Cliquez sur "🤖 Recommandations" dans le header'
UNION ALL SELECT '3. Observez les forums recommandés et leurs raisons'
UNION ALL SELECT '4. Cliquez sur "Blockchain et DeFi" pour interagir'
UNION ALL SELECT '5. Retournez aux recommandations et cliquez "🔄 Actualiser"'
UNION ALL SELECT '6. Observez comment les recommandations évoluent'
UNION ALL SELECT '7. Testez le bouton "🗑️ Tout supprimer"';

SELECT '' as '';
SELECT '✅ Script de test complet exécuté avec succès!' as Status;
SELECT 'Vous pouvez maintenant tester l''application!' as Message;
