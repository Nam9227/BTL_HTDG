package com.uet.server.socket;

import java.net.ServerSocket;
import java.net.Socket;

public class AuctionServer {
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(27915)) {
            System.out.println("--- SERVER ĐANG CHỜ Ở CỔNG 27915 ---");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("--- CÓ 1 CLIENT VỪA KẾT NỐI! ---");

                // Tạo một luồng (Thread) mới để phục vụ riêng client này
                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}