package com.zaryxstudios.okaso.storage;

import com.zaryxstudios.okaso.common.storage.StorageMessages;
import com.zaryxstudios.okaso.common.storage.StorageProvider;
import com.zaryxstudios.okaso.common.storage.StorageSettings;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonFileStorageProvider implements StorageProvider {

    private static final Logger LOGGER = Logger.getLogger(JsonFileStorageProvider.class.getName());
    private static final String TEMP_SUFFIX = ".tmp";

    private final File file;
    private final ObjectMapper mapper;
    private final ReentrantReadWriteLock lock;
    private final Map<String, Object> data;
    private final StorageSettings settings;
    private final AtomicBoolean dirty;
    private final AtomicBoolean closed;
    private final ScheduledExecutorService scheduler;

    public JsonFileStorageProvider(File file) {
        this(file, StorageSettings.defaults());
    }

    public JsonFileStorageProvider(File file, StorageSettings settings) {
        this.file = file;
        this.settings = settings != null ? settings : StorageSettings.defaults();
        this.mapper = new ObjectMapper();
        if (this.settings.isPrettyPrint()) {
            this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
        this.lock = new ReentrantReadWriteLock();
        this.data = new ConcurrentHashMap<>();
        this.dirty = new AtomicBoolean(false);
        this.closed = new AtomicBoolean(false);
        loadFromFile();
        this.scheduler = startScheduler();
    }

    public File getFile() {
        return file;
    }

    @Override
    public String getScope() {
        return file != null ? file.getName() : "unbound";
    }

    @Override
    public void store(String key, Object value) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("key must not be null or empty");
        }
        lock.writeLock().lock();
        try {
            data.put(key, value);
            if (settings.isAutoSave()) {
                dirty.set(true);
            } else {
                saveToFile();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        if (type == null) return Optional.empty();
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
            if (data.remove(key) == null) return;
            markDirtyOrSave();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Map<String, Object> getAll() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableMap(new LinkedHashMap<>(data));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            if (data.isEmpty()) return;
            data.clear();
            markDirtyOrSave();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Set<String> keys() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableSet(new LinkedHashMap<>(data).keySet());
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

    @Override
    public void flush() {
        lock.readLock().lock();
        try {
            if (!dirty.get()) return;
        } finally {
            lock.readLock().unlock();
        }
        lock.writeLock().lock();
        try {
            if (dirty.compareAndSet(true, false)) {
                saveToFile();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) return;
        if (scheduler != null) {
            scheduler.shutdownNow();
            try {
                scheduler.awaitTermination(2L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        flush();
    }

    private void markDirtyOrSave() {
        if (settings.isAutoSave()) {
            dirty.set(true);
        } else {
            saveToFile();
        }
    }

    private void loadFromFile() {
        if (file == null || !file.exists()) return;
        lock.writeLock().lock();
        try {
            Map<String, Object> loaded = mapper.readValue(file, new TypeReference<Map<String, Object>>() {});
            if (loaded != null) {
                data.putAll(loaded);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, StorageMessages.get(StorageMessages.LOAD_FAILED,
                file.getAbsolutePath(), String.valueOf(e.getMessage())));
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void saveToFile() {
        if (file == null) return;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.exists()) {
            LOGGER.log(Level.SEVERE, StorageMessages.get(StorageMessages.SAVE_FAILED,
                file.getAbsolutePath(), "parent directory could not be created"));
            return;
        }
        File temp = new File(file.getAbsolutePath() + TEMP_SUFFIX);
        try {
            mapper.writeValue(temp, data);
            move(temp, file);
            dirty.set(false);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, StorageMessages.get(StorageMessages.SAVE_FAILED,
                file.getAbsolutePath(), String.valueOf(e.getMessage())));
        }
    }

    private static void move(File source, File target) throws IOException {
        try {
            Files.move(source.toPath(), target.toPath(),
                StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private ScheduledExecutorService startScheduler() {
        long interval = settings.getSaveIntervalMillis();
        if (!settings.isAutoSave() || interval <= 0L) return null;
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    Thread thread = new Thread(runnable, "Okaso-Storage-Flush");
                    thread.setDaemon(true);
                    return thread;
                }
            });
        service.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    flush();
                } catch (RuntimeException e) {
                    LOGGER.log(Level.WARNING, StorageMessages.get(StorageMessages.SAVE_FAILED,
                        file != null ? file.getAbsolutePath() : "unknown",
                        String.valueOf(e.getMessage())));
                }
            }
        }, interval, interval, TimeUnit.MILLISECONDS);
        return service;
    }
}
