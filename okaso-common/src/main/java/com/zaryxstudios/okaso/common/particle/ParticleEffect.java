package com.zaryxstudios.okaso.common.particle;

public interface ParticleEffect {
    String getName();
    void play(Object location);
    void play(Object location, int count, double offsetX, double offsetY, double offsetZ, double speed);
}
