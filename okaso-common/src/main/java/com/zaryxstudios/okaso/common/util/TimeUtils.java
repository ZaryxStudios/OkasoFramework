package com.zaryxstudios.okaso.common.util;

import java.util.concurrent.TimeUnit;

public final class TimeUtils {

    private TimeUtils() {}

    public static String formatDuration(long millis) {
        if (millis < 1000) return millis + "ms";
        long seconds = millis / 1000;
        if (seconds < 60) return seconds + "s";
        long minutes = seconds / 60;
        seconds = seconds % 60;
        if (minutes < 60) return minutes + "m " + seconds + "s";
        long hours = minutes / 60;
        minutes = minutes % 60;
        return hours + "h " + minutes + "m " + seconds + "s";
    }

    public static long ticksToMillis(long ticks) {
        return ticks * 50L;
    }

    public static long millisToTicks(long millis) {
        return millis / 50L;
    }

    public static long now() {
        return System.currentTimeMillis();
    }
}
