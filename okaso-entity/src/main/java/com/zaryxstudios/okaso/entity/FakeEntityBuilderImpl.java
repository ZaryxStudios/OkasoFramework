package com.zaryxstudios.okaso.entity;

import com.zaryxstudios.okaso.common.entity.FakeEntityBuilder;

import java.lang.reflect.Method;

import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

final class FakeEntityBuilderImpl extends AbstractNPCBuilder<FakeEntityBuilder> implements FakeEntityBuilder {

    private final Entity entity;
    private EntityType entityType;
    private ItemStack heldItem;
    private boolean baby;
    private int variant;
    private String professional;
    private String catType;
    private String foxType;
    private String llamaColor;
    private String parrotVariant;
    private String rabbitType;
    private String sheepColor;
    private String shulkerColor;
    private String tropicalFishPattern;
    private String tropicalFishBodyColor;
    private String tropicalFishPatternColor;
    private String frogVariant;
    private String axolotlVariant;
    private Boolean camelSaddle;
    private Boolean snifferSniffing;
    private String pandaMainGene;
    private String pandaHiddenGene;

    FakeEntityBuilderImpl(Entity entity) {
        this.entity = entity;
    }

    @Override
    public FakeEntityBuilder entityType(EntityType type) {
        this.entityType = type;
        return this;
    }

    @Override
    public FakeEntityBuilder heldItem(ItemStack item) {
        this.heldItem = item;
        return this;
    }

    @Override
    public FakeEntityBuilder baby(boolean baby) {
        this.baby = baby;
        return this;
    }

    @Override
    public FakeEntityBuilder variant(int variant) {
        this.variant = variant;
        return this;
    }

    @Override
    public FakeEntityBuilder professional(String profession) {
        this.professional = profession;
        return this;
    }

    @Override
    public FakeEntityBuilder catType(String catType) {
        this.catType = catType;
        return this;
    }

    @Override
    public FakeEntityBuilder foxType(String foxType) {
        this.foxType = foxType;
        return this;
    }

    @Override
    public FakeEntityBuilder llamaColor(String color) {
        this.llamaColor = color;
        return this;
    }

    @Override
    public FakeEntityBuilder parrotVariant(String variant) {
        this.parrotVariant = variant;
        return this;
    }

    @Override
    public FakeEntityBuilder rabbitType(String type) {
        this.rabbitType = type;
        return this;
    }

    @Override
    public FakeEntityBuilder sheepColor(DyeColor color) {
        this.sheepColor = color == null ? null : color.name();
        return this;
    }

    @Override
    public FakeEntityBuilder shulkerColor(DyeColor color) {
        this.shulkerColor = color == null ? null : color.name();
        return this;
    }

    @Override
    public FakeEntityBuilder tropicalFishPattern(String pattern) {
        this.tropicalFishPattern = pattern;
        return this;
    }

    @Override
    public FakeEntityBuilder tropicalFishBodyColor(DyeColor body) {
        this.tropicalFishBodyColor = body == null ? null : body.name();
        return this;
    }

    @Override
    public FakeEntityBuilder tropicalFishPatternColor(DyeColor pattern) {
        this.tropicalFishPatternColor = pattern == null ? null : pattern.name();
        return this;
    }

    @Override
    public FakeEntityBuilder frogVariant(String variant) {
        this.frogVariant = variant;
        return this;
    }

    @Override
    public FakeEntityBuilder axolotlVariant(String variant) {
        this.axolotlVariant = variant;
        return this;
    }

    @Override
    public FakeEntityBuilder camel(boolean saddle) {
        this.camelSaddle = saddle;
        return this;
    }

    @Override
    public FakeEntityBuilder sniffer(boolean sniffing) {
        this.snifferSniffing = sniffing;
        return this;
    }

    @Override
    public FakeEntityBuilder pandaGene(String mainGene, String hiddenGene) {
        this.pandaMainGene = mainGene;
        this.pandaHiddenGene = hiddenGene;
        return this;
    }

    EntityType getRequestedEntityType() {
        return entityType;
    }

    void apply() {
        applyCommon(entity);
        if (heldItem != null && entity instanceof LivingEntity) {
            ((LivingEntity) entity).getEquipment().setItemInMainHand(heldItem);
        }
        safeInvoke(entity, "setBaby", baby);
        if (variant != 0) {
            safeInvoke(entity, "setVariant", variant);
        }
        if (professional != null && VersionUtil.hasVillagerProfessions()) {
            safeInvokeEnum(entity, "setProfession", "org.bukkit.entity.Villager$Profession", professional);
        }
        if (catType != null) {
            safeInvokeEnum(entity, "setCatType", "org.bukkit.entity.Cat$Type", catType);
        }
        if (foxType != null) {
            safeInvokeEnum(entity, "setFoxType", "org.bukkit.entity.Fox$Type", foxType);
        }
        if (llamaColor != null) {
            safeInvokeEnum(entity, "setColor", "org.bukkit.entity.Llama$Color", llamaColor);
        }
        if (parrotVariant != null) {
            safeInvokeEnum(entity, "setVariant", "org.bukkit.entity.Parrot$Variant", parrotVariant);
        }
        if (rabbitType != null) {
            safeInvokeEnum(entity, "setRabbitType", "org.bukkit.entity.Rabbit$Type", rabbitType);
        }
        if (sheepColor != null) {
            safeInvokeEnum(entity, "setColor", "org.bukkit.DyeColor", sheepColor);
        }
        if (shulkerColor != null) {
            safeInvokeEnum(entity, "setColor", "org.bukkit.DyeColor", shulkerColor);
        }
        if (tropicalFishBodyColor != null) {
            safeInvokeEnum(entity, "setBodyColor", "org.bukkit.DyeColor", tropicalFishBodyColor);
        }
        if (tropicalFishPattern != null) {
            safeInvokeEnum(entity, "setPattern", "org.bukkit.entity.TropicalFish$Pattern", tropicalFishPattern);
        }
        if (tropicalFishPatternColor != null) {
            safeInvokeEnum(entity, "setPatternColor", "org.bukkit.DyeColor", tropicalFishPatternColor);
        }
        if (frogVariant != null && VersionUtil.hasWildUpdate()) {
            safeInvokeEnum(entity, "setVariant", "org.bukkit.entity.Frog$Variant", frogVariant);
        }
        if (axolotlVariant != null && VersionUtil.hasCavesAndCliffs()) {
            safeInvokeEnum(entity, "setVariant", "org.bukkit.entity.Axolotl$Variant", axolotlVariant);
        }
        if (camelSaddle != null && VersionUtil.hasArmoredMobs()) {
            safeInvoke(entity, "setSaddled", camelSaddle);
        }
        if (snifferSniffing != null && VersionUtil.hasWildUpdate()) {
            safeInvoke(entity, "setSniffing", snifferSniffing);
        }
        if (pandaMainGene != null) {
            safeInvokeEnum(entity, "setMainGene", "org.bukkit.entity.Panda$Gene", pandaMainGene);
            if (pandaHiddenGene != null) {
                safeInvokeEnum(entity, "setHiddenGene", "org.bukkit.entity.Panda$Gene", pandaHiddenGene);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void safeInvokeEnum(Object target, String methodName, String enumClassName, String enumValue) {
        try {
            Class<?> enumClass = Class.forName(enumClassName);
            Object enumConstant = Enum.valueOf((Class<? extends Enum>) enumClass, enumValue);
            Method m = target.getClass().getMethod(methodName, enumClass);
            m.setAccessible(true);
            m.invoke(target, enumConstant);
        } catch (Exception ignored) {
        }
    }
}