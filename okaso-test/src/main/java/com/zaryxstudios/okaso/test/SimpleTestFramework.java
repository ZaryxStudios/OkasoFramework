package com.zaryxstudios.okaso.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleTestFramework {

    private final List<TestResult> results = new ArrayList<TestResult>();
    private final AtomicInteger passed = new AtomicInteger(0);
    private final AtomicInteger failed = new AtomicInteger(0);

    public void assertTrue(boolean condition) {
        assertTrue(condition, "Assertion failed");
    }

    public void assertTrue(boolean condition, String message) {
        if (condition) {
            passed.incrementAndGet();
        } else {
            fail("AssertTrue", message);
        }
    }

    public void assertFalse(boolean condition) {
        assertFalse(condition, "Expected false");
    }

    public void assertFalse(boolean condition, String message) {
        if (!condition) {
            passed.incrementAndGet();
        } else {
            fail("AssertFalse", message);
        }
    }

    public void assertEquals(Object expected, Object actual) {
        assertEquals(expected, actual, "Objects not equal");
    }

    public void assertEquals(Object expected, Object actual, String message) {
        if (expected == null) {
            if (actual == null) {
                passed.incrementAndGet();
            } else {
                fail("Equals", message + " (expected null, got " + actual + ")");
            }
        } else if (expected.equals(actual)) {
            passed.incrementAndGet();
        } else {
            fail("Equals", message + " - expected: " + expected + ", actual: " + actual);
        }
    }

    public void assertNull(Object object) {
        assertNull(object, "Expected null");
    }

    public void assertNull(Object object, String message) {
        if (object == null) {
            passed.incrementAndGet();
        } else {
            fail("Null", message + " (got " + object + ")");
        }
    }

    public void assertNotNull(Object object) {
        assertNotNull(object, "Expected non-null");
    }

    public void assertNotNull(Object object, String message) {
        if (object != null) {
            passed.incrementAndGet();
        } else {
            fail("NotNull", message);
        }
    }

    public void assertThrows(Class<? extends Exception> expectedType, Runnable runnable) {
        assertThrows(expectedType, runnable, "Expected " + expectedType.getSimpleName());
    }

    public void assertThrows(Class<? extends Exception> expectedType, Runnable runnable, String message) {
        try {
            runnable.run();
            fail("Exception", message + " — no exception was thrown");
        } catch (Exception e) {
            if (expectedType.isInstance(e)) {
                passed.incrementAndGet();
            } else {
                fail("Exception", message + " — expected " + expectedType.getSimpleName()
                    + " but got " + e.getClass().getName() + ": " + e.getMessage());
            }
        }
    }

    public int getPassed() {
        return passed.get();
    }

    public int getFailed() {
        return failed.get();
    }

    public List<TestResult> getResults() {
        return new ArrayList<TestResult>(results);
    }

    public void printSummary() {
        int total = passed.get() + failed.get();
        System.out.println();
        System.out.println("=== Test Summary ===");
        System.out.println("Total:  " + total);
        System.out.println("Passed: " + passed.get() + " \u2713");
        System.out.println("Failed: " + failed.get() + " \u2717");
        System.out.println("====================");
    }

    public boolean isAllPassed() {
        return failed.get() == 0;
    }

    private void fail(String type, String message) {
        failed.incrementAndGet();
        results.add(new TestResult(type, false, message));
        System.err.println("\u274C FAILED [" + type + "]: " + message);
    }

    public static final class TestResult {
        private final String type;
        private final boolean passed;
        private final String message;

        public TestResult(String type, boolean passed, String message) {
            this.type = type;
            this.passed = passed;
            this.message = message;
        }

        public String getType() {
            return type;
        }

        public boolean isPassed() {
            return passed;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return (passed ? "\u2713" : "\u2717") + " [" + type + "] " + message;
        }
    }
}
