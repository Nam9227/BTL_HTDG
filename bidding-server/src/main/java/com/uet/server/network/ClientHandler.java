package com.uet.server.network;

import com.uet.common.network.Response;
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
                    send(userDAO.handleLogin(request));

                } else if (obj instanceof UpdateRoleRequest request) {
                    userDAO.updateRole(request.getUserId(), request.getRole());
                    send(Response.success("Cập nhật quyền thành công", null));

                }  else if (obj instanceof RegisterRequest request) {
                    send(registerDAO.handleRegister(request));

                } else if ("LOGOUT".equals(obj)) {
                    send(Response.success("Đăng xuất thành công", null));
                    break;

                } else {
                    send(Response.fail("Yêu cầu không hợp lệ"));
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