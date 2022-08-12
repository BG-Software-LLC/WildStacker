package com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.net.minecraft.world.entity;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildstacker.nms.mapping.Remap;
import com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.MappedObject;
import com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.net.minecraft.world.entity.ai.BehaviorController;
import com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.net.minecraft.world.level.World;
import com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.EnumHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityAgeable;
import net.minecraft.world.entity.EntityExperienceOrb;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.ai.gossip.Reputation;
import net.minecraft.world.entity.ai.navigation.NavigationAbstract;
import net.minecraft.world.entity.animal.EntityAnimal;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.monster.EntityStrider;
import net.minecraft.world.entity.monster.EntityZombie;
import net.minecraft.world.entity.monster.EntityZombieVillager;
import net.minecraft.world.entity.monster.piglin.EntityPiglin;
import net.minecraft.world.entity.npc.EntityVillager;
import net.minecraft.world.entity.npc.EntityVillagerAbstract;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantRecipeList;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.event.entity.EntityPotionEffectEvent;

import javax.annotation.Nullable;
import java.util.List;

public class Entity extends MappedObject<net.minecraft.world.entity.Entity> {

    private static final ReflectMethod<ItemStack> ENTITY_ITEM_GET_ITEM;
    private static final ReflectMethod<Double> ENTITY_GET_X;
    private static final ReflectMethod<Double> ENTITY_GET_Y;
    private static final ReflectMethod<Double> ENTITY_GET_Z;
    private static final ReflectMethod<Void> ENTITY_INSENTIENT_SET_PERSISTENCE_REQUIRED;
    private static final ReflectMethod<net.minecraft.world.phys.AxisAlignedBB> ENTITY_GET_BOUNDING_BOX;
    private static final ReflectMethod<MerchantRecipeList> ENTITY_VILLAGER_ABSTRACT_GET_OFFERS;
    private static final ReflectMethod<net.minecraft.world.entity.Entity> ENTITY_GET_VEHICLE;
    private static final ReflectMethod<List<net.minecraft.world.entity.Entity>> ENTITY_GET_PASSENGERS;
    private static final ReflectMethod<Float> ENTITY_LIVING_GET_MAX_HEALTH;
    private static final ReflectMethod<BlockPosition> ENTITY_BLOCK_POSITION;
    private static final ReflectMethod<VillagerData> ENTITY_VILLAGER_GET_VILLAGER_DATA;
    private static final ReflectMethod<Boolean> ENTITY_INSENTIENT_IS_NO_AI;
    private static final ReflectMethod<Void> ENTITY_ANIMAL_RESET_LOVE;
    private static final ReflectMethod<Reputation> ENTITY_VILLAGER_GET_GOSSIPS;
    private static final ReflectMethod<Boolean> ENTITY_ANIMAL_IS_IN_LOVE;
    private static final ReflectMethod<net.minecraft.world.entity.ai.BehaviorController<EntityPiglin>> ENTITY_PIGLIN_GET_BRAIN;
    private static final ReflectMethod<ItemStack> ENTITY_LIVING_GET_OFFHAND_ITEM;
    private static final ReflectMethod<Boolean> ENTITY_GET_CUSTOM_NAME_VISIBLE;
    private static final ReflectMethod<Iterable<net.minecraft.world.entity.Entity>> ENTITY_GET_INDIRECT_PASSENGERS;

    static {
        ReflectMethod<?> method119 = new ReflectMethod<>(EntityVillager.class, VillagerData.class, "fV");
        boolean is119Mappings = method119.isValid();

        if (is119Mappings) {
            ENTITY_ITEM_GET_ITEM = new ReflectMethod<>(EntityItem.class, "h");
            ENTITY_GET_X = new ReflectMethod<>(net.minecraft.world.entity.Entity.class, "dg");
            ENTITY_GET_Y = new ReflectMethod<>(net.minecraft.world.entity.Entity.class, "di");
            ENTITY_GET_Z = new ReflectMethod<>(net.minecraft.world.entity.Entity.class, "dm");
            ENTITY_INSENTIENT_SET_PERSISTENCE_REQUIRED = new ReflectMethod<>(EntityInsentient.class, "fq");
            ENTITY_GET_BOUNDING_BOX = new ReflectMethod<>(net.minecraft.world.entity.Entity.class, "cz");
            ENTITY_VILLAGER_ABSTRACT_GET_OFFERS = new ReflectMethod<>(EntityVillagerAbstract.class, "fN");
            ENTITY_GET_VEHICLE = new ReflectMethod<>(net.minecraft.world.entity.Entity.class, "cR");
            ENTITY_GET_PASSENGERS = new ReflectMethod<>(net.minecraft.world.entity.Entity.class, "cJ");
            ENTITY_LIVING_GET_MAX_HEALTH = new ReflectMethod<>(EntityLiving.class, "eu");
            ENTITY_BLOCK_POSITION = new ReflectMethod<>(net.minecraft.world.entity.Entity.class, "db");
            ENTITY_VILLAGER_GET_VILLAGER_DATA = new ReflectMethod<>(EntityVillager.class, "fV");
            ENTITY_INSENTIENT_IS_NO_AI = new ReflectMethod<>(EntityInsentient.class, "fB");
            ENTITY_ANIMAL_RESET_LOVE = new ReflectMethod<>(EntityAnimal.class, "fR");
            ENTITY_VILLAGER_GET_GOSSIPS = new ReflectMethod<>(EntityVillager.class, "gg");
            ENTITY_ANIMAL_IS_IN_LOVE = new ReflectMethod<>(EntityAnimal.class, "fQ");
            ENTITY_PIGLIN_GET_BRAIN = new ReflectMethod<>(EntityPiglin.class, "dz");
            ENTITY_LIVING_GET_OFFHAND_ITEM = new ReflectMethod<>(EntityLiving.class, "eB");
            ENTITY_GET_CUSTOM_NAME_VISIBLE = new ReflectMethod<>(net.minecraft.world.entity.Entity.class, "cu");
            ENTITY_GET_INDIRECT_PASSENGERS = new ReflectMethod<>(net.minecraft.world.entity.Entity.class, "cN");
        } else {
            ENTITY_ITEM_GET_ITEM = null;
            ENTITY_GET_X = null;
            ENTITY_GET_Y = null;
            ENTITY_GET_Z = null;
            ENTITY_INSENTIENT_SET_PERSISTENCE_REQUIRED = null;
            ENTITY_GET_BOUNDING_BOX = null;
            ENTITY_VILLAGER_ABSTRACT_GET_OFFERS = null;
            ENTITY_GET_VEHICLE = null;
            ENTITY_GET_PASSENGERS = null;
            ENTITY_LIVING_GET_MAX_HEALTH = null;
            ENTITY_BLOCK_POSITION = null;
            ENTITY_VILLAGER_GET_VILLAGER_DATA = null;
            ENTITY_INSENTIENT_IS_NO_AI = null;
            ENTITY_ANIMAL_RESET_LOVE = null;
            ENTITY_VILLAGER_GET_GOSSIPS = null;
            ENTITY_ANIMAL_IS_IN_LOVE = null;
            ENTITY_PIGLIN_GET_BRAIN = null;
            ENTITY_LIVING_GET_OFFHAND_ITEM = null;
            ENTITY_GET_CUSTOM_NAME_VISIBLE = null;
            ENTITY_GET_INDIRECT_PASSENGERS = null;
        }

    }

    public static Entity ofNullable(net.minecraft.world.entity.Entity handle) {
        return handle == null ? null : new Entity(handle);
    }

    public Entity(net.minecraft.world.entity.Entity handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "getLevel",
            type = Remap.Type.METHOD,
            remappedName = "W")
    public World getWorld() {
        return new World(handle.W());
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "level",
            type = Remap.Type.FIELD,
            remappedName = "s")
    public void setWorld(net.minecraft.world.level.World world) {
        handle.s = world;
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "setCustomName",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public void setCustomName(IChatBaseComponent chatBaseComponent) {
        handle.b(chatBaseComponent);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "getCustomName",
            type = Remap.Type.METHOD,
            remappedName = "Z")
    public IChatBaseComponent getCustomName() {
        return handle.Z();
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "hasCustomName",
            type = Remap.Type.METHOD,
            remappedName = "Y")
    public boolean hasCustomName() {
        return handle.Y();
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "isCustomNameVisible",
            type = Remap.Type.METHOD,
            remappedName = "ct")
    public boolean getCustomNameVisible() {
        return ENTITY_GET_CUSTOM_NAME_VISIBLE == null ? handle.ct() : ENTITY_GET_CUSTOM_NAME_VISIBLE.invoke(handle);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "setCustomNameVisible",
            type = Remap.Type.METHOD,
            remappedName = "n")
    public void setCustomNameVisible(boolean isCustomNameVisible) {
        handle.n(isCustomNameVisible);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "getX",
            type = Remap.Type.METHOD,
            remappedName = "df")
    public double getX() {
        return ENTITY_GET_X == null ? handle.df() : ENTITY_GET_X.invoke(handle);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "getY",
            type = Remap.Type.METHOD,
            remappedName = "dh")
    public double getY() {
        return ENTITY_GET_Y == null ? handle.dh() : ENTITY_GET_Y.invoke(handle);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "getZ",
            type = Remap.Type.METHOD,
            remappedName = "dl")
    public double getZ() {
        return ENTITY_GET_Z == null ? handle.dl() : ENTITY_GET_Z.invoke(handle);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "getType",
            type = Remap.Type.METHOD,
            remappedName = "ad")
    public EntityTypes<?> getType() {
        return handle.ad();
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "getBoundingBox",
            type = Remap.Type.METHOD,
            remappedName = "cy")
    public AxisAlignedBB getBoundingBox() {
        return new AxisAlignedBB(ENTITY_GET_BOUNDING_BOX == null ? handle.cy() : ENTITY_GET_BOUNDING_BOX.invoke(handle));
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "getEntityData",
            type = Remap.Type.METHOD,
            remappedName = "ai")
    public DataWatcher getEntityData() {
        return handle.ai();
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "playSound",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void playSound(SoundEffect soundEffect, float volume, float pitch) {
        handle.a(soundEffect, volume, pitch);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "setPosRaw",
            type = Remap.Type.METHOD,
            remappedName = "o")
    public void setPosRaw(double x, double y, double z) {
        handle.o(x, y, z);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "discard",
            type = Remap.Type.METHOD,
            remappedName = "ah")
    public void discard() {
        handle.ah();
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "moveTo",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public void moveTo(double x, double y, double z, float yaw, float pitch) {
        handle.b(x, y, z, yaw, pitch);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "blockPosition",
            type = Remap.Type.METHOD,
            remappedName = "da")
    public BlockPosition blockPosition() {
        return ENTITY_BLOCK_POSITION == null ? handle.da() : ENTITY_BLOCK_POSITION.invoke(handle);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "getVehicle",
            type = Remap.Type.METHOD,
            remappedName = "cQ")
    public Entity getVehicle() {
        return ofNullable(ENTITY_GET_VEHICLE == null ? handle.cQ() : ENTITY_GET_VEHICLE.invoke(handle));
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "getIndirectPassengers",
            type = Remap.Type.METHOD,
            remappedName = "cM")
    public Iterable<net.minecraft.world.entity.Entity> getIndirectPassengers() {
        return ENTITY_GET_INDIRECT_PASSENGERS == null ? handle.cM() : ENTITY_GET_INDIRECT_PASSENGERS.invoke(handle);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "getPassengers",
            type = Remap.Type.METHOD,
            remappedName = "cI")
    public List<net.minecraft.world.entity.Entity> getPassengers() {
        return ENTITY_GET_PASSENGERS == null ? handle.cI() : ENTITY_GET_PASSENGERS.invoke(handle);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "copyPosition",
            type = Remap.Type.METHOD,
            remappedName = "s")
    public void copyPosition(net.minecraft.world.entity.Entity entity) {
        handle.s(entity);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "gameEvent",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void gameEvent(GameEvent gameEvent) {
        handle.a(gameEvent);
    }

    public CraftEntity getBukkitEntity() {
        return handle.getBukkitEntity();
    }

    @Remap(classPath = "net.minecraft.world.entity.AgeableMob",
            name = "isBaby",
            type = Remap.Type.METHOD,
            remappedName = "y_")
    public boolean isBaby() {
        return ((EntityAgeable) handle).y_();
    }

    @Remap(classPath = "net.minecraft.world.entity.animal.Animal",
            name = "setInLove",
            type = Remap.Type.METHOD,
            remappedName = "f")
    public void setInLove(EntityPlayer entityPlayer) {
        ((EntityAnimal) handle).f(entityPlayer);
    }

    @Remap(classPath = "net.minecraft.world.entity.animal.Animal",
            name = "resetLove",
            type = Remap.Type.METHOD,
            remappedName = "fQ")
    public void resetLove() {
        if (ENTITY_ANIMAL_RESET_LOVE == null) {
            ((EntityAnimal) handle).fQ();
        } else {
            ENTITY_ANIMAL_RESET_LOVE.invoke(handle);
        }
    }

    @Remap(classPath = "net.minecraft.world.entity.animal.Animal",
            name = "isInLove",
            type = Remap.Type.METHOD,
            remappedName = "fP")
    public boolean isInLove() {
        return ENTITY_ANIMAL_IS_IN_LOVE == null ? ((EntityAnimal) handle).fP() :
                ENTITY_ANIMAL_IS_IN_LOVE.invoke(handle);
    }

    @Remap(classPath = "net.minecraft.world.entity.animal.Animal",
            name = "isFood",
            type = Remap.Type.METHOD,
            remappedName = "n")
    public boolean isFood(ItemStack itemStack) {
        return ((EntityAnimal) handle).n(itemStack);
    }

    @Remap(classPath = "net.minecraft.world.entity.decoration.ArmorStand",
            name = "isMarker",
            type = Remap.Type.METHOD,
            remappedName = "t")
    public boolean isMarker() {
        return ((EntityArmorStand) handle).t();
    }

    @Remap(classPath = "net.minecraft.world.entity.Mob",
            name = "isNoAi",
            type = Remap.Type.METHOD,
            remappedName = "fA")
    public boolean isNoAI() {
        return ENTITY_INSENTIENT_IS_NO_AI == null ? ((EntityInsentient) handle).fA() :
                ENTITY_INSENTIENT_IS_NO_AI.invoke(handle);
    }

    @Remap(classPath = "net.minecraft.world.entity.Mob",
            name = "setNoAi",
            type = Remap.Type.METHOD,
            remappedName = "s")
    public void setNoAI(boolean isNoAI) {
        ((EntityInsentient) handle).s(isNoAI);
    }

    @Remap(classPath = "net.minecraft.world.entity.Mob",
            name = "spawnAnim",
            type = Remap.Type.METHOD,
            remappedName = "L")
    public void spawnAnim() {
        ((EntityInsentient) handle).L();
    }

    @Remap(classPath = "net.minecraft.world.entity.Mob",
            name = "getNavigation",
            type = Remap.Type.METHOD,
            remappedName = "D")
    public NavigationAbstract getNavigation() {
        return ((EntityInsentient) handle).D();
    }

    @Remap(classPath = "net.minecraft.world.entity.Mob",
            name = "setPersistenceRequired",
            type = Remap.Type.METHOD,
            remappedName = "fp")
    public void setPersistenceRequired() {
        if (ENTITY_INSENTIENT_SET_PERSISTENCE_REQUIRED == null) {
            ((EntityInsentient) handle).fp();
        } else {
            ENTITY_INSENTIENT_SET_PERSISTENCE_REQUIRED.invoke(handle);
        }
    }

    @Remap(classPath = "net.minecraft.world.entity.Mob",
            name = "finalizeSpawn",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public GroupDataEntity finalizeSpawn(WorldAccess worldAccess, DifficultyDamageScaler difficulty,
                                         EnumMobSpawn spawnReason, @Nullable GroupDataEntity entityData,
                                         @Nullable NBTTagCompound entityNbt) {
        return ((EntityInsentient) handle).a(worldAccess, difficulty, spawnReason, entityData, entityNbt);
    }

    @Remap(classPath = "net.minecraft.world.entity.Mob",
            name = "armorDropChances",
            type = Remap.Type.FIELD,
            remappedName = "bV")
    public float[] getArmorDropChances() {
        return ((EntityInsentient) handle).bV;
    }

    @Remap(classPath = "net.minecraft.world.entity.item.ItemEntity",
            name = "getItem",
            type = Remap.Type.METHOD,
            remappedName = "i")
    public ItemStack getItem() {
        return ENTITY_ITEM_GET_ITEM == null ? ((EntityItem) handle).i() : ENTITY_ITEM_GET_ITEM.invoke(handle);
    }

    @Remap(classPath = "net.minecraft.world.entity.item.ItemEntity",
            name = "pickupDelay",
            type = Remap.Type.FIELD,
            remappedName = "ap")
    public void setPickupDelay(int delay) {
        ((EntityItem) handle).ap = delay;
    }

    @Remap(classPath = "net.minecraft.world.entity.LivingEntity",
            name = "setHealth",
            type = Remap.Type.METHOD,
            remappedName = "c")
    public void setHealth(float health) {
        ((EntityLiving) handle).c(health);
    }

    @Remap(classPath = "net.minecraft.world.entity.LivingEntity",
            name = "onItemPickup",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void onItemPickup(EntityItem entityItem) {
        ((EntityLiving) handle).a(entityItem);
    }

    @Remap(classPath = "net.minecraft.world.entity.LivingEntity",
            name = "hurt",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void hurt(DamageSource damageSource, float damage) {
        handle.a(damageSource, damage);
    }

    @Remap(classPath = "net.minecraft.world.entity.LivingEntity",
            name = "take",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void take(net.minecraft.world.entity.Entity entity, int amount) {
        ((EntityLiving) handle).a(entity, amount);
    }

    @Remap(classPath = "net.minecraft.world.entity.LivingEntity",
            name = "getOffhandItem",
            type = Remap.Type.METHOD,
            remappedName = "eA")
    public ItemStack getOffhandItem() {
        return ENTITY_LIVING_GET_OFFHAND_ITEM == null ? ((EntityLiving) handle).eA() :
                ENTITY_LIVING_GET_OFFHAND_ITEM.invoke(handle);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "setItemSlot",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void setItemSlot(EnumItemSlot itemSlot, ItemStack itemStack) {
        handle.a(itemSlot, itemStack);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "spawnAtLocation",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public void spawnAtLocation(ItemStack itemStack) {
        handle.b(itemStack);
    }

    @Remap(classPath = "net.minecraft.world.entity.LivingEntity",
            name = "broadcastBreakEvent",
            type = Remap.Type.METHOD,
            remappedName = "d")
    public void broadcastBreakEvent(EnumItemSlot itemSlot) {
        ((EntityLiving) handle).d(itemSlot);
    }

    @Remap(classPath = "net.minecraft.world.entity.LivingEntity",
            name = "getItemBySlot",
            type = Remap.Type.METHOD,
            remappedName = "c")
    public ItemStack getItemBySlot(EnumItemSlot enumItemSlot) {
        return ((EntityLiving) handle).c(enumItemSlot);
    }

    @Remap(classPath = "net.minecraft.world.entity.LivingEntity",
            name = "addAdditionalSaveData",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public void addAdditionalSaveData(NBTTagCompound nbtTagCompound) {
        ((EntityLiving) handle).b(nbtTagCompound);
    }

    @Remap(classPath = "net.minecraft.world.entity.LivingEntity",
            name = "getMaxHealth",
            type = Remap.Type.METHOD,
            remappedName = "et")
    public float getMaxHealth() {
        return ENTITY_LIVING_GET_MAX_HEALTH == null ? ((EntityLiving) handle).et() :
                ENTITY_LIVING_GET_MAX_HEALTH.invoke(handle);
    }

    @Remap(classPath = "net.minecraft.world.entity.LivingEntity",
            name = "readAdditionalSaveData",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void readAdditionalSaveData(NBTTagCompound nbtTagCompound) {
        ((EntityLiving) handle).a(nbtTagCompound);
    }

    @Remap(classPath = "net.minecraft.world.entity.LivingEntity",
            name = "getItemInHand",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public ItemStack getItemInHand(EnumHand enumHand) {
        return ((EntityLiving) handle).b(enumHand);
    }

    public void removeAllEffects(EntityPotionEffectEvent.Cause cause) {
        ((EntityLiving) handle).removeAllEffects(cause);
    }

    public void addEffect(MobEffect mobEffect, EntityPotionEffectEvent.Cause cause) {
        ((EntityLiving) handle).addEffect(mobEffect, cause);
    }

    @Remap(classPath = "net.minecraft.world.entity.monster.piglin.Piglin",
            name = "getBrain",
            type = Remap.Type.METHOD,
            remappedName = "dy")
    public BehaviorController<EntityPiglin> getBrain() {
        return new BehaviorController<>(ENTITY_PIGLIN_GET_BRAIN == null ? ((EntityPiglin) handle).dy() :
                ENTITY_PIGLIN_GET_BRAIN.invoke(handle));
    }

    @Remap(classPath = "net.minecraft.world.entity.monster.Strider",
            name = "isSaddled",
            type = Remap.Type.METHOD,
            remappedName = "d")
    public boolean isSaddled() {
        return ((EntityStrider) handle).d();
    }

    @Remap(classPath = "net.minecraft.world.entity.monster.Strider",
            name = "steering",
            type = Remap.Type.FIELD,
            remappedName = "cg")
    public SaddleStorage getSaddleStorage() {
        return new SaddleStorage(((EntityStrider) handle).cg);
    }

    @Remap(classPath = "net.minecraft.world.entity.npc.Villager",
            name = "getVillagerData",
            type = Remap.Type.METHOD,
            remappedName = "fU")
    public VillagerData getVillagerData() {
        return ENTITY_VILLAGER_GET_VILLAGER_DATA == null ? ((EntityVillager) handle).fU() :
                ENTITY_VILLAGER_GET_VILLAGER_DATA.invoke(handle);
    }

    @Remap(classPath = "net.minecraft.world.entity.npc.Villager",
            name = "getGossips",
            type = Remap.Type.METHOD,
            remappedName = "gf")
    public Reputation getGossips() {
        return ENTITY_VILLAGER_GET_GOSSIPS == null ? ((EntityVillager) handle).gf() :
                ENTITY_VILLAGER_GET_GOSSIPS.invoke(handle);
    }

    @Remap(classPath = "net.minecraft.world.entity.npc.AbstractVillager",
            name = "getOffers",
            type = Remap.Type.METHOD,
            remappedName = "fM")
    public MerchantRecipeList getOffers() {
        return ENTITY_VILLAGER_ABSTRACT_GET_OFFERS == null ? ((EntityVillagerAbstract) handle).fM() :
                ENTITY_VILLAGER_ABSTRACT_GET_OFFERS.invoke(handle);
    }

    @Remap(classPath = "net.minecraft.world.entity.monster.Zombie",
            name = "setBaby",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void setBaby(boolean isBaby) {
        ((EntityZombie) handle).a(isBaby);
    }

    @Remap(classPath = "net.minecraft.world.entity.monster.ZombieVillager",
            name = "setVillagerData",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void setVillagerData(VillagerData villagerData) {
        ((EntityZombieVillager) handle).a(villagerData);
    }

    @Remap(classPath = "net.minecraft.world.entity.monster.ZombieVillager",
            name = "setTradeOffers",
            type = Remap.Type.METHOD,
            remappedName = "c")
    public void setTradeOffers(NBTTagCompound offerTag) {
        ((EntityZombieVillager) handle).c(offerTag);
    }

    @Remap(classPath = "net.minecraft.world.entity.monster.ZombieVillager",
            name = "setVillagerXp",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void setVillagerXp(int experience) {
        ((EntityZombieVillager) handle).a(experience);
    }

    @Remap(classPath = "net.minecraft.world.entity.monster.ZombieVillager",
            name = "setVillagerXp",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void setGossips(NBTBase gossips) {
        ((EntityZombieVillager) handle).a(gossips);
    }

    @Remap(classPath = "net.minecraft.world.entity.ExperienceOrb",
            name = "value",
            type = Remap.Type.FIELD,
            remappedName = "aq")
    public void setExperienceAmount(int experience) {
        ((EntityExperienceOrb) handle).aq = experience;
    }

    @Remap(classPath = "net.minecraft.world.damagesource.DamageSource",
            name = "playerAttack",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public static DamageSource playerAttack(EntityHuman entityHuman) {
        return DamageSource.a(entityHuman);
    }

    @Remap(classPath = "net.minecraft.world.entity.LivingEntity",
            name = "getEquipmentSlotForItem",
            type = Remap.Type.METHOD,
            remappedName = "i")
    public static EnumItemSlot getEquipmentSlotForItem(ItemStack itemStack) {
        return EntityLiving.i(itemStack);
    }

}
