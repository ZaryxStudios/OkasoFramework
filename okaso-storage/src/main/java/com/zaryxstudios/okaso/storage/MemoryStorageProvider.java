package com.zaryxstudios.okaso.storage;

import com.zaryxstudios.okaso.common.storage.StorageProvider;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryStorageProvider implements StorageProvider {

    private final Map<String, Object> storage;

    public MemoryStorageProvider() {
        this.storage = new ConcurrentHashMap<String, Object>();
    }

    @Override
    public void store(String key, Object value) {
        if (key == null) throw new IllegalArgumentException("key must not be null");
        storage.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = storage.get(key);
        if (value == null) return Optional.empty();
        if (type.isInstance(value)) {
            return Optional.of((T) value);
        }
        return Optional.empty();
    }

    @Override
    public boolean has(String key) {
        return storage.containsKey(key);
    }

    @Override
    public void delete(String key) {
        storage.remove(key);
    }

    @Override
    public Map<String, Object> getAll() {
        return Collections.unmodifiableMap(storage);
    }

    @Override
    public void clear() {
        storage.clear();
    }

    @Override
    public java.util.Set<String> keys() {
        return java.util.Collections.unmodifiableSet(storage.keySet());
    }

    @Override
    public int size() {
        return storage.size();
    }

    @Override
    public boolean isEmpty() {
        return storage.isEmpty();
    }
}
