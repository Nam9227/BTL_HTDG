package com.uet.common.model.item;

public class Fashion extends Item {

    private String brand;
    private String size;

    public Fashion() {
    }

    public Fashion(String brand, String size) {
        this.brand = brand;
        this.size = size;
    }

    public String getBrand() {
        return brand;
    }

    public String getSize() {
        return size;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setSize(String size) {
        this.size = size;
    }
    @Override
    public String getCategory() {
        return "FASHION";
    }
}