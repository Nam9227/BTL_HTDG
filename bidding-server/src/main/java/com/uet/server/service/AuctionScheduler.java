package com.uet.server.service;

import com.uet.server.database.dao.AuctionDAO;
import com.uet.server.network.ClientManager;
import com.uet.common.network.GetActiveAuctionsResponse;
import com.uet.common.model.auction.AuctionItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionScheduler {
    private static final Logger logger = LoggerFactory.getLogger(AuctionScheduler.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AuctionDAO auctionDAO = new AuctionDAO();

    public void start() {
        logger.info("[Scheduler] Bộ quét thời gian đấu giá tự động đã được kích hoạt!");

        // Thiết lập: Cứ mỗi 5 giây (TimeUnit.SECONDS), bộ quét sẽ tự động chạy lại một lần
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // 1. Kiểm tra kích hoạt phiên mới
                int activated = auctionDAO.startEligibleAuctions();

                // 2. Kiểm tra đóng phiên hết hạn
                int finished = auctionDAO.finishExpiredAuctions();

                // Nếu có bất cứ thay đổi nào, phát sóng danh sách mới đến trang chủ của tất cả Client
                if (activated > 0 || finished > 0) {
                    logger.info("[Scheduler] Phát hiện có thay đổi danh sách phiên đấu giá (Kích hoạt: {}, Kết thúc: {}). Tiến hành Broadcast...", activated, finished);
                    List<AuctionItem> activeAuctions = auctionDAO.getActiveAuctions();
                    ClientManager.broadcast(new GetActiveAuctionsResponse(activeAuctions));
                }

            } catch (Exception e) {
                logger.error("Lỗi trong quá trình quét Scheduler: ", e);
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
        logger.info("[Scheduler] Đã dừng bộ quét tự động.");
    }
}