package com.uet.server.dao;

import com.uet.server.database.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {
    public static boolean checkLogin(String user, String pass) {
        String query = "SELECT * FROM Users WHERE username = ? AND password = ?";
        try {
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // Nếu có dữ liệu trả về thì là đúng User/Pass
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}