package org.example.entities;

import java.sql.Timestamp;

 public class Forum {
    private Long id;
    private Long idCreator;
    private String title;
    private String description;
    private Timestamp createdAt;

    // Constructors
    public Forum() {}

    public Forum(Long idCreator, String title, String description) {
        this.idCreator = idCreator;
        this.title = title;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdCreator() { return idCreator; }
    public void setIdCreator(Long idCreator) { this.idCreator = idCreator; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Forum{id=" + id + ", title='" + title + "', creator=" + idCreator + "}";
    }
}
