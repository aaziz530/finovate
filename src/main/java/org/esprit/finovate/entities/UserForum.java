package org.esprit.finovate.entities;

import java.sql.Timestamp;
import java.util.Date;

public class UserForum {
    private int id;
    private Long user_id;
    private Long forum_id;
    private Date joined_at;

    // Constructeur vide
    public UserForum() {}

    // Constructeur complet
    public UserForum(int id, Long user_id, Long forum_id, Date joined_at) {
        this.id = id;
        this.user_id = user_id;
        this.forum_id = forum_id;
        this.joined_at = joined_at;
    }

    // Constructeur sans ID (pour création)
    public UserForum(Long user_id, Long forum_id) {
        this.user_id = user_id;
        this.forum_id = forum_id;
        this.joined_at = new Date();
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Long getUserId() {
        return user_id;
    }

    public void setUserId(Long user_id) {
        this.user_id = user_id;
    }

    public Long getForumId() {
        return forum_id;
    }

    public void setForumId(Long forum_id) {
        this.forum_id = forum_id;
    }

    public Date getJoinedAt() {
        return joined_at;
    }

    public void setJoinedAt(Date joined_at) {
        this.joined_at = joined_at;
    }

    @Override
    public String toString() {
        return "UserForum{" +
                "id=" + id +
                ", user_id=" + user_id +
                ", forum_id=" + forum_id +
                ", joined_at=" + joined_at +
                '}';
    }
}