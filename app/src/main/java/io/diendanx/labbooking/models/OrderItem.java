package io.diendanx.labbooking.models;

import com.google.firebase.Timestamp;

import java.util.List;

public class OrderItem {
    private String Id;
    private String packageId;
    private String packageName;
    private double packagePrice;
    private int duration;
    private Timestamp startDate;
    private List<String> selectedFacilities;
    private String orderId;

    public OrderItem() {
    }

    public OrderItem(String id, String packageId, String packageName, double packagePrice, int duration, Timestamp startDate, List<String> selectedFacilities, String orderId) {
        Id = id;
        this.packageId = packageId;
        this.packageName = packageName;
        this.packagePrice = packagePrice;
        this.duration = duration;
        this.startDate = startDate;
        this.selectedFacilities = selectedFacilities;
        this.orderId = orderId;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public double getPackagePrice() {
        return packagePrice;
    }

    public void setPackagePrice(double packagePrice) {
        this.packagePrice = packagePrice;
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

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
