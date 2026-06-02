package com.uet.common.model.auction;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AutoBid implements Serializable {
    private String id;
    private String auctionId;
    private String userId;
    private double maxPrice;
    private double stepPrice;
    private boolean active;
    private LocalDateTime createdAt;

    public AutoBid() {}

    public AutoBid(String id, String auctionId, String userId, double maxPrice, double stepPrice, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.auctionId = auctionId;
        this.userId = userId;
        this.maxPrice = maxPrice;
        this.stepPrice = stepPrice;
        this.active = active;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAuctionId() { return auctionId; }
    public void setAuctionId(String auctionId) { this.auctionId = auctionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getMaxPrice() { return maxPrice; }
    public void setMaxPrice(double maxPrice) { this.maxPrice = maxPrice; }

    public double getStepPrice() { return stepPrice; }
    public void setStepPrice(double stepPrice) { this.stepPrice = stepPrice; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
