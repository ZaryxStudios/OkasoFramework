package com.zaryxstudios.okaso.placeholder;

import com.zaryxstudios.okaso.common.placeholder.PlaceholderRegistry;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class SimplePlaceholderRegistry implements PlaceholderRegistry {

    private final Map<String, Function<Object, String>> resolvers;

    public SimplePlaceholderRegistry() {
        this.resolvers = new LinkedHashMap<String, Function<Object, String>>();
    }

    @Override
    public void register(String identifier, Function<Object, String> resolver) {
        resolvers.put(identifier, resolver);
    }

    @Override
    public void unregister(String identifier) {
        resolvers.remove(identifier);
    }

    @Override
    public Set<String> getIdentifiers() {
        return Collections.unmodifiableSet(resolvers.keySet());
    }

    @Override
    public void registerStatic(String identifier, String value) {
        resolvers.put(identifier, p -> value);
    }

    @Override
    public boolean isRegistered(String identifier) {
        return resolvers.containsKey(identifier);
    }

    public String resolve(String identifier, Object player) {
        Function<Object, String> resolver = resolvers.get(identifier);
        if (resolver != null) {
            return resolver.apply(player);
        }
        return null;
    }
}
