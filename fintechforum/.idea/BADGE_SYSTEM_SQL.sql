-- ============================================
-- SYSTÈME DE BADGES - GAMIFICATION
-- ============================================

-- Table des types de badges disponibles
CREATE TABLE IF NOT EXISTS badge_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon VARCHAR(50),
    category VARCHAR(50),
    requirement_type VARCHAR(50), -- 'VOTE_COUNT', 'POST_COUNT', 'COMMENT_COUNT', etc.
    requirement_value INT,
    forum_specific BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des badges gagnés par les utilisateurs
CREATE TABLE IF NOT EXISTS user_badges (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    badge_type_id INT NOT NULL,
    forum_id INT NULL, -- NULL si badge global, sinon ID du forum spécifique
    earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (badge_type_id) REFERENCES badge_types(id) ON DELETE CASCADE,
    FOREIGN KEY (forum_id) REFERENCES forums(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_badge (user_id, badge_type_id, forum_id)
);

-- Table pour tracker les progrès vers les badges
CREATE TABLE IF NOT EXISTS badge_progress (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    badge_type_id INT NOT NULL,
    forum_id INT NULL,
    current_count INT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (badge_type_id) REFERENCES badge_types(id) ON DELETE CASCADE,
    FOREIGN KEY (forum_id) REFERENCES forums(id) ON DELETE CASCADE,
    UNIQUE KEY unique_progress (user_id, badge_type_id, forum_id)
);

-- Insérer les types de badges
INSERT INTO badge_types (name, description, icon, category, requirement_type, requirement_value, forum_specific) VALUES
-- Badges de vote
('Fan du Forum', 'Votez sur 5 posts dans le même forum', '⭐', 'ENGAGEMENT', 'VOTE_COUNT', 5, TRUE),
('Super Fan', 'Votez sur 10 posts dans le même forum', '🌟', 'ENGAGEMENT', 'VOTE_COUNT', 10, TRUE),
('Mega Fan', 'Votez sur 25 posts dans le même forum', '💫', 'ENGAGEMENT', 'VOTE_COUNT', 25, TRUE),
('Voteur Actif', 'Votez sur 50 posts au total', '👍', 'ENGAGEMENT', 'VOTE_COUNT_GLOBAL', 50, FALSE),

-- Badges de création de contenu
('Premier Post', 'Créez votre premier post', '📝', 'CREATION', 'POST_COUNT', 1, FALSE),
('Auteur Régulier', 'Créez 10 posts', '✍️', 'CREATION', 'POST_COUNT', 10, FALSE),
('Auteur Prolifique', 'Créez 50 posts', '📚', 'CREATION', 'POST_COUNT', 50, FALSE),

-- Badges de commentaires
('Commentateur', 'Postez 10 commentaires', '💬', 'INTERACTION', 'COMMENT_COUNT', 10, FALSE),
('Conversateur', 'Postez 50 commentaires', '🗨️', 'INTERACTION', 'COMMENT_COUNT', 50, FALSE),

-- Badges de popularité
('Post Populaire', 'Recevez 10 upvotes sur un post', '🔥', 'POPULARITY', 'POST_UPVOTES', 10, FALSE),
('Post Viral', 'Recevez 50 upvotes sur un post', '🚀', 'POPULARITY', 'POST_UPVOTES', 50, FALSE),

-- Badges de communauté
('Membre Actif', 'Rejoignez 5 forums', '👥', 'COMMUNITY', 'FORUM_JOIN_COUNT', 5, FALSE),
('Explorateur', 'Rejoignez 10 forums', '🗺️', 'COMMUNITY', 'FORUM_JOIN_COUNT', 10, FALSE),

-- Badges de partage
('Partageur', 'Partagez 5 posts', '📤', 'SHARING', 'SHARE_COUNT', 5, FALSE),
('Influenceur', 'Partagez 20 posts', '📢', 'SHARING', 'SHARE_COUNT', 20, FALSE);

-- Vue pour afficher les badges des utilisateurs avec détails
CREATE OR REPLACE VIEW user_badges_view AS
SELECT 
    ub.id,
    ub.user_id,
    u.username,
    bt.name as badge_name,
    bt.description as badge_description,
    bt.icon as badge_icon,
    bt.category,
    f.name as forum_name,
    ub.earned_at
FROM user_badges ub
INNER JOIN users u ON ub.user_id = u.id
INNER JOIN badge_types bt ON ub.badge_type_id = bt.id
LEFT JOIN forums f ON ub.forum_id = f.id
ORDER BY ub.earned_at DESC;

-- Vue pour le progrès des badges
CREATE OR REPLACE VIEW badge_progress_view AS
SELECT 
    bp.user_id,
    u.username,
    bt.name as badge_name,
    bt.description,
    bt.icon,
    bt.requirement_value,
    bp.current_count,
    ROUND((bp.current_count / bt.requirement_value) * 100, 2) as progress_percentage,
    f.name as forum_name,
    CASE 
        WHEN bp.current_count >= bt.requirement_value THEN 'READY'
        ELSE 'IN_PROGRESS'
    END as status
FROM badge_progress bp
INNER JOIN users u ON bp.user_id = u.id
INNER JOIN badge_types bt ON bp.badge_type_id = bt.id
LEFT JOIN forums f ON bp.forum_id = f.id;
