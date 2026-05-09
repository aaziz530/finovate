package org.esprit.finovate.entities;

public class Ad {
    private int id;
    private String title;
    private String imagePath;
    private int duration;
    private int rewardPoints;

    public Ad(int id, String title, String imagePath, int duration, int rewardPoints) {
        this.id = id;
        this.title = title;
        this.imagePath = imagePath;
        this.duration = duration;
        this.rewardPoints = rewardPoints;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setRewardPoints(int rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    // Getters
    public String getTitle() { return title; }
    public String getImagePath() { return imagePath; }
    public int getDuration() { return duration; }
    public int getRewardPoints() { return rewardPoints; }
}
