package com.zaryxstudios.okaso.common.hologram;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface HologramManager {

    Hologram createHologram(String id);
    Hologram createHologram(String id, HologramLine... lines);
    Hologram createHologram(String id, List<HologramLine> lines);

    Optional<Hologram> getHologram(String id);
    Collection<Hologram> getHolograms();
    void removeHologram(String id);
    void removeAll();
    boolean exists(String id);
    int count();
}
