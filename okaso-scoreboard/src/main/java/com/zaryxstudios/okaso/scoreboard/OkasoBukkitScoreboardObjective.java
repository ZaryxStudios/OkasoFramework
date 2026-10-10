package com.zaryxstudios.okaso.scoreboard;

import com.zaryxstudios.okaso.common.scoreboard.OkasoScoreboardObjective;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

public class OkasoBukkitScoreboardObjective implements OkasoScoreboardObjective {

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
    private DisplaySlot displaySlot = DisplaySlot.SIDEBAR;
    private String criteria = "dummy";

    public OkasoBukkitScoreboardObjective(String title) {
        this.title = title;
        this.lines = new ArrayList<>();
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public List<String> getLines() {
        return new ArrayList<>(lines);
    }

    @Override
    public void setLines(List<String> lines) {
        this.lines.clear();
        if (lines != null) {
            this.lines.addAll(lines);
        }
    }

    @Override
    public void setLine(int index, String text) {
        if (index >= 0 && index < lines.size()) {
            lines.set(index, text);
        }
    }

    @Override
    public void addLine(String text) {
        if (text != null) {
            lines.add(text);
        }
    }

    @Override
    public void removeLine(int index) {
        if (index >= 0 && index < lines.size()) {
            lines.remove(index);
        }
    }

    @Override
    public void insertLine(int index, String text) {
        if (index >= 0 && index <= lines.size() && text != null) {
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

    public DisplaySlot getDisplaySlot() {
        return displaySlot;
    }

    public void setDisplaySlot(DisplaySlot displaySlot) {
        this.displaySlot = displaySlot;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria != null ? criteria : "dummy";
    }

    public Scoreboard createScoreboardFor(Player player) {
        Scoreboard board = createNewScoreboard();
        Objective obj;
        if (REGISTER_OBJ_3ARG != null) {
            try {
                obj = (Objective) REGISTER_OBJ_3ARG.invoke(board, "okaso_" + player.getUniqueId().toString().replace("-", ""), criteria, title);
            } catch (Exception e) {
                obj = board.registerNewObjective("okaso_" + player.getUniqueId().toString().replace("-", ""), criteria);
                obj.setDisplayName(title);
            }
        } else {
            obj = board.registerNewObjective("okaso_" + player.getUniqueId().toString().replace("-", ""), criteria);
            obj.setDisplayName(title);
        }
        obj.setDisplaySlot(displaySlot);

        for (int i = 0; i < lines.size(); i++) {
            String lineText = lines.get(i);
            if (lineText == null) continue;

            String teamName = "line_" + i;
            Team team = board.getTeam(teamName);
            if (team == null) {
                team = board.registerNewTeam(teamName);
            }
            team.setPrefix("");
            team.setSuffix("");

            String entry = ChatColor.values()[i % ChatColor.values().length] + "" + ChatColor.RESET + lineText;
            if (entry.length() > 40) {
                entry = entry.substring(0, 40);
            }
            team.addEntry(entry);

            Score score = obj.getScore(entry);
            score.setScore(lines.size() - i);
        }

        return board;
    }

    public void updateScoreboard(Player player, Scoreboard board) {
        Objective obj = board.getObjective("okaso_" + player.getUniqueId().toString().replace("-", ""));
        if (obj == null) {
            // Try to find any objective with our pattern
            for (Objective o : board.getObjectives()) {
                if (o.getName().startsWith("okaso_")) {
                    obj = o;
                    break;
                }
            }
        }
        if (obj == null) return;

        obj.setDisplayName(title);
        obj.setDisplaySlot(displaySlot);

        for (int i = 0; i < lines.size(); i++) {
            String lineText = lines.get(i);
            if (lineText == null) continue;

            String teamName = "line_" + i;
            Team team = board.getTeam(teamName);
            if (team == null) {
                team = board.registerNewTeam(teamName);
            }

            for (String entry : new ArrayList<>(team.getEntries())) {
                team.removeEntry(entry);
            }

            String entry = ChatColor.values()[i % ChatColor.values().length] + "" + ChatColor.RESET + lineText;
            if (entry.length() > 40) {
                entry = entry.substring(0, 40);
            }
            team.addEntry(entry);

            Score score = obj.getScore(entry);
            score.setScore(lines.size() - i);
        }

        for (Team team : new ArrayList<>(board.getTeams())) {
            if (team.getName().startsWith("line_")) {
                int lineIndex;
                try {
                    lineIndex = Integer.parseInt(team.getName().substring(5));
                } catch (NumberFormatException e) {
                    continue;
                }
                if (lineIndex >= lines.size()) {
                    for (String entry : new ArrayList<>(team.getEntries())) {
                        obj.getScoreboard().resetScores(entry);
                    }
                    team.unregister();
                }
            }
        }
    }

    public void applyTo(Player player) {
        Scoreboard board = createScoreboardFor(player);
        player.setScoreboard(board);
    }

    public void updateFor(Player player) {
        Scoreboard board = player.getScoreboard();
        if (board != null) {
            updateScoreboard(player, board);
        } else {
            applyTo(player);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String title;
        private List<String> lines = new ArrayList<>();
        private DisplaySlot displaySlot = DisplaySlot.SIDEBAR;
        private String criteria = "dummy";

        private Builder() {}

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder line(String line) {
            if (line != null) {
                this.lines.add(line);
            }
            return this;
        }

        public Builder lines(List<String> lines) {
            if (lines != null) {
                this.lines.addAll(lines);
            }
            return this;
        }

        public Builder displaySlot(DisplaySlot displaySlot) {
            this.displaySlot = displaySlot;
            return this;
        }

        public Builder criteria(String criteria) {
            this.criteria = criteria;
            return this;
        }

        public OkasoBukkitScoreboardObjective build() {
            if (title == null) {
                throw new IllegalStateException("Title is required");
            }
            OkasoBukkitScoreboardObjective obj = new OkasoBukkitScoreboardObjective(title);
            obj.setLines(lines);
            obj.setDisplaySlot(displaySlot);
            obj.setCriteria(criteria);
            return obj;
        }
    }
}
