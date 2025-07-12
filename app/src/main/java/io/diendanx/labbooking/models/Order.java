package io.diendanx.labbooking.models;

import com.google.firebase.Timestamp;

import java.util.List;
import java.util.Map;

public class Order {
    private String id;
    private String userId;
    private String username;
    private String email;
    private String phone;
    private long totalPrice;
    private Timestamp created_at;
    private List<String> orderItems;

    public Order() {
    }

    public Order(String id, String userId, String username, String email, String phone, long totalPrice, Timestamp created_at, List<String> orderItems) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.phone = phone;
        this.email = email;
        this.totalPrice = totalPrice;
        this.created_at = created_at;
        this.orderItems = orderItems;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(long totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public List<String> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<String> orderItems) {
        this.orderItems = orderItems;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
