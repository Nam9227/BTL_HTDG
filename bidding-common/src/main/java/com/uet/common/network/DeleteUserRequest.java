package com.uet.common.network;




public class DeleteUserRequest extends NetworkMessage {
    private static final long serialVersionUID = 1L;

    private String userId;

    
    public DeleteUserRequest(String userId) {
        this.userId = userId;
    }

    
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}