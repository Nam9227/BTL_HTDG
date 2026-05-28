package com.uet.common.network;

import java.io.Serializable;

public class DeleteProductRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String auctionId; // Dùng mã phiên đấu giá để Server biết cần xóa phiên nào
    private String userId;    // Gửi kèm ID người xóa để Server check quyền (tránh người khác xóa trộm)

    public DeleteProductRequest(String auctionId, String userId) {
        this.auctionId = auctionId;
        this.userId = userId;
    }

    public String getAuctionId() { return auctionId; }
    public String getUserId() { return userId; }
}