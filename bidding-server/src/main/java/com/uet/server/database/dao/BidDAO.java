package com.uet.server.database.dao;

import com.uet.common.network.BidRequest;
import com.uet.common.network.Response;
import com.uet.server.database.DBConnection;
import com.uet.server.util.IdGenerator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.UUID;
import com.uet.common.exception.AuctionClosedException;
import com.uet.common.exception.InvalidBidException;

public class BidDAO {

    public Response handleBid(BidRequest request) {
        String selectSql = """
                SELECT id, current_price, status, end_time
                FROM auctions
                WHERE id = ?
                FOR UPDATE
                """;

        String insertBidSql = """
                INSERT INTO bids(id, auction_id, user_id, bid_amount, bid_time)
                VALUES (?, ?, ?, ?, ?)
                """;

        String updateAuctionSql = """
                UPDATE auctions
                SET current_price = ?, winner_id = ?, end_time = ?
                WHERE id = ?
                """;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement selectPs = conn.prepareStatement(selectSql)) {
                selectPs.setString(1, request.getAuctionId());

                ResultSet rs = selectPs.executeQuery();

                if (!rs.next()) {
                    conn.rollback();
                    return Response.fail("Phiên đấu giá không tồn tại");
                }

                String status = rs.getString("status");
                double currentPrice = rs.getDouble("current_price");
                java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
                java.sql.Timestamp endTimeStamp = rs.getTimestamp("end_time", cal);

                if (!"RUNNING".equalsIgnoreCase(status) && !"ACTIVE".equalsIgnoreCase(status)) {
                    conn.rollback();
                    throw new AuctionClosedException("Phiên đấu giá đã đóng hoặc chưa bắt đầu.");
                }

                if (request.getAmount() <= currentPrice) {
                    conn.rollback();
                    throw new InvalidBidException("Giá đặt phải lớn hơn giá hiện tại.");
                }

                if (endTimeStamp != null) {
                    long endMillis = endTimeStamp.getTime();
                    long nowMillis = System.currentTimeMillis();
                    long diffMillis = endMillis - nowMillis;
                    if (diffMillis >= 0 && diffMillis <= 30000) {
                        endMillis += 10000;
                        endTimeStamp = new java.sql.Timestamp(endMillis);
                    }
                }

                String bidId = IdGenerator.generateId();

                try (PreparedStatement insertBidPs = conn.prepareStatement(insertBidSql)) {
                    insertBidPs.setString(1, bidId);
                    insertBidPs.setString(2, request.getAuctionId());
                    insertBidPs.setString(3, request.getBidderId());
                    insertBidPs.setDouble(4, request.getAmount());
                    insertBidPs.setObject(5, LocalDateTime.now());
                    insertBidPs.executeUpdate();
                }

                try (PreparedStatement updateAuctionPs = conn.prepareStatement(updateAuctionSql)) {
                    updateAuctionPs.setDouble(1, request.getAmount());
                    updateAuctionPs.setString(2, request.getBidderId());
                    updateAuctionPs.setTimestamp(3, endTimeStamp, cal);
                    updateAuctionPs.setString(4, request.getAuctionId());
                    updateAuctionPs.executeUpdate();
                }

                conn.commit();

                return Response.success("Đặt giá thành công", request.getAmount());

            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                return Response.fail("Đặt giá thất bại");
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail("Lỗi database");
        }
    }
}