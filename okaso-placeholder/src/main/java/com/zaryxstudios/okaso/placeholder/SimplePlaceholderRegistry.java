package com.zaryxstudios.okaso.placeholder;

import com.zaryxstudios.okaso.common.placeholder.PlaceholderRegistry;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class SimplePlaceholderRegistry implements PlaceholderRegistry {

    private final Map<String, Function<Object, String>> resolvers;
    private final Map<String, String> staticValues;
    private final Map<String, PlaceholderMetadata> metadata;

    public SimplePlaceholderRegistry() {
        this.resolvers = new ConcurrentHashMap<>();
        this.staticValues = new ConcurrentHashMap<>();
        this.metadata = new ConcurrentHashMap<>();
    }

    @Override
    public void register(String identifier, Function<Object, String> resolver) {
        if (identifier == null || identifier.isEmpty()) return;
        resolvers.put(identifier.toLowerCase(), resolver);
    }

    @Override
    public void unregister(String identifier) {
        if (identifier == null) return;
        String key = identifier.toLowerCase();
        resolvers.remove(key);
        staticValues.remove(key);
        metadata.remove(key);
    }

    @Override
    public Set<String> getIdentifiers() {
        return Collections.unmodifiableSet(resolvers.keySet());
    }

    @Override
    public void registerStatic(String identifier, String value) {
        if (identifier == null || identifier.isEmpty()) return;
        String key = identifier.toLowerCase();
        staticValues.put(key, value);
        resolvers.put(key, p -> value);
    }

    @Override
    public boolean isRegistered(String identifier) {
        if (identifier == null) return false;
        String key = identifier.toLowerCase();
        return resolvers.containsKey(key) || staticValues.containsKey(key);
    }

    public String resolve(String identifier, Object player) {
        if (identifier == null) return null;
        String key = identifier.toLowerCase();
        
        Function<Object, String> resolver = resolvers.get(key);
        if (resolver != null) {
            try {
                return resolver.apply(player);
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }
        
        String staticValue = staticValues.get(key);
        if (staticValue != null) {
            return staticValue;
        }
        
        return null;
    }

    public Optional<String> resolveOptional(String identifier, Object player) {
        String result = resolve(identifier, player);
        return Optional.ofNullable(result);
    }

    public void registerWithMetadata(String identifier, Function<Object, String> resolver, String description, String example) {
        register(identifier, resolver);
        metadata.put(identifier.toLowerCase(), new PlaceholderMetadata(description, example));
    }

    public void registerStaticWithMetadata(String identifier, String value, String description, String example) {
        registerStatic(identifier, value);
        metadata.put(identifier.toLowerCase(), new PlaceholderMetadata(description, example));
    }

    public Optional<PlaceholderMetadata> getMetadata(String identifier) {
        if (identifier == null) return Optional.empty();
        return Optional.ofNullable(metadata.get(identifier.toLowerCase()));
    }

    public Map<String, PlaceholderMetadata> getAllMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    public void clear() {
        resolvers.clear();
        staticValues.clear();
        metadata.clear();
    }

    public int size() {
        return resolvers.size();
    }

    public static class PlaceholderMetadata {
        private final String description;
        private final String example;

        public PlaceholderMetadata(String description, String example) {
            this.description = description;
            this.example = example;
        }

        public String getDescription() {
            return description;
        }

        public String getExample() {
            return example;
        }
    }
}
