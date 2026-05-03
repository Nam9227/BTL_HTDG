package com.uet.client.model.user;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String username;
    private String fullName; // Thêm cho khớp TableColumn
    private String email;    // Thêm cho khớp TableColumn
    private String phone;    // Thêm cho khớp TableColumn
    private String role;
    private String status;   // Thêm cho khớp TableColumn

    // Constructor mặc định
    public User() {}

    // Constructor đầy đủ để khởi tạo dữ liệu nhanh
    public User(String id, String username, String fullName, String email, String phone, String role, String status) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.status = status;
    }

    // --- BẮT BUỘC PHẢI CÓ CÁC HÀM GETTER ĐỂ TABLEVIEW HIỂN THỊ ---
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public String getStatus() { return status; }

    // --- CÁC HÀM SETTER ---
    public void setId(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setRole(String role) { this.role = role; }
    public void setStatus(String status) { this.status = status; }
}