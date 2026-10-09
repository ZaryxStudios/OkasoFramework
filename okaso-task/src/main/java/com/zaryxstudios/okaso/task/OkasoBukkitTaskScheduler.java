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
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
    }

    @Override
    public TaskHandle runAsync(Runnable task) {
        requireTask(task);
        return new OkasoBukkitTaskHandle(scheduler.runTaskAsynchronously(plugin, task));
    }

    @Override
    public TaskHandle runLater(Runnable task, long delay, TimeUnit unit) {
        requireTask(task);
        long ticks = toDelayTicks(delay, unit);
        return new OkasoBukkitTaskHandle(scheduler.runTaskLater(plugin, task, ticks));
    }

    @Override
    public TaskHandle runTimer(Runnable task, long delay, long interval, TimeUnit unit) {
        requireTask(task);
        long delayTicks = toDelayTicks(delay, unit);
        long intervalTicks = toIntervalTicks(interval, unit);
        return new OkasoBukkitTaskHandle(scheduler.runTaskTimer(plugin, task, delayTicks, intervalTicks));
    }

    @Override
    public TaskHandle runSync(Runnable task) {
        requireTask(task);
        return new OkasoBukkitTaskHandle(scheduler.runTask(plugin, task));
    }

    @Override
    public void cancelAll() {
        scheduler.cancelTasks(plugin);
    }

    private static long toDelayTicks(long duration, TimeUnit unit) {
        if (unit == null || duration < 0) {
            throw new IllegalArgumentException("Delay and time unit must be valid");
        }
        return toTicks(duration, unit, false);
    }

    private static long toIntervalTicks(long duration, TimeUnit unit) {
        if (unit == null || duration <= 0) {
            throw new IllegalArgumentException("Interval and time unit must be positive");
        }
        return toTicks(duration, unit, true);
    }

    private static long toTicks(long duration, TimeUnit unit, boolean minimumOne) {
        long millis = unit.toMillis(duration);
        if (millis <= 0) return minimumOne ? 1L : 0L;
        return Math.max(minimumOne ? 1L : 0L, (millis + 49L) / 50L);
    }

    private static void requireTask(Runnable task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
    }
}
