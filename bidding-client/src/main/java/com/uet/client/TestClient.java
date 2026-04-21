package com.uet.client;

import com.uet.common.network.LoginRequest;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class TestClient {
    public static void main(String[] args) {
        try (
                Socket socket = new Socket("localhost", 27915);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            LoginRequest request = new LoginRequest("admin1", "123456");

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();
            System.out.println("Server response: " + response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}