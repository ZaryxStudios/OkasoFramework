package com.zaryxstudios.okaso.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.zaryxstudios.okaso.common.entity.NPCData;
import com.zaryxstudios.okaso.common.entity.NPCSerializer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BukkitNPCSerializer implements NPCSerializer {

    private final File dataFile;
    private final ObjectMapper mapper;

    public BukkitNPCSerializer(File dataFile) {
        this.dataFile = dataFile;
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void saveAll(List<NPCData> npcs) {
        try {
            if (!dataFile.getParentFile().exists()) {
                dataFile.getParentFile().mkdirs();
            }
            mapper.writeValue(dataFile, npcs);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save NPC data", e);
        }
    }

    @Override
    public List<NPCData> loadAll() {
        if (!dataFile.exists()) return new ArrayList<>();
        try {
            return mapper.readValue(dataFile,
                mapper.getTypeFactory().constructCollectionType(List.class, NPCData.class));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load NPC data", e);
        }
    }

    @Override
    public void save(NPCData npc) {
        List<NPCData> all = loadAll();
        all.removeIf(existing -> existing.getId() != null && existing.getId().equals(npc.getId()));
        all.add(npc);
        saveAll(all);
    }

    @Override
    public void delete(String id) {
        List<NPCData> all = loadAll();
        all.removeIf(existing -> existing.getId() != null && existing.getId().equals(id));
        saveAll(all);
    }

    @Override
    public boolean exists(String id) {
        return loadAll().stream().anyMatch(npc -> id.equals(npc.getId()));
    }
}