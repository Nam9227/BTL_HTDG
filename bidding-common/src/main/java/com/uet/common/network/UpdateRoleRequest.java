package com.uet.common.network;

import com.uet.common.model.user.Role;
import java.io.Serializable;

public class UpdateRoleRequest implements Serializable {

    private String userId;
    private Role role;

    public UpdateRoleRequest(String userId, Role role) {
        this.userId = userId;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public Role getRole() {
        return role;
    }
}