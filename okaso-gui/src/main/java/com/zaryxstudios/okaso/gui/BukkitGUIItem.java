package com.zaryxstudios.okaso.gui;

import com.zaryxstudios.okaso.common.gui.GUIClickEvent;
import com.zaryxstudios.okaso.common.gui.GUIItem;

import org.bukkit.inventory.ItemStack;

public class BukkitGUIItem implements GUIItem {

    private final ItemStack itemStack;
    private final GUIClickHandler clickHandler;

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
    public Object getItemStack() {
        return itemStack;
    }

    @Override
    public void onClick(GUIClickEvent event) {
        if (clickHandler != null) {
            clickHandler.onClick(event);
        }
    }
}
