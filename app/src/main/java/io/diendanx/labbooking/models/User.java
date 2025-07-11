package io.diendanx.labbooking.models;

import com.google.firebase.Timestamp;

public class User {
    private String username;
    private String userId;
    private String email;

    public User() {
    }

    public User(String username, String userId, String email) {
        this.username = username;
        this.userId = userId;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
