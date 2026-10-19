package com.zaryxstudios.okaso.common.compat;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * Bukkit calls whose API changed after 1.8.8. Each method uses the modern API and falls back
 * to the 1.8 equivalent (or does nothing) when the server does not provide it.
 */
public final class BukkitCompat {

    private BukkitCompat() {}

    @CompatGuarded(since = "1.9")
    public static void setGlowing(Entity entity, boolean glowing) {
        try {
            entity.setGlowing(glowing);
        } catch (LinkageError ignored) {
        }
    }

    @CompatGuarded(since = "1.9")
    public static void setInvulnerable(Entity entity, boolean invulnerable) {
        try {
            entity.setInvulnerable(invulnerable);
        } catch (LinkageError ignored) {
        }
    }

    @CompatGuarded(since = "1.9")
    public static void setSilent(Entity entity, boolean silent) {
        try {
            entity.setSilent(silent);
        } catch (LinkageError ignored) {
        }
    }

    @CompatGuarded(since = "1.10")
    public static void setGravity(Entity entity, boolean gravity) {
        try {
            entity.setGravity(gravity);
        } catch (LinkageError ignored) {
        }
    }

    @CompatGuarded(since = "1.9")
    public static void setCollidable(LivingEntity entity, boolean collidable) {
        try {
            entity.setCollidable(collidable);
        } catch (LinkageError ignored) {
        }
    }

    @CompatGuarded(since = "1.9")
    public static void setAI(LivingEntity entity, boolean ai) {
        try {
            entity.setAI(ai);
        } catch (LinkageError ignored) {
        }
    }

    @CompatGuarded(since = "1.17")
    public static void setUnlimitedLifetime(Item item, boolean unlimited) {
        try {
            item.setUnlimitedLifetime(unlimited);
        } catch (LinkageError ignored) {
        }
    }

    @SuppressWarnings("deprecation")
    @CompatGuarded(since = "1.11")
    public static List<Entity> getPassengers(Entity entity) {
        try {
            return entity.getPassengers();
        } catch (LinkageError e) {
            Entity passenger = entity.getPassenger();
            return passenger == null ? Collections.emptyList() : Collections.singletonList(passenger);
        }
    }

    @SuppressWarnings("deprecation")
    @CompatGuarded(since = "1.9")
    public static void setItemInMainHand(EntityEquipment equipment, ItemStack item) {
        try {
            equipment.setItemInMainHand(item);
        } catch (LinkageError e) {
            equipment.setItemInHand(item);
        }
    }

    @CompatGuarded(since = "1.9")
    public static void setItemInOffHand(EntityEquipment equipment, ItemStack item) {
        try {
            equipment.setItemInOffHand(item);
        } catch (LinkageError ignored) {
        }
    }

    @SuppressWarnings("deprecation")
    @CompatGuarded(since = "1.9")
    public static ItemStack getItemInMainHand(PlayerInventory inventory) {
        try {
            return inventory.getItemInMainHand();
        } catch (LinkageError e) {
            return inventory.getItemInHand();
        }
    }

    @SuppressWarnings("deprecation")
    @CompatGuarded(since = "1.9")
    public static double getMaxHealth(Player player) {
        try {
            AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attribute != null) return attribute.getValue();
        } catch (LinkageError ignored) {
        }
        return player.getMaxHealth();
    }

    @CompatGuarded(since = "1.14")
    public static boolean isCreatable(InventoryType type) {
        try {
            return type.isCreatable();
        } catch (LinkageError e) {
            return true;
        }
    }

    @CompatGuarded(since = "1.11")
    public static boolean isUnbreakable(ItemMeta meta) {
        try {
            return meta.isUnbreakable();
        } catch (LinkageError e) {
            Object result = invokeSpigotMeta(meta, "isUnbreakable");
            return Boolean.TRUE.equals(result);
        }
    }

    @CompatGuarded(since = "1.11")
    public static void setUnbreakable(ItemMeta meta, boolean unbreakable) {
        try {
            meta.setUnbreakable(unbreakable);
        } catch (LinkageError e) {
            invokeSpigotMeta(meta, "setUnbreakable", unbreakable);
        }
    }

    /** Returns the custom model data, or -1 when the item has none or the server predates 1.14. */
    @CompatGuarded(since = "1.14")
    public static int getCustomModelData(ItemMeta meta) {
        try {
            return meta.hasCustomModelData() ? meta.getCustomModelData() : -1;
        } catch (LinkageError e) {
            return -1;
        }
    }

    @CompatGuarded(since = "1.14")
    public static void setCustomModelData(ItemMeta meta, int data) {
        try {
            meta.setCustomModelData(data);
        } catch (LinkageError ignored) {
        }
    }

    @CompatGuarded(since = "1.11")
    public static void setPotionColor(PotionMeta meta, Color color) {
        try {
            meta.setColor(color);
        } catch (LinkageError ignored) {
        }
    }

    @SuppressWarnings("deprecation")
    @CompatGuarded(since = "1.9")
    public static void setBasePotion(PotionMeta meta, PotionType type, boolean extended, boolean upgraded) {
        try {
            meta.setBasePotionData(new PotionData(type, extended, upgraded));
        } catch (LinkageError | IllegalArgumentException ignored) {
        }
    }

    public static boolean isPlayerHead(Material material) {
        if (material == null) return false;
        String name = material.name();
        return name.equals("PLAYER_HEAD") || name.equals("PLAYER_WALL_HEAD")
            || name.equals("SKULL_ITEM") || name.equals("SKULL");
    }

    /** Resolves a material by its modern name, falling back to the legacy name on old servers. */
    public static Material material(String name) {
        for (String candidate : CompatibilityNames.materials(name)) {
            Material material = Material.getMaterial(candidate);
            if (material != null) return material;
        }
        return null;
    }

    /**
     * Creates an item from its modern material name. On servers before 1.13 the legacy material
     * is used with its data value (e.g. LIME_WOOL becomes WOOL:5). Falls back to STONE.
     */
    @SuppressWarnings("deprecation")
    public static ItemStack item(String name, int amount) {
        List<String> candidates = CompatibilityNames.materials(name);
        for (int i = 0; i < candidates.size(); i++) {
            Material material = Material.getMaterial(candidates.get(i));
            if (material == null) continue;
            if (i == 0) return new ItemStack(material, amount);
            return new ItemStack(material, amount, CompatibilityNames.legacyData(name));
        }
        return new ItemStack(Material.STONE, amount);
    }

    public static ItemStack item(String name) {
        return item(name, 1);
    }

    private static Object invokeSpigotMeta(ItemMeta meta, String method, Object... args) {
        try {
            Object spigot = meta.getClass().getMethod("spigot").invoke(meta);
            for (Method candidate : spigot.getClass().getMethods()) {
                if (candidate.getName().equals(method) && candidate.getParameterCount() == args.length) {
                    candidate.setAccessible(true);
                    return candidate.invoke(spigot, args);
                }
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
        return null;
    }
}
