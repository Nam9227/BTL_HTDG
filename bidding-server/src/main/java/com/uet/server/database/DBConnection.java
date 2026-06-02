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

    
    private static boolean isLogPrinted = false;

    public static Connection getConnection() throws SQLException {
        try {
            Properties properties = new Properties();

            
            java.io.File dbPropFile = new java.io.File("config/db.properties");
            if (!dbPropFile.exists()) {
                dbPropFile = new java.io.File("bidding-server/config/db.properties");
            }
            FileInputStream fis = new FileInputStream(dbPropFile);
            properties.load(fis);

            String url = properties.getProperty("db.url");
            String username = properties.getProperty("db.username");
            String password = properties.getProperty("db.password");

            
            Connection conn = DriverManager.getConnection(url, username, password);

            
            if (!isLogPrinted) {
                logger.info("Connected to database successfully!");
                isLogPrinted = true; 
            }

            return conn;

        } catch (IOException e) {
            logger.error("Không thể đọc file db.properties: ", e);
            throw new SQLException("Cannot read db.properties", e);
        }
    }
}