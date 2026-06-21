package com.zaryxstudios.okaso.common.metrics;

public interface MetricsRegistry {
    Counter createCounter(String name);
    Gauge registerGauge(String name, Gauge gauge);
    Timer createTimer(String name);
    Histogram createHistogram(String name);
}
