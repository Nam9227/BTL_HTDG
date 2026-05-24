package com.uet.common.network;

import java.io.Serializable;

public class ForceEndRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String auctionId;

    // 🌟 Constructor mặc định không tham số (Bắt buộc phải có để gửi nhận Object)
    public ForceEndRequest() {
    }

    public ForceEndRequest(String auctionId) {
        this.auctionId = auctionId;
    }

    // Getter và Setter
    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }
}