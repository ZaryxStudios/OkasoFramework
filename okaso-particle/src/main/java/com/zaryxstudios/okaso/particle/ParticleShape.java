package com.zaryxstudios.okaso.particle;

import com.zaryxstudios.okaso.common.OkasoAPI;
import com.zaryxstudios.okaso.common.particle.ParticleEffect;
import com.zaryxstudios.okaso.common.particle.ParticleManager;

import org.bukkit.Location;

public class ParticleShape {

    public static void circle(Object center, String particleType, double radius, int count) {
        ParticleManager manager = OkasoAPI.service(ParticleManager.class);
        if (manager == null) return;
        ParticleEffect effect = manager.getOrCreateEffect("shape_circle", particleType);
        effect.playInCircle(center, radius, count, 0);
    }

    public static void sphere(Object center, String particleType, double radius, int count) {
        ParticleManager manager = OkasoAPI.service(ParticleManager.class);
        if (manager == null) return;
        ParticleEffect effect = manager.getOrCreateEffect("shape_sphere", particleType);
        effect.playInSphere(center, radius, count, 0);
    }

    public static void line(Object start, Object end, String particleType, int count) {
        ParticleManager manager = OkasoAPI.service(ParticleManager.class);
        if (manager == null) return;
        ParticleEffect effect = manager.getOrCreateEffect("shape_line", particleType);
        effect.playInLine(start, end, count, 0);
    }

    public static void spiral(Object center, String particleType, double radius, double height, int turns, int pointsPerTurn) {
        ParticleManager manager = OkasoAPI.service(ParticleManager.class);
        if (manager == null) return;
        ParticleEffect effect = manager.getOrCreateEffect("shape_spiral", particleType);
        effect.playSpiral(center, radius, height, turns, pointsPerTurn, 0);
    }

    public static void helix(Object center, String particleType, double radius, double height, int turns, int pointsPerTurn) {
        ParticleManager manager = OkasoAPI.service(ParticleManager.class);
        if (manager == null) return;
        ParticleEffect effect = manager.getOrCreateEffect("shape_helix", particleType);
        effect.playHelix(center, radius, height, turns, pointsPerTurn, 0);
    }

    public static void ring(Object center, String particleType, double radius, int count) {
        ParticleManager manager = OkasoAPI.service(ParticleManager.class);
        if (manager == null) return;
        ParticleEffect effect = manager.getOrCreateEffect("shape_ring", particleType);
        effect.playRing(center, radius, count, 0);
    }

    public static void arc(Object center, String particleType, double radius, double startAngle, double sweepAngle, int count) {
        ParticleManager manager = OkasoAPI.service(ParticleManager.class);
        if (manager == null) return;
        ParticleEffect effect = manager.getOrCreateEffect("shape_arc", particleType);
        effect.playArc(center, radius, startAngle, sweepAngle, count, 0);
    }

    public static void wave(Object center, String particleType, double radius, double amplitude, int count) {
        ParticleManager manager = OkasoAPI.service(ParticleManager.class);
        if (manager == null) return;
        ParticleEffect effect = manager.getOrCreateEffect("shape_wave", particleType);
        effect.playWave(center, radius, amplitude, count, 0);
    }

    public static void column(Object center, String particleType, double height, int count) {
        ParticleManager manager = OkasoAPI.service(ParticleManager.class);
        if (manager == null) return;
        ParticleEffect effect = manager.getOrCreateEffect("shape_column", particleType);
        effect.playColumn(center, height, count, 0);
    }
}
