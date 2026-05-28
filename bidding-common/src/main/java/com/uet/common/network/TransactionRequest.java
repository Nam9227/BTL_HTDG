package com.uet.common.network;

import java.io.Serializable;

public class TransactionRequest implements Serializable {

    private String userId;
    private double amount;
    private String type;
    public TransactionRequest(String userId, double amount, String type) {
        this.userId = userId;
        this.amount = amount;
        this.type = type;
    }
    public String getUserId(){
        return userId;
    }
    public double getAmount(){
        return amount;
    }
    public String getType(){
        return type;
    }
}
