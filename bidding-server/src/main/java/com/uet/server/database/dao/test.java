package com.uet.server.database.dao;

import com.uet.common.model.user.User;

public class test {
    public static void main(String[] args) {
        UserDAO dao = new UserDAO();

        User ok = dao.login("admin1", "123456");

        System.out.println(ok!=null ? ok : "Login FAIL");
    }
}