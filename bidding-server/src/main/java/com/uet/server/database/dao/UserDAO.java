package com.uet.server.database.dao;

import com.uet.server.database.DBConnection;
import com.uet.common.model.user.User;
import com.uet.common.model.user.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    public void updateRole(String userId, Role role) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role.name());
            ps.setString(2, userId);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public User login(String username, String password) {
        String sql = """
            SELECT u.id, u.username, u.role, COALESCE(w.balance, 0) AS balance
            FROM users u
            LEFT JOIN wallet w ON u.id = w.user_id
            WHERE u.username = ? AND u.password = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("username");

                String roleStr = rs.getString("role");
                Role role = null;

                if (roleStr != null) {
                    role = Role.valueOf(roleStr.toUpperCase());
                }

                double balance = rs.getDouble("balance");

                return new User(id, name, role, balance);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}