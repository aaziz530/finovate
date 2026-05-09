package org.esprit.finovate.entities;

import java.sql.Timestamp;
import java.util.Date;

 public class Forum {
    private int id;
    private Long creator_id;
    private String title;
    private String description;
    private String image_url;
    private Date created_at;

    // Constructors
    public Forum() {}

    public Forum(Long creator_id, String title, String description) {
        this.creator_id = creator_id;
        this.title = title;
        this.description = description;
        this.created_at = new Date();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Long getCreatorId() {
        return creator_id;
    }

    public void setCreatorId(Long creator_id) {
        this.creator_id = creator_id;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() {
        return image_url;
    }

    public void setImageUrl(String image_url) {
        this.image_url = image_url;
    }

    public Date getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(Date created_at) {
        this.created_at = created_at;
    }

    @Override
    public String toString() {
        return "Forum{id=" + id + ", title='" + title + "', creator=" + creator_id + "}";
    }
}
