package com.zaryxstudios.okaso.common.packet;

public interface PacketInterceptor {
    void registerHandler(String packetName, PacketHandler handler);
    void unregisterHandler(String packetName);
    boolean isIntercepted(String packetName);
}
