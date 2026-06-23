package com.zaryxstudios.okaso.gui;

import com.zaryxstudios.okaso.common.gui.GUIClickEvent;
import com.zaryxstudios.okaso.common.gui.GUIItem;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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

    public static BukkitGUIItem of(ItemStack itemStack, GUIClickHandler clickHandler) {
        return new BukkitGUIItem(itemStack, clickHandler);
    }

    public static BukkitGUIItem of(ItemStack itemStack) {
        return new BukkitGUIItem(itemStack);
    }

    @Override
    public void onClick(GUIClickEvent event) {
        if (clickHandler != null) {
            clickHandler.onClick(event);
        }
    }

    public boolean isAir() {
        return itemStack == null || itemStack.getType() == Material.AIR;
    }

    public boolean isSimilar(ItemStack other) {
        return itemStack != null && itemStack.isSimilar(other);
    }

    public BukkitGUIItem copy() {
        return new BukkitGUIItem(itemStack != null ? itemStack.clone() : null, clickHandler);
    }
}
