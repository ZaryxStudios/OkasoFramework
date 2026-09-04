package com.zaryxstudios.okaso.common.entity;

import java.util.List;

public interface NPCSerializer {
    void saveAll(List<NPCData> npcs);
    List<NPCData> loadAll();
    void save(NPCData npc);
    void delete(String id);
    boolean exists(String id);
}