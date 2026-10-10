package com.zaryxstudios.okaso.common.storage;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    default void storeIfAbsent(String key, Object value) {
        if (!has(key)) {
            store(key, value);
        }
    }

    default void storeAll(Map<String, Object> entries) {
        if (entries != null) {
            entries.forEach(this::store);
        }
    }

    default void deleteAll(Set<String> keys) {
        if (keys != null) {
            keys.forEach(this::delete);
        }
    }

    default <T> Optional<T> getAndConvert(String key, Class<T> type, Function<Object, T> converter) {
        return get(key, Object.class).map(obj -> {
            if (type.isInstance(obj)) {
                return type.cast(obj);
            }
            return converter.apply(obj);
        });
    }

    default <T> T getOrCompute(String key, Class<T> type, Function<String, T> computeFunction) {
        Optional<T> existing = get(key, type);
        if (existing.isPresent()) {
            return existing.get();
        }
        T computed = computeFunction.apply(key);
        if (computed != null) {
            store(key, computed);
        }
        return computed;
    }

    default void update(String key, Function<Optional<Object>, Object> updateFunction) {
        Object newValue = updateFunction.apply(get(key));
        if (newValue != null) {
            store(key, newValue);
        } else {
            delete(key);
        }
    }

    default boolean renameKey(String oldKey, String newKey) {
        if (oldKey == null || newKey == null || oldKey.equals(newKey)) return false;
        Optional<Object> value = get(oldKey, Object.class);
        if (!value.isPresent()) return false;
        store(newKey, value.get());
        delete(oldKey);
        return true;
    }

    default void copyTo(StorageProvider target) {
        getAll().forEach(target::store);
    }

    default void copyFrom(StorageProvider source) {
        source.getAll().forEach(this::store);
    }

    default Map<String, Object> getAllWithPrefix(String prefix) {
        return keys().stream()
            .filter(k -> k.startsWith(prefix))
            .collect(Collectors.toMap(
                k -> k.substring(prefix.length()),
                k -> get(k, Object.class).orElse(null)
            ));
    }

    default void deleteWithPrefix(String prefix) {
        keys().stream()
            .filter(k -> k.startsWith(prefix))
            .forEach(this::delete);
    }

    default List<String> keysWithPrefix(String prefix) {
        return keys().stream()
            .filter(k -> k.startsWith(prefix))
            .collect(Collectors.toList());
    }

    default long countWithPrefix(String prefix) {
        return keys().stream().filter(k -> k.startsWith(prefix)).count();
    }
}
