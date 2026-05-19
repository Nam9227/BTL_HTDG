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

public class BidDAO {

    public Response handleBid(BidRequest request) {
        String selectSql = """
                SELECT id, current_price, status
                FROM auctions
                WHERE id = ?
                FOR UPDATE
                """;

        String insertBidSql = """
                INSERT INTO bids(id, auction_id, bidder_id, amount, bid_time)
                VALUES (?, ?, ?, ?, ?)
                """;

        String updateAuctionSql = """
                UPDATE auctions
                SET current_price = ?, winner_id = ?
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

                if (!"RUNNING".equalsIgnoreCase(status) && !"ACTIVE".equalsIgnoreCase(status)) {
                    conn.rollback();
                    return Response.fail("Phiên đấu giá không hoạt động");
                }

                if (request.getAmount() <= currentPrice) {
                    conn.rollback();
                    return Response.fail("Giá đặt phải lớn hơn giá hiện tại");
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
                    updateAuctionPs.setString(3, request.getAuctionId());
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