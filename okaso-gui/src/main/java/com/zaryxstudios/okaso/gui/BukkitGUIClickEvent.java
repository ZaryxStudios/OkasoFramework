package com.zaryxstudios.okaso.gui;

import com.zaryxstudios.okaso.common.gui.GUIClickEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;

@Getter
public class BukkitGUIClickEvent implements GUIClickEvent {

    private final Object whoClicked;
    private final int slot;
    private final boolean leftClick;
    private final boolean rightClick;
    private final boolean shiftClick;
    private final boolean middleClick;
    private final boolean doubleClick;
    private final Object clickItem;
    private final ClickType clickType;
    private final InventoryAction action;
    private final int hotbarButton;

    public BukkitGUIClickEvent(Object whoClicked, int slot,
                               boolean leftClick, boolean rightClick, boolean shiftClick,
                               boolean middleClick, boolean doubleClick, Object clickItem,
                               ClickType clickType, InventoryAction action, int hotbarButton) {
        this.whoClicked = whoClicked;
        this.slot = slot;
        this.leftClick = leftClick;
        this.rightClick = rightClick;
        this.shiftClick = shiftClick;
        this.middleClick = middleClick;
        this.doubleClick = doubleClick;
        this.clickItem = clickItem;
        this.clickType = clickType;
        this.action = action;
        this.hotbarButton = hotbarButton;
    }

    public Player getPlayer() {
        return whoClicked instanceof Player ? (Player) whoClicked : null;
    }

    public ItemStack getClickedItem() {
        return clickItem instanceof ItemStack ? (ItemStack) clickItem : null;
    }

    public boolean isHotbarMove() {
        return clickType == ClickType.NUMBER_KEY;
    }

    public boolean isKeyboardClick() {
        return clickType == ClickType.NUMBER_KEY;
    }

    public boolean isCreativeAction() {
        return action == InventoryAction.CLONE_STACK;
    }

    public boolean isDropAction() {
        return action == InventoryAction.DROP_ALL_CURSOR || action == InventoryAction.DROP_ALL_SLOT
            || action == InventoryAction.DROP_ONE_CURSOR || action == InventoryAction.DROP_ONE_SLOT;
    }
}
