package org.esprit.finovate.entities;

import java.sql.Timestamp;
import java.util.Date;

public class Vote {
    private int id;
    private Long post_id;
    private Long user_id;
    private String vote_type;
    private Date created_at;

    // Constructeur vide
    public Vote() {}

    // Constructeur complet
    public Vote(int id, Long post_id, Long user_id, String vote_type, Date created_at) {
        this.id = id;
        this.post_id = post_id;
        this.user_id = user_id;
        this.vote_type = vote_type;
        this.created_at = created_at;
    }

    // Constructeur sans ID (pour création)
    public Vote(Long post_id, Long user_id, String vote_type) {
        this.post_id = post_id;
        this.user_id = user_id;
        this.vote_type = vote_type;
        this.created_at = new Date();
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Long getPostId() {
        return post_id;
    }

    public void setPostId(Long post_id) {
        this.post_id = post_id;
    }

    public Long getUserId() {
        return user_id;
    }

    public void setUserId(Long user_id) {
        this.user_id = user_id;
    }

    public String getVoteType() {
        return vote_type;
    }

    public void setVoteType(String vote_type) {
        this.vote_type = vote_type;
    }

    public Date getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(Date created_at) {
        this.created_at = created_at;
    }

    @Override
    public String toString() {
        return "Vote{" +
                "id=" + id +
                ", post_id=" + post_id +
                ", user_id=" + user_id +
                ", vote_type=" + vote_type +
                ", created_at=" + created_at +
                '}';
    }
}