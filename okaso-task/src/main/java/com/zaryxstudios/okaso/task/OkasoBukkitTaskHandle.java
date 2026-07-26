package com.zaryxstudios.okaso.task;

import com.zaryxstudios.okaso.common.task.TaskHandle;

import org.bukkit.scheduler.BukkitTask;

import lombok.Getter;

public class OkasoBukkitTaskHandle implements TaskHandle {

    private final BukkitTask task;
    @Getter
    private final int taskId;

    public OkasoBukkitTaskHandle(BukkitTask task) {
        this.task = task;
        this.taskId = task.getTaskId();
    }

    @Override
    public boolean isCancelled() {
        return task.isCancelled();
    }

    @Override
    public void cancel() {
        task.cancel();
    }
}
