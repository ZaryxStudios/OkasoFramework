package com.zaryxstudios.okaso.packet;

import com.zaryxstudios.okaso.common.packet.PacketHandler;
import com.zaryxstudios.okaso.common.packet.PacketInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;

public class ReflectionPacketInterceptor implements PacketInterceptor {

    private final Map<String, PacketHandler> handlers;

    public ReflectionPacketInterceptor() {
        this.handlers = new ConcurrentHashMap<String, PacketHandler>();
    }

    @Override
    public void registerHandler(String packetName, PacketHandler handler) {
        handlers.put(packetName, handler);
    }

    @Override
    public void unregisterHandler(String packetName) {
        handlers.remove(packetName);
    }

    @Override
    public boolean isIntercepted(String packetName) {
        return handlers.containsKey(packetName);
    }

    public PacketHandler getHandler(String packetName) {
        return handlers.get(packetName);
    }

    public Map<String, PacketHandler> getHandlers() {
        return Collections.unmodifiableMap(handlers);
    }
}
