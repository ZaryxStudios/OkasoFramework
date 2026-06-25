package com.zaryxstudios.okaso.metrics;

import com.zaryxstudios.okaso.common.metrics.Counter;
import com.zaryxstudios.okaso.common.metrics.Gauge;
import com.zaryxstudios.okaso.common.metrics.Histogram;
import com.zaryxstudios.okaso.common.metrics.MetricsRegistry;
import com.zaryxstudios.okaso.common.metrics.Timer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;

public class SimpleMetricsRegistry implements MetricsRegistry {

    @Getter
    private final Map<String, Counter> counters;
    @Getter
    private final Map<String, Gauge> gauges;
    @Getter
    private final Map<String, Timer> timers;
    @Getter
    private final Map<String, Histogram> histograms;

    public SimpleMetricsRegistry() {
        this.counters = new ConcurrentHashMap<String, Counter>();
        this.gauges = new ConcurrentHashMap<String, Gauge>();
        this.timers = new ConcurrentHashMap<String, Timer>();
        this.histograms = new ConcurrentHashMap<String, Histogram>();
    }

    @Override
    public Counter createCounter(String name) {
        Counter existing = counters.get(name);
        if (existing != null) return existing;
        Counter counter = new SimpleCounter(name);
        counters.put(name, counter);
        return counter;
    }

    @Override
    public Gauge registerGauge(String name, Gauge gauge) {
        gauges.put(name, gauge);
        return gauge;
    }

    @Override
    public Timer createTimer(String name) {
        Timer existing = timers.get(name);
        if (existing != null) return existing;
        Timer timer = new SimpleTimer(name);
        timers.put(name, timer);
        return timer;
    }

    @Override
    public Histogram createHistogram(String name) {
        Histogram existing = histograms.get(name);
        if (existing != null) return existing;
        Histogram histogram = new SimpleHistogram(name);
        histograms.put(name, histogram);
        return histogram;
    }

}
