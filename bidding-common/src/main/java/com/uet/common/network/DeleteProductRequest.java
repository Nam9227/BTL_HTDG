package com.uet.common.network;

import java.io.Serializable;

public class DeleteProductRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String auctionId; 
    private String userId;    

    public DeleteProductRequest(String auctionId, String userId) {
        this.auctionId = auctionId;
        this.userId = userId;
    }

    public String getAuctionId() { return auctionId; }
    public String getUserId() { return userId; }
}