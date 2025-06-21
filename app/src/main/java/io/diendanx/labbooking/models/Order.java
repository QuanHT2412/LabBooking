package io.diendanx.labbooking.models;

import com.google.firebase.Timestamp;

public class Order {
    private String id;
    private String userId;
    private double totalPrice;
    private Timestamp created_at;

    public Order() {
    }

    public Order(String id, String userId, double totalPrice, Timestamp created_at) {
        this.id = id;
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.created_at = created_at;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
