package com.zaryxstudios.okaso.common.compat;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;

public class CompatibilityNamesTest {

    @Test
    public void coloredMaterialFallsBackToLegacyFamily() {
        assertEquals(Arrays.asList("LIME_WOOL", "WOOL"), CompatibilityNames.materials("LIME_WOOL"));
        assertEquals(Arrays.asList("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"),
            CompatibilityNames.materials("gray stained glass pane"));
        assertEquals(Arrays.asList("RED_TERRACOTTA", "STAINED_CLAY"), CompatibilityNames.materials("RED_TERRACOTTA"));
    }

    @Test
    public void legacyFamilyResolvesToWhiteFirst() {
        assertEquals("WHITE_WOOL", CompatibilityNames.materials("WOOL").get(1));
    }

    @Test
    public void stainedGlassAndPaneStaySeparate() {
        assertEquals(Arrays.asList("BLACK_STAINED_GLASS", "STAINED_GLASS"),
            CompatibilityNames.materials("BLACK_STAINED_GLASS"));
    }

    @Test
    public void renamedItemsHaveLegacyAliases() {
        assertEquals(Arrays.asList("CLOCK", "WATCH"), CompatibilityNames.materials("CLOCK"));
        assertEquals(Arrays.asList("PLAYER_HEAD", "SKULL_ITEM"), CompatibilityNames.materials("PLAYER_HEAD"));
    }

    @Test
    public void legacyDataMatchesDyeColors() {
        assertEquals(0, CompatibilityNames.legacyData("WHITE_WOOL"));
        assertEquals(5, CompatibilityNames.legacyData("LIME_WOOL"));
        assertEquals(7, CompatibilityNames.legacyData("GRAY_STAINED_GLASS_PANE"));
        assertEquals(8, CompatibilityNames.legacyData("LIGHT_GRAY_CONCRETE"));
        assertEquals(3, CompatibilityNames.legacyData("LIGHT_BLUE_CARPET"));
        assertEquals(14, CompatibilityNames.legacyData("RED_WOOL"));
        assertEquals(15, CompatibilityNames.legacyData("BLACK_STAINED_GLASS_PANE"));
        assertEquals(3, CompatibilityNames.legacyData("PLAYER_HEAD"));
    }

    @Test
    public void legacyDataIgnoresNonColoredMaterials() {
        assertEquals(0, CompatibilityNames.legacyData("RED_SANDSTONE"));
        assertEquals(0, CompatibilityNames.legacyData("STONE"));
        assertEquals(0, CompatibilityNames.legacyData(null));
    }
}
