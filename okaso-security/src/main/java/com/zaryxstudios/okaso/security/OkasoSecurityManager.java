package com.zaryxstudios.okaso.security;

import com.zaryxstudios.okaso.common.security.SecurityManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class OkasoSecurityManager implements SecurityManager {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String ENCRYPT_ALGORITHM = "AES";

    private final Map<String, RateLimitEntry> rateLimits;
    private final long rateLimitWindowMs;
    private final int rateLimitMaxAttempts;

    public OkasoSecurityManager(long rateLimitWindowMs, int rateLimitMaxAttempts) {
        this.rateLimits = new ConcurrentHashMap<String, RateLimitEntry>();
        this.rateLimitWindowMs = rateLimitWindowMs;
        this.rateLimitMaxAttempts = rateLimitMaxAttempts;
    }

    public OkasoSecurityManager() {
        this(60000L, 10);
    }

    @Override
    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not available: " + HASH_ALGORITHM, e);
        }
    }

    @Override
    public boolean verifyPassword(String password, String hash) {
        return hashPassword(password).equals(hash);
    }

    @Override
    public String encrypt(String plainText, String key) {
        try {
            SecretKeySpec spec = new SecretKeySpec(keyBytes(key), ENCRYPT_ALGORITHM);
            Cipher cipher = Cipher.getInstance(ENCRYPT_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, spec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    @Override
    public String decrypt(String cipherText, String key) {
        try {
            SecretKeySpec spec = new SecretKeySpec(keyBytes(key), ENCRYPT_ALGORITHM);
            Cipher cipher = Cipher.getInstance(ENCRYPT_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, spec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    @Override
    public boolean isRateLimited(String key) {
        long now = System.currentTimeMillis();
        RateLimitEntry entry = rateLimits.get(key);

        if (entry == null) {
            entry = new RateLimitEntry(now, new AtomicLong(1));
            rateLimits.put(key, entry);
            return false;
        }

        synchronized (entry) {
            if (now - entry.timestamp > rateLimitWindowMs) {
                entry.timestamp = now;
                entry.count.set(1);
                return false;
            }

            long attempts = entry.count.incrementAndGet();
            return attempts > rateLimitMaxAttempts;
        }
    }

    private static byte[] keyBytes(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] padded = new byte[16];
        for (int i = 0; i < Math.min(keyBytes.length, 16); i++) {
            padded[i] = keyBytes[i];
        }
        return padded;
    }

    @Override
    public String generateSalt() {
        java.security.SecureRandom random = new java.security.SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        StringBuilder sb = new StringBuilder();
        for (byte b : salt) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static class RateLimitEntry {
        volatile long timestamp;
        final AtomicLong count;

        RateLimitEntry(long timestamp, AtomicLong count) {
            this.timestamp = timestamp;
            this.count = count;
        }
    }
}
