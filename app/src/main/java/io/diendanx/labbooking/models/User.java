package io.diendanx.labbooking.models;

import com.google.firebase.Timestamp;

public class User {
    private String username;
    private Timestamp createdAt;
    private String userId;
    private String role;

    public User() {
    }

    public User(String username, Timestamp createdAt, String userId) {
        this.username = username;
        this.createdAt = createdAt;
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
