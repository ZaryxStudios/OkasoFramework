package com.zaryxstudios.okaso.gui;

import com.zaryxstudios.okaso.common.gui.GUI;
import com.zaryxstudios.okaso.common.gui.GUIClickEvent;
import com.zaryxstudios.okaso.common.gui.GUIItem;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class GUIItemSlot implements GUIItem {

    private final GUI gui;
    private final int slot;
    private final Supplier<ItemStack> itemSupplier;
    private final Consumer<GUIClickEvent> clickHandler;
    private BukkitTask autoUpdateTask;
    private GUIItem currentDelegate;

    public GUIItemSlot(GUI gui, int slot, Supplier<ItemStack> itemSupplier) {
        this(gui, slot, itemSupplier, null);
    }

    public GUIItemSlot(GUI gui, int slot, Supplier<ItemStack> itemSupplier, Consumer<GUIClickEvent> clickHandler) {
        this.gui = gui;
        this.slot = slot;
        this.itemSupplier = itemSupplier;
        this.clickHandler = clickHandler;
        this.currentDelegate = null;
        update();
    }

    @Override
    public Object getItemStack() {
        return itemSupplier.get();
    }

    @Override
    public void onClick(GUIClickEvent event) {
        if (clickHandler != null) {
            clickHandler.accept(event);
        }
    }

    public void update() {
        ItemStack stack = itemSupplier.get();
        if (stack != null) {
            if (clickHandler != null) {
                currentDelegate = OkasoBukkitGUIItem.of(stack, this::onClickInternal);
            } else {
                currentDelegate = OkasoBukkitGUIItem.of(stack);
            }
        } else {
            currentDelegate = OkasoBukkitGUIItem.empty();
        }
        gui.setItem(slot, currentDelegate);
    }

    private void onClickInternal(GUIClickEvent event) {
        if (clickHandler != null) {
            clickHandler.accept(event);
        }
    }

    public void startAutoUpdate(Plugin plugin, long intervalTicks) {
        stopAutoUpdate();
        this.autoUpdateTask = new BukkitRunnable() {
            @Override
            public void run() {
                update();
            }
        }.runTaskTimer(plugin, 0L, intervalTicks);
    }

    public void stopAutoUpdate() {
        if (autoUpdateTask != null) {
            autoUpdateTask.cancel();
            autoUpdateTask = null;
        }
    }

    public boolean isAutoUpdating() {
        return autoUpdateTask != null;
    }

    public GUI getGUI() {
        return gui;
    }

    public int getSlot() {
        return slot;
    }

    public GUIItem getCurrentDelegate() {
        return currentDelegate;
    }

    public static GUIItemSlot create(GUI gui, int slot, Supplier<ItemStack> supplier) {
        return new GUIItemSlot(gui, slot, supplier);
    }

    public static GUIItemSlot create(GUI gui, int slot, Supplier<ItemStack> supplier,
                                     Consumer<GUIClickEvent> handler) {
        return new GUIItemSlot(gui, slot, supplier, handler);
    }

    public static GUIItemSlot createAuto(GUI gui, int slot, Plugin plugin, long intervalTicks,
                                         Supplier<ItemStack> supplier) {
        GUIItemSlot slotItem = new GUIItemSlot(gui, slot, supplier);
        slotItem.startAutoUpdate(plugin, intervalTicks);
        return slotItem;
    }
}
