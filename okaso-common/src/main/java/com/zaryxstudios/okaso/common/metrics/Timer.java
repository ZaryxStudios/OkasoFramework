package com.zaryxstudios.okaso.common.metrics;

import java.util.concurrent.TimeUnit;

public interface Timer {
    void record(long duration, TimeUnit unit);
    long getCount();
    double getAverage(TimeUnit unit);
}
