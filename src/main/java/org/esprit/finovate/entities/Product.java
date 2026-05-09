package org.esprit.finovate.entities;

public class Product {
    private int id;
    private String name;
    private String description;
    private int pricePoints;
    private String image;
    private int stock;

    public Product(int id, String name, String description, int pricePoints, String image) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.pricePoints = pricePoints;
        this.image = image;
        this.stock = 0;
    }

    public Product(int id, String name, String description, int pricePoints, String image, int stock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.pricePoints = pricePoints;
        this.image = image;
        this.stock = stock;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getPricePoints() { return pricePoints; }
    public String getImage() { return image; }
    public int getStock() { return stock; }

    // Setters
    public void setStock(int stock) { this.stock = stock; }
}
