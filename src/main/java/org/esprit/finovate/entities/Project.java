package org.esprit.finovate.entities;

import java.util.Date;

/**
 * Maps to table {@code project} (schema {@code finovate}).
 * DB PK column is {@code id}; exposed as {@link #project_id} for application compatibility.
 */
public class Project {

    private Long project_id;
    private String title;
    private String description;
    private double goal_amount;
    private double current_amount;
    private Date created_at;
    private Date deadline;
    private String status;
    private Long owner_id;
    private String imagePath;
    private Double latitude;
    private Double longitude;
    private String category;
    /** DB column: {@code funding_completed_at} */
    private Date funding_completed_at;

    public Project() {
    }

    public Project(String title, String description, double goal_amount, double current_amount,
            Date created_at, Date deadline, String status, Long owner_id) {
        this.title = title;
        this.description = description;
        this.goal_amount = goal_amount;
        this.current_amount = current_amount;
        this.created_at = created_at;
        this.deadline = deadline;
        this.status = status;
        this.owner_id = owner_id;
    }

    public Long getProject_id() {
        return project_id;
    }

    public void setProject_id(Long project_id) {
        this.project_id = project_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getGoal_amount() {
        return goal_amount;
    }

    public void setGoal_amount(double goal_amount) {
        this.goal_amount = goal_amount;
    }

    public double getCurrent_amount() {
        return current_amount;
    }

    public void setCurrent_amount(double current_amount) {
        this.current_amount = current_amount;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
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

    public Long getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(Long owner_id) {
        this.owner_id = owner_id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getFunding_completed_at() {
        return funding_completed_at;
    }

    public void setFunding_completed_at(Date funding_completed_at) {
        this.funding_completed_at = funding_completed_at;
    }

    /** @deprecated use {@link #getFunding_completed_at()} — kept for older call sites */
    @Deprecated
    public Date getFunding_completed_date() {
        return funding_completed_at;
    }

    /** @deprecated use {@link #setFunding_completed_at(Date)} */
    @Deprecated
    public void setFunding_completed_date(Date d) {
        this.funding_completed_at = d;
    }
}
