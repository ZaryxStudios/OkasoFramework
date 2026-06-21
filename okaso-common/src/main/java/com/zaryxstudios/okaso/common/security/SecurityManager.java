package com.zaryxstudios.okaso.common.security;

public interface SecurityManager {
    String hashPassword(String password);
    boolean verifyPassword(String password, String hash);
    String encrypt(String plainText, String key);
    String decrypt(String cipherText, String key);
    boolean isRateLimited(String key);
    String generateSalt();
}
