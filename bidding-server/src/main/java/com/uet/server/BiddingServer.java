package com.uet.server;

import com.uet.client.model.LoginRequest;
import com.uet.client.model.RegisterRequest;
import java.io.*;
import java.net.*;
import java.util.concurrent.Executors;

public class BiddingServer {
    private static final int PORT = 27915;

    public static void main(String[] args) {
        // Dùng Virtual Thread Executor của Java đời mới
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("=== SERVER ĐẤU GIÁ UET (JAVA 25) ===");
            System.out.println("Đang lắng nghe tại cổng: " + PORT);

            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("-> Có kết nối mới từ: " + client.getInetAddress());

                // Mỗi khách hàng kết nối sẽ được xử lý trong một luồng riêng
                executor.submit(() -> handleClient(client));
            }
        } catch (IOException e) {
            System.err.println("Lỗi Server: " + e.getMessage());
        }
    }

    private static void handleClient(Socket client) {
        try (client; // Tự động đóng socket khi xong việc
             ObjectInputStream in = new ObjectInputStream(client.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream())) {

            while (true) {
                // Đọc đối tượng gửi lên từ Client
                Object obj = in.readObject();

                // 1. Xử lý Đăng nhập (Sử dụng Pattern Matching của Java 25)
                if (obj instanceof LoginRequest login) {
                    System.out.println("Thử đăng nhập: " + login.getUsername());
                    if (login.getUsername().equals("admin") && login.getPassword().equals("123")) {
                        out.writeObject("LOGIN_SUCCESS");
                    } else {
                        out.writeObject("LOGIN_FAIL");
                    }
                }

                // 2. Xử lý Đăng ký
                else if (obj instanceof RegisterRequest reg) {
                    System.out.println("Yêu cầu đăng ký: " + reg.getUsername());
                    // Tạm thời cứ cho đăng ký thành công để Nam test giao diện
                    out.writeObject("REGISTER_SUCCESS");
                }

                out.flush();
            }
        } catch (EOFException e) {
            System.out.println("Client đã ngắt kết nối.");
        } catch (Exception e) {
            System.err.println("Lỗi xử lý Client: " + e.getMessage());
        }
    }
}