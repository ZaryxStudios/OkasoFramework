package com.zaryxstudios.okaso.common.gui;

public interface GUIClickEvent {
    Object getWhoClicked();
    int getSlot();
    int getRawSlot();
    int getHotbarButton();
    boolean isLeftClick();
    boolean isRightClick();
    boolean isShiftClick();
    boolean isMiddleClick();
    boolean isDoubleClick();
    boolean isTopInventory();
    boolean isCancelled();
    void setCancelled(boolean cancelled);
    boolean isShiftLeft();
    boolean isShiftRight();
    boolean isDropAction();
    boolean isKeyboardClick();
    Object getClickItem();
    Object getCursorItem();
}
