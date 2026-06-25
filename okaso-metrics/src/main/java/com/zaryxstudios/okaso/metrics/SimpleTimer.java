package com.zaryxstudios.okaso.metrics;

import com.zaryxstudios.okaso.common.metrics.Timer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Getter;

public class SimpleTimer implements Timer {

    @Getter
    private final String name;
    private final AtomicLong totalNanos;
    private final AtomicLong count;

    public SimpleTimer(String name) {
        this.name = name;
        this.totalNanos = new AtomicLong(0);
        this.count = new AtomicLong(0);
    }

    @Override
    public void record(long duration, TimeUnit unit) {
        long nanos = unit.toNanos(duration);
        totalNanos.addAndGet(nanos);
        count.incrementAndGet();
    }

    @Override
    public long getCount() {
        return count.get();
    }

    @Override
    public double getAverage(TimeUnit unit) {
        long c = count.get();
        if (c == 0) return 0.0D;
        long total = totalNanos.get();
        double avgNanos = (double) total / (double) c;
        return avgNanos / (double) unit.toNanos(1);
    }
}
