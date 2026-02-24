package org.example.services;

import org.example.entities.Vote;
import org.example.utils.Databaseconnection;
import org.example.utils.Databaseconnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoteService {

    /**
     * CREATE/UPDATE - Voter sur un post
     * Si le vote existe déjà, on le met à jour
     * Contrôles: Utilisateur ne doit pas être bloqué
     */
    public boolean vote(Vote vote, boolean isUserBlocked) throws SQLException {
        if (isUserBlocked) {
            throw new SecurityException("Utilisateur bloqué: impossible de voter");
        }

        // Vérifier si le vote existe déjà
        Vote existingVote = getVoteByUserAndPost(vote.getUserId(), vote.getPostId());

        if (existingVote != null) {
            // Mise à jour du vote
            if (existingVote.getVoteType() == vote.getVoteType()) {
                // Même vote: on le supprime (toggle)
                return deleteVote(existingVote.getId());
            } else {
                // Vote différent: on le change
                return updateVote(existingVote.getId(), vote.getVoteType());
            }
        } else {
            // Créer un nouveau vote
            return createVote(vote);
        }
    }

    /**
     * CREATE - Créer un vote
     */
    private boolean createVote(Vote vote) throws SQLException {
        String query = "INSERT INTO votes (post_id, user_id, vote_type) VALUES (?, ?, ?)";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, vote.getPostId());
            stmt.setInt(2, vote.getUserId());
            stmt.setString(3, vote.getVoteType().name());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    vote.setId(generatedKeys.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    /**
     * UPDATE - Modifier un vote
     */
    private boolean updateVote(int voteId, Vote.VoteType newVoteType) throws SQLException {
        String query = "UPDATE votes SET vote_type = ? WHERE id = ?";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newVoteType.name());
            stmt.setInt(2, voteId);

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * DELETE - Supprimer un vote
     */
    private boolean deleteVote(int voteId) throws SQLException {
        String query = "DELETE FROM votes WHERE id = ?";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, voteId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * READ - Récupérer un vote par utilisateur et post
     */
    public Vote getVoteByUserAndPost(int userId, int postId) throws SQLException {
        String query = "SELECT * FROM votes WHERE user_id = ? AND post_id = ?";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, postId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToVote(rs);
            }
            return null;
        }
    }

    /**
     * READ - Récupérer tous les votes d'un post
     */
    public List<Vote> getVotesByPost(int postId) throws SQLException {
        List<Vote> votes = new ArrayList<>();
        String query = "SELECT * FROM votes WHERE post_id = ?";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                votes.add(mapResultSetToVote(rs));
            }
        }
        return votes;
    }

    /**
     * Calculer le score d'un post (upvotes - downvotes)
     */
    public int getPostScore(int postId) throws SQLException {
        String query = "SELECT " +
                "SUM(CASE WHEN vote_type = 'UPVOTE' THEN 1 ELSE 0 END) as upvotes, " +
                "SUM(CASE WHEN vote_type = 'DOWNVOTE' THEN 1 ELSE 0 END) as downvotes " +
                "FROM votes WHERE post_id = ?";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int upvotes = rs.getInt("upvotes");
                int downvotes = rs.getInt("downvotes");
                return upvotes - downvotes;
            }
            return 0;
        }
    }

    /**
     * Compter les upvotes d'un post
     */
    public int getUpvoteCount(int postId) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM votes WHERE post_id = ? AND vote_type = 'UPVOTE'";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }
            return 0;
        }
    }

    /**
     * Compter les downvotes d'un post
     */
    public int getDownvoteCount(int postId) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM votes WHERE post_id = ? AND vote_type = 'DOWNVOTE'";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }
            return 0;
        }
    }

    /**
     * Mapper ResultSet vers Vote
     */
    private Vote mapResultSetToVote(ResultSet rs) throws SQLException {
        return new Vote(
                rs.getInt("id"),
                rs.getInt("post_id"),
                rs.getInt("user_id"),
                Vote.VoteType.valueOf(rs.getString("vote_type")),
                rs.getTimestamp("created_at")
        );
    }
}
