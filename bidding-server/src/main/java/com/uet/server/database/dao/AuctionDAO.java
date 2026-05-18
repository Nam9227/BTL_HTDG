package com.uet.server.database.dao;

import com.uet.common.model.auction.AuctionItem;
import com.uet.server.database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {

    public List<AuctionItem> getActiveAuctions() {

        List<AuctionItem> list = new ArrayList<>();

        String sql = """
                SELECT
                    a.id AS auction_id,
                    p.id AS product_id,
                    p.name,
                    p.description,
                    p.image_url,
                    p.seller_id,
                    a.start_price,
                    a.current_price,
                    a.winner_id,
                    a.end_time,
                    a.status
                FROM auctions a
                JOIN products p ON a.product_id = p.id
                WHERE a.status = 'ACTIVE'
                ORDER BY a.end_time ASC
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                AuctionItem item = new AuctionItem();

                item.setAuctionId(rs.getInt("auction_id"));
                item.setProductId(rs.getInt("product_id"));

                item.setProductName(rs.getString("name"));
                item.setDescription(rs.getString("description"));
                item.setImageUrl(rs.getString("image_url"));

                item.setSellerId(rs.getInt("seller_id"));

                item.setStartPrice(rs.getDouble("start_price"));
                item.setCurrentPrice(rs.getDouble("current_price"));

                item.setWinnerId(rs.getInt("winner_id"));

                item.setEndTime(
                        rs.getTimestamp("end_time").toLocalDateTime()
                );

                item.setStatus(rs.getString("status"));

                list.add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}