package com.uet.server.service;

import com.uet.server.database.dao.AuctionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                auctionDAO.startEligibleAuctions();

                // 2. Kiểm tra đóng phiên hết hạn
                auctionDAO.finishExpiredAuctions();

                // 💡 Mẹo Realtime nâng cao: Nếu Nam muốn màn hình Home của người dùng tự động nhảy
                // sản phẩm mới mà không cần F5, Nam có thể gọi thêm hàm Broadcast thông báo ở đây.

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