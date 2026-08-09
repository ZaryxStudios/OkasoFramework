package com.zaryxstudios.okaso.particle;

public class ParticleShape {

    public static void circle(Object center, String particleType, double radius, int count) {
        OkasoBukkitParticleEffect effect = new OkasoBukkitParticleEffect("shape_circle", particleType);
        effect.playInCircle(center, radius, count, 0);
    }

    public static void sphere(Object center, String particleType, double radius, int count) {
        OkasoBukkitParticleEffect effect = new OkasoBukkitParticleEffect("shape_sphere", particleType);
        effect.playInSphere(center, radius, count, 0);
    }

    public static void line(Object start, Object end, String particleType, int count) {
        OkasoBukkitParticleEffect effect = new OkasoBukkitParticleEffect("shape_line", particleType);
        effect.playInLine(start, end, count, 0);
    }

    public static void spiral(Object center, String particleType, double radius, double height, int turns, int pointsPerTurn) {
        OkasoBukkitParticleEffect effect = new OkasoBukkitParticleEffect("shape_spiral", particleType);
        effect.playSpiral(center, radius, height, turns, pointsPerTurn, 0);
    }

    public static void helix(Object center, String particleType, double radius, double height, int turns, int pointsPerTurn) {
        OkasoBukkitParticleEffect effect = new OkasoBukkitParticleEffect("shape_helix", particleType);
        effect.playHelix(center, radius, height, turns, pointsPerTurn, 0);
    }

    public static void ring(Object center, String particleType, double radius, int count) {
        OkasoBukkitParticleEffect effect = new OkasoBukkitParticleEffect("shape_ring", particleType);
        effect.playRing(center, radius, count, 0);
    }

    public static void arc(Object center, String particleType, double radius, double startAngle, double sweepAngle, int count) {
        OkasoBukkitParticleEffect effect = new OkasoBukkitParticleEffect("shape_arc", particleType);
        effect.playArc(center, radius, startAngle, sweepAngle, count, 0);
    }

    public static void wave(Object center, String particleType, double radius, double amplitude, int count) {
        OkasoBukkitParticleEffect effect = new OkasoBukkitParticleEffect("shape_wave", particleType);
        effect.playWave(center, radius, amplitude, count, 0);
    }

    public static void column(Object center, String particleType, double height, int count) {
        OkasoBukkitParticleEffect effect = new OkasoBukkitParticleEffect("shape_column", particleType);
        effect.playColumn(center, height, count, 0);
    }
}
