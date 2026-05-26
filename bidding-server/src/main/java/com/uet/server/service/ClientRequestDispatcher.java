package com.uet.server.service;

import com.uet.common.model.auction.AuctionItem;
import com.uet.common.model.auction.BidRecord;
import com.uet.common.model.user.User;
import com.uet.common.network.*;
import com.uet.server.database.dao.AuctionDAO;
import com.uet.server.database.dao.RegisterDAO;
import com.uet.server.database.dao.UserDAO;
import com.uet.server.network.ClientHandler;

import java.util.List;

public class ClientRequestDispatcher {

    private final UserDAO userDAO = new UserDAO();
    private final RegisterDAO registerDAO = new RegisterDAO();
    private final AuctionDAO auctionDAO = new AuctionDAO();
    private final AuctionRealtimeService auctionRealtimeService = new AuctionRealtimeService();
    private final AuctionService auctionService = new AuctionService(this.auctionDAO);

    public boolean dispatch(Object obj, ClientHandler client) {
        // 1. Xử lý yêu cầu lấy toàn bộ danh sách người dùng cho Admin
        if (obj instanceof GetAllUsersRequest) {
            handleGetAllUsers(client);
            return true;
        }

        // 2. Xử lý yêu cầu Khóa/Mở khóa tài khoản từ Admin
        // Trong hàm dispatch() của ClientRequestDispatcher:

        // 2. Xử lý yêu cầu Khóa/Mở khóa tài khoản từ Admin
        // Khúc xử lý UpdateUserStatusRequest phía Server
        if (obj instanceof UpdateUserStatusRequest request) {
            try {
                // 1. Cập nhật vào DB
                userDAO.updateActive(request.getUserId(), request.isActive());
                System.out.println("🎯 [Server] Đã đổi trạng thái user " + request.getUserId());

                // 2. ⚡ SIÊU TỐC: Tự quét DB và trả thẳng danh sách mới về cho Admin luôn
                handleGetAllUsers(client);

            } catch (Exception e) {
                e.printStackTrace();
                client.send(Response.fail("Lỗi Server: Không thể cập nhật trạng thái!"));
            }
            return true;
        }

// Khúc xử lý DeleteUserRequest phía Server
        if (obj instanceof DeleteUserRequest request) {
            try {
                // 1. Xóa trong DB
                userDAO.deleteUser(request.getUserId());
                System.out.println("🗑️ [Server] Đã xóa thành công user: " + request.getUserId());

                // 2. ⚡ SIÊU TỐC: Trả ngay danh sách mới về cho Admin
                handleGetAllUsers(client);

            } catch (Exception e) {
                e.printStackTrace();
                client.send(Response.fail("Lỗi Server: Không thể xóa tài khoản!"));
            }
            return true;
        }

        // 4. Xử lý Đăng nhập
        if (obj instanceof LoginRequest request) {
            handleLogin(request, client);
            return true;
        }

        if (obj instanceof RegisterRequest request) {
            handleRegister(request, client);
            return true;
        }

        if (obj instanceof UpdateRoleRequest request) {
            handleUpdateRole(request, client);
            return true;
        }

        if (obj instanceof GetActiveAuctionsRequest) {
            handleGetActiveAuctions(client);
            return true;
        }

        if (obj instanceof JoinAuctionRequest request) {
            auctionRealtimeService.joinAuction(request.getAuctionId(), client);
            return true;
        }

        if (obj instanceof LeaveAuctionRequest request) {
            auctionRealtimeService.leaveAuction(request.getAuctionId(), client);
            return true;
        }

        if (obj instanceof BidRequest request) {
            auctionRealtimeService.placeBid(request, client);
            return true;
        }

        if (obj instanceof UpdateProfileRequest request) {
            handleUpdateProfile(request, client);
            return true;
        }

        if ("LOGOUT".equals(obj)) {
            client.send(Response.success("Đăng xuất thành công", null));
            return false;
        }

        if (obj instanceof AddProductRequest req) {
            auctionService.handleRegisterProduct(req, client);
            return true;
        }

        if (obj instanceof GetBidHistoryRequest req) {
            List<BidRecord> history = auctionDAO.getBidHistory(req.getAuctionId());
            client.send(Response.success("Tải lịch sử thành công", history));
            return true;
        }

        if (obj instanceof GetPendingAuctionsRequest) {
            auctionRealtimeService.handleGetPendingAuctions(client);
            return true;
        }

        if (obj instanceof ApproveAuctionRequest approveReq) {
            auctionRealtimeService.handleApproveAuction(approveReq, client);
            return true;
        }

        if (obj instanceof ForceEndRequest forceEndReq) {
            auctionRealtimeService.handleForceEndAuction(forceEndReq.getAuctionId(), client);
            return true;
        }

        client.send(Response.fail("Yêu cầu không hợp lệ"));
        return true;
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ PRIVATE (HELPER METHODS)
    // ==========================================

    private void handleGetAllUsers(ClientHandler client) {
        try {
            List<User> users = userDAO.getAllUsers();
            client.send(new GetAllUsersResponse(users));
        } catch (Exception e) {
            e.printStackTrace();
            client.send(Response.fail("Lỗi hệ thống khi tải danh sách người dùng."));
        }
    }

    private void handleLogin(LoginRequest request, ClientHandler client) {
        System.out.println("Login attempt: " + request.getUsername());
        client.send(userDAO.handleLogin(request));
    }

    private void handleRegister(RegisterRequest request, ClientHandler client) {
        client.send(registerDAO.handleRegister(request));
    }

    private void handleUpdateRole(UpdateRoleRequest request, ClientHandler client) {
        userDAO.updateRole(request.getUserId(), request.getRole());
        client.send(Response.success("Cập nhật quyền thành công", null));
    }

    private void handleGetActiveAuctions(ClientHandler client) {
        List<AuctionItem> auctions = auctionDAO.getActiveAuctions();
        client.send(new GetActiveAuctionsResponse(auctions));
    }

    private void handleUpdateProfile(UpdateProfileRequest request, ClientHandler client) {
        Response response = userDAO.updateProfile(request);
        client.send(response);
    }
}