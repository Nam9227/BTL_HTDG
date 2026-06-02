package com.uet.server.service;

import com.uet.server.database.dao.WalletDAO;
import com.uet.server.database.dao.AuctionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WalletService {
    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);
    
    private final WalletDAO walletDAO;
    private final AuctionDAO auctionDAO;

    public WalletService(WalletDAO walletDAO, AuctionDAO auctionDAO) {
        this.walletDAO = walletDAO;
        this.auctionDAO = auctionDAO;
    }

    


    public double getAvailableBalance(String userId, String auctionId) {
        return walletDAO.getAvailableBalanceForAuction(userId, auctionId);
    }

    



    public boolean checkAndFreezeBalance(String userId, String auctionId, double bidAmount) {
        double available = getAvailableBalance(userId, auctionId);
        if (bidAmount > available) {
            logger.warn("User {} không đủ số dư khả dụng (có: {}, cần: {})", userId, available, bidAmount);
            return false;
        }
        return true;
    }

    





    public boolean processAuctionEnd(String auctionId) {
        logger.info("Bắt đầu xử lý giao dịch kết thúc cho phiên {}", auctionId);
        return auctionDAO.forceEndAuctionAndProcessTransaction(auctionId);
    }
}
