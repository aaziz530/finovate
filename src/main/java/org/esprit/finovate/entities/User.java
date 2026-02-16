package org.esprit.finovate.entities;

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
    private String cardNumber;
    private String cinNumber;

    public User() {

    }

    public User(String email, String password, String firstName, String lastName, Date birthdate, String cardNumber,
            String cinNumber) {
        this.email = email;
        this.password = password;
        this.firstname = firstName;
        this.lastname = lastName;
        this.birthdate = birthdate;
        this.cardNumber = cardNumber;
        this.cinNumber = cinNumber;

        this.role = "USER";
        this.points = 0;
        this.solde = 500;
        this.createdAt = new Date();
    }

    public User(Long id, String email, String password, String firstName, String lastName, String role, int points,
            Date createdAt, float solde, Date birthdate, String cardNumber, String cinNumber) {
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
        this.cardNumber = cardNumber;
        this.cinNumber = cinNumber;
    }

    // private static Long generateMastercardNumber() removed as unused

    // private static int luhnCheckDigit(String numberWithoutCheckDigit) removed as
    // unused

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

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCinNumber() {
        return cinNumber;
    }

    public void setCinNumber(String cinNumber) {
        this.cinNumber = cinNumber;
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
                ", cardNumber='" + cardNumber + '\'' +
                ", cinNumber='" + cinNumber + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
