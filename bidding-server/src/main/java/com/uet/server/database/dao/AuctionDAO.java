package com.uet.server.database.dao;

import com.uet.common.model.auction.AuctionItem;
import com.uet.common.model.auction.BidRecord;
import com.uet.server.database.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {
    private static final Logger logger = LoggerFactory.getLogger(AuctionDAO.class);

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
                AuctionItem item = mapAuctionItem(rs);
                if (item != null) {
                    item.setProductImageBytes(loadImageBytes(item.getImageUrl()));
                }
                list.add(item);
            }

        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách đấu giá từ Database: ", e);
        }

        return list;
    }

    public List<AuctionItem> getAuctionsForUser(String userId) {
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
                WHERE (i.seller_id = ? OR a.winner_id = ?)
                ORDER BY a.end_time DESC
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, userId);
            ps.setString(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuctionItem item = mapAuctionItem(rs);
                    if (item != null) {
                        item.setProductImageBytes(loadImageBytes(item.getImageUrl()));
                    }
                    list.add(item);
                }
            }

        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách đấu giá của User " + userId + " từ Database: ", e);
        }

        return list;
    }

    public AuctionItem getAuctionById(String auctionId) {
        return getAuctionById(auctionId, true);
    }

    public AuctionItem getAuctionById(String auctionId, boolean includeBytes) {
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
                    a.status,
                    i.category AS category,
                    i.extra_1 AS brand
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
                    AuctionItem item = mapAuctionItem(rs);
                    if (item != null && includeBytes) {
                        item.setProductImageBytes(loadImageBytes(item.getImageUrl()));
                    }
                    return item;
                }
            }

        } catch (Exception e) {
            logger.error("Lỗi khi lấy chi tiết đấu giá từ Database: ", e);
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

        java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        Timestamp endTimeStamp = rs.getTimestamp("end_time", cal);
        if (endTimeStamp != null) {
            item.setEndTime(java.time.LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(endTimeStamp.getTime()),
                java.time.ZoneId.of("Asia/Ho_Chi_Minh")
            ));
        }

        item.setStatus(rs.getString("status"));

        try {
            item.setCategory(rs.getString("category"));
        } catch (Exception ignored) {}

        try {
            item.setBrand(rs.getString("brand"));
        } catch (Exception ignored) {}

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

                java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
                java.sql.Timestamp startTs = java.sql.Timestamp.from(startTime.atZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).toInstant());
                java.sql.Timestamp endTs = java.sql.Timestamp.from(endTime.atZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).toInstant());

                psAuction.setTimestamp(5, startTs, cal);
                psAuction.setTimestamp(6, endTs, cal);

                psAuction.executeUpdate();
            }

            conn.commit();
            logger.info("[DATABASE SUCCESS] Tạo thành công Item ID: {} và Auction ID: {}", generatedItemId, generatedAuctionId);
            return true;

        } catch (Exception e) {
            logger.error("[DATABASE ERROR] Gặp sự cố chèn luồng. Tiến hành khôi phục (Rollback)...", e);
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

                java.time.ZonedDateTime nowVN = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
                java.sql.Timestamp nowTs = java.sql.Timestamp.from(nowVN.toInstant());
                java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

                psBid.setTimestamp(5, nowTs, cal);
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
            logger.info("[BID SUCCESS] User {} đã đặt giá {} cho phiên {}", userId, bidAmount, auctionId);
            return true;

        } catch (Exception e) {
            logger.error("[BID ERROR] Lỗi đặt giá, tiến hành khôi phục...", e);
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

                    java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
                    Timestamp t = rs.getTimestamp("bid_time", cal);
                    if (t != null) {
                        record.setBidTime(java.time.LocalDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(t.getTime()),
                            java.time.ZoneId.of("Asia/Ho_Chi_Minh")
                        ));
                    }

                    history.add(record);
                }
            }
        } catch (Exception e) {
            logger.error("Lỗi khi lấy lịch sử đặt giá: ", e);
        }
        return history;
    }

    public List<AuctionItem> getAllAuctionsForAdmin() {
        List<AuctionItem> list = new java.util.ArrayList<>();

        // 🌟 Câu lệnh SQL đã được khớp 100% với tên cột thực tế của Nam:
        // a.product_id kết nối với i.id
        // Sắp xếp theo thời gian bắt đầu a.start_time DESC
        String sql = "SELECT a.id AS auction_id, " +
                "       i.name AS product_name, " +
                "       i.seller_id, " +
                "       a.start_price, " +
                "       a.current_price, " +
                "       i.description, " +
                "       i.image_url, " +
                "       a.status, " +
                "       a.end_time " +
                "FROM auctions a " +
                "INNER JOIN items i ON a.product_id = i.id " +
                "ORDER BY a.start_time DESC";

        try (java.sql.Connection conn = com.uet.server.database.DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                AuctionItem item = new AuctionItem();

                item.setAuctionId(rs.getString("auction_id"));
                item.setProductName(rs.getString("product_name"));
                item.setSellerId(rs.getString("seller_id"));
                item.setStartPrice(rs.getDouble("start_price"));
                item.setCurrentPrice(rs.getDouble("current_price"));
                item.setDescription(rs.getString("description"));
                item.setImageUrl(rs.getString("image_url"));
                item.setStatus(rs.getString("status"));

                java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
                Timestamp endTimeStamp = rs.getTimestamp("end_time", cal);
                if (endTimeStamp != null) {
                    item.setEndTime(java.time.LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(endTimeStamp.getTime()),
                        java.time.ZoneId.of("Asia/Ho_Chi_Minh")
                    ));
                }
                list.add(item);
            }
        } catch (Exception e) {
            logger.error("Lỗi khi tải toàn bộ danh sách phiên đấu giá cho Admin: ", e);
        }
        return list;
    }

    public boolean updateAuctionStatus(String auctionId, String status) {
        String sql = "UPDATE auctions SET status = ? WHERE id = ?";
        try (java.sql.Connection conn = com.uet.server.database.DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, auctionId);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật trạng thái đấu giá cho phiên ID: " + auctionId, e);
            return false;
        }
    }

    public int startEligibleAuctions() {
        String sql = "UPDATE auctions SET status = 'RUNNING' " +
                "WHERE status = 'ACTIVE' AND start_time <= NOW()";
        try (java.sql.Connection conn = com.uet.server.database.DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            int rows = ps.executeUpdate();
            if (rows > 0) {
                logger.info("[Scheduler] Đã kích hoạt {} phiên đấu giá sang trạng thái RUNNING!", rows);
            }
            return rows;
        } catch (Exception e) {
            logger.error("Lỗi khi kích hoạt các phiên đấu giá: ", e);
            return 0;
        }
    }

    // 2. Hàm quét các phiên RUNNING đã hết giờ để chuyển sang FINISHED
    public int finishExpiredAuctions() {
        // 1. Dùng INNER JOIN để bốc luôn mã người bán (i.sender_id hoặc i.seller_id) từ bảng items lên
        // 💡 Chú ý: Ở ảnh HeidiSQL trước Nam chụp, cột người bán trong bảng items tên là 'seller_id' nhé!
        String selectSql = "SELECT a.id AS auction_id, a.winner_id, a.current_price, i.seller_id " +
                "FROM auctions a " +
                "INNER JOIN items i ON a.product_id = i.id " +
                "WHERE a.status = 'RUNNING' AND a.end_time <= NOW()";

        String updateSql = "UPDATE auctions SET status = 'FINISHED' WHERE id = ?";

        // Khởi tạo WalletDAO để xử lý luồng tiền
        com.uet.server.database.dao.WalletDAO walletDAO = new com.uet.server.database.dao.WalletDAO();
        int finishedCount = 0;

        try (java.sql.Connection conn = com.uet.server.database.DBConnection.getConnection();
             java.sql.PreparedStatement psSelect = conn.prepareStatement(selectSql);
             java.sql.ResultSet rs = psSelect.executeQuery()) {

            while (rs.next()) {
                String auctionId = rs.getString("auction_id");
                String winnerId = rs.getString("winner_id");
                String sellerId = rs.getString("seller_id"); // Mã của chủ sản phẩm (Người bán)
                double finalPrice = rs.getDouble("current_price");

                // Cập nhật trạng thái phiên này thành FINISHED
                try (java.sql.PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                    psUpdate.setString(1, auctionId);
                    psUpdate.executeUpdate();
                }

                finishedCount++;

                // Có người thắng cuộc -> Tiến hành luân chuyển dòng tiền
                if (winnerId != null && !winnerId.trim().isEmpty()) {

                    // Dòng 1: TRỪ TIỀN THẬT CỦA NGƯỜI THẮNG CUỘC (Giá trị âm)
                    boolean isDeducted = walletDAO.updateBalance(winnerId, -finalPrice);

                    // Dòng 2: CỘNG TIỀN THẬT VÀO VÍ NGƯỜI BÁN (Giá trị dương)
                    boolean isCredited = walletDAO.updateBalance(sellerId, finalPrice);

                    if (isDeducted && isCredited) {
                        logger.info("[Scheduler] Giao dịch thành công phiên {}:\n   -> Đã trừ {}đ từ người mua ({})\n   -> Đã cộng {}đ vào người bán ({})",
                                auctionId, finalPrice, winnerId, finalPrice, sellerId);
                    } else {
                        logger.error("[Scheduler] LỖI: Giao dịch dòng tiền thất bại tại phiên {}", auctionId);
                    }
                } else {
                    logger.info("[Scheduler] Phiên {} đã đóng nhưng không có ai mua.", auctionId);
                }
            }
        } catch (Exception e) {
            logger.error("Lỗi khi đóng các phiên đấu giá hết hạn: ", e);
        }
        return finishedCount;
    }

    public boolean forceEndAuctionAndProcessTransaction(String auctionId) {
        String selectSql = "SELECT a.winner_id, a.current_price, i.seller_id, a.status " +
                "FROM auctions a " +
                "INNER JOIN items i ON a.product_id = i.id " +
                "WHERE a.id = ?";
        
        String updateSql = "UPDATE auctions SET status = 'FINISHED' WHERE id = ?";
        com.uet.server.database.dao.WalletDAO walletDAO = new com.uet.server.database.dao.WalletDAO();
        
        try (Connection conn = DBConnection.getConnection()) {
            String winnerId = null;
            String sellerId = null;
            double finalPrice = 0;
            String status = null;
            
            try (PreparedStatement psSelect = conn.prepareStatement(selectSql)) {
                psSelect.setString(1, auctionId);
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) {
                        winnerId = rs.getString("winner_id");
                        sellerId = rs.getString("seller_id");
                        finalPrice = rs.getDouble("current_price");
                        status = rs.getString("status");
                    }
                }
            }
            
            if (status == null || !"RUNNING".equals(status)) {
                logger.warn("Không thể ép kết thúc phiên {} vì trạng thái hiện tại là: {}", auctionId, status);
                return false;
            }
            
            // 1. Cập nhật trạng thái thành FINISHED
            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setString(1, auctionId);
                int updatedRows = psUpdate.executeUpdate();
                if (updatedRows <= 0) {
                    return false;
                }
            }
            
            // 2. Thực hiện luân chuyển dòng tiền
            if (winnerId != null && !winnerId.trim().isEmpty()) {
                boolean isDeducted = walletDAO.updateBalance(winnerId, -finalPrice);
                boolean isCredited = walletDAO.updateBalance(sellerId, finalPrice);
                
                if (isDeducted && isCredited) {
                    logger.info("[Admin Force End] Giao dịch thành công phiên {}:\n   -> Đã trừ {}đ từ người mua ({})\n   -> Đã cộng {}đ vào người bán ({})",
                            auctionId, finalPrice, winnerId, finalPrice, sellerId);
                } else {
                    logger.error("[Admin Force End] LỖI: Giao dịch dòng tiền thất bại tại phiên {}", auctionId);
                }
            } else {
                logger.info("[Admin Force End] Phiên {} đã kết thúc bởi Admin nhưng không có ai mua.", auctionId);
            }
            return true;
            
        } catch (Exception e) {
            logger.error("Lỗi khi ép kết thúc phiên đấu giá và thanh toán ID: " + auctionId, e);
            return false;
        }
    }

    private byte[] loadImageBytes(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }
        try {
            byte[] bytes = null;
            if (imagePath.startsWith("file://")) {
                String p = imagePath.replaceFirst("^file:///*", "/");
                if (p.matches("^/[a-zA-Z]:/.*")) {
                    p = p.substring(1);
                }
                java.nio.file.Path filePath = java.nio.file.Paths.get(p);
                if (java.nio.file.Files.exists(filePath)) {
                    bytes = java.nio.file.Files.readAllBytes(filePath);
                }
            } else {
                java.nio.file.Path filePath = java.nio.file.Paths.get(imagePath);
                if (java.nio.file.Files.exists(filePath)) {
                    bytes = java.nio.file.Files.readAllBytes(filePath);
                }
            }
            return bytes;
        } catch (Exception e) {
            logger.warn("Không thể đọc file ảnh sản phẩm từ path: {}, lỗi: {}", imagePath, e.getMessage());
            return null;
        }
    }
    public boolean deleteAuction(String auctionId) {
        // 1. Câu lệnh lấy mã product_id trước khi xóa phiên đấu giá
        String sqlGetProductId = "SELECT product_id FROM auctions WHERE id = ?";

        // 2. Câu lệnh xóa ở bảng bids trước để gỡ ràng buộc khóa ngoại
        String sqlDeleteBids = "DELETE FROM bids WHERE auction_id = ?";

        // 3. Câu lệnh xóa ở bảng auctions sau khi bids sạch bóng
        String sqlDeleteAuction = "DELETE FROM auctions WHERE id = ?";

        // 4. Câu lệnh xóa ở bảng items sau khi auctions sạch bóng
        String sqlDeleteItem = "DELETE FROM items WHERE id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // 🌟 BẬT TRANSACTION: Đảm bảo xóa là phải xóa sạch cả hai, lỗi là hủy lệnh

            String productId = null;

            // BƯỚC 1: Tìm mã product_id liên kết
            try (PreparedStatement psGet = conn.prepareStatement(sqlGetProductId)) {
                psGet.setString(1, auctionId);
                try (ResultSet rs = psGet.executeQuery()) {
                    if (rs.next()) {
                        productId = rs.getString("product_id");
                    }
                }
            }

            // Nếu không tìm thấy phiên đấu giá này trong hệ thống, dừng lại luôn
            if (productId == null) {
                logger.warn("[DELETE WARN] Không tìm thấy phiên đấu giá với ID: {}", auctionId);
                return false;
            }

            // BƯỚC 2: Xóa các lượt đặt giá trong bảng bids liên kết trước
            try (PreparedStatement psDelBids = conn.prepareStatement(sqlDeleteBids)) {
                psDelBids.setString(1, auctionId);
                psDelBids.executeUpdate();
                logger.info("[DELETE] Đã xóa toàn bộ lượt bid liên quan ở bảng bids, ID: {}", auctionId);
            }

            // BƯỚC 3: Xóa dữ liệu tại bảng auctions
            try (PreparedStatement psDelAuction = conn.prepareStatement(sqlDeleteAuction)) {
                psDelAuction.setString(1, auctionId);
                psDelAuction.executeUpdate();
                logger.info("[DELETE] Đã xóa phiên đấu giá ở bảng auctions, ID: {}", auctionId);
            }

            // BƯỚC 4: Xóa dữ liệu tương ứng tại bảng items
            try (PreparedStatement psDelItem = conn.prepareStatement(sqlDeleteItem)) {
                psDelItem.setString(1, productId);
                psDelItem.executeUpdate();
                logger.info("[DELETE] Đã xóa sản phẩm ở bảng items thành công, ID: {}", productId);
            }

            // Vượt qua tất cả an toàn -> Chốt lưu thay đổi vào Database thật
            conn.commit();
            logger.info("[DELETE SUCCESS] Đã dọn dẹp sạch sẽ phiên {} và sản phẩm {} khỏi hệ thống!", auctionId, productId);
            return true;

        } catch (Exception e) {
            logger.error("[DELETE ERROR] Gặp sự cố khi thực thi xóa. Tiến hành khôi phục dữ liệu (Rollback)...", e);
            if (conn != null) {
                try {
                    conn.rollback(); // Hủy toàn bộ các lệnh xóa dở dang nếu có một bảng bị lỗi
                } catch (Exception ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Trả lại trạng thái mặc định cho Connection Pool
                    conn.close();
                } catch (Exception ignored) {}
            }
        }
    }

    public boolean updateAuction(AuctionItem item) {
        String sqlUpdateItem = """
                UPDATE items 
                SET name = ?, description = ?, image_url = ?, category = ?, extra_1 = ?
                WHERE id = ?
                """;

        String sqlUpdateAuction = """
                UPDATE auctions 
                SET start_price = ?, current_price = ?, end_time = ?, status = 'PENDING'
                WHERE id = ?
                """;

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Bật Transaction bảo mật

            // BƯỚC 1: CẬP NHẬT BẢNG ITEMS
            try (PreparedStatement psItem = conn.prepareStatement(sqlUpdateItem)) {
                psItem.setString(1, item.getProductName());
                psItem.setString(2, item.getDescription());
                psItem.setString(3, item.getImageUrl());
                psItem.setString(4, item.getCategory());
                psItem.setString(5, item.getBrand());
                psItem.setString(6, item.getProductId()); // items.id is product_id
                psItem.executeUpdate();
            }

            // BƯỚC 2: CẬP NHẬT BẢNG AUCTIONS
            try (PreparedStatement psAuction = conn.prepareStatement(sqlUpdateAuction)) {
                psAuction.setDouble(1, item.getStartPrice());
                psAuction.setDouble(2, item.getStartPrice()); // reset currentPrice về startPrice khi sửa
                
                java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
                java.sql.Timestamp endTs = java.sql.Timestamp.from(item.getEndTime().atZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).toInstant());
                psAuction.setTimestamp(3, endTs, cal);
                psAuction.setString(4, item.getAuctionId());
                
                psAuction.executeUpdate();
            }

            conn.commit();
            logger.info("[DATABASE SUCCESS] Cập nhật thành công Auction ID: {}", item.getAuctionId());
            return true;

        } catch (Exception e) {
            logger.error("[DATABASE ERROR] Gặp sự cố cập nhật. Tiến hành khôi phục (Rollback)...", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception ignored) {}
            }
        }
    }
}