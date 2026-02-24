-- Mise à jour de la table votes pour supporter les forums
-- Si la table existe déjà, on la modifie, sinon on la crée

-- Supprimer la table existante si nécessaire (attention aux données!)
-- DROP TABLE IF EXISTS votes;

-- Créer la table votes avec support pour posts ET forums
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

-- Index pour améliorer les performances
CREATE INDEX idx_votes_user ON votes(user_id);
CREATE INDEX idx_votes_post ON votes(post_id);
CREATE INDEX idx_votes_forum ON votes(forum_id);
