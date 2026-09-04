package com.zaryxstudios.okaso.common.entity;

import org.bukkit.Location;
import java.util.List;
import java.util.UUID;

public class NPCData {
    private UUID uniqueId;
    private String id;
    private NPCType npcType;
    private String displayName;
    private Location location;
    private boolean invulnerable = true;
    private boolean hasGravity = false;
    private boolean isSilent = true;
    private boolean hasGlow = false;
    private boolean hasAI = false;
    private float yaw;
    private float pitch;
    private List<NPCWaypoint> waypoints;
    private boolean patrolLoop;

    public NPCData() {}

    public NPCData(String id, NPCType npcType, Location location) {
        this.id = id;
        this.npcType = npcType;
        this.location = location;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getId() {
        return id;

    }

    public void setId(String id) {
        this.id = id;
    }

    public NPCType getNpcType() {
        return npcType;
    }

    public void setNpcType(NPCType npcType) {
        this.npcType = npcType;
    }

    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    public boolean hasGravity() {
        return hasGravity;

    }

    public void setHasGravity(boolean hasGravity) { 
        this.hasGravity = hasGravity;
    }

    public boolean isSilent() {
        return isSilent;
    }

    public void setSilent(boolean isSilent) {
        this.isSilent = isSilent;
    }

    public boolean hasGlow() {
        return hasGlow;
    }

    public void setHasGlow(boolean hasGlow) {
        this.hasGlow = hasGlow;
    }

    public boolean hasAI() {
        return hasAI;
    }

    public void setHasAI(boolean hasAI) {
        this.hasAI = hasAI;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public List<NPCWaypoint> getWaypoints() {
        return waypoints; 
    }

    public void setWaypoints(List<NPCWaypoint> waypoints) {
        this.waypoints = waypoints;
    }

    public boolean isPatrolLoop() {
        return patrolLoop;
    }

    public void setPatrolLoop(boolean patrolLoop) {
        this.patrolLoop = patrolLoop;
    }


    @Override
    public String toString() {
        return "NPCData{id='" + id + "', type=" + npcType + ", name='" + displayName + "'}";
    }
}