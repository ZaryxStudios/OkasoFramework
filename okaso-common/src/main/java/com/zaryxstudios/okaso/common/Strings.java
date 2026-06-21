package com.zaryxstudios.okaso.common;

import java.util.ArrayList;
import java.util.List;

public final class Strings {

    private Strings() {}

    public static String colorize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.replace('&', '\u00A7');
    }

    public static String stripColor(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.replaceAll("\u00A7[0-9a-fk-or]", "");
    }

    public static String repeat(String s, int count) {
        if (s == null || count <= 0) return "";
        StringBuilder sb = new StringBuilder(s.length() * count);
        for (int i = 0; i < count; i++) sb.append(s);
        return sb.toString();
    }

    public static String padLeft(String s, int len, char padChar) {
        if (s == null) s = "";
        if (s.length() >= len) return s;
        return repeat(String.valueOf(padChar), len - s.length()) + s;
    }

    public static String padRight(String s, int len, char padChar) {
        if (s == null) s = "";
        if (s.length() >= len) return s;
        return s + repeat(String.valueOf(padChar), len - s.length());
    }

    public static String toJsonText(String text) {
        return "{\"text\":\"" + text.replace("\"", "\\\"") + "\"}";
    }

    public static List<String> wrap(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) { lines.add(""); return lines; }
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxWidth, text.length());
            lines.add(text.substring(start, end));
            start = end;
        }
        return lines;
    }

    public static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String truncate(String s, int maxLen) {
        if (s == null || s.length() <= maxLen) return s;
        return s.substring(0, Math.max(0, maxLen - 1)) + "\u2026";
    }
}
