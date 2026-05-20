package com.uet.client.ui.admin;

import javafx.beans.property.*;

public class AuctionRow {

    private final IntegerProperty id;
    private final StringProperty product;
    private final StringProperty currentPrice;
    private final StringProperty leader;
    private final StringProperty endTime;
    private final StringProperty status;

    public AuctionRow(int id, String product, String currentPrice,
                      String leader, String endTime, String status) {
        this.id = new SimpleIntegerProperty(id);
        this.product = new SimpleStringProperty(product);
        this.currentPrice = new SimpleStringProperty(currentPrice);
        this.leader = new SimpleStringProperty(leader);
        this.endTime = new SimpleStringProperty(endTime);
        this.status = new SimpleStringProperty(status);
    }

    public IntegerProperty idProperty() { return id; }
    public StringProperty productProperty() { return product; }
    public StringProperty currentPriceProperty() { return currentPrice; }
    public StringProperty leaderProperty() { return leader; }
    public StringProperty endTimeProperty() { return endTime; }
    public StringProperty statusProperty() { return status; }


    public int getId() { return id.get(); }
    public String getProduct() { return product.get(); }
    public String getCurrentPrice() { return currentPrice.get(); }
    public String getLeader() { return leader.get(); }
    public String getEndTime() { return endTime.get(); }


    public String getStatus() { return status.get(); }


    public void setId(int value) { id.set(value); }
    public void setProduct(String value) { product.set(value); }
    public void setCurrentPrice(String value) { currentPrice.set(value); }
    public void setLeader(String value) { leader.set(value); }
    public void setEndTime(String value) { endTime.set(value); }
    public void setStatus(String value) { status.set(value); }
}