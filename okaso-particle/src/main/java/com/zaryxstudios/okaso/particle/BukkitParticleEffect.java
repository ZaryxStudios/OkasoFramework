package com.zaryxstudios.okaso.particle;

import com.zaryxstudios.okaso.common.particle.OkasoParticleEffect;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import lombok.Getter;

public class BukkitParticleEffect implements OkasoParticleEffect {

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

    @Getter
    private final String name;
    private final String particleType;

    public BukkitParticleEffect(String name, String particleType) {
        this.name = name;
        this.particleType = (particleType != null) ? particleType.toUpperCase() : "FLAME";
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

    @Override
    public void playForPlayer(Object player, Object location) {
        playForPlayer(player, location, 1, 0, 0, 0, 0);
    }

    @Override
    public void playForPlayer(Object player, Object location, int count,
                              double offsetX, double offsetY, double offsetZ, double speed) {
        if (!(location instanceof Location) || !(player instanceof Player)) return;
        Location loc = (Location) location;
        Player p = (Player) player;
        if (loc.getWorld() == null) return;

        if (HAS_BUKKIT_API) {
            playBukkitForPlayer(p, loc, count, offsetX, offsetY, offsetZ, speed);
        } else {
            playPacketForPlayer(p, loc, count, offsetX, offsetY, offsetZ, speed);
        }
    }

    @Override
    public void playInCircle(Object center, double radius, int count, double speed) {
        if (!(center instanceof Location)) return;
        Location centerLoc = (Location) center;
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = centerLoc.getX() + radius * Math.cos(angle);
            double z = centerLoc.getZ() + radius * Math.sin(angle);
            Location point = new Location(centerLoc.getWorld(), x, centerLoc.getY(), z);
            play(point, 1, 0, 0, 0, speed);
        }
    }

    @Override
    public void playInLine(Object start, Object end, int count, double speed) {
        if (!(start instanceof Location) || !(end instanceof Location)) return;
        Location s = (Location) start;
        Location e = (Location) end;
        for (int i = 0; i < count; i++) {
            double ratio = (double) i / Math.max(count - 1, 1);
            double x = s.getX() + (e.getX() - s.getX()) * ratio;
            double y = s.getY() + (e.getY() - s.getY()) * ratio;
            double z = s.getZ() + (e.getZ() - s.getZ()) * ratio;
            Location point = new Location(s.getWorld(), x, y, z);
            play(point, 1, 0, 0, 0, speed);
        }
    }

    @Override
    public void playInSphere(Object center, double radius, int count, double speed) {
        if (!(center instanceof Location)) return;
        Location centerLoc = (Location) center;
        for (int i = 0; i < count; i++) {
            double theta = 2 * Math.PI * Math.random();
            double phi = Math.acos(2 * Math.random() - 1);
            double r = radius * Math.cbrt(Math.random());
            double x = centerLoc.getX() + r * Math.sin(phi) * Math.cos(theta);
            double y = centerLoc.getY() + r * Math.sin(phi) * Math.sin(theta);
            double z = centerLoc.getZ() + r * Math.cos(phi);
            Location point = new Location(centerLoc.getWorld(), x, y, z);
            play(point, 1, 0, 0, 0, speed);
        }
    }

    @Override
    public void playSpiral(Object center, double radius, double height, int turns, int pointsPerTurn, double speed) {
        if (!(center instanceof Location)) return;
        Location centerLoc = (Location) center;
        int totalPoints = turns * pointsPerTurn;
        for (int i = 0; i < totalPoints; i++) {
            double angle = 2 * Math.PI * turns * i / totalPoints;
            double y = centerLoc.getY() + height * i / totalPoints;
            double x = centerLoc.getX() + radius * Math.cos(angle);
            double z = centerLoc.getZ() + radius * Math.sin(angle);
            Location point = new Location(centerLoc.getWorld(), x, y, z);
            play(point, 1, 0, 0, 0, speed);
        }
    }

    @Override
    public void playHelix(Object center, double radius, double height, int turns, int pointsPerTurn, double speed) {
        if (!(center instanceof Location)) return;
        Location centerLoc = (Location) center;
        int totalPoints = turns * pointsPerTurn;
        for (int i = 0; i < totalPoints; i++) {
            double angle = 2 * Math.PI * turns * i / totalPoints;
            double y = centerLoc.getY() + height * i / totalPoints;
            double x1 = centerLoc.getX() + radius * Math.cos(angle);
            double z1 = centerLoc.getZ() + radius * Math.sin(angle);
            Location point1 = new Location(centerLoc.getWorld(), x1, y, z1);
            play(point1, 1, 0, 0, 0, speed);
            double x2 = centerLoc.getX() + radius * Math.cos(angle + Math.PI);
            double z2 = centerLoc.getZ() + radius * Math.sin(angle + Math.PI);
            Location point2 = new Location(centerLoc.getWorld(), x2, y, z2);
            play(point2, 1, 0, 0, 0, speed);
        }
    }

    @Override
    public void playRing(Object center, double radius, int count, double speed) {
        if (!(center instanceof Location)) return;
        Location centerLoc = (Location) center;
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = centerLoc.getX() + radius * Math.cos(angle);
            double z = centerLoc.getZ() + radius * Math.sin(angle);
            Location point = new Location(centerLoc.getWorld(), x, centerLoc.getY(), z);
            play(point, 1, 0, 0, 0, speed);
        }
    }

    @Override
    public void playArc(Object center, double radius, double startAngle, double sweepAngle, int count, double speed) {
        if (!(center instanceof Location)) return;
        Location centerLoc = (Location) center;
        for (int i = 0; i < count; i++) {
            double angle = startAngle + sweepAngle * i / Math.max(count - 1, 1);
            double x = centerLoc.getX() + radius * Math.cos(angle);
            double z = centerLoc.getZ() + radius * Math.sin(angle);
            Location point = new Location(centerLoc.getWorld(), x, centerLoc.getY(), z);
            play(point, 1, 0, 0, 0, speed);
        }
    }

    @Override
    public void playRandom(Object center, double radius, int count, double speed) {
        if (!(center instanceof Location)) return;
        Location centerLoc = (Location) center;
        for (int i = 0; i < count; i++) {
            double x = centerLoc.getX() + (Math.random() * 2 - 1) * radius;
            double y = centerLoc.getY() + (Math.random() * 2 - 1) * radius;
            double z = centerLoc.getZ() + (Math.random() * 2 - 1) * radius;
            Location point = new Location(centerLoc.getWorld(), x, y, z);
            play(point, 1, 0, 0, 0, speed);
        }
    }

    @Override
    public void playColumn(Object center, double height, int count, double speed) {
        if (!(center instanceof Location)) return;
        Location centerLoc = (Location) center;
        for (int i = 0; i < count; i++) {
            double y = centerLoc.getY() + height * i / Math.max(count - 1, 1);
            Location point = new Location(centerLoc.getWorld(), centerLoc.getX(), y, centerLoc.getZ());
            play(point, 1, 0, 0, 0, speed);
        }
    }

    @Override
    public void playWave(Object center, double radius, double amplitude, int count, double speed) {
        if (!(center instanceof Location)) return;
        Location centerLoc = (Location) center;
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = centerLoc.getX() + radius * Math.cos(angle);
            double y = centerLoc.getY() + amplitude * Math.sin(angle * 3);
            double z = centerLoc.getZ() + radius * Math.sin(angle);
            Location point = new Location(centerLoc.getWorld(), x, y, z);
            play(point, 1, 0, 0, 0, speed);
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

    private void playBukkitForPlayer(Player player, Location loc, int count,
                                     double ox, double oy, double oz, double speed) {
        try {
            Method spawn = Player.class.getMethod("spawnParticle",
                Class.forName("org.bukkit.Particle"),
                Location.class, int.class, double.class, double.class, double.class, double.class);
            Object particleEnum = resolveBukkitParticle();
            if (particleEnum != null) {
                spawn.invoke(player, particleEnum, loc, count, ox, oy, oz, speed);
            }
        } catch (Exception ignored) {
            playBukkit(loc, count, ox, oy, oz, speed);
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
            sendPacket(player, packet);
        }
    }

    private void playPacketForPlayer(Player player, Location loc, int count,
                                     double ox, double oy, double oz, double speed) {
        Object packet = buildPacket(loc, count, ox, oy, oz, speed);
        if (packet == null) return;
        sendPacket(player, packet);
    }

    private void sendPacket(Player player, Object packet) {
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object connection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
            Class<?> packetSuper = packet.getClass().getSuperclass();
            Method send = connection.getClass().getMethod("sendPacket", packetSuper);
            send.invoke(connection, packet);
        } catch (Exception ignored) {
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
