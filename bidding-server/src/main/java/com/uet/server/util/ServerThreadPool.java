package com.uet.server.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerThreadPool {
    // Singleton CachedThreadPool cho toàn bộ Server
    private static final ExecutorService pool = Executors.newCachedThreadPool();

    public static void execute(Runnable task) {
        pool.execute(task);
    }
    
    public static void shutdown() {
        if (!pool.isShutdown()) {
            pool.shutdown();
        }
    }
}
