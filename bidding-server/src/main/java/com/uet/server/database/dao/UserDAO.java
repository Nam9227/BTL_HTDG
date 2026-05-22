package com.uet.server.database.dao;

import com.uet.common.model.user.Role;
import com.uet.common.model.user.User;
import com.uet.common.network.ImageData;
import com.uet.common.network.LoginRequest;
import com.uet.common.network.Response;
import com.uet.common.network.UpdateProfileRequest;
import com.uet.server.database.DBConnection;
import com.uet.server.service.FileStorageService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

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

        return Response.success("Đăng nhập thành công", user);
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
            e.printStackTrace();
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
            e.printStackTrace();
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
                System.out.println("🔄 Server đã lưu file mới tại path: " + avatarPath);

                // 4. TIẾN HÀNH XÓA FILE CŨ TRÊN ĐĨA CỦA VPS ĐỂ TRÁNH RÁC BỘ NHỚ
                if (oldAvatarPath != null && !oldAvatarPath.isBlank()) {
                    // Xử lý chuẩn hóa chuỗi đường dẫn nếu đường dẫn chứa tiền tố file://
                    String cleanOldPath = oldAvatarPath.replace("file://", "").replace("file:///", "/");
                    java.io.File oldFile = new java.io.File(cleanOldPath);

                    if (oldFile.exists()) {
                        boolean deleted = oldFile.delete();
                        if (deleted) {
                            System.out.println("🗑️ [Tối ưu đĩa] Đã xóa thành công file avatar cũ trên VPS: " + cleanOldPath);
                        } else {
                            System.out.println("⚠️ Không thể xóa file cũ (có thể đang bị luồng khác chiếm dụng): " + cleanOldPath);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("❌ Lỗi khi xử lý lưu file mới hoặc xóa file cũ: " + e.getMessage());
                e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
                                System.out.println("UserDAO: file not found: " + filePath);
                            }
                        } else if (avatarPath.startsWith("http://") || avatarPath.startsWith("https://")) {
                            try (java.io.InputStream in = new java.net.URL(avatarPath).openStream()) {
                                bytes = in.readAllBytes();
                            } catch (Exception e) {
                                System.out.println("UserDAO: cannot download avatar from url: " + avatarPath);
                                e.printStackTrace();
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
                            System.out.println("UserDAO: loaded avatar bytes len=" + bytes.length + " for user " + user.getId());
                        }
                    } catch (Exception e) {
                        System.out.println("UserDAO: error loading avatar: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                return user;
            }

        } catch (Exception e) {
            e.printStackTrace();
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
        String sql = """
            SELECT 
                u.id, u.username, u.role, u.active,
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
                        e.printStackTrace();
                    }
                }
                return user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}