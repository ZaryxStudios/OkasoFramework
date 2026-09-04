package com.zaryxstudios.okaso.hologram;

import com.zaryxstudios.okaso.common.hologram.OkasoHologram;
import com.zaryxstudios.okaso.common.hologram.HologramLine;
import com.zaryxstudios.okaso.common.hologram.HologramLineType;
import com.zaryxstudios.okaso.common.hologram.HologramRenderer;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;

public class OkasoBukkitHologram implements OkasoHologram {

    public static final double DEFAULT_LINE_SPACING = 0.30;
    @Getter
    private final String id;
    private final List<HologramLine> lines;
    private final List<Object> spawnedEntities;
    private final HologramRenderer renderer;
    private Location location;
    private boolean active;
    private double lineSpacing;

    public OkasoBukkitHologram(String id, Location location, List<HologramLine> lines, HologramRenderer renderer) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Hologram id cannot be null or empty");
        }
        this.id = id;
        this.location = location != null ? location.clone() : null;
        this.lines = new ArrayList<>();
        if (lines != null) {
            for (HologramLine l : lines) {
                if (l != null) {
                    this.lines.add(l);
                }
            }
        }
        this.spawnedEntities = Collections.synchronizedList(new ArrayList<>());
        this.renderer = renderer;
        this.active = false;
        this.lineSpacing = DEFAULT_LINE_SPACING;
    }

    public HologramRenderer getRenderer() {
        return renderer;
    }

    @Override
    public List<HologramLine> getLines() {
        return Collections.unmodifiableList(new ArrayList<>(lines));
    }

    @Override
    public void setLines(List<HologramLine> newLines) {
        lines.clear();
        if (newLines != null) {
            for (HologramLine l : newLines) {
                if (l != null){
                    lines.add(l);
                }
            }
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
            spawnEntityForLine(lines.size() - 1, line, lineLocation(lines.size() - 1));
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
        lines.clear(); if (active) refresh();
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
    public void setLocation(Object loc) {
        if (!(loc instanceof Location)) {
            return;
        }
        Location newLoc = ((Location) loc).clone();
        if (newLoc.getWorld() == null && this.location != null) {
            newLoc.setWorld(this.location.getWorld());
        }
        this.location = newLoc;
        if (active) {
            synchronized (spawnedEntities) {
                for (int i = 0; i < spawnedEntities.size(); i++) {
                    Object obj = spawnedEntities.get(i);
                    if (obj instanceof Entity) ((Entity) obj).teleport(lineLocation(i));
                }
            }
        }
    }

    public Location getLocation() {
        return location != null ? location.clone() : null;
    }

    @Override
    public void start() {
        if (active || location == null || location.getWorld() == null) return;
        active = true;
        spawnAll();
    }

    @Override
    public void stop() {
        if (!active) return;
        active = false;
        renderer.despawnAll();
        spawnedEntities.clear();
    }

    @Override
    public boolean isRunning() {
        return active;
    }

    public double getLineSpacing() {
        return lineSpacing;
    }

    public void setLineSpacing(double spacing) {
        if (spacing <= 0 || spacing == lineSpacing) return;
        this.lineSpacing = spacing;
        if (active) refresh();
    }

    public double getHeight() {
        return lines.size() * lineSpacing;
    }

    public int getEntityCount() {
        synchronized (spawnedEntities) {
            return spawnedEntities.size();
        }
        }

    public void moveUp(double amount) {
        if (amount > 0) {
            setLocation(location.clone().add(0, amount, 0));
        }
    }

    public void moveDown(double amount) {
        if (amount > 0) {
            setLocation(location.clone().subtract(0, amount, 0));
        }
    }

    private Location lineLocation(int index) {
        return location.clone().add(0, -lineSpacing * index, 0);
    }

    private void spawnAll() {
        synchronized (spawnedEntities) {
            spawnedEntities.clear();
            for (int i = 0; i < lines.size(); i++) spawnEntityForLine(i, lines.get(i), lineLocation(i));
        }
    }

    private void spawnEntityForLine(int index, HologramLine line, Location loc) {
        Object spawned = null;
        switch (line.getType()) {
            case TEXT:
                spawned = renderer.spawnText(loc, line.getText());
                break;
            case ITEM:
                if (renderer.supportsLineType(HologramLineType.ITEM)) {
                    spawned = renderer.spawnItem(loc, line.getItemMaterial(), line.getItemAmount());
                } else {
                    spawned = renderer.spawnText(loc, "[" + line.getItemMaterial() + " x" + line.getItemAmount() + "]");
                }
                break;
            case MOB:
                spawned = renderer.spawnMob(loc, line.getEntityType());
                break;
        }
        if (spawned != null) {
            spawnedEntities.add(spawned);
        }
    }

    private void refresh() {
        renderer.despawnAll();
        spawnedEntities.clear(); spawnAll();
    }
}