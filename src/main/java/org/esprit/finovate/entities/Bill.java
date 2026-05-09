package org.esprit.finovate.entities;

import java.util.Date;

public class Bill {
    private int id;
    private Long id_user;
    private String reference;
    private double amount;
    private Date date_paiement;

    public Bill() {
    }

    public Bill(Long id_user, String reference, double amount, Date date_paiement) {
        this.id_user = id_user;
        this.reference = reference;
        this.amount = amount;
        this.date_paiement = date_paiement;
    }

    public Bill(int id, Long id_user, String reference, double amount, Date date_paiement) {
        this.id = id;
        this.id_user = id_user;
        this.reference = reference;
        this.amount = amount;
        this.date_paiement = date_paiement;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Long getIdUser() {
        return id_user;
    }

    public void setIdUser(Long id_user) {
        this.id_user = id_user;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getDatePaiement() {
        return date_paiement;
    }

    public void setDatePaiement(Date date_paiement) {
        this.date_paiement = date_paiement;
    }
}
