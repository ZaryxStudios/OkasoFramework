package com.zaryxstudios.okaso.common.message;

import com.zaryxstudios.okaso.common.text.TextColorizer;

import java.util.Map;

@FunctionalInterface
public interface MessageProvider {

    String get(String key);

    default String format(String key, Object... args) {
        String msg = get(key);
        if (msg == null || msg.isEmpty()) return msg;
        for (int i = 0; i < args.length; i++) {
            String value = args[i] != null ? args[i].toString() : "null";
            msg = msg.replace("{" + i + "}", value);
        }
        return msg;
    }

    default String format(String key, Map<String, ?> placeholders, Object... args) {
        String msg = get(key);
        if (msg == null || msg.isEmpty()) return msg;
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
                    msg = msg.replace("{" + i + "}", args[i].toString());
                }
            }
        }
        if (placeholders != null) {
            for (Map.Entry<String, ?> entry : placeholders.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    msg = msg.replace("{" + entry.getKey() + "}", entry.getValue().toString());
                }
            }
        }
        return msg;
    }

    default void set(String key, String value) {
        throw new UnsupportedOperationException("This MessageProvider is immutable");
    }
}
