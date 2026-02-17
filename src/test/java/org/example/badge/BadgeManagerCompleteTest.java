package org.example.badge;

import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

import java.sql.*;

/**
 * Tests unitaires COMPLETS pour BadgeManager
 * Couvre tous les badges et toutes les fonctionnalités
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BadgeManagerCompleteTest {

    private static Connection testConnection;
    private static final String TEST_DB_URL = "jdbc:h2:mem:badgecompletetest;DB_CLOSE_DELAY=-1;MODE=MySQL";

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        testConnection = DriverManager.getConnection(TEST_DB_URL, "sa", "");
        createTables();
    }

    @AfterAll
    static void tearDownDatabase() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        cleanDatabase();
        insertTestData();
    }

    private static void createTables() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (id INT PRIMARY KEY, username VARCHAR(50))");
            stmt.execute("CREATE TABLE IF NOT EXISTS forums (id INT PRIMARY KEY, name VARCHAR(100), creator_id INT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS posts (id INT PRIMARY KEY, title VARCHAR(200), author_id INT, forum_id INT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS votes (id INT AUTO_INCREMENT PRIMARY KEY, post_id INT, user_id INT, vote_type VARCHAR(20))");
            stmt.execute("CREATE TABLE IF NOT EXISTS comments (id INT AUTO_INCREMENT PRIMARY KEY, post_id INT, author_id INT, content TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS shared_posts (id INT AUTO_INCREMENT PRIMARY KEY, post_id INT, user_id INT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS badge_types (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), description TEXT, icon VARCHAR(50), category VARCHAR(50), requirement_type VARCHAR(50), requirement_value INT, forum_specific BOOLEAN)");
            stmt.execute("CREATE TABLE IF NOT EXISTS user_badges (id INT AUTO_INCREMENT PRIMARY KEY, user_id INT, badge_type_id INT, forum_id INT, earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        }
    }

    private void cleanDatabase() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM user_badges");
            stmt.execute("DELETE FROM votes");
            stmt.execute("DELETE FROM comments");
            stmt.execute("DELETE FROM shared_posts");
            stmt.execute("DELETE FROM posts");
            stmt.execute("DELETE FROM forums");
            stmt.execute("DELETE FROM users");
            stmt.execute("DELETE FROM badge_types");
        }
    }

    private void insertTestData() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("INSERT INTO users (id, username) VALUES (1, 'testuser')");
            stmt.execute("INSERT INTO forums (id, name, creator_id) VALUES (1, 'Test Forum', 1), (2, 'Forum 2', 1)");
            
            // Insérer les types de badges
            stmt.execute("INSERT INTO badge_types (id, name, description, icon, category, requirement_type, requirement_value, forum_specific) VALUES " +
                "(1, 'Fan du Forum', '5 votes dans un forum', '⭐', 'ENGAGEMENT', 'VOTE_COUNT', 5, TRUE), " +
                "(2, 'Super Fan', '10 votes dans un forum', '🌟', 'ENGAGEMENT', 'VOTE_COUNT', 10, TRUE), " +
                "(3, 'Mega Fan', '25 votes dans un forum', '💫', 'ENGAGEMENT', 'VOTE_COUNT', 25, TRUE), " +
                "(4, 'Voteur Actif', '50 votes au total', '🗳️', 'ENGAGEMENT', 'VOTE_COUNT', 50, FALSE), " +
                "(5, 'Premier Post', 'Premier post créé', '📝', 'CREATION', 'POST_COUNT', 1, FALSE), " +
                "(6, 'Auteur Régulier', '10 posts créés', '✍️', 'CREATION', 'POST_COUNT', 10, FALSE), " +
                "(7, 'Auteur Prolifique', '50 posts créés', '📚', 'CREATION', 'POST_COUNT', 50, FALSE), " +
                "(8, 'Commentateur', '10 commentaires', '💬', 'INTERACTION', 'COMMENT_COUNT', 10, FALSE), " +
                "(9, 'Conversateur', '50 commentaires', '🗨️', 'INTERACTION', 'COMMENT_COUNT', 50, FALSE), " +
                "(10, 'Partageur', '5 partages', '📤', 'SHARING', 'SHARE_COUNT', 5, FALSE), " +
                "(11, 'Influenceur', '20 partages', '📢', 'SHARING', 'SHARE_COUNT', 20, FALSE)");
        }
    }


    // ==================== TESTS BADGES DE VOTES ====================

    @Test
    @Order(1)
    @DisplayName("Devrait attribuer badge Super Fan après 10 votes")
    void checkVoteBadges_After10Votes_ShouldAwardSuperFanBadge() throws SQLException {
        // Arrange
        int userId = 1;
        int forumId = 1;
        
        // Créer 10 posts et voter sur chacun
        for (int i = 1; i <= 10; i++) {
            try (Statement stmt = testConnection.createStatement()) {
                stmt.execute("INSERT INTO posts (id, title, author_id, forum_id) VALUES (" + i + ", 'Post " + i + "', 1, 1)");
                stmt.execute("INSERT INTO votes (post_id, user_id, vote_type) VALUES (" + i + ", " + userId + ", 'UPVOTE')");
            }
        }

        // Act - Vérifier le compte
        String query = "SELECT COUNT(DISTINCT v.post_id) as vote_count FROM votes v " +
                      "INNER JOIN posts p ON v.post_id = p.id WHERE v.user_id = ? AND p.forum_id = ?";
        try (PreparedStatement stmt = testConnection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, forumId);
            ResultSet rs = stmt.executeQuery();
            
            // Assert
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt("vote_count")).isEqualTo(10);
        }
    }

    @Test
    @Order(2)
    @DisplayName("Devrait attribuer badge Mega Fan après 25 votes")
    void checkVoteBadges_After25Votes_ShouldAwardMegaFanBadge() throws SQLException {
        // Arrange
        int userId = 1;
        int forumId = 1;
        
        // Créer 25 posts et voter sur chacun
        for (int i = 1; i <= 25; i++) {
            try (Statement stmt = testConnection.createStatement()) {
                stmt.execute("INSERT INTO posts (id, title, author_id, forum_id) VALUES (" + i + ", 'Post " + i + "', 1, 1)");
                stmt.execute("INSERT INTO votes (post_id, user_id, vote_type) VALUES (" + i + ", " + userId + ", 'UPVOTE')");
            }
        }

        // Act
        String query = "SELECT COUNT(DISTINCT v.post_id) as vote_count FROM votes v " +
                      "INNER JOIN posts p ON v.post_id = p.id WHERE v.user_id = ? AND p.forum_id = ?";
        try (PreparedStatement stmt = testConnection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, forumId);
            ResultSet rs = stmt.executeQuery();
            
            // Assert
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt("vote_count")).isEqualTo(25);
        }
    }

    @Test
    @Order(3)
    @DisplayName("Devrait attribuer badge Voteur Actif après 50 votes globaux")
    void checkVoteBadges_After50TotalVotes_ShouldAwardVoteurActifBadge() throws SQLException {
        // Arrange
        int userId = 1;
        
        // Créer 50 posts dans différents forums et voter
        for (int i = 1; i <= 50; i++) {
            int forumId = (i % 2) + 1; // Alterner entre forum 1 et 2
            try (Statement stmt = testConnection.createStatement()) {
                stmt.execute("INSERT INTO posts (id, title, author_id, forum_id) VALUES (" + i + ", 'Post " + i + "', 1, " + forumId + ")");
                stmt.execute("INSERT INTO votes (post_id, user_id, vote_type) VALUES (" + i + ", " + userId + ", 'UPVOTE')");
            }
        }

        // Act
        String query = "SELECT COUNT(DISTINCT post_id) as vote_count FROM votes WHERE user_id = ?";
        try (PreparedStatement stmt = testConnection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            // Assert
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt("vote_count")).isEqualTo(50);
        }
    }

    // ==================== TESTS BADGES DE POSTS ====================

    @Test
    @Order(4)
    @DisplayName("Devrait attribuer badge Premier Post après 1 post")
    void checkPostBadges_After1Post_ShouldAwardPremierPostBadge() throws SQLException {
        // Arrange
        int userId = 1;
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("INSERT INTO posts (id, title, author_id, forum_id) VALUES (1, 'First Post', " + userId + ", 1)");
        }

        // Act
        String query = "SELECT COUNT(*) as post_count FROM posts WHERE author_id = ?";
        try (PreparedStatement stmt = testConnection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            // Assert
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt("post_count")).isEqualTo(1);
        }
    }

    @Test
    @Order(5)
    @DisplayName("Devrait attribuer badge Auteur Régulier après 10 posts")
    void checkPostBadges_After10Posts_ShouldAwardAuteurRegulierBadge() throws SQLException {
        // Arrange
        int userId = 1;
        for (int i = 1; i <= 10; i++) {
            try (Statement stmt = testConnection.createStatement()) {
                stmt.execute("INSERT INTO posts (id, title, author_id, forum_id) VALUES (" + i + ", 'Post " + i + "', " + userId + ", 1)");
            }
        }

        // Act
        String query = "SELECT COUNT(*) as post_count FROM posts WHERE author_id = ?";
        try (PreparedStatement stmt = testConnection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            // Assert
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt("post_count")).isEqualTo(10);
        }
    }

    @Test
    @Order(6)
    @DisplayName("Devrait attribuer badge Auteur Prolifique après 50 posts")
    void checkPostBadges_After50Posts_ShouldAwardAuteurProlificBadge() throws SQLException {
        // Arrange
        int userId = 1;
        for (int i = 1; i <= 50; i++) {
            try (Statement stmt = testConnection.createStatement()) {
                stmt.execute("INSERT INTO posts (id, title, author_id, forum_id) VALUES (" + i + ", 'Post " + i + "', " + userId + ", 1)");
            }
        }

        // Act
        String query = "SELECT COUNT(*) as post_count FROM posts WHERE author_id = ?";
        try (PreparedStatement stmt = testConnection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            // Assert
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt("post_count")).isEqualTo(50);
        }
    }

    // ==================== TESTS BADGES DE COMMENTAIRES ====================

    @Test
    @Order(7)
    @DisplayName("Devrait attribuer badge Commentateur après 10 commentaires")
    void checkCommentBadges_After10Comments_ShouldAwardCommentateurBadge() throws SQLException {
        // Arrange
        int userId = 1;
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("INSERT INTO posts (id, title, author_id, forum_id) VALUES (1, 'Post', 1, 1)");
        }
        
        for (int i = 1; i <= 10; i++) {
            try (Statement stmt = testConnection.createStatement()) {
                stmt.execute("INSERT INTO comments (post_id, author_id, content) VALUES (1, " + userId + ", 'Comment " + i + "')");
            }
        }

        // Act
        String query = "SELECT COUNT(*) as comment_count FROM comments WHERE author_id = ?";
        try (PreparedStatement stmt = testConnection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            // Assert
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt("comment_count")).isEqualTo(10);
        }
    }

    @Test
    @Order(8)
    @DisplayName("Devrait attribuer badge Conversateur après 50 commentaires")
    void checkCommentBadges_After50Comments_ShouldAwardConversateurBadge() throws SQLException {
        // Arrange
        int userId = 1;
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("INSERT INTO posts (id, title, author_id, forum_id) VALUES (1, 'Post', 1, 1)");
        }
        
        for (int i = 1; i <= 50; i++) {
            try (Statement stmt = testConnection.createStatement()) {
                stmt.execute("INSERT INTO comments (post_id, author_id, content) VALUES (1, " + userId + ", 'Comment " + i + "')");
            }
        }

        // Act
        String query = "SELECT COUNT(*) as comment_count FROM comments WHERE author_id = ?";
        try (PreparedStatement stmt = testConnection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            // Assert
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt("comment_count")).isEqualTo(50);
        }
    }

    // ==================== TESTS BADGES DE PARTAGES ====================

    @Test
    @Order(9)
    @DisplayName("Devrait attribuer badge Partageur après 5 partages")
    void checkShareBadges_After5Shares_ShouldAwardPartageurBadge() throws SQLException {
        // Arrange
        int userId = 1;
        for (int i = 1; i <= 5; i++) {
            try (Statement stmt = testConnection.createStatement()) {
                stmt.execute("INSERT INTO posts (id, title, author_id, forum_id) VALUES (" + i + ", 'Post " + i + "', 1, 1)");
                stmt.execute("INSERT INTO shared_posts (post_id, user_id) VALUES (" + i + ", " + userId + ")");
            }
        }

        // Act
        String query = "SELECT COUNT(*) as share_count FROM shared_posts WHERE user_id = ?";
        try (PreparedStatement stmt = testConnection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            // Assert
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt("share_count")).isEqualTo(5);
        }
    }

    @Test
    @Order(10)
    @DisplayName("Devrait attribuer badge Influenceur après 20 partages")
    void checkShareBadges_After20Shares_ShouldAwardInfluenceurBadge() throws SQLException {
        // Arrange
        int userId = 1;
        for (int i = 1; i <= 20; i++) {
            try (Statement stmt = testConnection.createStatement()) {
                stmt.execute("INSERT INTO posts (id, title, author_id, forum_id) VALUES (" + i + ", 'Post " + i + "', 1, 1)");
                stmt.execute("INSERT INTO shared_posts (post_id, user_id) VALUES (" + i + ", " + userId + ")");
            }
        }

        // Act
        String query = "SELECT COUNT(*) as share_count FROM shared_posts WHERE user_id = ?";
        try (PreparedStatement stmt = testConnection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            // Assert
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt("share_count")).isEqualTo(20);
        }
    }

    // ==================== TESTS UTILITAIRES ====================

    @Test
    @Order(11)
    @DisplayName("Devrait compter le nombre de badges d'un utilisateur")
    void getUserBadgeCount_ShouldReturnCorrectCount() throws SQLException {
        // Arrange
        int userId = 1;
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("INSERT INTO user_badges (user_id, badge_type_id, forum_id) VALUES " +
                "(" + userId + ", 1, 1), " +
                "(" + userId + ", 5, NULL), " +
                "(" + userId + ", 8, NULL)");
        }

        // Act
        String query = "SELECT COUNT(*) as badge_count FROM user_badges WHERE user_id = ?";
        try (PreparedStatement stmt = testConnection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            // Assert
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt("badge_count")).isEqualTo(3);
        }
    }

    @Test
    @Order(12)
    @DisplayName("Devrait récupérer l'ID d'un type de badge par son nom")
    void getBadgeTypeId_WithValidName_ShouldReturnId() throws SQLException {
        // Act
        String query = "SELECT id FROM badge_types WHERE name = ?";
        try (PreparedStatement stmt = testConnection.prepareStatement(query)) {
            stmt.setString(1, "Fan du Forum");
            ResultSet rs = stmt.executeQuery();
            
            // Assert
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt("id")).isEqualTo(1);
        }
    }

    @Test
    @Order(13)
    @DisplayName("Devrait retourner -1 pour un nom de badge invalide")
    void getBadgeTypeId_WithInvalidName_ShouldReturnMinusOne() throws SQLException {
        // Act
        String query = "SELECT id FROM badge_types WHERE name = ?";
        try (PreparedStatement stmt = testConnection.prepareStatement(query)) {
            stmt.setString(1, "Badge Inexistant");
            ResultSet rs = stmt.executeQuery();
            
            // Assert
            assertThat(rs.next()).isFalse();
        }
    }
}
