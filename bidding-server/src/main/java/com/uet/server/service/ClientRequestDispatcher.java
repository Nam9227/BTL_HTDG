package com.uet.server.service;

import com.uet.common.model.auction.AuctionItem;
import com.uet.common.model.auction.BidRecord;
import com.uet.common.model.transaction.Transaction;
import com.uet.common.model.user.User;
import com.uet.common.network.*;
import com.uet.server.database.dao.AdminDAO;
import com.uet.server.database.dao.AuctionDAO;
import com.uet.server.database.dao.RegisterDAO;
import com.uet.server.database.dao.UserDAO;
import com.uet.server.network.ClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

public class ClientRequestDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(ClientRequestDispatcher.class);

    private final UserDAO userDAO = new UserDAO();
    private final RegisterDAO registerDAO = new RegisterDAO();
    private final AuctionDAO auctionDAO = new AuctionDAO();
    private final AuctionRealtimeService auctionRealtimeService = new AuctionRealtimeService();
    private final AuctionService auctionService = new AuctionService(this.auctionDAO);

    public boolean dispatch(Object obj, ClientHandler client) {
        
        if (obj instanceof GetAllUsersRequest) {
            handleGetAllUsers(client);
            return true;
        }

        
        if (obj instanceof UpdateUserStatusRequest request) {
            try {
                userDAO.updateActive(request.getUserId(), request.isActive());
                client.send(Response.success("Cập nhật trạng thái tài khoản thành công!", null));
                logger.info("[Server] Đã cập nhật trạng thái active = {} cho user ID: {}", request.isActive(), request.getUserId());
                AdminDAO.logAdminAction(request.isActive() ? "Mở khóa tài khoản" : "Khóa tài khoản", request.getUserId(), "Thành công");
            } catch (Exception e) {
                logger.error("Lỗi khi cập nhật trạng thái hoạt động của User ID: " + request.getUserId(), e);
                client.send(Response.fail("Lỗi Server: Không thể cập nhật trạng thái người dùng."));
                AdminDAO.logAdminAction(request.isActive() ? "Mở khóa tài khoản" : "Khóa tài khoản", request.getUserId(), "Thất bại");
            }
            return true;
        }

        
        if (obj instanceof DeleteUserRequest request) {
            try {
                userDAO.deleteUser(request.getUserId());
                client.send(Response.success("Xóa tài khoản người dùng thành công!", null));
                logger.info("[Server] Đã xử lý xóa thành công user ID: {}", request.getUserId());
                AdminDAO.logAdminAction("Xóa tài khoản", request.getUserId(), "Thành công");
            } catch (Exception e) {
                logger.error("Lỗi khi xử lý xóa User ID: " + request.getUserId(), e);
                client.send(Response.fail("Lỗi Server: Không thể xóa tài khoản người dùng."));
                AdminDAO.logAdminAction("Xóa tài khoản", request.getUserId(), "Thất bại");
            }
            return true;
        }

        
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

        if (obj instanceof GetActiveAuctionsRequest request) {
            handleGetActiveAuctions(request, client);
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

        if (obj instanceof GetNotificationsRequest request) {
            handleGetNotifications(request, client);
            return true;
        }

        if (obj instanceof ChangePasswordRequest request) {
            handleChangePassword(request, client);
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

        if (obj instanceof DeleteProductRequest deleteReq) {
            auctionRealtimeService.handleDeleteAuction(deleteReq, client);
            return true;
        }

        if (obj instanceof ForceEndRequest forceEndReq) {
            auctionRealtimeService.handleForceEndAuction(forceEndReq.getAuctionId(), client);
            return true;
        }

        
        if (obj instanceof AuctionItem item) {
            handleUpdateAuction(item, client);
            return true;
        }

        if (obj instanceof String str && "REQUEST_ADMIN_DASHBOARD".equals(str)) {
            handleGetAdminDashboard(client);
            return true;
        }
        
        if (obj instanceof TransactionRequest request) {
            try {
                userDAO.createTransaction(
                        request.getUserId(),
                        request.getAmount(),
                        request.getType()
                );
                client.send(Response.success("Đã gửi yêu cầu chờ admin duyệt", null));
            } catch (Exception e) {
                logger.error("Lỗi khi tạo yêu cầu giao dịch: ", e);
                client.send(Response.fail("Không thể gửi yêu cầu"));
            }
            return true;
        }

        
        if (obj instanceof GetPendingTransactionRequest) {
            try {
                List<Transaction> list = userDAO.getPendingTransaction();
                client.send(Response.success("Load pending transaction thành công", list));
            } catch (Exception e) {
                logger.error("Lỗi khi lấy danh sách giao dịch chờ duyệt: ", e);
                client.send(Response.fail("Không thể lấy danh sách giao dịch"));
            }
            return true;
        }

        if (obj instanceof ApproveTransactionRequest request) {
            try {
                User updatedUser = userDAO.approveTransaction(request.getTransactionId());
                client.send(Response.success("Duyệt giao dịch thành công", null));
                AdminDAO.logAdminAction("Duyệt giao dịch", String.valueOf(request.getTransactionId()), "Thành công");
                if (updatedUser != null) {
                    com.uet.server.network.ClientManager.broadcast(Response.success("BALANCE_UPDATED", updatedUser));
                }
            } catch (Exception e) {
                logger.error("Lỗi khi phê duyệt giao dịch ID: " + request.getTransactionId(), e);
                client.send(Response.fail("Phê duyệt giao dịch thất bại"));
                AdminDAO.logAdminAction("Duyệt giao dịch", String.valueOf(request.getTransactionId()), "Thất bại");
            }
            return true;
        }

        client.send(Response.fail("Yêu cầu không hợp lệ"));
        return true;
    }



    private void handleGetAllUsers(ClientHandler client) {
        try {
            List<User> users = userDAO.getAllUsers();
            client.send(new GetAllUsersResponse(users));
        } catch (Exception e) {
            logger.error("Lỗi khi tải danh sách người dùng: ", e);
            client.send(Response.fail("Lỗi hệ thống khi tải danh sách người dùng."));
        }
    }

    private void handleLogin(LoginRequest request, ClientHandler client) {
        logger.info("Đang đăng nhập: {}", request.getUsername());
        client.send(userDAO.handleLogin(request));
    }

    private void handleRegister(RegisterRequest request, ClientHandler client) {
        client.send(registerDAO.handleRegister(request));
    }

    private void handleUpdateRole(UpdateRoleRequest request, ClientHandler client) {
        try {
            userDAO.updateRole(request.getUserId(), request.getRole());
            client.send(Response.success("Cập nhật quyền thành công", null));
        } catch (Exception e) {
            client.send(Response.fail("Lỗi cập nhật quyền"));
        }
    }

    private void handleGetActiveAuctions(GetActiveAuctionsRequest request, ClientHandler client) {
        if ("USER".equalsIgnoreCase(request.getType())) {
            List<AuctionItem> auctions = auctionDAO.getAuctionsForUser(request.getUserId());
            client.send(new GetActiveAuctionsResponse(auctions));
        } else if ("SINGLE".equalsIgnoreCase(request.getType())) {
            AuctionItem item = auctionDAO.getAuctionById(request.getUserId());
            List<AuctionItem> list = new ArrayList<>();
            if (item != null) {
                list.add(item);
            }
            client.send(new GetActiveAuctionsResponse(list));
        } else {
            List<AuctionItem> auctions = auctionDAO.getActiveAuctions();
            client.send(new GetActiveAuctionsResponse(auctions));
        }
    }

    private void handleUpdateProfile(UpdateProfileRequest request, ClientHandler client) {
        Response response = userDAO.updateProfile(request);
        client.send(response);
    }

    private void handleUpdateAuction(AuctionItem item, ClientHandler client) {
        logger.info("[Server] Nhận yêu cầu chỉnh sửa sản phẩm ID: {}", item.getAuctionId());
        try {
            
            if (item.getProductImageBytes() != null && item.getProductImageBytes().length > 0) {
                FileStorageService fileStorageService = new FileStorageService();
                com.uet.common.network.ImageData imgData = new com.uet.common.network.ImageData("product.png", "image/png", item.getProductImageBytes());
                String newImageUrl = fileStorageService.save(imgData, "products", item.getSellerId());
                if (newImageUrl != null) {
                    item.setImageUrl(newImageUrl);
                    logger.info("[Server] Đã cập nhật ảnh sản phẩm mới thành công tại: {}", newImageUrl);
                }
            }

            
            boolean isUpdated = auctionDAO.updateAuction(item);

            if (isUpdated) {
                client.send(Response.success("Cập nhật thông tin sản phẩm thành công! Vui lòng chờ phê duyệt lại.", null));

                
                try {
                    List<AuctionItem> activeAuctions = auctionDAO.getActiveAuctions();
                    com.uet.server.network.ClientManager.broadcast(new GetActiveAuctionsResponse(activeAuctions));
                } catch (Exception e) {
                    logger.error("Lỗi khi phát sóng danh sách đấu giá mới sau khi cập nhật sản phẩm: ", e);
                }
            } else {
                client.send(Response.fail("Lỗi hệ thống: Không thể ghi dữ liệu cập nhật sản phẩm vào MySQL Database."));
            }

        } catch (Exception e) {
            logger.error("Lỗi khi xử lý chỉnh sửa sản phẩm: ", e);
            client.send(Response.fail("Hệ thống gặp lỗi ngoài ý muốn khi chỉnh sửa sản phẩm!"));
        }
    }
    
    private void handleGetNotifications(GetNotificationsRequest request, ClientHandler client) {
        String userId = request.getUserId();
        logger.info("[Server] Đang xử lý lấy thông báo cho User ID: {}", userId);

        try {
            
            List<com.uet.common.model.notification.Notification> list = userDAO.getNotificationsByUserId(userId);
            logger.info("[Server] Đã tìm thấy {} thông báo trong DB của User: {}", list.size(), userId);

            
            client.send(Response.success("Tải danh sách thông báo thành công", list));
            logger.info("[Server] Đã bắn gói tin phản hồi thành công về Client.");

        } catch (Exception e) {
            logger.error("[Server LỖI] Sự cố tại handleGetNotifications của User: " + userId, e);
            client.send(Response.fail("Lỗi Server: Không thể lấy danh sách thông báo hiện tại."));
        }
    }

    private void handleChangePassword(ChangePasswordRequest request, ClientHandler client) {
        Response response = userDAO.changePassword(request.getUserId(), request.getOldPassword(), request.getNewPassword());
        client.send(response);
    }

    private void handleGetAdminDashboard(ClientHandler client) {
        client.send(new AdminDAO().getDashboardData());
    }
}