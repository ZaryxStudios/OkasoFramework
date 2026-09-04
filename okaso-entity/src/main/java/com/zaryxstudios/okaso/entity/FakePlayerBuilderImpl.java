package com.zaryxstudios.okaso.entity;

import com.zaryxstudios.okaso.common.entity.FakePlayerBuilder;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

final class FakePlayerBuilderImpl extends AbstractNPCBuilder<FakePlayerBuilder> implements FakePlayerBuilder {

    private final Entity entity;

    private String skinUrl;
    private byte[] skinData;
    private byte[] capeData;
    private ItemStack mainHand;
    private ItemStack offHand;
    private boolean sneaking;
    private boolean sprinting;
    private boolean swimming;
    private boolean gliding;

    FakePlayerBuilderImpl(Entity entity) {
        this.entity = entity;
    }

    @Override
    protected boolean sneaking() {
        return sneaking;
    }

    @Override
    protected boolean sprinting() {
        return sprinting;
    }

    @Override
    protected boolean swimming() {
        return swimming;
    }

    @Override
    protected boolean gliding() {
        return gliding;
    }

    @Override
    public FakePlayerBuilder skin(String textureUrl) {
        this.skinUrl = textureUrl;
        return this;
    }

    @Override
    public FakePlayerBuilder skin(byte[] skinData, byte[] capeData) {
        this.skinData = skinData;
        this.capeData = capeData;
        return this;
    }

    @Override
    public FakePlayerBuilder heldItemMainHand(ItemStack item) {
        this.mainHand = item;
        return this;
    }

    @Override
    public FakePlayerBuilder heldItemOffHand(ItemStack item) {
        this.offHand = item;
        return this;
    }

    @Override
    public FakePlayerBuilder displayName(String name) {
        this.customName = name;
        return this;
    }

    @Override
    public FakePlayerBuilder sneaking(boolean sneaking) {
        this.sneaking = sneaking;
        return this;
    }

    @Override
    public FakePlayerBuilder sprinting(boolean sprinting) {
        this.sprinting = sprinting;
        return this;
    }

    @Override
    public FakePlayerBuilder swimming(boolean swimming) {
        this.swimming = swimming;
        return this;
    }

    @Override
    public FakePlayerBuilder gliding(boolean gliding) {
        this.gliding = gliding;
        return this;
    }

    void apply() {
        applyCommon(entity);
        if (entity instanceof LivingEntity) {
            LivingEntity le = (LivingEntity) entity;
            if (mainHand != null) le.getEquipment().setItemInMainHand(mainHand);
            if (offHand != null) le.getEquipment().setItemInOffHand(offHand);
        }
    }

    String getSkinUrl() {
        return skinUrl;
    }
    
    byte[] getSkinData() {
        return skinData;
    }

    byte[] getCapeData() {
        return capeData;
    }
}