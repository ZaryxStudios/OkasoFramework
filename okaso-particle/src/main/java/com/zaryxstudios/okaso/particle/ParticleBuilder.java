package com.zaryxstudios.okaso.particle;

import com.zaryxstudios.okaso.common.OkasoAPI;
import com.zaryxstudios.okaso.common.particle.OkasoParticleEffect;
import com.zaryxstudios.okaso.common.particle.ParticleManager;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

public class ParticleBuilder {

    private String effectName;
    private String particleType;
    private int count = 1;
    private double offsetX = 0;
    private double offsetY = 0;
    private double offsetZ = 0;
    private double speed = 0;
    private Object location;
    private Object player;
    private Color color;
    private Color transitionColor;
    private float size = 1.0f;
    private Material material;
    private BlockData blockData;
    private ItemStack itemStack;

    public static ParticleBuilder create(String name, String particleType) {
        ParticleBuilder builder = new ParticleBuilder();
        builder.effectName = name;
        builder.particleType = particleType;
        return builder;
    }

    public ParticleBuilder count(int count) {
        this.count = count;
        return this;
    }

    public ParticleBuilder offsets(double x, double y, double z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        return this;
    }

    public ParticleBuilder speed(double speed) {
        this.speed = speed;
        return this;
    }

    public ParticleBuilder at(Object location) {
        this.location = location;
        return this;
    }

    public ParticleBuilder forPlayer(Object player) {
        this.player = player;
        return this;
    }

    public ParticleBuilder color(Color color) {
        this.color = color;
        return this;
    }

    public ParticleBuilder transitionColor(Color color) {
        this.transitionColor = color;
        return this;
    }

    public ParticleBuilder size(float size) {
        this.size = size;
        return this;
    }

    public ParticleBuilder material(Material material) {
        this.material = material;
        return this;
    }

    public ParticleBuilder blockData(BlockData blockData) {
        this.blockData = blockData;
        return this;
    }

    public ParticleBuilder itemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        return this;
    }

    public void play() {
        if (effectName == null || location == null) return;
        ParticleManager manager = OkasoAPI.service(ParticleManager.class);
        if (manager == null) return;
        OkasoParticleEffect effect = manager.getOrCreateEffect(effectName, particleType);
        if (effect instanceof OkasoBukkitParticleEffect) {
            OkasoBukkitParticleEffect bukkitEffect = (OkasoBukkitParticleEffect) effect;
            if (color != null) bukkitEffect.color(color);
            if (transitionColor != null) bukkitEffect.transitionColor(transitionColor);
            if (size != 1.0f) bukkitEffect.size(size);
            if (material != null) bukkitEffect.material(material);
            if (blockData != null) bukkitEffect.blockData(blockData);
            if (itemStack != null) bukkitEffect.itemStack(itemStack);
        }
        if (player != null) {
            effect.playForPlayer(player, location, count, offsetX, offsetY, offsetZ, speed);
        } else {
            effect.play(location, count, offsetX, offsetY, offsetZ, speed);
        }
    }

    public void playShape(String shapeName, Object center, double... params) {
        if (effectName == null || center == null) return;
        ParticleManager manager = OkasoAPI.service(ParticleManager.class);
        if (manager == null) return;
        OkasoParticleEffect effect = manager.getOrCreateEffect(effectName, particleType);
        if (effect instanceof OkasoBukkitParticleEffect) {
            OkasoBukkitParticleEffect bukkitEffect = (OkasoBukkitParticleEffect) effect;
            if (color != null) bukkitEffect.color(color);
            if (transitionColor != null) bukkitEffect.transitionColor(transitionColor);
            if (size != 1.0f) bukkitEffect.size(size);
            if (material != null) bukkitEffect.material(material);
            if (blockData != null) bukkitEffect.blockData(blockData);
            if (itemStack != null) bukkitEffect.itemStack(itemStack);
        }
        switch (shapeName.toLowerCase()) {
            case "circle":
                if (params.length >= 2) effect.playInCircle(center, params[0], (int) params[1], params.length > 2 ? params[2] : 0);
                break;
            case "sphere":
                if (params.length >= 2) effect.playInSphere(center, params[0], (int) params[1], params.length > 2 ? params[2] : 0);
                break;
            case "line":
                if (params.length >= 3) effect.playInLine(center, params[0], (int) params[1], params[2]);
                break;
            case "spiral":
                if (params.length >= 4) effect.playSpiral(center, params[0], params[1], (int) params[2], (int) params[3], params.length > 4 ? params[4] : 0);
                break;
            case "helix":
                if (params.length >= 4) effect.playHelix(center, params[0], params[1], (int) params[2], (int) params[3], params.length > 4 ? params[4] : 0);
                break;
            case "heart":
                if (params.length >= 2) effect.playHeart(center, params[0], (int) params[1], params.length > 2 ? params[2] : 0);
                break;
            case "star":
                if (params.length >= 3) effect.playStar(center, params[0], (int) params[1], (int) params[2], params.length > 3 ? params[3] : 0);
                break;
            case "square":
                if (params.length >= 2) effect.playSquare(center, params[0], (int) params[1], params.length > 2 ? params[2] : 0);
                break;
            case "triangle":
                if (params.length >= 2) effect.playTriangle(center, params[0], (int) params[1], params.length > 2 ? params[2] : 0);
                break;
            case "cone":
                if (params.length >= 3) effect.playCone(center, params[0], params[1], (int) params[2], params.length > 3 ? params[3] : 0);
                break;
            case "tornado":
                if (params.length >= 4) effect.playTornado(center, params[0], params[1], (int) params[2], (int) params[3], params.length > 4 ? params[4] : 0);
                break;
            case "dna":
                if (params.length >= 4) effect.playDNA(center, params[0], params[1], (int) params[2], (int) params[3], params.length > 4 ? params[4] : 0);
                break;
            case "cube":
                if (params.length >= 2) effect.playCube(center, params[0], (int) params[1], params.length > 2 ? params[2] : 0);
                break;
            case "pyramid":
                if (params.length >= 3) effect.playPyramid(center, params[0], params[1], (int) params[2], params.length > 3 ? params[3] : 0);
                break;
        }
    }
}
