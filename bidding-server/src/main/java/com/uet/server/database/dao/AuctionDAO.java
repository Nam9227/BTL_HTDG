package com.uet.server.database.dao;

import com.uet.common.model.auction.AuctionItem;
import com.uet.common.model.auction.BidRecord;
import com.uet.server.database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {

    public List<AuctionItem> getActiveAuctions() {
        List<AuctionItem> list = new ArrayList<>();

        String sql = """
                SELECT
                    a.id AS auction_id,
                    i.id AS item_id,
                    i.name,
                    i.description,
                    i.image_url,
                    i.seller_id,
                    seller.username AS seller_name,
                    a.start_price,      
                    a.current_price,    
                    a.winner_id,
                    winner.username AS winner_name,
                    a.end_time,
                    a.status
                FROM auctions a
                JOIN items i ON a.product_id = i.id
                LEFT JOIN users seller ON i.seller_id = seller.id
                LEFT JOIN users winner ON a.winner_id = winner.id
                WHERE a.status IN ('RUNNING') 
                ORDER BY a.end_time ASC
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {
                list.add(mapAuctionItem(rs));
            }

        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách đấu giá từ Database: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    public AuctionItem getAuctionById(String auctionId) {
        String sql = """
                SELECT
                    a.id AS auction_id,
                    i.id AS item_id,
                    i.name,
                    i.description,
                    i.image_url,
                    i.seller_id,
                    seller.username AS seller_name,
                    a.start_price,      
                    a.current_price,    
                    a.winner_id,
                    winner.username AS winner_name,
                    a.end_time,
                    a.status
                FROM auctions a
                JOIN items i ON a.product_id = i.id
                LEFT JOIN users seller ON i.seller_id = seller.id
                LEFT JOIN users winner ON a.winner_id = winner.id
                WHERE a.id = ?
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, auctionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAuctionItem(rs);
                }
            }

        } catch (Exception e) {
            System.err.println("Lỗi khi lấy chi tiết đấu giá từ Database: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private AuctionItem mapAuctionItem(ResultSet rs) throws Exception {
        AuctionItem item = new AuctionItem();

        item.setAuctionId(rs.getString("auction_id"));
        item.setProductId(rs.getString("item_id"));

        item.setProductName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setImageUrl(rs.getString("image_url"));

        item.setSellerId(rs.getString("seller_id"));
        item.setSellerName(rs.getString("seller_name"));

        item.setStartPrice(rs.getDouble("start_price"));
        item.setCurrentPrice(rs.getDouble("current_price"));

        item.setWinnerId(rs.getString("winner_id"));
        item.setWinnerName(rs.getString("winner_name"));

        Timestamp endTimeStamp = rs.getTimestamp("end_time");
        if (endTimeStamp != null) {
            item.setEndTime(endTimeStamp.toLocalDateTime());
        }

        item.setStatus(rs.getString("status"));

        return item;
    }

    public boolean createNewAuction(String sellerId, String productName, String description,
                                    double startPrice, String imageUrl, String category,
                                    String extra1, String extra2, String extra3,
                                    java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {

        String sqlItem = """
                INSERT INTO items 
                (id, name, description, image_url, category, seller_id, extra_1, extra_2, extra_3) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        // 🌟 ĐÃ SỬA: Bồi thêm cột id vào câu lệnh INSERT của bảng auctions
        String sqlAuction = """
                INSERT INTO auctions 
                (id, product_id, start_price, current_price, start_time, end_time, status) 
                VALUES (?, ?, ?, ?, ?, ?, 'PENDING')
                """;

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Bật Transaction bảo mật

            // 🌟 1. Sinh ID ngẫu nhiên cho sản phẩm
            String generatedItemId = com.uet.server.util.IdGenerator.generateId();

            // 🌟 2. Sinh ID ngẫu nhiên cho phiên đấu giá (auction_id)
            String generatedAuctionId = com.uet.server.util.IdGenerator.generateId();

            // --- BƯỚC 1: CHÈN VÀO BẢNG ITEMS ---
            try (PreparedStatement psItem = conn.prepareStatement(sqlItem)) {
                psItem.setString(1, generatedItemId);
                psItem.setString(2, productName);
                psItem.setString(3, description);
                psItem.setString(4, imageUrl);
                psItem.setString(5, category);
                psItem.setString(6, sellerId);

                psItem.setString(7, (extra1 != null && !extra1.isBlank()) ? extra1 : null);
                psItem.setString(8, (extra2 != null && !extra2.isBlank()) ? extra2 : null);
                psItem.setString(9, (extra3 != null && !extra3.isBlank()) ? extra3 : null);

                psItem.executeUpdate();
            }

            // --- BƯỚC 2: CHÈN VÀO BẢNG AUCTIONS ---
            try (PreparedStatement psAuction = conn.prepareStatement(sqlAuction)) {
                psAuction.setString(1, generatedAuctionId); // Ném ID phiên đấu giá vừa sinh vào cột id
                psAuction.setString(2, generatedItemId);    // Khóa ngoại liên kết sang bảng items
                psAuction.setDouble(3, startPrice);
                psAuction.setDouble(4, startPrice);
                psAuction.setTimestamp(5, java.sql.Timestamp.valueOf(startTime));
                psAuction.setTimestamp(6, java.sql.Timestamp.valueOf(endTime));

                psAuction.executeUpdate();
            }

            conn.commit();
            System.out.println("✅ [DATABASE SUCCESS] Tạo thành công Item ID: " + generatedItemId + " và Auction ID: " + generatedAuctionId);
            return true;

        } catch (Exception e) {
            System.err.println("❌ [DATABASE ERROR] Gặp sự cố chèn luồng. Tiến hành khôi phục (Rollback)...");
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ignored) {
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public boolean insertBid(String auctionId, String userId, double bidAmount) {
        String sqlBid = "INSERT INTO bids (id, auction_id, user_id, bid_amount, bid_time) VALUES (?, ?, ?, ?, ?)";
        String sqlUpdateAuction = "UPDATE auctions SET current_price = ?, winner_id = ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Bật Transaction để an toàn cho ví tiền và giá cả

            String bidId = com.uet.server.util.IdGenerator.generateId(); // Dùng hàm sinh ID của Nam

            // 1. Chèn vào bảng lịch sử bids
            try (PreparedStatement psBid = conn.prepareStatement(sqlBid)) {
                psBid.setString(1, bidId);
                psBid.setString(2, auctionId);
                psBid.setString(3, userId);
                psBid.setDouble(4, bidAmount);
                psBid.setTimestamp(5, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                psBid.executeUpdate();
            }

            // 2. Cập nhật giá cao nhất hiện tại và người đang tạm thắng vào bảng auctions
            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateAuction)) {
                psUpdate.setDouble(1, bidAmount);
                psUpdate.setString(2, userId);
                psUpdate.setString(3, auctionId);
                psUpdate.executeUpdate();
            }

            conn.commit();
            System.out.println("✅ [BID SUCCESS] User " + userId + " đã đặt giá " + bidAmount + " cho phiên " + auctionId);
            return true;

        } catch (Exception e) {
            System.err.println("❌ [BID ERROR] Lỗi đặt giá, tiến hành khôi phục...");
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (Exception ignored) {}
            }
        }
    }

    // 🌟 HÀM 2: LẤY LỊCH SỬ ĐẤU GIÁ CỦA 1 SẢN PHẨM (Sắp xếp lượt mới nhất lên đầu)
    public List<BidRecord> getBidHistory(String auctionId) {
        List<BidRecord> history = new ArrayList<>();
        String sql = """
                SELECT b.id, b.auction_id, b.user_id, u.username, b.bid_amount, b.bid_time
                FROM bids b
                JOIN users u ON b.user_id = u.id
                WHERE b.auction_id = ?
                ORDER BY b.bid_amount DESC, b.bid_time DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, auctionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BidRecord record = new BidRecord();
                    record.setId(rs.getString("id"));
                    record.setAuctionId(rs.getString("auction_id"));
                    record.setUserId(rs.getString("user_id"));
                    record.setUsername(rs.getString("username")); // Lấy từ lệnh JOIN bảng users
                    record.setBidAmount(rs.getDouble("bid_amount"));

                    Timestamp t = rs.getTimestamp("bid_time");
                    if (t != null) {
                        // 🌟 Lấy thời gian bằng cách chỉ định rõ dùng múi giờ của hệ thống máy tính hiện tại (Asia/Ho_Chi_Minh)
                        // Cách này giúp ép Java đọc đúng boong con số "22:28:11" trong DB mà không tự trừ giờ ngầm nữa.
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        record.setBidTime(rs.getTimestamp("bid_time", cal).toLocalDateTime());
                    }

                    history.add(record);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy lịch sử đặt giá: " + e.getMessage());
            e.printStackTrace();
        }
        return history;
    }
}