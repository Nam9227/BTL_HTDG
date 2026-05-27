package com.uet.server.service;

import com.uet.common.model.auction.AuctionItem;
import com.uet.common.model.auction.BidRecord; // 🌟 Thêm import để dùng danh sách lịch sử
import com.uet.common.network.ApproveAuctionRequest;
import com.uet.common.network.AuctionUpdateResponse;
import com.uet.common.network.BidRequest;
import com.uet.common.network.Response;
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
            } else {
                client.send(Response.fail("Không thể cập nhật trạng thái phiên đấu giá."));
            }
        } catch (Exception e) {
            logger.error("Lỗi hệ thống khi xử lý phê duyệt đấu giá ID: " + request.getAuctionId(), e);
            client.send(Response.fail("Lỗi hệ thống khi xử lý phê duyệt."));
        }
    }
}