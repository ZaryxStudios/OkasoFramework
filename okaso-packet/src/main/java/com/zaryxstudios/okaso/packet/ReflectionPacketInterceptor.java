package com.zaryxstudios.okaso.packet;

import com.zaryxstudios.okaso.common.packet.PacketHandler;
import com.zaryxstudios.okaso.common.packet.PacketInterceptor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ReflectionPacketInterceptor implements PacketInterceptor {

    private final Map<String, List<HandlerEntry>> incomingHandlers;
    private final Map<String, List<HandlerEntry>> outgoingHandlers;

    public ReflectionPacketInterceptor() {
        this.incomingHandlers = new ConcurrentHashMap<>();
        this.outgoingHandlers = new ConcurrentHashMap<>();
    }

    @Override
    public void registerHandler(String packetName, PacketHandler handler) {
        registerIncomingHandler(packetName, PacketPriority.NORMAL, handler);
    }

    public void registerIncomingHandler(String packetName, PacketPriority priority, PacketHandler handler) {
        if (packetName == null || handler == null) return;
        incomingHandlers.computeIfAbsent(packetName, k -> new ArrayList<>())
            .add(new HandlerEntry(priority, handler));
        sortHandlers(incomingHandlers.get(packetName));
    }

    public void registerOutgoingHandler(String packetName, PacketPriority priority, PacketHandler handler) {
        if (packetName == null || handler == null) return;
        outgoingHandlers.computeIfAbsent(packetName, k -> new ArrayList<>())
            .add(new HandlerEntry(priority, handler));
        sortHandlers(outgoingHandlers.get(packetName));
    }

    @Override
    public void unregisterHandler(String packetName) {
        if (packetName == null) return;
        incomingHandlers.remove(packetName);
        outgoingHandlers.remove(packetName);
    }

    public boolean unregisterHandler(String packetName, PacketHandler handler) {
        if (packetName == null || handler == null) return false;
        boolean removed = false;
        List<HandlerEntry> incoming = incomingHandlers.get(packetName);
        if (incoming != null) {
            removed = incoming.removeIf(entry -> entry.handler == handler) || removed;
        }
        List<HandlerEntry> outgoing = outgoingHandlers.get(packetName);
        if (outgoing != null) {
            removed = outgoing.removeIf(entry -> entry.handler == handler) || removed;
        }
        return removed;
    }

    @Override
    public boolean isIntercepted(String packetName) {
        return incomingHandlers.containsKey(packetName) || outgoingHandlers.containsKey(packetName);
    }

    public boolean hasIncomingHandlers(String packetName) {
        return incomingHandlers.containsKey(packetName);
    }

    public boolean hasOutgoingHandlers(String packetName) {
        return outgoingHandlers.containsKey(packetName);
    }

    public PacketHandler getIncomingHandler(String packetName) {
        List<HandlerEntry> list = incomingHandlers.get(packetName);
        return list != null && !list.isEmpty() ? list.get(0).handler : null;
    }

    public PacketHandler getOutgoingHandler(String packetName) {
        List<HandlerEntry> list = outgoingHandlers.get(packetName);
        return list != null && !list.isEmpty() ? list.get(0).handler : null;
    }

    public List<PacketHandler> getIncomingHandlers(String packetName) {
        List<HandlerEntry> list = incomingHandlers.get(packetName);
        if (list == null) return Collections.emptyList();
        return Collections.unmodifiableList(
            list.stream().map(e -> e.handler).collect(Collectors.toList()));
    }

    public List<PacketHandler> getOutgoingHandlers(String packetName) {
        List<HandlerEntry> list = outgoingHandlers.get(packetName);
        if (list == null) return Collections.emptyList();
        return Collections.unmodifiableList(
            list.stream().map(e -> e.handler).collect(Collectors.toList()));
    }

    public Object processIncoming(Object player, Object packet, String packetName) {
        return processHandlers(player, packet, packetName, incomingHandlers);
    }

    public Object processOutgoing(Object player, Object packet, String packetName) {
        return processHandlers(player, packet, packetName, outgoingHandlers);
    }

    public void clear() {
        incomingHandlers.clear();
        outgoingHandlers.clear();
    }

    public int getIncomingPacketCount() {
        return incomingHandlers.size();
    }

    public int getOutgoingPacketCount() {
        return outgoingHandlers.size();
    }

    public int getTotalHandlerCount() {
        return incomingHandlers.values().stream().mapToInt(List::size).sum()
            + outgoingHandlers.values().stream().mapToInt(List::size).sum();
    }

    public int removeHandlers(Predicate<PacketHandler> predicate) {
        int count = 0;
        for (List<HandlerEntry> list : incomingHandlers.values()) {
            count += list.removeIf(entry -> predicate.test(entry.handler)) ? 1 : 0;
        }
        for (List<HandlerEntry> list : outgoingHandlers.values()) {
            count += list.removeIf(entry -> predicate.test(entry.handler)) ? 1 : 0;
        }
        return count;
    }

    private Object processHandlers(Object player, Object packet, String packetName,
                                   Map<String, List<HandlerEntry>> handlersMap) {
        List<HandlerEntry> handlers = handlersMap.get(packetName);
        if (handlers == null || handlers.isEmpty() || packet == null) {
            return packet;
        }

        Object currentPacket = packet;
        for (HandlerEntry entry : handlers) {
            try {
                Object result = entry.handler.handle(player, currentPacket);
                if (result == null) {
                    return null;
                }
                currentPacket = result;
            } catch (Exception e) {
            }
        }
        return currentPacket;
    }

    private void sortHandlers(List<HandlerEntry> list) {
        list.sort(Comparator.comparingInt(e -> e.priority.ordinal()));
    }

    public enum PacketPriority {
        LOWEST(0),
        LOW(1),
        NORMAL(2),
        HIGH(3),
        HIGHEST(4),
        MONITOR(5);

        private final int value;

        PacketPriority(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private static final class HandlerEntry {
        final PacketPriority priority;
        final PacketHandler handler;

        HandlerEntry(PacketPriority priority, PacketHandler handler) {
            this.priority = priority;
            this.handler = handler;
        }
    }

    public static PacketHandler createFieldModifier(String fieldName, Object newValue) {
        return (player, packet) -> {
            try {
                Method setter = packet.getClass().getMethod("set" + capitalize(fieldName), newValue.getClass());
                setter.invoke(packet, newValue);
            } catch (Exception ignored) {
            }
            return packet;
        };
    }

    public static PacketHandler createFieldReader(String fieldName, Consumer<Object> consumer) {
        return (player, packet) -> {
            try {
                Method getter = packet.getClass().getMethod("get" + capitalize(fieldName));
                Object value = getter.invoke(packet);
                consumer.accept(value);
            } catch (Exception ignored) {
            }
            return packet;
        };
    }

    public static PacketHandler createCanceller() {
        return (player, packet) -> null;
    }

    public static PacketHandler createLogger(BiConsumer<Object, Object> logger) {
        return (player, packet) -> {
            logger.accept(player, packet);
            return packet;
        };
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static Object createPacket(String packetClassName) {
        try {
            Class<?> packetClass = PacketCompatibilityResolver.getNMSClassGame(packetClassName);
            if (packetClass == null) {
                packetClass = PacketCompatibilityResolver.getNMSClass("network.protocol." + packetClassName);
            }
            if (packetClass != null) {
                return packetClass.getDeclaredConstructor().newInstance();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Object createPacket(String packetClassName, Object... args) {
        try {
            Class<?> packetClass = PacketCompatibilityResolver.getNMSClassGame(packetClassName);
            if (packetClass == null) {
                packetClass = PacketCompatibilityResolver.getNMSClass("network.protocol." + packetClassName);
            }
            if (packetClass != null) {
                Class<?>[] argTypes = new Class<?>[args.length];
                for (int i = 0; i < args.length; i++) {
                    argTypes[i] = args[i].getClass();
                }
                return packetClass.getConstructor(argTypes).newInstance(args);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Object createEntitySpawnPacket(int entityId, UUID uuid, int entityType, double x, double y, double z, float yaw, float pitch) {
        try {
            Class<?> packetClass = PacketCompatibilityResolver.getNMSClassGame("ClientboundAddEntityPacket");
            if (packetClass != null) {
                return packetClass.getConstructor(int.class, java.util.UUID.class, int.class, double.class, double.class, double.class, float.class, float.class)
                    .newInstance(entityId, uuid, entityType, x, y, z, yaw, pitch);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Object createEntityMetadataPacket(int entityId, Object metadata) {
        try {
            Class<?> packetClass = PacketCompatibilityResolver.getNMSClassGame("ClientboundSetEntityDataPacket");
            if (packetClass != null) {
                return packetClass.getConstructor(int.class, metadata.getClass()).newInstance(entityId, metadata);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Object createEntityTeleportPacket(int entityId, double x, double y, double z, float yaw, float pitch, boolean onGround) {
        try {
            Class<?> packetClass = PacketCompatibilityResolver.getNMSClassGame("ClientboundTeleportEntityPacket");
            if (packetClass != null) {
                return packetClass.getConstructor(int.class, double.class, double.class, double.class, float.class, float.class, boolean.class)
                    .newInstance(entityId, x, y, z, yaw, pitch, onGround);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Object createEntityAnimationPacket(int entityId, int animationId) {
        try {
            Class<?> packetClass = PacketCompatibilityResolver.getNMSClassGame("ClientboundAnimatePacket");
            if (packetClass != null) {
                return packetClass.getConstructor(int.class, int.class).newInstance(entityId, animationId);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Object createParticlePacket(String particleName, double x, double y, double z, float offsetX, float offsetY, float offsetZ, float speed, int count) {
        try {
            Object particle = PacketCompatibilityResolver.getParticleType(particleName);
            if (particle == null) return null;
            
            Class<?> packetClass = PacketCompatibilityResolver.getNMSClassGame("ClientboundLevelParticlesPacket");
            if (packetClass != null) {
                return packetClass.getConstructor(
                    particle.getClass(), boolean.class, double.class, double.class, double.class,
                    float.class, float.class, float.class, float.class, int.class
                ).newInstance(particle, false, x, y, z, offsetX, offsetY, offsetZ, speed, count);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Object createEntityRemovePacket(int... entityIds) {
        try {
            Class<?> packetClass = PacketCompatibilityResolver.getNMSClassGame("ClientboundRemoveEntitiesPacket");
            if (packetClass != null) {
                return packetClass.getConstructor(int[].class).newInstance((Object) entityIds);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Object createEntityHeadRotationPacket(int entityId, byte yaw, byte pitch) {
        try {
            Class<?> packetClass = PacketCompatibilityResolver.getNMSClassGame("ClientboundRotateHeadPacket");
            if (packetClass != null) {
                return packetClass.getConstructor(int.class, byte.class, byte.class).newInstance(entityId, yaw, pitch);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Object createEntityVelocityPacket(int entityId, double x, double y, double z) {
        try {
            Class<?> packetClass = PacketCompatibilityResolver.getNMSClassGame("ClientboundSetEntityMotionPacket");
            if (packetClass != null) {
                return packetClass.getConstructor(int.class, double.class, double.class, double.class)
                    .newInstance(entityId, x, y, z);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Object createEntityEquipmentPacket(int entityId, Object equipmentSlot, Object itemStack) {
        try {
            Class<?> packetClass = PacketCompatibilityResolver.getNMSClassGame("ClientboundSetEquipmentPacket");
            if (packetClass != null) {
                return packetClass.getConstructor(int.class, equipmentSlot.getClass(), itemStack.getClass())
                    .newInstance(entityId, equipmentSlot, itemStack);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Object createEntityStatusPacket(int entityId, byte status) {
        try {
            Class<?> packetClass = PacketCompatibilityResolver.getNMSClassGame("ClientboundEntityEventPacket");
            if (packetClass != null) {
                return packetClass.getConstructor(int.class, byte.class).newInstance(entityId, status);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Object createSoundPacket(String soundName, double x, double y, double z, float volume, float pitch) {
        try {
            Object sound = PacketCompatibilityResolver.getSoundEvent(soundName);
            if (sound == null) return null;
            
            Class<?> packetClass = PacketCompatibilityResolver.getNMSClassGame("ClientboundSoundPacket");
            if (packetClass != null) {
                return packetClass.getConstructor(
                    sound.getClass(), int.class, double.class, double.class, double.class, float.class, float.class
                ).newInstance(sound, 0, x, y, z, volume, pitch);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Object createExplosionPacket(double x, double y, double z, float radius, java.util.List<Object> blocks, float playerMotionX, float playerMotionY, float playerMotionZ) {
        try {
            Class<?> packetClass = PacketCompatibilityResolver.getNMSClassGame("ClientboundExplodePacket");
            if (packetClass != null) {
                return packetClass.getConstructor(
                    double.class, double.class, double.class, float.class,
                    java.util.List.class, float.class, float.class, float.class
                ).newInstance(x, y, z, radius, blocks, playerMotionX, playerMotionY, playerMotionZ);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static PacketHandler createFakeMobSpawner(int entityId, UUID uuid, int entityType, double x, double y, double z, float yaw, float pitch) {
        return (player, packet) -> {
            Object spawnPacket = createEntitySpawnPacket(entityId, uuid, entityType, x, y, z, yaw, pitch);
            if (spawnPacket != null) {
                sendPacket(player, spawnPacket);
            }
            return packet;
        };
    }

    public static PacketHandler createFakeAnimation(int entityId, int animationId) {
        return (player, packet) -> {
            Object animPacket = createEntityAnimationPacket(entityId, animationId);
            if (animPacket != null) {
                sendPacket(player, animPacket);
            }
            return packet;
        };
    }

    public static PacketHandler createFakeParticle(String particleName, double x, double y, double z, float offsetX, float offsetY, float offsetZ, float speed, int count) {
        return (player, packet) -> {
            Object particlePacket = createParticlePacket(particleName, x, y, z, offsetX, offsetY, offsetZ, speed, count);
            if (particlePacket != null) {
                sendPacket(player, particlePacket);
            }
            return packet;
        };
    }

    public static PacketHandler createFakeSound(String soundName, double x, double y, double z, float volume, float pitch) {
        return (player, packet) -> {
            Object soundPacket = createSoundPacket(soundName, x, y, z, volume, pitch);
            if (soundPacket != null) {
                sendPacket(player, soundPacket);
            }
            return packet;
        };
    }

    public static void sendPacket(Object player, Object packet) {
        try {
            if (player == null || packet == null) return;
            Class<?> playerClass = player.getClass();
            Method getHandle = playerClass.getMethod("getHandle");
            Object nmsPlayer = getHandle.invoke(player);
            Class<?> nmsPlayerClass = nmsPlayer.getClass();
            Method connectionField = nmsPlayerClass.getMethod("getConnection");
            Object connection = connectionField.invoke(nmsPlayer);
            Class<?> packetClass = PacketCompatibilityResolver.getNMSClass("network.protocol.Packet");
            if (packetClass != null) {
                Method sendPacket = connection.getClass().getMethod("sendPacket", packetClass);
                sendPacket.invoke(connection, packet);
            }
        } catch (Exception ignored) {
        }
    }

    public static void sendPackets(Object player, Object... packets) {
        for (Object packet : packets) {
            sendPacket(player, packet);
        }
    }

    public static Object getNMSPlayer(Object player) {
        try {
            if (player == null) return null;
            Method getHandle = player.getClass().getMethod("getHandle");
            return getHandle.invoke(player);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Object getPlayerConnection(Object player) {
        Object nmsPlayer = getNMSPlayer(player);
        if (nmsPlayer == null) return null;
        try {
            Method getConnection = nmsPlayer.getClass().getMethod("getConnection");
            return getConnection.invoke(nmsPlayer);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static void setField(Object object, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception ignored) {
        }
    }

    public static Object getField(Object object, String fieldName) {
        try {
            java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static void invokeMethod(Object object, String methodName, Object... args) {
        try {
            Class<?>[] argTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                argTypes[i] = args[i].getClass();
            }
            Method method = object.getClass().getMethod(methodName, argTypes);
            method.invoke(object, args);
        } catch (Exception ignored) {
        }
    }

    public static Object invokeMethodReturn(Object object, String methodName, Object... args) {
        try {
            Class<?>[] argTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                argTypes[i] = args[i].getClass();
            }
            Method method = object.getClass().getMethod(methodName, argTypes);
            return method.invoke(object, args);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Class<?> getNMSClass(String className) {
        return PacketCompatibilityResolver.getNMSClass(className);
    }

    public static Class<?> getNMSClassGame(String className) {
        return PacketCompatibilityResolver.getNMSClassGame(className);
    }

    public static Class<?> getNMSClassWorld(String className) {
        return PacketCompatibilityResolver.getNMSClassWorld(className);
    }

    public static Class<?> getNMSClassEntity(String className) {
        return PacketCompatibilityResolver.getNMSClassEntity(className);
    }

    public static Class<?> getNMSClassParticle(String className) {
        return PacketCompatibilityResolver.getNMSClassParticle(className);
    }

    public static Class<?> getNMSClassSound(String className) {
        return PacketCompatibilityResolver.getNMSClassSound(className);
    }
}
