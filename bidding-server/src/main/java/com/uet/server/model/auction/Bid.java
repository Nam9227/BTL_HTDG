package com.uet.server.model.auction;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Bid implements Serializable {
    private String id;
    private String bidderId;
    private double amount;
    private LocalDateTime timestamp;

    public Bid() {}

    public Bid(String id, String bidderId, double amount, LocalDateTime timestamp) {
        this.id = id;
        this.bidderId = bidderId;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getBidderId() { return bidderId; }
    public double getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public void setId(String id) { this.id = id; }
    public void setBidderId(String bidderId) { this.bidderId = bidderId; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}