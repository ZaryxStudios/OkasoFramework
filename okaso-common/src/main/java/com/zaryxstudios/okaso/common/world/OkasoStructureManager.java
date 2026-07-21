package com.zaryxstudios.okaso.common.world;

import java.util.Collection;
import java.util.Optional;

public interface OkasoStructureManager {

    boolean saveStructure(String name, Object world, int x1, int y1, int z1, int x2, int y2, int z2);

    Optional<OkasoStructure> loadStructure(String name);

    boolean placeStructure(String name, Object location);

    boolean placeStructure(OkasoStructure structure, Object location);

    Collection<String> getAvailableStructures();

    OkasoStructure createStructure(String name, int width, int height, int length);

    boolean deleteStructure(String name);

    boolean exportStructure(String name, String filePath);

    boolean importStructure(String name, String filePath);

    void setStructureDirectory(String path);
}
