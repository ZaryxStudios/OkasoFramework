package com.zaryxstudios.okaso.world.structure;

import com.zaryxstudios.okaso.common.world.Structure;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class BukkitStructure implements Structure {

    private final String name;
    private int width;
    private int height;
    private int length;
    private Material[][][] blocks;
    private byte[][][] blockData;

    public BukkitStructure(String name, int width, int height, int length) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.length = length;
        this.blocks = new Material[width][height][length];
        this.blockData = new byte[width][height][length];
    }

    private BukkitStructure(BukkitStructure original) {
        this.name = original.name;
        this.width = original.width;
        this.height = original.height;
        this.length = original.length;
        this.blocks = new Material[width][height][length];
        this.blockData = new byte[width][height][length];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    this.blocks[x][y][z] = original.blocks[x][y][z];
                    this.blockData[x][y][z] = original.blockData[x][y][z];
                }
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public void place(Object location) {
        place(location, false);
    }

    @Override
    public void place(Object location, boolean includeEntities) {
        if (!(location instanceof Location)) {
            throw new IllegalArgumentException("Location must be a Bukkit Location");
        }
        Location loc = (Location) location;
        World world = loc.getWorld();
        if (world == null) {
            throw new IllegalArgumentException("Location world cannot be null");
        }

        int baseX = loc.getBlockX();
        int baseY = loc.getBlockY();
        int baseZ = loc.getBlockZ();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    Material mat = blocks[x][y][z];
                    if (mat == null || mat == Material.AIR) {
                        continue;
                    }
                    Block block = world.getBlockAt(baseX + x, baseY + y, baseZ + z);

                    MaterialResolver.ensureChunkLoaded(block);

                    MaterialResolver.setBlockSilently(block, mat);

                    MaterialResolver.setLegacyData(block, blockData[x][y][z]);
                }
            }
        }
    }

    @Override
    public void rotate(int degrees) {
        if (degrees % 90 != 0) {
            throw new IllegalArgumentException("Rotation must be a multiple of 90");
        }
        int rotations = ((degrees % 360) / 90 + 4) % 4;
        for (int i = 0; i < rotations; i++) {
            rotate90();
        }
    }

    private void rotate90() {
        int newWidth = length;
        int newLength = width;
        Material[][][] newBlocks = new Material[newWidth][height][newLength];
        byte[][][] newData = new byte[newWidth][height][newLength];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    newBlocks[z][y][width - 1 - x] = blocks[x][y][z];
                    newData[z][y][width - 1 - x] = blockData[x][y][z];
                }
            }
        }

        this.width = newWidth;
        this.length = newLength;
        this.blocks = newBlocks;
        this.blockData = newData;
    }

    @Override
    public void mirror(boolean flipX, boolean flipZ) {
        Material[][][] newBlocks = new Material[width][height][length];
        byte[][][] newData = new byte[width][height][length];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    int srcX = flipX ? width - 1 - x : x;
                    int srcZ = flipZ ? length - 1 - z : z;
                    newBlocks[x][y][z] = blocks[srcX][y][srcZ];
                    newData[x][y][z] = blockData[srcX][y][srcZ];
                }
            }
        }

        this.blocks = newBlocks;
        this.blockData = newData;
    }

    @Override
    public Structure copy() {
        return new BukkitStructure(this);
    }

    @Override
    public boolean hasBlock(int x, int y, int z) {
        checkBounds(x, y, z);
        return blocks[x][y][z] != null && blocks[x][y][z] != Material.AIR;
    }

    @Override
    public Optional<Object> getBlockType(int x, int y, int z) {
        checkBounds(x, y, z);
        return Optional.ofNullable(blocks[x][y][z]);
    }

    @Override
    public byte getBlockData(int x, int y, int z) {
        checkBounds(x, y, z);
        return blockData[x][y][z];
    }

    @Override
    public void setBlock(int x, int y, int z, Object material) {
        setBlock(x, y, z, material, (byte) 0);
    }

    @Override
    public void setBlock(int x, int y, int z, Object material, byte data) {
        checkBounds(x, y, z);
        if (material instanceof Material) {
            blocks[x][y][z] = (Material) material;
        } else if (material instanceof String) {
            blocks[x][y][z] = Material.getMaterial((String) material);
        } else {
            throw new IllegalArgumentException("Material must be a Bukkit Material or a String name");
        }
        blockData[x][y][z] = data;
    }

    @Override
    public void fill(int x1, int y1, int z1, int x2, int y2, int z2, Object material) {
        int minX = Math.max(0, Math.min(x1, x2));
        int minY = Math.max(0, Math.min(y1, y2));
        int minZ = Math.max(0, Math.min(z1, z2));
        int maxX = Math.min(width - 1, Math.max(x1, x2));
        int maxY = Math.min(height - 1, Math.max(y1, y2));
        int maxZ = Math.min(length - 1, Math.max(z1, z2));

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    setBlock(x, y, z, material);
                }
            }
        }
    }

    @Override
    public Collection<Object[]> getBlocks() {
        Collection<Object[]> result = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    if (blocks[x][y][z] != null && blocks[x][y][z] != Material.AIR) {
                        result.add(new Object[]{x, y, z, blocks[x][y][z], blockData[x][y][z]});
                    }
                }
            }
        }
        return result;
    }

    Material[][][] getRawBlocks() {
        return blocks;
    }

    byte[][][] getRawBlockData() {
        return blockData;
    }

    private void checkBounds(int x, int y, int z) {
        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= length) {
            throw new IndexOutOfBoundsException(
                "Position (" + x + "," + y + "," + z + ") out of bounds for structure " +
                name + " [" + width + "x" + height + "x" + length + "]");
        }
    }
}
