package com.bgsoftware.wildstacker.nms;

import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.tags.TagKey;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.INamableTileEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAgeable;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.SaddleStorage;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.gossip.Reputation;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.NavigationAbstract;
import net.minecraft.world.entity.animal.EntityAnimal;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.monster.EntityStrider;
import net.minecraft.world.entity.monster.EntityZombie;
import net.minecraft.world.entity.monster.EntityZombieVillager;
import net.minecraft.world.entity.monster.piglin.EntityPiglin;
import net.minecraft.world.entity.npc.EntityVillager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantRecipeList;
import net.minecraft.world.level.EnumSkyBlock;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockLightAccess;
import net.minecraft.world.level.IEntityAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.IWorldWriter;
import net.minecraft.world.level.MobSpawnerAbstract;
import net.minecraft.world.level.MobSpawnerData;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AxisAlignedBB;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftVillager;
import org.bukkit.event.entity.CreatureSpawnEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class NMSMappings_v1_18_R2 {

    private NMSMappings_v1_18_R2() {

    }

    public static AxisAlignedBB inflate(AxisAlignedBB boundingBox, double modX, double modY, double modZ) {
        return boundingBox.c(modX, modY, modZ);
    }

    public static int getX(BaseBlockPosition baseBlockPosition) {
        return baseBlockPosition.u();
    }

    public static int getY(BaseBlockPosition baseBlockPosition) {
        return baseBlockPosition.v();
    }

    public static int getZ(BaseBlockPosition baseBlockPosition) {
        return baseBlockPosition.w();
    }

    public static void eraseMemory(BehaviorController<?> behaviorController, MemoryModuleType<?> memoryModuleType) {
        behaviorController.b(memoryModuleType);
    }

    public static boolean hasMemoryValue(BehaviorController<?> behaviorController, MemoryModuleType<?> memoryModuleType) {
        return behaviorController.a(memoryModuleType);
    }

    public static void broadcast(ChunkProviderServer chunkProviderServer, Entity entity, Packet<?> packet) {
        chunkProviderServer.b(entity, packet);
    }

    public static World getWorld(Entity entity) {
        return entity.W();
    }

    public static void setCustomName(Entity entity, IChatBaseComponent chatBaseComponent) {
        entity.a(chatBaseComponent);
    }

    public static boolean getCustomNameVisible(Entity entity) {
        return entity.cr();
    }

    public static void setCustomNameVisible(Entity entity, boolean isCustomNameVisible) {
        entity.n(isCustomNameVisible);
    }

    public static double getX(Entity entity) {
        return entity.dc();
    }

    public static double getY(Entity entity) {
        return entity.de();
    }

    public static double getZ(Entity entity) {
        return entity.di();
    }

    public static EntityTypes<?> getType(Entity entity) {
        return entity.ad();
    }

    public static AxisAlignedBB getBoundingBox(Entity entity) {
        return entity.cw();
    }

    public static int getId(Entity entity) {
        return entity.getBukkitEntity().getEntityId();
    }

    public static DataWatcher getEntityData(Entity entity) {
        return entity.ai();
    }

    public static void playSound(Entity entity, SoundEffect soundEffect, float volume, float pitch) {
        entity.a(soundEffect, volume, pitch);
    }

    public static void setPosRaw(Entity entity, double x, double y, double z) {
        entity.o(x, y, z);
    }

    public static void discard(Entity entity) {
        entity.ah();
    }

    public static Set<String> getTags(Entity entity) {
        return entity.af();
    }

    public static void moveTo(Entity entity, double x, double y, double z, float yaw, float pitch) {
        entity.b(x, y, z, yaw, pitch);
    }

    public static BlockPosition blockPosition(Entity entity) {
        return entity.cW();
    }

    public static Entity getVehicle(Entity entity) {
        return entity.cN();
    }

    public static Iterable<Entity> getIndirectPassengers(Entity entity) {
        return entity.cJ();
    }

    public static List<Entity> getPassengers(Entity entity) {
        return entity.cF();
    }

    public static boolean isBaby(EntityAgeable entityAgeable) {
        return entityAgeable.y_();
    }

    public static void setInLove(EntityAnimal entityAnimal, EntityPlayer entityPlayer) {
        entityAnimal.g(entityPlayer);
    }

    public static void resetLove(EntityAnimal entityAnimal) {
        entityAnimal.fG();
    }

    public static boolean isInLove(EntityAnimal entityAnimal) {
        return entityAnimal.fF();
    }

    public static boolean isFood(EntityAnimal entityAnimal, ItemStack itemStack) {
        return entityAnimal.n(itemStack);
    }

    public static boolean isMarker(EntityArmorStand entityArmorStand) {
        return entityArmorStand.t();
    }

    public static DamageSource playerAttack(EntityHuman entityHuman) {
        return DamageSource.a(entityHuman);
    }

    public static boolean isNoAI(EntityInsentient entityInsentient) {
        return entityInsentient.fs();
    }

    public static void setNoAI(EntityInsentient entityInsentient, boolean isNoAI) {
        entityInsentient.s(isNoAI);
    }

    public static void spawnAnim(EntityInsentient entityInsentient) {
        entityInsentient.L();
    }

    public static NavigationAbstract getNavigation(EntityInsentient entityInsentient) {
        return entityInsentient.D();
    }

    public static void setPersistenceRequired(EntityInsentient entityInsentient) {
        entityInsentient.fh();
    }

    public static GroupDataEntity finalizeSpawn(EntityInsentient entityInsentient, WorldAccess worldAccess,
                                                DifficultyDamageScaler difficulty, EnumMobSpawn spawnReason,
                                                @Nullable GroupDataEntity entityData, @Nullable NBTTagCompound entityNbt) {
        return entityInsentient.a(worldAccess, difficulty, spawnReason, entityData, entityNbt);
    }

    public static ItemStack getItem(EntityItem entityItem) {
        return entityItem.h();
    }

    public static void setHealth(EntityLiving entityLiving, float health) {
        entityLiving.c(health);
    }

    public static void hurt(EntityLiving entityLiving, DamageSource damageSource, float damage) {
        entityLiving.a(damageSource, damage);
    }

    public static void take(EntityLiving entityLiving, Entity entity, int amount) {
        entityLiving.a(entity, amount);
    }

    public static ItemStack getOffhandItem(EntityLiving entityLiving) {
        return entityLiving.et();
    }

    public static void setItemSlot(EntityLiving entityLiving, EnumItemSlot itemSlot, ItemStack itemStack) {
        entityLiving.a(itemSlot, itemStack);
    }

    public static ItemStack getItemBySlot(EntityLiving entityLiving, EnumItemSlot enumItemSlot) {
        return entityLiving.b(enumItemSlot);
    }

    public static void addAdditionalSaveData(EntityLiving entityLiving, NBTTagCompound nbtTagCompound) {
        entityLiving.b(nbtTagCompound);
    }

    public static float getMaxHealth(EntityLiving entityLiving) {
        return entityLiving.em();
    }

    public static void readAdditionalSaveData(EntityLiving entityLiving, NBTTagCompound nbtTagCompound) {
        entityLiving.a(nbtTagCompound);
    }

    public static BehaviorController<EntityPiglin> getBrain(EntityPiglin entityPiglin) {
        return entityPiglin.du();
    }

    public static boolean isSaddled(EntityStrider entityStrider) {
        return entityStrider.d();
    }

    public static SaddleStorage getSaddleStorage(EntityStrider entityStrider) {
        return entityStrider.ce;
    }

    public static VillagerData getVillagerData(EntityVillager entityVillager) {
        return entityVillager.fK();
    }

    public static Reputation getGossips(EntityVillager entityVillager) {
        return entityVillager.fW();
    }

    public static MerchantRecipeList getOffers(EntityVillager entityVillager) {
        return entityVillager.fB();
    }

    public static int getExperience(EntityVillager entityVillager) {
        return ((CraftVillager) entityVillager.getBukkitEntity()).getVillagerExperience();
    }

    public static void setBaby(EntityZombie entityZombie, boolean isBaby) {
        entityZombie.a(isBaby);
    }

    public static void setVillagerData(EntityZombieVillager entityVillager, VillagerData villagerData) {
        entityVillager.a(villagerData);
    }

    public static void setTradeOffers(EntityZombieVillager entityZombieVillager, NBTTagCompound offerTag) {
        entityZombieVillager.c(offerTag);
    }

    public static void setVillagerXp(EntityZombieVillager entityZombieVillager, int experience) {
        entityZombieVillager.a(experience);
    }

    public static void levelEvent(GeneratorAccess generatorAccess, EntityHuman entityHuman, int i,
                                  BlockPosition blockPosition, int j) {
        generatorAccess.a(entityHuman, i, blockPosition, j);
    }

    public static EnumDifficulty getDifficulty(GeneratorAccess generatorAccess) {
        return generatorAccess.af();
    }

    public static void levelEvent(GeneratorAccess generatorAccess, int i, BlockPosition position, int j) {
        generatorAccess.c(i, position, j);
    }

    public static Block getBlock(IBlockData blockData) {
        return blockData.b();
    }

    public static int getRawBrightness(IBlockLightAccess blockLightAccess, BlockPosition position, int ambientDarkness) {
        return blockLightAccess.b(position, ambientDarkness);
    }

    public static int getBrightness(IBlockLightAccess blockLightAccess, EnumSkyBlock skyBlock, BlockPosition position) {
        return blockLightAccess.a(skyBlock, position);
    }

    public static <T extends Entity> List<T> getEntitiesOfClass(IEntityAccess entityAccess, Class<T> entityClass,
                                                                AxisAlignedBB boundingBox) {
        return entityAccess.a(entityClass, boundingBox);
    }

    public static boolean hasCustomName(INamableTileEntity namableTileEntity) {
        return namableTileEntity.Y();
    }

    public static IChatBaseComponent getCustomName(INamableTileEntity namableTileEntity) {
        return namableTileEntity.Z();
    }

    public static boolean canBeDepleted(Item item) {
        return item.o();
    }

    public static Item getItem(ItemStack itemStack) {
        return itemStack.c();
    }

    public static boolean isEmpty(ItemStack itemStack) {
        return itemStack.b();
    }

    public static void shrink(ItemStack itemStack, int amount) {
        itemStack.g(amount);
    }

    public static NBTTagCompound getOrCreateTag(ItemStack itemStack) {
        return itemStack.u();
    }

    public static void setTag(ItemStack itemStack, NBTTagCompound nbtTagCompound) {
        itemStack.c(nbtTagCompound);
    }

    public static ItemStack copy(ItemStack itemStack) {
        return itemStack.n();
    }

    public static void setCount(ItemStack itemStack, int count) {
        itemStack.e(count);
    }

    public static EnumItemSlot getEquipmentSlotForItem(ItemStack itemStack) {
        return EntityLiving.i(itemStack);
    }

    public static int getDamageValue(ItemStack itemStack) {
        return itemStack.i();
    }

    public static void setDamageValue(ItemStack itemStack, int damage) {
        itemStack.b(damage);
    }

    public static void save(ItemStack itemStack, NBTTagCompound nbtTagCompound) {
        itemStack.b(nbtTagCompound);
    }

    public static boolean hasTag(ItemStack itemStack) {
        return itemStack.s();
    }

    public static NBTTagCompound getTag(ItemStack itemStack) {
        return itemStack.t();
    }

    public static int getMaxLocalRawBrightness(IWorldReader worldReader, BlockPosition position) {
        return worldReader.B(position);
    }

    public static void addFreshEntity(IWorldWriter worldWriter, Entity entity) {
        worldWriter.b(entity);
    }

    public static boolean addFreshEntity(IWorldWriter worldWriter, Entity entity, CreatureSpawnEvent.SpawnReason spawnReason) {
        return worldWriter.addFreshEntity(entity, spawnReason);
    }

    public static String getPath(MinecraftKey minecraftKey) {
        return minecraftKey.a();
    }

    public static void setEntityId(MobSpawnerAbstract mobSpawnerAbstract, EntityTypes<?> entityTypes) {
        mobSpawnerAbstract.a(entityTypes);
    }

    public static NBTTagCompound getEntityToSpawn(MobSpawnerData mobSpawnerData) {
        return mobSpawnerData.a();
    }

    public static boolean contains(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.e(key);
    }

    public static boolean contains(NBTTagCompound nbtTagCompound, String key, int type) {
        return nbtTagCompound.b(key, type);
    }

    public static NBTTagCompound getCompound(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.p(key);
    }

    public static void putString(NBTTagCompound nbtTagCompound, String key, String value) {
        nbtTagCompound.a(key, value);
    }

    public static void put(NBTTagCompound nbtTagCompound, String key, NBTBase nbtBase) {
        nbtTagCompound.a(key, nbtBase);
    }

    public static void putFloat(NBTTagCompound nbtTagCompound, String key, float value) {
        nbtTagCompound.a(key, value);
    }

    public static void remove(NBTTagCompound nbtTagCompound, String key) {
        nbtTagCompound.r(key);
    }

    public static boolean getBoolean(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.q(key);
    }

    public static int getInt(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.h(key);
    }

    public static String getString(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.l(key);
    }

    public static double getDouble(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.k(key);
    }

    public static short getShort(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.g(key);
    }

    public static byte getByte(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.f(key);
    }

    public static float getFloat(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.j(key);
    }

    public static long getLong(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.i(key);
    }

    public static void setSaddle(SaddleStorage saddleStorage, boolean hasStorage) {
        saddleStorage.a(hasStorage);
    }

    public static boolean is(TagKey<Block> tag, Block block) {
        return block.n().a(tag);
    }

    public static boolean is(TagKey<Item> tag, Item item) {
        return item.k().a(tag);
    }

    public static World getWorld(TileEntity tileEntity) {
        return tileEntity.k();
    }

    public static BlockPosition getBlockPos(TileEntity tileEntity) {
        return tileEntity.p();
    }

    public static MobSpawnerAbstract getSpawner(TileEntityMobSpawner tileEntityMobSpawner) {
        return tileEntityMobSpawner.d();
    }

    public static GameRules getGameRules(World world) {
        return world.W();
    }

    public static Random getRandom(World world) {
        return world.r_();
    }

    public static LevelEntityGetter<Entity> getEntities(World world) {
        return world.H();
    }

    public static void broadcastEntityEvent(World world, Entity entity, byte status) {
        world.a(entity, status);
    }

    public static TileEntity getBlockEntity(World world, BlockPosition blockPosition) {
        return world.c_(blockPosition);
    }

    public static IBlockData getBlockState(World world, BlockPosition blockPosition) {
        return world.a_(blockPosition);
    }

    public static int getSeaLevel(World world) {
        return world.m_();
    }

    public static Fluid getFluidState(World world, BlockPosition blockPosition) {
        return world.b_(blockPosition);
    }

    public static DifficultyDamageScaler getCurrentDifficultyAt(World world, BlockPosition blockPosition) {
        return world.d_(blockPosition);
    }

    public static ChunkProviderServer getChunkSource(WorldServer worldServer) {
        return worldServer.k();
    }

}
