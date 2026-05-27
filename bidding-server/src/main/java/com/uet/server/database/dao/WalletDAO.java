package com.uet.server.database.dao;

import com.uet.server.database.DBConnection;
import com.uet.common.network.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class WalletDAO {

    /**
     * 1. LẤY TỔNG SỐ DƯ THỰC TẾ TRONG VÍ
     * Đọc trực tiếp số tiền đang có trong bảng wallet của người dùng
     */
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

    /**
     * 2. LẤY SỐ TIỀN ĐANG BỊ ĐÓNG BĂNG (SỐ DƯ ẢO)
     * Quét toàn bộ các phiên đang chạy (RUNNING) mà user này đang dẫn đầu (winner_id).
     * Tổng số tiền này sẽ tạm thời không được dùng để đi đấu giá sản phẩm khác.
     */
    public double getFrozenBalance(String userId) {
        String sql = "SELECT SUM(current_price) AS total_frozen FROM auctions " +
                "WHERE winner_id = ? AND status = 'RUNNING'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_frozen"); // Trả về tổng tiền đang giữ, nếu không có sẽ trả về 0
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public double getFrozenBalanceExcludeCurrent(String userId, String excludeAuctionId) {
        // Thêm điều kiện: AND id != ? để không tính tiền bị giam của chính phiên này
        String sql = "SELECT SUM(current_price) AS total_frozen FROM auctions " +
                "WHERE winner_id = ? AND status = 'RUNNING' AND id != ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setString(2, excludeAuctionId); // Loại trừ phiên đang đứng ra

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

    /**
     * 3b. TÍNH TOÁN SỐ DƯ KHẢ DỤNG KHI ĐANG ĐỨNG TRONG MỘT PHÒNG CỤ THỂ
     * Công thức: Available = Total - (Frozen - Tiền_Phiên_Hiện_Tại)
     */
    public double getAvailableBalanceForAuction(String userId, String auctionId) {
        double total = getBalance(userId);
        double frozenExceptCurrent = getFrozenBalanceExcludeCurrent(userId, auctionId);
        double available = total - frozenExceptCurrent;
        return available < 0 ? 0.0 : available;
    }
    /**
     * 3. TÍNH TOÁN SỐ DƯ KHẢ DỤNG (TIỀN THỰC SỰ CÓ THỂ XÀI LÚC NÀY)
     * Công thức: Available = Total - Frozen
     */
    public double getAvailableBalance(String userId) {
        double total = getBalance(userId);
        double frozen = getFrozenBalance(userId);
        double available = total - frozen;
        return available < 0 ? 0.0 : available; // Đảm bảo không bao giờ bị âm do sai số
    }

    /**
     * 4. CẬP NHẬT TĂNG/GIẢM SỐ DƯ THỰC TẾ (Nạp tiền / Trừ tiền khi thắng cuộc)
     * Dùng để cộng tiền khi nạp, hoặc trừ hẳn tiền khi phiên đấu giá kết thúc thực sự.
     */
    public boolean updateBalance(String userId, double amount) {
        // Lệnh này cộng/trừ trực tiếp vào số dư gốc trong DB
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

    /**
     * 5. XỬ LÝ LỆNH NẠP TIỀN (DEPOSIT)
     */
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

    /**
     * 6. XỬ LÝ LỆNH RÚT TIỀN (WITHDRAW)
     * Chỉ được rút trong phạm vi SỐ DƯ KHẢ DỤNG (Không được rút khoản tiền đang đi đóng băng đấu giá)
     */
    public Response handleWithdraw(String userId, double amount) {
        if (amount <= 0) {
            return Response.fail("Số tiền rút phải lớn hơn 0");
        }

        double available = getAvailableBalance(userId);
        if (amount > available) {
            return Response.fail("Số dư khả dụng không đủ! Bạn đang có tiền bị đóng băng ở phiên đấu giá khác.");
        }

        // Rút tiền đồng nghĩa với việc cộng một số âm vào tài khoản
        boolean success = updateBalance(userId, -amount);
        if (success) {
            return Response.success("Rút tiền thành công!", getBalance(userId));
        }
        return Response.fail("Lỗi hệ thống, không thể rút tiền.");
    }
}