package com.uet.server.socket;

import com.uet.server.dao.UserDAO;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private UserDAO userDAO = new UserDAO();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String request;
            while ((request = in.readLine()) != null) {
                request = request.trim();
                System.out.println("Nhận từ Client: " + request);

                // Giả sử Client gửi: LOGIN:nam:123
                if (request.startsWith("LOGIN:")) {
                    String[] parts = request.split(":");
                    String user = parts[1];
                    String pass = parts[2];

                    // Gọi DAO để check trong MySQL
                    boolean isOk = userDAO.checkLogin(user, pass);
                    if (isOk) {
                        out.println("LOGIN_SUCCESS");
                    } else {
                        out.println("LOGIN_FAILED");
                    }
                }
                // Tí nữa Nam viết thêm logic BID (đấu giá) ở dưới này nhé
            }
        } catch (IOException e) {
            System.err.println("Một đại gia đã rời sàn!");
        }
    }
}