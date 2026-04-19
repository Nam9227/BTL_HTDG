package com.uet.server.model.item;

public class Electronics extends Item {
    private String brand;
    private int warrantyMonths;

    public Electronics() {}

    public Electronics(String id, String name, String description,
                       double startingPrice, double currentPrice, String imageUrl,
                       String brand, int warrantyMonths) {
        super(id, name, description, startingPrice, currentPrice, imageUrl);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    public String getBrand() { return brand; }
    public int getWarrantyMonths() { return warrantyMonths; }

    public void setBrand(String brand) { this.brand = brand; }
    public void setWarrantyMonths(int warrantyMonths) { this.warrantyMonths = warrantyMonths; }

    @Override
    public String getCategory() {
        return "Electronics";
    }
}