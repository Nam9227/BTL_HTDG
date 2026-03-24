package com.uet.client.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AuctionController {
    @FXML private Label priceLabel;
    private int currentPrice = 100;

    @FXML
    public void handleBid() {
        currentPrice += 10;
        priceLabel.setText("Giá: " + currentPrice + "$");
    }
}