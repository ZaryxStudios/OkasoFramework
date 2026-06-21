package com.zaryxstudios.okaso.tablist;

import com.zaryxstudios.okaso.common.tablist.TabListManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BukkitTabListManager implements TabListManager {

    private static final boolean HAS_API_METHOD;

    static {
        boolean hasApi = false;
        try {
            Player.class.getMethod("setPlayerListHeaderFooter", String.class, String.class);
            hasApi = true;
        } catch (NoSuchMethodException ignored) {
        }
        HAS_API_METHOD = hasApi;
    }

    @Override
    public void setHeaderAndFooter(Object player, String header, String footer) {
        if (!(player instanceof Player)) return;
        Player p = (Player) player;
        String h = colorize(header);
        String f = colorize(footer);

        if (HAS_API_METHOD) {
            p.setPlayerListHeaderFooter(h, f);
        } else {
            sendPacket(p, h, f);
        }
    }

    @Override
    public void setHeader(Object player, String header) {
        setHeaderAndFooter(player, header, null);
    }

    @Override
    public void setFooter(Object player, String footer) {
        setHeaderAndFooter(player, null, footer);
    }

    @Override
    public void reset(Object player) {
        setHeaderAndFooter(player, "", "");
    }

    private void sendPacket(Player player, String header, String footer) {
        try {
            Object entityPlayer = getHandle(player);
            Object packet = buildPacket(header, footer);
            if (packet != null) {
                sendPacket(entityPlayer, packet);
            }
        } catch (Exception ignored) {
        }
    }

    private Object getHandle(Player player) throws Exception {
        return player.getClass().getMethod("getHandle").invoke(player);
    }

    private Object buildPacket(String header, String footer) throws Exception {
        String serverPkg = Bukkit.getServer().getClass().getPackage().getName();
        String nmsVersion = serverPkg.substring(serverPkg.lastIndexOf('.') + 1);

        try {
            return buildPacketLegacy(nmsVersion, header, footer);
        } catch (Exception ignored) {
        }

        try {
            return buildPacketModern(header, footer);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Object buildPacketLegacy(String nmsVersion, String header, String footer) throws Exception {
        String base = "net.minecraft.server." + nmsVersion;
        Class<?> chatComponent = Class.forName(base + ".IChatBaseComponent");
        Class<?> serializer  = Class.forName(base + ".IChatBaseComponent$ChatSerializer");
        Method a = serializer.getMethod("a", String.class);

        Object hComp = a.invoke(null, jsonText(header));
        Object fComp = a.invoke(null, jsonText(footer));

        Class<?> packetClass = Class.forName(base + ".PacketPlayOutPlayerListHeaderFooter");
        Constructor<?> ctor = packetClass.getConstructor(chatComponent, chatComponent);
        return ctor.newInstance(hComp, fComp);
    }

    private Object buildPacketModern(String header, String footer) throws Exception {
        Class<?> chatComponent = Class.forName("net.minecraft.network.chat.IChatBaseComponent");
        Class<?> serializer  = Class.forName("net.minecraft.network.chat.IChatBaseComponent$ChatSerializer");
        Method a = serializer.getMethod("a", String.class);

        Object hComp = a.invoke(null, jsonText(header));
        Object fComp = a.invoke(null, jsonText(footer));

        Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutPlayerListHeaderFooter");
        Constructor<?> ctor = packetClass.getConstructor(chatComponent, chatComponent);
        return ctor.newInstance(hComp, fComp);
    }

    private void sendPacket(Object entityPlayer, Object packet) throws Exception {
        try {
            Field connField = entityPlayer.getClass().getField("playerConnection");
            Object connection = connField.get(entityPlayer);
            connection.getClass().getMethod("sendPacket", packet.getClass()).invoke(connection, packet);
        } catch (NoSuchFieldException ignored) {
            for (String name : new String[]{"b", "connection"}) {
                try {
                    Field connField = entityPlayer.getClass().getDeclaredField(name);
                    connField.setAccessible(true);
                    Object connection = connField.get(entityPlayer);
                    Method send = findSendPacketMethod(connection, packet);
                    if (send != null) {
                        send.invoke(connection, packet);
                        return;
                    }
                } catch (NoSuchFieldException ignored2) {
                }
            }
        }
    }

    private static Method findSendPacketMethod(Object connection, Object packet) {
        for (Method m : connection.getClass().getMethods()) {
            if ("sendPacket".equals(m.getName()) && m.getParameterCount() == 1) {
                Class<?> paramType = m.getParameterTypes()[0];
                if (paramType.isAssignableFrom(packet.getClass())) {
                    return m;
                }
            }
        }
        return null;
    }

    private static String jsonText(String text) {
        String safe = text.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"text\":\"" + safe + "\"}";
    }

    private static String colorize(String text) {
        return text != null
            ? ChatColor.translateAlternateColorCodes('&', text)
            : "";
    }

    public void setPlayerName(Object player, int index, String name) {
        if (!(player instanceof Player)) return;
        ((Player) player).setPlayerListName(colorize(name));
    }

    public void setPlayerPing(Object player, int index, int ping) {
    }

    public void removePlayer(Object player, int index) {
    }
}
