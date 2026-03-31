package com.uet.client.network;

import java.io.*;
import java.net.Socket;

public class ClientSocket {
    private static ClientSocket instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private ClientSocket() {}

    public static ClientSocket getInstance() {
        if (instance == null) instance = new ClientSocket();
        return instance;
    }

    public void connect() throws IOException {
        if (socket == null || socket.isClosed()) {
            socket = new Socket("localhost", 915227);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        }
    }

    public void send(Object msg) throws IOException {
        out.writeObject(msg);
        out.flush();
    }

    public Object receive() throws IOException, ClassNotFoundException {
        return in.readObject();
    }
}