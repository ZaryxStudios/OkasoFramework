package com.zaryxstudios.okaso.hologram;

import com.zaryxstudios.okaso.common.hologram.OkasoHologram;
import com.zaryxstudios.okaso.common.hologram.HologramLine;
import com.zaryxstudios.okaso.common.hologram.HologramLineType;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;

public class OkasoBukkitHologram implements OkasoHologram {

    @Getter
    private final String id;
    private final List<HologramLine> lines;
    private final List<Entity> entities;
    private Location location;
    private boolean active;

    public OkasoBukkitHologram(String id, Location location, List<HologramLine> lines) {
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
        if (newLines != null) {
            lines.addAll(newLines);
        }
        if (active) refresh();
    }

    @Override
    public void setLine(int index, HologramLine line) {
        if (index < 0 || index >= lines.size() || line == null) return;
        lines.set(index, line);
        if (active) refresh();
    }

    @Override
    public void addLine(HologramLine line) {
        if (line == null) return;
        lines.add(line);
        if (active) {
            int idx = lines.size() - 1;
            spawnEntityForLine(idx, line, lineLocation(idx));
        }
    }

    @Override
    public void insertLine(int index, HologramLine line) {
        if (index < 0 || index > lines.size() || line == null) return;
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
        if (textLines != null) {
            for (String t : textLines) {
                lines.add(HologramLine.text(t));
            }
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
        setLocation(new Location(location.getWorld(), x, y, z, yaw, pitch));
    }

    @Override
    public void setLocation(Object location) {
        if (!(location instanceof Location)) return;
        this.location = ((Location) location).clone();
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
        World world = loc.getWorld();
        if (world == null) return;
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
        } catch (Exception ignored) {
        }
    }

    private void spawnItemLine(Location loc, String materialName, int amount) {
        World world = loc.getWorld();
        if (world == null) return;

        Material mat = Material.getMaterial(materialName.toUpperCase());
        if (mat == null) mat = Material.STONE;

        ItemStack stack = new ItemStack(mat, Math.max(1, amount));

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
