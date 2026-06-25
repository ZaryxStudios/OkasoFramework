package com.zaryxstudios.okaso.gui;

import com.zaryxstudios.okaso.common.gui.GUIClickEvent;
import com.zaryxstudios.okaso.common.gui.GUIItem;
import com.zaryxstudios.okaso.common.text.TextColorizer;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

public class BukkitGUIItem implements GUIItem {

    @Getter @Setter
    private ItemStack itemStack;
    @Setter
    private GUIClickHandler clickHandler;

    @FunctionalInterface
    public interface GUIClickHandler {
        void onClick(GUIClickEvent event);
    }

    public BukkitGUIItem(ItemStack itemStack, GUIClickHandler clickHandler) {
        this.itemStack = itemStack;
        this.clickHandler = clickHandler;
    }

    public BukkitGUIItem(ItemStack itemStack) {
        this(itemStack, null);
    }

    @Override
    public void onClick(GUIClickEvent event) {
        if (clickHandler != null) {
            clickHandler.onClick(event);
        }
    }

    public static BukkitGUIItem of(ItemStack itemStack, GUIClickHandler clickHandler) {
        return new BukkitGUIItem(itemStack, clickHandler);
    }

    public static BukkitGUIItem of(ItemStack itemStack) {
        return new BukkitGUIItem(itemStack);
    }

    public static BukkitGUIItem of(Material material) {
        return new BukkitGUIItem(new ItemStack(material));
    }

    public static BukkitGUIItem of(Material material, int amount) {
        return new BukkitGUIItem(new ItemStack(material, amount));
    }

    public static BukkitGUIItem of(Material material, String displayName) {
        return builder(material).name(displayName).build();
    }

    public static BukkitGUIItem empty() {
        return new BukkitGUIItem(new ItemStack(Material.AIR));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Material material) {
        return new Builder().material(material);
    }

    public static Builder builder(ItemStack itemStack) {
        return new Builder().itemStack(itemStack);
    }

    public static Builder builder(Material material, int amount) {
        return new Builder().material(material).amount(amount);
    }

    public static final class Builder {
        private Material material = Material.STONE;
        private int amount = 1;
        private String displayName;
        private List<String> lore;
        private Map<Enchantment, Integer> enchantments = new HashMap<>();
        private boolean glow;
        private boolean unbreakable;
        private int customModelData = -1;
        private short durability = -1;
        private Collection<ItemFlag> flags = new ArrayList<>();
        private String skullOwner;
        private GUIClickHandler clickHandler;

        public Builder itemStack(ItemStack stack) {
            this.material = stack.getType();
            this.amount = stack.getAmount();
            if (stack.hasItemMeta()) {
                ItemMeta meta = stack.getItemMeta();
                if (meta.hasDisplayName()) this.displayName = meta.getDisplayName();
                if (meta.hasLore()) this.lore = meta.getLore();
                if (meta.hasEnchants()) this.enchantments.putAll(meta.getEnchants());
                this.flags.addAll(meta.getItemFlags());
                if (meta instanceof org.bukkit.inventory.meta.SkullMeta) {
                    org.bukkit.inventory.meta.SkullMeta skull = (org.bukkit.inventory.meta.SkullMeta) meta;
                    if (skull.hasOwner()) {
                        try {
                            this.skullOwner = (String) SkullMeta.class.getMethod("getOwner").invoke(skull);
                        } catch (Exception ignored) {}
                    }
                }
                try {
                    Method isUnbr = meta.getClass().getMethod("isUnbreakable");
                    this.unbreakable = (boolean) isUnbr.invoke(meta);
                } catch (Exception ignored) {}
                try {
                    Method getCmd = meta.getClass().getMethod("getCustomModelData");
                    this.customModelData = (int) getCmd.invoke(meta);
                } catch (Exception ignored) {}
            }
            return this;
        }

        private Builder() {}

        public Builder material(Material material) {
            this.material = material;
            return this;
        }

        public Builder amount(int amount) {
            this.amount = amount;
            return this;
        }

        public Builder name(String name) {
            this.displayName = name;
            return this;
        }

        public Builder lore(String... lines) {
            this.lore = new ArrayList<>(Arrays.asList(lines));
            return this;
        }

        public Builder lore(List<String> lines) {
            this.lore = new ArrayList<>(lines);
            return this;
        }

        public Builder loreLine(String line) {
            if (this.lore == null) this.lore = new ArrayList<>();
            this.lore.add(line);
            return this;
        }

        public Builder enchant(Enchantment enchantment, int level) {
            this.enchantments.put(enchantment, level);
            return this;
        }

        public Builder glow() {
            this.glow = true;
            return this;
        }

        public Builder unbreakable() {
            this.unbreakable = true;
            return this;
        }

        public Builder customModelData(int data) {
            this.customModelData = data;
            return this;
        }

        public Builder durability(short durability) {
            this.durability = durability;
            return this;
        }

        public Builder flag(ItemFlag flag) {
            this.flags.add(flag);
            return this;
        }

        public Builder skullOwner(String owner) {
            this.skullOwner = owner;
            return this;
        }

        public Builder clickHandler(GUIClickHandler handler) {
            this.clickHandler = handler;
            return this;
        }

        public BukkitGUIItem build() {
            ItemStack stack = new ItemStack(material, amount);

            if (durability >= 0) {
                stack.setDurability(durability);
            }

            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                if (displayName != null) {
                    meta.setDisplayName(TextColorizer.translate(displayName));
                }
                if (lore != null && !lore.isEmpty()) {
                    meta.setLore(lore.stream()
                        .map(TextColorizer::translate)
                        .collect(java.util.stream.Collectors.toList()));
                }
                if (!enchantments.isEmpty()) {
                    for (Map.Entry<Enchantment, Integer> e : enchantments.entrySet()) {
                        meta.addEnchant(e.getKey(), e.getValue(), true);
                    }
                }
                if (glow) {
                    if (!meta.hasEnchants()) {
                        meta.addEnchant(Enchantment.DURABILITY, 1, true);
                    }
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
                if (unbreakable) {
                    try {
                        meta.setUnbreakable(true);
                    } catch (NoSuchMethodError ignored) {
                    }
                    meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                }
                if (customModelData >= 0) {
                    try {
                        Method setCmd = meta.getClass().getMethod("setCustomModelData", Integer.TYPE);
                        setCmd.invoke(meta, customModelData);
                    } catch (Exception ignored) {
                    }
                }
                if (!flags.isEmpty()) {
                    meta.addItemFlags(flags.toArray(new ItemFlag[0]));
                }
                if (skullOwner != null && meta instanceof SkullMeta) {
                    try {
                        SkullMeta.class.getMethod("setOwner", String.class).invoke(meta, skullOwner);
                    } catch (Exception ignored) {
                    }
                }
                stack.setItemMeta(meta);
            }

            return new BukkitGUIItem(stack, clickHandler);
        }
    }

    public boolean isAir() {
        return itemStack == null || itemStack.getType() == Material.AIR;
    }

    public boolean isSimilar(ItemStack other) {
        return itemStack != null && itemStack.isSimilar(other);
    }

    public int getAmount() {
        return itemStack == null ? 0 : itemStack.getAmount();
    }

    public void setAmount(int amount) {
        if (itemStack != null) {
            itemStack.setAmount(amount);
        }
    }

    public String getDisplayName() {
        if (itemStack == null) return null;
        ItemMeta meta = itemStack.getItemMeta();
        return meta == null ? null : meta.getDisplayName();
    }

    public void setDisplayName(String name) {
        if (itemStack == null) return;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(TextColorizer.translate(name));
            itemStack.setItemMeta(meta);
        }
    }

    public List<String> getLore() {
        if (itemStack == null) return null;
        ItemMeta meta = itemStack.getItemMeta();
        return meta == null ? null : meta.getLore();
    }

    public void setLore(List<String> lore) {
        if (itemStack == null) return;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setLore(lore.stream()
                .map(TextColorizer::translate)
                .collect(java.util.stream.Collectors.toList()));
            itemStack.setItemMeta(meta);
        }
    }

    public void setLore(String... lore) {
        setLore(Arrays.asList(lore));
    }

    public void addLore(String line) {
        if (itemStack == null || line == null) return;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add(TextColorizer.translate(line));
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
    }

    public void clearLore() {
        if (itemStack == null) return;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setLore(null);
            itemStack.setItemMeta(meta);
        }
    }

    public boolean hasGlow() {
        if (itemStack == null) return false;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null || !meta.hasEnchants()) return false;
        return meta.getItemFlags().contains(ItemFlag.HIDE_ENCHANTS);
    }

    public void setGlow(boolean glow) {
        if (itemStack == null) return;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return;
        if (glow) {
            if (!meta.hasEnchants()) {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
            }
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            for (Enchantment e : meta.getEnchants().keySet()) {
                meta.removeEnchant(e);
            }
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        itemStack.setItemMeta(meta);
    }

    public boolean isUnbreakable() {
        if (itemStack == null) return false;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return false;
        try {
            return (boolean) ItemMeta.class.getMethod("isUnbreakable").invoke(meta);
        } catch (Exception e) {
            return false;
        }
    }

    public void setUnbreakable(boolean unbreakable) {
        if (itemStack == null) return;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return;
        try {
            meta.setUnbreakable(unbreakable);
        } catch (NoSuchMethodError ignored) {
        }
        if (unbreakable) {
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        } else {
            meta.removeItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }
        itemStack.setItemMeta(meta);
    }

    public int getCustomModelData() {
        if (itemStack == null) return -1;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return -1;
        try {
            Method getCmd = meta.getClass().getMethod("getCustomModelData");
            return (int) getCmd.invoke(meta);
        } catch (Exception e) {
            return -1;
        }
    }

    public void setCustomModelData(int data) {
        if (itemStack == null) return;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return;
        try {
            Method setCmd = meta.getClass().getMethod("setCustomModelData", Integer.TYPE);
            setCmd.invoke(meta, data);
        } catch (Exception ignored) {
        }
        itemStack.setItemMeta(meta);
    }

    public Collection<ItemFlag> getFlags() {
        if (itemStack == null) return new ArrayList<>();
        ItemMeta meta = itemStack.getItemMeta();
        return meta == null ? new ArrayList<>() : meta.getItemFlags();
    }

    public void addFlags(ItemFlag... flags) {
        if (itemStack == null) return;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(flags);
            itemStack.setItemMeta(meta);
        }
    }

    public void removeFlags(ItemFlag... flags) {
        if (itemStack == null) return;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.removeItemFlags(flags);
            itemStack.setItemMeta(meta);
        }
    }

    public BukkitGUIItem withName(String name) {
        BukkitGUIItem copy = copy();
        copy.setDisplayName(name);
        return copy;
    }

    public BukkitGUIItem withLore(List<String> lore) {
        BukkitGUIItem copy = copy();
        copy.setLore(lore);
        return copy;
    }

    public BukkitGUIItem withLore(String... lore) {
        return withLore(Arrays.asList(lore));
    }

    public BukkitGUIItem withAmount(int amount) {
        BukkitGUIItem copy = copy();
        copy.setAmount(amount);
        return copy;
    }

    public BukkitGUIItem withGlow() {
        BukkitGUIItem copy = copy();
        copy.setGlow(true);
        return copy;
    }

    public BukkitGUIItem withUnbreakable() {
        BukkitGUIItem copy = copy();
        copy.setUnbreakable(true);
        return copy;
    }

    public BukkitGUIItem withCustomModelData(int data) {
        BukkitGUIItem copy = copy();
        copy.setCustomModelData(data);
        return copy;
    }

    public BukkitGUIItem clickHandler(GUIClickHandler handler) {
        this.clickHandler = handler;
        return this;
    }

    public BukkitGUIItem copy() {
        return new BukkitGUIItem(itemStack != null ? itemStack.clone() : null, clickHandler);
    }
}
