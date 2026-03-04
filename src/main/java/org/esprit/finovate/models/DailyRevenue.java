package org.esprit.finovate.models;

import java.util.Date;

public class DailyRevenue {
    private Long revenue_id;
    private Long project_id;
    private Date revenue_date;
    private double amount;

    public DailyRevenue() {
    }

    public DailyRevenue(Long project_id, Date revenue_date, double amount) {
        this.project_id = project_id;
        this.revenue_date = revenue_date;
        this.amount = amount;
    }

    public Long getRevenue_id() {
        return revenue_id;
    }

    public void setRevenue_id(Long revenue_id) {
        this.revenue_id = revenue_id;
    }

    public Long getProject_id() {
        return project_id;
    }

    public void setProject_id(Long project_id) {
        this.project_id = project_id;
    }

    public Date getRevenue_date() {
        return revenue_date;
    }

    public void setRevenue_date(Date revenue_date) {
        this.revenue_date = revenue_date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
