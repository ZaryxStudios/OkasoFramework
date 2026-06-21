package com.zaryxstudios.okaso.particle;

import com.zaryxstudios.okaso.common.particle.ParticleEffect;
import com.zaryxstudios.okaso.common.particle.ParticleManager;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitParticleManager implements ParticleManager {

    private final Map<String, BukkitParticleEffect> effects;

    public BukkitParticleManager() {
        this.effects = new ConcurrentHashMap<String, BukkitParticleEffect>();
    }

    @Override
    public ParticleEffect createEffect(String name, String particleType) {
        BukkitParticleEffect effect = new BukkitParticleEffect(name, particleType);
        effects.put(name, effect);
        return effect;
    }

    @Override
    public Optional<ParticleEffect> getEffect(String name) {
        return Optional.ofNullable(effects.get(name));
    }

    @Override
    public void removeEffect(String name) {
        effects.remove(name);
    }

    @Override
    public java.util.Collection<ParticleEffect> getAllEffects() {
        return java.util.Collections.<ParticleEffect>unmodifiableCollection(effects.values());
    }

    @Override
    public void clearEffects() {
        effects.clear();
    }
}
