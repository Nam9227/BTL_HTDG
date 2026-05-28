package com.uet.common.model.item;

public class Furniture extends Item {

    private String material;
    private String dimensions;

    public Furniture() {
    }

    public Furniture(String material, String dimensions) {
        this.material = material;
        this.dimensions = dimensions;
    }

    public String getMaterial() {
        return material;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public String getCategory() {
        return "FURNITURE";
    }
}