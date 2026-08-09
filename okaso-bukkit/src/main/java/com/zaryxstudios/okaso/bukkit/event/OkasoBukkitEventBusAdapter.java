package com.zaryxstudios.okaso.bukkit.event;

import com.zaryxstudios.okaso.common.event.EventBus;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OkasoBukkitEventBusAdapter implements Listener {

    private static final Logger LOGGER = Logger.getLogger(OkasoBukkitEventBusAdapter.class.getName());
    private static final String EVENT_PACKAGE = "org/bukkit/event";

    private final EventBus eventBus;
    private final Plugin plugin;
    private final Set<Class<? extends Event>> registeredEvents;

    public OkasoBukkitEventBusAdapter(EventBus eventBus, Plugin plugin) {
        this.eventBus = eventBus;
        this.plugin = plugin;
        this.registeredEvents = new HashSet<>();
    }

    public void register() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();

        EventExecutor executor = (listener, event) -> {
            if (eventBus.hasHandlers(OkasoBukkitEvent.class)) {
                eventBus.publish(new OkasoBukkitEvent(event));
            }
        };

        int registered = 0;
        for (Class<? extends Event> eventClass : discoverEventClasses()) {
            try {
                pluginManager.registerEvent(eventClass, this, EventPriority.NORMAL, executor, plugin, false);
                registeredEvents.add(eventClass);
                registered++;
            } catch (Exception ex) {
                LOGGER.log(Level.FINE, "[Okaso] Could not bridge Bukkit event " + eventClass.getName(), ex);
            }
        }

        LOGGER.info("[Okaso] Bridged " + registered + " Bukkit events into the Okaso EventBus.");
    }

    public void unregister() {
        HandlerList.unregisterAll(this);
        registeredEvents.clear();
    }

    @SuppressWarnings("unchecked")
    private static Set<Class<? extends Event>> discoverEventClasses() {
        Set<String> classNames = new HashSet<>();

        URL location = Event.class.getProtectionDomain().getCodeSource().getLocation();
        if (location != null) {
            try {
                File file = new File(location.toURI());
                if (file.isFile()) {
                    try (JarFile jar = new JarFile(file)) {
                        collectClasses(jar, classNames);
                    }
                } else if (file.isDirectory()) {
                    collectClasses(file, EVENT_PACKAGE, classNames);
                }
            } catch (Exception ex) {
                LOGGER.log(Level.FINE, "[Okaso] Could not inspect Bukkit event jar at " + location, ex);
            }
        }

        if (classNames.isEmpty()) {
            collectFromClassloader(classNames);
        }

        Set<Class<? extends Event>> events = new HashSet<>();
        ClassLoader loader = Event.class.getClassLoader();
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className, false, loader);
                if (!Event.class.isAssignableFrom(clazz)) continue;
                if (Modifier.isAbstract(clazz.getModifiers())) continue;
                if (!hasGetHandlerList(clazz)) continue;
                events.add((Class<? extends Event>) clazz);
            } catch (Throwable ignored) {
            }
        }
        return events;
    }

    private static void collectFromClassloader(Set<String> classNames) {
        ClassLoader loader = Event.class.getClassLoader();
        try {
            Enumeration<URL> roots = loader.getResources(EVENT_PACKAGE);
            while (roots.hasMoreElements()) {
                URL url = roots.nextElement();
                if ("jar".equalsIgnoreCase(url.getProtocol())) {
                    try {
                        JarURLConnection connection = (JarURLConnection) url.openConnection();
                        try (JarFile jar = connection.getJarFile()) {
                            collectClasses(jar, classNames);
                        }
                    } catch (IOException ex) {
                        LOGGER.log(Level.FINE, "[Okaso] Could not inspect " + url, ex);
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, "[Okaso] Could not discover Bukkit event classes", ex);
        }
    }

    private static void collectClasses(JarFile jar, Set<String> classNames) {
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            String name = entries.nextElement().getName();
            if (name.startsWith(EVENT_PACKAGE + "/") && name.endsWith(".class") && !name.contains("$")) {
                classNames.add(name.substring(0, name.length() - 6).replace('/', '.'));
            }
        }
    }

    private static void collectClasses(File dir, String prefix, Set<String> classNames) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            String childPrefix = prefix + "/" + file.getName();
            if (file.isDirectory()) {
                collectClasses(file, childPrefix, classNames);
            } else if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
                classNames.add(childPrefix.substring(0, childPrefix.length() - 6).replace('/', '.'));
            }
        }
    }

    private static boolean hasGetHandlerList(Class<?> clazz) {
        try {
            Method method = clazz.getMethod("getHandlerList");
            return HandlerList.class.isAssignableFrom(method.getReturnType());
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
