package com.zaryxstudios.okaso.test;

import com.zaryxstudios.okaso.common.service.ServiceRegistry;

import java.util.Map;

public class MockServiceContainer extends ServiceRegistry {

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void registerAll(Map<Class<?>, Object> services) {
        for (Map.Entry<Class<?>, Object> entry : services.entrySet()) {
            Class rawType = entry.getKey();
            registerOrReplace(rawType, entry.getValue());
        }
    }

    public int size() {
        return getAll().size();
    }

    public void clear() {
        for (Class<?> type : getAll().keySet()) {
            unregister(type);
        }
    }
}
