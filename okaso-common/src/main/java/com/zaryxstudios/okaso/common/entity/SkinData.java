package com.zaryxstudios.okaso.common.entity;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class SkinData implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String textureValue;
    private final String signature;
    private final String skinUrl;
    private final String capeUrl;
    private final boolean slimModel;
    private final String ownerName;

    private SkinData(String textureValue, String signature, String skinUrl, String capeUrl, boolean slimModel, String ownerName) {
        this.textureValue = textureValue;
        this.signature = signature;
        this.skinUrl = skinUrl;
        this.capeUrl = capeUrl;
        this.slimModel = slimModel;
        this.ownerName = ownerName;
    }

    public static SkinData signed(String textureValue, String signature) {
        if (textureValue == null) {
            throw new IllegalArgumentException("textureValue must not be null");
        }
        return new SkinData(textureValue, signature, null, null, false, null);
    }

    public static SkinData unsigned(String textureValue) {
        if (textureValue == null) {
            throw new IllegalArgumentException("textureValue must not be null");
        }
        return new SkinData(textureValue, null, null, null, false, null);
    }

    public static SkinData fromUrls(String skinUrl, String capeUrl) {
        return fromUrls(skinUrl, capeUrl, false, null);
    }

    public static SkinData fromUrls(String skinUrl, String capeUrl, boolean slimModel, String ownerName) {
        if (skinUrl == null) {
            throw new IllegalArgumentException("skinUrl must not be null");
        }
        StringBuilder json = new StringBuilder(256);
        json.append("{\"timestamp\":0");
        if (ownerName != null) {
            json.append(",\"profileName\":\"").append(escape(ownerName)).append("\"");
        }
        json.append(",\"textures\":{\"SKIN\":{\"url\":\"").append(escape(skinUrl)).append("\"");
        if (slimModel) {
            json.append(",\"metadata\":{\"model\":\"slim\"}");
        }
        json.append("}");
        if (capeUrl != null) {
            json.append(",\"CAPE\":{\"url\":\"").append(escape(capeUrl)).append("\"}");
        }
        json.append("}}");
        String value = Base64.getEncoder().encodeToString(json.toString().getBytes(StandardCharsets.UTF_8));
        return new SkinData(value, null, skinUrl, capeUrl, slimModel, ownerName);
    }

    public static SkinData owner(String playerName) {
        if (playerName == null) {
            throw new IllegalArgumentException("playerName must not be null");
        }
        return new SkinData(null, null, null, null, false, playerName);
    }

    public SkinData withResolvedUrls(String skinUrl, String capeUrl, boolean slimModel) {
        return new SkinData(textureValue, signature, skinUrl, capeUrl, slimModel, ownerName);
    }

    public String getTextureValue() {
        return textureValue;
    }

    public String getSignature() {
        return signature;
    }

    public String getSkinUrl() {
        return skinUrl;
    }

    public String getCapeUrl() {
        return capeUrl;
    }

    public boolean isSlimModel() {
        return slimModel;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public boolean isSigned() {
        return textureValue != null && signature != null;
    }

    public boolean hasTexture() {
        return textureValue != null;
    }

    public boolean isPendingOwnerLookup() {
        return textureValue == null && ownerName != null;
    }

    private static String escape(String input) {
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
