package com.uet.server.service;

import com.uet.common.model.auction.AuctionItem;
import com.uet.common.model.auction.BidRecord; // 🌟 Thêm import để dùng danh sách lịch sử
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

        // Chặn người bán tự đấu giá sản phẩm của chính mình
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

        // Phát sóng danh sách cập nhật mới nhất cho tất cả Client ở trang chủ
        try {
            List<AuctionItem> activeAuctions = auctionDAO.getActiveAuctions();
            ClientManager.broadcast(new GetActiveAuctionsResponse(activeAuctions));
        } catch (Exception e) {
            logger.error("Lỗi khi phát sóng danh sách đấu giá mới sau khi bid: ", e);
        }
    }

    public void handleGetPendingAuctions(ClientHandler client) { // Giữ nguyên tên hàm ở Dispatcher đỡ phải sửa
        logger.info("==> Admin đang yêu cầu tải toàn bộ danh sách phiên đấu giá!");

        try {
            // 🌟 Lấy HẾT tất cả các phiên thay vì mỗi pending
            List<AuctionItem> allList = auctionDAO.getAllAuctionsForAdmin();
            client.send(Response.success("Tải danh sách chờ duyệt thành công", allList));
        } catch (Exception e) {
            logger.error("Lỗi hệ thống khi tải danh sách phiên đấu giá cho Admin: ", e);
            client.send(Response.fail("Lỗi hệ thống khi tải danh sách"));
        }
    }

    // 🌟 Viết thêm hàm ép kết thúc phiên đấu giá đang chạy
    public void handleForceEndAuction(String auctionId, ClientHandler client) {
        try {
            // Cập nhật trạng thái phiên thành FINISHED và thực hiện giao dịch chuyển tiền giữa người mua và người bán
            boolean success = auctionDAO.forceEndAuctionAndProcessTransaction(auctionId);
            if (success) {
                client.send(Response.success("Đã ép kết thúc phiên đấu giá thành công!", null));

                // 💡 Realtime: Phát thông báo cho những người đang ở trong phòng biết phiên đã bị đóng
                com.uet.server.network.ClientManager.broadcastAuction(
                        auctionId,
                        new com.uet.common.network.AuctionUpdateResponse(auctionDAO.getAuctionById(auctionId, false), "Phiên đấu giá đã bị Admin kết thúc.")
                );

                // Phát sóng danh sách cập nhật mới nhất cho tất cả Client ở trang chủ để xóa phiên đấu giá đã đóng
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

            boolean success = auctionDAO.updateAuctionStatus(request.getAuctionId(), newStatus);

            if (success) {
                client.send(Response.success(statusText + " phiên đấu giá thành công!", null));

                // Nếu được phê duyệt, thử kích hoạt phiên đấu giá ngay lập tức nếu đến giờ
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

            // 1. Lấy dữ liệu phiên từ Database lên để kiểm tra điều kiện xóa
            AuctionItem item = auctionDAO.getAuctionById(auctionId);

            if (item == null) {
                client.send( Response.fail("Sản phẩm hoặc phiên đấu giá không tồn tại!"));
                return;
            }

            // 🛡️ Kiểm tra quyền: Chủ sở hữu (Seller) HOẶC Người thắng (Winner) đều có quyền xóa
            boolean isSeller = item.getSellerId() != null && item.getSellerId().equals(userId);
            boolean isWinner = item.getWinnerId() != null && item.getWinnerId().equals(userId);

            if (!isSeller && !isWinner) {
                client.send(Response.fail("Bạn không có quyền xóa sản phẩm này!"));
                return;
            }

            // 🛡️ Kiểm tra trạng thái:
            // 1. Chặn không cho xóa nếu phiên đấu giá đang diễn ra (RUNNING)
            if ("RUNNING".equalsIgnoreCase(item.getStatus())) {
                client.send(Response.fail("Không thể xóa! Phiên đấu giá đang diễn ra (RUNNING)."));
                return;
            }

            // 2. Nếu là Seller tự xóa:
            if (isSeller && !isWinner) {
                // Chặn xóa nếu phiên đã kết thúc (FINISHED) và thực sự có người thắng
                if ("FINISHED".equalsIgnoreCase(item.getStatus())) {
                    if (item.getWinnerId() != null && !item.getWinnerId().trim().isEmpty()) {
                        client.send(Response.fail("Không thể xóa! Phiên đấu giá đã kết thúc giao dịch thành công."));
                        return;
                    }
                } else {
                    // Nếu phiên chưa kết thúc nhưng đã có người tham gia đấu giá (có leader hiện tại) thì cũng chặn
                    if (item.getWinnerId() != null && !item.getWinnerId().trim().isEmpty()) {
                        client.send(Response.fail("Không thể xóa! Phiên đấu giá đã có thành viên đặt giá."));
                        return;
                    }
                }
            }

            // 2. Tiến hành xóa dữ liệu trong Database sau khi vượt qua các chốt chặn an toàn
            boolean isDeleted = auctionDAO.deleteAuction(auctionId);

            if (isDeleted) {
                // Bắn phản hồi thành công về cho duy nhất Client vừa bấm nút Xóa
                client.send( Response.success("Đã gỡ bỏ sản phẩm và hủy phiên đấu giá thành công!", null));

                // Phát sóng danh sách cập nhật mới nhất cho tất cả Client ở trang chủ để cập nhật giao diện realtime
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