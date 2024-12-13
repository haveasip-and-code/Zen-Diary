package com.example.zendiary.ui.profile;

public class StoreItem {
    private String name;
    private double price;
    private String imageUrl;

    public StoreItem() {
        // Default constructor required for calls to DataSnapshot.getValue(StoreItem.class)
    }

    public StoreItem(String name, double price, String imageUrl) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}