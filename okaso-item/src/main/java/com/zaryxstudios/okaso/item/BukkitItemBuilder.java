package com.zaryxstudios.okaso.item;

import com.zaryxstudios.okaso.common.item.ItemBuilder;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Damageable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.enchantments.Enchantment;

public class BukkitItemBuilder implements ItemBuilder {

    private static final boolean HAS_SET_DURABILITY;
    private static final Method SET_DURABILITY_METHOD;

    static {
        boolean hasMethod = false;
        Method setDurabilityMethod = null;
        try {
            setDurabilityMethod = ItemStack.class.getMethod("setDurability", short.class);
            hasMethod = true;
        } catch (NoSuchMethodException ignored) {
        }
        HAS_SET_DURABILITY = hasMethod;
        SET_DURABILITY_METHOD = setDurabilityMethod;
    }

    private final ItemStack itemStack;
    private ItemMeta meta;

    public BukkitItemBuilder(Material material, int amount) {
        this.itemStack = new ItemStack(material, amount);
        this.meta = itemStack.getItemMeta();
    }

    public BukkitItemBuilder(Material material) {
        this(material, 1);
    }

    @Override
    public ItemBuilder name(String name) {
        if (meta != null && name != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        }
        return this;
    }

    @Override
    public ItemBuilder lore(List<String> lore) {
        if (meta != null && lore != null) {
            List<String> colored = new ArrayList<String>();
            for (String line : lore) {
                colored.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(colored);
        }
        return this;
    }

    @Override
    public ItemBuilder lore(String... lines) {
        if (lines != null) {
            return lore(Arrays.asList(lines));
        }
        return this;
    }

    @Override
    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    @Override
    public ItemBuilder durability(short durability) {
        if (HAS_SET_DURABILITY) {
            try {
                SET_DURABILITY_METHOD.invoke(itemStack, durability);
            } catch (Exception ignored) {
            }
        } else {
            ItemMeta im = itemStack.getItemMeta();
            if (im instanceof Damageable) {
                ((Damageable) im).setDamage(durability);
                itemStack.setItemMeta(im);
            }
        }
        return this;
    }

    @Override
    public ItemBuilder enchant(String enchantment, int level) {
        if (meta != null && enchantment != null) {
            Enchantment ench = Enchantment.getByName(enchantment.toUpperCase());
            if (ench != null) {
                meta.addEnchant(ench, level, true);
            }
        }
        return this;
    }

    @Override
    public ItemBuilder glow() {
        if (meta != null) {
            Enchantment ench = Enchantment.getByName("DURABILITY");
            if (ench != null) {
                meta.addEnchant(ench, 1, true);
            }
            try {
                Class<?> itemFlagClass = Class.forName("org.bukkit.inventory.ItemFlag");
                Method valueOf = itemFlagClass.getMethod("valueOf", String.class);
                Object hideEnchants = valueOf.invoke(null, "HIDE_ENCHANTS");
                Method addItemFlags = ItemMeta.class.getMethod("addItemFlags", itemFlagClass);
                addItemFlags.invoke(meta, hideEnchants);
            } catch (Exception ignored) {
            }
        }
        return this;
    }

    @Override
    public ItemBuilder unbreakable(boolean unbreakable) {
        if (meta != null) {
            try {
                Method setUnbreakable = ItemMeta.class.getMethod("setUnbreakable", boolean.class);
                setUnbreakable.invoke(meta, unbreakable);
            } catch (Exception ignored) {
                try {
                    Object spigot = ItemMeta.class.getMethod("spigot").invoke(meta);
                    Method setUnbreakableSpigot = spigot.getClass().getMethod("setUnbreakable", boolean.class);
                    setUnbreakableSpigot.invoke(spigot, unbreakable);
                } catch (Exception ignored2) {
                }
            }
        }
        return this;
    }

    @Override
    public Object build() {
        if (meta != null) {
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }
}
