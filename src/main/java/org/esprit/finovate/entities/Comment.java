package org.esprit.finovate.entities;

import java.sql.Timestamp;
import java.util.Date;

public class Comment {
    private int id;
    private int post_id;
    private Long author_id;
    private String content;
    private Date created_at;
    private Date updated_at;

    // Constructeur vide
    public Comment() {}

    // Constructeur complet
    public Comment(int id, int post_id, Long author_id, String content, Date created_at, Date updated_at) {
        this.id = id;
        this.post_id = post_id;
        this.author_id = author_id;
        this.content = content;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    // Constructeur pour création (sans ID)
    public Comment(int post_id, Long author_id, String content) {
        this.post_id = post_id;
        this.author_id = author_id;
        this.content = content;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPostId() {
        return post_id;
    }

    public void setPostId(int post_id) {
        this.post_id = post_id;
    }

    public Long getAuthorId() {
        return author_id;
    }

    public void setAuthorId(Long author_id) {
        this.author_id = author_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdatedAt() {
        return updated_at;
    }

    public void setUpdatedAt(Date updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", post_id=" + post_id +
                ", author_id=" + author_id +
                ", content='" + content + '\'' +
                ", created_at=" + created_at +
                '}';
    }
}