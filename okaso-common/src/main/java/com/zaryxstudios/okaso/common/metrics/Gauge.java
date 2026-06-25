package com.zaryxstudios.okaso.common.metrics;

import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.Getter;

public class Gauge {
    @Getter
    private final String name;
    @Getter(AccessLevel.NONE)
    private final Supplier<Number> supplier;

    public Gauge(String name, Supplier<Number> supplier) {
        this.name = name;
        this.supplier = supplier;
    }

    public Number getValue() { return supplier.get(); }
}
