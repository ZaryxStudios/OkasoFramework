package com.zaryxstudios.okaso.event;

import com.zaryxstudios.okaso.common.event.OkasoEvent;
import com.zaryxstudios.okaso.common.event.EventBus;
import com.zaryxstudios.okaso.common.event.OkasoEventHandler;
import com.zaryxstudios.okaso.common.event.OkasoEventPriority;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AnnotationEventRegistry {

    private final EventBus eventBus;

    public AnnotationEventRegistry(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void register(Object listener) {
        for (Method method : findAllMethods(listener.getClass())) {
            OkasoEventHandler ann = method.getAnnotation(OkasoEventHandler.class);
            if (ann == null) continue;

            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length != 1 || !OkasoEvent.class.isAssignableFrom(paramTypes[0])) {
                throw new IllegalArgumentException(
                    "@OkasoEventHandler method " + method.getName() + " in "
                    + listener.getClass().getName()
                    + " must accept exactly one OkasoEvent parameter");
            }

            Class<?> eventClass = paramTypes[0];
            OkasoEventPriority priority = ann.priority();

            if (!method.isAccessible()) {
                method.setAccessible(true);
            }

            registerUnsafe(eventClass, priority, listener, event -> {
                try {
                    method.invoke(listener, event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void unregister(Object listener) {
        eventBus.unregister(listener);
    }

    public void unregisterAll() {
        eventBus.shutdown();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerUnsafe(Class<?> eventClass, OkasoEventPriority priority,
                                Object owner, Consumer<OkasoEvent> handler) {
        eventBus.register((Class) eventClass, priority, owner, (Consumer) handler);
    }

    private static List<Method> findAllMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Method m : current.getDeclaredMethods()) {
                methods.add(m);
            }
            current = current.getSuperclass();
        }
        return methods;
    }
}
