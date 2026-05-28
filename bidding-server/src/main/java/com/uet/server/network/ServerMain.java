package com.uet.server.network;

import com.uet.server.service.AuctionBroadcastService;
import com.uet.server.service.AuctionScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);

    public static void main(String[] args){
        // Thiết lập múi giờ mặc định của Server JVM sang Asia/Ho_Chi_Minh (GMT+7)
        // giúp tránh lệch 7 tiếng khi deploy hệ thống Server lên các VPS quốc tế sử dụng múi giờ UTC.
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        AuctionBroadcastService.start();
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

                // Tạo một luồng (Thread) mới để phục vụ riêng client này
                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start();
            }
        } catch (Exception e) {
            logger.error("Lỗi xảy ra trong ServerMain: ", e);
        }
    }
}