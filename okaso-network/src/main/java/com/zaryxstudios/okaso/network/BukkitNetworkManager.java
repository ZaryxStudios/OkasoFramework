package com.zaryxstudios.okaso.network;

import com.zaryxstudios.okaso.common.network.NetworkManager;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.HashSet;
import java.util.Set;

public class BukkitNetworkManager implements NetworkManager, PluginMessageListener {

    private final Plugin plugin;
    private final Messenger messenger;
    private final Set<String> incomingChannels;
    private final Set<String> outgoingChannels;

    public BukkitNetworkManager(Plugin plugin) {
        this.plugin = plugin;
        this.messenger = plugin.getServer().getMessenger();
        this.incomingChannels = new HashSet<String>();
        this.outgoingChannels = new HashSet<String>();
    }

    @Override
    public void sendPluginMessage(Object player, String channel, byte[] data) {
        if (player instanceof Player) {
            registerOutgoingChannel(channel);
            ((Player) player).sendPluginMessage(plugin, channel, data);
        }
    }

    @Override
    public void registerIncomingChannel(String channel) {
        if (incomingChannels.add(channel)) {
            messenger.registerIncomingPluginChannel(plugin, channel, this);
        }
    }

    @Override
    public void registerOutgoingChannel(String channel) {
        if (outgoingChannels.add(channel)) {
            messenger.registerOutgoingPluginChannel(plugin, channel);
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, org.bukkit.entity.Player player, byte[] message) {
    }

    @Override
    public boolean isChannelRegistered(String channel) {
        return incomingChannels.contains(channel) || outgoingChannels.contains(channel);
    }

    @Override
    public java.util.Set<String> getRegisteredChannels() {
        java.util.Set<String> all = new java.util.LinkedHashSet<String>();
        all.addAll(incomingChannels);
        all.addAll(outgoingChannels);
        return java.util.Collections.unmodifiableSet(all);
    }
}
