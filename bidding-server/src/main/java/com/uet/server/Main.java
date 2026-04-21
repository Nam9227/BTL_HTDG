package com.uet.server;

import com.uet.server.network.ServerMain;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== HỆ THỐNG ĐẤU GIÁ UET K70 STARTING... ===");
        ServerMain server = new ServerMain();
        server.start();
    }
}