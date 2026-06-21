package com.zaryxstudios.okaso.common.tablist;

public interface TabListManager {
    void setHeaderAndFooter(Object player, String header, String footer);
    void setHeader(Object player, String header);
    void setFooter(Object player, String footer);
    void reset(Object player);
    void setPlayerName(Object player, int index, String name);
    void setPlayerPing(Object player, int index, int ping);
    void removePlayer(Object player, int index);
}
