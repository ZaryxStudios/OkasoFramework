package com.zaryxstudios.okaso.common.metrics;

import java.util.function.Supplier;

public class Gauge {
    private final String name;
    private final Supplier<Number> supplier;

    public Gauge(String name, Supplier<Number> supplier) {
        this.name = name;
        this.supplier = supplier;
    }

    public String getName() { return name; }
    public Number getValue() { return supplier.get(); }
}
