package com.uet.server.network;

import com.uet.server.service.ClientRequestDispatcher;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

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
        }catch (java.net.SocketException e) {
            System.out.println(" Server: Không thể gửi phản hồi do Client đã chủ động ngắt kết nối vật lý (Đăng xuất/Tắt app).");
        } catch (Exception e) {
            e.printStackTrace();
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
            System.out.println("Client disconnected because:");
            e.printStackTrace();
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

            System.out.println("🔌 [SERVER] Đã giải phóng hoàn toàn kết nối Socket vật lý.");

        } catch (Exception ignored) {
            // Đúng bài Clean Code, những lỗi đóng tài nguyên này có thể bỏ qua
        } finally {
            // 🌟 BẮT BUỘC ĐỂ Ở ĐÂY: Dù đống đóng Socket ở trên có lỗi hay không,
            // thì Client này VẪN PHẢI được xóa khỏi danh sách quản lý để tránh rò rỉ RAM!
            ClientManager.removeClient(this);
            System.out.println("🗑️ [SERVER] Đã Xóa Client khỏi ClientManager thành công.");
        }
    }
}