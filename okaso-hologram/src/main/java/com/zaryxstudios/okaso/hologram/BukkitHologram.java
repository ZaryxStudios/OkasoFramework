package com.zaryxstudios.okaso.hologram;

import com.zaryxstudios.okaso.common.hologram.Hologram;
import com.zaryxstudios.okaso.common.hologram.HologramLine;
import com.zaryxstudios.okaso.common.hologram.HologramLineType;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Item;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;

public class BukkitHologram implements Hologram {

    private static final boolean HAS_ARMOR_STAND;
    private static final Class<?> ARMOR_STAND_CLASS;
    private static final Method WORLD_SPAWN;
    private static final Method SET_VISIBLE;
    private static final Method SET_GRAVITY;
    private static final Method SET_PICKUP;
    private static final Method SET_NAME_VISIBLE;
    private static final Method SET_CUSTOM_NAME;
    private static final Method SET_MARKER;
    private static final Method ENTITY_REMOVE;
    private static final Method ENTITY_TELEPORT;
    private static final Method SET_BASE_PLATE;
    private static final Method SET_SMALL;

    private static final boolean HAS_ITEM_ENTITY;
    private static final Class<?> ITEM_CLASS;
    private static final Method SET_ITEM_STACK;
    private static final Method SET_PICKUP_DELAY;
    private static final Method SET_UNLIMITED_LIFETIME;
    private static final Method ITEM_TELEPORT;

    private static final boolean HAS_AI_METHOD;
    private static final Method SET_AI;
    private static final Method SET_REMOVE_WHEN_FAR_AWAY;

    static {
        Class<?> asClass = null;
        Method spawn = null, vis = null, grav = null, pickup = null,
               nameVis = null, cname = null, marker = null, remove = null,
               teleport = null, basePlate = null, small = null;

        Class<?> itemClass = null;
        Method setItem = null, setPickupDelay = null, unlimitedLife = null, itemTp = null;

        boolean hasAi = false;
        Method setAiMethod = null, removeFar = null;

        try {
            asClass = Class.forName("org.bukkit.entity.ArmorStand");

            spawn     = World.class.getMethod("spawn", Location.class, Class.class);
            vis       = asClass.getMethod("setVisible", boolean.class);
            grav      = asClass.getMethod("setGravity", boolean.class);
            pickup    = asClass.getMethod("setCanPickupItems", boolean.class);
            nameVis   = asClass.getMethod("setCustomNameVisible", boolean.class);
            cname     = asClass.getMethod("setCustomName", String.class);
            marker    = asClass.getMethod("setMarker", boolean.class);
            remove    = asClass.getMethod("remove");
            teleport  = asClass.getMethod("teleport", Location.class);
            basePlate = asClass.getMethod("setBasePlate", boolean.class);
            small     = asClass.getMethod("setSmall", boolean.class);
        } catch (Exception ignored) {
        }

        try {
            itemClass     = Class.forName("org.bukkit.entity.Item");
            setItem       = itemClass.getMethod("setItemStack", ItemStack.class);
            setPickupDelay = itemClass.getMethod("setPickupDelay", int.class);
            unlimitedLife = itemClass.getMethod("setUnlimitedLifetime", boolean.class);
            itemTp        = itemClass.getMethod("teleport", Location.class);
        } catch (Exception ignored) {
        }

        try {
            Class<?> living = Class.forName("LivingEntity");
            setAiMethod = living.getMethod("setAI", boolean.class);
            removeFar   = living.getMethod("setRemoveWhenFarAway", boolean.class);
            hasAi = true;
        } catch (Exception ignored) {
        }

        HAS_ARMOR_STAND   = asClass != null;
        ARMOR_STAND_CLASS = asClass;
        WORLD_SPAWN       = spawn;
        SET_VISIBLE       = vis;
        SET_GRAVITY       = grav;
        SET_PICKUP        = pickup;
        SET_NAME_VISIBLE  = nameVis;
        SET_CUSTOM_NAME   = cname;
        SET_MARKER        = marker;
        ENTITY_REMOVE     = remove;
        ENTITY_TELEPORT   = teleport;
        SET_BASE_PLATE    = basePlate;
        SET_SMALL         = small;

        HAS_ITEM_ENTITY       = itemClass != null;
        ITEM_CLASS            = itemClass;
        SET_ITEM_STACK        = setItem;
        SET_PICKUP_DELAY      = setPickupDelay;
        SET_UNLIMITED_LIFETIME = unlimitedLife;
        ITEM_TELEPORT         = itemTp;

        HAS_AI_METHOD              = hasAi;
        SET_AI                     = setAiMethod;
        SET_REMOVE_WHEN_FAR_AWAY   = removeFar;
    }

    @Getter
    private final String id;
    private final List<HologramLine> lines;
    private final List<Entity> entities;
    private Location location;
    private boolean active;

    public BukkitHologram(String id, Location location, List<HologramLine> lines) {
        this.id = id;
        this.location = location.clone();
        this.lines = new ArrayList<>(lines);
        this.entities = new ArrayList<>();
        this.active = false;
    }

    @Override
    public List<HologramLine> getLines() {
        return Collections.unmodifiableList(new ArrayList<>(lines));
    }

    @Override
    public void setLines(List<HologramLine> newLines) {
        lines.clear();
        lines.addAll(newLines);
        if (active) refresh();
    }

    @Override
    public void setLine(int index, HologramLine line) {
        if (index < 0 || index >= lines.size()) return;
        lines.set(index, line);
        if (active) refresh();
    }

    @Override
    public void addLine(HologramLine line) {
        lines.add(line);
        if (active) {
            int idx = lines.size() - 1;
            spawnEntityForLine(idx, line, lineLocation(idx));
        }
    }

    @Override
    public void insertLine(int index, HologramLine line) {
        if (index < 0 || index > lines.size()) return;
        lines.add(index, line);
        if (active) refresh();
    }

    @Override
    public void removeLine(int index) {
        if (index < 0 || index >= lines.size()) return;
        lines.remove(index);
        if (active) refresh();
    }

    @Override
    public int getLineCount() {
        return lines.size();
    }

    @Override
    public void clearLines() {
        lines.clear();
        if (active) refresh();
    }

    @Override
    public List<String> getTextLines() {
        return lines.stream()
            .filter(l -> l.getType() == HologramLineType.TEXT)
            .map(HologramLine::getText)
            .collect(Collectors.toList());
    }

    @Override
    public void setTextLines(List<String> textLines) {
        lines.clear();
        for (String t : textLines) {
            lines.add(HologramLine.text(t));
        }
        if (active) refresh();
    }

    @Override
    public void setText(int index, String text) {
        if (index < 0 || index >= lines.size()) return;
        lines.set(index, HologramLine.text(text));
        if (active) refresh();
    }

    @Override
    public void addText(String text) {
        addLine(HologramLine.text(text));
    }

    @Override
    public void insertText(int index, String text) {
        insertLine(index, HologramLine.text(text));
    }

    @Override
    public void addItem(String materialName, int amount) {
        addLine(HologramLine.item(materialName, amount));
    }

    @Override
    public void addItem(String materialName) {
        addLine(HologramLine.item(materialName, 1));
    }

    @Override
    public void addMob(String entityType) {
        addLine(HologramLine.mob(entityType));
    }

    @Override
    public void teleport(double x, double y, double z, float yaw, float pitch) {
        this.location = new Location(location.getWorld(), x, y, z, yaw, pitch);
        if (active) {
            for (int i = 0; i < entities.size(); i++) {
                Entity e = entities.get(i);
                if (e != null) {
                    e.teleport(lineLocation(i));
                }
            }
        }
    }

    public Location getLocation() {
        return location.clone();
    }

    @Override
    public void start() {
        if (active || location.getWorld() == null) return;
        active = true;
        spawnAll();
    }

    @Override
    public void stop() {
        if (!active) return;
        active = false;
        despawnAll();
    }

    @Override
    public boolean isRunning() {
        return active;
    }

    private Location lineLocation(int index) {
        return location.clone().add(0, -0.30 * index, 0);
    }

    private void spawnAll() {
        entities.clear();
        for (int i = 0; i < lines.size(); i++) {
            HologramLine line = lines.get(i);
            spawnEntityForLine(i, line, lineLocation(i));
        }
    }

    private void spawnEntityForLine(int index, HologramLine line, Location loc) {
        switch (line.getType()) {
            case TEXT:
                spawnTextLine(loc, line.getText());
                break;
            case ITEM:
                spawnItemLine(loc, line.getItemMaterial(), line.getItemAmount());
                break;
            case MOB:
                spawnMobLine(loc, line.getEntityType());
                break;
        }
    }

    private void spawnTextLine(Location loc, String text) {
        if (!HAS_ARMOR_STAND) return;
        World world = loc.getWorld();
        if (world == null) return;
        try {
            Object stand = WORLD_SPAWN.invoke(world, loc, ARMOR_STAND_CLASS);
            SET_VISIBLE.invoke(stand, false);
            SET_GRAVITY.invoke(stand, false);
            SET_PICKUP.invoke(stand, false);
            SET_NAME_VISIBLE.invoke(stand, true);
            SET_CUSTOM_NAME.invoke(stand, text);
            SET_MARKER.invoke(stand, true);
            if (SET_BASE_PLATE != null) SET_BASE_PLATE.invoke(stand, false);
            if (SET_SMALL != null) SET_SMALL.invoke(stand, true);
            entities.add((Entity) stand);
        } catch (Exception ignored) {
        }
    }

    private void spawnItemLine(Location loc, String materialName, int amount) {
        World world = loc.getWorld();
        if (world == null) return;

        Material mat = Material.getMaterial(materialName.toUpperCase());
        if (mat == null) mat = Material.STONE;

        ItemStack stack = new ItemStack(mat, Math.max(1, amount));
        if (!HAS_ITEM_ENTITY) {
            spawnTextLine(loc, "[" + mat.name() + " x" + amount + "]");
            return;
        }

        try {
            Item item = world.dropItem(loc, stack);
            item.setPickupDelay(Integer.MAX_VALUE);
            item.setUnlimitedLifetime(true);
            item.setVelocity(item.getVelocity().zero());
            entities.add(item);
        } catch (Exception ignored) {
            spawnTextLine(loc, "[" + mat.name() + " x" + amount + "]");
        }
    }

    private void spawnMobLine(Location loc, String entityTypeName) {
        World world = loc.getWorld();
        if (world == null) return;

        EntityType type;
        try {
            type = EntityType.valueOf(entityTypeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            spawnTextLine(loc, "[Mob: " + entityTypeName + "]");
            return;
        }

        if (!type.isSpawnable() || !type.isAlive()) {
            spawnTextLine(loc, "[Mob: " + type.name() + "]");
            return;
        }

        try {
            Entity entity = world.spawnEntity(loc, type);
            entities.add(entity);

            if (HAS_AI_METHOD && SET_AI != null) {
                try {
                    SET_AI.invoke(entity, false);
                } catch (Exception ignored) {
                }
            }
            if (SET_REMOVE_WHEN_FAR_AWAY != null) {
                try {
                    SET_REMOVE_WHEN_FAR_AWAY.invoke(entity, false);
                } catch (Exception ignored) {
                }
            }

            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;
                living.setCollidable(false);
                living.setInvulnerable(true);
                living.setSilent(true);
                living.setGravity(false);
                living.setCanPickupItems(false);
                living.setRemoveWhenFarAway(false);
                living.setMaxHealth(1.0);
                living.setHealth(1.0);

                if (entity instanceof Ageable) {
                    ((Ageable) entity).setAdult();
                    ((Ageable) entity).setAgeLock(true);
                }
            }

            if (SET_CUSTOM_NAME != null && SET_NAME_VISIBLE != null) {
                try {
                    SET_CUSTOM_NAME.invoke(entity, "");
                    SET_NAME_VISIBLE.invoke(entity, false);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
            spawnTextLine(loc, "[Mob: " + type.name() + "]");
        }
    }

    private void despawnAll() {
        for (Entity e : entities) {
            if (e != null && e.isValid()) {
                e.remove();
            }
        }
        entities.clear();
    }

    private void refresh() {
        despawnAll();
        spawnAll();
    }
}
