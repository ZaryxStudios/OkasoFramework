package com.zaryxstudios.okaso.common.gui.annotation;

import com.zaryxstudios.okaso.common.gui.GUI;
import com.zaryxstudios.okaso.common.gui.GUIClickEvent;
import com.zaryxstudios.okaso.common.gui.GUIItem;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class GUIAnnotationBinder {

    private GUIAnnotationBinder() {
    }

    public static void bind(Object guiController, GUI gui) {
        Class<?> clazz = guiController.getClass();
        for (Field field : findAllFields(clazz)) {
            GUISlot ann = field.getAnnotation(GUISlot.class);
            if (ann == null) continue;
            if (!GUIItem.class.isAssignableFrom(field.getType())) continue;
            try {
                if (!field.isAccessible()) field.setAccessible(true);
                GUIItem item = (GUIItem) field.get(guiController);
                if (item != null) {
                    gui.setItem(ann.value(), item);
                }
            } catch (Exception ignored) {
            }
        }
        for (Method method : findAllMethods(clazz)) {
            GUIClick ann = method.getAnnotation(GUIClick.class);
            if (ann == null) continue;
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length != 1 || !GUIClickEvent.class.isAssignableFrom(paramTypes[0])) continue;
            if (!method.isAccessible()) method.setAccessible(true);
            int slot = ann.slot();
            GUIItem existing = gui.getItem(slot);
            if (existing != null) {
                gui.setItem(slot, wrapClick(existing, guiController, method));
            }
        }
    }

    private static GUIItem wrapClick(GUIItem original, Object controller, Method method) {
        return new GUIItem() {
            @Override
            public Object getItemStack() {
                return original.getItemStack();
            }

            @Override
            public void onClick(GUIClickEvent event) {
                original.onClick(event);
                if (!event.isCancelled()) {
                    try {
                        method.invoke(controller, event);
                    } catch (Exception ignored) {
                    }
                }
            }
        };
    }

    private static java.util.List<Field> findAllFields(Class<?> clazz) {
        java.util.List<Field> fields = new java.util.ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field f : current.getDeclaredFields()) {
                fields.add(f);
            }
            current = current.getSuperclass();
        }
        return fields;
    }

    private static java.util.List<Method> findAllMethods(Class<?> clazz) {
        java.util.List<Method> methods = new java.util.ArrayList<>();
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
