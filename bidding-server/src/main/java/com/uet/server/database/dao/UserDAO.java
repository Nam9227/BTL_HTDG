package com.uet.server.database.dao;

import com.uet.common.model.transaction.Transaction;
import com.uet.common.model.user.Role;
import com.uet.common.model.user.User;
import com.uet.common.network.LoginRequest;
import com.uet.common.network.Response;
import com.uet.common.network.UpdateProfileRequest;
import com.uet.server.database.DBConnection;
import com.uet.server.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import com.uet.common.model.notification.Notification;

public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    private final FileStorageService fileStorageService = new FileStorageService();

    public Response handleLogin(LoginRequest request) {
        User user = findUserByUsernameAndPassword(
                request.getUsername(),
                request.getPassword());

        if (user == null) {
            return Response.fail("Sai tài khoản hoặc mật khẩu");
        }

        if (!user.getActive()) {
            return Response.fail("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.");
        }

        
        String updateSql = "UPDATE users SET last_login = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setString(1, user.getId());
            ps.executeUpdate();

            
            user.setLastLoginAt(LocalDateTime.now());

            logger.info("[UserDAO] ⏰ Đã cập nhật mốc đăng nhập mới cho user ID: {}", user.getId());

        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật thời gian đăng nhập: ", e);
        }

        return Response.success("Đăng nhập thành công", user);
    }

    public java.util.List<User> getAllUsers() {
        java.util.List<User> userList = new java.util.ArrayList<>();

        String sql = """
                SELECT
                    u.id,
                    u.username,
                    u.role,
                    u.active,
                    u.last_login,
                    p.full_name,
                    p.email,
                    p.phone_number,
                    p.address,
                    p.avatar_path,
                    COALESCE(w.balance, 0) AS balance
                FROM users u
                LEFT JOIN user_profiles p ON u.id = p.user_id
                LEFT JOIN wallet w ON u.id = w.user_id
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone_number"));
                user.setAddress(rs.getString("address"));
                user.setAvatarPath(rs.getString("avatar_path"));
                user.setRole(parseRole(rs.getString("role")));
                user.setActive(rs.getBoolean("active"));
                user.setBalance(rs.getBigDecimal("balance"));

                LocalDateTime timestamp = rs.getObject("last_login", LocalDateTime.class);
                if (timestamp != null) {
                    user.setLastLoginAt(timestamp);
                } else {
                    user.setLastLoginAt(null);
                }

                userList.add(user);
            }
            logger.info("[UserDAO] Đã tải thành công {} người dùng cho Admin.", userList.size());

        } catch (Exception e) {
            logger.error("Lỗi xảy ra khi lấy toàn bộ danh sách user từ DB: ", e);
        }

        return userList;
    }

    public void updateRole(String userId, Role role) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (role == null) {
                ps.setNull(1, java.sql.Types.VARCHAR);
            } else {
                ps.setString(1, role.name());
            }

            ps.setString(2, userId);
            ps.executeUpdate();

        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật quyền cho User ID: " + userId, e);
        }
    }

    public void deleteUser(String userId) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                logger.info("[UserDAO] Đã xóa thành công user ID: {} khỏi Database.", userId);
            }

        } catch (Exception e) {
            logger.error("Lỗi khi xóa user trong UserDAO: ", e);
        }
    }

    public void updateActive(String userId, boolean active) {
        String sql = "UPDATE users SET active = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, active);
            ps.setString(2, userId);
            ps.executeUpdate();

        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật trạng thái hoạt động cho User ID: " + userId, e);
        }
    }

    public Response updateProfile(UpdateProfileRequest request) {
        String oldAvatarPath = getAvatarPathByUserId(request.getUserId());
        String avatarPath = null;

        if (request.getAvatar() != null && request.getAvatar().getData() != null
                && request.getAvatar().getData().length > 0) {
            try {
                String originalName = request.getAvatar().getOriginalFileName();
                String uniqueName = System.currentTimeMillis() + "_" + originalName;
                request.getAvatar().setOriginalFileName(uniqueName);

                avatarPath = fileStorageService.save(
                        request.getAvatar(),
                        "avatars",
                        request.getUserId());
                logger.info("🔄 Server đã lưu file mới tại path: {}", avatarPath);

                if (oldAvatarPath != null && !oldAvatarPath.isBlank()) {
                    String cleanOldPath = oldAvatarPath.replace("file://", "").replace("file:///", "/");
                    java.io.File oldFile = new java.io.File(cleanOldPath);

                    if (oldFile.exists()) {
                        boolean deleted = oldFile.delete();
                        if (deleted) {
                            logger.info("[Tối ưu đĩa] Đã xóa thành công file avatar cũ trên VPS: {}", cleanOldPath);
                        } else {
                            logger.warn("Không thể xóa file cũ (có thể đang bị luồng khác chiếm dụng): {}",
                                    cleanOldPath);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Lỗi khi xử lý lưu file mới hoặc xóa file cũ: ", e);
            }
        }

        String sql;
        if (avatarPath != null) {
            sql = "UPDATE user_profiles SET full_name = ?, email = ?, phone_number = ?, address = ?, avatar_path = ? WHERE user_id = ?";
        } else {
            sql = "UPDATE user_profiles SET full_name = ?, email = ?, phone_number = ?, address = ? WHERE user_id = ?";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, request.getFullName());
            ps.setString(2, request.getEmail());
            ps.setString(3, request.getPhoneNumber());
            ps.setString(4, request.getAddress());

            if (avatarPath != null) {
                ps.setString(5, avatarPath);
                ps.setString(6, request.getUserId());
            } else {
                ps.setString(5, request.getUserId());
            }

            int rows = ps.executeUpdate();

            if (rows > 0) {
                User updatedUser = findUserByUserId(request.getUserId());
                if (updatedUser != null) {
                    return Response.success("Cập nhật thông tin tài khoản thành công!", updatedUser);
                }
            }

            return Response.fail("Không tìm thấy hồ sơ người dùng để cập nhật!");

        } catch (Exception e) {
            logger.error("Lỗi server khi cập nhật thông tin tài khoản cho User ID: " + request.getUserId(), e);
            return Response.fail("Lỗi server khi cập nhật thông tin tài khoản!");
        }
    }

    private String getAvatarPathByUserId(String userId) {
        String sql = "SELECT avatar_path FROM user_profiles WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("avatar_path");
            }

        } catch (Exception e) {
            logger.error("Lỗi khi lấy avatar path cho User ID: " + userId, e);
        }
        return null;
    }

    private User findUserByUsernameAndPassword(String username, String password) {
        String sql = """
                SELECT
                    u.id,
                    u.username,
                    u.role,
                    u.active,
                    p.full_name,
                    p.email,
                    p.phone_number,
                    p.address,
                    p.avatar_path,
                    COALESCE(w.balance, 0) AS balance
                FROM users u
                LEFT JOIN user_profiles p ON u.id = p.user_id
                LEFT JOIN wallet w ON u.id = w.user_id
                WHERE u.username = ? AND u.password = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Role role = parseRole(rs.getString("role"));
                BigDecimal balance = rs.getBigDecimal("balance");

                User user = new User();
                user.setId(rs.getString("id"));
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone_number"));
                user.setAddress(rs.getString("address"));
                user.setAvatarPath(rs.getString("avatar_path"));
                user.setRole(role);
                user.setActive(rs.getBoolean("active"));
                user.setBalance(balance);

                String avatarPath = rs.getString("avatar_path");
                if (avatarPath != null && !avatarPath.isBlank()) {
                    try {
                        byte[] bytes = null;
                        if (avatarPath.startsWith("file://")) {
                            String p = avatarPath.replaceFirst("^file:///*", "/");
                            java.nio.file.Path filePath = java.nio.file.Paths.get(p);
                            if (java.nio.file.Files.exists(filePath)) {
                                bytes = java.nio.file.Files.readAllBytes(filePath);
                            } else {
                                logger.warn("UserDAO: file not found: {}", filePath);
                            }
                        } else if (avatarPath.startsWith("http://") || avatarPath.startsWith("https://")) {
                            try (java.io.InputStream in = new java.net.URL(avatarPath).openStream()) {
                                bytes = in.readAllBytes();
                            } catch (Exception e) {
                                logger.error("UserDAO: cannot download avatar from url: {}", avatarPath, e);
                            }
                        } else {
                            java.nio.file.Path filePath = java.nio.file.Paths.get(avatarPath);
                            if (java.nio.file.Files.exists(filePath)) {
                                bytes = java.nio.file.Files.readAllBytes(filePath);
                            }
                        }

                        if (bytes != null && bytes.length > 0) {
                            user.setAvatarBytes(bytes);
                            logger.info("UserDAO: loaded avatar bytes len={} for user {}", bytes.length, user.getId());
                        }
                    } catch (Exception e) {
                        logger.error("UserDAO: error loading avatar for user " + user.getId(), e);
                    }
                }
                return user;
            }

        } catch (Exception e) {
            logger.error("Lỗi khi tìm user bằng username/password: ", e);
        }
        return null;
    }

    private Role parseRole(String roleStr) {
        if (roleStr == null || roleStr.isBlank()) {
            return null;
        }
        try {
            return Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public User findUserByUserId(String userId) {
        String sql = """
                SELECT
                    u.id, u.username, u.role, u.active, u.last_login,
                    p.full_name, p.email, p.phone_number, p.address, p.avatar_path,
                    COALESCE(w.balance, 0) AS balance
                FROM users u
                LEFT JOIN user_profiles p ON u.id = p.user_id
                LEFT JOIN wallet w ON u.id = w.user_id
                WHERE u.id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone_number"));
                user.setAddress(rs.getString("address"));
                user.setAvatarPath(rs.getString("avatar_path"));
                user.setRole(parseRole(rs.getString("role")));
                user.setActive(rs.getBoolean("active"));
                user.setBalance(rs.getBigDecimal("balance"));

                LocalDateTime timestamp = rs.getObject("last_login", LocalDateTime.class);
                if (timestamp != null) {
                    user.setLastLoginAt(timestamp);
                } else {
                    user.setLastLoginAt(null);
                }

                String avatarPath = rs.getString("avatar_path");
                if (avatarPath != null && !avatarPath.isBlank()) {
                    try {
                        byte[] bytes = null;
                        if (avatarPath.startsWith("file://")) {
                            String p = avatarPath.replaceFirst("^file:///*", "/");
                            java.nio.file.Path filePath = java.nio.file.Paths.get(p);
                            if (java.nio.file.Files.exists(filePath)) {
                                bytes = java.nio.file.Files.readAllBytes(filePath);
                            }
                        } else {
                            java.nio.file.Path filePath = java.nio.file.Paths.get(avatarPath);
                            if (java.nio.file.Files.exists(filePath)) {
                                bytes = java.nio.file.Files.readAllBytes(filePath);
                            }
                        }
                        if (bytes != null && bytes.length > 0) {
                            user.setAvatarBytes(bytes);
                        }
                    } catch (Exception e) {
                        logger.error("Lỗi khi đọc file avatar cho User ID: " + userId, e);
                    }
                }
                return user;
            }
        } catch (Exception e) {
            logger.error("Lỗi khi tìm user bằng User ID: " + userId, e);
        }
        return null;
    }

    public void createTransaction(String userId, double amount, String type) {
        String sql = "INSERT INTO transactions (user_id, amount, type, status, created_at) VALUES (?, ?, ?, 'PENDING', NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setDouble(2, amount);
            ps.setString(3, type);
            ps.executeUpdate();

            String actionStr = "DEPOSIT".equals(type) ? "nạp tiền" : "rút tiền";
            String title = "Yêu cầu " + actionStr + " đang chờ duyệt";
            String content = "Yêu cầu " + actionStr + " số tiền " + String.format("%,.0f", amount)
                    + "đ của bạn đã được gửi và đang chờ Admin xử lý.";
            createNotification(userId, title, content);

        } catch (Exception e) {
            logger.error("Lỗi khi tạo transaction mới: ", e);
        }
    }

    public List<Transaction> getPendingTransaction() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE status = 'PENDING'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Transaction t = new Transaction(
                        rs.getLong("id"),
                        rs.getString("user_id"),
                        rs.getDouble("amount"),
                        rs.getString("type"),
                        rs.getObject("created_at", LocalDateTime.class).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        rs.getString("status"));
                list.add(t);
            }
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách pending transactions: ", e);
        }
        return list;
    }

    
    public User deposit(Connection conn, String userId, double amount) throws Exception {
        if (amount <= 0) {
            logger.warn("Số tiền nạp không hợp lệ: {}", amount);
            return null;
        }
        String sql = "UPDATE wallet SET balance = balance + ? WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setString(2, userId);
            int rows = ps.executeUpdate();
            if (rows == 0) return null;
            return findUserByUserId(userId);
        }
    }

    
    public User withdraw(Connection conn, String userId, double amount) throws Exception {
        String balanceSql = "SELECT balance FROM wallet WHERE user_id = ?";
        try (PreparedStatement balancePs = conn.prepareStatement(balanceSql)) {
            balancePs.setString(1, userId);
            try (ResultSet rs = balancePs.executeQuery()) {
                if (rs.next()) {
                    double balance = rs.getDouble("balance");
                    if (amount > balance) {
                        logger.warn("Số dư không đủ để rút! User: {}, Số dư: {}, Cần rút: {}", userId, balance, amount);
                        return null; 
                    }
                } else {
                    return null; 
                }
            }
        }

        String sql = "UPDATE wallet SET balance = balance - ? WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setString(2, userId);
            int rows = ps.executeUpdate();
            if (rows == 0) return null;
            return findUserByUserId(userId);
        }
    }

    
    public User approveTransaction(long transactionId) {
        String selectSql = "SELECT * FROM transactions WHERE id = ?";
        String updateSql = "UPDATE transactions SET status = 'APPROVED' WHERE id = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement updatePs = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            
            conn.setAutoCommit(false);

            ps = conn.prepareStatement(selectSql);
            ps.setLong(1, transactionId);
            rs = ps.executeQuery();

            if (rs.next()) {
                String userId = rs.getString("user_id");
                double amount = rs.getDouble("amount");
                String type = rs.getString("type");

                
                User updatedUser = null;
                if ("DEPOSIT".equals(type)) {
                    updatedUser = deposit(conn, userId, amount);
                } else if ("WITHDRAW".equals(type)) {
                    updatedUser = withdraw(conn, userId, amount);
                }

                
                if (updatedUser == null) {
                    conn.rollback();
                    return null;
                }

                
                updatePs = conn.prepareStatement(updateSql);
                updatePs.setLong(1, transactionId);
                updatePs.executeUpdate();

                
                conn.commit();

                
                updatedUser = findUserByUserId(userId);

                
                String actionStr = "DEPOSIT".equals(type) ? "nạp tiền" : "rút tiền";
                String title = "Giao dịch " + actionStr + " thành công";
                String content = "Yêu cầu " + actionStr + " số tiền " + String.format("%,.0f", amount)
                        + "đ của bạn đã được phê duyệt.";

                deleteNotificationByKeyword(userId, "Yêu cầu " + actionStr + " số tiền "
                        + String.format("%,.0f", amount) + "đ của bạn đã được gửi");
                createNotification(userId, title, content);

                return updatedUser;
            } else {
                conn.rollback();
            }

        } catch (Exception e) {
            logger.error("Lỗi xảy ra tại approveTransaction ID: " + transactionId + ", tiến hành rollback!", e);
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
        } finally {
            
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (Exception ex) { ex.printStackTrace(); }
            }
            try { if (rs != null) rs.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (updatePs != null) updatePs.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (Exception e) { e.printStackTrace(); }
        }
        return null;
    }

    public User rejectTransaction(long transactionId) {

        String selectSql =
                "SELECT * FROM transactions WHERE id = ?";

        String updateSql =
                "UPDATE transactions " +
                        "SET status = 'REJECTED' " +
                        "WHERE id = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement updatePs = null;
        ResultSet rs = null;

        try {

            conn = DBConnection.getConnection();

            conn.setAutoCommit(false);

            ps = conn.prepareStatement(selectSql);
            ps.setLong(1, transactionId);

            rs = ps.executeQuery();

            if (rs.next()) {

                String userId = rs.getString("user_id");
                double amount = rs.getDouble("amount");
                String type = rs.getString("type");

                updatePs = conn.prepareStatement(updateSql);
                updatePs.setLong(1, transactionId);

                updatePs.executeUpdate();

                conn.commit();

                String actionStr =
                        "DEPOSIT".equals(type)
                                ? "nạp tiền"
                                : "rút tiền";

                String title =
                        "Giao dịch " + actionStr + " bị từ chối";

                String content =
                        "Yêu cầu " + actionStr +
                                " số tiền " +
                                String.format("%,.0f", amount) +
                                "đ của bạn đã bị từ chối.";

                deleteNotificationByKeyword(
                        userId,
                        "Yêu cầu " + actionStr +
                                " số tiền " +
                                String.format("%,.0f", amount) +
                                "đ của bạn đã được gửi"
                );

                createNotification(
                        userId,
                        title,
                        content
                );

            } else {
                conn.rollback();
            }

        } catch (Exception e) {

            logger.error(
                    "Lỗi xảy ra tại rejectTransaction ID: "
                            + transactionId,
                    e
            );

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        } finally {

            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            try {
                if (rs != null) rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (ps != null) ps.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (updatePs != null) updatePs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public List<Notification> getNotificationsByUserId(String userId) {
        String deleteOldSql = "DELETE FROM notifications WHERE user_id = ? AND created_at < DATE_SUB(NOW(), INTERVAL 3 DAY)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteOldSql)) {
            ps.setString(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            logger.error("Lỗi khi xóa thông báo cũ hơn 3 ngày của user: " + userId, e);
        }

        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Notification(
                            rs.getInt("id"),
                            rs.getString("user_id"),
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getBoolean("is_read"),
                            rs.getTimestamp("created_at")));
                }
            }
        } catch (Exception e) {
            logger.error("Lỗi lấy danh sách thông báo: ", e);
        }
        return list;
    }

    public void createNotification(String userId, String title, String content) {
        String sql = "INSERT INTO notifications (user_id, title, content, is_read, created_at) VALUES (?, ?, ?, 0, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, title);
            ps.setString(3, content);
            ps.executeUpdate();
            logger.info("[Notification] Đã tạo thông báo mới cho User {}: {}", userId, title);
        } catch (Exception e) {
            logger.error("Lỗi khi tạo thông báo cho User: " + userId, e);
        }
    }

    public void deleteNotificationByKeyword(String userId, String keyword) {
        String sql = "DELETE FROM notifications WHERE user_id = ? AND content LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, "%" + keyword + "%");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                logger.info("[Notification] Đã xóa {} thông báo cũ chứa từ khóa '{}' của User {}", rows, keyword, userId);
            }
        } catch (Exception e) {
            logger.error("Lỗi khi xóa thông báo cũ cho User: " + userId, e);
        }
    }

    public Response changePassword(String userId, String oldPassword, String newPassword) {
        String checkSql = "SELECT password FROM users WHERE id = ?";
        String updateSql = "UPDATE users SET password = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
            
            psCheck.setString(1, userId);
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next()) {
                    String currentPassword = rs.getString("password");
                    if (!currentPassword.equals(oldPassword)) {
                        return Response.fail("Mật khẩu cũ không chính xác!");
                    }
                    
                    try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                        psUpdate.setString(1, newPassword);
                        psUpdate.setString(2, userId);
                        int rows = psUpdate.executeUpdate();
                        if (rows > 0) {
                            return Response.success("Đổi mật khẩu thành công!", null);
                        }
                    }
                } else {
                    return Response.fail("Không tìm thấy người dùng!");
                }
            }
        } catch (Exception e) {
            logger.error("Lỗi khi đổi mật khẩu cho User ID: " + userId, e);
        }
        return Response.fail("Lỗi hệ thống khi đổi mật khẩu!");
    }
}