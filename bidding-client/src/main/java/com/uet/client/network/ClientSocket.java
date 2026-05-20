package com.uet.client.network;

import com.uet.client.config.AppConfig;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ClientSocket {
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

            startListening();
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
        if (listening) {
            return;
        }

        listening = true;

        listenerThread = new Thread(() -> {
            while (listening) {
                try {
                    Object message = in.readObject();
                    notifyListeners(message);

                } catch (Exception e) {
                    if (listening) {
                        e.printStackTrace();
                    }
                    stopListening();
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
                e.printStackTrace();
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

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}