package com.uet.server.network;

import com.uet.server.service.AuctionBroadcastService;
import com.uet.server.service.AuctionScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.uet.server.util.ServerThreadPool;

public class ServerMain {
    private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);
    
    // Đã chuyển sang dùng ServerThreadPool dùng chung

    public static void main(String[] args){
        
        
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        
        AuctionScheduler scheduler = new AuctionScheduler();
        scheduler.start();
        new ServerMain().start();
    }
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(27915)) {
            logger.info("--- SERVER ĐANG CHỜ Ở CỔNG 27915 ---");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("--- CÓ 1 CLIENT VỪA KẾT NỐI! ---");

                
                ClientHandler handler = new ClientHandler(clientSocket);
                ServerThreadPool.execute(handler); // Bỏ vào Thread Pool để xử lý
            }
        } catch (Exception e) {
            logger.error("Lỗi xảy ra trong ServerMain: ", e);
        }
    }
}