package com.zaryxstudios.okaso.common.message;

import com.zaryxstudios.okaso.common.text.TextColorizer;

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

    default void set(String key, String value) {
        throw new UnsupportedOperationException("This MessageProvider is immutable");
    }
}
