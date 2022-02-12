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
import net.minecraft.tags.Tag;
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
import net.minecraft.world.level.IEntityAccess;
import net.minecraft.world.level.MobSpawnerAbstract;
import net.minecraft.world.level.MobSpawnerData;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IFluidContainer;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.event.entity.CreatureSpawnEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class NMSMappings_v1_18_R1 {

    private NMSMappings_v1_18_R1() {

    }

    public static void addEntity(World world, Entity entity) {
        world.b(entity);
    }

    public static boolean addEntity(World world, Entity entity, CreatureSpawnEvent.SpawnReason spawnReason) {
        return world.addFreshEntity(entity, spawnReason);
    }

    public static World getWorld(Entity entity) {
        return entity.t;
    }

    public static World getWorld(TileEntity tileEntity) {
        return tileEntity.k();
    }

    public static VillagerData getVillagerData(EntityVillager entityVillager) {
        return entityVillager.fJ();
    }

    public static void setVillagerData(EntityZombieVillager entityVillager, VillagerData villagerData) {
        entityVillager.a(villagerData);
    }

    public static Reputation getReputation(EntityVillager entityVillager) {
        return entityVillager.fV();
    }

    public static void setOffers(EntityZombieVillager entityZombieVillager, NBTTagCompound offerTag) {
        entityZombieVillager.c(offerTag);
    }

    public static MerchantRecipeList getOffers(EntityVillager entityVillager) {
        return entityVillager.fA();
    }

    public static int getExperience(EntityVillager entityVillager) {
        return entityVillager.t();
    }

    public static void setExperience(EntityZombieVillager entityZombieVillager, int experience) {
        entityZombieVillager.a(experience);
    }

    public static boolean isBaby(EntityAgeable entityAgeable) {
        return entityAgeable.y_();
    }

    public static void setBaby(EntityZombie entityZombie, boolean isBaby) {
        entityZombie.a(isBaby);
    }

    public static boolean isNoAI(EntityInsentient entityInsentient) {
        return entityInsentient.fr();
    }

    public static void setNoAI(EntityInsentient entityInsentient, boolean isNoAI) {
        entityInsentient.s(isNoAI);
    }

    public static boolean hasCustomName(INamableTileEntity namableTileEntity) {
        return namableTileEntity.Y();
    }

    public static IChatBaseComponent getCustomName(INamableTileEntity namableTileEntity) {
        return namableTileEntity.Z();
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

    public static void sendWorldEvent(GeneratorAccess generatorAccess, EntityHuman entityHuman, int i,
                                      BlockPosition blockPosition, int j) {
        generatorAccess.a(entityHuman, i, blockPosition, j);
    }

    public static Vec3D getPositionVector(Entity entity) {
        return entity.ac();
    }

    public static double locX(Entity entity) {
        return getPositionVector(entity).b;
    }

    public static double locY(Entity entity) {
        return getPositionVector(entity).c;
    }

    public static double locZ(Entity entity) {
        return getPositionVector(entity).d;
    }

    public static void setInLove(EntityAnimal entityAnimal, EntityPlayer entityPlayer) {
        entityAnimal.g(entityPlayer);
    }

    public static void resetLove(EntityAnimal entityAnimal) {
        entityAnimal.fF();
    }

    public static boolean isInLove(EntityAnimal entityAnimal) {
        return entityAnimal.fE();
    }

    public static boolean isBreedItem(EntityAnimal entityAnimal, ItemStack itemStack) {
        return entityAnimal.n(itemStack);
    }

    public static GameRules getGameRules(World world) {
        return world.X();
    }

    public static void setHealth(EntityLiving entityLiving, float health) {
        entityLiving.c(health);
    }

    public static EntityTypes<?> getEntityType(Entity entity) {
        return entity.ad();
    }

    public static Random getRandom(World world) {
        return world.r_();
    }

    public static LevelEntityGetter<Entity> getEntities(World world) {
        return world.I();
    }

    public static boolean hasSaddle(EntityStrider entityStrider) {
        return entityStrider.d();
    }

    public static SaddleStorage getSaddleStorage(EntityStrider entityStrider) {
        return entityStrider.cf;
    }

    public static void setSaddle(SaddleStorage saddleStorage, boolean hasStorage) {
        saddleStorage.a(hasStorage);
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

    public static Item getItem(ItemStack itemStack) {
        return itemStack.c();
    }

    public static AxisAlignedBB getBoundingBox(Entity entity) {
        return entity.cw();
    }

    public static AxisAlignedBB grow(AxisAlignedBB boundingBox, double modX, double modY, double modZ) {
        return boundingBox.c(modX, modY, modZ);
    }

    public static <T extends Entity> List<T> getEntities(IEntityAccess entityAccess, Class<T> entityClass,
                                                         AxisAlignedBB boundingBox) {
        return entityAccess.a(entityClass, boundingBox);
    }

    public static DamageSource playerAttack(EntityHuman entityHuman) {
        return DamageSource.a(entityHuman);
    }

    public static void damageEntity(EntityLiving entityLiving, DamageSource damageSource, float damage) {
        entityLiving.a(damageSource, damage);
    }

    public static boolean isMarker(EntityArmorStand entityArmorStand) {
        return entityArmorStand.t();
    }

    public static boolean isEmpty(ItemStack itemStack) {
        return itemStack.b();
    }

    public static void subtract(ItemStack itemStack, int amount) {
        itemStack.g(amount);
    }

    public static void broadcastEntityEffect(World world, Entity entity, byte status) {
        world.a(entity, status);
    }

    public static TileEntity getTileEntity(World world, BlockPosition blockPosition) {
        return world.c_(blockPosition);
    }

    public static IBlockData getType(World world, BlockPosition blockPosition) {
        return world.a_(blockPosition);
    }

    public static Block getBlock(IBlockData blockData) {
        return blockData.b();
    }

    public static NBTTagCompound getOrCreateTag(ItemStack itemStack) {
        return itemStack.t();
    }

    public static boolean hasKey(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.e(key);
    }

    public static boolean hasKeyOfType(NBTTagCompound nbtTagCompound, String key, int type) {
        return nbtTagCompound.b(key, type);
    }

    public static NBTTagCompound getCompound(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.p(key);
    }

    public static void setString(NBTTagCompound nbtTagCompound, String key, String value) {
        nbtTagCompound.a(key, value);
    }

    public static void set(NBTTagCompound nbtTagCompound, String key, NBTBase nbtBase) {
        nbtTagCompound.a(key, nbtBase);
    }

    public static void setTag(ItemStack itemStack, NBTTagCompound nbtTagCompound) {
        itemStack.c(nbtTagCompound);
    }

    public static ChunkProviderServer getChunkProvider(WorldServer worldServer) {
        return worldServer.k();
    }

    public static int getId(Entity entity) {
        return entity.ae();
    }

    public static void broadcast(ChunkProviderServer chunkProviderServer, Entity entity, Packet<?> packet) {
        chunkProviderServer.b(entity, packet);
    }

    public static DataWatcher getDataWatcher(Entity entity) {
        return entity.ai();
    }

    public static void playSound(Entity entity, SoundEffect soundEffect, float volume, float pitch) {
        entity.a(soundEffect, volume, pitch);
    }

    public static void doSpawnEffect(EntityInsentient entityInsentient) {
        entityInsentient.L();
    }

    public static void place(IFluidContainer fluidContainer, GeneratorAccess generatorAccess,
                             BlockPosition blockPosition, IBlockData blockData, Fluid fluid) {
        fluidContainer.a(generatorAccess, blockPosition, blockData, fluid);
    }

    public static BehaviorController<EntityPiglin> getBehaviorController(EntityPiglin entityPiglin) {
        return entityPiglin.dt();
    }

    public static void removeMemory(BehaviorController<?> behaviorController, MemoryModuleType<?> memoryModuleType) {
        behaviorController.b(memoryModuleType);
    }

    public static NavigationAbstract getNavigation(EntityInsentient entityInsentient) {
        return entityInsentient.D();
    }

    public static void receive(EntityLiving entityLiving, Entity entity, int amount) {
        entityLiving.a(entity, amount);
    }

    public static ItemStack getItemStack(EntityItem entityItem) {
        return entityItem.h();
    }

    public static ItemStack cloneItemStack(ItemStack itemStack) {
        return itemStack.m();
    }

    public static void setCount(ItemStack itemStack, int count) {
        itemStack.e(count);
    }

    public static <T> boolean isTagged(Tag.e<T> tag, T value) {
        return tag.a(value);
    }

    public static ItemStack getItemInOffHand(EntityLiving entityLiving) {
        return entityLiving.es();
    }

    public static void setSlot(EntityLiving entityLiving, EnumItemSlot itemSlot, ItemStack itemStack) {
        entityLiving.a(itemSlot, itemStack);
    }

    public static void setPersistent(EntityInsentient entityInsentient) {
        entityInsentient.fg();
    }

    public static boolean hasMemory(BehaviorController<?> behaviorController, MemoryModuleType<?> memoryModuleType) {
        return behaviorController.a(memoryModuleType);
    }

    public static EnumItemSlot getEquipmentSlotForItem(ItemStack itemStack) {
        return EntityLiving.i(itemStack);
    }

    public static ItemStack getEquipment(EntityLiving entityLiving, EnumItemSlot enumItemSlot) {
        return entityLiving.b(enumItemSlot);
    }

    public static boolean usesDurability(Item item) {
        return item.n();
    }

    public static void setPositionRaw(Entity entity, double x, double y, double z) {
        entity.o(x, y, z);
    }

    public static int getDamage(ItemStack itemStack) {
        return itemStack.h();
    }

    public static void die(Entity entity) {
        entity.ah();
    }

    public static void setDamage(ItemStack itemStack, int damage) {
        itemStack.b(damage);
    }

    public static void saveData(EntityLiving entityLiving, NBTTagCompound nbtTagCompound) {
        entityLiving.b(nbtTagCompound);
    }

    public static float getMaxHealth(EntityLiving entityLiving) {
        return entityLiving.el();
    }

    public static void setFloat(NBTTagCompound nbtTagCompound, String key, float value) {
        nbtTagCompound.a(key, value);
    }

    public static void remove(NBTTagCompound nbtTagCompound, String key) {
        nbtTagCompound.r(key);
    }

    public static boolean getBoolean(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.q(key);
    }

    public static void loadData(EntityLiving entityLiving, NBTTagCompound nbtTagCompound) {
        entityLiving.a(nbtTagCompound);
    }

    public static void save(ItemStack itemStack, NBTTagCompound nbtTagCompound) {
        itemStack.b(nbtTagCompound);
    }

    public static boolean hasTag(ItemStack itemStack) {
        return itemStack.r();
    }

    public static NBTTagCompound getTag(ItemStack itemStack) {
        return itemStack.s();
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

    public static Set<String> getScoreboardTags(Entity entity) {
        return entity.af();
    }

    public static BlockPosition getPosition(TileEntity tileEntity) {
        return tileEntity.p();
    }

    public static String getKey(MinecraftKey minecraftKey) {
        return minecraftKey.a();
    }

    public static MobSpawnerAbstract getSpawner(TileEntityMobSpawner tileEntityMobSpawner) {
        return tileEntityMobSpawner.d();
    }

    public static void setMobName(MobSpawnerAbstract mobSpawnerAbstract, EntityTypes<?> entityTypes) {
        mobSpawnerAbstract.a(entityTypes);
    }

    public static NBTTagCompound getEntity(MobSpawnerData mobSpawnerData) {
        return mobSpawnerData.a();
    }

    public static int getLightLevel(World world, BlockPosition position, int ambientDarkness) {
        return world.b(position, ambientDarkness);
    }

    public static int getLightLevel(World world, BlockPosition position) {
        return world.B(position);
    }

    public static int getSeaLevel(World world) {
        return world.m_();
    }

    public static int getBrightness(World world, EnumSkyBlock skyBlock, BlockPosition position) {
        return world.a(skyBlock, position);
    }

    public static EnumDifficulty getDifficulty(World world) {
        return world.af();
    }

    public static Fluid getFluid(World world, BlockPosition blockPosition) {
        return world.b_(blockPosition);
    }

    public static void triggerEffect(World world, int i, BlockPosition position, int j) {
        world.c(i, position, j);
    }

    public static void setPositionRotation(Entity entity, double x, double y, double z, float yaw, float pitch) {
        entity.b(x, y, z, yaw, pitch);
    }

    public static GroupDataEntity prepare(EntityInsentient entityInsentient, WorldAccess worldAccess,
                                          DifficultyDamageScaler difficulty, EnumMobSpawn spawnReason,
                                          @Nullable GroupDataEntity entityData, @Nullable NBTTagCompound entityNbt) {
        return entityInsentient.a(worldAccess, difficulty, spawnReason, entityData, entityNbt);
    }

    public static BlockPosition getChunkCoordinates(Entity entity) {
        return entity.cW();
    }

    public static DifficultyDamageScaler getDamageScaler(World world, BlockPosition blockPosition) {
        return world.d_(blockPosition);
    }

    public static Entity getVehicle(Entity entity) {
        return entity.cN();
    }

    public static Iterable<Entity> getAllPassengers(Entity entity) {
        return entity.cJ();
    }

    public static List<Entity> getPassengers(Entity entity) {
        return entity.cF();
    }

}
