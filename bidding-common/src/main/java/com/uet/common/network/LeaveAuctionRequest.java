package com.uet.common.network;

import java.io.Serializable;

public class LeaveAuctionRequest implements Serializable {
    private String auctionId;

    public LeaveAuctionRequest() {
    }

    public LeaveAuctionRequest(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }
}