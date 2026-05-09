package org.esprit.finovate.entities;

import java.sql.Timestamp;

public class Post {
    private int id;
    private int forum_id;
    private String title;
    private String content;
    private String image_url;
    private Long author_id;
    private Timestamp created_at;
    private Timestamp updated_at;

    // Constructeur vide
    public Post() {}

    // Constructeur complet
    public Post(int id, int forum_id, String title, String content, Long author_id,
                Timestamp created_at, Timestamp updated_at) {
        this.id = id;
        this.forum_id = forum_id;
        this.title = title;
        this.content = content;
        this.author_id = author_id;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    // Constructeur sans ID (pour création)
    public Post(int forum_id, String title, String content, Long author_id) {
        this.forum_id = forum_id;
        this.title = title;
        this.content = content;
        this.author_id = author_id;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getForumId() {
        return forum_id;
    }

    public void setForumId(int forum_id) {
        this.forum_id = forum_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return image_url;
    }

    public void setImageUrl(String image_url) {
        this.image_url = image_url;
    }

    public Long getAuthorId() {
        return author_id;
    }

    public void setAuthorId(Long author_id) {
        this.author_id = author_id;
    }

    public Timestamp getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(Timestamp created_at) {
        this.created_at = created_at;
    }

    public Timestamp getUpdatedAt() {
        return updated_at;
    }

    public void setUpdatedAt(Timestamp updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", forum_id=" + forum_id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", author_id=" + author_id +
                ", created_at=" + created_at +
                '}';
    }
}