package com.zaryxstudios.okaso.common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

public final class ReflectionUtils {

    private ReflectionUtils() {}

    public static <T> T getField(Class<?> clazz, Object instance, String fieldName) {
        Objects.requireNonNull(clazz, "clazz");
        Objects.requireNonNull(fieldName, "fieldName");
        try {
            Field field = findField(clazz, fieldName);
            if (field == null) throw new IllegalArgumentException("Field not found: " + fieldName);
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            T result = (T) field.get(instance);
            return result;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access field: " + fieldName, e);
        }
    }

    public static void setField(Class<?> clazz, Object instance, String fieldName, Object value) {
        Objects.requireNonNull(clazz, "clazz");
        Objects.requireNonNull(fieldName, "fieldName");
        try {
            Field field = findField(clazz, fieldName);
            if (field == null) throw new IllegalArgumentException("Field not found: " + fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot set field: " + fieldName, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(Class<?> clazz, Object instance, String methodName, Object... args) {
        Objects.requireNonNull(clazz, "clazz");
        Objects.requireNonNull(methodName, "methodName");
        try {
            Class<?>[] paramTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
            }
            Method method = findMethod(clazz, methodName, paramTypes);
            if (method == null) throw new IllegalArgumentException("Method not found: " + methodName);
            method.setAccessible(true);
            return (T) method.invoke(instance, args);
        } catch (Exception e) {
            throw new RuntimeException("Cannot invoke method: " + methodName, e);
        }
    }

    public static Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try { return current.getDeclaredField(fieldName); } catch (NoSuchFieldException e) { current = current.getSuperclass(); }
        }
        return null;
    }

    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try { return current.getDeclaredMethod(methodName, paramTypes); } catch (NoSuchMethodException e) { current = current.getSuperclass(); }
        }
        return null;
    }

    public static String getMinecraftVersion() {
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Method getBukkitVersion = bukkitClass.getMethod("getBukkitVersion");
            String full = (String) getBukkitVersion.invoke(null);
            if (full == null) return "unknown";
            int dash = full.indexOf('-');
            return dash > 0 ? full.substring(0, dash) : full;
        } catch (Exception e) {
            return "unknown";
        }
    }

    public static Class<?> tryClass(String name) {
        try { return Class.forName(name); } catch (ClassNotFoundException e) { return null; }
    }

    public static <T> T newInstance(Class<T> clazz) {
        try { return clazz.getDeclaredConstructor().newInstance(); } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate " + clazz.getName(), e);
        }
    }
}
