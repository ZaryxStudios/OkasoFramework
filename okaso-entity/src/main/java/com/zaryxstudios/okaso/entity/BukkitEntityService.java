package com.zaryxstudios.okaso.entity;

import com.zaryxstudios.okaso.common.entity.EntityService;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class BukkitEntityService implements EntityService {

    private static final boolean HAS_GET_ENTITY_UUID;
    private static final Method GET_ENTITY_UUID_METHOD;

    static {
        boolean hasMethod = false;
        Method method = null;
        try {
            method = Bukkit.class.getMethod("getEntity", UUID.class);
            hasMethod = true;
        } catch (NoSuchMethodException ignored) {
        }
        HAS_GET_ENTITY_UUID = hasMethod;
        GET_ENTITY_UUID_METHOD = method;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<T> getEntitiesInWorld(Object world, Class<T> type) {
        if (world instanceof World) {
            java.util.List<T> result = new ArrayList<T>();
            for (Entity entity : ((World) world).getEntities()) {
                if (type.isInstance(entity)) {
                    result.add((T) entity);
                }
            }
            return result;
        }
        return new ArrayList<T>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<T> getNearbyEntities(Object location, double radius, Class<T> type) {
        if (location instanceof Location) {
            java.util.List<T> result = new ArrayList<T>();
            for (Entity entity : ((Location) location).getWorld().getEntities()) {
                if (type.isInstance(entity)) {
                    Location loc = entity.getLocation();
                    if (loc.distanceSquared((Location) location) <= radius * radius) {
                        result.add((T) entity);
                    }
                }
            }
            return result;
        }
        return new ArrayList<T>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Object> getEntity(UUID uuid) {
        if (HAS_GET_ENTITY_UUID) {
            try {
                Entity entity = (Entity) GET_ENTITY_UUID_METHOD.invoke(null, uuid);
                return Optional.ofNullable(entity);
            } catch (Exception ignored) {
            }
        }
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getUniqueId().equals(uuid)) {
                    return Optional.of(entity);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isValid(Object entity) {
        if (entity instanceof Entity) {
            return ((Entity) entity).isValid();
        }
        return false;
    }

    @Override
    public void remove(Object entity) {
        if (entity instanceof Entity) {
            ((Entity) entity).remove();
        }
    }

    @Override
    public void teleport(Object entity, Object location) {
        if (entity instanceof Entity && location instanceof Location) {
            ((Entity) entity).teleport((Location) location);
        }
    }

    @Override
    public Object getLocation(Object entity) {
        if (entity instanceof Entity) {
            return ((Entity) entity).getLocation();
        }
        return null;
    }

    @Override
    public Object getWorld(Object entity) {
        if (entity instanceof Entity) {
            return ((Entity) entity).getWorld();
        }
        return null;
    }

    @Override
    public String getName(Object entity) {
        if (entity instanceof Entity) {
            String customName = ((Entity) entity).getCustomName();
            if (customName != null) return customName;
            try {
                return (String) entity.getClass().getMethod("getName").invoke(entity);
            } catch (Exception ignored) {
            }
        }
        return "";
    }

    @Override
    public void setFire(Object entity, int ticks) {
        if (entity instanceof Entity) {
            ((Entity) entity).setFireTicks(ticks);
        }
    }

    @Override
    public String getType(Object entity) {
        if (entity instanceof Entity) {
            return ((Entity) entity).getType().name();
        }
        return "";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Object> getPassengers(Object entity) {
        if (entity instanceof Entity) {
            return (Collection<Object>) (Collection<?>) ((Entity) entity).getPassengers();
        }
        return new ArrayList<Object>();
    }
}
