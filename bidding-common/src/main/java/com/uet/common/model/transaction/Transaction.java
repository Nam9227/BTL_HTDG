package com.uet.common.model.transaction;

import java.io.Serializable;
public class Transaction implements Serializable {
    private long id;

    private String userId;

    private double amount;

    private String type;

    private String date;

    private String status;

    public Transaction(long id,
                       String userId,
                       double amount,
                       String type,
                       String date,
                       String status) {

        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.date = date;
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

    public String getDate(){
        return date;
    }

    public void setDate(String date){this.date = date;}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
