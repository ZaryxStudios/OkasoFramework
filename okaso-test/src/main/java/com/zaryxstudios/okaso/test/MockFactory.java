package com.zaryxstudios.okaso.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        private final String name;
        private final Map<String, String> metadata = new ConcurrentHashMap<String, String>();
        private int health = 20;
        private int food = 20;
        private int level = 0;
        private float exp = 0f;
        private boolean online = true;

        MockPlayer(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public int getHealth() {
            return health;
        }

        public void setHealth(int health) {
            this.health = Math.max(0, Math.min(health, 20));
        }

        public int getFood() {
            return food;
        }

        public void setFood(int food) {
            this.food = Math.max(0, Math.min(food, 20));
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = Math.max(0, level);
        }

        public float getExp() {
            return exp;
        }

        public void setExp(float exp) {
            this.exp = Math.max(0f, Math.min(exp, 1f));
        }

        public boolean isOnline() {
            return online;
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
        private String version = "1.21";
        private int port = 25565;
        private String motd = "A Minecraft Server";
        private int maxPlayers = 20;

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

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getMotd() {
            return motd;
        }

        public void setMotd(String motd) {
            this.motd = motd;
        }

        public int getMaxPlayers() {
            return maxPlayers;
        }

        public void setMaxPlayers(int maxPlayers) {
            this.maxPlayers = maxPlayers;
        }

        @Override
        public String toString() {
            return "MockServer{version='" + version + "', players=" + getPlayerCount()
                + "/" + maxPlayers + "}";
        }
    }
}
