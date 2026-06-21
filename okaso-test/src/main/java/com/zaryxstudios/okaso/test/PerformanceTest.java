package com.zaryxstudios.okaso.test;

import java.util.ArrayList;
import java.util.List;

public final class PerformanceTest {

    private PerformanceTest() {
    }

    public static BenchmarkResult benchmark(Runnable runnable, int iterations, int warmup) {
        for (int i = 0; i < warmup; i++) {
            runnable.run();
        }

        List<Long> samples = new ArrayList<Long>(iterations);
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            runnable.run();
            long elapsed = System.nanoTime() - start;
            samples.add(elapsed);
        }

        return new BenchmarkResult(samples);
    }

    public static BenchmarkResult benchmark(Runnable runnable) {
        return benchmark(runnable, 100, 10);
    }

    public static final class BenchmarkResult {
        private final List<Long> samples;
        private final long min;
        private final long max;
        private final double avg;
        private final double median;
        private final double opsPerSecond;

        BenchmarkResult(List<Long> samples) {
            this.samples = new ArrayList<Long>(samples);

            long sum = 0;
            long minVal = Long.MAX_VALUE;
            long maxVal = Long.MIN_VALUE;

            for (long s : samples) {
                sum += s;
                if (s < minVal) minVal = s;
                if (s > maxVal) maxVal = s;
            }

            this.min = minVal;
            this.max = maxVal;
            this.avg = samples.isEmpty() ? 0.0 : (double) sum / samples.size();

            List<Long> sorted = new ArrayList<Long>(samples);
            java.util.Collections.sort(sorted);
            if (sorted.isEmpty()) {
                this.median = 0.0;
            } else if (sorted.size() % 2 == 1) {
                this.median = sorted.get(sorted.size() / 2);
            } else {
                int mid = sorted.size() / 2;
                this.median = (sorted.get(mid - 1) + sorted.get(mid)) / 2.0;
            }

            this.opsPerSecond = avg > 0 ? 1_000_000_000.0 / avg : 0.0;
        }

        public List<Long> getSamples() {
            return new ArrayList<Long>(samples);
        }

        public long getMin() {
            return min;
        }

        public long getMax() {
            return max;
        }

        public double getAvg() {
            return avg;
        }

        public double getMedian() {
            return median;
        }

        public double getOpsPerSecond() {
            return opsPerSecond;
        }

        public String report() {
            StringBuilder sb = new StringBuilder();
            sb.append("Benchmark Report (").append(samples.size()).append(" samples)\n");
            sb.append(String.format("  Min:    %.3f \u00b5s%n", min / 1000.0));
            sb.append(String.format("  Max:    %.3f \u00b5s%n", max / 1000.0));
            sb.append(String.format("  Avg:    %.3f \u00b5s%n", avg / 1000.0));
            sb.append(String.format("  Median: %.3f \u00b5s%n", median / 1000.0));
            sb.append(String.format("  Ops/s:  %.0f%n", opsPerSecond));
            return sb.toString();
        }

        @Override
        public String toString() {
            return String.format("BenchmarkResult{avg=%.3f\u00b5s, ops/s=%.0f}", avg / 1000.0, opsPerSecond);
        }
    }
}
