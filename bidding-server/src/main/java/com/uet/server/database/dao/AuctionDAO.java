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

        
        String sqlAuction = """
                INSERT INTO auctions 
                (id, product_id, start_price, current_price, start_time, end_time, status) 
                VALUES (?, ?, ?, ?, ?, ?, 'PENDING')
                """;

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 

            
            String generatedItemId = com.uet.server.util.IdGenerator.generateId();

            
            String generatedAuctionId = com.uet.server.util.IdGenerator.generateId();

            
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

            
            try (PreparedStatement psAuction = conn.prepareStatement(sqlAuction)) {
                psAuction.setString(1, generatedAuctionId); 
                psAuction.setString(2, generatedItemId);    
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
            conn.setAutoCommit(false); 

            String bidId = com.uet.server.util.IdGenerator.generateId(); 

             
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
                    record.setUsername(rs.getString("username")); 
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
        
        String selectSql = "SELECT a.id, i.name, i.seller_id " +
                           "FROM auctions a JOIN items i ON a.product_id = i.id " +
                           "WHERE a.status = 'ACTIVE' AND a.start_time <= NOW()";
                           
        String sql = "UPDATE auctions SET status = 'RUNNING' " +
                "WHERE status = 'ACTIVE' AND start_time <= NOW()";
                
        try (java.sql.Connection conn = com.uet.server.database.DBConnection.getConnection();
             java.sql.PreparedStatement psSelect = conn.prepareStatement(selectSql);
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
             
            
            List<String[]> startingAuctions = new ArrayList<>();
            try (ResultSet rs = psSelect.executeQuery()) {
                while (rs.next()) {
                    startingAuctions.add(new String[]{
                        rs.getString("seller_id"),
                        rs.getString("name")
                    });
                }
            }

            
            int rows = ps.executeUpdate();
            if (rows > 0) {
                logger.info("[Scheduler] Đã kích hoạt {} phiên đấu giá sang trạng thái RUNNING!", rows);
                
                
                com.uet.server.database.dao.UserDAO userDAO = new com.uet.server.database.dao.UserDAO();
                for (String[] auctionInfo : startingAuctions) {
                    String sellerId = auctionInfo[0];
                    String productName = auctionInfo[1];
                    
                    
                    userDAO.deleteNotificationByKeyword(sellerId, "Sản phẩm '" + productName + "' đã được phê duyệt");
                    
                    
                    userDAO.createNotification(sellerId, "Đang đấu giá", "Sản phẩm '" + productName + "' đã bắt đầu phiên đấu giá.");
                }
            }
            return rows;
        } catch (Exception e) {
            logger.error("Lỗi khi kích hoạt các phiên đấu giá: ", e);
            return 0;
        }
    }

    
    public int finishExpiredAuctions() {
        
        
        String selectSql = "SELECT a.id AS auction_id, a.winner_id, a.current_price, i.seller_id, i.name " +
                "FROM auctions a " +
                "INNER JOIN items i ON a.product_id = i.id " +
                "WHERE a.status = 'RUNNING' AND a.end_time <= NOW()";

        String updateSql = "UPDATE auctions SET status = 'FINISHED' WHERE id = ?";

        
        com.uet.server.database.dao.WalletDAO walletDAO = new com.uet.server.database.dao.WalletDAO();
        com.uet.server.database.dao.UserDAO userDAO = new com.uet.server.database.dao.UserDAO();
        int finishedCount = 0;

        try (java.sql.Connection conn = com.uet.server.database.DBConnection.getConnection();
             java.sql.PreparedStatement psSelect = conn.prepareStatement(selectSql);
             java.sql.ResultSet rs = psSelect.executeQuery()) {

            while (rs.next()) {
                String auctionId = rs.getString("auction_id");
                String winnerId = rs.getString("winner_id");
                String sellerId = rs.getString("seller_id"); 
                String productName = rs.getString("name");
                double finalPrice = rs.getDouble("current_price");

                
                try (java.sql.PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                    psUpdate.setString(1, auctionId);
                    psUpdate.executeUpdate();
                }

                finishedCount++;
                
                
                userDAO.deleteNotificationByKeyword(sellerId, "Sản phẩm '" + productName + "' đã bắt đầu phiên đấu giá");

                
                userDAO.createNotification(sellerId, "Đấu giá kết thúc", "Sản phẩm '" + productName + "' đã kết thúc đấu giá. Đang chờ xử lý giao dịch.");

                
                if (winnerId != null && !winnerId.trim().isEmpty()) {

                    
                    boolean isDeducted = walletDAO.updateBalance(winnerId, -finalPrice);

                    
                    boolean isCredited = walletDAO.updateBalance(sellerId, finalPrice);

                    if (isDeducted && isCredited) {
                        logger.info("[Scheduler] Giao dịch thành công phiên {}:\n   -> Đã trừ {}đ từ người mua ({})\n   -> Đã cộng {}đ vào người bán ({})",
                                auctionId, finalPrice, winnerId, finalPrice, sellerId);
                                
                        
                        userDAO.createNotification(winnerId, "Trúng đấu giá", "Chúc mừng! Bạn đã trúng đấu giá sản phẩm '" + productName + "' với mức giá " + String.format("%,.0f", finalPrice) + "đ.");
                        userDAO.createNotification(sellerId, "Giao dịch thành công", "Sản phẩm '" + productName + "' đã được bán với giá " + String.format("%,.0f", finalPrice) + "đ.");
                        
                        
                        com.uet.common.model.user.User updatedWinner = userDAO.findUserByUserId(winnerId);
                        if (updatedWinner != null) {
                            com.uet.server.network.ClientManager.broadcast(com.uet.common.network.Response.success("BALANCE_UPDATED", updatedWinner));
                        }
                        com.uet.common.model.user.User updatedSeller = userDAO.findUserByUserId(sellerId);
                        if (updatedSeller != null) {
                            com.uet.server.network.ClientManager.broadcast(com.uet.common.network.Response.success("BALANCE_UPDATED", updatedSeller));
                        }
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
        String selectSql = "SELECT a.winner_id, a.current_price, i.seller_id, i.name, a.status " +
                "FROM auctions a " +
                "INNER JOIN items i ON a.product_id = i.id " +
                "WHERE a.id = ?";
        
        String updateSql = "UPDATE auctions SET status = 'FINISHED' WHERE id = ?";
        com.uet.server.database.dao.WalletDAO walletDAO = new com.uet.server.database.dao.WalletDAO();
        com.uet.server.database.dao.UserDAO userDAO = new com.uet.server.database.dao.UserDAO();
        
        try (Connection conn = DBConnection.getConnection()) {
            String winnerId = null;
            String sellerId = null;
            String productName = null;
            double finalPrice = 0;
            String status = null;
            
            try (PreparedStatement psSelect = conn.prepareStatement(selectSql)) {
                psSelect.setString(1, auctionId);
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) {
                        winnerId = rs.getString("winner_id");
                        sellerId = rs.getString("seller_id");
                        productName = rs.getString("name");
                        finalPrice = rs.getDouble("current_price");
                        status = rs.getString("status");
                    }
                }
            }
            
            if (status == null || !"RUNNING".equals(status)) {
                logger.warn("Không thể ép kết thúc phiên {} vì trạng thái hiện tại là: {}", auctionId, status);
                return false;
            }
            
            
            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setString(1, auctionId);
                int updatedRows = psUpdate.executeUpdate();
                if (updatedRows <= 0) {
                    return false;
                }
            }
            
            
            userDAO.deleteNotificationByKeyword(sellerId, "Sản phẩm '" + productName + "' đã bắt đầu phiên đấu giá");

            
            userDAO.createNotification(sellerId, "Đấu giá bị buộc kết thúc", "Phiên đấu giá sản phẩm '" + productName + "' đã bị Admin buộc kết thúc.");
            
            
            if (winnerId != null && !winnerId.trim().isEmpty()) {
                boolean isDeducted = walletDAO.updateBalance(winnerId, -finalPrice);
                boolean isCredited = walletDAO.updateBalance(sellerId, finalPrice);
                
                if (isDeducted && isCredited) {
                    logger.info("[Admin Force End] Giao dịch thành công phiên {}:\n   -> Đã trừ {}đ từ người mua ({})\n   -> Đã cộng {}đ vào người bán ({})",
                            auctionId, finalPrice, winnerId, finalPrice, sellerId);
                            
                    
                    userDAO.createNotification(winnerId, "Trúng đấu giá (Admin đóng)", "Phiên đấu giá bị đóng. Bạn đã trúng đấu giá sản phẩm '" + productName + "' với mức giá " + String.format("%,.0f", finalPrice) + "đ.");
                    userDAO.createNotification(sellerId, "Giao dịch thành công", "Sản phẩm '" + productName + "' đã được bán với giá " + String.format("%,.0f", finalPrice) + "đ.");
                    
                    
                    com.uet.common.model.user.User updatedWinner = userDAO.findUserByUserId(winnerId);
                    if (updatedWinner != null) {
                        com.uet.server.network.ClientManager.broadcast(com.uet.common.network.Response.success("BALANCE_UPDATED", updatedWinner));
                    }
                    com.uet.common.model.user.User updatedSeller = userDAO.findUserByUserId(sellerId);
                    if (updatedSeller != null) {
                        com.uet.server.network.ClientManager.broadcast(com.uet.common.network.Response.success("BALANCE_UPDATED", updatedSeller));
                    }
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
        
        String sqlGetProductId = "SELECT product_id FROM auctions WHERE id = ?";

        
        String sqlDeleteBids = "DELETE FROM bids WHERE auction_id = ?";

        
        String sqlDeleteAuction = "DELETE FROM auctions WHERE id = ?";

        
        String sqlDeleteItem = "DELETE FROM items WHERE id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 

            String productId = null;

            
            try (PreparedStatement psGet = conn.prepareStatement(sqlGetProductId)) {
                psGet.setString(1, auctionId);
                try (ResultSet rs = psGet.executeQuery()) {
                    if (rs.next()) {
                        productId = rs.getString("product_id");
                    }
                }
            }

            
            if (productId == null) {
                logger.warn("[DELETE WARN] Không tìm thấy phiên đấu giá với ID: {}", auctionId);
                return false;
            }

            
            try (PreparedStatement psDelBids = conn.prepareStatement(sqlDeleteBids)) {
                psDelBids.setString(1, auctionId);
                psDelBids.executeUpdate();
                logger.info("[DELETE] Đã xóa toàn bộ lượt bid liên quan ở bảng bids, ID: {}", auctionId);
            }

            
            try (PreparedStatement psDelAuction = conn.prepareStatement(sqlDeleteAuction)) {
                psDelAuction.setString(1, auctionId);
                psDelAuction.executeUpdate();
                logger.info("[DELETE] Đã xóa phiên đấu giá ở bảng auctions, ID: {}", auctionId);
            }

            
            try (PreparedStatement psDelItem = conn.prepareStatement(sqlDeleteItem)) {
                psDelItem.setString(1, productId);
                psDelItem.executeUpdate();
                logger.info("[DELETE] Đã xóa sản phẩm ở bảng items thành công, ID: {}", productId);
            }

            
            conn.commit();
            logger.info("[DELETE SUCCESS] Đã dọn dẹp sạch sẽ phiên {} và sản phẩm {} khỏi hệ thống!", auctionId, productId);
            return true;

        } catch (Exception e) {
            logger.error("[DELETE ERROR] Gặp sự cố khi thực thi xóa. Tiến hành khôi phục dữ liệu (Rollback)...", e);
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
            conn.setAutoCommit(false); 

            
            try (PreparedStatement psItem = conn.prepareStatement(sqlUpdateItem)) {
                psItem.setString(1, item.getProductName());
                psItem.setString(2, item.getDescription());
                psItem.setString(3, item.getImageUrl());
                psItem.setString(4, item.getCategory());
                psItem.setString(5, item.getBrand());
                psItem.setString(6, item.getProductId()); 
                psItem.executeUpdate();
            }

            
            try (PreparedStatement psAuction = conn.prepareStatement(sqlUpdateAuction)) {
                psAuction.setDouble(1, item.getStartPrice());
                psAuction.setDouble(2, item.getStartPrice()); 
                
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