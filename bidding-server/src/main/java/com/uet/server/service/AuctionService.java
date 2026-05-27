package com.uet.server.service;

import com.uet.common.network.AddProductRequest;
import com.uet.common.network.Response;
import com.uet.server.database.dao.AuctionDAO;
import com.uet.server.network.ClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

            // BẮT CHƯỚC 100% CÁCH LƯU ẢNH CỦA USER_DAO:
            if (req.getProductImage() != null && req.getProductImage().getData() != null && req.getProductImage().getData().length > 0) {

                // 1. Sinh tên file duy nhất bằng mã Timestamp chống trùng và chống cache
                String originalName = req.getProductImage().getOriginalFileName();
                String uniqueName = System.currentTimeMillis() + "_" + originalName;
                req.getProductImage().setOriginalFileName(uniqueName);

                // 2. Gọi fileStorageService.save y hệt như bên UserDAO nhưng lưu vào thư mục "products"
                // Truyền vào: Đối tượng ImageData, Tên thư mục cha, và ID định danh (Dùng SellerId hoặc Tên sản phẩm đều được)
                imageUrl = fileStorageService.save(
                        req.getProductImage(),
                        "products",
                        req.getSellerId()
                );
                logger.info("[SERVER] Đã lưu ảnh sản phẩm chờ duyệt tại path: {}", imageUrl);
            } else {
                imageUrl = "/images/default_product.png"; // Ảnh mặc định nếu lỗi
            }

            // 3. Gọi DAO chèn vào MySQL Database với trạng thái PENDING (Chờ duyệt) như Nam yêu cầu
            boolean isInserted = auctionDAO.createNewAuction(
                    req.getSellerId(),
                    req.getProductName(),
                    req.getDescription(),
                    req.getStartPrice(),
                    imageUrl,         // Chuỗi đường dẫn file:// chuẩn chỉnh vừa lưu đĩa xong
                    req.getItemType(),
                    req.getBrand(),
                    null,
                    null,
                    req.getStartTime(),
                    req.getEndTime()
            );

            // 4. Trả phản hồi về cho Client
            if (isInserted) {
                client.send(Response.success("Đăng bán sản phẩm đấu giá thành công! Vui lòng chờ Admin phê duyệt.", null));
                logger.info("[SERVER] Sản phẩm của User {} đang ở trạng thái PENDING.", req.getSellerId());
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

