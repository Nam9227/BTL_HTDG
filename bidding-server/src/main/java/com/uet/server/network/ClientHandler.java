package com.uet.server.network;

import com.uet.common.model.user.User;
import com.uet.common.network.LoginRequest;
import com.uet.common.network.RegisterRequest;
import com.uet.common.network.UpdateRoleRequest;
import com.uet.server.database.dao.RegisterDAO;
import com.uet.server.database.dao.UserDAO;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private final UserDAO userDAO = new UserDAO();
    private final RegisterDAO registerDAO = new RegisterDAO();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void send(Object message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (Exception e) {
            ClientManager.removeClient(this);
        }
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            ClientManager.addClient(this);

            while (true) {
                Object obj = in.readObject();

                if (obj instanceof LoginRequest request) {
                    System.out.println("Login attempt: " + request.getUsername());

                    User ok = userDAO.login(
                            request.getUsername(),
                            request.getPassword()
                    );

                    if (ok != null) {
                        send(ok);
                    }
                    else{
                        send("LOGIN_FAIL");
                    }

                }else if (obj instanceof UpdateRoleRequest request) {
                    userDAO.updateRole(request.getUserId(), request.getRole());
                } else if (obj instanceof RegisterRequest request) {

                    String result = registerDAO.register(request);
                    send(result);

                } else if ("LOGOUT".equals(obj)) {
                    send("LOGOUT_SUCCESS");
                    break;

                }else {
                    send("UNKNOWN_REQUEST");
                }
            }

        } catch (Exception e) {
            System.out.println("Client disconnected because:");
            e.printStackTrace();
        } finally {
            ClientManager.removeClient(this);

            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (Exception ignored) {
            }
        }
    }
}