package com.uet.server.model.user;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
    private String status;
    private Double balance = 0.0; // Khởi tạo giá trị mặc định

    // 1. Constructor mặc định
    public User() {
    }

    // 2. Constructor đầy đủ 7 tham số (để hết lỗi "no suitable constructor found")
    public User(String id, String username, String fullName, String email, String phoneNumber, String role, String status) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.status = status;
    }

    // 3. Các hàm Getter và Setter (để hết lỗi "cannot find symbol method")
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
}