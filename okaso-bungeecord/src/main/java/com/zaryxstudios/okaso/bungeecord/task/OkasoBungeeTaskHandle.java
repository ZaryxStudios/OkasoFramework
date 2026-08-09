package com.zaryxstudios.okaso.bungeecord.task;

import com.zaryxstudios.okaso.common.task.TaskHandle;

import net.md_5.bungee.api.scheduler.ScheduledTask;

import lombok.Getter;

public class OkasoBungeeTaskHandle implements TaskHandle {

    private final ScheduledTask task;
    @Getter
    private final int taskId;
    private volatile boolean cancelled;

    public OkasoBungeeTaskHandle(ScheduledTask task) {
        this.task = task;
        this.taskId = task.getId();
        this.cancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void cancel() {
        this.cancelled = true;
        task.cancel();
    }
}
