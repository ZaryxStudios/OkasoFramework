package com.zaryxstudios.okaso.entity;

import com.zaryxstudios.okaso.common.entity.*;
import com.zaryxstudios.okaso.common.Preconditions;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

public final class OkasoBukkitEntityService implements EntityService {

    private static final boolean HAS_GET_ENTITY_UUID;
    private static final Method GET_ENTITY_UUID_METHOD;

    static {
        boolean has = false;
        Method m = null;
        try {
            m = Bukkit.class.getMethod("getEntity", UUID.class);
            has = true;
        } catch (NoSuchMethodException ignored) {
        }
        HAS_GET_ENTITY_UUID = has;
        GET_ENTITY_UUID_METHOD = m;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<T> getEntitiesInWorld(Object world, Class<T> type) {
        if (!(world instanceof World)) return Collections.emptyList();
        List<T> result = new ArrayList<>();
        for (Entity e : ((World) world).getEntities()) {
            if (type.isInstance(e)) result.add((T) e);
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<T> getNearbyEntities(Object location, double radius, Class<T> type) {
        if (!(location instanceof Location)) return Collections.emptyList();
        Location center = (Location) location;
        if (center.getWorld() == null) return Collections.emptyList();
        List<T> result = new ArrayList<>();
        for (Entity e : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (type.isInstance(e)) result.add((T) e);
        }
        return result;
    }

    @Override
    public Optional<Object> getEntity(UUID uuid) {
        if (HAS_GET_ENTITY_UUID) {
            try {
                Entity e = (Entity) GET_ENTITY_UUID_METHOD.invoke(null, uuid);
                return Optional.ofNullable(e);
            } catch (Exception ignored) {
            }
        }
        for (World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {
                if (e.getUniqueId().equals(uuid)) return Optional.of(e);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isValid(Object entity) {
        return entity instanceof Entity && ((Entity) entity).isValid();
    }

    @Override
    public void remove(Object entity) {
        if (entity instanceof Entity) ((Entity) entity).remove();
    }

    @Override
    public void teleport(Object entity, Object location) {
        if (entity instanceof Entity && location instanceof Location)
            ((Entity) entity).teleport((Location) location);
    }

    @Override
    public Object getLocation(Object entity) {
        return entity instanceof Entity ? ((Entity) entity).getLocation() : null;
    }

    @Override
    public Object getWorld(Object entity) {
        return entity instanceof Entity ? ((Entity) entity).getWorld() : null;
    }

    @Override
    public String getName(Object entity) {
        if (!(entity instanceof Entity)) return "";
        Entity e = (Entity) entity;
        String customName = e.getCustomName();
        if (customName != null) return customName;
        try {
            return (String) e.getClass().getMethod("getName").invoke(e);
        } catch (Exception ignored) {
            return "";
        }
    }

    @Override
    public void setFire(Object entity, int ticks) {
        if (entity instanceof Entity) ((Entity) entity).setFireTicks(ticks);
    }

    @Override
    public String getType(Object entity) {
        return entity instanceof Entity ? ((Entity) entity).getType().name() : "";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Object> getPassengers(Object entity) {
        if (!(entity instanceof Entity)) return Collections.emptyList();
        return (Collection<Object>) (Collection<?>) ((Entity) entity).getPassengers();
    }

    @Override
    public NPCHandle createFakePlayer(String name, Location loc, Consumer<FakePlayerBuilder> builder) {
        Preconditions.requireNonNull(loc, "Location cannot be null");
        Preconditions.requireNonNull(builder, "Builder consumer cannot be null");
        Location spawnLoc = requireSpawnableLocation(loc);
        requireMainThread("createFakePlayer");
        EntityType placeholder = resolvePlaceholderType();
        PacketNPCHandle.NamedConfigurator configurator = new PacketNPCHandle.NamedConfigurator(entity -> {
            FakePlayerBuilderImpl impl = new FakePlayerBuilderImpl(entity);
            if (name != null) {
                impl.customName(name);
                impl.customNameVisible(true);
            }
            builder.accept(impl);
            impl.apply();
        }, name);
        PacketNPCHandle handle = new PacketNPCHandle(placeholder, spawnLoc, true, configurator);
        handle.spawn();
        return handle;
    }

    @Override
    public NPCHandle createFakeEntity(EntityType type, Location loc, Consumer<FakeEntityBuilder> builder) {
        Preconditions.requireNonNull(type, "EntityType cannot be null");
        Preconditions.requireNonNull(loc, "Location cannot be null");
        Preconditions.requireNonNull(builder, "Builder consumer cannot be null");
        Location spawnLoc = requireSpawnableLocation(loc);
        requireMainThread("createFakeEntity");
        EntityType resolved = resolveSpawnableType(type);
        PacketNPCHandle.NamedConfigurator configurator = new PacketNPCHandle.NamedConfigurator(entity -> {
            FakeEntityBuilderImpl impl = new FakeEntityBuilderImpl(entity);
            builder.accept(impl);
            impl.apply();
        }, null);
        PacketNPCHandle handle = new PacketNPCHandle(resolved, spawnLoc, false, configurator);
        handle.spawn();
        return handle;
    }

    @Override
    public NPCHandle createNPC(NPCType npcType, Location loc, Consumer<NPCBuilder> builder) {
        Preconditions.requireNonNull(npcType, "NPCType cannot be null");
        Preconditions.requireNonNull(loc, "Location cannot be null");
        Preconditions.requireNonNull(builder, "Builder consumer cannot be null");
        if (npcType == NPCType.FAKE_PLAYER) {
            return createFakePlayer("NPC", loc, b -> builder.accept(b));
        } else {
            return createFakeEntity(EntityType.ZOMBIE, loc, b -> builder.accept(b));
        }
    }

    private Location requireSpawnableLocation(Location loc) {
        World world = loc.getWorld();
        if (world == null) {
            throw new IllegalArgumentException("NPC Location world must not be null");
        }
        world.getChunkAt(loc).load(true);
        return loc.clone();
    }

    private void requireMainThread(String operation) {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("EntityService." + operation + " must be called on the server main thread");
        }
    }

    private EntityType resolvePlaceholderType() {
        if (VersionUtil.hasArmorStand()) {
            try {
                return EntityType.valueOf("ARMOR_STAND");
            } catch (IllegalArgumentException ignored) {
            }
        }
        return EntityType.ZOMBIE;
    }

    private EntityType resolveSpawnableType(EntityType requested) {
        try {
            EntityType.valueOf(requested.name());
            return requested;
        } catch (IllegalArgumentException ignored) {
            return EntityType.ZOMBIE;
        }
    }
}
