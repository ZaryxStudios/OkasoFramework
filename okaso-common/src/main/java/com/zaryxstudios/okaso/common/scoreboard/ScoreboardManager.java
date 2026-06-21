package com.zaryxstudios.okaso.common.scoreboard;

import java.util.Optional;

public interface ScoreboardManager {
    void setScoreboard(Object player, ScoreboardObjective objective);
    void clearScoreboard(Object player);
    Optional<ScoreboardObjective> getCurrentObjective(Object player);
    void setScore(Object player, String objectiveName, int score);
    int getScore(Object player, String objectiveName);
    void resetScore(Object player, String objectiveName);
}
