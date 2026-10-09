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

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("deprecation")
public class ArmorStandRenderer implements HologramRenderer {

    private final List<Entity> entities = Collections.synchronizedList(new ArrayList<>());

    @Getter @Setter
    private boolean marker = true;
    @Getter @Setter
    private boolean basePlate = false;
    @Getter @Setter
    private boolean small = true;
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

    @Override
    public Object spawnText(Location loc, String text) {
        World world = loc.getWorld();
        if (world == null) return null;
        try {
            ArmorStand stand = world.spawn(loc, ArmorStand.class);
            stand.setVisible(false);
            stand.setGravity(gravity);
            stand.setCanPickupItems(canPickupItems);
            stand.setCustomNameVisible(true);
            stand.setCustomName(text);
            stand.setMarker(marker);
            stand.setBasePlate(basePlate);
            stand.setSmall(small);
            stand.setGlowing(glowing);
            stand.setInvulnerable(invulnerable);
            stand.setCollidable(collidable);
            stand.setSilent(silent);
            stand.setAI(ai);
            stand.setRemoveWhenFarAway(removeWhenFarAway);
            stand.setMaxHealth(maxHealth);
            stand.setHealth(maxHealth);
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
        Material mat = BukkitCompatibilityResolver.material(materialName);
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
        EntityType type = BukkitCompatibilityResolver.entity(entityType);
        if (type == null) {
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
            return spawnText(loc, "[Mob: " + type.name() + "]");
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
        return true;
    }

    public List<Entity> getEntities() {
        synchronized (entities) {
            entities.removeIf(entity -> entity == null || !entity.isValid());
            return Collections.unmodifiableList(new ArrayList<>(entities));
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private boolean marker = true;
        private boolean basePlate = false;
        private boolean small = true;
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

        public Builder marker(boolean marker) {
            this.marker = marker;
            return this;
        }

        public Builder basePlate(boolean basePlate) {
            this.basePlate = basePlate;
            return this;
        }

        public Builder small(boolean small) {
            this.small = small;
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

        public ArmorStandRenderer build() {
            ArmorStandRenderer renderer = new ArmorStandRenderer();
            renderer.setMarker(marker);
            renderer.setBasePlate(basePlate);
            renderer.setSmall(small);
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