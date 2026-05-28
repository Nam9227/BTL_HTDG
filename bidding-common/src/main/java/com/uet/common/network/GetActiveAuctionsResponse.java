package com.uet.common.network;

import com.uet.common.model.auction.AuctionItem;

import java.io.Serializable;
import java.util.List;

public class GetActiveAuctionsResponse implements Serializable {

    private final List<AuctionItem> auctions;

    public GetActiveAuctionsResponse(List<AuctionItem> auctions) {
        this.auctions = auctions;
    }

    public List<AuctionItem> getAuctions() {
        return auctions;
    }
}