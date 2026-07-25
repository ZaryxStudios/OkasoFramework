package com.zaryxstudios.okaso.common.particle;

public interface ParticleEffect {
    String getName();
    void play(Object location);
    void play(Object location, int count, double offsetX, double offsetY, double offsetZ, double speed);
    void playForPlayer(Object player, Object location);
    void playForPlayer(Object player, Object location, int count, double offsetX, double offsetY, double offsetZ, double speed);
    void playInCircle(Object center, double radius, int count, double speed);
    void playInLine(Object start, Object end, int count, double speed);
    void playInSphere(Object center, double radius, int count, double speed);
    void playSpiral(Object center, double radius, double height, int turns, int pointsPerTurn, double speed);
    void playHelix(Object center, double radius, double height, int turns, int pointsPerTurn, double speed);
    void playRing(Object center, double radius, int count, double speed);
    void playArc(Object center, double radius, double startAngle, double sweepAngle, int count, double speed);
    void playRandom(Object center, double radius, int count, double speed);
    void playColumn(Object center, double height, int count, double speed);
    void playWave(Object center, double radius, double amplitude, int count, double speed);
}
