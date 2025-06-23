package io.diendanx.labbooking.models;

public class Package {
    private String id;
    private String name;
    private int seatCount;
    private int tableCount;
    private String description;
    private Long price;

    public Package(String id, String name, int seatCount, int tableCount, String description, Long price) {
        this.id = id;
        this.name = name;
        this.seatCount = seatCount;
        this.tableCount = tableCount;
        this.description = description;
        this.price = price;
    }

    public Package() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(int seatCount) {
        this.seatCount = seatCount;
    }

    public int getTableCount() {
        return tableCount;
    }

    public void setTableCount(int tableCount) {
        this.tableCount = tableCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }
}
