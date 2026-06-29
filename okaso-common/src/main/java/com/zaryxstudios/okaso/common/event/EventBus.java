package com.zaryxstudios.okaso.common.event;

import com.zaryxstudios.okaso.common.OkasoAPI;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import lombok.Getter;

public class EventBus {

    private final Map<Class<?>, Map<OkasoEventPriority, CopyOnWriteArrayList<RegisteredHandler>>> handlers =
        new ConcurrentHashMap<>();
    @Getter
    private volatile boolean shutdown;

    public EventBus() {}

    public <T extends OkasoEvent> void register(Class<T> eventClass, OkasoEventPriority priority, Object owner, Consumer<T> handler) {
        Objects.requireNonNull(eventClass, "eventClass");
        Objects.requireNonNull(priority, "priority");
        Objects.requireNonNull(handler, "handler");
        if (shutdown) throw new IllegalStateException("EventBus is shut down");

        Map<OkasoEventPriority, CopyOnWriteArrayList<RegisteredHandler>> priorityMap =
            handlers.computeIfAbsent(eventClass, k -> new ConcurrentHashMap<>());
        CopyOnWriteArrayList<RegisteredHandler> list =
            priorityMap.computeIfAbsent(priority, k -> new CopyOnWriteArrayList<>());
        list.add(new RegisteredHandler(owner, handler));
    }

    public <T extends OkasoEvent> void register(Class<T> eventClass, Object owner, Consumer<T> handler) {
        register(eventClass, OkasoEventPriority.NORMAL, owner, handler);
    }

    public void unregister(Object owner) {
        for (Map<OkasoEventPriority, CopyOnWriteArrayList<RegisteredHandler>> pm : handlers.values()) {
            for (CopyOnWriteArrayList<RegisteredHandler> list : pm.values()) {
                list.removeIf(reg -> reg.owner == owner);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends OkasoEvent> T publish(T event) {
        if (shutdown) return event;

        Map<OkasoEventPriority, CopyOnWriteArrayList<RegisteredHandler>> priorityMap = handlers.get(event.getClass());
        if (priorityMap == null) return event;

        for (OkasoEventPriority priority : OkasoEventPriority.values()) {
            CopyOnWriteArrayList<RegisteredHandler> list = priorityMap.get(priority);
            if (list != null) {
                for (RegisteredHandler reg : list) {
                    if (event.isCancelled() && priority != OkasoEventPriority.MONITOR) break;
                    try {
                        ((Consumer<T>) reg.handler).accept(event);
                    } catch (Exception e) {
                        onHandlerError(event, e);
                    }
                }
            }
        }
        return event;
    }

    protected void onHandlerError(OkasoEvent event, Exception exception) {
        try {
            OkasoAPI api = OkasoAPI.getInstance();
            api.getPlugin().getOkasoLogger().warning(
                "[EventBus] Handler error for " + event.getClass().getSimpleName() + ": " + exception.getMessage());
        } catch (Exception ignored) {
            System.err.println("[Okaso EventBus] Handler error for " + event.getClass().getSimpleName() + ": " + exception.getMessage());
        }
    }

    public void shutdown() {
        this.shutdown = true;
        handlers.clear();
    }

    private static class RegisteredHandler {
        final Object owner;
        final Object handler;

        RegisteredHandler(Object owner, Object handler) {
            this.owner = owner;
            this.handler = handler;
        }
    }
}
