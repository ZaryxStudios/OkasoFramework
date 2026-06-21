package com.zaryxstudios.okaso.common.service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRegistry {

    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    public <T> void register(Class<T> type, T implementation) {
        if (services.putIfAbsent(type, implementation) != null) {
            throw new IllegalArgumentException("Service already registered: " + type.getName());
        }
    }

    public <T> void registerOrReplace(Class<T> type, T implementation) {
        services.put(type, implementation);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type) {
        return (T) services.get(type);
    }

    public boolean isRegistered(Class<?> type) {
        return services.containsKey(type);
    }

    public <T> void unregister(Class<T> type) {
        services.remove(type);
    }

    public Map<Class<?>, Object> getAll() {
        return Collections.unmodifiableMap(services);
    }
}
