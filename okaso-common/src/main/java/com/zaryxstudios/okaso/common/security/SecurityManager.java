package com.zaryxstudios.okaso.common.security;

public interface SecurityManager {
    String hashPassword(String password);
    boolean verifyPassword(String password, String hash);
    String encrypt(String plainText, String key);
    String decrypt(String cipherText, String key);
    boolean isRateLimited(String key);
    String generateSalt();
    String sha256Hwid(String identifier);
    String hmacSha256(String data, String key);
}
