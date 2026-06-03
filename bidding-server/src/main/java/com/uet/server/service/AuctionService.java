package com.uet.server.service;

import com.uet.common.network.AddProductRequest;
import com.uet.common.network.Response;
import com.uet.server.database.dao.AuctionDAO;
import com.uet.server.network.ClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.uet.server.database.dao.UserDAO;

public class AuctionService {
    private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);

    private final FileStorageService fileStorageService = new FileStorageService();
    private final AuctionDAO auctionDAO;

    public AuctionService(AuctionDAO auctionDAO) {
        this.auctionDAO = auctionDAO;
    }

    public void handleRegisterProduct(AddProductRequest req, ClientHandler client) {
        try {
            String imageUrl = null;

            
            if (req.getProductImage() != null && req.getProductImage().getData() != null && req.getProductImage().getData().length > 0) {

                
                String originalName = req.getProductImage().getOriginalFileName();
                String uniqueName = System.currentTimeMillis() + "_" + originalName;
                req.getProductImage().setOriginalFileName(uniqueName);

                
                
                imageUrl = fileStorageService.save(
                        req.getProductImage(),
                        "products",
                        req.getSellerId()
                );
                logger.info("[SERVER] Đã lưu ảnh sản phẩm chờ duyệt tại path: {}", imageUrl);
            } else {
                imageUrl = "/images/default_product.png"; 
            }

            
            boolean isInserted = auctionDAO.createNewAuction(
                    req.getSellerId(),
                    req.getProductName(),
                    req.getDescription(),
                    req.getStartPrice(),
                    imageUrl,         
                    req.getItemType(),
                    req.getBrand(),
                    null,
                    null,
                    req.getStartTime(),
                    req.getEndTime()
            );

            
            if (isInserted) {
                client.send(Response.success("Đăng bán sản phẩm đấu giá thành công! Vui lòng chờ Admin phê duyệt.", null));
                logger.info("[SERVER] Sản phẩm của User {} đang ở trạng thái PENDING.", req.getSellerId());
                
                
                UserDAO userDAO = new UserDAO();
                userDAO.createNotification(req.getSellerId(), "Chờ duyệt sản phẩm", "Sản phẩm '" + req.getProductName() + "' đang chờ Admin duyệt.");
            } else {
                client.send(Response.fail("Lỗi: Không thể ghi dữ liệu sản phẩm vào MySQL Database."));
            }

        } catch (Exception e) {
            logger.error("Lỗi khi xử lý lưu ảnh sản phẩm hoặc ghi DB: ", e);
            try {
                client.send(Response.fail("Hệ thống Server gặp sự cố xử lý dữ liệu!"));
            } catch (Exception ignored) {}
        }
    }
}

