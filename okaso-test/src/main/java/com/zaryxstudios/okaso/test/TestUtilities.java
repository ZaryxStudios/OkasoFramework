package com.zaryxstudios.okaso.test;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class TestUtilities {

    private TestUtilities() {
    }

    public static boolean await(CountDownLatch latch, long timeout, TimeUnit unit) {
        try {
            return latch.await(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for latch");
        }
    }

    public static CountDownLatch newLatch(int count) {
        return new CountDownLatch(count);
    }

    @SafeVarargs
    public static <T> boolean containsExactly(Collection<T> collection, T... items) {
        if (collection.size() != items.length) {
            return false;
        }
        for (T item : items) {
            if (!collection.contains(item)) {
                return false;
            }
        }
        return true;
    }

    @SafeVarargs
    public static <T> boolean containsNone(Collection<T> collection, T... items) {
        for (T item : items) {
            if (collection.contains(item)) {
                return false;
            }
        }
        return true;
    }

    public static String repeat(int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append('-');
        }
        return sb.toString();
    }

    public static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static long time(Runnable runnable) {
        long start = System.nanoTime();
        runnable.run();
        return (System.nanoTime() - start) / 1_000_000L;
    }
}
