package com.zaryxstudios.okaso.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import lombok.Setter;

public final class MockFactory {

    private MockFactory() {
    }

    public static MockPlayer createMockPlayer(String name) {
        return new MockPlayer(name);
    }

    public static MockServer createMockServer() {
        return new MockServer();
    }

    public static final class MockPlayer {
        @Getter private final String name;
        private final Map<String, String> metadata = new ConcurrentHashMap<String, String>();
        @Getter private int health = 20;
        @Getter private int food = 20;
        @Getter private int level = 0;
        @Getter private float exp = 0f;
        @Getter private boolean online = true;

        MockPlayer(String name) {
            this.name = name;
        }

        public void setHealth(int health) {
            this.health = Math.max(0, Math.min(health, 20));
        }

        public void setFood(int food) {
            this.food = Math.max(0, Math.min(food, 20));
        }

        public void setLevel(int level) {
            this.level = Math.max(0, level);
        }

        public void setExp(float exp) {
            this.exp = Math.max(0f, Math.min(exp, 1f));
        }

        public void setOnline(boolean online) {
            this.online = online;
        }

        public void setMetadata(String key, String value) {
            metadata.put(key, value);
        }

        public String getMetadata(String key) {
            return metadata.get(key);
        }

        public boolean hasMetadata(String key) {
            return metadata.containsKey(key);
        }

        @Override
        public String toString() {
            return "MockPlayer{name='" + name + "', level=" + level + ", health=" + health + "}";
        }
    }

    public static final class MockServer {
        private final List<MockPlayer> players = new ArrayList<MockPlayer>();
        @Getter @Setter private String version = "1.21";
        @Getter @Setter private int port = 25565;
        @Getter @Setter private String motd = "A Minecraft Server";
        @Getter @Setter private int maxPlayers = 20;

        MockServer() {
        }

        public void addPlayer(MockPlayer player) {
            if (!players.contains(player)) {
                players.add(player);
            }
        }

        public void removePlayer(MockPlayer player) {
            players.remove(player);
        }

        public List<MockPlayer> getOnlinePlayers() {
            List<MockPlayer> online = new ArrayList<MockPlayer>();
            for (MockPlayer p : players) {
                if (p.isOnline()) {
                    online.add(p);
                }
            }
            return online;
        }

        public int getPlayerCount() {
            return getOnlinePlayers().size();
        }

        @Override
        public String toString() {
            return "MockServer{version='" + version + "', players=" + getPlayerCount()
                + "/" + maxPlayers + "}";
        }
    }
}
