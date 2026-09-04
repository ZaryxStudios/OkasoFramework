package com.zaryxstudios.okaso.entity;

import com.zaryxstudios.okaso.common.entity.*;
import com.zaryxstudios.okaso.common.task.TaskHandle;
import com.zaryxstudios.okaso.common.task.TaskScheduler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BukkitNPCMovementController implements NPCMovementController {

    private final TaskScheduler scheduler;
    private final Map<UUID, PatrolState> patrols = new ConcurrentHashMap<>();
    private final Map<UUID, TaskHandle> tasks = new ConcurrentHashMap<>();

    public BukkitNPCMovementController(TaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void startPatrol(NPCHandle handle, List<NPCWaypoint> waypoints, boolean loop) {
        stopPatrol(handle);
        PatrolState state = new PatrolState(waypoints, loop, 0);
        patrols.put(handle.getUniqueId(), state);
        scheduleNext(handle, state);
    }

    @Override
    public void stopPatrol(NPCHandle handle) {
        patrols.remove(handle.getUniqueId());
        TaskHandle task = tasks.remove(handle.getUniqueId());
        if (task != null) task.cancel();
    }

    @Override
    public void moveTo(NPCHandle handle, Location location, double speed) {
        Entity entity = getEntity(handle);
        if (entity == null) return;
        final double moveSpeed = speed <= 0 ? 0.2 : speed;

        scheduler.runTimer(() -> {
            if (entity == null || !entity.isValid()) return;
            Location current = entity.getLocation();
            double dist = current.distance(location);
            if (dist < 0.5) return;

            double dx = location.getX() - current.getX();
            double dy = location.getY() - current.getY();
            double dz = location.getZ() - current.getZ();
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len < 0.01) return;

            Location next = current.clone().add(dx / len * moveSpeed, dy / len * moveSpeed, dz / len * moveSpeed);
            next.setYaw((float) Math.toDegrees(Math.atan2(-dx, dz)));
            entity.teleport(next);
        }, 0L, 1L, TimeUnit.MILLISECONDS);
    }

    @Override
    public void lookAt(NPCHandle handle, Location location) {
        Entity entity = getEntity(handle);
        if (entity == null) return;
        Location current = entity.getLocation();
        double dx = location.getX() - current.getX();
        double dy = location.getY() - current.getY();
        double dz = location.getZ() - current.getZ();
        double hDist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) Math.toDegrees(Math.atan2(-dy, hDist));
        current.setYaw(yaw);
        current.setPitch(pitch);
        entity.teleport(current);
    }

    @Override
    public boolean isMoving(NPCHandle handle) {
        return patrols.containsKey(handle.getUniqueId());
    }

    @Override
    public List<NPCWaypoint> getWaypoints(NPCHandle handle) {
        PatrolState state = patrols.get(handle.getUniqueId());
        return state != null ? Collections.unmodifiableList(state.waypoints) : Collections.emptyList();
    }

    private void scheduleNext(NPCHandle handle, PatrolState state) {
        if (state.currentIndex >= state.waypoints.size()) {
            if (state.loop) {
                state.currentIndex = 0;
            } else {
                stopPatrol(handle);
                return;
            }
        }

        NPCWaypoint wp = state.waypoints.get(state.currentIndex);
        Entity entity = getEntity(handle);
        if (entity == null) { stopPatrol(handle); return; }

        TaskHandle task = scheduler.runLater(() -> {
            moveTo(handle, wp.getLocation(), wp.getSpeed());
            lookAt(handle, wp.getLocation());
            state.currentIndex++;
            if (patrols.containsKey(handle.getUniqueId())) {
                scheduler.runLater(() -> scheduleNext(handle, state), wp.getDelayTicks() + 20L, TimeUnit.MILLISECONDS);
            }
        }, 50L, TimeUnit.MILLISECONDS);
        tasks.put(handle.getUniqueId(), task);
    }

    private Entity getEntity(NPCHandle handle) {
        if (handle instanceof PacketNPCHandle) {
            return ((PacketNPCHandle) handle).getEntity();
        }
        return null;
    }

    private static class PatrolState {
        final List<NPCWaypoint> waypoints;
        final boolean loop;
        int currentIndex;

        PatrolState(List<NPCWaypoint> waypoints, boolean loop, int startIndex) {
            this.waypoints = new ArrayList<>(waypoints);
            this.loop = loop;
            this.currentIndex = startIndex;
        }
    }
}