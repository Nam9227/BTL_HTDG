package com.uet.server.socket;

import com.uet.server.dao.UserDAO;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private UserDAO userDAO = new UserDAO();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // QUAN TRỌNG: Phải tạo OutputStream trước InputStream để tránh bị treo (Deadlock)
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            Object request;
            while ((request = in.readObject()) != null) {
                System.out.println("Nhận Object từ Client: " + request);

                // Nếu Nam gửi chuỗi String "LOGIN:nam:123"
                if (request instanceof String) {
                    String cmd = (String) request;
                    if (cmd.startsWith("LOGIN:")) {
                        String[] parts = cmd.split(":");
                        boolean isOk = userDAO.checkLogin(parts[1].trim(), parts[2].trim());
                        out.writeObject(isOk ? "LOGIN_SUCCESS" : "LOGIN_FAILED");
                        out.flush();
                    }
                }

                // NẾU NAM MUỐN GỬI OBJECT (Cực khuyến khích)
                // if (request instanceof LoginRequest) { ... }
            }
        } catch (Exception e) {
            System.err.println("Một user đã ngắt kết nối!");
        }
    }
}