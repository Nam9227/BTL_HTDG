package com.uet.common.network;

import java.io.Serializable;

public class ApproveAuctionRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String auctionId;
    private boolean approved;

    // Hàm tạo (Constructor)
    public ApproveAuctionRequest(String auctionId, boolean approved) {
        this.auctionId = auctionId;
        this.approved = approved;
    }

    // Các hàm Getter để Server bóc tách dữ liệu
    public String getAuctionId() {
        return auctionId;
    }

    public boolean isApproved() {
        return approved;
    }
}