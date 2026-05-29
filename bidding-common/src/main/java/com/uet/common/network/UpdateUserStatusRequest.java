package com.uet.common.network;

/**
 * Class gửi yêu cầu cập nhật trạng thái Khóa/Mở khóa người dùng từ Admin lên Server
 */
public class UpdateUserStatusRequest extends NetworkMessage {
    private static final long serialVersionUID = 1L;

    private String userId;
    private boolean active; 

    
    public UpdateUserStatusRequest(String userId, boolean active) {
        this.userId = userId;
        this.active = active;
    }

    
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}