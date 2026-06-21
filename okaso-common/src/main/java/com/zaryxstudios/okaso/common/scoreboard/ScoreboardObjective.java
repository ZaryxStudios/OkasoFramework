package com.zaryxstudios.okaso.common.scoreboard;

import java.util.List;

public interface ScoreboardObjective {
    String getTitle();
    void setTitle(String title);
    List<String> getLines();
    void setLines(List<String> lines);
    void setLine(int index, String text);
    void addLine(String text);
    void removeLine(int index);
    void insertLine(int index, String text);
    int getLineCount();
    void clear();
}
