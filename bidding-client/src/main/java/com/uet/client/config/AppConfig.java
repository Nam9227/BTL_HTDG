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
        String val = properties.getProperty(key);
        return val != null ? val.trim() : null;
    }

    public static int getInt(String key) {
        String val = properties.getProperty(key);
        if (val != null) {
            val = val.trim();
        }
        return Integer.parseInt(val);
    }
}