package org.example.entities;

import java.sql.Timestamp;

public class UserForum {
    private int id;
    private int userId;
    private int forumId;
    private Timestamp joinedAt;

    // Constructeur vide
    public UserForum() {}

    // Constructeur complet
    public UserForum(int id, int userId, int forumId, Timestamp joinedAt) {
        this.id = id;
        this.userId = userId;
        this.forumId = forumId;
        this.joinedAt = joinedAt;
    }

    // Constructeur sans ID (pour création)
    public UserForum(int userId, int forumId) {
        this.userId = userId;
        this.forumId = forumId;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getForumId() {
        return forumId;
    }

    public void setForumId(int forumId) {
        this.forumId = forumId;
    }

    public Timestamp getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Timestamp joinedAt) {
        this.joinedAt = joinedAt;
    }

    @Override
    public String toString() {
        return "UserForum{" +
                "id=" + id +
                ", userId=" + userId +
                ", forumId=" + forumId +
                ", joinedAt=" + joinedAt +
                '}';
    }
}