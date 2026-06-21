package com.zaryxstudios.okaso.storage;

import com.zaryxstudios.okaso.common.storage.StorageProvider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class JsonFileStorageProvider implements StorageProvider {

    private final File file;
    private final ObjectMapper mapper;
    private final ReentrantReadWriteLock lock;
    private final Map<String, Object> data;

    public JsonFileStorageProvider(File file) {
        this.file = file;
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.lock = new ReentrantReadWriteLock();
        this.data = new ConcurrentHashMap<String, Object>();
        loadFromFile();
    }

    @Override
    public void store(String key, Object value) {
        if (key == null) throw new IllegalArgumentException("key must not be null");
        lock.writeLock().lock();
        try {
            data.put(key, value);
            saveToFile();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        lock.readLock().lock();
        try {
            Object value = data.get(key);
            if (value == null) return Optional.empty();
            if (type.isInstance(value)) {
                return Optional.of((T) value);
            }
            return Optional.empty();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean has(String key) {
        lock.readLock().lock();
        try {
            return data.containsKey(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void delete(String key) {
        lock.writeLock().lock();
        try {
            data.remove(key);
            saveToFile();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Map<String, Object> getAll() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableMap(new LinkedHashMap<String, Object>(data));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            data.clear();
            saveToFile();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        if (file == null || !file.exists()) return;
        lock.writeLock().lock();
        try {
            Map<String, Object> loaded = mapper.readValue(
                file, new TypeReference<Map<String, Object>>() {});
            if (loaded != null) {
                data.putAll(loaded);
            }
        } catch (IOException e) {
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public java.util.Set<String> keys() {
        lock.readLock().lock();
        try {
            return java.util.Collections.unmodifiableSet(data.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int size() {
        lock.readLock().lock();
        try {
            return data.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return data.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

    private void saveToFile() {
        if (file == null) return;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try {
            mapper.writeValue(file, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
