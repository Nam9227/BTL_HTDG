package com.uet.common.model.user;

import java.io.Serializable;
import java.math.BigDecimal;

public class User implements Serializable {
    private String id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private String address;
    private BigDecimal balance;
    private String avatarPath;
    private boolean active;
    private byte[] avatarBytes;

    public User() {
    }

    public User(String id, String username, String fullName, String email, String phone, Role role, BigDecimal balance,boolean active) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.balance = balance;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public boolean getActive() {
        return active;
    }

    public String getPhone() {
        return phone;
    }

    public Role getRole() {
        return role;
    }


    public BigDecimal getBalance() {
        return balance;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public byte[] getAvatarBytes() { return avatarBytes; }

    public void setAvatarBytes(byte[] avatarBytes) { this.avatarBytes = avatarBytes; }
}