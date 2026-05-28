package com.uet.common.network;

/**
 * Class gửi yêu cầu cập nhật trạng thái Khóa/Mở khóa người dùng từ Admin lên Server
 */
public class UpdateUserStatusRequest extends NetworkMessage {
    private static final long serialVersionUID = 1L;

    private String userId;
    private boolean active; // true: Mở khóa (Hoạt động), false: Khóa tài khoản

    // Constructor đầy đủ tham số
    public UpdateUserStatusRequest(String userId, boolean active) {
        this.userId = userId;
        this.active = active;
    }

    // Hàm lấy ID người dùng cần xử lý
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Hàm lấy trạng thái mong muốn (định dạng của kiểu boolean bắt đầu bằng 'is')
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}