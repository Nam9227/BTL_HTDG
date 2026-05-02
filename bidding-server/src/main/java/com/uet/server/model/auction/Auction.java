package com.uet.server.model.auction;

import com.uet.server.model.item.Item;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Auction implements Serializable {
    private String id;
    private Item item;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // OPEN / RUNNING / FINISHED / PAID / CANCELED
    private Bid highestBid;

    public Auction() {}

    public Auction(String id, Item item, LocalDateTime startTime,
                   LocalDateTime endTime, String status, Bid highestBid) {
        this.id = id;
        this.item = item;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.highestBid = highestBid;
    }

    public String getId() { return id; }
    public Item getItem() { return item; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getStatus() { return status; }
    public Bid getHighestBid() { return highestBid; }

    public void setId(String id) { this.id = id; }
    public void setItem(Item item) { this.item = item; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public void setStatus(String status) { this.status = status; }
    public void setHighestBid(Bid highestBid) { this.highestBid = highestBid; }
}