package com.uet.server.database.dao;

import com.uet.common.network.AdminActivity;
import com.uet.common.network.AdminDashboardResponse;
import com.uet.server.database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AdminDAO {

    public AdminDashboardResponse getDashboardData() {
        int users = 0, products = 0, auctions = 0, pending = 0;
        List<AdminActivity> activities = new ArrayList<>();

        
        String countUsersSql = "SELECT COUNT(*) FROM users";
        String countProductsSql = "SELECT COUNT(*) FROM items"; 
        String countAuctionsSql = "SELECT COUNT(*) FROM auctions WHERE status = 'RUNNING'";
        String countPendingSql = "SELECT (SELECT COUNT(*) FROM transactions WHERE status = 'PENDING') + (SELECT COUNT(*) FROM auctions WHERE status = 'PENDING')";

        
        String logSql = "SELECT DATE_FORMAT(created_at, '%H:%i') as time, action_name, target_name, status FROM admin_logs ORDER BY id DESC LIMIT 5";

        try (Connection conn = DBConnection.getConnection()) {

            
            try (PreparedStatement ps = conn.prepareStatement(countUsersSql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) users = rs.getInt(1);
            }
            
            try (PreparedStatement ps = conn.prepareStatement(countProductsSql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) products = rs.getInt(1);
            }
            
            try (PreparedStatement ps = conn.prepareStatement(countAuctionsSql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) auctions = rs.getInt(1);
            }
            
            try (PreparedStatement ps = conn.prepareStatement(countPendingSql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) pending = rs.getInt(1);
            }

            
            try (PreparedStatement ps = conn.prepareStatement(logSql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    activities.add(new AdminActivity(
                            rs.getString("time"),
                            rs.getString("action_name"),
                            rs.getString("target_name"),
                            rs.getString("status")
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return new AdminDashboardResponse(users, products, auctions, pending, activities);
    }

    public static void logAdminAction(String actionName, String targetName, String status) {
        String sql = "INSERT INTO admin_logs (action_name, target_name, status) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, actionName);
            ps.setString(2, targetName);
            ps.setString(3, status);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Lỗi khi lưu log admin: " + e.getMessage());
        }
    }
}