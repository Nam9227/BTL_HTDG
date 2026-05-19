package com.uet.common.network;

import com.uet.common.model.auction.AuctionItem;

import java.io.Serializable;

public class AuctionUpdateResponse implements Serializable {
    private AuctionItem auctionItem;
    private String message;

    public AuctionUpdateResponse() {
    }

    public AuctionUpdateResponse(AuctionItem auctionItem, String message) {
        this.auctionItem = auctionItem;
        this.message = message;
    }

    public AuctionItem getAuctionItem() {
        return auctionItem;
    }

    public void setAuctionItem(AuctionItem auctionItem) {
        this.auctionItem = auctionItem;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}