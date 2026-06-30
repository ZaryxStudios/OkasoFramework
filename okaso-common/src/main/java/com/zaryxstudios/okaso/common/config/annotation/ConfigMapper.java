package com.zaryxstudios.okaso.common.config.annotation;

import com.zaryxstudios.okaso.common.config.ConfigurationSection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class ConfigMapper {

    private ConfigMapper() {
    }

    public static <T> T mapFromSection(T instance, ConfigurationSection section) {
        if (instance == null || section == null) return instance;
        Class<?> clazz = instance.getClass();
        for (Field field : findAllFields(clazz)) {
            ConfigValue ann = field.getAnnotation(ConfigValue.class);
            if (ann == null) continue;
            String path = ann.path();
            if (!section.contains(path) && !ann.defaultValue().isEmpty()) {
                setField(instance, field, convertValue(ann.defaultValue(), field.getType()));
                continue;
            }
            Object value = resolveValue(section, path, field.getType());
            if (value != null) {
                setField(instance, field, value);
            }
        }
        return instance;
    }

    public static <T> T mapFromSection(Class<T> type, ConfigurationSection section) {
        try {
            return mapFromSection(type.newInstance(), section);
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate " + type.getName(), e);
        }
    }

    private static Object resolveValue(ConfigurationSection section, String path, Class<?> fieldType) {
        if (fieldType == String.class) return section.getString(path);
        if (fieldType == int.class || fieldType == Integer.class) return section.getInt(path);
        if (fieldType == double.class || fieldType == Double.class) return section.getDouble(path);
        if (fieldType == long.class || fieldType == Long.class) return section.getLong(path);
        if (fieldType == boolean.class || fieldType == Boolean.class) return section.getBoolean(path);
        return section.getString(path);
    }

    private static Object convertValue(String value, Class<?> fieldType) {
        if (fieldType == String.class) return value;
        if (fieldType == int.class || fieldType == Integer.class) return Integer.parseInt(value);
        if (fieldType == double.class || fieldType == Double.class) return Double.parseDouble(value);
        if (fieldType == long.class || fieldType == Long.class) return Long.parseLong(value);
        if (fieldType == boolean.class || fieldType == Boolean.class) return Boolean.parseBoolean(value);
        return value;
    }

    private static void setField(Object instance, Field field, Object value) {
        try {
            if (!field.isAccessible()) field.setAccessible(true);
            field.set(instance, value);
        } catch (Exception e) {
            throw new RuntimeException("Cannot set field " + field.getName() + " on " + instance.getClass().getName(), e);
        }
    }

    private static List<Field> findAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field f : current.getDeclaredFields()) {
                fields.add(f);
            }
            current = current.getSuperclass();
        }
        return fields;
    }
}
