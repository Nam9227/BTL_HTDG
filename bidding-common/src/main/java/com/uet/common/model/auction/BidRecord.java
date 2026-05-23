package com.uet.common.model.auction;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BidRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String auctionId;
    private String userId;
    private String username;    // Thêm tên người trả giá để hiển thị lên giao diện Client
    private double bidAmount;
    private LocalDateTime bidTime;

    // Constructor mặc định
    public BidRecord() {}

    // Getter và Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAuctionId() { return auctionId; }
    public void setAuctionId(String auctionId) { this.auctionId = auctionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public double getBidAmount() { return bidAmount; }
    public void setBidAmount(double bidAmount) { this.bidAmount = bidAmount; }

    public LocalDateTime getBidTime() { return bidTime; }
    public void setBidTime(LocalDateTime bidTime) { this.bidTime = bidTime; }
}