-- Run this script on your finovate database (once) to add new columns and table
-- MySQL 8.0.12+ supports IF NOT EXISTS. For MySQL 5.7, run the 3 ALTER lines below (ignore errors if columns exist).

ALTER TABLE project ADD COLUMN latitude DOUBLE NULL;
ALTER TABLE project ADD COLUMN longitude DOUBLE NULL;
ALTER TABLE project ADD COLUMN category VARCHAR(100) NULL;

-- History of project funding (for milestones and charts)
-- Sans clé étrangère pour éviter l'erreur 150
CREATE TABLE IF NOT EXISTS project_amount_history (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  project_id BIGINT NOT NULL,
  amount DOUBLE NOT NULL,
  recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_project (project_id)
) ENGINE=InnoDB;
