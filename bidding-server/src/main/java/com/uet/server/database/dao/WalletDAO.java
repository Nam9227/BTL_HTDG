package com.uet.server.database.dao;

import com.uet.server.database.DBConnection;
import com.uet.common.network.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class WalletDAO {

    



    public double getBalance(String userId) {
        String sql = "SELECT balance FROM wallet WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    




    public double getFrozenBalance(String userId) {
        String sql = "SELECT SUM(current_price) AS total_frozen FROM auctions " +
                "WHERE winner_id = ? AND status = 'RUNNING'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_frozen"); 
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public double getFrozenBalanceExcludeCurrent(String userId, String excludeAuctionId) {
        
        String sql = "SELECT SUM(current_price) AS total_frozen FROM auctions " +
                "WHERE winner_id = ? AND status = 'RUNNING' AND id != ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setString(2, excludeAuctionId); 

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_frozen");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    



    public double getAvailableBalanceForAuction(String userId, String auctionId) {
        double total = getBalance(userId);
        double frozenExceptCurrent = getFrozenBalanceExcludeCurrent(userId, auctionId);
        double available = total - frozenExceptCurrent;
        return available < 0 ? 0.0 : available;
    }
    



    public double getAvailableBalance(String userId) {
        double total = getBalance(userId);
        double frozen = getFrozenBalance(userId);
        double available = total - frozen;
        return available < 0 ? 0.0 : available; 
    }

    



    public boolean updateBalance(String userId, double amount) {
        
        String sql = "UPDATE wallet SET balance = balance + ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, amount);
            ps.setString(2, userId);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    


    public Response handleDeposit(String userId, double amount) {
        if (amount <= 0) {
            return Response.fail("Số tiền nạp phải lớn hơn 0");
        }
        boolean success = updateBalance(userId, amount);
        if (success) {
            return Response.success("Nạp tiền vào ví thành công!", getBalance(userId));
        }
        return Response.fail("Lỗi hệ thống, không thể nạp tiền.");
    }

    



    public Response handleWithdraw(String userId, double amount) {
        if (amount <= 0) {
            return Response.fail("Số tiền rút phải lớn hơn 0");
        }

        double available = getAvailableBalance(userId);
        if (amount > available) {
            return Response.fail("Số dư khả dụng không đủ! Bạn đang có tiền bị đóng băng ở phiên đấu giá khác.");
        }

        
        boolean success = updateBalance(userId, -amount);
        if (success) {
            return Response.success("Rút tiền thành công!", getBalance(userId));
        }
        return Response.fail("Lỗi hệ thống, không thể rút tiền.");
    }
}