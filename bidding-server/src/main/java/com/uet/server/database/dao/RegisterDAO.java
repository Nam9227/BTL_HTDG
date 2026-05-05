package com.uet.server.database.dao;

import com.uet.common.network.RegisterRequest;
import com.uet.server.database.DBConnection;
import com.uet.server.util.IdGenerator;

import java.util.Random;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.UUID;

public class RegisterDAO {

    private String generateUniqueId(Connection conn) throws Exception {
        String sql = "SELECT id FROM users WHERE id = ?";

        while (true) {
            String id = IdGenerator.generateId();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, id);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    return id; // chưa tồn tại → dùng được
                }
            }
        }
    }

    public String register(RegisterRequest request) {
        String checkUserSql = "SELECT id FROM users WHERE username = ?";
        String checkEmailSql = "SELECT user_id FROM user_profiles WHERE email = ?";
        String insertUserSql = "INSERT INTO users (id, username, password, role) VALUES (?, ?, ?, ?)";
        String insertProfileSql = "INSERT INTO user_profiles (user_id, full_name, email, phone_number, avatar_url) VALUES (?, ?, ?, ?, ?)";
        String sqlWallet = "INSERT INTO wallet(user_id, balance) VALUES (?, 0)";

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // check username
            try (PreparedStatement checkUserStmt = conn.prepareStatement(checkUserSql)) {
                checkUserStmt.setString(1, request.getUsername());
                ResultSet rs = checkUserStmt.executeQuery();
                if (rs.next()) {
                    return "USERNAME_EXISTS";
                }
            }

            // check email
            if (request.getEmail() != null && !request.getEmail().isBlank()) {
                try (PreparedStatement checkEmailStmt = conn.prepareStatement(checkEmailSql)) {
                    checkEmailStmt.setString(1, request.getEmail());
                    ResultSet rs = checkEmailStmt.executeQuery();
                    if (rs.next()) {
                        return "EMAIL_EXISTS";
                    }
                }
            }

            String userId = generateUniqueId(conn);

            // insert users
            try (PreparedStatement insertUserStmt = conn.prepareStatement(insertUserSql)) {
                insertUserStmt.setString(1, userId);
                insertUserStmt.setString(2, request.getUsername());
                insertUserStmt.setString(3, request.getPassword());
                insertUserStmt.setNull(4, Types.VARCHAR); // role = null
                insertUserStmt.executeUpdate();
            }

            // insert user_profiles
            try (PreparedStatement insertProfileStmt = conn.prepareStatement(insertProfileSql)) {
                insertProfileStmt.setString(1, userId);
                insertProfileStmt.setString(2, request.getFullName());
                insertProfileStmt.setString(3, emptyToNull(request.getEmail()));
                insertProfileStmt.setNull(4, Types.VARCHAR); // phone = null
                insertProfileStmt.setNull(5, Types.VARCHAR); // avatar = null
                insertProfileStmt.executeUpdate();
            }

            try (PreparedStatement psWallet = conn.prepareStatement(sqlWallet)) {
                psWallet.setString(1, userId);
                psWallet.executeUpdate();
            }

            conn.commit();
            return "REGISTER_SUCCESS";

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (Exception ignored) {
            }
            return "REGISTER_FAIL";

        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (Exception ignored) {
            }
        }
    }

    private String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}