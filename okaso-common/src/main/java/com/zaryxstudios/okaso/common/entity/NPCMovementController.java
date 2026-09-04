package com.zaryxstudios.okaso.common.entity;

import java.util.List;
import org.bukkit.Location;

public interface NPCMovementController {
    void startPatrol(NPCHandle handle, List<NPCWaypoint> waypoints, boolean loop);
    void stopPatrol(NPCHandle handle);
    void moveTo(NPCHandle handle, Location location, double speed);
    void lookAt(NPCHandle handle, Location location);
    boolean isMoving(NPCHandle handle);
    List<NPCWaypoint> getWaypoints(NPCHandle handle);
}