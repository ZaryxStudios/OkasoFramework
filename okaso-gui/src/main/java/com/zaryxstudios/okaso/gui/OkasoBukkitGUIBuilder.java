package com.zaryxstudios.okaso.gui;

import com.zaryxstudios.okaso.common.gui.GUIItem;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class OkasoBukkitGUIBuilder {

    private final Plugin plugin;
    private String title;
    private int size;
    private InventoryType type;
    private final Map<Integer, GUIItem> items = new HashMap<>();
    private final Set<Integer> freeSlots = new HashSet<>();
    private Consumer<Object> openHandler;
    private Consumer<Object> closeHandler;
    private Consumer<Integer> dragHandler;
    private List<GUIItem> pageableItems;
    private int pageSize;

    OkasoBukkitGUIBuilder(Plugin plugin) {
        this.plugin = plugin;
    }

    public OkasoBukkitGUIBuilder title(String title) {
        this.title = title;
        return this;
    }

    public OkasoBukkitGUIBuilder size(int size) {
        this.size = size;
        this.type = null;
        return this;
    }

    public OkasoBukkitGUIBuilder type(InventoryType type) {
        this.type = type;
        this.size = 0;
        return this;
    }

    public OkasoBukkitGUIBuilder item(int slot, GUIItem item) {
        this.items.put(slot, item);
        return this;
    }

    public OkasoBukkitGUIBuilder item(int slot, Material material) {
        this.items.put(slot, OkasoBukkitGUIItem.of(material));
        return this;
    }

    public OkasoBukkitGUIBuilder item(int slot, Material material, String name) {
        this.items.put(slot, OkasoBukkitGUIItem.of(material, name));
        return this;
    }

    public OkasoBukkitGUIBuilder items(Map<Integer, GUIItem> items) {
        if (items != null) {
            this.items.putAll(items);
        }
        return this;
    }

    public OkasoBukkitGUIBuilder freeSlot(int... slots) {
        for (int s : slots) {
            this.freeSlots.add(s);
        }
        return this;
    }

    public OkasoBukkitGUIBuilder freeSlots(Collection<Integer> slots) {
        this.freeSlots.addAll(slots);
        return this;
    }

    public OkasoBukkitGUIBuilder openHandler(Consumer<Object> handler) {
        this.openHandler = handler;
        return this;
    }

    public OkasoBukkitGUIBuilder closeHandler(Consumer<Object> handler) {
        this.closeHandler = handler;
        return this;
    }

    public OkasoBukkitGUIBuilder dragHandler(Consumer<Integer> handler) {
        this.dragHandler = handler;
        return this;
    }

    public OkasoBukkitGUIBuilder pageableItems(List<GUIItem> items) {
        this.pageableItems = items;
        return this;
    }

    public OkasoBukkitGUIBuilder pageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public OkasoBukkitGUI build() {
        if (title == null) {
            throw new IllegalStateException("Title is required");
        }
        OkasoBukkitGUI gui;
        if (type != null) {
            gui = new OkasoBukkitGUI(plugin, title, type);
        } else {
            if (size <= 0) size = 9;
            gui = new OkasoBukkitGUI(plugin, title, size);
        }
        for (Map.Entry<Integer, GUIItem> entry : items.entrySet()) {
            gui.setItem(entry.getKey(), entry.getValue());
        }
        for (int slot : freeSlots) {
            gui.setFreeSlot(slot);
        }
        if (openHandler != null) {
            gui.setOpenHandler(openHandler);
        }
        if (closeHandler != null) {
            gui.setCloseHandler(closeHandler);
        }
        if (dragHandler != null) {
            gui.setDragHandler(dragHandler);
        }
        if (pageableItems != null) {
            gui.setPageableItems(pageableItems, pageSize > 0 ? pageSize : gui.getSize());
        }
        return gui;
    }
}
