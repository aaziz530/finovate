package org.esprit.finovate.entities;

import java.util.Date;

public class Goal {
    private int id;
    private int idUser;
    private String title;
    private float targetAmount;
    private float currentAmount;
    private Date deadline;
    private String status;
    private Date createdAt;

    public Goal() {
    }

    public Goal(int idUser, String title, float targetAmount, Date deadline) {
        this.idUser = idUser;
        this.title = title;
        this.targetAmount = targetAmount;
        this.currentAmount = 0;
        this.deadline = deadline;
        this.status = "In Progress";
        this.createdAt = new Date();
    }

    public Goal(int id, int idUser, String title, float targetAmount, float currentAmount, Date deadline, String status,
            Date createdAt) {
        this.id = id;
        this.idUser = idUser;
        this.title = title;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.deadline = deadline;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public float getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(float targetAmount) {
        this.targetAmount = targetAmount;
    }

    public float getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(float currentAmount) {
        this.currentAmount = currentAmount;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public float getProgress() {
        if (targetAmount == 0)
            return 0;
        return Math.min(1.0f, currentAmount / targetAmount);
    }
}
