package com.zaryxstudios.okaso.entity;

import com.zaryxstudios.okaso.common.entity.SkinData;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public final class SkullSkin {

    private SkullSkin() {
    }

    public static ItemStack playerHead(SkinData skin) {
        if (skin == null || !skin.hasTexture()) {
            return null;
        }
        Material material = playerHeadMaterial();
        if (material == null) {
            return null;
        }
        ItemStack head;
        if (material.name().equals("SKULL_ITEM")) {
            head = new ItemStack(material, 1, (short) 3);
        } else {
            head = new ItemStack(material, 1);
        }
        ItemMeta meta = head.getItemMeta();
        if (!(meta instanceof SkullMeta)) {
            return head;
        }
        if (!applyProfile((SkullMeta) meta, skin)) {
            return head;
        }
        head.setItemMeta(meta);
        return head;
    }

    public static Material playerHeadMaterial() {
        Material modern = Material.matchMaterial("PLAYER_HEAD");
        if (modern != null) {
            return modern;
        }
        try {
            return Material.valueOf("SKULL_ITEM");
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static boolean applyProfile(SkullMeta meta, SkinData skin) {
        try {
            Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
            Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
            Constructor<?> profileCtor = gameProfileClass.getConstructor(UUID.class, String.class);
            Object profile = profileCtor.newInstance(UUID.randomUUID(), "OkasoNPC");
            Object property;
            if (skin.getSignature() == null) {
                property = propertyClass.getConstructor(String.class, String.class)
                        .newInstance("textures", skin.getTextureValue());
            } else {
                property = propertyClass.getConstructor(String.class, String.class, String.class)
                        .newInstance("textures", skin.getTextureValue(), skin.getSignature());
            }
            Object propertyMap = gameProfileClass.getMethod("getProperties").invoke(profile);
            propertyMap.getClass().getMethod("put", Object.class, Object.class).invoke(propertyMap, "textures", property);
            try {
                Method setter = meta.getClass().getDeclaredMethod("setProfile", gameProfileClass);
                setter.setAccessible(true);
                setter.invoke(meta, profile);
                return true;
            } catch (NoSuchMethodException first) {
                try {
                    Field field = findField(meta.getClass(), "profile");
                    if (field == null) {
                        return false;
                    }
                    field.setAccessible(true);
                    field.set(meta, profile);
                    return true;
                } catch (Exception second) {
                    return false;
                }
            }
        } catch (Exception ignored) {
            return false;
        }
    }

    private static Field findField(Class<?> clazz, String name) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
