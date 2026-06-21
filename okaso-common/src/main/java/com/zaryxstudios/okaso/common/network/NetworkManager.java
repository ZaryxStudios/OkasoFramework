package com.zaryxstudios.okaso.common.network;

import java.util.Set;

public interface NetworkManager {
    void sendPluginMessage(Object player, String channel, byte[] data);
    void registerIncomingChannel(String channel);
    void registerOutgoingChannel(String channel);
    boolean isChannelRegistered(String channel);
    java.util.Set<String> getRegisteredChannels();
}
