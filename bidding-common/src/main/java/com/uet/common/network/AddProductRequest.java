package com.uet.common.network;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AddProductRequest implements Serializable {
    // Mã định danh phiên bản để đảm bảo Client và Server đồng bộ cấu trúc Object
    private static final long serialVersionUID = 1L;

    private String sellerId;
    private String productName;
    private String description;
    private double startPrice;
    private ImageData productImage; // Đối tượng chứa mảng byte ảnh đơn của Nam
    private String itemType;        // Chuỗi tiếng Anh lưu loại sản phẩm ("Electronics", "Fashion"...)

    // 🌟 ĐÃ ĐƯA ĐÚNG VÀO TRONG CLASS: Khai báo thêm 3 thuộc tính mới
    private String brand;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // 🌟 ĐÃ CẬP NHẬT CONSTRUCTOR: Truyền đầy đủ tham số từ Client lên
    public AddProductRequest(String sellerId, String productName, String description, double startPrice,
                             ImageData productImage, String itemType, String brand,
                             LocalDateTime startTime, LocalDateTime endTime) {
        this.sellerId = sellerId;
        this.productName = productName;
        this.description = description;
        this.startPrice = startPrice;
        this.productImage = productImage;
        this.itemType = itemType;
        this.brand = brand;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // =========================================================================
    // ĐẦY ĐỦ CÁC HÀM GETTER VÀ SETTER
    // =========================================================================

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(double startPrice) {
        this.startPrice = startPrice;
    }

    public ImageData getProductImage() {
        return productImage;
    }

    public void setProductImage(ImageData productImage) {
        this.productImage = productImage;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}