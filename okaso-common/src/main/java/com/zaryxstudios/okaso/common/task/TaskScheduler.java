package com.zaryxstudios.okaso.common.task;

import java.util.concurrent.TimeUnit;

public interface TaskScheduler {
    TaskHandle runAsync(Runnable task);
    TaskHandle runLater(Runnable task, long delay, TimeUnit unit);
    TaskHandle runTimer(Runnable task, long delay, long interval, TimeUnit unit);
    TaskHandle runSync(Runnable task);
    void cancelAll();
}
