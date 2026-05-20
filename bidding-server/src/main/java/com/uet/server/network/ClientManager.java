package com.uet.server.network;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientManager {
    private static final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    private static final ConcurrentHashMap<String, Set<ClientHandler>> auctionViewers =
            new ConcurrentHashMap<>();

    public static void addClient(ClientHandler client) {
        clients.add(client);
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);

        for (Set<ClientHandler> viewers : auctionViewers.values()) {
            viewers.remove(client);
        }
    }

    public static void broadcast(Object message) {
        for (ClientHandler client : clients) {
            client.send(message);
        }
    }

    public static void joinAuction(String auctionId, ClientHandler client) {
        auctionViewers
                .computeIfAbsent(auctionId, key -> ConcurrentHashMap.newKeySet())
                .add(client);

        System.out.println("Client joined auction: " + auctionId);
    }

    public static void leaveAuction(String auctionId, ClientHandler client) {
        Set<ClientHandler> viewers = auctionViewers.get(auctionId);

        if (viewers != null) {
            viewers.remove(client);

            if (viewers.isEmpty()) {
                auctionViewers.remove(auctionId);
            }
        }

        System.out.println("Client left auction: " + auctionId);
    }

    public static void broadcastAuction(String auctionId, Object message) {
        Set<ClientHandler> viewers = auctionViewers.get(auctionId);

        if (viewers == null || viewers.isEmpty()) {
            System.out.println("Không có client nào đang xem auction: " + auctionId);
            return;
        }

        System.out.println("Gửi cập nhật ngay cho " + viewers.size()
                + " client đang xem auction: " + auctionId);

        for (ClientHandler client : viewers) {
            client.send(message);
        }
    }

    public static Set<String> getActiveAuctionIds() {
        return auctionViewers.keySet();
    }
}