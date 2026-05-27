package com.uet.client.network;

import com.uet.client.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ClientSocket {
    private static final Logger logger = LoggerFactory.getLogger(ClientSocket.class);
    private static ClientSocket instance;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private volatile boolean listening = false;
    private Thread listenerThread;

    private final List<Consumer<Object>> listeners = new CopyOnWriteArrayList<>();

    private ClientSocket() {
    }

    public static ClientSocket getInstance() {
        if (instance == null) {
            instance = new ClientSocket();
        }
        return instance;
    }

    public synchronized void connect() throws IOException {
        if (socket == null || socket.isClosed()) {
            socket = new Socket(AppConfig.get("server.host"), AppConfig.getInt("server.port"));
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // Đảm bảo cờ hiệu được dựng lên trước khi kích hoạt luồng nghe
            this.listening = true;
            startListening();
            logger.info("🔌 ClientSocket: Kết nối Server thành công!");
        }
    }

    public synchronized void send(Object msg) throws IOException {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        out.writeObject(msg);
        out.flush();
    }

    private void startListening() {
        // Nếu luồng cũ đang chạy thì không tạo luồng mới trùng lặp
        if (listenerThread != null && listenerThread.isAlive()) {
            return;
        }

        this.listening = true;

        listenerThread = new Thread(() -> {
            while (listening) {
                try {
                    if (in == null) break;

                    Object message = in.readObject();
                    notifyListeners(message);

                } catch (java.io.EOFException | java.net.SocketException e) {
                    // 🌟 MẸO KHỬ LỖI ĐỎ: Nếu ta chủ động gọi close(), biến listening sẽ bằng false.
                    // Khi đó, việc dính EOFException là hoàn toàn bình thường, ta cho luồng chết êm ái, không in lỗi ra.
                    if (!listening) {
                        logger.info("🔌 ClientSocket: Luồng nghe ngầm đã dừng an toàn sau khi Đăng xuất.");
                    } else {
                        logger.warn("⚠️ Đột ngột mất kết nối vật lý tới Server!");
                        stopListening();
                    }
                    break; // Thoát hẳn vòng lặp while để hủy Thread ngầm
                } catch (Exception e) {
                    if (listening) {
                        logger.error("Lỗi xảy ra trong luồng nghe ClientSocket: ", e);
                    }
                    stopListening();
                    break;
                }
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void notifyListeners(Object message) {
        for (Consumer<Object> listener : listeners) {
            try {
                listener.accept(message);
            } catch (Exception e) {
                logger.error("Lỗi xảy ra khi truyền tin tới listener: ", e);
            }
        }
    }

    public void addMessageListener(Consumer<Object> listener) {
        listeners.add(listener);
    }

    public void removeMessageListener(Consumer<Object> listener) {
        listeners.remove(listener);
    }

    public void stopListening() {
        listening = false;
    }

    public void close() {
        try {
            // Hạ cờ hiệu nghe xuống trước để vòng lặp while nhận biết hành vi chủ động đóng
            stopListening();

            // Đóng tuần tự từ Stream ra đến Socket vật lý
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

        } catch (Exception e) {
            logger.error("⚠️ Lỗi xảy ra khi đang đóng tài nguyên Socket: ", e);
        } finally {
            // 🌟 QUAN TRỌNG NHẤT: Xóa trắng toàn bộ Object cũ về null
            // Để lần sau khi quay lại màn Login bấm nút Đăng nhập, hàm connect() check (socket == null) sẽ tự tạo luồng mới tinh.
            this.socket = null;
            this.in = null;
            this.out = null;
            this.listenerThread = null;
            logger.info("🗑️ ClientSocket: Đã dọn dẹp sạch sẽ Session kết nối cũ!");
        }
    }
}