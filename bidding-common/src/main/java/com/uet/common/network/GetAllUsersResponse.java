package com.uet.common.network;

import com.uet.common.model.user.User;
import java.util.List;

public class GetAllUsersResponse extends NetworkMessage {
    private static final long serialVersionUID = 1L;

    private List<User> users;

    public GetAllUsersResponse(List<User> users) {
        this.users = users;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}