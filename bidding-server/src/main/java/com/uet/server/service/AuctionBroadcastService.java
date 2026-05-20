package com.uet.server.service;

import com.uet.common.model.auction.AuctionItem;
import com.uet.common.network.AuctionUpdateResponse;
import com.uet.common.network.GetActiveAuctionsResponse;
import com.uet.server.database.dao.AuctionDAO;
import com.uet.server.network.ClientManager;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionBroadcastService {

    private static final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private static final AuctionDAO auctionDAO = new AuctionDAO();

    public static void start() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                for (String auctionId : ClientManager.getActiveAuctionIds()) {
                    AuctionItem auctionItem = auctionDAO.getAuctionById(auctionId);

                    if (auctionItem != null) {
                        ClientManager.broadcastAuction(
                                auctionId,
                                new AuctionUpdateResponse(auctionItem, "Cập nhật giá định kỳ")
                        );
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 1500, TimeUnit.MILLISECONDS);
    }

    public static void stop() {
        scheduler.shutdownNow();
    }
}