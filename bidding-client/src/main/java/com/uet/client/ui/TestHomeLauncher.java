package com.uet.client.ui;

public class TestHomeLauncher {
    public static void main(String[] args) {
        // Chỉ chạy duy nhất luồng của HomeController để test giao diện
        HomeController.main(args);
    }
}
