package com.zaryxstudios.okaso.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.zaryxstudios.okaso.common.entity.NPCData;
import com.zaryxstudios.okaso.common.entity.NPCSerializer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;

public class BukkitNPCSerializer implements NPCSerializer {

    private final File dataFile;
    private final ObjectMapper mapper;

    public BukkitNPCSerializer(File dataFile) {
        if (dataFile == null) {
            throw new IllegalArgumentException("NPC data file cannot be null");
        }
        this.dataFile = dataFile;
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public synchronized void saveAll(List<NPCData> npcs) {
        if (npcs == null) {
            throw new IllegalArgumentException("NPC data cannot be null");
        }
        File temporaryFile = new File(dataFile.getPath() + ".tmp");
        try {
            File parent = dataFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IOException("Could not create NPC data directory");
            }
            mapper.writeValue(temporaryFile, npcs);
            try {
                Files.move(temporaryFile.toPath(), dataFile.toPath(), StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporaryFile.toPath(), dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            temporaryFile.delete();
            throw new RuntimeException("Failed to save NPC data", e);
        }
    }

    @Override
    public synchronized List<NPCData> loadAll() {
        if (!dataFile.exists()) return new ArrayList<>();
        try {
            List<NPCData> result = mapper.readValue(dataFile,
                mapper.getTypeFactory().constructCollectionType(List.class, NPCData.class));
            return result == null ? new ArrayList<>() : result;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load NPC data", e);
        }
    }

    @Override
    public synchronized void save(NPCData npc) {
        if (npc == null || npc.getId() == null || npc.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("NPC and NPC id are required");
        }
        List<NPCData> all = loadAll();
        all.removeIf(existing -> existing.getId() != null && existing.getId().equals(npc.getId()));
        all.add(npc);
        saveAll(all);
    }

    @Override
    public synchronized void delete(String id) {
        if (id == null || id.trim().isEmpty()) return;
        List<NPCData> all = loadAll();
        all.removeIf(existing -> existing.getId() != null && existing.getId().equals(id));
        saveAll(all);
    }

    @Override
    public synchronized boolean exists(String id) {
        return id != null && loadAll().stream().anyMatch(npc -> id.equals(npc.getId()));
    }
}