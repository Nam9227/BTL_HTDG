package com.uet.client.config;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = AppConfig.class
                .getResourceAsStream("/config/client.properties")) {

            if (input == null) {
                throw new RuntimeException("Không tìm thấy file config");
            }

            properties.load(input);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc config", e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static int getInt(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }
}