package com.zaryxstudios.okaso.packet;

import com.zaryxstudios.okaso.common.packet.PacketHandler;
import com.zaryxstudios.okaso.common.packet.PacketInterceptor;

import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
}
