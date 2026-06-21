package com.zaryxstudios.okaso.common;

public interface Lifecycle {

    void start();

    void stop();
    
    boolean isRunning();
}
