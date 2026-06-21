package com.zaryxstudios.okaso.test;

import java.util.function.Supplier;

public final class AssertUtils {

    private AssertUtils() {
    }

    public static void assertTrueEventually(Supplier<Boolean> condition, long timeoutMs)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (condition.get()) {
                return;
            }
            Thread.sleep(10);
        }
        throw new AssertionError("Condition not met within " + timeoutMs + " ms");
    }

    public static void assertArrayEquals(int[] expected, int[] actual, String message) {
        if (expected == actual) return;
        if (expected == null || actual == null) {
            throw new AssertionError(message + ": null mismatch");
        }
        if (expected.length != actual.length) {
            throw new AssertionError(message + ": lengths differ ("
                + expected.length + " vs " + actual.length + ")");
        }
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                throw new AssertionError(message + ": differ at index " + i);
            }
        }
    }

    public static void assertInRange(int value, int min, int max, String message) {
        if (value < min || value > max) {
            throw new AssertionError(message + ": " + value + " not in [" + min + ", " + max + "]");
        }
    }

    public static void assertInRange(long value, long min, long max, String message) {
        if (value < min || value > max) {
            throw new AssertionError(message + ": " + value + " not in [" + min + ", " + max + "]");
        }
    }

    public static void assertNotEquals(Object unexpected, Object actual, String message) {
        if (unexpected == null && actual == null) {
            throw new AssertionError(message + ": both are null");
        }
        if (unexpected != null && unexpected.equals(actual)) {
            throw new AssertionError(message + ": objects are equal when they should not be");
        }
    }

    public static void assertContains(String haystack, String needle, String message) {
        if (haystack == null || !haystack.contains(needle)) {
            throw new AssertionError(message + ": \"" + haystack + "\" does not contain \"" + needle + "\"");
        }
    }

    public static void assertStartsWith(String text, String prefix, String message) {
        if (text == null || !text.startsWith(prefix)) {
            throw new AssertionError(message + ": \"" + text + "\" does not start with \"" + prefix + "\"");
        }
    }

    public static void assertEndsWith(String text, String suffix, String message) {
        if (text == null || !text.endsWith(suffix)) {
            throw new AssertionError(message + ": \"" + text + "\" does not end with \"" + suffix + "\"");
        }
    }

    public static void assertEmpty(java.util.Collection<?> collection, String message) {
        if (collection != null && !collection.isEmpty()) {
            throw new AssertionError(message + ": expected empty, size=" + collection.size());
        }
    }

    public static void assertNotEmpty(java.util.Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new AssertionError(message + ": expected non-empty");
        }
    }

    public static void assertGreaterThan(int actual, int expected, String message) {
        if (actual <= expected) {
            throw new AssertionError(message + ": " + actual + " <= " + expected);
        }
    }

    public static void assertLessThan(int actual, int expected, String message) {
        if (actual >= expected) {
            throw new AssertionError(message + ": " + actual + " >= " + expected);
        }
    }
}
