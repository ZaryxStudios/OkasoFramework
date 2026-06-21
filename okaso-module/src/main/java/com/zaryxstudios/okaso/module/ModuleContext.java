package com.zaryxstudios.okaso.module;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ModuleContext {

    private final String name;
    private final Map<String, Object> data;

    public ModuleContext(String name) {
        this.name = name;
        this.data = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void set(String key, Object value) {
        data.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    public Map<String, Object> getData() {
        return Collections.unmodifiableMap(data);
    }
}
