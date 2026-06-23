package com.zaryxstudios.okaso.gui;

import com.zaryxstudios.okaso.common.gui.GUI;
import com.zaryxstudios.okaso.common.gui.GUIItem;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BukkitGUI implements GUI, Listener {

    private final Plugin plugin;
    private final String title;
    private final int size;
    private final Inventory inventory;
    private final Map<Integer, GUIItem> items;
    private boolean registered;
    private int viewerCount;

    public BukkitGUI(Plugin plugin, String title, int size) {
        this.plugin = plugin;
        this.title = title;
        this.size = size;
        this.inventory = Bukkit.createInventory(null, size, title);
        this.items = new HashMap<>();
        this.registered = false;
        this.viewerCount = 0;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public void open(Object player) {
        if (player instanceof Player) {
            registerListener();
            viewerCount++;
            ((Player) player).openInventory(inventory);
        }
    }

    @Override
    public void close(Object player) {
        if (player instanceof HumanEntity) {
            ((HumanEntity) player).closeInventory();
        }
    }

    @Override
    public void setItem(int slot, GUIItem item) {
        if (slot < 0 || slot >= size) return;
        items.put(slot, item);
        Object bukkitItem = item.getItemStack();
        if (bukkitItem instanceof ItemStack) {
            inventory.setItem(slot, (ItemStack) bukkitItem);
        }
    }

    @Override
    public GUIItem getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public void removeItem(int slot) {
        items.remove(slot);
        inventory.clear(slot);
    }

    @Override
    public int addItem(GUIItem item) {
        for (int slot = 0; slot < size; slot++) {
            if (!items.containsKey(slot)) {
                setItem(slot, item);
                return slot;
            }
        }
        return -1;
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public int countItems() {
        return items.size();
    }

    @Override
    public void clear() {
        items.clear();
        inventory.clear();
    }

    public void updateSlot(int slot) {
        if (slot < 0 || slot >= size) return;
        inventory.clear(slot);
        GUIItem item = items.get(slot);
        if (item != null) {
            Object bukkitItem = item.getItemStack();
            if (bukkitItem instanceof ItemStack) {
                inventory.setItem(slot, (ItemStack) bukkitItem);
            }
        }
    }

    public void updateAll() {
        inventory.clear();
        for (Map.Entry<Integer, GUIItem> entry : items.entrySet()) {
            int slot = entry.getKey();
            Object bukkitItem = entry.getValue().getItemStack();
            if (bukkitItem instanceof ItemStack) {
                inventory.setItem(slot, (ItemStack) bukkitItem);
            }
        }
    }

    public List<HumanEntity> getViewers() {
        return new ArrayList<>(inventory.getViewers());
    }

    public void fillEmpty(GUIItem item) {
        Object bukkitItem = item.getItemStack();
        if (!(bukkitItem instanceof ItemStack)) return;
        ItemStack stack = (ItemStack) bukkitItem;
        for (int slot = 0; slot < size; slot++) {
            if (!items.containsKey(slot)) {
                items.put(slot, item);
                inventory.setItem(slot, stack);
            }
        }
    }

    public boolean contains(int slot) {
        return items.containsKey(slot);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= size) return;

        GUIItem item = items.get(slot);
        if (item == null) return;

        BukkitGUIClickEvent clickEvent = new BukkitGUIClickEvent(
            event.getWhoClicked(), slot,
            event.isLeftClick(), event.isRightClick(), event.isShiftClick(),
            event.getClick().equals(org.bukkit.event.inventory.ClickType.MIDDLE),
            event.getClick().equals(org.bukkit.event.inventory.ClickType.DOUBLE_CLICK),
            event.getCurrentItem());
        item.onClick(clickEvent);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        viewerCount--;
        if (viewerCount <= 0 && registered) {
            HandlerList.unregisterAll(this);
            registered = false;
            viewerCount = 0;
        }
    }

    private void registerListener() {
        if (!registered) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            registered = true;
        }
    }
}
