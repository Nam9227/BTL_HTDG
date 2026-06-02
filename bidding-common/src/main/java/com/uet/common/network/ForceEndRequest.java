package com.uet.common.network;

import java.io.Serializable;

public class ForceEndRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String auctionId;

    
    public ForceEndRequest() {
    }

    public ForceEndRequest(String auctionId) {
        this.auctionId = auctionId;
    }

    
    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }
}