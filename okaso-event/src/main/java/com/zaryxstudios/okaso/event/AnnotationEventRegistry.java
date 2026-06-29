package com.zaryxstudios.okaso.event;

import com.zaryxstudios.okaso.common.event.OkasoEvent;
import com.zaryxstudios.okaso.common.event.EventBus;
import com.zaryxstudios.okaso.common.event.EventHandler;
import com.zaryxstudios.okaso.common.event.EventPriority;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class AnnotationEventRegistry {

    private final EventBus eventBus;

    public AnnotationEventRegistry(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @SuppressWarnings("unchecked")
    public void register(Object listener) {
        Class<?> clazz = listener.getClass();
        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            EventHandler ann = method.getAnnotation(EventHandler.class);
            if (ann == null) continue;

            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length != 1 || !OkasoEvent.class.isAssignableFrom(paramTypes[0])) {
                throw new IllegalArgumentException(
                    "@EventHandler method " + method.getName() + " in " + clazz.getName()
                    + " must accept exactly one OkasoEvent parameter");
            }

            final Class<?> eventClass = paramTypes[0];
            EventPriority priority = ann.priority();
            final Method finalMethod = method;

            if (!finalMethod.isAccessible()) {
                finalMethod.setAccessible(true);
            }

            registerUnsafe(eventClass, priority, listener, new Consumer<OkasoEvent>() {
                @Override
                public void accept(OkasoEvent event) {
                    try {
                        finalMethod.invoke(listener, event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void unregister(Object listener) {
        eventBus.unregister(listener);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerUnsafe(Class<?> eventClass, EventPriority priority,
                                Object owner, java.util.function.Consumer<OkasoEvent> handler) {
        Class rawClass = eventClass;
        eventBus.register(rawClass, priority, owner, handler);
    }
}
