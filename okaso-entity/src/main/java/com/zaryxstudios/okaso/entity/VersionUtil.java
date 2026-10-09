package com.zaryxstudios.okaso.entity;

import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VersionUtil {

    private static final int MAJOR;
    private static final int MINOR;
    private static final int PATCH;

    static {
        String bukkitVersion = Bukkit.getBukkitVersion();
        Matcher m = Pattern.compile("(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?").matcher(bukkitVersion);
        if (m.find()) {
            MAJOR = Integer.parseInt(m.group(1));
            MINOR = m.group(2) == null ? 0 : Integer.parseInt(m.group(2));
            PATCH = m.group(3) == null ? 0 : Integer.parseInt(m.group(3));
        } else {
            MAJOR = 1;
            MINOR = 7;
            PATCH = 10;
        }
    }

    private VersionUtil() {}

    public static boolean atLeast(int major, int minor, int patch) {
        if (MAJOR != major) {
            return MAJOR > major;
        }

        if (MINOR != minor) {
            return MINOR > minor;
        }

        return PATCH >= patch;
    }

    public static int getMajor() {
        return MAJOR;
    }

    public static int getMinor() {
        return MINOR;
    }

    public static int getPatch() {
        return PATCH;
    }

    public static boolean hasArmorStand() {
        return atLeast(1, 8, 0);
    }
    public static boolean hasPoseFlags() {
        return atLeast(1, 9, 0);
    }
    public static boolean hasFlattenedEntityTypes() {
        return atLeast(1, 13, 0);
    }
    public static boolean hasVillagerProfessions() {
        return atLeast(1, 14, 0);
    }
    public static boolean hasNewMobVariants() {
        return atLeast(1, 16, 0);
    }
    public static boolean hasCavesAndCliffs() {
        return atLeast(1, 17, 0);
    }
    public static boolean hasWildUpdate() {
        return atLeast(1, 19, 0);
    }
    public static boolean hasArmoredMobs() {
        return atLeast(1, 20, 5);
    }

    public static boolean hasClass(String className) {
        if (className == null || className.trim().isEmpty()) return false;
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public static boolean hasMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        if (type == null || name == null) return false;
        try {
            type.getMethod(name, parameterTypes);
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }

    public static boolean hasItemMetaMethod(String name, Class<?>... parameterTypes) {
        try {
            Class<?> itemMeta = Class.forName("org.bukkit.inventory.meta.ItemMeta");
            return hasMethod(itemMeta, name, parameterTypes);
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public static boolean hasModernDisplayEntities() {
        return hasClass("org.bukkit.entity.TextDisplay")
            && hasClass("org.bukkit.entity.ItemDisplay");
    }

    public static boolean hasPersistentData() {
        try {
            Class<?> entity = Class.forName("org.bukkit.persistence.PersistentDataHolder");
            return hasMethod(entity, "getPersistentDataContainer");
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
