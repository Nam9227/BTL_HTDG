package com.uet.server.database.dao;

import com.uet.common.model.auction.AutoBid;
import com.uet.common.network.AutoBidRequest;
import com.uet.common.network.Response;
import com.uet.server.database.DBConnection;
import com.uet.server.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.uet.common.network.AutoBidResponse;

public class AutoBidDAO {
    private static final Logger logger = LoggerFactory.getLogger(AutoBidDAO.class);

    public AutoBidResponse saveAutoBidConfig(AutoBidRequest request) {
        try (Connection conn = DBConnection.getConnection()) {
            
            String checkSql = "SELECT id FROM auto_bids WHERE auction_id = ? AND user_id = ?";
            String existingId = null;
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setString(1, request.getAuctionId());
                psCheck.setString(2, request.getUserId());
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        existingId = rs.getString("id");
                    }
                }
            }

            if (existingId != null) {
                String updateSql = "UPDATE auto_bids SET max_price = ?, step_price = ?, active = ?, created_at = NOW() WHERE id = ?";
                try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                    psUpdate.setDouble(1, request.getMaxPrice());
                    psUpdate.setDouble(2, request.getStepPrice());
                    psUpdate.setBoolean(3, request.isActive());
                    psUpdate.setString(4, existingId);
                    psUpdate.executeUpdate();
                }
            } else {
                String insertSql = "INSERT INTO auto_bids (id, auction_id, user_id, max_price, step_price, active, created_at) VALUES (?, ?, ?, ?, ?, ?, NOW())";
                try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                    psInsert.setString(1, IdGenerator.generateId());
                    psInsert.setString(2, request.getAuctionId());
                    psInsert.setString(3, request.getUserId());
                    psInsert.setDouble(4, request.getMaxPrice());
                    psInsert.setDouble(5, request.getStepPrice());
                    psInsert.setBoolean(6, request.isActive());
                    psInsert.executeUpdate();
                }
            }

            if (request.isActive()) {
                return new AutoBidResponse(true, "Đã bật Đấu giá tự động thành công!");
            } else {
                return new AutoBidResponse(true, "Đã tắt Đấu giá tự động.");
            }

        } catch (Exception e) {
            logger.error("Lỗi khi lưu cấu hình Auto Bid: ", e);
            return new AutoBidResponse(false, "Lỗi hệ thống khi thiết lập Auto Bid.");
        }
    }

    public AutoBid getAutoBidConfig(String auctionId, String userId) {
        String sql = "SELECT * FROM auto_bids WHERE auction_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, auctionId);
            ps.setString(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
                    java.sql.Timestamp t = rs.getTimestamp("created_at", cal);
                    LocalDateTime createdAt = t != null ? java.time.LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(t.getTime()),
                        java.time.ZoneId.of("Asia/Ho_Chi_Minh")
                    ) : null;

                    return new AutoBid(
                            rs.getString("id"),
                            rs.getString("auction_id"),
                            rs.getString("user_id"),
                            rs.getDouble("max_price"),
                            rs.getDouble("step_price"),
                            rs.getBoolean("active"),
                            createdAt
                    );
                }
            }
        } catch (Exception e) {
            logger.error("Lỗi lấy cấu hình Auto Bid: ", e);
        }
        return null;
    }

    public List<AutoBid> getActiveAutoBids(String auctionId) {
        List<AutoBid> list = new ArrayList<>();
        String sql = "SELECT * FROM auto_bids WHERE auction_id = ? AND active = TRUE ORDER BY created_at ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, auctionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
                    java.sql.Timestamp t = rs.getTimestamp("created_at", cal);
                    LocalDateTime createdAt = t != null ? java.time.LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(t.getTime()),
                        java.time.ZoneId.of("Asia/Ho_Chi_Minh")
                    ) : null;

                    list.add(new AutoBid(
                            rs.getString("id"),
                            rs.getString("auction_id"),
                            rs.getString("user_id"),
                            rs.getDouble("max_price"),
                            rs.getDouble("step_price"),
                            rs.getBoolean("active"),
                            createdAt
                    ));
                }
            }
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách Auto Bids: ", e);
        }
        return list;
    }
    
    public void deactivateAutoBid(String auctionId, String userId) {
        String sql = "UPDATE auto_bids SET active = FALSE WHERE auction_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             ps.setString(1, auctionId);
             ps.setString(2, userId);
             ps.executeUpdate();
        } catch (Exception e) {
            logger.error("Lỗi khi vô hiệu hóa Auto Bid cho user " + userId, e);
        }
    }
}
