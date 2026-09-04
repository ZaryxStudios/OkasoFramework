package com.zaryxstudios.okaso.entity;

import com.zaryxstudios.okaso.common.entity.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OkasoBukkitEntityService implements EntityService {

    private static final boolean HAS_GET_ENTITY_UUID;
    private static final Method GET_ENTITY_UUID_METHOD;

    static {
        boolean has = false;
        Method m = null;
        try {
            m = Bukkit.class.getMethod("getEntity", UUID.class); has = true;
        } catch (NoSuchMethodException ignored) {

        }
        HAS_GET_ENTITY_UUID = has;
        GET_ENTITY_UUID_METHOD = m;
    }

    @Override @SuppressWarnings("unchecked")
    public <T> Collection<T> getEntitiesInWorld(Object world, Class<T> type) {
        if (world instanceof World) {
            List<T> out = new ArrayList<>();
            for (Entity e : ((World) world).getEntities()) {
                if (type.isInstance(e)) {
                    out.add((T) e);
                }
            }
            return out;
        }
        return Collections.emptyList();
    }

    @Override @SuppressWarnings("unchecked")
    public <T> Collection<T> getNearbyEntities(Object location, double radius, Class<T> type) {
        if (location instanceof Location) {
            Location c = (Location) location;
            List<T> out = new ArrayList<>();
            for (Entity e : c.getWorld().getEntities()) {
                if (type.isInstance(e) && e.getLocation().distanceSquared(c) <= radius * radius) {
                    out.add((T) e);
                }
            }
            return out;
        }
        return Collections.emptyList();
    }

    @Override @SuppressWarnings("unchecked")
    public Optional<Object> getEntity(UUID uuid) {
        if (HAS_GET_ENTITY_UUID) {
            try {
                Entity e = (Entity) GET_ENTITY_UUID_METHOD.invoke(null, uuid);
                return Optional.ofNullable(e);
            } catch (Exception ignored) {}
        }
        for (World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {
                if (e.getUniqueId().equals(uuid)) {
                    return Optional.of(e);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isValid(Object entity) {
        return entity instanceof Entity && ((Entity) entity).isValid();
    }

    @Override
    public void remove(Object entity) {
        if (entity instanceof Entity) ((Entity) entity).remove();
    }

    @Override
    public void teleport(Object entity, Object location) {
         if (entity instanceof Entity && location instanceof Location) {
            ((Entity) entity).teleport((Location) location); 
         }
    }
    @Override
    public Object getLocation(Object entity) {
        return entity instanceof Entity ? ((Entity) entity).getLocation() : null; 
    }

    @Override
    public Object getWorld(Object entity) {
        return entity instanceof Entity ? ((Entity) entity).getWorld() : null; 
    }

    @Override
    public String getName(Object entity) {
        if (!(entity instanceof Entity)){
            return "";
        }
        Entity e = (Entity) entity;
        String cn = e.getCustomName();
        if (cn != null) {
            return cn;
        }
        try {
            return (String) e.getClass().getMethod("getName").invoke(e); 
        } catch (Exception ignored) {
        }
        return "";
    }

    @Override
    public void setFire(Object entity, int ticks) {
        if (entity instanceof Entity) ((Entity) entity).setFireTicks(ticks); }

    @Override
    public String getType(Object entity) {
        return entity instanceof Entity ? ((Entity) entity).getType().name() : "";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Object> getPassengers(Object entity) {
        return entity instanceof Entity ? (Collection<Object>) (Collection<?>) ((Entity) entity).getPassengers() : Collections.emptyList();
    }

    @Override
    public NPCHandle createFakePlayer(String name, Location loc, Consumer<FakePlayerBuilder> builder) {
        Entity base = spawnPlaceholder(loc, true);
        FakePlayerBuilderImpl impl = new FakePlayerBuilderImpl(base);
        builder.accept(impl);
        impl.apply();
        return new PacketNPCHandle(base, true);
    }

    @Override
    public NPCHandle createFakeEntity(EntityType type, Location loc, Consumer<FakeEntityBuilder> builder) {
        Entity base = spawnEntity(type, loc);
        FakeEntityBuilderImpl impl = new FakeEntityBuilderImpl(base);
        builder.accept(impl);
        impl.apply();
        return new PacketNPCHandle(base, false);
    }

    @Override
    public NPCHandle createNPC(NPCType npcType, Location loc, Consumer<NPCBuilder> builder) {
        return npcType == NPCType.FAKE_PLAYER
                ? createFakePlayer("NPC", loc, b -> builder.accept(b))
                : createFakeEntity(EntityType.ZOMBIE, loc, b -> builder.accept(b));
    }

    private Entity spawnPlaceholder(Location loc, boolean forceArmorStand) {
        if (VersionUtil.hasArmorStand() && (forceArmorStand || VersionUtil.hasArmorStand())) {
            return loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        }
        Entity z = loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
        safeInvoke(z, "setAI", false);
        safeInvoke(z, "setGravity", false);
        safeInvoke(z, "setSilent", true);
        safeInvoke(z, "setInvulnerable", true);
        safeInvoke(z, "setCustomNameVisible", true);
        z.setCustomName("§r");
        return z;
    }

    private Entity spawnEntity(EntityType type, Location loc) { return loc.getWorld().spawnEntity(loc, type); }

    private void safeInvoke(Object target, String methodName, Object... args) {
        try {
            Class<?>[] types = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) types[i] = args[i].getClass();
            Method m = target.getClass().getMethod(methodName, types);
            m.invoke(target, args);
        } catch (Exception ignored) {}
    }

    final class FakePlayerBuilderImpl implements FakePlayerBuilder {
        private final Entity entity;
        private float yaw, pitch;
        private ItemStack helmet, chest, legs, boots;
        private final List<PotionEffect> effects = new ArrayList<>();
        private String customName;
        private boolean customNameVisible = true;
        private boolean gravity = false, invulnerable = true, silent = true, glowing = false, ai = false;
        private double moveSpeed = 0.2;
        private NPCHandle followTarget;
        private boolean attackPlayers = false, interactable = true;
        private String skinUrl;
        private ItemStack mainHand, offHand;
        private boolean sneaking, sprinting, swimming, gliding;

        FakePlayerBuilderImpl(Entity e) { this.entity = e; }

        @Override
        public FakePlayerBuilder rotation(float yaw, float pitch) {
            this.yaw = yaw; this.pitch = pitch;
            return this;
        }
        
        @Override
        public FakePlayerBuilder armor(ItemStack h, ItemStack c, ItemStack l, ItemStack b) {
            helmet = h; chest = c; legs = l; boots = b;
            return this;
        }
        
        @Override
        public FakePlayerBuilder addPotionEffect(PotionEffect pe) {
            effects.add(pe);
            return this;
        }
        
        @Override
        public FakePlayerBuilder removePotionEffect(org.bukkit.potion.PotionEffectType t) {
            effects.removeIf(e -> e.getType().equals(t));
            return this;
        }
        
        @Override
        public FakePlayerBuilder customName(String n) {
            customName = n;
            return this;
        }

        @Override
        public FakePlayerBuilder customNameVisible(boolean v) {
            customNameVisible = v;
            return this;
        }

        @Override
        public FakePlayerBuilder gravity(boolean g) {
            gravity = g;
            return this;
        }

        @Override
        public FakePlayerBuilder invulnerable(boolean i) {
            invulnerable = i;
            return this;
        }

        @Override
        public FakePlayerBuilder silent(boolean s) {
            silent = s;
            return this;
        }

        @Override
        public FakePlayerBuilder glow(boolean g) {
            glowing = g;
            return this;
        }

        @Override
        public FakePlayerBuilder ai(boolean a) {
            ai = a;
            return this;
        }

        @Override
        public FakePlayerBuilder moveSpeed(double s) {
            moveSpeed = s;
            return this;
        }

        @Override
        public FakePlayerBuilder followTarget(NPCHandle t) {
            followTarget = t;
            return this;
        }

        @Override
        public FakePlayerBuilder attackPlayers(boolean a) {
            attackPlayers = a;
            return this;
        }

        @Override
        public FakePlayerBuilder interactable(boolean i) {
            interactable = i;
            return this;
        }

        @Override
        public FakePlayerBuilder skin(String url) {
            skinUrl = url;
            return this;
        }

        @Override
        public FakePlayerBuilder skin(byte[] s, byte[] c) {
            return this;
        }

        @Override
        public FakePlayerBuilder heldItemMainHand(ItemStack i) {
            mainHand = i;
            return this;
        }

        @Override
        public FakePlayerBuilder heldItemOffHand(ItemStack i) {
            offHand = i;
            return this;
        }

        @Override
        public FakePlayerBuilder displayName(String n) {
            customName = n;
            return this;
        }

        @Override
        public FakePlayerBuilder sneaking(boolean s) {
            sneaking = s;
            return this;
        }

        @Override
        public FakePlayerBuilder sprinting(boolean s) {
            sprinting = s;
            return this;

        }

        @Override
        public FakePlayerBuilder swimming(boolean s) {
            swimming = s;
            return this;
        }

        @Override
        public FakePlayerBuilder gliding(boolean g) {
            gliding = g;
            return this;
        }

        public void apply() {
            if (customName != null) {
                entity.setCustomName(customName);
                safeInvoke(entity, "setCustomNameVisible", customNameVisible);
            }
            safeInvoke(entity, "setGravity", gravity);
            safeInvoke(entity, "setInvulnerable", invulnerable);
            safeInvoke(entity, "setSilent", silent);
            safeInvoke(entity, "setGlowing", glowing);
            if (VersionUtil.hasPoseFlags()) {
                safeInvoke(entity, "setSneaking", sneaking);
                safeInvoke(entity, "setSprinting", sprinting);
                safeInvoke(entity, "setSwimming", swimming);
                safeInvoke(entity, "setGliding", gliding);
            }
            if (entity instanceof LivingEntity le) {
                if (helmet != null) le.getEquipment().setHelmet(helmet);
                if (chest != null) le.getEquipment().setChestplate(chest);
                if (legs != null) le.getEquipment().setLeggings(legs);
                if (boots != null) le.getEquipment().setBoots(boots);
                if (mainHand != null) le.getEquipment().setItemInMainHand(mainHand);
                if (offHand != null) le.getEquipment().setItemInOffHand(offHand);
                for (PotionEffect pe : effects) le.addPotionEffect(pe, true);
            }
        }
    }

    final class FakeEntityBuilderImpl implements FakeEntityBuilder {
        private final Entity entity;
        private float yaw, pitch;
        private ItemStack helmet, chest, legs, boots;
        private final List<PotionEffect> effects = new ArrayList<>();
        private String customName;
        private boolean customNameVisible = true;
        private boolean gravity = true, invulnerable = false, silent = false, glowing = false, ai = true;
        private double moveSpeed = 0.2;
        private NPCHandle followTarget;
        private boolean attackPlayers = false, interactable = true;
        private EntityType entityType;
        private ItemStack heldItem;
        private boolean baby = false;
        private int variant = 0;

        FakeEntityBuilderImpl(Entity e) {
            this.entity = e;
        }

        @Override
        public FakeEntityBuilder rotation(float yaw, float pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
            return this;
        }

        @Override
        public FakeEntityBuilder armor(ItemStack h, ItemStack c, ItemStack l, ItemStack b) {
            helmet = h;
            chest = c;
            legs = l;
            boots = b;
            return this;
        }

        @Override
        public FakeEntityBuilder addPotionEffect(PotionEffect pe) {
            effects.add(pe);
            return this;
        }
        
        @Override
        public FakeEntityBuilder removePotionEffect(org.bukkit.potion.PotionEffectType t) {
            effects.removeIf(e -> e.getType().equals(t));
            return this;
        }
        
        @Override
        public FakeEntityBuilder customName(String n) {
            customName = n;
            return this;
        }
        
        @Override
        public FakeEntityBuilder customNameVisible(boolean v) {
            customNameVisible = v; 
            return this;
        }
    
        @Override
        public FakeEntityBuilder gravity(boolean g) {
            gravity = g; 
            return this;
        }
        
        @Override
        public FakeEntityBuilder invulnerable(boolean i) {
            invulnerable = i;
            return this;
        }
        
        @Override
        public FakeEntityBuilder silent(boolean s) {
            silent = s;
            return this;
        }
        
        @Override 
        public FakeEntityBuilder glow(boolean g) {
            glowing = g;
            return this;
        }
        
        @Override
        public FakeEntityBuilder ai(boolean a) {
            ai = a;
            return this;
        }
        
        @Override
        public FakeEntityBuilder moveSpeed(double s) { 
            moveSpeed = s;
            return this;
        }
        
        @Override
        public FakeEntityBuilder followTarget(NPCHandle t) {
            followTarget = t;
            return this;
        }
        
        @Override
        public FakeEntityBuilder attackPlayers(boolean a) {
            attackPlayers = a;
            return this; 
        }
        
        @Override
        public FakeEntityBuilder interactable(boolean i) {
            interactable = i;
            return this;
        }
        
        @Override
        public FakeEntityBuilder entityType(EntityType t) {
            entityType = t;
            return this;
        }
        
        @Override
        public FakeEntityBuilder heldItem(ItemStack i) {
            heldItem = i;
            return this;
        }
        
        @Override
        public FakeEntityBuilder baby(boolean b) {
            baby = b;
            return this;
        }
        
        @Override
        public FakeEntityBuilder variant(int v) {
            variant = v;
            return this;
        }
        
        @Override
        public FakeEntityBuilder professional(Villager.Profession p) {
            return this;
        }
        
        @Override
        public FakeEntityBuilder catType(Cat.Type t) {
            return this;
        }
        
        @Override
        public FakeEntityBuilder foxType(Fox.Type t) {
            return this;
        }
        
        @Override
        public FakeEntityBuilder llamaColor(Llama.Color c) {
            return this;
        }
        
        @Override
        public FakeEntityBuilder parrotVariant(Parrot.Variant v) {
            return this;
        }
        
        @Override
        public FakeEntityBuilder rabbitType(Rabbit.Type t) {
            return this;
        }
        
        @Override
        public FakeEntityBuilder sheepColor(DyeColor c) {
            return this;
        }
        
        @Override
        public FakeEntityBuilder shulkerColor(DyeColor c) {
            return this;
        }
        
        @Override
        public FakeEntityBuilder tropicalFishPattern(TropicalFish.Pattern p) {
            return this;
        }
        
        @Override
        public FakeEntityBuilder tropicalFishBodyColor(DyeColor b) {
            return this;
        }
        
        @Override
        public FakeEntityBuilder tropicalFishPatternColor(DyeColor p) {
            return this;
        }
        
        @Override
        public FakeEntityBuilder frogVariant(Frog.Variant v) {
            return this;
        }
        
        @Override
        public FakeEntityBuilder axolotlVariant(Axolotl.Variant v) {
            return this;
        }
        
        @Override
        public FakeEntityBuilder camel(Boolean saddle) {
            return this;
        }
        
        @Override
        public FakeEntityBuilder sniffer(Boolean sniffing) {
            return this;
        }
        
        @Override
        public FakeEntityBuilder pandaGene(Panda.Gene m, Panda.Gene h) {
            return this;
        }

        public void apply() {
            if (customName != null) {
                entity.setCustomName(customName);
                safeInvoke(entity, "setCustomNameVisible", customNameVisible);
            }
            safeInvoke(entity, "setGravity", gravity);
            safeInvoke(entity, "setInvulnerable", invulnerable);
            safeInvoke(entity, "setSilent", silent);
            safeInvoke(entity, "setGlowing", glowing);
            safeInvoke(entity, "setAI", ai);
            if (entity instanceof LivingEntity le) {

                if (helmet != null) {
                    le.getEquipment().setHelmet(helmet);
                }

                if (chest != null) {
                    le.getEquipment().setChestplate(chest);
                }

                if (legs != null) {
                    le.getEquipment().setLeggings(legs);
                }

                if (boots != null) {
                    le.getEquipment().setBoots(boots);
                }

                if (heldItem != null) {
                    le.getEquipment().setItemInMainHand(heldItem);
                }
                for (PotionEffect pe : effects) {
                    le.addPotionEffect(pe, true);
                } 
                safeInvoke(le, "setBaby", baby);
                safeInvoke(le, "setVariant", variant);
            }
        }
    }

    public static final class PacketNPCHandle implements NPCHandle {
        private final Entity entity;
        private final boolean fakePlayer;
        private boolean spawned;

        PacketNPCHandle(Entity entity, boolean fakePlayer) {
            this.entity = entity;
            this.fakePlayer = fakePlayer; 
        }

        @Override
        public void spawn() {
            if (!spawned) spawned = true;
        }

        @Override
        public void despawn() {
            if (spawned && entity.isValid()) {
                entity.remove(); spawned = false;
            }
        }

        @Override
        public void setLocation(Location loc) {
            entity.teleport(loc);
        }

        @Override
        public Location getLocation() {
            return entity.getLocation(); 
        }

        @Override
        public boolean isSpawned() {
            return spawned && entity.isValid();
        }

        @Override
        public UUID getUniqueId() {
            return entity.getUniqueId();
        }

        public Entity getEntity() { 
            return entity;
        }

        public boolean isFakePlayer() {
            return fakePlayer;
        }
    }

    private static final class VersionUtil {

        private static final int MAJOR, MINOR, PATCH;

        static {
            String ver = Bukkit.getBukkitVersion();
            Matcher m = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)").matcher(ver);

            if (m.find()) {
                MAJOR = Integer.parseInt(m.group(1));
                MINOR = Integer.parseInt(m.group(2));
                PATCH = Integer.parseInt(m.group(3));
            } else {
                MAJOR = 1;
                MINOR = 7;
                PATCH = 10;
            }
        }
        private static boolean atLeast(int major, int minor, int patch) {

            if (MAJOR != major) {
                return MAJOR > major;
            }

            if (MINOR != minor) {
                return MINOR > minor;
            }
            return PATCH >= patch;
        }

        static boolean hasArmorStand() {
            return atLeast(1,8,0);
        }

        static boolean hasPoseFlags() {
            return atLeast(1,9,0);
        }

        static boolean hasFlattenedEntityTypes() {
            return atLeast(1,13,0);
        }

        static boolean hasVillagerProfessions() {
            return atLeast(1,14,0); 
        }

        static boolean hasNewMobVariants() {
            return atLeast(1,16,0);
        }

        static boolean hasCavesAndCliffs() {
            return atLeast(1,17,0);
        }

        static boolean hasWildUpdate() {
            return atLeast(1,19,0);
        }

        static boolean hasArmoredMobs() {
            return atLeast(1,20,5);
        }
    }
}