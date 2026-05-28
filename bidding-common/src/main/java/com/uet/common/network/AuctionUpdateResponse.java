package com.uet.common.network;

import com.uet.common.model.auction.AuctionItem;
import com.uet.common.model.auction.BidRecord;

import java.io.Serializable;
import java.util.List;

public class AuctionUpdateResponse implements Serializable {
    private AuctionItem auctionItem;
    private String message;
    private List<BidRecord> bidHistory;

    public AuctionUpdateResponse() {
    }

    public AuctionUpdateResponse(AuctionItem auctionItem, String message, List<BidRecord> bidHistory) {
        this.auctionItem = auctionItem;
        this.message = message;
        this.bidHistory = bidHistory;
    }
    public AuctionUpdateResponse(AuctionItem auctionItem, String message) {
        this.auctionItem = auctionItem;
        this.message = message;
        this.bidHistory = null;
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
    public List<BidRecord> getBidHistory() { return bidHistory; }
    public void setBidHistory(List<BidRecord> bidHistory) { this.bidHistory = bidHistory; }
}