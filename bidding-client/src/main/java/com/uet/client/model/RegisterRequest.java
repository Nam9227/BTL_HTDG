package com.uet.client.model;

import java.io.Serializable;

public class RegisterRequest extends NetworkMessage implements Serializable {
    private String username;
    private String password;
    private String email; // Đăng ký thì thường cần thêm email

    public RegisterRequest(String username, String password, String email) {
        super("REGISTER"); // Gửi loại hành động là REGISTER lên Server
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // Đóng gói (Encapsulation): Getter
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
}