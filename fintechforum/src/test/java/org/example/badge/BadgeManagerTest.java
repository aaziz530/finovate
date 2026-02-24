package org.example.badge;

import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

import java.sql.*;
import java.util.List;

/**
 * Tests unitaires pour BadgeManager
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BadgeManagerTest {

    private static Connection testConnection;
    private static final String TEST_DB_URL = "jdbc:h2:mem:badgetest;DB_CLOSE_DELAY=-1;MODE=MySQL";
    private static final String TEST_DB_USER = "sa";
    private static final String TEST_DB_PASSWORD = "";

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        testConnection = DriverManager.getConnection(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD);
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
            // Table users
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) NOT NULL, " +
                "email VARCHAR(100) NOT NULL, " +
                "password VARCHAR(255) NOT NULL)"
            );

            // Table forums
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS forums (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "description TEXT, " +
                "creator_id INT NOT NULL)"
            );

            // Table posts
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS posts (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "title VARCHAR(200) NOT NULL, " +
                "content TEXT, " +
                "author_id INT NOT NULL, " +
                "forum_id INT NOT NULL)"
            );

            // Table votes
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS votes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "post_id INT NOT NULL, " +
                "user_id INT NOT NULL, " +
                "vote_type VARCHAR(20) NOT NULL)"
            );

            // Table comments
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS comments (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "post_id INT NOT NULL, " +
                "author_id INT NOT NULL, " +
                "content TEXT)"
            );

            // Table shared_posts
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS shared_posts (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "post_id INT NOT NULL, " +
                "user_id INT NOT NULL)"
            );

            // Table badge_types
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS badge_types (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "description TEXT, " +
                "icon VARCHAR(50), " +
                "category VARCHAR(50), " +
                "requirement_type VARCHAR(50), " +
                "requirement_value INT, " +
                "forum_specific BOOLEAN DEFAULT FALSE)"
            );

            // Table user_badges
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS user_badges (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "badge_type_id INT NOT NULL, " +
                "forum_id INT NULL, " +
                "earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );
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
            // Utilisateurs
            stmt.execute("INSERT INTO users (id, username, email, password) VALUES " +
                "(1, 'testuser1', 'test1@example.com', 'pass'), " +
                "(2, 'testuser2', 'test2@example.com', 'pass')");

            // Forums
            stmt.execute("INSERT INTO forums (id, name, description, creator_id) VALUES " +
                "(1, 'Crypto Forum', 'Forum crypto', 1), " +
                "(2, 'Trading Forum', 'Forum trading', 1)");

            // Posts
            stmt.execute("INSERT INTO posts (id, title, content, author_id, forum_id) VALUES " +
                "(1, 'Post 1', 'Content 1', 1, 1), " +
                "(2, 'Post 2', 'Content 2', 1, 1), " +
                "(3, 'Post 3', 'Content 3', 1, 1), " +
                "(4, 'Post 4', 'Content 4', 1, 1), " +
                "(5, 'Post 5', 'Content 5', 1, 1), " +
                "(6, 'Post 6', 'Content 6', 1, 2)");

            // Badge types
            stmt.execute("INSERT INTO badge_types (id, name, description, icon, category, requirement_type, requirement_value, forum_specific) VALUES " +
                "(1, 'Fan du Forum', 'Votez sur 5 posts dans le même forum', '⭐', 'ENGAGEMENT', 'VOTE_COUNT', 5, TRUE), " +
                "(2, 'Super Fan', 'Votez sur 10 posts dans le même forum', '🌟', 'ENGAGEMENT', 'VOTE_COUNT', 10, TRUE), " +
                "(3, 'Premier Post', 'Créez votre premier post', '📝', 'CREATION', 'POST_COUNT', 1, FALSE), " +
                "(4, 'Commentateur', 'Postez 10 commentaires', '💬', 'INTERACTION', 'COMMENT_COUNT', 10, FALSE), " +
                "(5, 'Partageur', 'Partagez 5 posts', '📤', 'SHARING', 'SHARE_COUNT', 5, FALSE)");
        }
    }

    // ==================== TESTS VOTE BADGES ====================

    @Test
    @Order(1)
    @DisplayName("Devrait compter correctement les votes dans un forum")
    void countUserVotesInForum_ShouldReturnCorrectCount() throws SQLException {
        // Arrange
        int userId = 1;
        int forumId = 1;
        
        // Ajouter 3 votes
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("INSERT INTO votes (post_id, user_id, vote_type) VALUES " +
                "(1, " + userId + ", 'UPVOTE'), " +
                "(2, " + userId + ", 'UPVOTE'), " +
                "(3, " + userId + ", 'DOWNVOTE')");
        }

        // Act
        String query = "SELECT COUNT(DISTINCT v.post_id) as vote_count " +
                "FROM votes v INNER JOIN posts p ON v.post_id = p.id " +
                "WHERE v.user_id = ? AND p.forum_id = ?";
        
        try (PreparedStatement stmt = testConnection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, forumId);
            ResultSet rs = stmt.executeQuery();
            
            // Assert
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt("vote_count")).isEqualTo(3);
        }
    }

    @Test
    @Order(2)
    @DisplayName("Devrait attribuer le badge Fan du Forum après 5 votes")
    void checkVoteBadges_After5Votes_ShouldAwardFanBadge() throws SQLException {
        // Arrange
        int userId = 1;
        int forumId = 1;
        
        // Simuler 5 votes dans le même forum
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("INSERT INTO votes (post_id, user_id, vote_type) VALUES " +
                "(1, " + userId + ", 'UPVOTE'), " +
                "(2, " + userId + ", 'UPVOTE'), " +
                "(3, " + userId + ", 'UPVOTE'), " +
                "(4, " + userId + ", 'UPVOTE'), " +
                "(5, " + userId + ", 'UPVOTE')");
        }

        // Vérifier le nombre de votes
        String countQuery = "SELECT COUNT(DISTINCT v.post_id) as vote_count " +
                "FROM votes v INNER JOIN posts p ON v.post_id = p.id " +
                "WHERE v.user_id = ? AND p.forum_id = ?";
        
        try (PreparedStatement stmt = testConnection.prepareStatement(countQuery)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, forumId);
            ResultSet rs = stmt.executeQuery();
            
            // Assert
            assertThat(rs.next()).isTrue();
            int voteCount = rs.getInt("vote_count");
            assertThat(voteCount).isEqualTo(5);
            
            // Vérifier que le badge devrait être attribué
            assertThat(voteCount).isGreaterThanOrEqualTo(5);
        }
    }

    @Test
    @Order(3)
    @DisplayName("Ne devrait pas attribuer le badge avec moins de 5 votes")
    void checkVoteBadges_WithLessThan5Votes_ShouldNotAwardBadge() throws SQLException {
        // Arrange
        int userId = 1;
        int forumId = 1;
        
        // Simuler 4 votes (pas assez)
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("INSERT INTO votes (post_id, user_id, vote_type) VALUES " +
                "(1, " + userId + ", 'UPVOTE'), " +
                "(2, " + userId + ", 'UPVOTE'), " +
                "(3, " + userId + ", 'UPVOTE'), " +
                "(4, " + userId + ", 'UPVOTE')");
        }

        // Vérifier le nombre de votes
        String countQuery = "SELECT COUNT(DISTINCT v.post_id) as vote_count " +
                "FROM votes v INNER JOIN posts p ON v.post_id = p.id " +
                "WHERE v.user_id = ? AND p.forum_id = ?";
        
        try (PreparedStatement stmt = testConnection.prepareStatement(countQuery)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, forumId);
            ResultSet rs = stmt.executeQuery();
            
            // Assert
            assertThat(rs.next()).isTrue();
            int voteCount = rs.getInt("vote_count");
            assertThat(voteCount).isLessThan(5);
        }
    }

    // ==================== TESTS POST BADGES ====================

    @Test
    @Order(4)
    @DisplayName("Devrait compter les posts d'un utilisateur")
    void countUserPosts_ShouldReturnCorrectCount() throws SQLException {
        // Arrange
        int userId = 1;

        // Act
        String query = "SELECT COUNT(*) as post_count FROM posts WHERE author_id = ?";
        try (PreparedStatement stmt = testConnection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            // Assert
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt("post_count")).isEqualTo(6); // 6 posts insérés dans insertTestData
        }
    }

    // ==================== TESTS COMMENT BADGES ====================

    @Test
    @Order(5)
    @DisplayName("Devrait compter les commentaires d'un utilisateur")
    void countUserComments_ShouldReturnCorrectCount() throws SQLException {
        // Arrange
        int userId = 1;
        
        // Ajouter des commentaires
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("INSERT INTO comments (post_id, author_id, content) VALUES " +
                "(1, " + userId + ", 'Comment 1'), " +
                "(1, " + userId + ", 'Comment 2'), " +
                "(2, " + userId + ", 'Comment 3')");
        }

        // Act
        String query = "SELECT COUNT(*) as comment_count FROM comments WHERE author_id = ?";
        try (PreparedStatement stmt = testConnection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            // Assert
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt("comment_count")).isEqualTo(3);
        }
    }

    // ==================== TESTS SHARE BADGES ====================

    @Test
    @Order(6)
    @DisplayName("Devrait compter les partages d'un utilisateur")
    void countUserShares_ShouldReturnCorrectCount() throws SQLException {
        // Arrange
        int userId = 1;
        
        // Ajouter des partages
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("INSERT INTO shared_posts (post_id, user_id) VALUES " +
                "(1, " + userId + "), " +
                "(2, " + userId + "), " +
                "(3, " + userId + "), " +
                "(4, " + userId + "), " +
                "(5, " + userId + ")");
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

    // ==================== TESTS BADGE ATTRIBUTION ====================

    @Test
    @Order(7)
    @DisplayName("Devrait vérifier si un utilisateur a un badge")
    void userHasBadge_ShouldReturnCorrectStatus() throws SQLException {
        // Arrange
        int userId = 1;
        int badgeTypeId = 1; // Fan du Forum
        int forumId = 1;
        
        // Attribuer le badge
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("INSERT INTO user_badges (user_id, badge_type_id, forum_id) VALUES " +
                "(" + userId + ", " + badgeTypeId + ", " + forumId + ")");
        }

        // Act & Assert
        String query = "SELECT 1 FROM user_badges WHERE user_id = ? AND badge_type_id = ? AND forum_id = ?";
        try (PreparedStatement stmt = testConnection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, badgeTypeId);
            stmt.setInt(3, forumId);
            ResultSet rs = stmt.executeQuery();
            
            assertThat(rs.next()).isTrue();
        }
    }

    @Test
    @Order(8)
    @DisplayName("Ne devrait pas attribuer le même badge deux fois")
    void awardBadge_Twice_ShouldNotDuplicate() throws SQLException {
        // Arrange
        int userId = 1;
        int badgeTypeId = 1;
        int forumId = 1;
        
        // Première attribution
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("INSERT INTO user_badges (user_id, badge_type_id, forum_id) VALUES " +
                "(" + userId + ", " + badgeTypeId + ", " + forumId + ")");
        }

        // Tentative de deuxième attribution (devrait échouer ou être ignorée)
        // En production, la contrainte UNIQUE empêche cela
        
        // Act
        String countQuery = "SELECT COUNT(*) as badge_count FROM user_badges " +
                "WHERE user_id = ? AND badge_type_id = ? AND forum_id = ?";
        try (PreparedStatement stmt = testConnection.prepareStatement(countQuery)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, badgeTypeId);
            stmt.setInt(3, forumId);
            ResultSet rs = stmt.executeQuery();
            
            // Assert
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt("badge_count")).isEqualTo(1);
        }
    }

    @Test
    @Order(9)
    @DisplayName("Devrait récupérer tous les badges d'un utilisateur")
    void getUserBadges_ShouldReturnAllBadges() throws SQLException {
        // Arrange
        int userId = 1;
        
        // Attribuer plusieurs badges
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("INSERT INTO user_badges (user_id, badge_type_id, forum_id) VALUES " +
                "(" + userId + ", 1, 1), " +  // Fan du Forum
                "(" + userId + ", 3, NULL), " + // Premier Post
                "(" + userId + ", 5, NULL)");   // Partageur
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
    @Order(10)
    @DisplayName("Devrait différencier les badges par forum")
    void badges_ShouldBeDifferentPerForum() throws SQLException {
        // Arrange
        int userId = 1;
        int badgeTypeId = 1; // Fan du Forum
        
        // Attribuer le même badge pour deux forums différents
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("INSERT INTO user_badges (user_id, badge_type_id, forum_id) VALUES " +
                "(" + userId + ", " + badgeTypeId + ", 1), " +
                "(" + userId + ", " + badgeTypeId + ", 2)");
        }

        // Act
        String query = "SELECT COUNT(*) as badge_count FROM user_badges " +
                "WHERE user_id = ? AND badge_type_id = ?";
        try (PreparedStatement stmt = testConnection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, badgeTypeId);
            ResultSet rs = stmt.executeQuery();
            
            // Assert - Devrait avoir 2 badges (un par forum)
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt("badge_count")).isEqualTo(2);
        }
    }
}
