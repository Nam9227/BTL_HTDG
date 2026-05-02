package com.uet.client.model.item;

import java.io.Serializable;

public abstract class Item implements Serializable {
    protected String id;
    protected String name;
    protected String description;
    protected double startingPrice;
    protected double currentPrice;
    protected String imageUrl;

    public Item() {}

    public Item(String id, String name, String description,
                double startingPrice, double currentPrice, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.imageUrl = imageUrl;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getStartingPrice() { return startingPrice; }
    public double getCurrentPrice() { return currentPrice; }
    public String getImageUrl() { return imageUrl; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setStartingPrice(double startingPrice) { this.startingPrice = startingPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public abstract String getCategory();
}