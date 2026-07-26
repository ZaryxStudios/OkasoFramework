package com.zaryxstudios.okaso.task;

import com.zaryxstudios.okaso.common.task.TaskHandle;
import com.zaryxstudios.okaso.common.task.TaskScheduler;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.concurrent.TimeUnit;

public class OkasoBukkitTaskScheduler implements TaskScheduler {

    private final Plugin plugin;
    private final BukkitScheduler scheduler;

    public OkasoBukkitTaskScheduler(Plugin plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
    }

    @Override
    public TaskHandle runAsync(Runnable task) {
        return new OkasoBukkitTaskHandle(scheduler.runTaskAsynchronously(plugin, task));
    }

    @Override
    public TaskHandle runLater(Runnable task, long delay, TimeUnit unit) {
        long ticks = toTicks(delay, unit);
        return new OkasoBukkitTaskHandle(scheduler.runTaskLater(plugin, task, ticks));
    }

    @Override
    public TaskHandle runTimer(Runnable task, long delay, long interval, TimeUnit unit) {
        long delayTicks = toTicks(delay, unit);
        long intervalTicks = toTicks(interval, unit);
        return new OkasoBukkitTaskHandle(scheduler.runTaskTimer(plugin, task, delayTicks, intervalTicks));
    }

    @Override
    public TaskHandle runSync(Runnable task) {
        return new OkasoBukkitTaskHandle(scheduler.runTask(plugin, task));
    }

    @Override
    public void cancelAll() {
        scheduler.cancelTasks(plugin);
    }

    private static long toTicks(long duration, TimeUnit unit) {
        long millis = unit.toMillis(duration);
        if (millis < 50) return 1;
        return millis / 50;
    }
}
