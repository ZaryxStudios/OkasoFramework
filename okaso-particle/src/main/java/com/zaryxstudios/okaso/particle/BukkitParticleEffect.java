package com.zaryxstudios.okaso.particle;

import com.zaryxstudios.okaso.common.particle.ParticleEffect;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class BukkitParticleEffect implements ParticleEffect {

    private static final boolean HAS_BUKKIT_API;
    private static final Constructor<?> PACKET_CTOR_ENUM;
    private static final Constructor<?> PACKET_CTOR_STRING;
    private static final Object   ENUM_PARTICLE_FLAME;

    static {
        boolean hasApi = false;
        Constructor<?> ctorEnum = null;
        Constructor<?> ctorString = null;
        Object flameEnum = null;

        try {
            Class.forName("org.bukkit.Particle");
            World.class.getMethod("spawnParticle",
                Class.forName("org.bukkit.Particle"),
                Location.class, int.class, double.class, double.class, double.class, double.class);
            hasApi = true;
        } catch (Exception ignored) {
        }
        HAS_BUKKIT_API = hasApi;

        if (!HAS_BUKKIT_API) {
            try {
                String pkg = Bukkit.getServer().getClass().getPackage().getName();
                String nms  = pkg.substring(pkg.lastIndexOf('.') + 1);

                try {
                    Class<?> enumParticle = Class.forName("net.minecraft.server." + nms + ".EnumParticle");
                    Class<?> packetClass  = Class.forName("net.minecraft.server." + nms + ".PacketPlayOutWorldParticles");
                    ctorEnum = packetClass.getConstructor(enumParticle, boolean.class,
                        float.class, float.class, float.class,
                        float.class, float.class, float.class,
                        float.class, int.class, int[].class);
                    for (Object c : enumParticle.getEnumConstants()) {
                        Enum<?> e = (Enum<?>) c;
                        if ("FLAME".equals(e.name())) {
                            flameEnum = c;
                            break;
                        }
                    }
                    if (flameEnum == null) flameEnum = enumParticle.getEnumConstants()[0];
                } catch (Exception ignored) {
                }

                if (ctorString == null && ctorEnum == null) {
                    try {
                        Class<?> packetClass = Class.forName("net.minecraft.server." + nms + ".PacketPlayOutWorldParticles");
                        ctorString = packetClass.getConstructor(String.class,
                            float.class, float.class, float.class,
                            float.class, float.class, float.class,
                            float.class, int.class);
                    } catch (Exception ignored2) {
                    }
                }
            } catch (Exception ignored) {
            }
        }

        PACKET_CTOR_ENUM   = ctorEnum;
        PACKET_CTOR_STRING = ctorString;
        ENUM_PARTICLE_FLAME = flameEnum;
    }

    private final String name;
    private final String particleType;

    public BukkitParticleEffect(String name, String particleType) {
        this.name = name;
        this.particleType = (particleType != null) ? particleType.toUpperCase() : "FLAME";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void play(Object location) {
        play(location, 1, 0, 0, 0, 0);
    }

    @Override
    public void play(Object location, int count, double offsetX, double offsetY, double offsetZ, double speed) {
        if (!(location instanceof Location)) return;
        Location loc = (Location) location;
        World world = loc.getWorld();
        if (world == null) return;

        if (HAS_BUKKIT_API) {
            playBukkit(loc, count, offsetX, offsetY, offsetZ, speed);
        } else {
            playPacket(loc, count, offsetX, offsetY, offsetZ, speed);
        }
    }

    private void playBukkit(Location loc, int count, double ox, double oy, double oz, double speed) {
        try {
            Method spawn = World.class.getMethod("spawnParticle",
                Class.forName("org.bukkit.Particle"),
                Location.class, int.class, double.class, double.class, double.class, double.class);
            Object particleEnum = resolveBukkitParticle();
            if (particleEnum != null) {
                spawn.invoke(loc.getWorld(), particleEnum, loc, count, ox, oy, oz, speed);
            }
        } catch (Exception ignored) {
        }
    }

    private Object resolveBukkitParticle() {
        try {
            Class<?> clazz = Class.forName("org.bukkit.Particle");
            for (Object c : clazz.getEnumConstants()) {
                if (((Enum<?>) c).name().equals(particleType)) {
                    return c;
                }
            }
            for (Object c : clazz.getEnumConstants()) {
                if (((Enum<?>) c).name().equals("FLAME")) return c;
            }
            return clazz.getEnumConstants()[0];
        } catch (Exception e) {
            return null;
        }
    }

    private void playPacket(Location loc, int count, double ox, double oy, double oz, double speed) {
        Object packet = buildPacket(loc, count, ox, oy, oz, speed);
        if (packet == null) return;

        for (Player player : loc.getWorld().getPlayers()) {
            try {
                Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
                Object connection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
                Class<?> packetSuper = packet.getClass().getSuperclass();
                Method send = connection.getClass().getMethod("sendPacket", packetSuper);
                send.invoke(connection, packet);
            } catch (Exception ignored) {
            }
        }
    }

    private Object buildPacket(Location loc, int count, double ox, double oy, double oz, double speed) {
        float x = (float) loc.getX();
        float y = (float) loc.getY();
        float z = (float) loc.getZ();
        float fOx = (float) ox;
        float fOy = (float) oy;
        float fOz = (float) oz;
        float fSpeed = (float) speed;

        if (PACKET_CTOR_ENUM != null) {
            try {
                Object enumValue = resolveEnumParticle();
                int[] empty = new int[0];
                return PACKET_CTOR_ENUM.newInstance(enumValue, true, x, y, z, fOx, fOy, fOz, fSpeed, count, empty);
            } catch (Exception ignored) {
            }
        }

        if (PACKET_CTOR_STRING != null) {
            try {
                return PACKET_CTOR_STRING.newInstance(particleType.toLowerCase(), x, y, z, fOx, fOy, fOz, fSpeed, count);
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private Object resolveEnumParticle() {
        if (PACKET_CTOR_ENUM == null) return null;
        Class<?> enumClass = PACKET_CTOR_ENUM.getParameterTypes()[0];
        try {
            for (Object c : enumClass.getEnumConstants()) {
                if (((Enum<?>) c).name().equals(particleType)) {
                    return c;
                }
            }
        } catch (Exception ignored) {
        }
        return ENUM_PARTICLE_FLAME;
    }
}
