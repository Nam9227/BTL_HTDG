package com.uet.server.database.dao;

import com.uet.common.model.user.Role;
import com.uet.common.model.user.User;
import com.uet.common.network.LoginRequest;
import com.uet.common.network.Response;
import com.uet.common.network.UpdateProfileRequest;
import com.uet.server.database.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

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
        String sql = """
            UPDATE user_profiles
            SET full_name = ?, email = ?, phone_number = ?, address = ?
            WHERE user_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, request.getFullName());
            ps.setString(2, request.getEmail());
            ps.setString(3, request.getPhoneNumber());
            ps.setString(4, request.getAddress());
            ps.setString(5, request.getUserId());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                return Response.success("Cập nhật thông tin tài khoản thành công!", null);
            }

            return Response.fail("Không tìm thấy hồ sơ người dùng để cập nhật!");

        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail("Lỗi server khi cập nhật thông tin tài khoản!");
        }
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
                user.setRole(role);
                user.setActive(rs.getBoolean("active"));
                user.setBalance(balance);

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
}