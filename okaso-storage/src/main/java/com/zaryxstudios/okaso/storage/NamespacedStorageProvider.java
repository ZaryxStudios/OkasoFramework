package com.zaryxstudios.okaso.storage;

import com.zaryxstudios.okaso.common.storage.StorageProvider;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class NamespacedStorageProvider implements StorageProvider {

    private final StorageProvider delegate;
    private final String namespace;
    private final String prefix;

    public NamespacedStorageProvider(StorageProvider delegate, String namespace, String separator) {
        if (delegate == null) throw new IllegalArgumentException("delegate must not be null");
        if (namespace == null || namespace.trim().isEmpty()) {
            throw new IllegalArgumentException("namespace must not be null or empty");
        }
        this.delegate = delegate;
        this.namespace = namespace.trim();
        this.prefix = this.namespace + normalize(separator);
    }

    private static String normalize(String separator) {
        return separator == null || separator.isEmpty() ? "." : separator;
    }

    public String getNamespace() {
        return namespace;
    }

    public StorageProvider getDelegate() {
        return delegate;
    }

    @Override
    public String getScope() {
        return namespace;
    }

    @Override
    public boolean isPersistent() {
        return delegate.isPersistent();
    }

    @Override
    public void store(String key, Object value) {
        delegate.store(resolve(key), value);
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        return delegate.get(resolve(key), type);
    }

    @Override
    public boolean has(String key) {
        return delegate.has(resolve(key));
    }

    @Override
    public void delete(String key) {
        delegate.delete(resolve(key));
    }

    @Override
    public Map<String, Object> getAll() {
        Map<String, Object> scoped = new LinkedHashMap<>();
        for (String key : delegate.keys()) {
            String stripped = strip(key);
            if (stripped != null) {
                scoped.put(stripped, delegate.get(key, Object.class).orElse(null));
            }
        }
        return Collections.unmodifiableMap(scoped);
    }

    @Override
    public void clear() {
        for (String key : delegate.keys()) {
            if (key.startsWith(prefix)) {
                delegate.delete(key);
            }
        }
    }

    @Override
    public Set<String> keys() {
        Set<String> scoped = new LinkedHashSet<>();
        for (String key : delegate.keys()) {
            String stripped = strip(key);
            if (stripped != null) {
                scoped.add(stripped);
            }
        }
        return Collections.unmodifiableSet(scoped);
    }

    @Override
    public int size() {
        return keys().size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public void flush() {
        delegate.flush();
    }

    private String resolve(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("key must not be null or empty");
        }
        return prefix + key;
    }

    private String strip(String key) {
        if (key == null || !key.startsWith(prefix)) return null;
        return key.substring(prefix.length());
    }
}
