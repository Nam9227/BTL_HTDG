package com.uet.common.network;

import java.io.Serializable;

public class AutoBidResponse implements Serializable {
    private boolean success;
    private String message;

    public AutoBidResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
