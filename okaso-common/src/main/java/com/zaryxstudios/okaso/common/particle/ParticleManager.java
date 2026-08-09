package com.zaryxstudios.okaso.common.particle;

import java.util.Collection;
import java.util.Optional;

public interface ParticleManager {
    OkasoParticleEffect createEffect(String name, String particleType);
    Optional<OkasoParticleEffect> getEffect(String name);
    void removeEffect(String name);
    Collection<OkasoParticleEffect> getAllEffects();
    void clearEffects();
    boolean hasEffect(String name);
    int getEffectCount();
    OkasoParticleEffect getOrCreateEffect(String name, String particleType);
}
