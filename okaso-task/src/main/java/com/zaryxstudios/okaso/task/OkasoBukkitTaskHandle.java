package com.zaryxstudios.okaso.task;

import com.zaryxstudios.okaso.common.task.TaskHandle;

import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import lombok.Getter;

public class OkasoBukkitTaskHandle implements TaskHandle {

    private final BukkitScheduler scheduler;
    @Getter
    private final int taskId;
    // BukkitTask.isCancelled() is not present in the 1.8 API, so track cancellation here
    private volatile boolean cancelled;

    public OkasoBukkitTaskHandle(BukkitScheduler scheduler, BukkitTask task) {
        if (scheduler == null) {
            throw new IllegalArgumentException("Bukkit scheduler cannot be null");
        }
        if (task == null) {
            throw new IllegalArgumentException("Bukkit task cannot be null");
        }
        this.scheduler = scheduler;
        this.taskId = task.getTaskId();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void cancel() {
        if (!cancelled) {
            scheduler.cancelTask(taskId);
            cancelled = true;
        }
    }
}
