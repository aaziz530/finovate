-- Table pour les posts partagés
CREATE TABLE IF NOT EXISTS shared_posts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    shared_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_share (post_id, user_id)
);

-- Index pour améliorer les performances
CREATE INDEX idx_shared_posts_user ON shared_posts(user_id);
CREATE INDEX idx_shared_posts_post ON shared_posts(post_id);
