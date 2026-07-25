package com.zaryxstudios.okaso.particle;

import com.zaryxstudios.okaso.common.particle.OkasoParticleEffect;
import com.zaryxstudios.okaso.common.particle.ParticleManager;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitParticleManager implements ParticleManager {

    private final Map<String, BukkitParticleEffect> effects;

    public BukkitParticleManager() {
        this.effects = new ConcurrentHashMap<>();
    }

    @Override
    public OkasoParticleEffect createEffect(String name, String particleType) {
        BukkitParticleEffect effect = new BukkitParticleEffect(name, particleType);
        effects.put(name, effect);
        return effect;
    }

    @Override
    public Optional<OkasoParticleEffect> getEffect(String name) {
        return Optional.ofNullable(effects.get(name));
    }

    @Override
    public void removeEffect(String name) {
        effects.remove(name);
    }

    @Override
    public java.util.Collection<OkasoParticleEffect> getAllEffects() {
        return java.util.Collections.<OkasoParticleEffect>unmodifiableCollection(effects.values());
    }

    @Override
    public void clearEffects() {
        effects.clear();
    }

    @Override
    public boolean hasEffect(String name) {
        return effects.containsKey(name);
    }

    @Override
    public int getEffectCount() {
        return effects.size();
    }

    @Override
    public OkasoParticleEffect getOrCreateEffect(String name, String particleType) {
        BukkitParticleEffect existing = effects.get(name);
        if (existing != null) {
            return existing;
        }
        return createEffect(name, particleType);
    }
}
