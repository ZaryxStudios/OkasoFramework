package com.zaryxstudios.okaso.hologram;

import com.zaryxstudios.okaso.common.compat.CompatibilityNames;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

final class BukkitCompatibilityResolver {

    private BukkitCompatibilityResolver() {}

    static Material material(String name) {
        for (String candidate : CompatibilityNames.materials(name)) {
            Material material = Material.getMaterial(candidate);
            if (material != null) return material;
        }
        return null;
    }

    static EntityType entity(String name) {
        for (String candidate : CompatibilityNames.entities(name)) {
            try {
                return EntityType.valueOf(candidate);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }
}