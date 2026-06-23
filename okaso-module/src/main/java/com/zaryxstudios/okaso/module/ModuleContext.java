package com.zaryxstudios.okaso.module;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class ModuleContext {

    private final String name;
    @Getter(AccessLevel.NONE)
    private final Map<String, Object> data;

    public ModuleContext(String name) {
        this.name = name;
        this.data = new HashMap<>();
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
