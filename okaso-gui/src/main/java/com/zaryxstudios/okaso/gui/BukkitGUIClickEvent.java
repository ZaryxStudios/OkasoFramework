package com.zaryxstudios.okaso.gui;

import com.zaryxstudios.okaso.common.gui.GUIClickEvent;

public class BukkitGUIClickEvent implements GUIClickEvent {

    private final Object whoClicked;
    private final int slot;
    private final boolean leftClick;
    private final boolean rightClick;
    private final boolean shiftClick;
    private final boolean middleClick;
    private final boolean doubleClick;
    private final Object clickItem;

    public BukkitGUIClickEvent(Object whoClicked, int slot,
                               boolean leftClick, boolean rightClick, boolean shiftClick,
                               boolean middleClick, boolean doubleClick, Object clickItem) {
        this.whoClicked = whoClicked;
        this.slot = slot;
        this.leftClick = leftClick;
        this.rightClick = rightClick;
        this.shiftClick = shiftClick;
        this.middleClick = middleClick;
        this.doubleClick = doubleClick;
        this.clickItem = clickItem;
    }

    @Override
    public Object getWhoClicked() {
        return whoClicked;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public boolean isLeftClick() {
        return leftClick;
    }

    @Override
    public boolean isRightClick() {
        return rightClick;
    }

    @Override
    public boolean isShiftClick() {
        return shiftClick;
    }

    @Override
    public boolean isMiddleClick() {
        return middleClick;
    }

    @Override
    public boolean isDoubleClick() {
        return doubleClick;
    }

    @Override
    public Object getClickItem() {
        return clickItem;
    }
}
