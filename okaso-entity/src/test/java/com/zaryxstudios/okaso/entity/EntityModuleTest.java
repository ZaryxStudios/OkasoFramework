package com.zaryxstudios.okaso.entity;

import com.zaryxstudios.okaso.common.entity.*;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Modifier;
import java.util.*;

public class EntityModuleTest {

    static class TestTarget {
        boolean gravitySet = false;
        boolean invulnerableSet = false;
        float lastYaw = 0f;
        int lastFireTicks = 0;
        String lastCustomName = null;
        public void setGravity(boolean g) {
            gravitySet = g;
        }

        public void setInvulnerable(boolean i) {
            invulnerableSet = i;
        }

        public void setFireTicks(int t) {
            lastFireTicks = t;
        }

        public void setRotation(float yaw, float pitch) {
            lastYaw = yaw;
        }

        public void setCustomName(String name) {
            lastCustomName = name;
        }
    }

    @Test
    public void testSafeInvokeBoolean() {
        TestTarget t = new TestTarget();
        AbstractNPCBuilder.safeInvoke(t, "setGravity", true);
        assertTrue(t.gravitySet);
    }

    @Test
    public void testSafeInvokeInt() {
        TestTarget t = new TestTarget();
        AbstractNPCBuilder.safeInvoke(t, "setFireTicks", 200);
        assertEquals(200, t.lastFireTicks);
    }

    @Test
    public void testSafeInvokeString() {
        TestTarget t = new TestTarget();
        AbstractNPCBuilder.safeInvoke(t, "setCustomName", "Hello");
        assertEquals("Hello", t.lastCustomName);
    }

    @Test
    public void testSafeInvokeNullTarget() {
        AbstractNPCBuilder.safeInvoke(null, "setGravity", true);
    }

    @Test
    public void testSafeInvokeNonexistent() {
        TestTarget t = new TestTarget();
        AbstractNPCBuilder.safeInvoke(t, "nonexistent", "arg");
    }

    @Test
    public void testBukkitNPCManager() {
        BukkitNPCManager mgr = new BukkitNPCManager();
        assertEquals(0, mgr.getCount());
        assertFalse(mgr.getAllNPCs().iterator().hasNext());
    }

    @Test
    public void testNPCInteractionTypeEnum() {
        assertEquals(4, NPCInteractionType.values().length);
        assertEquals(NPCInteractionType.RIGHT_CLICK, NPCInteractionType.valueOf("RIGHT_CLICK"));
    }

    @Test
    public void testNPCTypeEnum() {
        assertEquals(2, NPCType.values().length);
        assertEquals(NPCType.FAKE_PLAYER, NPCType.valueOf("FAKE_PLAYER"));
    }

    @Test
    public void testNPCDataDefaults() {
        NPCData data = new NPCData("test", NPCType.FAKE_PLAYER, null);
        assertEquals("test", data.getId());
        assertEquals(NPCType.FAKE_PLAYER, data.getNpcType());
        assertTrue(data.isInvulnerable());
        assertFalse(data.hasGravity());
        assertTrue(data.isSilent());
    }

    @Test
    public void testNPCDataSetters() {
        NPCData data = new NPCData();
        data.setId("id1");
        data.setDisplayName("Guard");
        data.setInvulnerable(false);
        data.setPatrolLoop(true);
        assertEquals("id1", data.getId());
        assertEquals("Guard", data.getDisplayName());
        assertFalse(data.isInvulnerable());
        assertTrue(data.isPatrolLoop());
    }

    @Test
    public void testAbstractNPCBuilderIsAbstract() {
        assertTrue(Modifier.isAbstract(AbstractNPCBuilder.class.getModifiers()));
    }

    @Test
    public void testPacketNPCHandleImplementsInterface() {
        assertTrue(NPCHandle.class.isAssignableFrom(PacketNPCHandle.class));
    }

    @Test
    public void testOkasoBukkitEntityServiceImplementsInterface() {
        assertTrue(EntityService.class.isAssignableFrom(OkasoBukkitEntityService.class));
    }

    @Test
    public void testVersionUtilExists() {
        assertNotNull(VersionUtil.class);
    }
}