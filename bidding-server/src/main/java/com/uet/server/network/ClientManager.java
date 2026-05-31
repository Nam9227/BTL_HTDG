package com.uet.server.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientManager {
    private static final Logger logger = LoggerFactory.getLogger(ClientManager.class);

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

        logger.info("Client joined auction: {}", auctionId);
    }

    public static void leaveAuction(String auctionId, ClientHandler client) {
        Set<ClientHandler> viewers = auctionViewers.get(auctionId);

        if (viewers != null) {
            viewers.remove(client);

            if (viewers.isEmpty()) {
                auctionViewers.remove(auctionId);
            }
        }

        logger.info("Client left auction: {}", auctionId);
    }

    public static void broadcastAuction(String auctionId, Object message) {
        Set<ClientHandler> viewers = auctionViewers.get(auctionId);

        if (viewers == null || viewers.isEmpty()) {
            logger.debug("Không có client nào đang xem auction: {}", auctionId);
            return;
        }

        logger.debug("Gửi cập nhật ngay cho {} client đang xem auction: {}", viewers.size(), auctionId);

        for (ClientHandler client : viewers) {
            client.send(message);
        }
    }

    public static Set<String> getActiveAuctionIds() {
        return auctionViewers.keySet();
    }
}