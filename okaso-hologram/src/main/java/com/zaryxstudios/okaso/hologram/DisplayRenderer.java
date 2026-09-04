package com.zaryxstudios.okaso.hologram;

import com.zaryxstudios.okaso.common.hologram.HologramLineType;
import com.zaryxstudios.okaso.common.hologram.HologramRenderer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ageable;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DisplayRenderer implements HologramRenderer {

    private static final boolean AVAILABLE;
    private static Class<?> TEXT_DISPLAY_CLASS;
    private static Method SET_TEXT;
    private static Method SET_SHADOWED;
    private static Method SET_BACKGROUND;
    private static Method SET_VIEW_RANGE;
    private static Method SET_ALIGNMENT;

    static {
        boolean avail = false;
        try {
            TEXT_DISPLAY_CLASS = Class.forName("org.bukkit.entity.TextDisplay");
            SET_TEXT = TEXT_DISPLAY_CLASS.getMethod("setText", String.class);
            SET_SHADOWED = TEXT_DISPLAY_CLASS.getMethod("setShadowed", boolean.class);
            SET_BACKGROUND = TEXT_DISPLAY_CLASS.getMethod("setBackgroundColor", int.class);
            SET_VIEW_RANGE = TEXT_DISPLAY_CLASS.getMethod("setViewRange", float.class);
            Class<?> alignClass = Class.forName("org.bukkit.entity.TextDisplay");
            SET_ALIGNMENT = TEXT_DISPLAY_CLASS.getMethod("setAlignment", alignClass);
            avail = true;
        } catch (Exception ignored) {
        }
        AVAILABLE = avail;
    }

    private final List<Entity> entities = Collections.synchronizedList(new ArrayList<>());

    public static boolean isAvailable() {
        return AVAILABLE;
    }

    @Override
    public Object spawnText(Location loc, String text) {
        if (!AVAILABLE) {
            return null;
        }
        World world = loc.getWorld();

        if (world == null) {
            return null;
        }

        try {
            EntityType type = EntityType.valueOf("TEXT_DISPLAY");
            Entity entity = world.spawnEntity(loc, type);
            entities.add(entity);
            SET_TEXT.invoke(entity, text);
            SET_SHADOWED.invoke(entity, false);
            SET_BACKGROUND.invoke(entity, 0x00000000);
            SET_VIEW_RANGE.invoke(entity, 0.5f);
            try {
                Object center = Enum.valueOf(
                    (Class<? extends Enum>) Class.forName("org.bukkit.entity.TextDisplay"),
                    "CENTER");
                SET_ALIGNMENT.invoke(entity, center);
            } catch (Exception ignored) {
            }
            return entity;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Object spawnItem(Location loc, String materialName, int amount) {
        if (!AVAILABLE) {
            return null;
        }
        World world = loc.getWorld();
        if (world == null) {
            return null;
        }
        try {
            EntityType type = EntityType.valueOf("ITEM_DISPLAY");
            Entity entity = world.spawnEntity(loc, type);
            entities.add(entity);
            Material mat = Material.getMaterial(materialName.toUpperCase());
            if (mat == null) {
                mat = Material.STONE;
            }

            ItemStack stack = new ItemStack(mat, Math.max(1, amount));
            try {
                Method setItemStack = entity.getClass().getMethod("setItemStack", ItemStack.class);
                setItemStack.invoke(entity, stack);
            } catch (Exception ignored) {
            }
            return entity;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Object spawnMob(Location loc, String entityType) {
        if (!AVAILABLE) {
            return null;
        }
        World world = loc.getWorld();
        if (world == null) {
            return null;
        }

        EntityType type;
        try {
            type = EntityType.valueOf(entityType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }

        if (!type.isSpawnable() || !type.isAlive()) {
            return null;
        }

        try {
            Entity entity = world.spawnEntity(loc, type);
            entities.add(entity);
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;
                living.setCollidable(false);
                living.setInvulnerable(true);
                living.setSilent(true);
                living.setGravity(false);
                living.setAI(false);
                living.setCanPickupItems(false);
                living.setRemoveWhenFarAway(false);
                living.setMaxHealth(1.0);
                living.setHealth(1.0);
                if (entity instanceof Ageable) {
                    ((Ageable) entity).setAdult();
                    ((Ageable) entity).setAgeLock(true);
                }
            }
            entity.setCustomName("");
            entity.setCustomNameVisible(false);
            return entity;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void despawnAll() {
        synchronized (entities) {
            for (Entity e : entities) {
                if (e != null) e.remove();
            }
            entities.clear();
        }
    }

    @Override
    public boolean supportsLineType(HologramLineType type) {
        return type == HologramLineType.TEXT || type == HologramLineType.ITEM;
    }

    public List<Entity> getEntities() {
        return Collections.unmodifiableList(new ArrayList<>(entities));
    }
}