package com.zaryxstudios.okaso.scoreboard;

import com.zaryxstudios.okaso.common.scoreboard.ScoreboardObjective;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class BukkitScoreboardObjective implements ScoreboardObjective {

    private static final Method GET_NEW_SCOREBOARD;
    private static final Method REGISTER_OBJ_3ARG;

    static {
        Method m1 = null, m2 = null;
        try {
            m1 = org.bukkit.scoreboard.ScoreboardManager.class.getMethod("getNewScoreboard");
        } catch (Exception ignored) {
        }
        try {
            m2 = Scoreboard.class.getMethod("registerNewObjective", String.class, String.class, String.class);
        } catch (Exception ignored) {
        }
        GET_NEW_SCOREBOARD = m1;
        REGISTER_OBJ_3ARG = m2;
    }

    private static Scoreboard createNewScoreboard() {
        org.bukkit.scoreboard.ScoreboardManager mgr = Bukkit.getScoreboardManager();
        if (GET_NEW_SCOREBOARD != null) {
            try {
                return (Scoreboard) GET_NEW_SCOREBOARD.invoke(mgr);
            } catch (Exception ignored) {
            }
        }
        return mgr.getMainScoreboard();
    }

    @Getter @Setter
    private String title;
    private final List<String> lines;

    public BukkitScoreboardObjective(String title) {
        this.title = title;
        this.lines = new ArrayList<String>();
    }

    @Override
    public List<String> getLines() {
        return new ArrayList<String>(lines);
    }

    @Override
    public void setLines(List<String> lines) {
        this.lines.clear();
        this.lines.addAll(lines);
    }

    @Override
    public void setLine(int index, String text) {
        if (index >= 0 && index < lines.size()) {
            lines.set(index, text);
        }
    }

    @Override
    public void addLine(String text) {
        lines.add(text);
    }

    @Override
    public void removeLine(int index) {
        if (index >= 0 && index < lines.size()) {
            lines.remove(index);
        }
    }

    @Override
    public void insertLine(int index, String text) {
        if (index >= 0 && index <= lines.size()) {
            lines.add(index, text);
        }
    }

    @Override
    public int getLineCount() {
        return lines.size();
    }

    @Override
    public void clear() {
        lines.clear();
    }

    public Scoreboard getOrCreateScoreboard() {
        Scoreboard board = createNewScoreboard();
        Objective obj;
        if (REGISTER_OBJ_3ARG != null) {
            try {
                obj = (Objective) REGISTER_OBJ_3ARG.invoke(board, "okaso", "dummy", title);
            } catch (Exception e) {
                obj = board.registerNewObjective("okaso", "dummy");
                obj.setDisplayName(title);
            }
        } else {
            obj = board.registerNewObjective("okaso", "dummy");
            obj.setDisplayName(title);
        }
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (int i = 0; i < lines.size(); i++) {
            String text = lines.get(i);
            if (text.length() > 40) text = text.substring(0, 40);
            String teamName = ChatColor.values()[i % ChatColor.values().length] + "" + ChatColor.RESET;
            Score score = obj.getScore(teamName + text);
            score.setScore(lines.size() - i);
        }

        return board;
    }
}
