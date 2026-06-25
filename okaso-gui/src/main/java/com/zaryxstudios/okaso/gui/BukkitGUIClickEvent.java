package com.zaryxstudios.okaso.gui;

import com.zaryxstudios.okaso.common.gui.GUIClickEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import lombok.Setter;

@Getter
public class BukkitGUIClickEvent implements GUIClickEvent {

    private final Object whoClicked;
    private final int slot;
    private final int rawSlot;
    private final boolean leftClick;
    private final boolean rightClick;
    private final boolean shiftClick;
    private final boolean middleClick;
    private final boolean doubleClick;
    private final boolean topInventory;
    private final Object clickItem;
    private final Object cursorItem;
    private final ClickType clickType;
    private final InventoryAction action;
    private final int hotbarButton;
    @Setter
    private boolean cancelled;

    public BukkitGUIClickEvent(Object whoClicked, int slot, int rawSlot,
                               boolean leftClick, boolean rightClick, boolean shiftClick,
                               boolean middleClick, boolean doubleClick, boolean topInventory,
                               Object clickItem, Object cursorItem,
                               ClickType clickType, InventoryAction action, int hotbarButton) {
        this.whoClicked = whoClicked;
        this.slot = slot;
        this.rawSlot = rawSlot;
        this.leftClick = leftClick;
        this.rightClick = rightClick;
        this.shiftClick = shiftClick;
        this.middleClick = middleClick;
        this.doubleClick = doubleClick;
        this.topInventory = topInventory;
        this.clickItem = clickItem;
        this.cursorItem = cursorItem;
        this.clickType = clickType;
        this.action = action;
        this.hotbarButton = hotbarButton;
        this.cancelled = true;
    }

    public Player getPlayer() {
        return whoClicked instanceof Player ? (Player) whoClicked : null;
    }

    public ItemStack getClickedItem() {
        return clickItem instanceof ItemStack ? (ItemStack) clickItem : null;
    }

    public ItemStack getCursorItem() {
        return cursorItem instanceof ItemStack ? (ItemStack) cursorItem : null;
    }

    public boolean isHotbarMove() {
        return clickType == ClickType.NUMBER_KEY;
    }

    public boolean isCreativeAction() {
        return action == InventoryAction.CLONE_STACK;
    }

    @Override
    public boolean isDropAction() {
        return action == InventoryAction.DROP_ALL_CURSOR || action == InventoryAction.DROP_ALL_SLOT
            || action == InventoryAction.DROP_ONE_CURSOR || action == InventoryAction.DROP_ONE_SLOT;
    }

    @Override
    public boolean isShiftLeft() {
        return shiftClick && leftClick;
    }

    @Override
    public boolean isShiftRight() {
        return shiftClick && rightClick;
    }

    public boolean isNumberKey() {
        return clickType == ClickType.NUMBER_KEY;
    }

    public boolean isLeftOrRight() {
        return leftClick || rightClick;
    }

    @Override
    public boolean isKeyboardClick() {
        return clickType == ClickType.NUMBER_KEY || clickType == ClickType.DROP
            || clickType == ClickType.CONTROL_DROP;
    }

    @Override
    public boolean isTopInventory() {
        return topInventory;
    }
}
