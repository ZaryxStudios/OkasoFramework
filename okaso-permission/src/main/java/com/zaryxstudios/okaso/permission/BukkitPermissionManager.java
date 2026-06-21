package com.zaryxstudios.okaso.permission;

import com.zaryxstudios.okaso.common.permission.PermissionManager;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;

public class BukkitPermissionManager implements PermissionManager {

    private final Plugin plugin;
    private final Set<String> registeredNodes;

    public BukkitPermissionManager(Plugin plugin) {
        this.plugin = plugin;
        this.registeredNodes = new HashSet<String>();
    }

    @Override
    public boolean hasPermission(Object player, String permission) {
        if (player instanceof org.bukkit.command.CommandSender) {
            return ((org.bukkit.command.CommandSender) player).hasPermission(permission);
        }
        return false;
    }

    @Override
    public void registerPermission(String node, String description, String parent) {
        if (registeredNodes.contains(node)) return;

        Permission perm = new Permission(node, description != null ? description : "");
        if (parent != null && !parent.isEmpty()) {
            Permission parentPerm = Bukkit.getPluginManager().getPermission(parent);
            if (parentPerm != null) {
                parentPerm.getChildren().put(node, true);
                parentPerm.recalculatePermissibles();
            }
        }
        try {
            Bukkit.getPluginManager().addPermission(perm);
            registeredNodes.add(node);
        } catch (IllegalArgumentException e) {
        }
    }

    @Override
    public boolean isRegistered(String node) {
        return Bukkit.getPluginManager().getPermission(node) != null;
    }

    @Override
    public java.util.Set<String> getPermissions() {
        return java.util.Collections.unmodifiableSet(registeredNodes);
    }

    @Override
    public void addPermission(Object player, String permission) {
        if (player instanceof org.bukkit.entity.Player) {
            ((org.bukkit.entity.Player) player).addAttachment(plugin, permission, true);
        }
    }

    @Override
    public void removePermission(Object player, String permission) {
        if (player instanceof org.bukkit.entity.Player) {
            ((org.bukkit.entity.Player) player).addAttachment(plugin, permission, false);
        }
    }
}
