package com.uet.common.network;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AddProductRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String sellerId;
    private String productName;
    private String description;
    private double startPrice;
    private ImageData productImage; 
    private String itemType;        

    
    private String brand;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    
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