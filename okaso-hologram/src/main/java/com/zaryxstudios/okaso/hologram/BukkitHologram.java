package com.zaryxstudios.okaso.hologram;

import com.zaryxstudios.okaso.common.hologram.Hologram;

import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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

    static {
        Class<?> asClass = null;
        Method spawn = null, vis = null, grav = null, pickup = null,
               nameVis = null, cname = null, marker = null, remove = null,
               teleport = null;

        try {
            asClass = Class.forName("org.bukkit.entity.ArmorStand");

            spawn = World.class.getMethod("spawn", Location.class, Class.class);

            vis     = asClass.getMethod("setVisible", boolean.class);
            grav    = asClass.getMethod("setGravity", boolean.class);
            pickup  = asClass.getMethod("setCanPickupItems", boolean.class);
            nameVis = asClass.getMethod("setCustomNameVisible", boolean.class);
            cname   = asClass.getMethod("setCustomName", String.class);
            marker  = asClass.getMethod("setMarker", boolean.class);
            remove  = asClass.getMethod("remove");
            teleport = asClass.getMethod("teleport", Location.class);

        } catch (Exception ignored) {
            asClass = null;
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
    }

    @Getter
    private final String id;
    private final List<String> lines;
    private final List<Object> entities;
    private Location location;
    private boolean active;

    public BukkitHologram(String id, Location location, List<String> lines) {
        this.id = id;
        this.location = location.clone();
        this.lines = new ArrayList<>(lines);
        this.entities = new ArrayList<>();
        this.active = false;
    }

    @Override
    public List<String> getLines() {
        return new ArrayList<>(lines);
    }

    @Override
    public void setLines(List<String> lines) {
        this.lines.clear();
        this.lines.addAll(lines);
        if (active) {
            refresh();
        }
    }

    @Override
    public void setLine(int index, String text) {
        if (index >= 0 && index < lines.size()) {
            lines.set(index, text);
            if (active && index < entities.size()) {
                try {
                    SET_CUSTOM_NAME.invoke(entities.get(index), text);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public void addLine(String text) {
        lines.add(text);
        if (active) {
            int index = lines.size() - 1;
            Location lineLoc = location.clone().add(0, -0.25 * index, 0);
            spawnLine(index, lineLoc);
        }
    }

    @Override
    public void removeLine(int index) {
        if (index >= 0 && index < lines.size()) {
            lines.remove(index);
            if (active) {
                refresh();
            }
        }
    }

    @Override
    public void insertLine(int index, String text) {
        if (index >= 0 && index <= lines.size()) {
            lines.add(index, text);
            if (active) {
                refresh();
            }
        }
    }

    @Override
    public int getLineCount() {
        return lines.size();
    }

    @Override
    public void clearLines() {
        lines.clear();
        if (active) {
            refresh();
        }
    }

    @Override
    public void teleport(double x, double y, double z, float yaw, float pitch) {
        Location oldLoc = this.location;
        this.location = new Location(oldLoc.getWorld(), x, y, z, yaw, pitch);
        if (active && ENTITY_TELEPORT != null) {
            for (int i = 0; i < entities.size(); i++) {
                Location lineLoc = this.location.clone().add(0, -0.25 * i, 0);
                try {
                    ENTITY_TELEPORT.invoke(entities.get(i), lineLoc);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public void start() {
        if (active || !HAS_ARMOR_STAND) return;
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

    public Location getLocation() {
        return location.clone();
    }

    private void spawnAll() {
        entities.clear();
        for (int i = 0; i < lines.size(); i++) {
            Location lineLoc = location.clone().add(0, -0.25 * i, 0);
            spawnLine(i, lineLoc);
        }
    }

    private void spawnLine(int index, Location loc) {
        if (!HAS_ARMOR_STAND) return;
        World world = loc.getWorld();
        if (world == null) return;

        try {
            Object stand = WORLD_SPAWN.invoke(world, loc, ARMOR_STAND_CLASS);
            SET_VISIBLE.invoke(stand, false);
            SET_GRAVITY.invoke(stand, false);
            SET_PICKUP.invoke(stand, false);
            SET_NAME_VISIBLE.invoke(stand, true);
            SET_CUSTOM_NAME.invoke(stand, lines.get(index));
            SET_MARKER.invoke(stand, true);
            entities.add(stand);
        } catch (Exception ignored) {
        }
    }

    private void despawnAll() {
        for (Object stand : entities) {
            try {
                ENTITY_REMOVE.invoke(stand);
            } catch (Exception ignored) {
            }
        }
        entities.clear();
    }

    private void refresh() {
        despawnAll();
        spawnAll();
    }
}
