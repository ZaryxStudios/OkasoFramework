package com.zaryxstudios.okaso.common.entity;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface EntityService {
    <T> Collection<T> getEntitiesInWorld(Object world, Class<T> type);
    <T> Collection<T> getNearbyEntities(Object location, double radius, Class<T> type);
    Optional<Object> getEntity(UUID uuid);
    boolean isValid(Object entity);
    void remove(Object entity);
    void teleport(Object entity, Object location);
    Object getLocation(Object entity);
    Object getWorld(Object entity);
    String getName(Object entity);
    void setFire(Object entity, int ticks);
    String getType(Object entity);
    Collection<Object> getPassengers(Object entity);
}
