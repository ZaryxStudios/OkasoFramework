package com.zaryxstudios.okaso.common.metrics;

public interface Counter {
    void increment();
    void increment(long amount);
    void decrement();
    void decrement(long amount);
    long getCount();
    void reset();
}
