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

    public static void heart(Object center, String particleType, double size, int count) {
        OkasoBukkitParticleEffect effect = new OkasoBukkitParticleEffect("shape_heart", particleType);
        effect.playHeart(center, size, count, 0);
    }

    public static void star(Object center, String particleType, double size, int points, int count) {
        OkasoBukkitParticleEffect effect = new OkasoBukkitParticleEffect("shape_star", particleType);
        effect.playStar(center, size, points, count, 0);
    }

    public static void square(Object center, String particleType, double size, int count) {
        OkasoBukkitParticleEffect effect = new OkasoBukkitParticleEffect("shape_square", particleType);
        effect.playSquare(center, size, count, 0);
    }

    public static void triangle(Object center, String particleType, double size, int count) {
        OkasoBukkitParticleEffect effect = new OkasoBukkitParticleEffect("shape_triangle", particleType);
        effect.playTriangle(center, size, count, 0);
    }

    public static void cone(Object center, String particleType, double radius, double height, int count) {
        OkasoBukkitParticleEffect effect = new OkasoBukkitParticleEffect("shape_cone", particleType);
        effect.playCone(center, radius, height, count, 0);
    }

    public static void tornado(Object center, String particleType, double radius, double height, int turns, int pointsPerTurn) {
        OkasoBukkitParticleEffect effect = new OkasoBukkitParticleEffect("shape_tornado", particleType);
        effect.playTornado(center, radius, height, turns, pointsPerTurn, 0);
    }

    public static void dna(Object center, String particleType, double radius, double height, int turns, int pointsPerTurn) {
        OkasoBukkitParticleEffect effect = new OkasoBukkitParticleEffect("shape_dna", particleType);
        effect.playDNA(center, radius, height, turns, pointsPerTurn, 0);
    }

    public static void cube(Object center, String particleType, double size, int count) {
        OkasoBukkitParticleEffect effect = new OkasoBukkitParticleEffect("shape_cube", particleType);
        effect.playCube(center, size, count, 0);
    }

    public static void pyramid(Object center, String particleType, double baseSize, double height, int count) {
        OkasoBukkitParticleEffect effect = new OkasoBukkitParticleEffect("shape_pyramid", particleType);
        effect.playPyramid(center, baseSize, height, count, 0);
    }
}
