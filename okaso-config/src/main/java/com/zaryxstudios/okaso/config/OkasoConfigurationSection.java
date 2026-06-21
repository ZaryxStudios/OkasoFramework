package com.zaryxstudios.okaso.config;

import com.zaryxstudios.okaso.common.config.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OkasoConfigurationSection implements ConfigurationSection {

    private final Map<String, Object> data;

    public OkasoConfigurationSection() {
        this.data = new LinkedHashMap<String, Object>();
    }

    public OkasoConfigurationSection(Map<String, Object> data) {
        this.data = data != null ? data : new LinkedHashMap<String, Object>();
    }

    public Map<String, Object> getRaw() {
        return data;
    }

    @Override
    public String getString(String path) {
        return getString(path, null);
    }

    @Override
    public String getString(String path, String def) {
        Object value = resolve(path);
        if (value instanceof String) return (String) value;
        return def;
    }

    @Override
    public int getInt(String path) {
        return getInt(path, 0);
    }

    @Override
    public int getInt(String path, int def) {
        Object value = resolve(path);
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            try { return Integer.parseInt((String) value); } catch (NumberFormatException ignored) {}
        }
        return def;
    }

    @Override
    public double getDouble(String path) {
        return getDouble(path, 0.0D);
    }

    @Override
    public double getDouble(String path, double def) {
        Object value = resolve(path);
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) {
            try { return Double.parseDouble((String) value); } catch (NumberFormatException ignored) {}
        }
        return def;
    }

    @Override
    public long getLong(String path) {
        return getLong(path, 0L);
    }

    @Override
    public long getLong(String path, long def) {
        Object value = resolve(path);
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof String) {
            try { return Long.parseLong((String) value); } catch (NumberFormatException ignored) {}
        }
        return def;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        Object value = resolve(path);
        if (value instanceof Boolean) return (Boolean) value;
        return def;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getStringList(String path) {
        Object value = resolve(path);
        if (value instanceof List) {
            List<?> raw = (List<?>) value;
            List<String> result = new ArrayList<String>();
            for (Object item : raw) {
                if (item != null) result.add(item.toString());
            }
            return result;
        }
        return Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Integer> getIntegerList(String path) {
        Object value = resolve(path);
        if (value instanceof List) {
            List<?> raw = (List<?>) value;
            List<Integer> result = new ArrayList<Integer>();
            for (Object item : raw) {
                if (item instanceof Number) result.add(((Number) item).intValue());
            }
            return result;
        }
        return Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConfigurationSection getSection(String path) {
        Object value = resolve(path);
        if (value instanceof Map) {
            return new OkasoConfigurationSection((Map<String, Object>) value);
        }
        return new OkasoConfigurationSection();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getKeys(boolean deep) {
        if (!deep) return data.keySet();
        Set<String> keys = new LinkedHashSet<String>();
        collectKeys(data, "", keys);
        return keys;
    }

    @Override
    public void set(String path, Object value) {
        if (path == null || path.isEmpty()) return;
        String[] parts = path.split("\\.");
        Map<String, Object> current = data;

        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            Object next = current.get(part);
            if (next instanceof Map) {
                current = (Map<String, Object>) next;
            } else {
                Map<String, Object> nested = new LinkedHashMap<String, Object>();
                current.put(part, nested);
                current = nested;
            }
        }

        String last = parts[parts.length - 1];
        if (value == null) {
            current.remove(last);
        } else {
            current.put(last, value);
        }
    }

    @Override
    public boolean contains(String path) {
        return resolve(path) != null;
    }

    @SuppressWarnings("unchecked")
    private Object resolve(String path) {
        if (path == null || path.isEmpty()) return null;
        String[] parts = path.split("\\.");
        Map<String, Object> current = data;

        for (int i = 0; i < parts.length - 1; i++) {
            Object next = current.get(parts[i]);
            if (next instanceof Map) {
                current = (Map<String, Object>) next;
            } else {
                return null;
            }
        }

        return current.get(parts[parts.length - 1]);
    }

    @SuppressWarnings("unchecked")
    private void collectKeys(Map<String, Object> map, String prefix, Set<String> keys) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String fullKey = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            keys.add(fullKey);
            if (entry.getValue() instanceof Map) {
                collectKeys((Map<String, Object>) entry.getValue(), fullKey, keys);
            }
        }
    }
}
