package com.uet.server.service;

import com.uet.common.model.auction.AuctionItem;
import com.uet.common.model.auction.BidRecord; // 🌟 Thêm import để dùng danh sách lịch sử
import com.uet.common.network.AuctionUpdateResponse;
import com.uet.common.network.BidRequest;
import com.uet.common.network.Response;
import com.uet.server.database.dao.AuctionDAO;
import com.uet.server.database.dao.BidDAO;
import com.uet.server.network.ClientHandler;
import com.uet.server.network.ClientManager;

import java.util.List;

public class AuctionRealtimeService {

    private final AuctionDAO auctionDAO = new AuctionDAO();
    private final BidDAO bidDAO = new BidDAO();

    public void joinAuction(String auctionId, ClientHandler client) {
        ClientManager.joinAuction(auctionId, client);
        AuctionItem auctionItem = auctionDAO.getAuctionById(auctionId);

        List<BidRecord> bidHistory = auctionDAO.getBidHistory(auctionId);

        client.send(new AuctionUpdateResponse(auctionItem, "Đã tham gia xem phiên đấu giá", bidHistory));
    }

    public void leaveAuction(String auctionId, ClientHandler client) {
        ClientManager.leaveAuction(auctionId, client);
        client.send(Response.success("Đã rời phiên đấu giá", null));
    }

    public void placeBid(BidRequest request, ClientHandler client) {
        System.out.println("Nhận đặt giá mới: auctionId="
                + request.getAuctionId()
                + ", bidderId="
                + request.getBidderId()
                + ", amount="
                + request.getAmount());

        Response response = bidDAO.handleBid(request);
        client.send(response);

        if (!response.isSuccess()) {
            return;
        }
        AuctionItem updatedAuction = auctionDAO.getAuctionById(request.getAuctionId());

        List<BidRecord> updatedHistory = auctionDAO.getBidHistory(request.getAuctionId());

        ClientManager.broadcastAuction(
                request.getAuctionId(),
                new AuctionUpdateResponse(updatedAuction, "Có giá mới từ người dùng!", updatedHistory)
        );
    }
}