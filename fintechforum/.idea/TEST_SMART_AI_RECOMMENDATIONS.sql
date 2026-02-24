-- ============================================
-- TEST DU SYSTÈME AI INTELLIGENT
-- ============================================
-- Ce script teste la similarité textuelle et les recommandations intelligentes

USE fintechforum;

-- Supprimer les anciens forums de test (optionnel)
-- DELETE FROM forums WHERE creator_id = 2;

-- ============================================
-- CRÉER DES FORUMS AVEC DESCRIPTIONS SIMILAIRES
-- ============================================

-- Groupe 1: Forums sur la Crypto et Blockchain
INSERT INTO forums (name, description, creator_id, created_at) VALUES
('Bitcoin et Cryptomonnaies', 'Discussions sur Bitcoin, Ethereum, et autres cryptomonnaies. Trading crypto, analyse technique, et stratégies d''investissement blockchain.', 2, NOW()),
('Blockchain et DeFi', 'Finance décentralisée, smart contracts, NFT, et technologies blockchain. Investissement dans les projets DeFi et crypto.', 2, NOW()),
('Trading Crypto Avancé', 'Stratégies de trading crypto, analyse des marchés, bots de trading, et gestion de portefeuille cryptomonnaies.', 2, NOW());

-- Groupe 2: Forums sur l'Investissement Traditionnel
INSERT INTO forums (name, description, creator_id, created_at) VALUES
('Bourse et Actions', 'Investissement en bourse, analyse d''actions, dividendes, et stratégies de trading sur les marchés financiers.', 2, NOW()),
('Investissement Long Terme', 'Stratégies d''investissement à long terme, portefeuille diversifié, actions à dividendes, et planification financière.', 2, NOW()),
('Trading Forex et CFD', 'Trading sur le marché des changes, analyse technique forex, stratégies de day trading et swing trading.', 2, NOW());

-- Groupe 3: Forums sur l'Immobilier
INSERT INTO forums (name, description, creator_id, created_at) VALUES
('Investissement Immobilier', 'Achat immobilier, location, SCPI, et stratégies d''investissement dans la pierre. Rentabilité locative et fiscalité immobilière.', 2, NOW()),
('Immobilier Locatif', 'Gestion locative, recherche de locataires, optimisation fiscale, et rentabilité des investissements immobiliers locatifs.', 2, NOW());

-- Groupe 4: Forums sur l'Entrepreneuriat
INSERT INTO forums (name, description, creator_id, created_at) VALUES
('Startup et Entrepreneuriat', 'Créer sa startup fintech, levée de fonds, business plan, et développement d''entreprise innovante.', 2, NOW()),
('Business et Innovation', 'Innovation technologique, modèles d''affaires disruptifs, et stratégies de croissance pour startups et PME.', 2, NOW());

-- Groupe 5: Forums Divers
INSERT INTO forums (name, description, creator_id, created_at) VALUES
('Épargne et Budget', 'Gestion de budget personnel, épargne mensuelle, livrets d''épargne, et conseils pour économiser au quotidien.', 2, NOW()),
('Fiscalité et Impôts', 'Optimisation fiscale, déclarations d''impôts, niches fiscales, et stratégies de réduction d''impôts légales.', 2, NOW());

-- ============================================
-- AJOUTER DES MEMBRES ET ACTIVITÉ
-- ============================================

-- Ajouter des membres aux forums crypto
INSERT INTO user_forum (user_id, forum_id, joined_at) VALUES
(2, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), NOW()),
(3, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), NOW()),
(4, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), NOW()),
(2, (SELECT id FROM forums WHERE name = 'Blockchain et DeFi'), NOW()),
(3, (SELECT id FROM forums WHERE name = 'Blockchain et DeFi'), NOW());

-- Ajouter des posts récents
INSERT INTO posts (forum_id, author_id, title, content, created_at) VALUES
((SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), 2, 'Bitcoin Bull Run 2026', 'Analyse du marché crypto', NOW()),
((SELECT id FROM forums WHERE name = 'Blockchain et DeFi'), 3, 'Meilleurs protocoles DeFi', 'Mes recommandations', DATE_SUB(NOW(), INTERVAL 1 DAY)),
((SELECT id FROM forums WHERE name = 'Bourse et Actions'), 2, 'Actions tech à surveiller', 'FAANG analysis', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- ============================================
-- SIMULER LES INTERACTIONS DE L'UTILISATEUR 1
-- ============================================

-- L'utilisateur 1 interagit BEAUCOUP avec "Bitcoin et Cryptomonnaies"
INSERT INTO user_interactions (user_id, forum_id, interaction_type, interaction_count) VALUES
(1, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), 'click', 10),
(1, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), 'view', 15),
(1, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), 'post', 3);

-- L'utilisateur 1 vote positivement pour "Bitcoin et Cryptomonnaies"
INSERT INTO votes (forum_id, user_id, vote_type) VALUES
((SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), 1, 'upvote');

-- L'utilisateur 1 a aussi regardé "Bourse et Actions" (moins d'interactions)
INSERT INTO user_interactions (user_id, forum_id, interaction_type, interaction_count) VALUES
(1, (SELECT id FROM forums WHERE name = 'Bourse et Actions'), 'view', 3);

-- ============================================
-- SIMULER LE FILTRAGE COLLABORATIF
-- ============================================

-- D'autres utilisateurs qui aiment "Bitcoin et Cryptomonnaies" aiment aussi "Blockchain et DeFi"
INSERT INTO user_interactions (user_id, forum_id, interaction_type, interaction_count) VALUES
(2, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), 'click', 12),
(2, (SELECT id FROM forums WHERE name = 'Blockchain et DeFi'), 'click', 10),
(2, (SELECT id FROM forums WHERE name = 'Trading Crypto Avancé'), 'click', 8),
(3, (SELECT id FROM forums WHERE name = 'Bitcoin et Cryptomonnaies'), 'click', 9),
(3, (SELECT id FROM forums WHERE name = 'Blockchain et DeFi'), 'click', 7),
(3, (SELECT id FROM forums WHERE name = 'Trading Crypto Avancé'), 'click', 6);

-- ============================================
-- RÉSULTATS ATTENDUS
-- ============================================

SELECT '============================================' as '';
SELECT 'RÉSULTATS ATTENDUS POUR L''UTILISATEUR 1:' as '';
SELECT '============================================' as '';

SELECT 'Forums avec lesquels vous avez interagi:' as Info;
SELECT f.name, ui.interaction_type, ui.interaction_count
FROM user_interactions ui
JOIN forums f ON ui.forum_id = f.id
WHERE ui.user_id = 1
ORDER BY ui.interaction_count DESC;

SELECT '' as '';
SELECT 'Recommandations attendues (par ordre de pertinence):' as Info;
SELECT '1. Blockchain et DeFi - TRÈS SIMILAIRE (mots: crypto, blockchain, investissement)' as Recommandation
UNION ALL SELECT '2. Trading Crypto Avancé - TRÈS SIMILAIRE (mots: crypto, trading, stratégies)'
UNION ALL SELECT '3. Investissement Long Terme - SIMILAIRE (mots: investissement, stratégies)'
UNION ALL SELECT '4. Trading Forex et CFD - SIMILAIRE (mots: trading, analyse, stratégies)'
UNION ALL SELECT '5. Autres forums populaires avec activité récente';

SELECT '' as '';
SELECT '============================================' as '';
SELECT 'MAINTENANT, TESTEZ DANS L''APPLICATION:' as '';
SELECT '============================================' as '';
SELECT '1. Lancez l''application' as Etape
UNION ALL SELECT '2. Cliquez sur "🤖 Recommandations"'
UNION ALL SELECT '3. Vous devriez voir les forums crypto en premier'
UNION ALL SELECT '4. Les raisons incluront: "Similaire à: Bitcoin et Cryptomonnaies"'
UNION ALL SELECT '5. Cliquez sur quelques forums recommandés'
UNION ALL SELECT '6. Cliquez sur "🔄 Actualiser" pour voir les nouvelles recommandations';

-- ============================================
-- VÉRIFICATION DES DONNÉES
-- ============================================

SELECT '' as '';
SELECT 'Vérification: Tous les forums créés' as Info;
SELECT id, name, LEFT(description, 50) as description_preview
FROM forums
WHERE creator_id = 2
ORDER BY id DESC;
