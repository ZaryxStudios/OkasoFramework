package com.zaryxstudios.okaso.metrics;

import com.zaryxstudios.okaso.common.metrics.Counter;

import java.util.concurrent.atomic.AtomicLong;

public class SimpleCounter implements Counter {

    private final AtomicLong count;
    private final String name;

    public SimpleCounter(String name) {
        this.name = name;
        this.count = new AtomicLong(0);
    }

    public String getName() {
        return name;
    }

    @Override
    public void increment() {
        count.incrementAndGet();
    }

    @Override
    public void increment(long amount) {
        count.addAndGet(amount);
    }

    @Override
    public void decrement() {
        count.decrementAndGet();
    }

    @Override
    public void decrement(long amount) {
        count.addAndGet(-amount);
    }

    @Override
    public long getCount() {
        return count.get();
    }

    @Override
    public void reset() {
        count.set(0);
    }
}
