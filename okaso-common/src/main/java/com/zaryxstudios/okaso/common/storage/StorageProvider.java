package com.zaryxstudios.okaso.common.storage;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface StorageProvider {

    void store(String key, Object value);

    <T> Optional<T> get(String key, Class<T> type);

    default Optional<Object> get(String key) {
        return get(key, Object.class);
    }

    default <T> T getOrDefault(String key, Class<T> type, T fallback) {
        Optional<T> value = get(key, type);
        return value.isPresent() ? value.get() : fallback;
    }

    boolean has(String key);

    void delete(String key);

    Map<String, Object> getAll();

    void clear();

    Set<String> keys();

    int size();

    boolean isEmpty();

    default String getScope() {
        return "default";
    }

    default boolean isPersistent() {
        return true;
    }

    default void flush() {
    }

    default void close() {
    }
}
