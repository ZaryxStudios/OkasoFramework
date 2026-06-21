package com.zaryxstudios.okaso.common.permission;

import java.util.Set;

public interface PermissionManager {
    boolean hasPermission(Object player, String permission);
    void registerPermission(String node, String description, String parent);
    boolean isRegistered(String node);
    java.util.Set<String> getPermissions();
    void addPermission(Object player, String permission);
    void removePermission(Object player, String permission);
}
