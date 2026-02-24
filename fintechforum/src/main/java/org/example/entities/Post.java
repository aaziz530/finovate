package org.example.entities;

import java.sql.Timestamp;

public class Post {
    private int id;
    private int forumId;
    private String title;
    private String content;
    private int authorId;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Constructeur vide
    public Post() {}

    // Constructeur complet
    public Post(int id, int forumId, String title, String content, int authorId,
                Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.forumId = forumId;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructeur sans ID (pour création)
    public Post(int forumId, String title, String content, int authorId) {
        this.forumId = forumId;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getForumId() {
        return forumId;
    }

    public void setForumId(int forumId) {
        this.forumId = forumId;
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

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", forumId=" + forumId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", authorId=" + authorId +
                ", createdAt=" + createdAt +
                '}';
    }
}