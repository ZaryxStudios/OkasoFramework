package com.zaryxstudios.okaso.common.particle;

import java.util.Collection;
import java.util.Optional;

public interface ParticleManager {
    ParticleEffect createEffect(String name, String particleType);
    Optional<ParticleEffect> getEffect(String name);
    void removeEffect(String name);
    Collection<ParticleEffect> getAllEffects();
    void clearEffects();
}
