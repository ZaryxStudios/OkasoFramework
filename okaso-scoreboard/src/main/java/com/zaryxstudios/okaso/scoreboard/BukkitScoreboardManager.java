package com.zaryxstudios.okaso.scoreboard;

import com.zaryxstudios.okaso.common.scoreboard.ScoreboardManager;
import com.zaryxstudios.okaso.common.scoreboard.ScoreboardObjective;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitScoreboardManager implements ScoreboardManager {

    private static final Method GET_NEW_SCOREBOARD;

    static {
        Method m = null;
        try {
            m = org.bukkit.scoreboard.ScoreboardManager.class.getMethod("getNewScoreboard");
        } catch (Exception ignored) {
        }
        GET_NEW_SCOREBOARD = m;
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

    private final Map<UUID, BukkitScoreboardObjective> objectives;

    public BukkitScoreboardManager() {
        this.objectives = new ConcurrentHashMap<UUID, BukkitScoreboardObjective>();
    }

    @Override
    public void setScoreboard(Object player, ScoreboardObjective objective) {
        if (!(player instanceof Player)) return;

        Player p = (Player) player;
        if (objective instanceof BukkitScoreboardObjective) {
            BukkitScoreboardObjective bukkitObj = (BukkitScoreboardObjective) objective;
            Scoreboard board = bukkitObj.getOrCreateScoreboard();
            p.setScoreboard(board);
            objectives.put(p.getUniqueId(), bukkitObj);
        }
    }

    @Override
    public void clearScoreboard(Object player) {
        if (player instanceof Player) {
            Player p = (Player) player;
            p.setScoreboard(createNewScoreboard());
            objectives.remove(p.getUniqueId());
        }
    }

    @Override
    public Optional<ScoreboardObjective> getCurrentObjective(Object player) {
        if (player instanceof Player) {
            BukkitScoreboardObjective obj = objectives.get(((Player) player).getUniqueId());
            return Optional.ofNullable((ScoreboardObjective) obj);
        }
        return Optional.empty();
    }

    @Override
    public void setScore(Object player, String objectiveName, int score) {
        if (!(player instanceof Player)) return;
        Player p = (Player) player;
        Scoreboard board = p.getScoreboard();
        if (board == null) return;
        org.bukkit.scoreboard.Objective obj = board.getObjective(objectiveName);
        if (obj == null) return;
        obj.getScore(p.getName()).setScore(score);
    }

    @Override
    public int getScore(Object player, String objectiveName) {
        if (!(player instanceof Player)) return 0;
        Player p = (Player) player;
        Scoreboard board = p.getScoreboard();
        if (board == null) return 0;
        org.bukkit.scoreboard.Objective obj = board.getObjective(objectiveName);
        if (obj == null) return 0;
        org.bukkit.scoreboard.Score score = obj.getScore(p.getName());
        return score != null ? score.getScore() : 0;
    }

    @Override
    public void resetScore(Object player, String objectiveName) {
        if (!(player instanceof Player)) return;
        Player p = (Player) player;
        Scoreboard board = p.getScoreboard();
        if (board == null) return;
        board.resetScores(p.getName());
    }
}
