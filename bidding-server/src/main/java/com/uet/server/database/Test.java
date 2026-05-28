package com.uet.server.database;

import java.sql.Connection;

public class Test {
    public static void main(String[] args) {
        try {
            Connection conn = DBConnection.getConnection();

            if (conn != null && !conn.isClosed()) {
                System.out.println("Connected OK!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}