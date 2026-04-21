package com.uet.server.network;

import com.uet.common.network.LoginRequest;
import com.uet.server.database.dao.UserDAO;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    UserDAO userDAO = new UserDAO();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            while (true) {
                Object obj = in.readObject();

                if (obj instanceof LoginRequest request) {

                    System.out.println("Login attempt: " + request.getUsername());

                    boolean ok = userDAO.checkLogin(request.getUsername(), request.getPassword());

                    if (ok) {
                        out.writeObject("LOGIN_SUCCESS");
                    } else {
                        out.writeObject("LOGIN_FAIL");
                    }

                    out.flush();
                } else {
                    out.writeObject("UNKNOWN_REQUEST");
                    out.flush();
                }
            }

        } catch (Exception e) {
            System.out.println("Client disconnected.");
        }
    }
}