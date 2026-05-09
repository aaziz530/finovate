package org.esprit.finovate.entities;

import java.util.Date;

public class Goal {
    private Long id;
    private Long id_user;
    private String title;
    private String target_amount;
    private String current_amount;
    private Date deadline;
    private String status;
    private Date created_at;

    public Goal() {
    }

    public Goal(Long id_user, String title, String target_amount, Date deadline) {
        this.id_user = id_user;
        this.title = title;
        this.target_amount = target_amount;
        this.current_amount = "0";
        this.deadline = deadline;
        this.status = "In Progress";
        this.created_at = new Date();
    }

    public Goal(Long id, Long id_user, String title, String target_amount, String current_amount, Date deadline, String status,
            Date created_at) {
        this.id = id;
        this.id_user = id_user;
        this.title = title;
        this.target_amount = target_amount;
        this.current_amount = current_amount;
        this.deadline = deadline;
        this.status = status;
        this.created_at = created_at;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdUser() {
        return id_user;
    }

    public void setIdUser(Long id_user) {
        this.id_user = id_user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTargetAmount() {
        return target_amount;
    }

    public void setTargetAmount(String target_amount) {
        this.target_amount = target_amount;
    }

    public String getCurrentAmount() {
        return current_amount;
    }

    public void setCurrentAmount(String current_amount) {
        this.current_amount = current_amount;
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
        return created_at;
    }

    public void setCreatedAt(Date created_at) {
        this.created_at = created_at;
    }

    public float getProgress() {
        float target = Float.parseFloat(target_amount.isEmpty() ? "0" : target_amount);
        float current = Float.parseFloat(current_amount.isEmpty() ? "0" : current_amount);
        if (target == 0)
            return 0;
        return Math.min(1.0f, current / target);
    }

    /**
     * Calcule le montant mensuel suggéré à épargner pour atteindre l'objectif.
     * @return montant suggéré par mois, ou 0 si deadline passée ou déjà atteint
     */
    public float getSuggestedMonthlySaving() {
        float target = Float.parseFloat(target_amount.isEmpty() ? "0" : target_amount);
        float current = Float.parseFloat(current_amount.isEmpty() ? "0" : current_amount);
        if (target <= current || deadline == null) {
            return 0;
        }

        long diffInMillies = deadline.getTime() - System.currentTimeMillis();
        if (diffInMillies <= 0) {
            return target - current; // Doit être fait immédiatement
        }

        // Convertir en mois (approximatif : 30.44 jours par mois)
        double diffInDays = diffInMillies / (1000.0 * 60 * 60 * 24);
        double diffInMonths = diffInDays / 30.44;

        if (diffInMonths < 1) {
            return target - current; // Moins d'un mois restant
        }

        return (float) ((target - current) / diffInMonths);
    }
}
