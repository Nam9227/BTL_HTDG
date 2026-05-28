package com.uet.common.network;

import java.io.Serializable;

/**
 * Gói tin mạng gửi từ Client lên Server để yêu cầu lấy danh sách thông báo
 */
public class GetNotificationsRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId; // ID của người dùng đang đăng nhập (kiểu VARCHAR/String)

    // Constructor (Hàm khởi tạo)
    public GetNotificationsRequest(String userId) {
        this.userId = userId;
    }

    // Getter để Server lấy ra ID và quét database
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}