package com.zaryxstudios.okaso.metrics;

import com.zaryxstudios.okaso.common.metrics.Histogram;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import lombok.Getter;

public class SimpleHistogram implements Histogram {

    @Getter
    private final String name;
    private final List<Long> values;
    private final ReentrantReadWriteLock lock;

    public SimpleHistogram(String name) {
        this.name = name;
        this.values = new ArrayList<Long>();
        this.lock = new ReentrantReadWriteLock();
    }

    @Override
    public void record(long value) {
        lock.writeLock().lock();
        try {
            values.add(value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public long getCount() {
        lock.readLock().lock();
        try {
            return values.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public double getMin() {
        lock.readLock().lock();
        try {
            if (values.isEmpty()) return 0.0D;
            long min = Long.MAX_VALUE;
            for (long v : values) {
                if (v < min) min = v;
            }
            return (double) min;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public double getMax() {
        lock.readLock().lock();
        try {
            if (values.isEmpty()) return 0.0D;
            long max = Long.MIN_VALUE;
            for (long v : values) {
                if (v > max) max = v;
            }
            return (double) max;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public double getMean() {
        lock.readLock().lock();
        try {
            if (values.isEmpty()) return 0.0D;
            long sum = 0;
            for (long v : values) {
                sum += v;
            }
            return (double) sum / (double) values.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public double getPercentile(double percentile) {
        lock.readLock().lock();
        try {
            if (values.isEmpty()) return 0.0D;
            List<Long> sorted = new ArrayList<Long>(values);
            Collections.sort(sorted);
            int index = (int) Math.ceil(percentile / 100.0D * sorted.size()) - 1;
            if (index < 0) index = 0;
            if (index >= sorted.size()) index = sorted.size() - 1;
            return (double) sorted.get(index);
        } finally {
            lock.readLock().unlock();
        }
    }
}
