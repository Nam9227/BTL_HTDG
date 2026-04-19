package com.uet.client.model.item;

public class Vehicle extends Item {
    private String manufacturer;
    private int year;
    private double mileage;

    public Vehicle() {}

    public Vehicle(String id, String name, String description,
                   double startingPrice, double currentPrice, String imageUrl,
                   String manufacturer, int year, double mileage) {
        super(id, name, description, startingPrice, currentPrice, imageUrl);
        this.manufacturer = manufacturer;
        this.year = year;
        this.mileage = mileage;
    }

    public String getManufacturer() { return manufacturer; }
    public int getYear() { return year; }
    public double getMileage() { return mileage; }

    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public void setYear(int year) { this.year = year; }
    public void setMileage(double mileage) { this.mileage = mileage; }

    @Override
    public String getCategory() {
        return "Vehicle";
    }
}