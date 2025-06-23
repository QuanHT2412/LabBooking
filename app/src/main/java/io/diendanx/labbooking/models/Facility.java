package io.diendanx.labbooking.models;

public class Facility {
    private String id;
    private String name;
    private String description;
    private double price;

    public Facility() {}

    public Facility(String name, String description, double price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
