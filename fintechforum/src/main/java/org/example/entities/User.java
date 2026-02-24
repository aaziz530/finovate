package org.example.entities;

import java.security.SecureRandom;
import java.util.Date;

public class User {
    private Long id;
    private String email;
    private String password;
    private String firstname;
    private String lastname;
    private String role;
    private int points;
    private Date createdAt;
    private float solde;
    private Date birthdate;
    private Long numeroCarte;
    private String cinNumber;
    private boolean isBlocked;

    // Constructeur vide
    public User() {
    }

    // Constructeur pour création (comme dans git pull)
    public User(String email, String password, String firstName, String lastName, Date birthdate,
                String cinNumber) {
        this.email = email;
        this.password = password;
        this.firstname = firstName;
        this.lastname = lastName;
        this.birthdate = birthdate;
        this.cinNumber = cinNumber;

        this.role = "USER";
        this.points = 0;
        this.solde = 500;
        this.createdAt = new Date();
        this.numeroCarte = generateMastercardNumber();
        this.isBlocked = false;
    }

    // Constructeur complet
    public User(Long id, String email, String password, String firstName, String lastName, String role, int points,
                Date createdAt, float solde, Date birthdate, Long numeroCarte, String cinNumber) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.firstname = firstName;
        this.lastname = lastName;
        this.role = role;
        this.points = points;
        this.createdAt = createdAt;
        this.solde = solde;
        this.birthdate = birthdate;
        this.numeroCarte = numeroCarte;
        this.cinNumber = cinNumber;
        this.isBlocked = false;
    }

    // Génération de numéro Mastercard
    private static Long generateMastercardNumber() {
        SecureRandom random = new SecureRandom();

        String prefix;
        if (random.nextBoolean()) {
            prefix = String.valueOf(51 + random.nextInt(5));
        } else {
            prefix = String.valueOf(2221 + random.nextInt(2720 - 2221 + 1));
        }

        StringBuilder sb = new StringBuilder(prefix);
        while (sb.length() < 15) {
            sb.append(random.nextInt(10));
        }

        int checkDigit = luhnCheckDigit(sb.toString());
        sb.append(checkDigit);

        return Long.parseLong(sb.toString());
    }

    private static int luhnCheckDigit(String numberWithoutCheckDigit) {
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = numberWithoutCheckDigit.length() - 1; i >= 0; i--) {
            int d = numberWithoutCheckDigit.charAt(i) - '0';
            if (doubleDigit) {
                d *= 2;
                if (d > 9)
                    d -= 9;
            }
            sum += d;
            doubleDigit = !doubleDigit;
        }
        return (10 - (sum % 10)) % 10;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstname;
    }

    public void setFirstName(String firstName) {
        this.firstname = firstName;
    }

    public String getLastName() {
        return lastname;
    }

    public void setLastName(String lastName) {
        this.lastname = lastName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public float getSolde() {
        return solde;
    }

    public void setSolde(float solde) {
        this.solde = solde;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public Long getNumeroCarte() {
        return numeroCarte;
    }

    public void setNumeroCarte(Long numeroCarte) {
        this.numeroCarte = numeroCarte;
    }

    public String getCinNumber() {
        return cinNumber;
    }

    public void setCinNumber(String cinNumber) {
        this.cinNumber = cinNumber;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", firstName='" + firstname + '\'' +
                ", lastName='" + lastname + '\'' +
                ", role='" + role + '\'' +
                ", points=" + points +
                ", solde=" + solde +
                ", birthdate=" + birthdate +
                ", numeroCarte=" + numeroCarte +
                ", cinNumber='" + cinNumber + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}