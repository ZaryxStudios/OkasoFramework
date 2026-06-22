package com.zaryxstudios.okaso.world.structure;

import org.bukkit.Material;

public class StructureBuilder {

    private final BukkitStructure structure;

    public StructureBuilder(String name, int width, int height, int length) {
        this.structure = new BukkitStructure(name, width, height, length);
    }

    public StructureBuilder setBlock(int x, int y, int z, Material material) {
        structure.setBlock(x, y, z, material);
        return this;
    }

    public StructureBuilder setBlock(int x, int y, int z, Material material, byte data) {
        structure.setBlock(x, y, z, material, data);
        return this;
    }

    public StructureBuilder fill(int x1, int y1, int z1, int x2, int y2, int z2, Material material) {
        structure.fill(x1, y1, z1, x2, y2, z2, material);
        return this;
    }

    public StructureBuilder hollow(int x1, int y1, int z1, int x2, int y2, int z2, Material material) {
        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);
        int maxZ = Math.max(z1, z2);

        fill(minX, minY, minZ, maxX, minY, maxZ, material);
        fill(minX, maxY, minZ, maxX, maxY, maxZ, material);
        fill(minX, minY, minZ, minX, maxY, maxZ, material);
        fill(maxX, minY, minZ, maxX, maxY, maxZ, material);
        fill(minX, minY, minZ, maxX, maxY, minZ, material);
        fill(minX, minY, maxZ, maxX, maxY, maxZ, material);

        return this;
    }

    public StructureBuilder layer(int y, Material material) {
        structure.fill(0, y, 0, structure.getWidth() - 1, y, structure.getLength() - 1, material);
        return this;
    }

    public StructureBuilder clear() {
        for (int x = 0; x < structure.getWidth(); x++) {
            for (int y = 0; y < structure.getHeight(); y++) {
                for (int z = 0; z < structure.getLength(); z++) {
                    structure.setBlock(x, y, z, Material.AIR);
                }
            }
        }
        return this;
    }

    public BukkitStructure build() {
        return structure;
    }
}
