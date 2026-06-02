package com.uet.common.model.transaction;

import java.io.Serializable;
public class Transaction implements Serializable {
    private long id;

    private String userId;

    private double amount;

    private String type;

    private String createdAt;

    private String status;

    public Transaction(long id,
                       String userId,
                       double amount,
                       String type,
                       String createdAt,
                       String status) {

        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.createdAt = createdAt;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreatedAt(){
        return createdAt;
    }

    public void setCreatedAt(String createdAt){this.createdAt = createdAt;}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
