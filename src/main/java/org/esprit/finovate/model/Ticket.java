package org.esprit.finovate.model;

public class Ticket {

    private Long id;
    private String type;
    private String description;
    private String priorite;
    private String statut;

    public Ticket() {}

    public Ticket(String type, String description, String priorite, String statut) {
        this.type = type;
        this.description = description;
        this.priorite = priorite;
        this.statut = statut;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriorite() {
        return priorite;
    }

    public void setPriorite(String priorite) {
        this.priorite = priorite;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return "ID: " + id +
                "\nType: " + type +
                "\nDescription: " + description +
                "\nPriorité: " + priorite +
                "\nStatut: " + statut;
    }
}
