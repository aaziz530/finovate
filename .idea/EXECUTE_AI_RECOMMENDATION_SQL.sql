-- ============================================
-- SCRIPT D'INSTALLATION DU SYSTÈME AI
-- ============================================
-- Copier et exécuter ce script dans MySQL Workbench ou phpMyAdmin
-- Base de données: fintechforum

USE fintechforum;

-- Table pour tracker les interactions utilisateur
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

-- Table pour stocker les scores de recommandation
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

-- Table pour les préférences utilisateur (tags/catégories)
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

-- Index pour optimiser les requêtes
CREATE INDEX idx_interactions_user ON user_interactions(user_id);
CREATE INDEX idx_interactions_forum ON user_interactions(forum_id);
CREATE INDEX idx_recommendations_user ON forum_recommendations(user_id);
CREATE INDEX idx_recommendations_score ON forum_recommendations(score DESC);

-- ============================================
-- DONNÉES DE TEST (OPTIONNEL)
-- ============================================
-- Simuler quelques interactions pour tester le système

-- Utilisateur 1 interagit avec Forum 1
INSERT INTO user_interactions (user_id, forum_id, interaction_type, interaction_count) 
VALUES (1, 1, 'click', 5) 
ON DUPLICATE KEY UPDATE interaction_count = interaction_count + 5;

INSERT INTO user_interactions (user_id, forum_id, interaction_type, interaction_count) 
VALUES (1, 1, 'view', 10) 
ON DUPLICATE KEY UPDATE interaction_count = interaction_count + 10;

-- Utilisateur 1 interagit avec Forum 2
INSERT INTO user_interactions (user_id, forum_id, interaction_type, interaction_count) 
VALUES (1, 2, 'click', 3) 
ON DUPLICATE KEY UPDATE interaction_count = interaction_count + 3;

INSERT INTO user_interactions (user_id, forum_id, interaction_type, interaction_count) 
VALUES (1, 2, 'post', 2) 
ON DUPLICATE KEY UPDATE interaction_count = interaction_count + 2;

-- Vérifier les tables créées
SELECT 'Tables créées avec succès!' as Status;
SELECT COUNT(*) as 'Interactions de test' FROM user_interactions;

-- ============================================
-- FIN DU SCRIPT
-- ============================================
