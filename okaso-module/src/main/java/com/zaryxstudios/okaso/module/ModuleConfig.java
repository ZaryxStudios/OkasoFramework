package com.zaryxstudios.okaso.module;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

public class ModuleConfig {

    @Getter
    private final String moduleName;
    private final Map<String, String> values;

    public ModuleConfig(String moduleName) {
        this.moduleName = moduleName;
        this.values = new HashMap<>();
    }

    public void set(String key, String value) {
        values.put(key, value);
    }

    public String getString(String key) {
        return values.get(key);
    }

    public String getString(String key, String defaultValue) {
        return values.getOrDefault(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        String val = values.get(key);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String val = values.get(key);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val);
    }

    public Map<String, String> getAll() {
        return new HashMap<>(values);
    }

    public void putAll(Map<String, String> entries) {
        values.putAll(entries);
    }

    public void remove(String key) {
        values.remove(key);
    }

    public void clear() {
        values.clear();
    }
}
