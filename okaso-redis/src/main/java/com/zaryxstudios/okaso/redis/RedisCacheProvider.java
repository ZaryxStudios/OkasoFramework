package com.zaryxstudios.okaso.redis;

import com.zaryxstudios.okaso.common.cache.Cache;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class RedisCacheProvider<K, V> implements Cache<K, V> {

    private final JedisPool pool;
    private final String prefix;
    
    public RedisCacheProvider(String host, int port, String password, int database, String prefix) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(8);
        config.setMaxIdle(4);
        config.setMinIdle(1);

        if (password != null && !password.isEmpty()) {
            this.pool = new JedisPool(config, host, port, 2000, password, database);
        } else {
            this.pool = new JedisPool(config, host, port, 2000, null, database);
        }
        this.prefix = (prefix != null) ? prefix : "okaso:";
    }

    public RedisCacheProvider(String host, int port, String prefix) {
        this(host, port, null, 0, prefix);
    }

    @Override
    public void put(K key, V value) {
        try (Jedis jedis = pool.getResource()) {
            jedis.set(redisKey(key), String.valueOf(value));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<V> get(K key) {
        try (Jedis jedis = pool.getResource()) {
            String value = jedis.get(redisKey(key));
            if (value == null) return Optional.empty();
            return Optional.of((V) value);
        }
    }

    @Override
    public boolean contains(K key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.exists(redisKey(key));
        }
    }

    @Override
    public void remove(K key) {
        try (Jedis jedis = pool.getResource()) {
            jedis.del(redisKey(key));
        }
    }

    @Override
    public void clear() {
        try (Jedis jedis = pool.getResource()) {
            Set<String> keys = jedis.keys(prefix + "*");
            if (!keys.isEmpty()) {
                jedis.del(keys.toArray(new String[0]));
            }
        }
    }

    @Override
    public int size() {
        try (Jedis jedis = pool.getResource()) {
            Set<String> keys = jedis.keys(prefix + "*");
            return keys.size();
        }
    }

    @Override
    public Set<K> keys() {
        try (Jedis jedis = pool.getResource()) {
            Set<String> rawKeys = jedis.keys(prefix + "*");
            Set<K> result = new HashSet<K>();
            for (String raw : rawKeys) {
                String stripped = raw.substring(prefix.length());
                @SuppressWarnings("unchecked")
                K key = (K) stripped;
                result.add(key);
            }
            return result;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public V putIfAbsent(K key, V value) {
        try (Jedis jedis = pool.getResource()) {
            String k = redisKey(key);
            if (jedis.setnx(k, String.valueOf(value)) == 1) {
                return null;
            }
            String existing = jedis.get(k);
            return (V) existing;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public V getOrDefault(K key, V defaultValue) {
        try (Jedis jedis = pool.getResource()) {
            String value = jedis.get(redisKey(key));
            return value != null ? (V) value : defaultValue;
        }
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    public void close() {
        pool.close();
    }

    private String redisKey(K key) {
        return prefix + key;
    }
}
