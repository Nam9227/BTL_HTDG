package com.uet.client.model;

import java.io.Serializable;

public class User implements Serializable {
    private String username;
    private String fullName;
    private double balance; // Số dư ví để đấu giá
    private String avatarPath; // Đường dẫn ảnh đại diện

    public User(String username, String fullName, double balance) {
        this.username = username;
        this.fullName = fullName;
        this.balance = balance;
        this.avatarPath = "/photo/icon_app.png"; // Ảnh mặc định Nam đã có trong resources
    }

    // Getter và Setter để cập nhật số dư khi đấu giá thắng
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public double getBalance() { return balance; }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}