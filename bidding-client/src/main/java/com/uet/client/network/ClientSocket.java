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
import com.uet.client.util.ThreadPoolManager;
import com.uet.common.model.user.User;
import com.uet.common.network.Response;

public class ClientSocket {
    private static final Logger logger = LoggerFactory.getLogger(ClientSocket.class);
    private static ClientSocket instance;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private volatile boolean listening = false;

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

            
            socket.setTcpNoDelay(true);

            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            this.listening = true;
            startListening();
            logger.info("ClientSocket: Kết nối Server thành công!");
        }
    }

    public synchronized void send(Object msg) throws IOException {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        out.writeObject(msg);
        out.flush();

        
        out.reset();
    }

    private volatile boolean isListenerRunning = false;

    private void startListening() {
        if (isListenerRunning) {
            return;
        }

        this.isListenerRunning = true;
        this.listening = true;

        ThreadPoolManager.execute(() -> {
            while (listening) {
                try {
                    if (in == null) break;

                    Object message = in.readObject();
                    notifyListeners(message);

                } catch (java.io.EOFException | java.net.SocketException e) {
                    
                    
                    if (!listening) {
                        logger.info("ClientSocket: Luồng nghe ngầm đã dừng an toàn sau khi Đăng xuất.");
                    } else {
                        logger.warn("Đột ngột mất kết nối vật lý tới Server!");
                        stopListening();
                    }
                    break; 
                } catch (Exception e) {
                    if (listening) {
                        logger.error("Lỗi xảy ra trong luồng nghe ClientSocket: ", e);
                    }
                    stopListening();
                    break;
                }
            }
            isListenerRunning = false;
        });
    }

    public static Consumer<User> onUserUpdated;

    private void notifyListeners(Object message) {
        if (message instanceof Response res && "BALANCE_UPDATED".equals(res.getMessage())) {
            if (res.getData() instanceof User updatedUser) {
                if (onUserUpdated != null) {
                    onUserUpdated.accept(updatedUser);
                }
            }
        }
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
            
            stopListening();

            
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
            logger.error("Lỗi xảy ra khi đang đóng tài nguyên Socket: ", e);
        } finally {
            
            
            this.socket = null;
            this.in = null;
            this.out = null;
            logger.info("ClientSocket: Đã dọn dẹp sạch sẽ Session kết nối cũ!");
        }
    }
}