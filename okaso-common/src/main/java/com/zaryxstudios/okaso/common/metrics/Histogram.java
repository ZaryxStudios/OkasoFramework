package com.zaryxstudios.okaso.common.metrics;

public interface Histogram {
    void record(long value);
    long getCount();
    double getMin();
    double getMax();
    double getMean();
    double getPercentile(double percentile);
}
