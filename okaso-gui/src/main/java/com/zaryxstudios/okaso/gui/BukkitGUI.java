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
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;

public class BukkitGUI implements GUI, Listener {

    private final Plugin plugin;
    @Getter
    private final String title;
    @Getter
    private final int size;
    @Getter
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
    public void open(Object player) {
        if (player instanceof Player) {
            registerListener();
            if (!((Player) player).getOpenInventory().getTopInventory().equals(inventory)) {
                viewerCount++;
            }
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
        if (item == null) {
            removeItem(slot);
            return;
        }
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

    public boolean isViewing(Player player) {
        return player != null && player.getOpenInventory().getTopInventory().equals(inventory);
    }

    public void closeAll() {
        for (HumanEntity viewer : new ArrayList<>(inventory.getViewers())) {
            viewer.closeInventory();
        }
    }

    public void swap(int slot1, int slot2) {
        if (slot1 < 0 || slot1 >= size || slot2 < 0 || slot2 >= size) return;
        if (slot1 == slot2) return;
        GUIItem item1 = items.get(slot1);
        GUIItem item2 = items.get(slot2);
        if (item1 != null) {
            items.put(slot2, item1);
            Object bukkit1 = item1.getItemStack();
            if (bukkit1 instanceof ItemStack) {
                inventory.setItem(slot2, (ItemStack) bukkit1);
            }
        } else {
            inventory.clear(slot2);
            items.remove(slot2);
        }
        if (item2 != null) {
            items.put(slot1, item2);
            Object bukkit2 = item2.getItemStack();
            if (bukkit2 instanceof ItemStack) {
                inventory.setItem(slot1, (ItemStack) bukkit2);
            }
        } else {
            inventory.clear(slot1);
            items.remove(slot1);
        }
    }

    public void moveItem(int fromSlot, int toSlot) {
        if (fromSlot < 0 || fromSlot >= size || toSlot < 0 || toSlot >= size) return;
        if (fromSlot == toSlot) return;
        GUIItem item = items.get(fromSlot);
        if (item == null) return;
        items.put(toSlot, item);
        Object bukkitItem = item.getItemStack();
        if (bukkitItem instanceof ItemStack) {
            inventory.setItem(toSlot, (ItemStack) bukkitItem);
        }
        items.remove(fromSlot);
        inventory.clear(fromSlot);
    }

    public void setSlotEmpty(int slot) {
        if (slot < 0 || slot >= size) return;
        items.remove(slot);
        inventory.clear(slot);
    }

    public Set<Integer> getOccupiedSlots() {
        return new HashSet<>(items.keySet());
    }

    public int getFirstEmptySlot() {
        for (int slot = 0; slot < size; slot++) {
            if (!items.containsKey(slot)) {
                return slot;
            }
        }
        return -1;
    }

    public void setItems(Map<Integer, GUIItem> items) {
        for (Map.Entry<Integer, GUIItem> entry : items.entrySet()) {
            setItem(entry.getKey(), entry.getValue());
        }
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

    public void fillBorder(GUIItem item) {
        Object bukkitItem = item.getItemStack();
        if (!(bukkitItem instanceof ItemStack)) return;
        ItemStack stack = (ItemStack) bukkitItem;
        int rows = size / 9;
        for (int slot = 0; slot < size; slot++) {
            int col = slot % 9;
            int row = slot / 9;
            if (row == 0 || row == rows - 1 || col == 0 || col == 8) {
                if (!items.containsKey(slot)) {
                    items.put(slot, item);
                    inventory.setItem(slot, stack);
                }
            }
        }
    }

    public boolean contains(int slot) {
        return items.containsKey(slot);
    }

    public boolean hasSlot(int slot) {
        return slot >= 0 && slot < size;
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
            event.getCurrentItem(), event.getClick(), event.getAction(), event.getHotbarButton());
        item.onClick(clickEvent);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().equals(inventory)) {
            event.setCancelled(true);
        }
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
