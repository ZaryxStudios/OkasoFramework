package com.zaryxstudios.okaso.common.task;

public interface TaskHandle {
    boolean isCancelled();
    void cancel();
    int getTaskId();
}
