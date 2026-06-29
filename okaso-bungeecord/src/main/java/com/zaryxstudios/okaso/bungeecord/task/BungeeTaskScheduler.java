package com.zaryxstudios.okaso.bungeecord.task;

import com.zaryxstudios.okaso.common.task.TaskHandle;
import com.zaryxstudios.okaso.common.task.TaskScheduler;

import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class BungeeTaskScheduler implements TaskScheduler {

    private final Plugin plugin;
    private final net.md_5.bungee.api.scheduler.TaskScheduler scheduler;

    public BungeeTaskScheduler(Plugin plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getProxy().getScheduler();
    }

    @Override
    public TaskHandle runAsync(Runnable task) {
        return new BungeeTaskHandle(scheduler.runAsync(plugin, task));
    }

    @Override
    public TaskHandle runLater(Runnable task, long delay, TimeUnit unit) {
        long millis = unit.toMillis(delay);
        return new BungeeTaskHandle(scheduler.schedule(plugin, task, millis, TimeUnit.MILLISECONDS));
    }

    @Override
    public TaskHandle runTimer(Runnable task, long delay, long interval, TimeUnit unit) {
        long delayMillis = unit.toMillis(delay);
        long intervalMillis = unit.toMillis(interval);
        return new BungeeTaskHandle(scheduler.schedule(plugin, task, delayMillis, intervalMillis, TimeUnit.MILLISECONDS));
    }

    @Override
    public TaskHandle runSync(Runnable task) {
        return new BungeeTaskHandle(scheduler.runAsync(plugin, task));
    }

    @Override
    public void cancelAll() {
        scheduler.cancel(plugin);
    }
}
