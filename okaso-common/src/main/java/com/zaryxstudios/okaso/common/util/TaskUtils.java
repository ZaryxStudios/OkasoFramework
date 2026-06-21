package com.zaryxstudios.okaso.common.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class TaskUtils {

    private static final ScheduledExecutorService SCHEDULER =
        Executors.newScheduledThreadPool(4, new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "Okaso-Async-" + count.incrementAndGet());
                t.setDaemon(true);
                return t;
            }
        });

    public static ScheduledExecutorService getScheduler() {
        return SCHEDULER;
    }

    private TaskUtils() {}
}
