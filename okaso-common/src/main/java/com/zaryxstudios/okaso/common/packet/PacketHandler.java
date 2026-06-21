package com.zaryxstudios.okaso.common.packet;

@FunctionalInterface
public interface PacketHandler {
    Object handle(Object player, Object packet);
}
