package com.uet.common.network;

import java.io.Serializable;

public class GetActiveAuctionsRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private String type; 

    public GetActiveAuctionsRequest() {
        this.type = "ACTIVE";
    }

    public GetActiveAuctionsRequest(String userId, String type) {
        this.userId = userId;
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }
}