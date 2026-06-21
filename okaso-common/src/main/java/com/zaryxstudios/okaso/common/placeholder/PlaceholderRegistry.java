package com.zaryxstudios.okaso.common.placeholder;

import java.util.Set;
import java.util.function.Function;

public interface PlaceholderRegistry {
    void register(String identifier, Function<Object, String> resolver);
    void registerStatic(String identifier, String value);
    void unregister(String identifier);
    Set<String> getIdentifiers();
    boolean isRegistered(String identifier);
}
