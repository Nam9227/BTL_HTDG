package com.uet.server.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {

                Properties properties = new Properties();
                FileInputStream fis = new FileInputStream("bidding-server/config/db.properties");
                properties.load(fis);

                String url = properties.getProperty("db.url");
                String username = properties.getProperty("db.username");
                String password = properties.getProperty("db.password");

                connection = DriverManager.getConnection(url, username, password);
                System.out.println("Connected to database!");
            }
            return connection;
        } catch (IOException e) {
            throw new SQLException("Cannot read db.properties", e);
        }
    }
}