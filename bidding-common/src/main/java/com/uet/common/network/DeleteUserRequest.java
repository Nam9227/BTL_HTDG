package com.uet.common.network;

/**
 * Class gửi yêu cầu XÓA người dùng từ Admin lên Server
 */
public class DeleteUserRequest extends NetworkMessage {
    private static final long serialVersionUID = 1L;

    private String userId;

    // Constructor để bọc ID user cần xóa
    public DeleteUserRequest(String userId) {
        this.userId = userId;
    }

    // Getter và Setter để Server có thể trích xuất thông tin
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}