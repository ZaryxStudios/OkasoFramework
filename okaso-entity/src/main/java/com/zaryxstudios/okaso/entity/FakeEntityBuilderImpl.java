package com.zaryxstudios.okaso.entity;

import com.zaryxstudios.okaso.common.entity.FakeEntityBuilder;

import java.lang.reflect.Method;

import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.Villager;

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
    public FakeEntityBuilder variant(int variant) { this.variant = variant; return this; }
    
    @Override
    public FakeEntityBuilder professional(Villager.Profession p) { this.professional = p.name(); return this; }
    
    @Override
    public FakeEntityBuilder catType(Cat.Type t) {
        this.catType = t.name();
        return this;
    }
    
    @Override
    public FakeEntityBuilder foxType(Fox.Type t) {
        this.foxType = t.name();
        return this;
    }
    
    @Override
    public FakeEntityBuilder llamaColor(Llama.Color c) {
        this.llamaColor = c.name();
        return this;
    }
    
    @Override
    public FakeEntityBuilder parrotVariant(Parrot.Variant v) {
        this.parrotVariant = v.name();
        return this;
    }
    
    @Override
    public FakeEntityBuilder rabbitType(Rabbit.Type t) {
        this.rabbitType = t.name();
        return this;
    }
    
    @Override
    public FakeEntityBuilder sheepColor(DyeColor c) {
        this.sheepColor = c.name();
        return this;
    }

    @Override
    public FakeEntityBuilder shulkerColor(DyeColor c) {
        this.shulkerColor = c.name();
        return this;
    }

    @Override
    public FakeEntityBuilder tropicalFishPattern(TropicalFish.Pattern p) {
        this.tropicalFishPattern = p.name();
        return this;
    }
    
    @Override
    public FakeEntityBuilder tropicalFishBodyColor(DyeColor b) {
        this.tropicalFishBodyColor = b.name();
        return this;
    }
    
    @Override
    public FakeEntityBuilder tropicalFishPatternColor(DyeColor p) {
        this.tropicalFishPatternColor = p.name();
        return this;
    }
    
    @Override
    public FakeEntityBuilder frogVariant(String v) {
        this.frogVariant = v;
        return this;
    }
    
    @Override
    public FakeEntityBuilder axolotlVariant(String v) {
        this.axolotlVariant = v;
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
    public FakeEntityBuilder pandaGene(String main, String hidden) {
        this.pandaMainGene = main;
        this.pandaHiddenGene = hidden;
        return this;
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
            safeInvokeEnum(entity, "setProfession", "org.bukkit.entity.Villager", professional);
        }

        if (catType != null) {
            safeInvokeEnum(entity, "setCatType", "org.bukkit.entity.Cat", catType);
        }

        if (foxType != null) {
            safeInvokeEnum(entity, "setFoxType", "org.bukkit.entity.Fox", foxType);
        }

        if (llamaColor != null) {
            safeInvokeEnum(entity, "setColor", "org.bukkit.entity.Llama", llamaColor);
        }

        if (parrotVariant != null) {
            safeInvokeEnum(entity, "setVariant", "org.bukkit.entity.Parrot", parrotVariant);
        }

        if (rabbitType != null) {
            safeInvokeEnum(entity,"setRabbitType", "org.bukkit.entity.Rabbit", rabbitType);
        }

        if (sheepColor != null) {
            safeInvokeEnum(entity, "setColour", "org.bukkit.DyeColor", sheepColor);
        }

        if (tropicalFishBodyColor != null) {
            safeInvokeEnum(entity, "setBodyColor", "org.bukkit.DyeColor", tropicalFishBodyColor);
        }

        if (tropicalFishPattern != null) {
            safeInvokeEnum(entity, "setPattern", "org.bukkit.entity.TropicalFish", tropicalFishPattern);
        }

        if (tropicalFishPatternColor != null) {
            safeInvokeEnum(entity, "setPatternColor", "org.bukkit.DyeColor", tropicalFishPatternColor);
        }

        if (frogVariant != null && VersionUtil.hasWildUpdate()) {
            safeInvokeEnum(entity, "setVariant", "org.bukkit.entity.Frog$Variant", frogVariant);
        }

        if (axolotlVariant != null && VersionUtil.hasCavesAndCliffs()) {
            safeInvokeEnum(entity, "setVariant",  "org.bukkit.entity.Axolotl$Variant", axolotlVariant);
        }

        if (camelSaddle != null && VersionUtil.hasArmoredMobs()) {
            safeInvoke(entity, "setSaddled", camelSaddle);
        }

        if (pandaMainGene != null) {
            safeInvokeEnum(entity, "setMainGene", "org.bukkit.entity.Panda$Gene", pandaMainGene);
            if (pandaHiddenGene != null) safeInvokeEnum(entity, "setHiddenGene", "org.bukkit.entity.Panda$Gene", pandaHiddenGene);
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