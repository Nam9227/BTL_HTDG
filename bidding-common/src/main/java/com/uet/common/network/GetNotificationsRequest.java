package com.uet.common.network;

import java.io.Serializable;

/**
 * Gói tin mạng gửi từ Client lên Server để yêu cầu lấy danh sách thông báo
 */
public class GetNotificationsRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId; 

    
    public GetNotificationsRequest(String userId) {
        this.userId = userId;
    }

    
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}