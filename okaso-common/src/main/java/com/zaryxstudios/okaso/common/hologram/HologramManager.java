package com.zaryxstudios.okaso.common.hologram;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface HologramManager {

    OkasoHologram createHologram(String id);
    OkasoHologram createHologram(String id, HologramLine... lines);
    OkasoHologram createHologram(String id, List<HologramLine> lines);

    Optional<OkasoHologram> getHologram(String id);
    Collection<OkasoHologram> getHolograms();
    void removeHologram(String id);
    void removeAll();
    boolean exists(String id);
    int count();
}
