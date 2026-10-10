package com.zaryxstudios.okaso.particle;

import com.zaryxstudios.okaso.common.particle.OkasoParticleEffect;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.Getter;

public class OkasoBukkitParticleEffect implements OkasoParticleEffect {

    private static final boolean HAS_BUKKIT_API;
    private static final Method SPAWN_PARTICLE;
    private static final Method PLAYER_SPAWN_PARTICLE;
    private static final Method PLAYER_GET_HANDLE;
    private static final Field PLAYER_CONNECTION;
    private static final Method SEND_PACKET;
    private static final Constructor<?> PACKET_CTOR_ENUM;
    private static final Constructor<?> PACKET_CTOR_STRING;
    private static final Object   ENUM_PARTICLE_FLAME;

    static {
        boolean hasApi = false;
        Method spawnParticle = null;
        Method playerSpawnParticle = null;
        Method getHandle = null;
        Field playerConnection = null;
        Method sendPacket = null;
        Constructor<?> ctorEnum = null;
        Constructor<?> ctorString = null;
        Object flameEnum = null;

        try {
            Class<?> particleClass = Class.forName("org.bukkit.Particle");
            Class<?>[] params = {particleClass, Location.class, int.class, double.class, double.class, double.class, double.class};
            spawnParticle = World.class.getMethod("spawnParticle", params);
            playerSpawnParticle = Player.class.getMethod("spawnParticle", params);
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

                try {
                    Class<?> packetClass = Class.forName("net.minecraft.server." + nms + ".Packet");
                    getHandle = Player.class.getMethod("getHandle");
                    playerConnection = Class.forName("net.minecraft.server." + nms + ".EntityPlayer").getField("playerConnection");
                    sendPacket = playerConnection.getType().getMethod("sendPacket", packetClass);
                } catch (Exception ignored2) {
                }
            } catch (Exception ignored) {
            }
        }

        SPAWN_PARTICLE          = spawnParticle;
        PLAYER_SPAWN_PARTICLE   = playerSpawnParticle;
        PLAYER_GET_HANDLE       = getHandle;
        PLAYER_CONNECTION       = playerConnection;
        SEND_PACKET             = sendPacket;
        PACKET_CTOR_ENUM   = ctorEnum;
        PACKET_CTOR_STRING = ctorString;
        ENUM_PARTICLE_FLAME = flameEnum;
    }

    @Getter
    private final String name;
    private final String particleType;
    private final Object particleEnum;
    private Color color;
    private Color transitionColor;
    private float size;
    private Material material;
    private BlockData blockData;
    private ItemStack itemStack;
    private boolean useColor;
    private boolean useTransition;
    private boolean useMaterial;
    private boolean useBlockData;
    private boolean useItemStack;

    public OkasoBukkitParticleEffect(String name, String particleType) {
        this.name = name;
        this.particleType = (particleType != null) ? particleType.toUpperCase() : "FLAME";
        this.particleEnum = HAS_BUKKIT_API ? resolveBukkitParticle() : resolveEnumParticle();
        this.size = 1.0f;
    }

    public OkasoBukkitParticleEffect(String name, String particleType, Color color) {
        this(name, particleType);
        this.color = color;
        this.useColor = color != null;
    }

    public OkasoBukkitParticleEffect(String name, String particleType, Color color, Color transitionColor) {
        this(name, particleType, color);
        this.transitionColor = transitionColor;
        this.useTransition = transitionColor != null;
    }

    public OkasoBukkitParticleEffect(String name, String particleType, Material material) {
        this(name, particleType);
        this.material = material;
        this.useMaterial = material != null;
    }

    public OkasoBukkitParticleEffect(String name, String particleType, BlockData blockData) {
        this(name, particleType);
        this.blockData = blockData;
        this.useBlockData = blockData != null;
    }

    public OkasoBukkitParticleEffect(String name, String particleType, ItemStack itemStack) {
        this(name, particleType);
        this.itemStack = itemStack;
        this.useItemStack = itemStack != null;
    }

    public OkasoBukkitParticleEffect color(Color color) {
        this.color = color;
        this.useColor = color != null;
        return this;
    }

    public OkasoBukkitParticleEffect transitionColor(Color color) {
        this.transitionColor = color;
        this.useTransition = color != null;
        return this;
    }

    public OkasoBukkitParticleEffect size(float size) {
        this.size = size;
        return this;
    }

    public OkasoBukkitParticleEffect material(Material material) {
        this.material = material;
        this.useMaterial = material != null;
        return this;
    }

    public OkasoBukkitParticleEffect blockData(BlockData blockData) {
        this.blockData = blockData;
        this.useBlockData = blockData != null;
        return this;
    }

    public OkasoBukkitParticleEffect itemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.useItemStack = itemStack != null;
        return this;
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
        playInCircle(center, radius, count, speed);
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

    @Override
    public void playHeart(Object center, double size, int count, double speed) {
        if (!(center instanceof Location)) return;
        Location centerLoc = (Location) center;
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = centerLoc.getX() + size * 16 * Math.pow(Math.sin(angle), 3);
            double y = centerLoc.getY() + size * (13 * Math.cos(angle) - 5 * Math.cos(2 * angle) - 2 * Math.cos(3 * angle) - Math.cos(4 * angle));
            double z = centerLoc.getZ();
            Location point = new Location(centerLoc.getWorld(), x, y, z);
            play(point, 1, 0, 0, 0, speed);
        }
    }

    @Override
    public void playStar(Object center, double size, int points, int count, double speed) {
        if (!(center instanceof Location)) return;
        Location centerLoc = (Location) center;
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double outerRadius = size;
            double innerRadius = size * 0.4;
            double x = centerLoc.getX() + (i % 2 == 0 ? outerRadius : innerRadius) * Math.cos(angle);
            double z = centerLoc.getZ() + (i % 2 == 0 ? outerRadius : innerRadius) * Math.sin(angle);
            double y = centerLoc.getY();
            Location point = new Location(centerLoc.getWorld(), x, y, z);
            play(point, 1, 0, 0, 0, speed);
        }
    }

    @Override
    public void playSquare(Object center, double size, int count, double speed) {
        if (!(center instanceof Location)) return;
        Location centerLoc = (Location) center;
        int perSide = count / 4;
        for (int side = 0; side < 4; side++) {
            for (int i = 0; i < perSide; i++) {
                double ratio = (double) i / Math.max(perSide - 1, 1);
                double x, z;
                switch (side) {
                    case 0: x = centerLoc.getX() - size + 2 * size * ratio; z = centerLoc.getZ() - size; break;
                    case 1: x = centerLoc.getX() + size; z = centerLoc.getZ() - size + 2 * size * ratio; break;
                    case 2: x = centerLoc.getX() + size - 2 * size * ratio; z = centerLoc.getZ() + size; break;
                    default: x = centerLoc.getX() - size; z = centerLoc.getZ() + size - 2 * size * ratio; break;
                }
                Location point = new Location(centerLoc.getWorld(), x, centerLoc.getY(), z);
                play(point, 1, 0, 0, 0, speed);
            }
        }
    }

    @Override
    public void playTriangle(Object center, double size, int count, double speed) {
        if (!(center instanceof Location)) return;
        Location centerLoc = (Location) center;
        int perSide = count / 3;
        for (int side = 0; side < 3; side++) {
            for (int i = 0; i < perSide; i++) {
                double ratio = (double) i / Math.max(perSide - 1, 1);
                double angle = side * 2 * Math.PI / 3;
                double x = centerLoc.getX() + size * Math.cos(angle) * (1 - ratio) + size * Math.cos(angle + 2 * Math.PI / 3) * ratio;
                double z = centerLoc.getZ() + size * Math.sin(angle) * (1 - ratio) + size * Math.sin(angle + 2 * Math.PI / 3) * ratio;
                Location point = new Location(centerLoc.getWorld(), x, centerLoc.getY(), z);
                play(point, 1, 0, 0, 0, speed);
            }
        }
    }

    @Override
    public void playCone(Object center, double radius, double height, int count, double speed) {
        if (!(center instanceof Location)) return;
        Location centerLoc = (Location) center;
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * Math.random();
            double r = radius * Math.sqrt(Math.random());
            double y = centerLoc.getY() + height * Math.random();
            double x = centerLoc.getX() + r * Math.cos(angle);
            double z = centerLoc.getZ() + r * Math.sin(angle);
            Location point = new Location(centerLoc.getWorld(), x, y, z);
            play(point, 1, 0, 0, 0, speed);
        }
    }

    @Override
    public void playTornado(Object center, double radius, double height, int turns, int pointsPerTurn, double speed) {
        if (!(center instanceof Location)) return;
        Location centerLoc = (Location) center;
        int totalPoints = turns * pointsPerTurn;
        for (int i = 0; i < totalPoints; i++) {
            double angle = 2 * Math.PI * turns * i / totalPoints;
            double y = centerLoc.getY() + height * i / totalPoints;
            double r = radius * (1.0 - (double) i / totalPoints);
            double x = centerLoc.getX() + r * Math.cos(angle);
            double z = centerLoc.getZ() + r * Math.sin(angle);
            Location point = new Location(centerLoc.getWorld(), x, y, z);
            play(point, 1, 0, 0, 0, speed);
        }
    }

    @Override
    public void playDNA(Object center, double radius, double height, int turns, int pointsPerTurn, double speed) {
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
    public void playCube(Object center, double size, int count, double speed) {
        if (!(center instanceof Location)) return;
        Location centerLoc = (Location) center;
        int perEdge = count / 12;
        double half = size / 2;
        double[][] vertices = {
            {-half, -half, -half}, {half, -half, -half}, {half, half, -half}, {-half, half, -half},
            {-half, -half, half}, {half, -half, half}, {half, half, half}, {-half, half, half}
        };
        int[][] edges = {
            {0,1}, {1,2}, {2,3}, {3,0},
            {4,5}, {5,6}, {6,7}, {7,4},
            {0,4}, {1,5}, {2,6}, {3,7}
        };
        for (int[] edge : edges) {
            for (int i = 0; i < perEdge; i++) {
                double ratio = (double) i / Math.max(perEdge - 1, 1);
                double x = centerLoc.getX() + vertices[edge[0]][0] * (1 - ratio) + vertices[edge[1]][0] * ratio;
                double y = centerLoc.getY() + vertices[edge[0]][1] * (1 - ratio) + vertices[edge[1]][1] * ratio;
                double z = centerLoc.getZ() + vertices[edge[0]][2] * (1 - ratio) + vertices[edge[1]][2] * ratio;
                Location point = new Location(centerLoc.getWorld(), x, y, z);
                play(point, 1, 0, 0, 0, speed);
            }
        }
    }

    @Override
    public void playPyramid(Object center, double baseSize, double height, int count, double speed) {
        if (!(center instanceof Location)) return;
        Location centerLoc = (Location) center;
        int perEdge = count / 8;
        double halfBase = baseSize / 2;
        double[][] baseVertices = {
            {-halfBase, 0, -halfBase}, {halfBase, 0, -halfBase}, {halfBase, 0, halfBase}, {-halfBase, 0, halfBase}
        };
        double[] apex = {0, height, 0};
        int[][] baseEdges = {{0,1}, {1,2}, {2,3}, {3,0}};
        int[][] sideEdges = {{0,4}, {1,4}, {2,4}, {3,4}};
        for (int[] edge : baseEdges) {
            for (int i = 0; i < perEdge; i++) {
                double ratio = (double) i / Math.max(perEdge - 1, 1);
                double x = centerLoc.getX() + baseVertices[edge[0]][0] * (1 - ratio) + baseVertices[edge[1]][0] * ratio;
                double z = centerLoc.getZ() + baseVertices[edge[0]][2] * (1 - ratio) + baseVertices[edge[1]][2] * ratio;
                Location point = new Location(centerLoc.getWorld(), x, centerLoc.getY(), z);
                play(point, 1, 0, 0, 0, speed);
            }
        }
        for (int[] edge : sideEdges) {
            for (int i = 0; i < perEdge; i++) {
                double ratio = (double) i / Math.max(perEdge - 1, 1);
                double x = centerLoc.getX() + baseVertices[edge[0]][0] * (1 - ratio);
                double y = centerLoc.getY() + apex[1] * ratio;
                double z = centerLoc.getZ() + baseVertices[edge[0]][2] * (1 - ratio);
                Location point = new Location(centerLoc.getWorld(), x, y, z);
                play(point, 1, 0, 0, 0, speed);
            }
        }
    }

    private void playBukkit(Location loc, int count, double ox, double oy, double oz, double speed) {
        try {
            if (particleEnum != null && HAS_BUKKIT_API) {
                Object particleData = buildParticleData();
                if (particleData != null) {
                    Class<?>[] params = {particleEnum.getClass(), Location.class, int.class, double.class, double.class, double.class, double.class, Object.class};
                    Method spawnParticleWithData = World.class.getMethod("spawnParticle", params);
                    spawnParticleWithData.invoke(loc.getWorld(), particleEnum, loc, count, ox, oy, oz, speed, particleData);
                } else {
                    SPAWN_PARTICLE.invoke(loc.getWorld(), particleEnum, loc, count, ox, oy, oz, speed);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void playBukkitForPlayer(Player player, Location loc, int count,
                                     double ox, double oy, double oz, double speed) {
        try {
            if (particleEnum != null && HAS_BUKKIT_API) {
                Object particleData = buildParticleData();
                if (particleData != null) {
                    Class<?>[] params = {particleEnum.getClass(), Location.class, int.class, double.class, double.class, double.class, double.class, Object.class};
                    Method spawnParticleWithData = Player.class.getMethod("spawnParticle", params);
                    spawnParticleWithData.invoke(player, particleEnum, loc, count, ox, oy, oz, speed, particleData);
                } else {
                    PLAYER_SPAWN_PARTICLE.invoke(player, particleEnum, loc, count, ox, oy, oz, speed);
                }
            }
        } catch (Exception ignored) {
            playBukkit(loc, count, ox, oy, oz, speed);
        }
    }

    private Object buildParticleData() {
        if (!HAS_BUKKIT_API) return null;
        try {
            Class<?> particleClass = Class.forName("org.bukkit.Particle");
            Enum<?> particle = (Enum<?>) particleEnum;

            String particleName = particle.name();
            if (useColor && "DUST".equals(particleName)) {
                Class<?> dustOptionsClass = Class.forName("org.bukkit.Particle$DustOptions");
                return dustOptionsClass.getConstructor(Color.class, float.class).newInstance(color, size);
            }
            if (useColor && useTransition && "DUST_COLOR_TRANSITION".equals(particleName)) {
                Class<?> dustTransitionClass = Class.forName("org.bukkit.Particle$DustTransition");
                return dustTransitionClass.getConstructor(Color.class, Color.class, float.class).newInstance(color, transitionColor, size);
            }
            if (useMaterial && ("BLOCK_CRACK".equals(particleName) || "BLOCK_DUST".equals(particleName) || "FALLING_DUST".equals(particleName))) {
                return material;
            }
            if (useBlockData && ("BLOCK_CRACK".equals(particleName) || "BLOCK_DUST".equals(particleName) || "FALLING_DUST".equals(particleName))) {
                return blockData;
            }
            if (useItemStack && "ITEM_CRACK".equals(particleName)) {
                return itemStack;
            }
        } catch (Exception ignored) {
        }
        return null;
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
            Object entityPlayer = PLAYER_GET_HANDLE.invoke(player);
            Object connection = PLAYER_CONNECTION.get(entityPlayer);
            SEND_PACKET.invoke(connection, packet);
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
                int[] empty = new int[0];
                return PACKET_CTOR_ENUM.newInstance(particleEnum, true, x, y, z, fOx, fOy, fOz, fSpeed, count, empty);
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
