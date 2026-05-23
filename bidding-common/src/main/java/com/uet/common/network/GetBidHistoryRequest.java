package com.uet.common.network;

import java.io.Serializable;

public class GetBidHistoryRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String auctionId; // Client truyền ID phiên đấu giá muốn xem lịch sử

    public GetBidHistoryRequest(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getAuctionId() { return auctionId; }
}