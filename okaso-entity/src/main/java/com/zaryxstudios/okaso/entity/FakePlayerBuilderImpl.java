package com.zaryxstudios.okaso.entity;

import com.zaryxstudios.okaso.common.entity.FakePlayerBuilder;
import com.zaryxstudios.okaso.common.entity.SkinData;
import com.zaryxstudios.okaso.common.entity.SkinData;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

final class FakePlayerBuilderImpl extends AbstractNPCBuilder<FakePlayerBuilder> implements FakePlayerBuilder {

    private final Entity entity;

    private String skinUrl;
    private byte[] skinData;
    private byte[] capeData;
    private SkinData skin;
    private String skinOwner;
    private boolean slimModel;
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
        if (textureUrl != null && (skin == null || !skin.hasTexture())) {
            this.skin = SkinData.fromUrls(textureUrl, null);
        }
        return this;
    }

    @Override
    public FakePlayerBuilder skin(byte[] skinData, byte[] capeData) {
        this.skinData = skinData;
        this.capeData = capeData;
        return this;
    }

    @Override
    public FakePlayerBuilder skin(SkinData skin) {
        this.skin = skin;
        if (skin != null && skin.getSkinUrl() != null) {
            this.skinUrl = skin.getSkinUrl();
        }
        if (skin != null) {
            this.slimModel = skin.isSlimModel();
        }
        return this;
    }

    @Override
    public FakePlayerBuilder skin(String textureValue, String signature) {
        this.skin = signature == null ? SkinData.unsigned(textureValue) : SkinData.signed(textureValue, signature);
        return this;
    }

    @Override
    public FakePlayerBuilder skinOwner(String playerName) {
        this.skinOwner = playerName;
        if (playerName != null) {
            SkinData cached = SkinResolver.getCached(playerName);
            if (cached != null) {
                this.skin = cached;
            }
        }
        return this;
    }

    @Override
    public FakePlayerBuilder skinUrls(String skinUrl, String capeUrl) {
        if (skinUrl == null) {
            return this;
        }
        this.skin = SkinData.fromUrls(skinUrl, capeUrl, slimModel, skinOwner);
        this.skinUrl = skinUrl;
        return this;
    }

    @Override
    public FakePlayerBuilder slimModel(boolean slim) {
        this.slimModel = slim;
        if (skin != null && skin.hasTexture() && skin.isSlimModel() != slim && skin.getSkinUrl() != null) {
            this.skin = SkinData.fromUrls(skin.getSkinUrl(), skin.getCapeUrl(), slim, skin.getOwnerName());
        }
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
        resolvePendingSkin();
        shapePlaceholder(entity);
        applyCommon(entity);
        if (entity instanceof LivingEntity) {
            LivingEntity le = (LivingEntity) entity;
            if (mainHand != null) le.getEquipment().setItemInMainHand(mainHand);
            if (offHand != null) le.getEquipment().setItemInOffHand(offHand);
            applySkullPreview(le);
        }
    }

    private void resolvePendingSkin() {
        if (skin != null && skin.hasTexture()) {
            return;
        }
        if (skinOwner == null) {
            return;
        }
        SkinData cached = SkinResolver.getCached(skinOwner);
        if (cached != null) {
            skin = cached;
            if (cached.getSkinUrl() != null) {
                skinUrl = cached.getSkinUrl();
            }
            slimModel = cached.isSlimModel();
        }
    }

    private void shapePlaceholder(Entity target) {
        if (!"ARMOR_STAND".equals(target.getType().name())) {
            return;
        }
        safeInvoke(target, "setVisible", false);
        safeInvoke(target, "setMarker", true);
        safeInvoke(target, "setSmall", false);
        safeInvoke(target, "setArms", true);
        safeInvoke(target, "setBasePlate", false);
        safeInvoke(target, "setCanPickupItems", false);
        safeInvoke(target, "setRemoveWhenFarAway", false);
    }

    private void applySkullPreview(LivingEntity le) {
        if (helmet != null || skin == null || !skin.hasTexture()) {
            return;
        }
        if (!"ARMOR_STAND".equals(le.getType().name())) {
            return;
        }
        ItemStack head = SkullSkin.playerHead(skin);
        if (head != null) {
            le.getEquipment().setHelmet(head);
        }
    }

    SkinData getSkin() {
        resolvePendingSkin();
        return skin;
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

    String getSkinOwner() {
        return skinOwner;
    }

    boolean isSlimModel() {
        return slimModel;
    }
}
