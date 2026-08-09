package com.zaryxstudios.okaso.particle;

import com.zaryxstudios.okaso.common.OkasoAPI;
import com.zaryxstudios.okaso.common.particle.OkasoParticleEffect;
import com.zaryxstudios.okaso.common.particle.ParticleManager;

public class ParticleBuilder {

    private String effectName;
    private String particleType;
    private int count = 1;
    private double offsetX = 0;
    private double offsetY = 0;
    private double offsetZ = 0;
    private double speed = 0;
    private Object location;
    private Object player;

    public static ParticleBuilder create(String name, String particleType) {
        ParticleBuilder builder = new ParticleBuilder();
        builder.effectName = name;
        builder.particleType = particleType;
        return builder;
    }

    public ParticleBuilder count(int count) {
        this.count = count;
        return this;
    }

    public ParticleBuilder offsets(double x, double y, double z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        return this;
    }

    public ParticleBuilder speed(double speed) {
        this.speed = speed;
        return this;
    }

    public ParticleBuilder at(Object location) {
        this.location = location;
        return this;
    }

    public ParticleBuilder forPlayer(Object player) {
        this.player = player;
        return this;
    }

    public void play() {
        if (effectName == null || location == null) return;
        ParticleManager manager = OkasoAPI.service(ParticleManager.class);
        if (manager == null) return;
        OkasoParticleEffect effect = manager.getOrCreateEffect(effectName, particleType);
        if (player != null) {
            effect.playForPlayer(player, location, count, offsetX, offsetY, offsetZ, speed);
        } else {
            effect.play(location, count, offsetX, offsetY, offsetZ, speed);
        }
    }
}
