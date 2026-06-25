package com.zaryxstudios.okaso.world.structure;

import com.zaryxstudios.okaso.common.world.Structure;
import com.zaryxstudios.okaso.common.world.StructureManager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.Getter;

public class BukkitStructureManager implements StructureManager {

    private static final String FORMAT_HEADER = "OKASO_STRUCTURE_V1";
    private static final String FILE_EXTENSION = ".okstr";

    private final Plugin plugin;
    private final Logger logger;
    @Getter
    private File structureDirectory;

    public BukkitStructureManager(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.structureDirectory = new File(plugin.getDataFolder(), "structures");
        ensureDirectoryExists(structureDirectory);
    }

    @Override
    public boolean saveStructure(String name, Object worldObj, int x1, int y1, int z1, int x2, int y2, int z2) {
        if (!(worldObj instanceof World)) {
            logger.warning("saveStructure: world must be a Bukkit World");
            return false;
        }
        World world = (World) worldObj;

        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);
        int maxZ = Math.max(z1, z2);

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        int length = maxZ - minZ + 1;

        BukkitStructure structure = new BukkitStructure(name, width, height, length);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    Block block = world.getBlockAt(minX + x, minY + y, minZ + z);
                    if (block.getType() != Material.AIR) {
                        byte data = MaterialResolver.getLegacyData(block);
                        structure.setBlock(x, y, z, block.getType(), data);
                    }
                }
            }
        }

        return saveStructureToFile(structure);
    }

    @Override
    public Optional<Structure> loadStructure(String name) {
        BukkitStructure structure = loadStructureFromFile(name);
        return Optional.ofNullable(structure);
    }

    @Override
    public boolean placeStructure(String name, Object location) {
        Optional<Structure> opt = loadStructure(name);
        if (opt.isPresent()) {
            return placeStructure(opt.get(), location);
        }
        logger.warning("Structure not found: " + name);
        return false;
    }

    @Override
    public boolean placeStructure(Structure structure, Object location) {
        if (!(location instanceof Location)) {
            logger.warning("placeStructure: location must be a Bukkit Location");
            return false;
        }
        try {
            structure.place(location, true);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to place structure: " + structure.getName(), e);
            return false;
        }
    }

    @Override
    public Collection<String> getAvailableStructures() {
        List<String> names = new ArrayList<>();
        File[] files = structureDirectory.listFiles((dir, name) -> name.endsWith(FILE_EXTENSION));
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                name = name.substring(0, name.length() - FILE_EXTENSION.length());
                names.add(name);
            }
        }
        Collections.sort(names);
        return names;
    }

    @Override
    public Structure createStructure(String name, int width, int height, int length) {
        return new BukkitStructure(name, width, height, length);
    }

    @Override
    public boolean deleteStructure(String name) {
        File file = getStructureFile(name);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    @Override
    public boolean exportStructure(String name, String filePath) {
        BukkitStructure structure = loadStructureFromFile(name);
        if (structure == null) {
            return false;
        }

        boolean isNbt = filePath.toLowerCase().endsWith(".nbt");
        try {
            if (isNbt) {
                NbtStructureWriter.write(structure, filePath);
            } else {
                NbtStructureWriter.writeSimple(structure, filePath);
            }
            logger.fine("Exported structure: " + name + " -> " + filePath);
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to export structure: " + name, e);
            return false;
        }
    }

    public boolean exportStructureNBT(String name, String filePath) {
        if (!filePath.toLowerCase().endsWith(".nbt")) {
            filePath = filePath + ".nbt";
        }
        return exportStructure(name, filePath);
    }

    @Override
    public boolean importStructure(String name, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            logger.warning("Import file not found: " + filePath);
            return false;
        }

        String lower = filePath.toLowerCase();
        try {
            if (lower.endsWith(".nbt")) {
                BukkitStructure structure = NbtStructureReader.readStructure(name, filePath);
                return saveStructureToFile(structure);
            } else if (lower.endsWith(FILE_EXTENSION)) {
                BukkitStructure structure = parseOkstrFile(file, name);
                if (structure != null) {
                    return saveStructureToFile(structure);
                }
                return false;
            } else {
                try {
                    BukkitStructure structure = NbtStructureReader.readStructure(name, filePath);
                    return saveStructureToFile(structure);
                } catch (Exception nbtErr) {
                    BukkitStructure structure = parseOkstrFile(file, name);
                    if (structure != null) {
                        return saveStructureToFile(structure);
                    }
                    logger.log(Level.SEVERE, "Failed to import structure (tried NBT and .okstr): " + filePath);
                    return false;
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to import structure from: " + filePath, e);
            return false;
        }
    }
    private BukkitStructure parseOkstrFile(File file, String expectedName) {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (!FORMAT_HEADER.equals(header)) {
                return null;
            }

            String nameLine = reader.readLine();
            String widthLine = reader.readLine();
            String heightLine = reader.readLine();
            String lengthLine = reader.readLine();
            String blocksLine = reader.readLine();

            if (nameLine == null || widthLine == null || heightLine == null ||
                lengthLine == null || blocksLine == null) {
                return null;
            }

            String structName = nameLine.substring(nameLine.indexOf('=') + 1);
            int w = Integer.parseInt(widthLine.substring(widthLine.indexOf('=') + 1));
            int h = Integer.parseInt(heightLine.substring(heightLine.indexOf('=') + 1));
            int l = Integer.parseInt(lengthLine.substring(lengthLine.indexOf('=') + 1));
            int expectedBlocks = Integer.parseInt(blocksLine.substring(blocksLine.indexOf('=') + 1));

            BukkitStructure structure = new BukkitStructure(structName, w, h, l);

            String line;
            int blockCount = 0;
            while ((line = reader.readLine()) != null && blockCount < expectedBlocks) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    int bx = Integer.parseInt(parts[0]);
                    int by = Integer.parseInt(parts[1]);
                    int bz = Integer.parseInt(parts[2]);
                    String materialName = parts[3];
                    byte bdata = Byte.parseByte(parts[4]);

                    try {
                        Material mat = Material.valueOf(materialName);
                        structure.setBlock(bx, by, bz, mat, bdata);
                    } catch (IllegalArgumentException ignored) {
                    }
                    blockCount++;
                }
            }

            return structure;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void setStructureDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.structureDirectory = dir;
    }

    private File getStructureFile(String name) {
        String sanitized = name.replaceAll("[^a-zA-Z0-9_-]", "_");
        return new File(structureDirectory, sanitized + FILE_EXTENSION);
    }

    private boolean saveStructureToFile(BukkitStructure structure) {
        File file = getStructureFile(structure.getName());
        ensureDirectoryExists(file.getParentFile());

        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write(FORMAT_HEADER);
            writer.newLine();

            writer.write("name=" + structure.getName());
            writer.newLine();
            writer.write("width=" + structure.getWidth());
            writer.newLine();
            writer.write("height=" + structure.getHeight());
            writer.newLine();
            writer.write("length=" + structure.getLength());
            writer.newLine();

            Material[][][] blocks = structure.getRawBlocks();
            byte[][][] data = structure.getRawBlockData();
            int blockCount = 0;
            for (int x = 0; x < structure.getWidth(); x++) {
                for (int y = 0; y < structure.getHeight(); y++) {
                    for (int z = 0; z < structure.getLength(); z++) {
                        if (blocks[x][y][z] != null && blocks[x][y][z] != Material.AIR) {
                            blockCount++;
                        }
                    }
                }
            }
            writer.write("blocks=" + blockCount);
            writer.newLine();

            for (int x = 0; x < structure.getWidth(); x++) {
                for (int y = 0; y < structure.getHeight(); y++) {
                    for (int z = 0; z < structure.getLength(); z++) {
                        if (blocks[x][y][z] != null && blocks[x][y][z] != Material.AIR) {
                            writer.write(x + "," + y + "," + z + "," +
                                         blocks[x][y][z].name() + "," +
                                         (data[x][y][z] & 0xFF));
                            writer.newLine();
                        }
                    }
                }
            }

            logger.fine("Saved structure: " + structure.getName() + " (" + blockCount + " blocks)");
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save structure: " + structure.getName(), e);
            return false;
        }
    }

    private BukkitStructure loadStructureFromFile(String name) {
        File file = getStructureFile(name);
        if (!file.exists()) {
            return null;
        }

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (!FORMAT_HEADER.equals(header)) {
                logger.warning("Invalid structure file header: " + file.getName());
                return null;
            }

            String nameLine = reader.readLine();
            String widthLine = reader.readLine();
            String heightLine = reader.readLine();
            String lengthLine = reader.readLine();
            String blocksLine = reader.readLine();

            if (nameLine == null || widthLine == null || heightLine == null ||
                lengthLine == null || blocksLine == null) {
                logger.warning("Truncated structure file: " + file.getName());
                return null;
            }

            String structName = nameLine.substring(nameLine.indexOf('=') + 1);
            int w = Integer.parseInt(widthLine.substring(widthLine.indexOf('=') + 1));
            int h = Integer.parseInt(heightLine.substring(heightLine.indexOf('=') + 1));
            int l = Integer.parseInt(lengthLine.substring(lengthLine.indexOf('=') + 1));
            int expectedBlocks = Integer.parseInt(blocksLine.substring(blocksLine.indexOf('=') + 1));

            BukkitStructure structure = new BukkitStructure(structName, w, h, l);

            String line;
            int blockCount = 0;
            while ((line = reader.readLine()) != null && blockCount < expectedBlocks) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    int bx = Integer.parseInt(parts[0]);
                    int by = Integer.parseInt(parts[1]);
                    int bz = Integer.parseInt(parts[2]);
                    String materialName = parts[3];
                    byte bdata = Byte.parseByte(parts[4]);

                    try {
                        Material mat = Material.valueOf(materialName);
                        structure.setBlock(bx, by, bz, mat, bdata);
                    } catch (IllegalArgumentException e) {
                        logger.fine("Unknown material in structure file: " + materialName);
                    }
                    blockCount++;
                }
            }

            logger.fine("Loaded structure: " + structName + " (" + blockCount + " blocks)");
            return structure;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load structure: " + name, e);
            return null;
        }
    }

    private void ensureDirectoryExists(File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}
