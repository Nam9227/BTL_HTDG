package com.uet.server.network;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientManager {
    private static final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    public static void addClient(ClientHandler client) {
        clients.add(client);
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public static void broadcast(Object message) {
        for (ClientHandler client : clients) {
            client.send(message);
        }
    }
}