package com.zaryxstudios.okaso.common.storage;

import java.util.Map;
import java.util.Optional;

public interface StorageProvider {
    void store(String key, Object value);
    <T> Optional<T> get(String key, Class<T> type);
    boolean has(String key);
    void delete(String key);
    Map<String, Object> getAll();
    void clear();
    java.util.Set<String> keys();
    int size();
    boolean isEmpty();
}
