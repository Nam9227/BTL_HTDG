package com.uet.server.network;

import com.uet.server.service.ClientRequestDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private final Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private final ClientRequestDispatcher dispatcher = new ClientRequestDispatcher();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void send(Object message) {
        try {
            out.writeObject(message);
            out.flush();
            out.reset();
        } catch (java.net.SocketException e) {
            logger.warn("Server: Không thể gửi phản hồi do Client đã chủ động ngắt kết nối vật lý (Đăng xuất/Tắt app).");
        } catch (Exception e) {
            logger.error("Lỗi xảy ra khi gửi dữ liệu cho Client: ", e);
            ClientManager.removeClient(this);
        }
    }

    @Override
    public void run() {
        try {
            initStreams();
            ClientManager.addClient(this);

            listenClientMessages();

        } catch (Exception e) {
            logger.info("Client ngắt kết nối hoặc có lỗi xảy ra: {}", e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void initStreams() throws Exception {
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }

    private void listenClientMessages() throws Exception {
        while (true) {
            Object message = in.readObject();

            boolean keepRunning = dispatcher.dispatch(message, this);

            if (!keepRunning) {
                break;
            }
        }
    }

    private void cleanup() {
        try {
            // 1. Chủ động đóng luồng ghi dữ liệu trước
            if (out != null) {
                try { out.close(); } catch (Exception ignored) {}
            }

            // 2. Chủ động đóng luồng đọc (Ép in.readObject() văng Exception để thoát vòng lặp)
            if (in != null) {
                try { in.close(); } catch (Exception ignored) {}
            }

            // 3. Đóng Socket vật lý
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            logger.info("🔌 [SERVER] Đã giải phóng hoàn toàn kết nối Socket vật lý.");

        } catch (Exception ignored) {
            // Đúng bài Clean Code, những lỗi đóng tài nguyên này có thể bỏ qua
        } finally {
            // 🌟 BẮT BUỘC ĐỂ Ở ĐÂY: Dù đống đóng Socket ở trên có lỗi hay không,
            // thì Client này VẪN PHẢI được xóa khỏi danh sách quản lý để tránh rò rỉ RAM!
            ClientManager.removeClient(this);
            logger.info("🗑️ [SERVER] Đã Xóa Client khỏi ClientManager thành công.");
        }
    }
}