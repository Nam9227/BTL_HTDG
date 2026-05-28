package com.uet.server.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);

    // 🌟 BIẾN QUYẾT ĐỊNH: Cờ hiệu kiểm tra xem đã in log kết nối lần nào chưa
    private static boolean isLogPrinted = false;

    public static Connection getConnection() throws SQLException {
        try {
            Properties properties = new Properties();

            // Đọc cấu hình từ file db.properties
            FileInputStream fis = new FileInputStream("bidding-server/config/db.properties");
            properties.load(fis);

            String url = properties.getProperty("db.url");
            String username = properties.getProperty("db.username");
            String password = properties.getProperty("db.password");

            // Khởi tạo kết nối động tới MySQL để tránh nghẽn luồng
            Connection conn = DriverManager.getConnection(url, username, password);

            // 🌟 CHỈ HIỆN 1 LẦN ĐẦU: Nếu cờ hiệu chưa bật thì mới in log và bật cờ lên
            if (!isLogPrinted) {
                logger.info("DB URL = {}", url);
                logger.info("Connected to database successfully!");
                isLogPrinted = true; // Khóa cờ lại, các lần gọi sau sẽ bỏ qua khối lệnh này
            }

            return conn;

        } catch (IOException e) {
            logger.error("Không thể đọc file db.properties: ", e);
            throw new SQLException("Cannot read db.properties", e);
        }
    }
}