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
        ClientManager.removeClient(this);

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception ignored) {
        }
    }
}