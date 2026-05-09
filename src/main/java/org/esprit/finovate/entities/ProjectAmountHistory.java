package org.esprit.finovate.entities;

import java.util.Date;

/** Maps to {@code project_amount_history}: id, project_id, amount, recorded_at */
public class ProjectAmountHistory {
    private Long id;
    private Long project_id;
    private double amount;
    private Date recorded_at;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProject_id() { return project_id; }
    public void setProject_id(Long project_id) { this.project_id = project_id; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public Date getRecorded_at() { return recorded_at; }
    public void setRecorded_at(Date recorded_at) { this.recorded_at = recorded_at; }
}
