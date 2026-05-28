package com.uet.common.model.item;

public class Other extends Item {
    private String customNotes;

    public Other() {}

    public Other(String id, String name, String description,
                 double startingPrice, double currentPrice, String imageUrl,
                 String customNotes) {
        super(id, name, description, startingPrice, currentPrice, imageUrl);
        this.customNotes = customNotes;
    }

    public String getCustomNotes() { return customNotes; }

    public void setCustomNotes(String customNotes) { this.customNotes = customNotes; }

    @Override
    public String getCategory() {
        return "Other";
    }
}
