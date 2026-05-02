package com.uet.common.network;

import java.io.Serializable;

public class BidRequest implements Serializable {
    private String auctionId;
    private String bidderId;
    private double amount;

    public BidRequest() {}

    public BidRequest(String auctionId, String bidderId, double amount) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
    }

    public String getAuctionId() { return auctionId; }
    public String getBidderId() { return bidderId; }
    public double getAmount() { return amount; }

    public void setAuctionId(String auctionId) { this.auctionId = auctionId; }
    public void setBidderId(String bidderId) { this.bidderId = bidderId; }
    public void setAmount(double amount) { this.amount = amount; }
}