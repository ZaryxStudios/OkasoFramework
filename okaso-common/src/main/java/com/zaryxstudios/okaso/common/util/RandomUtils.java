package com.zaryxstudios.okaso.common.util;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomUtils {

    private RandomUtils() {}

    private static Random random() {
        return ThreadLocalRandom.current();
    }

    public static int nextInt(int bound) {
        return random().nextInt(bound);
    }

    public static int nextInt(int min, int max) {
        return min + random().nextInt(max - min + 1);
    }

    public static double nextDouble() {
        return random().nextDouble();
    }

    public static <T> T pick(List<T> list) {
        if (list == null || list.isEmpty()) return null;
        return list.get(nextInt(list.size()));
    }

    @SafeVarargs
    public static <T> T pick(T... array) {
        if (array == null || array.length == 0) return null;
        return array[nextInt(array.length)];
    }

    public static boolean chance(double probability) {
        return random().nextDouble() < probability;
    }
}
