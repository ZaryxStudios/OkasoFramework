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
        Entity base = spawnPlaceholder(loc);
        FakePlayerBuilderImpl impl = new FakePlayerBuilderImpl(base);
        builder.accept(impl);
        impl.apply();
        return new PacketNPCHandle(base, true);
    }

    @Override
    public NPCHandle createFakeEntity(EntityType type, Location loc, Consumer<FakeEntityBuilder> builder) {
        Preconditions.requireNonNull(type, "EntityType cannot be null");
        Preconditions.requireNonNull(loc, "Location cannot be null");
        Preconditions.requireNonNull(builder, "Builder consumer cannot be null");
        Entity base = spawnEntity(type, loc);
        FakeEntityBuilderImpl impl = new FakeEntityBuilderImpl(base);
        builder.accept(impl);
        impl.apply();
        return new PacketNPCHandle(base, false);
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

    private Entity spawnPlaceholder(Location loc) {
        if (VersionUtil.hasArmorStand()) {
            return loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        }
        Entity z = loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
        AbstractNPCBuilder.safeInvoke(z, "setAI", false);
        AbstractNPCBuilder.safeInvoke(z, "setGravity", false);
        AbstractNPCBuilder.safeInvoke(z, "setSilent", true);
        AbstractNPCBuilder.safeInvoke(z, "setInvulnerable", true);
        AbstractNPCBuilder.safeInvoke(z, "setCustomNameVisible", true);
        z.setCustomName("\u00a7r");
        return z;
    }

    private Entity spawnEntity(EntityType type, Location loc) {
        return loc.getWorld().spawnEntity(loc, type);
    }
}