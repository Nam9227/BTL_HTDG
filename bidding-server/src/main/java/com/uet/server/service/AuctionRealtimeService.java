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

    public void handleGetPendingAuctions(ClientHandler client) { // Giữ nguyên tên hàm ở Dispatcher đỡ phải sửa
        System.out.println("==> Admin đang yêu cầu tải toàn bộ danh sách phiên đấu giá!");

        try {
            // 🌟 Lấy HẾT tất cả các phiên thay vì mỗi pending
            List<AuctionItem> allList = auctionDAO.getAllAuctionsForAdmin();
            client.send(Response.success("Tải danh sách chờ duyệt thành công", allList));
        } catch (Exception e) {
            e.printStackTrace();
            client.send(Response.fail("Lỗi hệ thống khi tải danh sách"));
        }
    }

    // 🌟 Viết thêm hàm ép kết thúc phiên đấu giá đang chạy
    public void handleForceEndAuction(String auctionId, ClientHandler client) {
        try {
            // Cập nhật trạng thái phiên thành FINISHED và chốt người thắng hiện tại
            boolean success = auctionDAO.updateAuctionStatus(auctionId, "FINISHED");
            if (success) {
                client.send(Response.success("Đã ép kết thúc phiên đấu giá thành công!", null));

                // 💡 Realtime: Phát thông báo cho những người đang ở trong phòng biết phiên đã bị đóng
                com.uet.server.network.ClientManager.broadcastAuction(
                        auctionId,
                        new com.uet.common.network.AuctionUpdateResponse(auctionDAO.getAuctionById(auctionId), "Phiên đấu giá đã bị Admin kết thúc.")
                );
            } else {
                client.send(Response.fail("Không thể kết thúc phiên đấu giá."));
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
            client.send(Response.fail("Lỗi hệ thống khi xử lý phê duyệt."));
        }
    }

}