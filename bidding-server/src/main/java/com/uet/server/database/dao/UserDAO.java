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
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    private final FileStorageService fileStorageService = new FileStorageService();

    public Response handleLogin(LoginRequest request) {
        User user = findUserByUsernameAndPassword(
                request.getUsername(),
                request.getPassword()
        );

        if (user == null) {
            return Response.fail("Sai tài khoản hoặc mật khẩu");
        }

        if (!user.getActive()) {
            return Response.fail("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.");
        }

        // --- CẬP NHẬT THỜI GIAN ĐĂNG NHẬP GẦN NHẤT VÀO DATABASE ---
        String updateSql = "UPDATE users SET last_login = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setString(1, user.getId());
            ps.executeUpdate();

            // Đồng bộ luôn mốc thời gian này vào object user để trả về Client
            user.setLastLoginAt(LocalDateTime.now());

            logger.info("[UserDAO] ⏰ Đã cập nhật mốc đăng nhập mới cho user ID: {}", user.getId());
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật thời gian đăng nhập: ", e);
        }

        return Response.success("Đăng nhập thành công", user);
    }
    public java.util.List<User> getAllUsers() {
        java.util.List<User> userList = new java.util.ArrayList<>();

        // Đã thêm u.last_login_at vào câu SELECT
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

                // --- ĐỌC TRƯỜNG LAST_LOGIN_AT TỪ RESULTSET VÀ CHUYỂN THÀNH LOCALDATETIME ---
                java.sql.Timestamp timestamp = rs.getTimestamp("last_login");
                if (timestamp != null) {
                    user.setLastLoginAt(timestamp.toLocalDateTime());
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
        // Lệnh xóa tài khoản dựa trên ID
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
        // 1. LẤY PATH AVATAR CŨ TRONG DATABASE RA TRƯỚC ĐỂ TÝ NỮA XÓA
        String oldAvatarPath = getAvatarPathByUserId(request.getUserId());
        String avatarPath = null;

        // Kiểm tra xem Client có thực sự gửi ảnh mới lên không
        if (request.getAvatar() != null && request.getAvatar().getData() != null && request.getAvatar().getData().length > 0) {
            try {
                // 2. ÉP ĐỔI TÊN FILE MỚI BẰNG TIMESTAMP ĐỂ ĐÁNH LỪA CACHE JAVAFX
                String originalName = request.getAvatar().getOriginalFileName();
                String uniqueName = System.currentTimeMillis() + "_" + originalName;
                request.getAvatar().setOriginalFileName(uniqueName);

                // 3. LƯU FILE MỚI VÀO VPS
                avatarPath = fileStorageService.save(
                        request.getAvatar(),
                        "avatars",
                        request.getUserId()
                );
                logger.info("🔄 Server đã lưu file mới tại path: {}", avatarPath);

                // 4. TIẾN HÀNH XÓA FILE CŨ TRÊN ĐĨA CỦA VPS ĐỂ TRÁNH RÁC BỘ NHỚ
                if (oldAvatarPath != null && !oldAvatarPath.isBlank()) {
                    // Xử lý chuẩn hóa chuỗi đường dẫn nếu đường dẫn chứa tiền tố file://
                    String cleanOldPath = oldAvatarPath.replace("file://", "").replace("file:///", "/");
                    java.io.File oldFile = new java.io.File(cleanOldPath);

                    if (oldFile.exists()) {
                        boolean deleted = oldFile.delete();
                        if (deleted) {
                            logger.info("[Tối ưu đĩa] Đã xóa thành công file avatar cũ trên VPS: {}", cleanOldPath);
                        } else {
                            logger.warn("Không thể xóa file cũ (có thể đang bị luồng khác chiếm dụng): {}", cleanOldPath);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Lỗi khi xử lý lưu file mới hoặc xóa file cũ: ", e);
            }
        }

        // 5. CẬP NHẬT THÔNG TIN VÀO MYSQL (Giữ nguyên logic SQL động bám sát cấu trúc của Nam)
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
                // 6. BUILD LẠI ĐỐI TƯỢNG USER MỚI NHẤT ĐỂ TRẢ VỀ CHO CLIENT ĐỒNG BỘ UI
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

                // ✅ Đọc ảnh từ disk nếu có avatar_path
                String avatarPath = rs.getString("avatar_path");
                if (avatarPath != null && !avatarPath.isBlank()) {
                    try {
                        byte[] bytes = null;
                        if (avatarPath.startsWith("file://")) {
                            // normalize: remove prefix file:// or file:///
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
                            // assume plain filesystem path
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

    private User findUserByUserId(String userId) {
        // Đã thêm u.last_login_at vào câu SELECT
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

                // --- ĐỌC TRƯỜNG LAST_LOGIN_AT ---
                java.sql.Timestamp timestamp = rs.getTimestamp("last_login");
                if (timestamp != null) {
                    user.setLastLoginAt(timestamp.toLocalDateTime());
                } else {
                    user.setLastLoginAt(null);
                }

                // Đọc ảnh ra mảng byte y hệt hàm tìm theo Username
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
    public void createTransaction(String userId,
                                  double amount,
                                  String type){

        try{

            Connection conn = DBConnection.getConnection();

            String sql =
                    "INSERT INTO transactions " +
                            "(user_id, amount, type, status) " +
                            "VALUES (?, ?, ?, ?)";

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setString(1, userId);

            ps.setDouble(2, amount);

            ps.setString(3, type);

            ps.setString(4, "PENDING");

            ps.executeUpdate();

        } catch (Exception e){

            e.printStackTrace();
        }
    }

    public List<Transaction> getPendingTransaction(){

        List<Transaction> list =
                new ArrayList<>();

        try{

            Connection conn =
                    DBConnection.getConnection();

            String sql =
                    "SELECT * FROM transactions " +
                            "WHERE status = 'PENDING'";

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ResultSet rs =
                    ps.executeQuery();

            while(rs.next()){

                Transaction t =
                        new Transaction(rs.getLong("id"),
                                rs.getString("user_id"),
                                rs.getDouble("amount"),
                                rs.getString("type"),
                                rs.getString("status"),
                                rs.getTimestamp("created_at").toLocalDateTime().toString()
                        );

                t.setId(rs.getLong("id"));

                t.setUserId(
                        rs.getString("user_id")
                );

                t.setAmount(
                        rs.getDouble("amount")
                );

                t.setType(
                        rs.getString("type")
                );

                t.setStatus(
                        rs.getString("status")
                );

                list.add(t);
            }

        } catch (Exception e){

            e.printStackTrace();
        }

        return list;
    }


    public User deposit(String userId, double amount){
        try{
            Connection conn = DBConnection.getConnection();

            String sql =
                    "UPDATE wallet " +
                            "SET balance = balance + ? " +
                            "WHERE user_id = ?";

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setDouble(1, amount);
            ps.setString(2, userId);
            int rows = ps.executeUpdate();
            System.out.println("rows updated: " + rows);
            // Không tìm thấy user
            if (rows == 0) {
                return null;
            }

            // Trả user mới sau khi update
            return findUserByUserId(userId);

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }
    public User withdraw(String userId, double amount){
        try{
            Connection conn = DBConnection.getConnection();

            String sql =
                    "UPDATE wallet " +
                            "SET balance = balance - ? " +
                            "WHERE user_id = ?";

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setDouble(1, amount);
            ps.setString(2, userId);

            int rows = ps.executeUpdate();

            // Không tìm thấy user
            if (rows == 0) {
                return null;
            }

            // Trả user mới sau khi update
            return findUserByUserId(userId);

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }
    public void approveTransaction(long transactionId){

        try{

            Connection conn =
                    DBConnection.getConnection();
            System.out.println("URL = " + conn.getMetaData().getURL());
            System.out.println("autoCommit = " + conn.getAutoCommit());

            String sql =
                    "SELECT * FROM transactions " +
                            "WHERE id = ?";

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setLong(1, transactionId);

            ResultSet rs =
                    ps.executeQuery();
            System.out.println(
                    "TRANSACTION ID = " + transactionId
            );

            boolean found = rs.next();

            System.out.println(
                    "FOUND = " + found
            );


            if(found){

                String userId =
                        rs.getString("user_id");

                double amount =
                        rs.getDouble("amount");

                String type =
                        rs.getString("type");

                // ===== CỘNG/TRỪ TIỀN THẬT =====

                if("DEPOSIT".equals(type)) {

                    deposit(userId, amount);
                }
                else if("WITHDRAW".equals(type)){

                    withdraw(userId, amount);
                }

                // ===== UPDATE STATUS =====
                System.out.println("SẮP UPDATE STATUS");
                String updateSql =
                        "UPDATE transactions " +
                                "SET status = 'APPROVED' " +
                                "WHERE id = ?";

                PreparedStatement updatePs =
                        conn.prepareStatement(updateSql);

                updatePs.setLong(1, transactionId);
                int rows =
                        updatePs.executeUpdate();
                conn.commit();

                System.out.println("ROWS UPDATED = " + rows);

            }

        } catch (Exception e){

            e.printStackTrace();
        }
    }
}
