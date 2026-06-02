package com.uet.common.network;

import java.io.Serializable;

public class GetAutoBidRequest implements Serializable {
    private String auctionId;
    private String userId;

    public GetAutoBidRequest(String auctionId, String userId) {
        this.auctionId = auctionId;
        this.userId = userId;
    }

    public String getAuctionId() { return auctionId; }
    public String getUserId() { return userId; }
}
