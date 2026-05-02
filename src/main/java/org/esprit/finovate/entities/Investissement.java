package org.esprit.finovate.entities;

import java.util.Date;

/**
 * Maps to table {@code investissement}.
 * DB columns: {@code id}, {@code user_id}, {@code project_id}, …
 * Java keeps legacy names {@link #investissement_id} and {@link #investor_id} mapped from {@code id} and {@code user_id}.
 */
public class Investissement {

    private Long investissement_id;
    private Long project_id;
    /** Maps to DB column {@code user_id} */
    private Long investor_id;
    private double amount;
    private Date investment_date;
    private String status;
    private Double revenuePercentage;

    public Investissement() {
    }

    public Investissement(Long project_id, Long investor_id, double amount, double revenuePercentage) {
        this.project_id = project_id;
        this.investor_id = investor_id;
        this.amount = amount;
        this.revenuePercentage = revenuePercentage;
        this.status = "CONFIRMED";
        this.investment_date = new Date();
    }

    public Long getInvestissement_id() {
        return investissement_id;
    }

    public void setInvestissement_id(Long investissement_id) {
        this.investissement_id = investissement_id;
    }

    public Long getProject_id() {
        return project_id;
    }

    public void setProject_id(Long project_id) {
        this.project_id = project_id;
    }

    /** Same as {@link #getUser_id()} — DB column {@code user_id} */
    public Long getInvestor_id() {
        return investor_id;
    }

    public void setInvestor_id(Long investor_id) {
        this.investor_id = investor_id;
    }

    public Long getUser_id() {
        return investor_id;
    }

    public void setUser_id(Long user_id) {
        this.investor_id = user_id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getInvestment_date() {
        return investment_date;
    }

    public void setInvestment_date(Date investment_date) {
        this.investment_date = investment_date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getRevenuePercentage() {
        return revenuePercentage != null ? revenuePercentage : 0d;
    }

    public void setRevenuePercentage(double revenuePercentage) {
        this.revenuePercentage = revenuePercentage;
    }
}
