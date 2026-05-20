package com.uet.common.network;

import java.io.Serializable;

public class JoinAuctionRequest implements Serializable {
    private String auctionId;

    public JoinAuctionRequest() {
    }

    public JoinAuctionRequest(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }
}