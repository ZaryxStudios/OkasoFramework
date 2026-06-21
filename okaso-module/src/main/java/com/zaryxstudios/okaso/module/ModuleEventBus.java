package com.zaryxstudios.okaso.module;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ModuleEventBus {

    private final Map<Class<?>, CopyOnWriteArrayList<Consumer<?>>> handlers = new ConcurrentHashMap<>();

    public <T> void register(Class<T> eventType, Consumer<T> handler) {
        handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(handler);
    }

    public <T> void unregister(Class<T> eventType, Consumer<T> handler) {
        CopyOnWriteArrayList<Consumer<?>> list = handlers.get(eventType);
        if (list != null) {
            list.remove(handler);
            if (list.isEmpty()) {
                handlers.remove(eventType);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void post(T event) {
        CopyOnWriteArrayList<Consumer<?>> list = handlers.get(event.getClass());
        if (list != null) {
            for (Consumer<?> handler : list) {
                try {
                    ((Consumer<T>) handler).accept(event);
                } catch (Exception e) {
                    System.err.println("[ModuleEventBus] Error handling " + event.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        }
    }

    public void clear() {
        handlers.clear();
    }
}
