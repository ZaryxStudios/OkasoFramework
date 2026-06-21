package com.zaryxstudios.okaso.common.hologram;

import java.util.Collection;
import java.util.Optional;

public interface HologramManager {
    Hologram createHologram(String id);
    Optional<Hologram> getHologram(String id);
    Collection<Hologram> getHolograms();
    void removeHologram(String id);
    void removeAll();
    boolean exists(String id);
    int count();
}
