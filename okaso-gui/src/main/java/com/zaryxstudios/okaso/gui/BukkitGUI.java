package com.zaryxstudios.okaso.gui;

import com.zaryxstudios.okaso.common.gui.GUI;
import com.zaryxstudios.okaso.common.gui.GUIItem;
import com.zaryxstudios.okaso.common.text.TextColorizer;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import lombok.Getter;

public class BukkitGUI implements GUI, Listener {

    private final Plugin plugin;
    private final String title;
    private final int size;
    @Getter
    private final Inventory inventory;
    private final Map<Integer, GUIItem> items;
    private boolean registered;
    private Consumer<Object> openHandler;
    private Consumer<Object> closeHandler;

    private int page;
    private List<GUIItem> pageableItems;
    private int pageSize;

    private BukkitTask animTask;
    private int animTick;
    private List<Runnable> animFrames;

    private Consumer<Boolean> confirmCallback;

    public BukkitGUI(Plugin plugin, String title, int size) {
        if (size % 9 != 0 || size < 9 || size > 54) {
            throw new IllegalArgumentException("Inventory size must be a multiple of 9 between 9 and 54");
        }
        this.plugin = plugin;
        this.title = title;
        this.size = size;
        this.inventory = Bukkit.createInventory(null, size, title);
        this.items = new HashMap<>();
        this.registered = false;
        this.page = 0;
        this.pageableItems = null;
        this.pageSize = size;
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
    public int getPage() {
        return page;
    }

    @Override
    public void open(Object player) {
        if (player instanceof Player) {
            registerListener();
            ((Player) player).openInventory(inventory);
            if (openHandler != null) {
                openHandler.accept(player);
            }
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
        if (slot < 0 || slot >= size) return;
        items.remove(slot);
        inventory.clear(slot);
    }

    @Override
    public int addItem(GUIItem item) {
        if (item == null) return -1;
        for (int slot = 0; slot < size; slot++) {
            if (!items.containsKey(slot)) {
                setItem(slot, item);
                return slot;
            }
        }
        return -1;
    }

    @Override
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

    @Override
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
        pageableItems = null;
        page = 0;
    }

    @Override
    public Map<Integer, GUIItem> getItems() {
        return new HashMap<>(items);
    }

    @Override
    public Collection<Object> getViewers() {
        Collection<Object> result = new ArrayList<>();
        result.addAll(inventory.getViewers());
        return result;
    }

    @Override
    public boolean isViewing(Object player) {
        return player instanceof Player
            && ((Player) player).getOpenInventory().getTopInventory().equals(inventory);
    }

    @Override
    public void closeAll() {
        for (HumanEntity viewer : new ArrayList<>(inventory.getViewers())) {
            viewer.closeInventory();
        }
    }

    @Override
    public void setOpenHandler(Consumer<Object> handler) {
        this.openHandler = handler;
    }

    @Override
    public void setCloseHandler(Consumer<Object> handler) {
        this.closeHandler = handler;
    }

    @Override
    public int getRows() {
        return size / 9;
    }

    @Override
    public void fillEmpty(GUIItem item) {
        if (item == null) return;
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

    @Override
    public void fillBorder(GUIItem item) {
        if (item == null) return;
        Object bukkitItem = item.getItemStack();
        if (!(bukkitItem instanceof ItemStack)) return;
        ItemStack stack = (ItemStack) bukkitItem;
        int rows = getRows();
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

    @Override
    public void fillRow(int row, GUIItem item) {
        if (item == null) return;
        int start = row * 9;
        int end = Math.min(start + 9, size);
        for (int slot = start; slot < end; slot++) {
            if (!items.containsKey(slot)) {
                setItem(slot, item);
            }
        }
    }

    @Override
    public void fillColumn(int column, GUIItem item) {
        if (item == null) return;
        if (column < 0 || column > 8) return;
        int rows = getRows();
        for (int row = 0; row < rows; row++) {
            int slot = row * 9 + column;
            if (slot < size && !items.containsKey(slot)) {
                setItem(slot, item);
            }
        }
    }

    @Override
    public int getFirstEmptySlot() {
        for (int slot = 0; slot < size; slot++) {
            if (!items.containsKey(slot)) {
                return slot;
            }
        }
        return -1;
    }

    @Override
    public boolean contains(int slot) {
        return items.containsKey(slot);
    }

    @Override
    public boolean hasSlot(int slot) {
        return slot >= 0 && slot < size;
    }

    public boolean setItemIfAbsent(int slot, GUIItem item) {
        if (items.containsKey(slot)) return false;
        setItem(slot, item);
        return true;
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

    public void setItems(Map<Integer, GUIItem> items) {
        if (items == null) return;
        for (Map.Entry<Integer, GUIItem> entry : items.entrySet()) {
            setItem(entry.getKey(), entry.getValue());
        }
    }

    public int findSlot(Predicate<GUIItem> predicate) {
        for (Map.Entry<Integer, GUIItem> entry : items.entrySet()) {
            if (predicate.test(entry.getValue())) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public List<Integer> findSlots(Predicate<GUIItem> predicate) {
        return items.entrySet().stream()
            .filter(e -> predicate.test(e.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    public boolean replaceItem(Predicate<GUIItem> predicate, GUIItem newItem) {
        int slot = findSlot(predicate);
        if (slot == -1) return false;
        setItem(slot, newItem);
        return true;
    }

    @Override
    public void setPage(int page) {
        if (pageableItems == null) return;
        if (page < 0) page = 0;
        if (page >= getTotalPages()) page = getTotalPages() - 1;
        this.page = page;
        renderPage();
    }

    @Override
    public int getTotalPages() {
        if (pageableItems == null || pageSize <= 0) return 1;
        return (int) Math.ceil((double) pageableItems.size() / pageSize);
    }

    @Override
    public boolean hasNextPage() {
        return pageableItems != null && page < getTotalPages() - 1;
    }

    @Override
    public boolean hasPreviousPage() {
        return pageableItems != null && page > 0;
    }

    @Override
    public void nextPage() {
        if (hasNextPage()) {
            setPage(page + 1);
        }
    }

    @Override
    public void previousPage() {
        if (hasPreviousPage()) {
            setPage(page - 1);
        }
    }

    public void setPageableItems(List<GUIItem> items, int pageSize) {
        if (items == null) {
            this.pageableItems = null;
            this.page = 0;
            renderPage();
            return;
        }
        if (pageSize <= 0) {
            this.pageableItems = null;
            this.page = 0;
            renderPage();
            return;
        }
        this.pageableItems = new ArrayList<>(items);
        this.pageSize = pageSize;
        this.page = 0;
        renderPage();
    }

    public void setPageableItems(List<GUIItem> items) {
        setPageableItems(items, size);
    }

    private void renderPage() {
        items.clear();
        inventory.clear();
        if (pageableItems == null) return;
        int start = page * pageSize;
        int end = Math.min(start + pageSize, pageableItems.size());
        for (int i = start; i < end; i++) {
            int slot = i - start;
            if (slot >= size) break;
            GUIItem item = pageableItems.get(i);
            items.put(slot, item);
            Object bukkitItem = item.getItemStack();
            if (bukkitItem instanceof ItemStack) {
                inventory.setItem(slot, (ItemStack) bukkitItem);
            }
        }
    }

    public void animate(List<Runnable> frames, long intervalTicks) {
        if (frames == null || frames.isEmpty()) return;
        if (intervalTicks <= 0) intervalTicks = 1;
        stopAnimation();
        this.animFrames = new ArrayList<>(frames);
        this.animTick = 0;
        this.animTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (animFrames != null && !animFrames.isEmpty()) {
                animFrames.get(animTick % animFrames.size()).run();
                animTick++;
            }
        }, 0L, intervalTicks);
    }

    public void animateSlots(Map<Integer, List<ItemStack>> slotAnimations, long intervalTicks) {
        if (slotAnimations == null || slotAnimations.isEmpty()) return;
        if (intervalTicks <= 0) intervalTicks = 1;
        for (int slot : slotAnimations.keySet()) {
            if (slot < 0 || slot >= size) return;
        }
        stopAnimation();
        int maxFrames = slotAnimations.values().stream()
            .mapToInt(List::size)
            .max().orElse(0);
        if (maxFrames == 0) return;
        this.animTick = 0;
        this.animTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int frame = animTick % maxFrames;
            for (Map.Entry<Integer, List<ItemStack>> entry : slotAnimations.entrySet()) {
                int slot = entry.getKey();
                List<ItemStack> frames = entry.getValue();
                if (frame < frames.size()) {
                    inventory.setItem(slot, frames.get(frame));
                }
            }
            animTick++;
        }, 0L, intervalTicks);
    }

    public void stopAnimation() {
        if (animTask != null) {
            animTask.cancel();
            animTask = null;
        }
        animFrames = null;
        animTick = 0;
    }

    public boolean isAnimating() {
        return animTask != null;
    }

    public void confirm(Consumer<Boolean> callback, GUIItem confirmItem, GUIItem cancelItem,
                        String confirmTitle, String cancelTitle, int confirmSlot, int cancelSlot) {
        this.confirmCallback = callback;
        if (confirmItem != null) {
            ItemStack stack = confirmItem.getItemStack() instanceof ItemStack ? (ItemStack) confirmItem.getItemStack() : null;
            if (stack != null) {
                stack = stack.clone();
                if (confirmTitle != null) {
                    ItemMeta meta = stack.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(TextColorizer.translate(confirmTitle));
                        stack.setItemMeta(meta);
                    }
                }
            }
            BukkitGUIItem confirmBtn = new BukkitGUIItem(stack, event -> handleConfirm(true));
            setItem(confirmSlot, confirmBtn);
        }
        if (cancelItem != null) {
            ItemStack stack = cancelItem.getItemStack() instanceof ItemStack ? (ItemStack) cancelItem.getItemStack() : null;
            if (stack != null) {
                stack = stack.clone();
                if (cancelTitle != null) {
                    ItemMeta meta = stack.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(TextColorizer.translate(cancelTitle));
                        stack.setItemMeta(meta);
                    }
                }
            }
            BukkitGUIItem cancelBtn = new BukkitGUIItem(stack, event -> handleConfirm(false));
            setItem(cancelSlot, cancelBtn);
        }
    }

    public void confirm(Consumer<Boolean> callback) {
        this.confirmCallback = callback;
    }

    private void handleConfirm(boolean result) {
        if (confirmCallback != null) {
            Consumer<Boolean> cb = confirmCallback;
            confirmCallback = null;
            cb.accept(result);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;

        int rawSlot = event.getRawSlot();
        boolean topInv = rawSlot < size;

        if (topInv) {
            int slot = rawSlot;
            GUIItem item = items.get(slot);
            if (item != null) {
                BukkitGUIClickEvent clickEvent = new BukkitGUIClickEvent(
                    event.getWhoClicked(), slot, rawSlot,
                    event.isLeftClick(), event.isRightClick(), event.isShiftClick(),
                    event.getClick() == ClickType.MIDDLE,
                    event.getClick() == ClickType.DOUBLE_CLICK,
                    true,
                    event.getCurrentItem(), event.getCursor(),
                    event.getClick(), event.getAction(), event.getHotbarButton());
                item.onClick(clickEvent);
                if (clickEvent.isCancelled()) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        } else if (event.isShiftClick()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().equals(inventory)) {
            for (int slot : event.getRawSlots()) {
                if (slot < size) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (closeHandler != null) {
            closeHandler.accept(event.getPlayer());
        }
        if (inventory.getViewers().isEmpty() && registered) {
            stopAnimation();
            HandlerList.unregisterAll(this);
            registered = false;
        }
    }

    private void registerListener() {
        if (!registered) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            registered = true;
        }
    }
}
