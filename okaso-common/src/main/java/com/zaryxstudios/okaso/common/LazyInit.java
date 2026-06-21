package com.zaryxstudios.okaso.common;

import java.util.Objects;
import java.util.function.Supplier;

public class LazyInit<T> {

    private volatile T value;
    private final Supplier<T> supplier;

    public LazyInit(Supplier<T> supplier) {
        this.supplier = Objects.requireNonNull(supplier, "supplier");
    }

    public static <T> LazyInit<T> of(Supplier<T> supplier) {
        return new LazyInit<>(supplier);
    }

    public T get() {
        T v = value;
        if (v == null) {
            synchronized (this) {
                v = value;
                if (v == null) {
                    v = supplier.get();
                    value = v;
                }
            }
        }
        return v;
    }

    public boolean isInitialized() {
        return value != null;
    }

    public synchronized void reset() {
        value = null;
    }
}
