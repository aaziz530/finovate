-- ============================================
-- SCRIPT 1: Créer la table shared_posts
-- ============================================
CREATE TABLE IF NOT EXISTS shared_posts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    shared_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_share (post_id, user_id)
);

CREATE INDEX idx_shared_posts_user ON shared_posts(user_id);
CREATE INDEX idx_shared_posts_post ON shared_posts(post_id);

-- ============================================
-- SCRIPT 2: Créer/Mettre à jour la table votes
-- ============================================

-- Option A: Si la table votes n'existe PAS DU TOUT
CREATE TABLE IF NOT EXISTS votes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    post_id INT NULL,
    forum_id INT NULL,
    vote_type ENUM('UPVOTE', 'DOWNVOTE') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (forum_id) REFERENCES forums(id) ON DELETE CASCADE,
    UNIQUE KEY unique_post_vote (user_id, post_id),
    UNIQUE KEY unique_forum_vote (user_id, forum_id),
    CHECK ((post_id IS NOT NULL AND forum_id IS NULL) OR (post_id IS NULL AND forum_id IS NOT NULL))
);

CREATE INDEX idx_votes_user ON votes(user_id);
CREATE INDEX idx_votes_post ON votes(post_id);
CREATE INDEX idx_votes_forum ON votes(forum_id);

-- ============================================
-- Option B: Si la table votes EXISTE DÉJÀ mais sans forum_id
-- Décommente et exécute ces lignes:
-- ============================================

-- ALTER TABLE votes ADD COLUMN forum_id INT NULL AFTER post_id;
-- ALTER TABLE votes ADD FOREIGN KEY (forum_id) REFERENCES forums(id) ON DELETE CASCADE;
-- ALTER TABLE votes ADD UNIQUE KEY unique_forum_vote (user_id, forum_id);
-- ALTER TABLE votes ADD CHECK ((post_id IS NOT NULL AND forum_id IS NULL) OR (post_id IS NULL AND forum_id IS NOT NULL));
-- CREATE INDEX idx_votes_forum ON votes(forum_id);

-- ============================================
-- VÉRIFICATION: Voir la structure de la table votes
-- ============================================
-- DESCRIBE votes;
