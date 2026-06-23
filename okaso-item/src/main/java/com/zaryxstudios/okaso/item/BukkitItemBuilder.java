package com.zaryxstudios.okaso.item;

import com.zaryxstudios.okaso.common.item.ItemBuilder;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private BukkitItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.meta = itemStack.getItemMeta();
    }

    public BukkitItemBuilder(Material material, int amount) {
        this.itemStack = new ItemStack(material, amount);
        this.meta = itemStack.getItemMeta();
    }

    public BukkitItemBuilder(Material material) {
        this(material, 1);
    }

    public static BukkitItemBuilder of(Material material) {
        return new BukkitItemBuilder(material);
    }

    public static BukkitItemBuilder of(Material material, int amount) {
        return new BukkitItemBuilder(material, amount);
    }

    public static BukkitItemBuilder named(String name, Material material) {
        BukkitItemBuilder builder = new BukkitItemBuilder(material);
        builder.name(name);
        return builder;
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
            List<String> colored = new ArrayList<>();
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

    public ItemBuilder type(Material material) {
        itemStack.setType(material);
        this.meta = itemStack.getItemMeta();
        return this;
    }

    public ItemBuilder damage(short damage) {
        return durability(damage);
    }

    public ItemBuilder customModelData(int data) {
        if (meta != null) {
            try {
                Method setCustomModelData = ItemMeta.class.getMethod("setCustomModelData", Integer.TYPE);
                setCustomModelData.invoke(meta, data);
            } catch (Exception ignored) {
            }
        }
        return this;
    }

    public ItemBuilder addLoreLine(String line) {
        if (meta == null || line == null) return this;
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(ChatColor.translateAlternateColorCodes('&', line));
        meta.setLore(lore);
        return this;
    }

    public ItemBuilder setLoreLine(int index, String line) {
        if (meta == null || line == null) return this;
        List<String> lore = meta.getLore();
        if (lore != null && index >= 0 && index < lore.size()) {
            lore.set(index, ChatColor.translateAlternateColorCodes('&', line));
            meta.setLore(lore);
        }
        return this;
    }

    public ItemBuilder insertLoreLine(int index, String line) {
        if (meta == null || line == null) return this;
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        if (index >= 0 && index <= lore.size()) {
            lore.add(index, ChatColor.translateAlternateColorCodes('&', line));
            meta.setLore(lore);
        }
        return this;
    }

    public ItemBuilder removeLoreLine(int index) {
        if (meta == null) return this;
        List<String> lore = meta.getLore();
        if (lore != null && index >= 0 && index < lore.size()) {
            lore.remove(index);
            meta.setLore(lore);
        }
        return this;
    }

    public ItemBuilder clearLore() {
        if (meta != null) {
            meta.setLore(null);
        }
        return this;
    }

    public ItemBuilder skullOwner(String name) {
        if (meta instanceof SkullMeta && name != null) {
            try {
                Method setOwner = SkullMeta.class.getMethod("setOwner", String.class);
                setOwner.invoke(meta, name);
            } catch (Exception ignored) {
            }
        }
        return this;
    }

    public ItemBuilder leatherColor(int r, int g, int b) {
        if (meta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta) meta).setColor(Color.fromRGB(r, g, b));
        }
        return this;
    }

    public ItemBuilder leatherColor(Color color) {
        if (meta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta) meta).setColor(color);
        }
        return this;
    }

    public ItemBuilder removeEnchantment(String enchantment) {
        if (meta != null && enchantment != null) {
            Enchantment ench = Enchantment.getByName(enchantment.toUpperCase());
            if (ench != null) {
                meta.removeEnchant(ench);
            }
        }
        return this;
    }

    public ItemBuilder clearEnchantments() {
        if (meta != null) {
            for (Enchantment ench : meta.getEnchants().keySet()) {
                meta.removeEnchant(ench);
            }
        }
        return this;
    }

    public ItemBuilder flags(String... flags) {
        if (meta == null || flags == null) return this;
        try {
            Class<?> itemFlagClass = Class.forName("org.bukkit.inventory.ItemFlag");
            Method valueOf = itemFlagClass.getMethod("valueOf", String.class);
            Method addItemFlags = ItemMeta.class.getMethod("addItemFlags", itemFlagClass);
            for (String flag : flags) {
                if (flag != null) {
                    try {
                        Object itemFlag = valueOf.invoke(null, flag.toUpperCase());
                        addItemFlags.invoke(meta, itemFlag);
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return this;
    }

    public BukkitItemBuilder copy() {
        BukkitItemBuilder cloned = new BukkitItemBuilder(itemStack.clone());
        cloned.meta = cloned.itemStack.getItemMeta();
        return cloned;
    }
}
