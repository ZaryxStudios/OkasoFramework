package com.zaryxstudios.okaso.packet;

import org.bukkit.Bukkit;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class PacketCompatibilityResolver {

    private static final String VERSION;
    private static final int MAJOR_VERSION;
    private static final int MINOR_VERSION;
    private static final boolean IS_LEGACY;
    private static final boolean IS_MODERN;
    private static final String NMS_PACKAGE;
    private static final String CRAFTBUKKIT_PACKAGE;

    private static final ConcurrentMap<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();

    static {
        String versionString = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        VERSION = versionString;
        
        int major = 1;
        int minor = 7;
        try {
            String[] parts = versionString.split("_");
            if (parts.length >= 2) {
                major = Integer.parseInt(parts[0].replace("v", ""));
                minor = Integer.parseInt(parts[1].split("R")[0]);
            }
        } catch (Exception ignored) {
        }
        MAJOR_VERSION = major;
        MINOR_VERSION = minor;
        
        IS_LEGACY = major == 1 && minor <= 12;
        IS_MODERN = major > 1 || (major == 1 && minor >= 13);
        
        NMS_PACKAGE = "net.minecraft.server." + versionString;
        CRAFTBUKKIT_PACKAGE = "org.bukkit.craftbukkit." + versionString;
    }

    private PacketCompatibilityResolver() {}

    public static String getVersion() {
        return VERSION;
    }

    public static int getMajorVersion() {
        return MAJOR_VERSION;
    }

    public static int getMinorVersion() {
        return MINOR_VERSION;
    }

    public static boolean isLegacy() {
        return IS_LEGACY;
    }

    public static boolean isModern() {
        return IS_MODERN;
    }

    public static boolean isVersionAtLeast(int major, int minor) {
        return MAJOR_VERSION > major || (MAJOR_VERSION == major && MINOR_VERSION >= minor);
    }

    public static boolean isVersionAtMost(int major, int minor) {
        return MAJOR_VERSION < major || (MAJOR_VERSION == major && MINOR_VERSION <= minor);
    }

    public static Class<?> getNMSClass(String className) {
        return CLASS_CACHE.computeIfAbsent(className, key -> {
            try {
                if (IS_MODERN) {
                    return Class.forName("net.minecraft." + className);
                } else {
                    return Class.forName(NMS_PACKAGE + "." + className);
                }
            } catch (ClassNotFoundException e) {
                return null;
            }
        });
    }

    public static Class<?> getNMSClassGame(String className) {
        return CLASS_CACHE.computeIfAbsent("game." + className, key -> {
            try {
                if (IS_MODERN) {
                    return Class.forName("net.minecraft.network.protocol.game." + className);
                } else {
                    return Class.forName(NMS_PACKAGE + "." + className);
                }
            } catch (ClassNotFoundException e) {
                return null;
            }
        });
    }

    public static Class<?> getNMSClassWorld(String className) {
        return CLASS_CACHE.computeIfAbsent("world." + className, key -> {
            try {
                if (IS_MODERN) {
                    return Class.forName("net.minecraft.world." + className);
                } else {
                    return Class.forName(NMS_PACKAGE + "." + className);
                }
            } catch (ClassNotFoundException e) {
                return null;
            }
        });
    }

    public static Class<?> getNMSClassEntity(String className) {
        return CLASS_CACHE.computeIfAbsent("entity." + className, key -> {
            try {
                if (IS_MODERN) {
                    return Class.forName("net.minecraft.world.entity." + className);
                } else {
                    return Class.forName(NMS_PACKAGE + "." + className);
                }
            } catch (ClassNotFoundException e) {
                return null;
            }
        });
    }

    public static Class<?> getNMSClassParticle(String className) {
        return CLASS_CACHE.computeIfAbsent("particle." + className, key -> {
            try {
                if (IS_MODERN) {
                    return Class.forName("net.minecraft.core.particles." + className);
                } else {
                    return Class.forName(NMS_PACKAGE + "." + className);
                }
            } catch (ClassNotFoundException e) {
                return null;
            }
        });
    }

    public static Class<?> getNMSClassSound(String className) {
        return CLASS_CACHE.computeIfAbsent("sound." + className, key -> {
            try {
                if (IS_MODERN) {
                    return Class.forName("net.minecraft.sounds." + className);
                } else {
                    return Class.forName(NMS_PACKAGE + "." + className);
                }
            } catch (ClassNotFoundException e) {
                return null;
            }
        });
    }

    public static Class<?> getCraftBukkitClass(String className) {
        return CLASS_CACHE.computeIfAbsent("cb." + className, key -> {
            try {
                return Class.forName(CRAFTBUKKIT_PACKAGE + "." + className);
            } catch (ClassNotFoundException e) {
                return null;
            }
        });
    }

    public static Class<?> getEntityClass(String entityName) {
        if (IS_MODERN) {
            Class<?> clazz = getNMSClassEntity("EntityTypes");
            if (clazz != null) {
                try {
                    Object value = clazz.getField(entityName.toUpperCase()).get(null);
                    if (value != null) return value.getClass();
                } catch (Exception ignored) {
                }
            }
        } else {
            Class<?> clazz = getNMSClass("EntityTypes");
            if (clazz != null) {
                try {
                    Object value = clazz.getField(entityName.toUpperCase()).get(null);
                    if (value != null) return value.getClass();
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    public static Object getEntityType(String entityName) {
        if (IS_MODERN) {
            Class<?> entityTypes = getNMSClass("world.entity.EntityType");
            if (entityTypes != null) {
                try {
                    return entityTypes.getField(entityName.toUpperCase()).get(null);
                } catch (Exception ignored) {
                }
            }
        } else {
            Class<?> entityTypes = getNMSClass("EntityTypes");
            if (entityTypes != null) {
                try {
                    return entityTypes.getField(entityName.toUpperCase()).get(null);
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    public static Object getParticleType(String particleName) {
        if (IS_MODERN) {
            Class<?> particleTypes = getNMSClass("core.particles.ParticleTypes");
            if (particleTypes != null) {
                try {
                    return particleTypes.getField(particleName.toUpperCase()).get(null);
                } catch (Exception ignored) {
                }
            }
        } else {
            Class<?> particleTypes = getNMSClass("ParticleTypes");
            if (particleTypes != null) {
                try {
                    return particleTypes.getField(particleName.toUpperCase()).get(null);
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    public static Object getSoundEvent(String soundName) {
        if (IS_MODERN) {
            Class<?> soundEvents = getNMSClass("sounds.SoundEvents");
            if (soundEvents != null) {
                try {
                    return soundEvents.getField(soundName.toUpperCase()).get(null);
                } catch (Exception ignored) {
                }
            }
        } else {
            Class<?> soundEvents = getNMSClass("SoundEvents");
            if (soundEvents != null) {
                try {
                    return soundEvents.getField(soundName.toUpperCase()).get(null);
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    public static String getPacketClassName(String modernName, String legacyName) {
        return IS_MODERN ? modernName : legacyName;
    }

    public static List<String> getPacketClassNames(String modernName, String... legacyNames) {
        if (IS_MODERN) {
            return Arrays.asList(modernName);
        } else {
            return Arrays.asList(legacyNames);
        }
    }

    public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        if (clazz == null) return null;
        try {
            return clazz.getMethod(name, paramTypes);
        } catch (NoSuchMethodException e) {
            for (Method method : clazz.getMethods()) {
                if (method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), paramTypes)) {
                    return method;
                }
            }
        }
        return null;
    }

    public static Method findMethodIgnoreParams(Class<?> clazz, String name) {
        if (clazz == null) return null;
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }

    public static Optional<Class<?>> getOptionalNMSClass(String className) {
        return Optional.ofNullable(getNMSClass(className));
    }

    public static Optional<Class<?>> getOptionalNMSClassGame(String className) {
        return Optional.ofNullable(getNMSClassGame(className));
    }

    public static Optional<Class<?>> getOptionalNMSClassWorld(String className) {
        return Optional.ofNullable(getNMSClassWorld(className));
    }

    public static Optional<Class<?>> getOptionalNMSClassEntity(String className) {
        return Optional.ofNullable(getNMSClassEntity(className));
    }

    public static Optional<Class<?>> getOptionalNMSClassParticle(String className) {
        return Optional.ofNullable(getNMSClassParticle(className));
    }

    public static Optional<Class<?>> getOptionalNMSClassSound(String className) {
        return Optional.ofNullable(getNMSClassSound(className));
    }
}