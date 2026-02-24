package org.example.entities;

import java.sql.Timestamp;

public class Vote {
    private int id;
    private int postId;
    private int userId;
    private VoteType voteType;
    private Timestamp createdAt;

    public enum VoteType {
        UPVOTE, DOWNVOTE
    }

    // Constructeur vide
    public Vote() {}

    // Constructeur complet
    public Vote(int id, int postId, int userId, VoteType voteType, Timestamp createdAt) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.voteType = voteType;
        this.createdAt = createdAt;
    }

    // Constructeur sans ID (pour création)
    public Vote(int postId, int userId, VoteType voteType) {
        this.postId = postId;
        this.userId = userId;
        this.voteType = voteType;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public VoteType getVoteType() {
        return voteType;
    }

    public void setVoteType(VoteType voteType) {
        this.voteType = voteType;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Vote{" +
                "id=" + id +
                ", postId=" + postId +
                ", userId=" + userId +
                ", voteType=" + voteType +
                ", createdAt=" + createdAt +
                '}';
    }
}