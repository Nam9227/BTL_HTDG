package com.uet.server;

import com.uet.server.socket.AuctionServer;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== HỆ THỐNG ĐẤU GIÁ UET K70 STARTING... ===");
        AuctionServer server = new AuctionServer();
        server.start();
    }
}