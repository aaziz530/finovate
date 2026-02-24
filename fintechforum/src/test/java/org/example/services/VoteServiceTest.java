package org.example.services;

import org.example.entities.Vote;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

import java.sql.*;
import java.util.List;

/**
 * Tests unitaires pour VoteService
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class VoteServiceTest {

    private static Connection testConnection;
    private static VoteService voteService;
    private static final String TEST_DB_URL = "jdbc:h2:mem:votetest;DB_CLOSE_DELAY=-1;MODE=MySQL";

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        testConnection = DriverManager.getConnection(TEST_DB_URL, "sa", "");
        createTables();
        voteService = new VoteService();
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
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS posts (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "title VARCHAR(200) NOT NULL, " +
                "content TEXT NOT NULL, " +
                "author_id BIGINT NOT NULL)"
            );
            
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS votes (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "post_id BIGINT NOT NULL, " +
                "user_id BIGINT NOT NULL, " +
                "vote_type VARCHAR(20) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );
        }
    }

    private void cleanDatabase() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM votes");
            stmt.execute("DELETE FROM posts");
        }
    }

    private void insertTestData() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("INSERT INTO posts (id, title, content, author_id) VALUES (1, 'Test Post', 'Content', 1)");
        }
    }

    // ==================== TESTS CREATE ====================

    @Test
    @Order(1)
    @DisplayName("Devrait créer un vote UPVOTE")
    void vote_CreateUpvote_ShouldReturnTrue() throws SQLException {
        // Arrange
        Vote vote = new Vote(1, 1, Vote.VoteType.UPVOTE);

        // Act
        boolean result = voteService.vote(vote, false);

        // Assert
        assertThat(result).isTrue();
        Vote foundVote = voteService.getVoteByUserAndPost(1, 1);
        assertThat(foundVote).isNotNull();
        assertThat(foundVote.getVoteType()).isEqualTo(Vote.VoteType.UPVOTE);
    }

    @Test
    @Order(2)
    @DisplayName("Devrait créer un vote DOWNVOTE")
    void vote_CreateDownvote_ShouldReturnTrue() throws SQLException {
        // Arrange
        Vote vote = new Vote(1, 2, Vote.VoteType.DOWNVOTE);

        // Act
        boolean result = voteService.vote(vote, false);

        // Assert
        assertThat(result).isTrue();
        Vote foundVote = voteService.getVoteByUserAndPost(2, 1);
        assertThat(foundVote).isNotNull();
        assertThat(foundVote.getVoteType()).isEqualTo(Vote.VoteType.DOWNVOTE);
    }

    @Test
    @Order(3)
    @DisplayName("Devrait rejeter si utilisateur bloqué")
    void vote_WhenUserBlocked_ShouldThrowSecurityException() {
        // Arrange
        Vote vote = new Vote(1, 1, Vote.VoteType.UPVOTE);

        // Act & Assert
        assertThatThrownBy(() -> voteService.vote(vote, true))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("bloqué");
    }

    // ==================== TESTS UPDATE ====================

    @Test
    @Order(4)
    @DisplayName("Devrait changer vote de UPVOTE à DOWNVOTE")
    void vote_ChangeUpvoteToDownvote_ShouldReturnTrue() throws SQLException {
        // Arrange
        Vote upvote = new Vote(1, 1, Vote.VoteType.UPVOTE);
        voteService.vote(upvote, false);
        
        Vote downvote = new Vote(1, 1, Vote.VoteType.DOWNVOTE);

        // Act
        boolean result = voteService.vote(downvote, false);

        // Assert
        assertThat(result).isTrue();
        Vote foundVote = voteService.getVoteByUserAndPost(1, 1);
        assertThat(foundVote.getVoteType()).isEqualTo(Vote.VoteType.DOWNVOTE);
    }

    @Test
    @Order(5)
    @DisplayName("Devrait changer vote de DOWNVOTE à UPVOTE")
    void vote_ChangeDownvoteToUpvote_ShouldReturnTrue() throws SQLException {
        // Arrange
        Vote downvote = new Vote(1, 1, Vote.VoteType.DOWNVOTE);
        voteService.vote(downvote, false);
        
        Vote upvote = new Vote(1, 1, Vote.VoteType.UPVOTE);

        // Act
        boolean result = voteService.vote(upvote, false);

        // Assert
        assertThat(result).isTrue();
        Vote foundVote = voteService.getVoteByUserAndPost(1, 1);
        assertThat(foundVote.getVoteType()).isEqualTo(Vote.VoteType.UPVOTE);
    }

    // ==================== TESTS DELETE (TOGGLE) ====================

    @Test
    @Order(6)
    @DisplayName("Devrait supprimer un vote si on vote deux fois pareil (toggle)")
    void vote_SameVoteTwice_ShouldDeleteVote() throws SQLException {
        // Arrange
        Vote vote1 = new Vote(1, 1, Vote.VoteType.UPVOTE);
        voteService.vote(vote1, false);
        
        Vote vote2 = new Vote(1, 1, Vote.VoteType.UPVOTE);

        // Act
        boolean result = voteService.vote(vote2, false);

        // Assert
        assertThat(result).isTrue();
        Vote foundVote = voteService.getVoteByUserAndPost(1, 1);
        assertThat(foundVote).isNull(); // Vote supprimé
    }

    // ==================== TESTS READ ====================

    @Test
    @Order(7)
    @DisplayName("Devrait récupérer tous les votes d'un post")
    void getVotesByPost_ShouldReturnAllVotes() throws SQLException {
        // Arrange
        voteService.vote(new Vote(1, 1, Vote.VoteType.UPVOTE), false);
        voteService.vote(new Vote(1, 2, Vote.VoteType.UPVOTE), false);
        voteService.vote(new Vote(1, 3, Vote.VoteType.DOWNVOTE), false);

        // Act
        List<Vote> votes = voteService.getVotesByPost(1);

        // Assert
        assertThat(votes).hasSize(3);
    }

    @Test
    @Order(8)
    @DisplayName("Devrait compter les upvotes d'un post")
    void getUpvoteCount_ShouldReturnCorrectCount() throws SQLException {
        // Arrange
        voteService.vote(new Vote(1, 1, Vote.VoteType.UPVOTE), false);
        voteService.vote(new Vote(1, 2, Vote.VoteType.UPVOTE), false);
        voteService.vote(new Vote(1, 3, Vote.VoteType.DOWNVOTE), false);

        // Act
        int upvoteCount = voteService.getUpvoteCount(1);

        // Assert
        assertThat(upvoteCount).isEqualTo(2);
    }

    @Test
    @Order(9)
    @DisplayName("Devrait compter les downvotes d'un post")
    void getDownvoteCount_ShouldReturnCorrectCount() throws SQLException {
        // Arrange
        voteService.vote(new Vote(1, 1, Vote.VoteType.UPVOTE), false);
        voteService.vote(new Vote(1, 2, Vote.VoteType.DOWNVOTE), false);
        voteService.vote(new Vote(1, 3, Vote.VoteType.DOWNVOTE), false);

        // Act
        int downvoteCount = voteService.getDownvoteCount(1);

        // Assert
        assertThat(downvoteCount).isEqualTo(2);
    }

    @Test
    @Order(10)
    @DisplayName("Devrait calculer le score d'un post (upvotes - downvotes)")
    void getPostScore_ShouldReturnCorrectScore() throws SQLException {
        // Arrange
        voteService.vote(new Vote(1, 1, Vote.VoteType.UPVOTE), false);
        voteService.vote(new Vote(1, 2, Vote.VoteType.UPVOTE), false);
        voteService.vote(new Vote(1, 3, Vote.VoteType.UPVOTE), false);
        voteService.vote(new Vote(1, 4, Vote.VoteType.DOWNVOTE), false);

        // Act
        int score = voteService.getPostScore(1);

        // Assert
        assertThat(score).isEqualTo(2); // 3 upvotes - 1 downvote = 2
    }

    @Test
    @Order(11)
    @DisplayName("Devrait retourner 0 pour un post sans votes")
    void getPostScore_WithNoVotes_ShouldReturnZero() throws SQLException {
        // Act
        int score = voteService.getPostScore(1);

        // Assert
        assertThat(score).isEqualTo(0);
    }
}
