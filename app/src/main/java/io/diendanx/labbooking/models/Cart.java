package io.diendanx.labbooking.models;

import com.google.firebase.Timestamp;

import java.util.List;

public class Cart {
    private String id;
    private String userId;
    private String packageId;
    private int duration;
    private Timestamp startDate;
    private List<String> selectedFacilities;
    private long totalPrice;

    public Cart() {}

    public Cart(String userId, String packageId, int duration, Timestamp startDate, List<String> selectedFacilities, long totalPrice) {
        this.userId = userId;
        this.packageId = packageId;
        this.duration = duration;
        this.startDate = startDate;
        this.selectedFacilities = selectedFacilities;
        this.totalPrice = totalPrice;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public List<String> getSelectedFacilities() {
        return selectedFacilities;
    }

    public void setSelectedFacilities(List<String> selectedFacilities) {
        this.selectedFacilities = selectedFacilities;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTotalPrice() {
        return totalPrice;
    }
    public void setTotalPrice(long totalPrice) {
        this.totalPrice = totalPrice;
    }
}
