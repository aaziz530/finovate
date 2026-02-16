package org.esprit.finovate.entities;

import java.util.Date;
import java.security.SecureRandom;

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
    private Long numeroCarte;
    private Date birthdate;
    private String cardNumber;

    public User() {

    }

    public User(String email, String password, String firstName, String lastName, Date birthdate, String cardNumber) {
        this.email = email;
        this.password = password;
        this.firstname = firstName;
        this.lastname = lastName;
        this.birthdate = birthdate;
        this.cardNumber = cardNumber;

        this.role = "USER";
        this.points = 0;
        this.solde = 0;
        this.createdAt = new Date();
        this.numeroCarte = generateMastercardNumber();
    }

    public User(Long id, String email, String password, String firstName, String lastName, String role, int points,
            Date createdAt, float solde, Long numeroCarte, Date birthdate, String cardNumber) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.firstname = firstName;
        this.lastname = lastName;
        this.role = role;
        this.points = points;
        this.createdAt = createdAt;
        this.solde = solde;
        this.numeroCarte = numeroCarte;
        this.birthdate = birthdate;
        this.cardNumber = cardNumber;
    }

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

    public Long getNumeroCarte() {
        return numeroCarte;
    }

    public void setNumeroCarte(Long numeroCarte) {
        this.numeroCarte = numeroCarte;
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
                ", numeroCarte=" + numeroCarte +
                ", birthdate=" + birthdate +
                ", cardNumber='" + cardNumber + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
