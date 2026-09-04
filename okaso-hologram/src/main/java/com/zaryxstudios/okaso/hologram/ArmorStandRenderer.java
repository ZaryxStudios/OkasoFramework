package com.zaryxstudios.okaso.hologram;

import com.zaryxstudios.okaso.common.hologram.HologramLineType;
import com.zaryxstudios.okaso.common.hologram.HologramRenderer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ageable;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArmorStandRenderer implements HologramRenderer {

    private final List<Entity> entities = Collections.synchronizedList(new ArrayList<>());

    @Override
    public Object spawnText(Location loc, String text) {
        World world = loc.getWorld();
        if (world == null) return null;
        try {
            ArmorStand stand = world.spawn(loc, ArmorStand.class);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setCanPickupItems(false);
            stand.setCustomNameVisible(true);
            stand.setCustomName(text);
            stand.setMarker(true);
            stand.setBasePlate(false);
            stand.setSmall(true);
            entities.add(stand);
            return stand;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Object spawnItem(Location loc, String materialName, int amount) {
        World world = loc.getWorld();
        if (world == null) return null;
        Material mat = Material.getMaterial(materialName.toUpperCase());
        if (mat == null) mat = Material.STONE;
        ItemStack stack = new ItemStack(mat, Math.max(1, amount));
        try {
            Item item = world.dropItem(loc, stack);
            item.setPickupDelay(Integer.MAX_VALUE);
            item.setUnlimitedLifetime(true);
            item.setVelocity(item.getVelocity().zero());
            entities.add(item);
            return item;
        } catch (Exception e) {
            return spawnText(loc, "[" + mat.name() + " x" + amount + "]");
        }
    }

    @Override
    public Object spawnMob(Location loc, String entityType) {
        World world = loc.getWorld();
        if (world == null) return null;
        EntityType type;
        try {
            type = EntityType.valueOf(entityType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return spawnText(loc, "[Mob: " + entityType + "]");
        }
        if (!type.isSpawnable() || !type.isAlive()) {
            return spawnText(loc, "[Mob: " + type.name() + "]");
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
            return spawnText(loc, "[Mob: " + type.name() + "]");
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
        return true;
    }

    public List<Entity> getEntities() {
        return Collections.unmodifiableList(new ArrayList<>(entities));
    }
}