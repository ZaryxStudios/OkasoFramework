package com.zaryxstudios.okaso.entity;

import com.zaryxstudios.okaso.common.entity.NPCHandle;
import com.zaryxstudios.okaso.common.entity.SkinData;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.UUID;
import java.util.function.Consumer;

public class PacketNPCHandle implements NPCHandle {

    private final EntityType spawnType;
    private final boolean fakePlayer;
    private final Consumer<Entity> configurator;
    private final UUID stableId;
    private final Object spawnLock = new Object();
    private volatile Entity entity;
    private volatile Location pendingLocation;
    private volatile boolean spawned;
    private volatile SkinData skin;

    PacketNPCHandle(Entity entity, boolean fakePlayer) {
        this(entity, fakePlayer, null, entity == null ? null : entity.getLocation().clone());
    }

    PacketNPCHandle(Entity entity, boolean fakePlayer, Consumer<Entity> configurator, Location pendingLocation) {
        this.entity = entity;
        this.fakePlayer = fakePlayer;
        this.configurator = configurator;
        this.pendingLocation = pendingLocation == null ? null : pendingLocation.clone();
        this.spawnType = entity == null ? null : entity.getType();
        this.stableId = entity == null ? UUID.randomUUID() : entity.getUniqueId();
        this.spawned = entity != null && entity.isValid();
    }

    PacketNPCHandle(EntityType spawnType, Location location, boolean fakePlayer, Consumer<Entity> configurator) {
        this.spawnType = spawnType;
        this.pendingLocation = location.clone();
        this.fakePlayer = fakePlayer;
        this.configurator = configurator;
        this.stableId = UUID.randomUUID();
        this.spawned = false;
        this.entity = null;
    }

    @Override
    public void spawn() {
        synchronized (spawnLock) {
            Entity current = entity;
            if (spawned && current != null && current.isValid()) {
                return;
            }
            if (!Bukkit.isPrimaryThread()) {
                throw new IllegalStateException("NPCHandle.spawn() must be called on the server main thread");
            }
            Location loc = pendingLocation;
            if (loc == null && current != null) {
                loc = current.getLocation();
            }
            if (loc == null) {
                throw new IllegalStateException("NPC location is null");
            }
            World world = loc.getWorld();
            if (world == null) {
                throw new IllegalStateException("NPC world is null");
            }
            int cx = loc.getBlockX() >> 4;
            int cz = loc.getBlockZ() >> 4;
            if (!world.isChunkLoaded(cx, cz)) {
                world.loadChunk(cx, cz, true);
            }
            EntityType type = spawnType != null ? spawnType : resolveFallbackType();
            Entity spawnedEntity;
            try {
                spawnedEntity = world.spawnEntity(loc, type);
            } catch (IllegalArgumentException ex) {
                spawnedEntity = world.spawnEntity(loc, EntityType.ZOMBIE);
            }
            if (configurator != null) {
                configurator.accept(spawnedEntity);
            }
            entity = spawnedEntity;
            pendingLocation = spawnedEntity.getLocation().clone();
            spawned = true;
        }
    }

    @Override
    public void despawn() {
        synchronized (spawnLock) {
            Entity current = entity;
            if (current != null) {
                pendingLocation = current.getLocation().clone();
                if (current.isValid()) {
                    current.remove();
                }
            }
            spawned = false;
        }
    }

    @Override
    public void setLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            throw new IllegalArgumentException("Location and world must not be null");
        }
        synchronized (spawnLock) {
            pendingLocation = location.clone();
            Entity current = entity;
            if (spawned && current != null && current.isValid()) {
                if (!Bukkit.isPrimaryThread()) {
                    throw new IllegalStateException("NPCHandle.setLocation() on a spawned NPC must be called on the server main thread");
                }
                current.teleport(location);
            }
        }
    }

    @Override
    public Location getLocation() {
        Entity current = entity;
        if (current != null && current.isValid()) {
            return current.getLocation();
        }
        return pendingLocation == null ? null : pendingLocation.clone();
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        synchronized (spawnLock) {
            Location loc = getLocation();
            if (loc == null) {
                throw new IllegalStateException("NPC location is null");
            }
            loc.setYaw(yaw);
            loc.setPitch(pitch);
            pendingLocation = loc.clone();
            Entity current = entity;
            if (spawned && current != null && current.isValid()) {
                if (!Bukkit.isPrimaryThread()) {
                    throw new IllegalStateException("NPCHandle.setRotation() on a spawned NPC must be called on the server main thread");
                }
                current.teleport(loc);
            }
        }
    }

    @Override
    public boolean isSpawned() {
        Entity current = entity;
        return spawned && current != null && current.isValid();
    }

    @Override
    public UUID getUniqueId() {
        Entity current = entity;
        if (current != null) {
            return current.getUniqueId();
        }
        return stableId;
    }

    @Override
    public boolean isFakePlayer() {
        return fakePlayer;
    }

    @Override
    public SkinData getSkin() {
        return skin;
    }

    public void setSkin(SkinData skin) {
        this.skin = skin;
    }

    @Override
    public void setCustomName(String name) {
        synchronized (spawnLock) {
            Entity current = entity;
            if (current != null && current.isValid()) {
                current.setCustomName(name);
                return;
            }
            if (configurator instanceof NamedConfigurator) {
                ((NamedConfigurator) configurator).setName(name);
            }
        }
    }

    public Entity getEntity() {
        return entity;
    }

    private EntityType resolveFallbackType() {
        try {
            return EntityType.valueOf("ARMOR_STAND");
        } catch (IllegalArgumentException ignored) {
            return EntityType.ZOMBIE;
        }
    }

    static final class NamedConfigurator implements Consumer<Entity> {
        private final Consumer<Entity> delegate;
        private volatile String name;

        NamedConfigurator(Consumer<Entity> delegate, String name) {
            this.delegate = delegate;
            this.name = name;
        }

        void setName(String name) {
            this.name = name;
        }

        @Override
        public void accept(Entity entity) {
            if (delegate != null) {
                delegate.accept(entity);
            }
            if (name != null) {
                entity.setCustomName(name);
            }
        }
    }

    @Override
    public String toString() {
        return "PacketNPCHandle{uuid=" + getUniqueId()
            + ", fakePlayer=" + fakePlayer
            + ", spawned=" + isSpawned()
            + "}";
    }
}
