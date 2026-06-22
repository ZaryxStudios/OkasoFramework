package com.zaryxstudios.okaso.common.world;

import java.util.Collection;
import java.util.Optional;

public interface Structure {

    String getName();

    int getWidth();

    int getHeight();

    int getLength();

    void place(Object location);

    void place(Object location, boolean includeEntities);

    void rotate(int degrees);

    void mirror(boolean flipX, boolean flipZ);

    Structure copy();

    boolean hasBlock(int x, int y, int z);

    Optional<Object> getBlockType(int x, int y, int z);

    byte getBlockData(int x, int y, int z);

    void setBlock(int x, int y, int z, Object material);

    void setBlock(int x, int y, int z, Object material, byte data);

    void fill(int x1, int y1, int z1, int x2, int y2, int z2, Object material);

    Collection<Object[]> getBlocks();
}
