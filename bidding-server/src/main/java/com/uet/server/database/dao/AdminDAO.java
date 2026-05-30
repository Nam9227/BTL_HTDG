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

        // 1. Câu lệnh đếm số lượng tổng quan
        String countUsersSql = "SELECT COUNT(*) FROM users";
        String countProductsSql = "SELECT COUNT(*) FROM items"; // Tên bảng item của Nam
        String countAuctionsSql = "SELECT COUNT(*) FROM auctions WHERE status = 'RUNNING'";
        String countPendingSql = "SELECT COUNT(*) FROM transactions WHERE status = 'PENDING'";

        // 2. Câu lệnh lấy 5 hoạt động gần nhất (Lấy từ bảng log hoặc lịch sử hệ thống của Nam)
        String logSql = "SELECT DATE_FORMAT(created_at, '%H:%i') as time, action_name, target_name, status FROM admin_logs ORDER BY id DESC LIMIT 5";

        try (Connection conn = DBConnection.getConnection()) {

            // Đếm số User
            try (PreparedStatement ps = conn.prepareStatement(countUsersSql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) users = rs.getInt(1);
            }
            // Đếm sản phẩm
            try (PreparedStatement ps = conn.prepareStatement(countProductsSql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) products = rs.getInt(1);
            }
            // Đếm phiên đấu giá đang chạy
            try (PreparedStatement ps = conn.prepareStatement(countAuctionsSql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) auctions = rs.getInt(1);
            }
            // Đếm số giao dịch nạp rút / phiên chờ duyệt
            try (PreparedStatement ps = conn.prepareStatement(countPendingSql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) pending = rs.getInt(1);
            }

            // Đổ dữ liệu lịch sử hoạt động vào bảng
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

            // Nếu DB trống rỗng chưa có log, tạo dữ liệu ảo giống hệt ảnh của Nam để test UI
            if (activities.isEmpty()) {
                activities.add(new AdminActivity("10:30", "Đăng nhập hệ thống", "Admin_01", "Thành công"));
                activities.add(new AdminActivity("10:25", "Duyệt sản phẩm", "Laptop Dell XPS", "Thành công"));
                activities.add(new AdminActivity("10:15", "Khóa tài khoản", "User_Bad_01", "Hoàn tất"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new AdminDashboardResponse(users, products, auctions, pending, activities);
    }
}