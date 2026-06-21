package com.zaryxstudios.okaso.common.gui;

public interface GUIClickEvent {
    Object getWhoClicked();
    int getSlot();
    boolean isLeftClick();
    boolean isRightClick();
    boolean isShiftClick();
    boolean isMiddleClick();
    boolean isDoubleClick();
    Object getClickItem();
}
