package com.uet.server.database.dao;

import com.uet.common.model.auction.AuctionItem;
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
                WHERE a.status IN ('RUNNING', 'ACTIVE')
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
}