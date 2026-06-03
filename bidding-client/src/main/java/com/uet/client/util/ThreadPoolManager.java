package com.uet.client.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolManager {
    // Singleton CachedThreadPool cho toàn bộ Client, sử dụng Daemon thread
    private static final ExecutorService pool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    public static void execute(Runnable task) {
        pool.execute(task);
    }
    
    public static void shutdown() {
        if (!pool.isShutdown()) {
            pool.shutdown();
        }
    }
}
