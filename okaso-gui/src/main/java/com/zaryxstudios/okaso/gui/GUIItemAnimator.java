package com.zaryxstudios.okaso.gui;

import com.zaryxstudios.okaso.common.gui.GUI;
import com.zaryxstudios.okaso.common.gui.GUIItem;
import com.zaryxstudios.okaso.common.text.TextColorizer;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class GUIItemAnimator {

    private final Plugin plugin;
    private final Map<Integer, BukkitTask> slotTasks;
    private final Map<Integer, BukkitRunnable> slotRunnables;

    public GUIItemAnimator(Plugin plugin) {
        this.plugin = plugin;
        this.slotTasks = new HashMap<>();
        this.slotRunnables = new HashMap<>();
    }

    public void pulse(GUI gui, int slot, long intervalTicks) {
        GUIItem original = gui.getItem(slot);
        if (original == null) return;
        stop(gui, slot);
        boolean[] state = {false};
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                Object bukkitItem = original.getItemStack();
                if (!(bukkitItem instanceof ItemStack)) {
                    cancel();
                    return;
                }
                ItemStack stack = ((ItemStack) bukkitItem).clone();
                ItemMeta meta = stack.getItemMeta();
                if (meta == null) {
                    cancel();
                    return;
                }
                state[0] = !state[0];
                if (state[0]) {
                    if (!meta.hasEnchants()) {
                        meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
                    }
                    meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
                } else {
                    meta.removeItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
                    if (meta.hasEnchants() && meta.getEnchants().size() == 1
                        && meta.hasEnchant(org.bukkit.enchantments.Enchantment.DURABILITY)) {
                        meta.removeEnchant(org.bukkit.enchantments.Enchantment.DURABILITY);
                    }
                }
                stack.setItemMeta(meta);
                gui.updateSlot(slot);
            }
        };
        BukkitTask bukkitTask = task.runTaskTimer(plugin, 0L, intervalTicks);
        slotTasks.put(slot, bukkitTask);
        slotRunnables.put(slot, task);
    }

    public void glowToggle(GUI gui, int slot, long intervalTicks) {
        pulse(gui, slot, intervalTicks);
    }

    public void cycleItems(GUI gui, int slot, long intervalTicks, GUIItem... items) {
        if (items == null || items.length == 0) return;
        stop(gui, slot);
        final int[] index = {0};
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                GUIItem current = items[index[0] % items.length];
                gui.setItem(slot, current);
                index[0]++;
            }
        };
        BukkitTask bukkitTask = task.runTaskTimer(plugin, 0L, intervalTicks);
        slotTasks.put(slot, bukkitTask);
        slotRunnables.put(slot, task);
    }

    public void cycleDisplayNames(GUI gui, int slot, long intervalTicks, GUIItem baseItem, String... names) {
        if (baseItem == null || names == null || names.length == 0) return;
        stop(gui, slot);
        final int[] index = {0};
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                Object bukkitItem = baseItem.getItemStack();
                if (!(bukkitItem instanceof ItemStack)) {
                    cancel();
                    return;
                }
                ItemStack stack = ((ItemStack) bukkitItem).clone();
                ItemMeta meta = stack.getItemMeta();
                if (meta == null) {
                    cancel();
                    return;
                }
                meta.setDisplayName(TextColorizer.translate(names[index[0] % names.length]));
                stack.setItemMeta(meta);
                if (baseItem instanceof OkasoBukkitGUIItem) {
                    OkasoBukkitGUIItem copy = ((OkasoBukkitGUIItem) baseItem).copy();
                    copy.setItemStack(stack);
                    gui.setItem(slot, copy);
                }
                index[0]++;
            }
        };
        BukkitTask bukkitTask = task.runTaskTimer(plugin, 0L, intervalTicks);
        slotTasks.put(slot, bukkitTask);
        slotRunnables.put(slot, task);
    }

    public void countdown(GUI gui, int slot, int fromSeconds, String format,
                          GUIItem baseItem, Runnable onFinish) {
        if (baseItem == null || fromSeconds <= 0) return;
        stop(gui, slot);
        final int[] remaining = {fromSeconds};
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (remaining[0] <= 0) {
                    cancel();
                    stop(gui, slot);
                    if (onFinish != null) {
                        onFinish.run();
                    }
                    return;
                }
                Object bukkitItem = baseItem.getItemStack();
                if (!(bukkitItem instanceof ItemStack)) {
                    cancel();
                    return;
                }
                ItemStack stack = ((ItemStack) bukkitItem).clone();
                ItemMeta meta = stack.getItemMeta();
                if (meta == null) {
                    cancel();
                    return;
                }
                int mins = remaining[0] / 60;
                int secs = remaining[0] % 60;
                String timeStr = String.format("%02d:%02d", mins, secs);
                String display = format.replace("{time}", timeStr)
                    .replace("{seconds}", String.valueOf(remaining[0]));
                meta.setDisplayName(TextColorizer.translate(display));
                stack.setItemMeta(meta);
                if (baseItem instanceof OkasoBukkitGUIItem) {
                    OkasoBukkitGUIItem copy = ((OkasoBukkitGUIItem) baseItem).copy();
                    copy.setItemStack(stack);
                    gui.setItem(slot, copy);
                }
                remaining[0]--;
            }
        };
        BukkitTask bukkitTask = task.runTaskTimer(plugin, 20L, 20L);
        slotTasks.put(slot, bukkitTask);
        slotRunnables.put(slot, task);
    }

    public void rainbow(GUI gui, int slot, long intervalTicks, GUIItem baseItem) {
        if (baseItem == null) return;
        stop(gui, slot);
        String[] colors = {"&0", "&1", "&2", "&3", "&4", "&5", "&6", "&7", "&8", "&9", "&a", "&b", "&c", "&d", "&e", "&f"};
        final int[] index = {0};
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                Object bukkitItem = baseItem.getItemStack();
                if (!(bukkitItem instanceof ItemStack)) {
                    cancel();
                    return;
                }
                ItemStack stack = ((ItemStack) bukkitItem).clone();
                ItemMeta meta = stack.getItemMeta();
                if (meta == null) {
                    cancel();
                    return;
                }
                if (meta.hasDisplayName()) {
                    String base = meta.getDisplayName().replaceAll("§[0-9a-fk-or]", "");
                    meta.setDisplayName(TextColorizer.translate(colors[index[0] % colors.length] + base));
                    stack.setItemMeta(meta);
                    if (baseItem instanceof OkasoBukkitGUIItem) {
                        OkasoBukkitGUIItem copy = ((OkasoBukkitGUIItem) baseItem).copy();
                        copy.setItemStack(stack);
                        gui.setItem(slot, copy);
                    }
                }
                index[0]++;
            }
        };
        BukkitTask bukkitTask = task.runTaskTimer(plugin, 0L, intervalTicks);
        slotTasks.put(slot, bukkitTask);
        slotRunnables.put(slot, task);
    }

    public void stop(GUI gui, int slot) {
        BukkitTask task = slotTasks.remove(slot);
        if (task != null) {
            task.cancel();
        }
        slotRunnables.remove(slot);
        if (gui != null) {
            gui.updateSlot(slot);
        }
    }

    public void stopAll(GUI gui) {
        for (Map.Entry<Integer, BukkitTask> entry : slotTasks.entrySet()) {
            entry.getValue().cancel();
            if (gui != null) {
                gui.updateSlot(entry.getKey());
            }
        }
        slotTasks.clear();
        slotRunnables.clear();
    }

    public boolean isAnimating(int slot) {
        return slotTasks.containsKey(slot);
    }

    public int getAnimatingCount() {
        return slotTasks.size();
    }
}
