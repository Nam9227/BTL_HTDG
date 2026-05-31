package com.uet.server.service;

import com.uet.common.model.auction.AuctionItem;
import com.uet.common.model.auction.BidRecord; 
import com.uet.common.network.*;
import com.uet.server.database.dao.AuctionDAO;
import com.uet.server.database.dao.BidDAO;
import com.uet.server.network.ClientHandler;
import com.uet.server.network.ClientManager;
import com.uet.server.database.dao.WalletDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AuctionRealtimeService {
    private static final Logger logger = LoggerFactory.getLogger(AuctionRealtimeService.class);

    private final AuctionDAO auctionDAO = new AuctionDAO();
    private final BidDAO bidDAO = new BidDAO();
    private final WalletDAO walletDAO = new WalletDAO();

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
        String bidderId = request.getBidderId();
        double bidAmount = request.getAmount();

        logger.info("Nhận đặt giá mới: auctionId={}, bidderId={}, amount={}",
                request.getAuctionId(),
                bidderId,
                bidAmount);

        
        AuctionItem auctionItem = auctionDAO.getAuctionById(request.getAuctionId(), false);
        if (auctionItem != null && bidderId.equals(auctionItem.getSellerId())) {
            logger.warn("[Chặn Bid] Người dùng {} cố tình đấu giá sản phẩm của chính mình!", bidderId);
            client.send(Response.fail("Bạn không thể đấu giá sản phẩm của chính mình!"));
            return;
        }

        double availableBalance = walletDAO.getAvailableBalanceForAuction(bidderId, request.getAuctionId());

        if (bidAmount > availableBalance) {
            logger.warn("-> [Chặn Bid] Người dùng {} không đủ số dư khả dụng! (Có: {})", bidderId, availableBalance);
            client.send(Response.fail("Số dư khả dụng không đủ! Bạn đang có khoản tiền bị đóng băng do dẫn đầu ở phiên đấu giá khác."));
            return;
        }

        Response response = bidDAO.handleBid(request);
        client.send(response);

        if (!response.isSuccess()) {
            return;
        }

        AuctionItem updatedAuction = auctionDAO.getAuctionById(request.getAuctionId(), false);
        List<BidRecord> updatedHistory = auctionDAO.getBidHistory(request.getAuctionId());

        ClientManager.broadcastAuction(
                request.getAuctionId(),
                new AuctionUpdateResponse(updatedAuction, "Có giá mới từ người dùng!", updatedHistory)
        );

        
        try {
            List<AuctionItem> activeAuctions = auctionDAO.getActiveAuctions();
            ClientManager.broadcast(new GetActiveAuctionsResponse(activeAuctions));
        } catch (Exception e) {
            logger.error("Lỗi khi phát sóng danh sách đấu giá mới sau khi bid: ", e);
        }
    }

    public void handleGetPendingAuctions(ClientHandler client) { 
        logger.info("==> Admin đang yêu cầu tải toàn bộ danh sách phiên đấu giá!");

        try {
            
            List<AuctionItem> allList = auctionDAO.getAllAuctionsForAdmin();
            client.send(Response.success("Tải danh sách chờ duyệt thành công", allList));
        } catch (Exception e) {
            logger.error("Lỗi hệ thống khi tải danh sách phiên đấu giá cho Admin: ", e);
            client.send(Response.fail("Lỗi hệ thống khi tải danh sách"));
        }
    }

    
    public void handleForceEndAuction(String auctionId, ClientHandler client) {
        try {
            
            boolean success = auctionDAO.forceEndAuctionAndProcessTransaction(auctionId);
            if (success) {
                client.send(Response.success("Đã ép kết thúc phiên đấu giá thành công!", null));
                com.uet.server.database.dao.AdminDAO.logAdminAction("Ép kết thúc đấu giá", auctionId, "Thành công");

                
                com.uet.server.network.ClientManager.broadcastAuction(
                        auctionId,
                        new com.uet.common.network.AuctionUpdateResponse(auctionDAO.getAuctionById(auctionId, false), "Phiên đấu giá đã bị Admin kết thúc.")
                );

                
                try {
                    List<AuctionItem> activeAuctions = auctionDAO.getActiveAuctions();
                    ClientManager.broadcast(new GetActiveAuctionsResponse(activeAuctions));
                } catch (Exception e) {
                    logger.error("Lỗi khi phát sóng danh sách đấu giá mới sau khi ép kết thúc: ", e);
                }
            } else {
                client.send(Response.fail("Không thể kết thúc phiên đấu giá."));
            }
        } catch (Exception e) {
            logger.error("Lỗi khi ép kết thúc phiên đấu giá ID: " + auctionId, e);
        }
    }

    public void handleApproveAuction(ApproveAuctionRequest request, ClientHandler client) {
        try {
            String newStatus = request.isApproved() ? "ACTIVE" : "REJECTED";
            String statusText = request.isApproved() ? "Phê duyệt" : "Từ chối";
            
            
            AuctionItem item = auctionDAO.getAuctionById(request.getAuctionId(), false);

            boolean success = auctionDAO.updateAuctionStatus(request.getAuctionId(), newStatus);

            if (success) {
                client.send(Response.success(statusText + " phiên đấu giá thành công!", null));
                com.uet.server.database.dao.AdminDAO.logAdminAction(statusText + " đấu giá", request.getAuctionId(), "Thành công");
                
                if (item != null) {
                    com.uet.server.database.dao.UserDAO userDAO = new com.uet.server.database.dao.UserDAO();
                    
                    userDAO.deleteNotificationByKeyword(item.getSellerId(), "Sản phẩm '" + item.getProductName() + "' đang chờ Admin duyệt");
                    
                    
                    String title = request.isApproved() ? "Sản phẩm đã duyệt" : "Sản phẩm bị từ chối";
                    String content = request.isApproved() 
                            ? "Sản phẩm '" + item.getProductName() + "' đã được phê duyệt." 
                            : "Sản phẩm '" + item.getProductName() + "' đã bị từ chối.";
                    userDAO.createNotification(item.getSellerId(), title, content);
                }

                
                if (request.isApproved()) {
                    try {
                        int started = auctionDAO.startEligibleAuctions();
                        if (started > 0) {
                            logger.info("[Approve] Đã kích hoạt trực tiếp {} phiên đấu giá sang RUNNING. Tiến hành phát sóng...", started);
                            List<AuctionItem> activeAuctions = auctionDAO.getActiveAuctions();
                            ClientManager.broadcast(new GetActiveAuctionsResponse(activeAuctions));
                        }
                    } catch (Exception e) {
                        logger.error("Lỗi khi tự động chạy phiên sau khi phê duyệt: ", e);
                    }
                }
            } else {
                client.send(Response.fail("Không thể cập nhật trạng thái phiên đấu giá."));
            }
        } catch (Exception e) {
            logger.error("Lỗi hệ thống khi xử lý phê duyệt đấu giá ID: " + request.getAuctionId(), e);
            client.send(Response.fail("Lỗi hệ thống khi xử lý phê duyệt."));
        }
    }

    public void handleDeleteAuction(DeleteProductRequest deleteReq, ClientHandler client) {
        try {
            String auctionId = deleteReq.getAuctionId();
            String userId = deleteReq.getUserId();

            
            AuctionItem item = auctionDAO.getAuctionById(auctionId);

            if (item == null) {
                client.send( Response.fail("Sản phẩm hoặc phiên đấu giá không tồn tại!"));
                return;
            }

            
            boolean isSeller = item.getSellerId() != null && item.getSellerId().equals(userId);
            boolean isWinner = item.getWinnerId() != null && item.getWinnerId().equals(userId);

            if (!isSeller && !isWinner) {
                client.send(Response.fail("Bạn không có quyền xóa sản phẩm này!"));
                return;
            }

            
            
            if ("RUNNING".equalsIgnoreCase(item.getStatus())) {
                client.send(Response.fail("Không thể xóa! Phiên đấu giá đang diễn ra (RUNNING)."));
                return;
            }

            
            if (isSeller && !isWinner) {
                
                if ("FINISHED".equalsIgnoreCase(item.getStatus())) {
                    if (item.getWinnerId() != null && !item.getWinnerId().trim().isEmpty()) {
                        client.send(Response.fail("Không thể xóa! Phiên đấu giá đã kết thúc giao dịch thành công."));
                        return;
                    }
                } else {
                    
                    if (item.getWinnerId() != null && !item.getWinnerId().trim().isEmpty()) {
                        client.send(Response.fail("Không thể xóa! Phiên đấu giá đã có thành viên đặt giá."));
                        return;
                    }
                }
            }

            
            boolean isDeleted = auctionDAO.deleteAuction(auctionId);

            if (isDeleted) {
                
                client.send( Response.success("Đã gỡ bỏ sản phẩm và hủy phiên đấu giá thành công!", null));

                
                try {
                    List<AuctionItem> activeAuctions = auctionDAO.getActiveAuctions();
                    ClientManager.broadcast(new GetActiveAuctionsResponse(activeAuctions));
                } catch (Exception e) {
                    logger.error("Lỗi khi phát sóng danh sách đấu giá mới sau khi xóa phiên: ", e);
                }
            } else {
                client.send( Response.fail("Lỗi hệ thống cơ sở dữ liệu, không thể xóa lúc này!"));
            }

        } catch (Exception e) {
            logger.error("Lỗi xảy ra khi xử lý xóa phiên đấu giá: ", e);
            try {
                client.send( Response.fail("Hệ thống gặp sự cố ngoài ý muốn khi xử lý lệnh xóa!"));
            } catch (Exception ignored) {}
        }
    }
}