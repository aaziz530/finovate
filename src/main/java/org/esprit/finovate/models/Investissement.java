package org.esprit.finovate.models;

import java.util.Date;

public class Investissement {
    private Long investissement_id;
    private Long project_id;
    private Long investor_id;
    private double amount;
    private Date investment_date;
    private String status;

    public Investissement() {}

    public Investissement(Long project_id, Long investor_id, double amount) {
        this.project_id = project_id;
        this.investor_id = investor_id;
        this.amount = amount;
        this.status = "CONFIRMED";
        this.investment_date = new Date();
    }

    public Long getInvestissement_id() { return investissement_id; }
    public void setInvestissement_id(Long investissement_id) { this.investissement_id = investissement_id; }
    public Long getProject_id() { return project_id; }
    public void setProject_id(Long project_id) { this.project_id = project_id; }
    public Long getInvestor_id() { return investor_id; }
    public void setInvestor_id(Long investor_id) { this.investor_id = investor_id; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public Date getInvestment_date() { return investment_date; }
    public void setInvestment_date(Date investment_date) { this.investment_date = investment_date; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
