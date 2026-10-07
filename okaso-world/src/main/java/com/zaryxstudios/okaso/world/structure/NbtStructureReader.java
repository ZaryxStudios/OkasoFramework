package com.zaryxstudios.okaso.world.structure;

import org.bukkit.Material;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.nio.charset.StandardCharsets;

public class NbtStructureReader {

    private NbtStructureReader() {}

    public static OkasoBukkitStructure readStructure(String name, String filePath) throws IOException {
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(
                new GZIPInputStream(
                new FileInputStream(filePath))))) {

            int rootTagType = in.readUnsignedByte();
            if (rootTagType != 10) {
                throw new IOException("Expected TAG_Compound at root, got tag type " + rootTagType);
            }

            readString(in);

            Map<String, Object> root = readCompound(in);

            return parseStructure(name, root);
        }
    }

    @SuppressWarnings("unchecked")
    private static OkasoBukkitStructure parseStructure(String name, Map<String, Object> root) {
        List<Object> sizeList = (List<Object>) root.get("size");
        if (sizeList == null || sizeList.size() < 3) {
            throw new IllegalArgumentException("Structure file missing 'size' tag");
        }
        int width = ((Number) sizeList.get(0)).intValue();
        int height = ((Number) sizeList.get(1)).intValue();
        int length = ((Number) sizeList.get(2)).intValue();

        if (width <= 0 || height <= 0 || length <= 0) {
            throw new IllegalArgumentException("Structure dimensions must be positive");
        }

        OkasoBukkitStructure structure = new OkasoBukkitStructure(name, width, height, length);

        List<Object> paletteList = (List<Object>) root.get("palette");
        if (paletteList == null) {
            throw new IllegalArgumentException("Structure file missing 'palette' tag");
        }

        List<Map<String, Object>> palette = new ArrayList<>();
        for (Object obj : paletteList) {
            palette.add((Map<String, Object>) obj);
        }

        List<Object> blocksList = (List<Object>) root.get("blocks");
        if (blocksList == null) {
            return structure;
        }

        for (Object blockObj : blocksList) {
            Map<String, Object> blockEntry = (Map<String, Object>) blockObj;

            List<Object> posList = (List<Object>) blockEntry.get("pos");
            if (posList == null || posList.size() < 3) continue;

            int bx = ((Number) posList.get(0)).intValue();
            int by = ((Number) posList.get(1)).intValue();
            int bz = ((Number) posList.get(2)).intValue();

                Object stateValue = blockEntry.get("state");
                if (!(stateValue instanceof Number)) continue;
                int state = ((Number) stateValue).intValue();

                if (bx >= 0 && bx < width && by >= 0 && by < height && bz >= 0 && bz < length
                    && state >= 0 && state < palette.size()) {
                Map<String, Object> paletteEntry = palette.get(state);
                Material material = resolveMaterial(paletteEntry);

                if (material != null && material != Material.AIR) {
                    structure.setBlock(bx, by, bz, material);
                }
            }
        }

        return structure;
    }

    private static Material resolveMaterial(Map<String, Object> paletteEntry) {
        String name = (String) paletteEntry.get("Name");
        if (name == null) return Material.STONE;
        return MaterialResolver.resolveMaterial(name);
    }

    private static Map<String, Object> readCompound(DataInputStream in) throws IOException {
        Map<String, Object> compound = new HashMap<>();
        while (true) {
            int tagType = in.readUnsignedByte();
            if (tagType == 0) break;

            String tagName = readString(in);
            compound.put(tagName, readTagValue(in, tagType));
        }
        return compound;
    }

    private static List<Object> readList(DataInputStream in) throws IOException {
        int elementType = in.readUnsignedByte();
        int elementCount = in.readInt();
        if (elementCount < 0 || elementCount > 1_000_000) {
            throw new IOException("Invalid NBT list length: " + elementCount);
        }

        List<Object> list = new ArrayList<>(elementCount);
        for (int i = 0; i < elementCount; i++) {
            list.add(readTagValue(in, elementType));
        }
        return list;
    }

    private static Object readTagValue(DataInputStream in, int tagType) throws IOException {
        switch (tagType) {
            case 1:  return in.readByte();
            case 2:  return in.readShort();
            case 3:  return in.readInt();
            case 4:  return in.readLong();
            case 5:  return in.readFloat();
            case 6:  return in.readDouble();
            case 7: 
                int byteLen = in.readInt();
                if (byteLen < 0 || byteLen > 64 * 1024 * 1024) throw new IOException("Invalid NBT byte array length: " + byteLen);
                byte[] byteArr = new byte[byteLen];
                in.readFully(byteArr);
                return byteArr;
            case 8:  return readString(in);
            case 9:  return readList(in);
            case 10: return readCompound(in);
            case 11:
                int intLen = in.readInt();
                if (intLen < 0 || intLen > 1_000_000) throw new IOException("Invalid NBT int array length: " + intLen);
                int[] intArr = new int[intLen];
                for (int i = 0; i < intLen; i++) {
                    intArr[i] = in.readInt();
                }
                return intArr;
            case 12:
                int longLen = in.readInt();
                if (longLen < 0 || longLen > 1_000_000) throw new IOException("Invalid NBT long array length: " + longLen);
                long[] longArr = new long[longLen];
                for (int i = 0; i < longLen; i++) {
                    longArr[i] = in.readLong();
                }
                return longArr;
            default:
                throw new IOException("Unknown NBT tag type: " + tagType);
        }
    }

    private static String readString(DataInputStream in) throws IOException {
        int len = in.readUnsignedShort();
        byte[] bytes = new byte[len];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
