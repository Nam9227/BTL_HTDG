package com.uet.server.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    public Connection getConnection() throws Exception {
        // bidding_db là tên Schema Nam tạo trong MySQL Workbench nhé
        String url = "jdbc:mysql://localhost:3306/bidding_db";
        String user = "root";
        String password = "123456";

        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, user, password);
    }

    // Nhấn chuột phải chọn "Run Main" để test thử luôn
    public static void main(String[] args) {
        try {
            Connection conn = new DBConnection().getConnection();
            if (conn != null) {
                System.out.println("--- [OK] SQL ĐÃ THÔNG RỒI NAM ƠI! ---");
            }
        } catch (Exception e) {
            System.err.println("--- [LỖI] KIỂM TRA LẠI PASS HOẶC TÊN DB NHÉ! ---");
            e.printStackTrace();
        }
    }
}