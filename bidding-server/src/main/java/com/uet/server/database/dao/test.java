package com.uet.server.database.dao;

public class test {
    public static void main(String[] args) {
        UserDAO dao = new UserDAO();

        boolean ok = dao.checkLogin("admin1", "123456");

        System.out.println(ok ? "Login SUCCESS" : "Login FAIL");
    }
}