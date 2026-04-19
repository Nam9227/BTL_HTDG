package com.uet.client.model.item;

public class Art extends Item {
    private String artist;
    private int yearCreated;

    public Art() {}

    public Art(String id, String name, String description,
               double startingPrice, double currentPrice, String imageUrl,
               String artist, int yearCreated) {
        super(id, name, description, startingPrice, currentPrice, imageUrl);
        this.artist = artist;
        this.yearCreated = yearCreated;
    }

    public String getArtist() { return artist; }
    public int getYearCreated() { return yearCreated; }

    public void setArtist(String artist) { this.artist = artist; }
    public void setYearCreated(int yearCreated) { this.yearCreated = yearCreated; }

    @Override
    public String getCategory() {
        return "Art";
    }
}