package org.example.entities;

import java.util.Date;

public class Bill {
    private int id;
    private int idUser;
    private String reference;
    private double amount;
    private Date datePaiement;

    public Bill() {}

    public Bill(int idUser, String reference, double amount, Date datePaiement) {
        this.idUser = idUser;
        this.reference = reference;
        this.amount = amount;
        this.datePaiement = datePaiement;
    }

    public Bill(int id, int idUser, String reference, double amount, Date datePaiement) {
        this.id = id;
        this.idUser = idUser;
        this.reference = reference;
        this.amount = amount;
        this.datePaiement = datePaiement;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Date getDatePaiement() { return datePaiement; }
    public void setDatePaiement(Date datePaiement) { this.datePaiement = datePaiement; }
}
