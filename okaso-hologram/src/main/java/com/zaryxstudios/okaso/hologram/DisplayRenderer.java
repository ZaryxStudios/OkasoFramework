package com.zaryxstudios.okaso.hologram;

import com.zaryxstudios.okaso.common.hologram.HologramLineType;
import com.zaryxstudios.okaso.common.hologram.HologramRenderer;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ageable;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
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
            SET_SHADOWED = findMethod("setShadowed", boolean.class);
            SET_BACKGROUND = findMethod("setBackgroundColor", findParameterType("setBackgroundColor"));
            SET_VIEW_RANGE = findMethod("setViewRange", float.class);
            Class<?> alignmentType = findParameterType("setAlignment");
            SET_ALIGNMENT = alignmentType == null ? null : findMethod("setAlignment", alignmentType);
            avail = true;
        } catch (Exception ignored) {
        }
        AVAILABLE = avail;
    }

    private final List<Entity> entities = Collections.synchronizedList(new ArrayList<>());

    @Getter @Setter
    private boolean shadowed = false;
    @Getter @Setter
    private int backgroundColor = 0;
    @Getter @Setter
    private float viewRange = 0.5f;
    @Getter @Setter
    private String alignment = "CENTER";
    @Getter @Setter
    private boolean glowing = false;
    @Getter @Setter
    private boolean invulnerable = true;
    @Getter @Setter
    private boolean collidable = false;
    @Getter @Setter
    private boolean silent = true;
    @Getter @Setter
    private boolean gravity = false;
    @Getter @Setter
    private boolean ai = false;
    @Getter @Setter
    private boolean canPickupItems = false;
    @Getter @Setter
    private boolean removeWhenFarAway = false;
    @Getter @Setter
    private double maxHealth = 1.0;

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
            invokeIfAvailable(SET_SHADOWED, entity, shadowed);
            invokeIfAvailable(SET_BACKGROUND, entity, createBackgroundColor());
            invokeIfAvailable(SET_VIEW_RANGE, entity, viewRange);
            if (SET_ALIGNMENT != null) {
                try {
                    Object align = Enum.valueOf((Class<? extends Enum>) SET_ALIGNMENT.getParameterTypes()[0], alignment);
                    SET_ALIGNMENT.invoke(entity, align);
                } catch (Exception ignored) {
                }
            }
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;
                living.setCollidable(collidable);
                living.setInvulnerable(invulnerable);
                living.setSilent(silent);
                living.setGravity(gravity);
                living.setAI(ai);
                living.setCanPickupItems(canPickupItems);
                living.setRemoveWhenFarAway(removeWhenFarAway);
                living.setMaxHealth(maxHealth);
                living.setHealth(maxHealth);
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
            Material mat = BukkitCompatibilityResolver.material(materialName);
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

        EntityType type = BukkitCompatibilityResolver.entity(entityType);
        if (type == null) {
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
                living.setCollidable(collidable);
                living.setInvulnerable(invulnerable);
                living.setSilent(silent);
                living.setGravity(gravity);
                living.setAI(ai);
                living.setCanPickupItems(canPickupItems);
                living.setRemoveWhenFarAway(removeWhenFarAway);
                living.setMaxHealth(maxHealth);
                living.setHealth(maxHealth);
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
                if (e != null && e.isValid()) e.remove();
            }
            entities.clear();
        }
    }

    @Override
    public boolean supportsLineType(HologramLineType type) {
        return type == HologramLineType.TEXT || type == HologramLineType.ITEM;
    }

    public List<Entity> getEntities() {
        synchronized (entities) {
            entities.removeIf(entity -> entity == null || !entity.isValid());
            return Collections.unmodifiableList(new ArrayList<>(entities));
        }
    }

    private static Method findMethod(String name, Class<?> parameterType) {
        if (parameterType == null) return null;
        try {
            return TEXT_DISPLAY_CLASS.getMethod(name, parameterType);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static Class<?> findParameterType(String name) {
        for (Method method : TEXT_DISPLAY_CLASS.getMethods()) {
            if (method.getName().equals(name) && method.getParameterTypes().length == 1) {
                return method.getParameterTypes()[0];
            }
        }
        return null;
    }

    private static Object createTransparentBackground() {
        try {
            Class<?> colorClass = Class.forName("org.bukkit.Color");
            Method fromRGB = colorClass.getMethod("fromRGB", int.class);
            return fromRGB.invoke(null, 0);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Object createBackgroundColor() {
        try {
            Class<?> colorClass = Class.forName("org.bukkit.Color");
            Method fromRGB = colorClass.getMethod("fromRGB", int.class);
            return fromRGB.invoke(null, backgroundColor);
        } catch (Exception ignored) {
            return createTransparentBackground();
        }
    }

    private static void invokeIfAvailable(Method method, Object target, Object argument) throws Exception {
        if (method != null && argument != null) {
            method.invoke(target, argument);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private boolean shadowed = false;
        private int backgroundColor = 0;
        private float viewRange = 0.5f;
        private String alignment = "CENTER";
        private boolean glowing = false;
        private boolean invulnerable = true;
        private boolean collidable = false;
        private boolean silent = true;
        private boolean gravity = false;
        private boolean ai = false;
        private boolean canPickupItems = false;
        private boolean removeWhenFarAway = false;
        private double maxHealth = 1.0;

        private Builder() {}

        public Builder shadowed(boolean shadowed) {
            this.shadowed = shadowed;
            return this;
        }

        public Builder backgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder viewRange(float viewRange) {
            this.viewRange = viewRange;
            return this;
        }

        public Builder alignment(String alignment) {
            this.alignment = alignment;
            return this;
        }

        public Builder glowing(boolean glowing) {
            this.glowing = glowing;
            return this;
        }

        public Builder invulnerable(boolean invulnerable) {
            this.invulnerable = invulnerable;
            return this;
        }

        public Builder collidable(boolean collidable) {
            this.collidable = collidable;
            return this;
        }

        public Builder silent(boolean silent) {
            this.silent = silent;
            return this;
        }

        public Builder gravity(boolean gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder ai(boolean ai) {
            this.ai = ai;
            return this;
        }

        public Builder canPickupItems(boolean canPickupItems) {
            this.canPickupItems = canPickupItems;
            return this;
        }

        public Builder removeWhenFarAway(boolean removeWhenFarAway) {
            this.removeWhenFarAway = removeWhenFarAway;
            return this;
        }

        public Builder maxHealth(double maxHealth) {
            this.maxHealth = maxHealth;
            return this;
        }

        public DisplayRenderer build() {
            DisplayRenderer renderer = new DisplayRenderer();
            renderer.setShadowed(shadowed);
            renderer.setBackgroundColor(backgroundColor);
            renderer.setViewRange(viewRange);
            renderer.setAlignment(alignment);
            renderer.setGlowing(glowing);
            renderer.setInvulnerable(invulnerable);
            renderer.setCollidable(collidable);
            renderer.setSilent(silent);
            renderer.setGravity(gravity);
            renderer.setAi(ai);
            renderer.setCanPickupItems(canPickupItems);
            renderer.setRemoveWhenFarAway(removeWhenFarAway);
            renderer.setMaxHealth(maxHealth);
            return renderer;
        }
    }
}