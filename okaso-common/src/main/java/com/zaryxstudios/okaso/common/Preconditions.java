package com.zaryxstudios.okaso.common;

public final class Preconditions {

    private Preconditions() {}

    public static <T> T requireNonNull(T obj, String message) {
        if (obj == null) throw new IllegalArgumentException(message);
        return obj;
    }

    public static String requireNonEmpty(String value, String message) {
        if (value == null || value.isEmpty()) throw new IllegalArgumentException(message);
        return value;
    }

    public static void requireState(boolean expression, String message) {
        if (!expression) throw new IllegalStateException(message);
    }

    public static int requirePositive(int value, String name) {
        if (value <= 0) throw new IllegalArgumentException(name + " must be positive, got: " + value);
        return value;
    }

    public static long requirePositive(long value, String name) {
        if (value <= 0) throw new IllegalArgumentException(name + " must be positive, got: " + value);
        return value;
    }

    public static int requireNonNegative(int value, String name) {
        if (value < 0) throw new IllegalArgumentException(name + " must be >= 0, got: " + value);
        return value;
    }
}
