package com.zaryxstudios.okaso.scoreboard;

import com.zaryxstudios.okaso.common.scoreboard.OkasoScoreboardManager;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class OkasoBukkitScoreboardManager implements OkasoScoreboardManager {

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

    private final Map<UUID, OkasoBukkitScoreboardObjective> objectives;

    public OkasoBukkitScoreboardManager() {
        this.objectives = new ConcurrentHashMap<>();
    }

    @Override
    public void setScoreboard(Object player, OkasoScoreboardObjective objective) {
        if (!(player instanceof Player)) return;

        Player p = (Player) player;
        if (objective instanceof OkasoBukkitScoreboardObjective) {
            OkasoBukkitScoreboardObjective bukkitObj = (OkasoBukkitScoreboardObjective) objective;
            bukkitObj.applyTo(p);
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
    public Optional<OkasoScoreboardObjective> getCurrentObjective(Object player) {
        if (player instanceof Player) {
            OkasoBukkitScoreboardObjective obj = objectives.get(((Player) player).getUniqueId());
            return Optional.ofNullable((OkasoScoreboardObjective) obj);
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
        Objective obj = board.getObjective(objectiveName);
        if (obj == null) return 0;
        Score score = obj.getScore(p.getName());
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

    public Collection<Player> getPlayersWithScoreboard() {
        return objectives.keySet().stream()
            .map(Bukkit::getPlayer)
            .filter(p -> p != null && p.isOnline())
            .collect(Collectors.toList());
    }

    public int getPlayerCount() {
        return (int) objectives.keySet().stream()
            .map(Bukkit::getPlayer)
            .filter(p -> p != null && p.isOnline())
            .count();
    }

    public int cleanupOfflinePlayers() {
        List<UUID> toRemove = new ArrayList<>();
        for (UUID uuid : objectives.keySet()) {
            if (Bukkit.getPlayer(uuid) == null) {
                toRemove.add(uuid);
            }
        }
        for (UUID uuid : toRemove) {
            objectives.remove(uuid);
        }
        return toRemove.size();
    }

    public OkasoBukkitScoreboardObjective createObjective(String title) {
        return new OkasoBukkitScoreboardObjective(title);
    }

    public OkasoBukkitScoreboardObjective createObjective(String title, List<String> lines) {
        OkasoBukkitScoreboardObjective obj = new OkasoBukkitScoreboardObjective(title);
        obj.setLines(lines);
        return obj;
    }

    public void updateAll(OkasoBukkitScoreboardObjective objective) {
        for (Player player : getPlayersWithScoreboard()) {
            if (objectives.get(player.getUniqueId()) == objective) {
                setScoreboard(player, objective);
            }
        }
    }

    public void removeObjective(OkasoBukkitScoreboardObjective objective) {
        objectives.entrySet().removeIf(entry -> entry.getValue() == objective);
    }
}
