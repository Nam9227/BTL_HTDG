package com.uet.common.network;

import java.io.Serializable;

public class AutoBidRequest implements Serializable {
    private String auctionId;
    private String userId;
    private double maxPrice;
    private double stepPrice;
    private boolean active;

    public AutoBidRequest() {}

    public AutoBidRequest(String auctionId, String userId, double maxPrice, double stepPrice, boolean active) {
        this.auctionId = auctionId;
        this.userId = userId;
        this.maxPrice = maxPrice;
        this.stepPrice = stepPrice;
        this.active = active;
    }

    public String getAuctionId() { return auctionId; }
    public String getUserId() { return userId; }
    public double getMaxPrice() { return maxPrice; }
    public double getStepPrice() { return stepPrice; }
    public boolean isActive() { return active; }

    public void setAuctionId(String auctionId) { this.auctionId = auctionId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setMaxPrice(double maxPrice) { this.maxPrice = maxPrice; }
    public void setStepPrice(double stepPrice) { this.stepPrice = stepPrice; }
    public void setActive(boolean active) { this.active = active; }
}
