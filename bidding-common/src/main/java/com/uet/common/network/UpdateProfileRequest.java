package com.uet.common.network;

import java.io.Serializable;

public class UpdateProfileRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private ImageData avatar;

    public UpdateProfileRequest(String userId, String fullName, String email, String phoneNumber, String address, ImageData avatar) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.avatar = avatar;
    }

    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public ImageData getAvatar() {
        return avatar;
    }
}