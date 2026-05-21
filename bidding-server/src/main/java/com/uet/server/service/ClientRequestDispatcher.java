package com.uet.server.service;

import com.uet.common.model.auction.AuctionItem;
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

    public boolean dispatch(Object obj, ClientHandler client) {
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

        client.send(Response.fail("Yêu cầu không hợp lệ"));
        return true;
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