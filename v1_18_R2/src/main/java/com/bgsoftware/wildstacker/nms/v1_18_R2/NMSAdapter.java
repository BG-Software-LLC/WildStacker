package com.bgsoftware.wildstacker.nms.v1_18_R2;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.nms.mapping.Remap;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.core.BlockPosition;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.nbt.NBTTagCompound;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.resources.MinecraftKey;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.server.level.ChunkProviderServer;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.server.level.MobSpawnerData;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.world.entity.Entity;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.world.entity.SaddleStorage;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.world.entity.ai.BehaviorController;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.world.item.Item;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.world.item.ItemStack;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.world.level.World;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.world.level.block.entity.TileEntity;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.world.level.block.state.IBlockData;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.utils.chunks.ChunkPosition;
import com.bgsoftware.wildstacker.utils.spawners.SpawnerCachedData;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.advancements.critereon.CriterionTriggerUsedTotem;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTReadLimiter;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutCollect;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.stats.StatisticList;
import net.minecraft.stats.StatisticWrapper;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.UtilColor;
import net.minecraft.world.EnumHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityExperienceOrb;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityPositionTypes;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.ai.gossip.Reputation;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryTarget;
import net.minecraft.world.entity.animal.EntityChicken;
import net.minecraft.world.entity.animal.EntityPig;
import net.minecraft.world.entity.animal.EntityTurtle;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.monster.EntityZombieVillager;
import net.minecraft.world.entity.monster.piglin.EntityPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinAI;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.EntityArrow;
import net.minecraft.world.entity.projectile.EntityFireballFireball;
import net.minecraft.world.entity.projectile.EntityThrownTrident;
import net.minecraft.world.entity.raid.EntityRaider;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.ItemSword;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.MobSpawnerAbstract;
import net.minecraft.world.level.block.BlockRotatable;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AxisAlignedBB;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.TurtleEgg;
import org.bukkit.craftbukkit.v1_18_R2.CraftParticle;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_18_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftChicken;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftItem;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPiglin;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftStrider;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftTurtle;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_18_R2.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Strider;
import org.bukkit.entity.Turtle;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings("ConstantConditions")
public final class NMSAdapter implements com.bgsoftware.wildstacker.nms.NMSAdapter {

    private static final ReflectField<Integer> ENTITY_EXP = new ReflectField<>(EntityInsentient.class, int.class, Modifier.PROTECTED, 1);
    private static final ReflectField<net.minecraft.world.entity.Entity.RemovalReason> ENTITY_REMOVE_REASON = new ReflectField<>(net.minecraft.world.entity.Entity.class, net.minecraft.world.entity.Entity.RemovalReason.class, Modifier.PRIVATE, 1);

    @Remap(classPath = "net.minecraft.world.entity.LivingEntity", name = "lastHurtByPlayerTime", type = Remap.Type.FIELD)
    private static final ReflectField<Integer> LAST_DAMAGE_BY_PLAYER_TIME = new ReflectField<>(EntityLiving.class, int.class, "bd");
    @Remap(classPath = "net.minecraft.world.entity.LivingEntity", name = "shouldDropExperience", type = Remap.Type.METHOD)
    private static final ReflectMethod<Boolean> IS_DROP_EXPERIENCE = new ReflectMethod<>(EntityLiving.class, boolean.class, "dI");
    @Remap(classPath = "net.minecraft.world.entity.LivingEntity", name = "getDeathSound", type = Remap.Type.METHOD)
    private static final ReflectMethod<SoundEffect> GET_SOUND_DEATH = new ReflectMethod<>(EntityLiving.class, "x_");
    @Remap(classPath = "net.minecraft.world.entity.LivingEntity", name = "getSoundVolume", type = Remap.Type.METHOD)
    private static final ReflectMethod<Float> GET_SOUND_VOLUME = new ReflectMethod<>(EntityLiving.class, "ev");
    @Remap(classPath = "net.minecraft.world.entity.LivingEntity", name = "getVoicePitch", type = Remap.Type.METHOD)
    private static final ReflectMethod<Float> GET_SOUND_PITCH = new ReflectMethod<>(EntityLiving.class, "ew");
    @Remap(classPath = "net.minecraft.world.entity.animal.Chicken", name = "eggTime", type = Remap.Type.FIELD)
    private static final ReflectField<Integer> CHICKEN_EGG_LAY_TIME = new ReflectField<>(EntityChicken.class, Integer.class, "cb");
    @Remap(classPath = "net.minecraft.world.entity.raid.Raider", name = "hasActiveRaid", type = Remap.Type.METHOD)
    private static final ReflectMethod<Boolean> RAIDER_CAN_RAID = new ReflectMethod<>(EntityRaider.class, boolean.class, "fO");
    @Remap(classPath = "net.minecraft.world.entity.raid.Raider", name = "getCurrentRaid", type = Remap.Type.METHOD)
    private static final ReflectMethod<Raid> RAIDER_RAID = new ReflectMethod<>(EntityRaider.class, Raid.class, "fN");
    @Remap(classPath = "net.minecraft.world.entity.animal.Turtle", name = "setHasEgg", type = Remap.Type.METHOD)
    private static final ReflectMethod<Void> TURTLE_SET_HAS_EGG = new ReflectMethod<>(EntityTurtle.class, "v", boolean.class);
    @Remap(classPath = "net.minecraft.world.entity.animal.Turtle", name = "getHomePos", type = Remap.Type.METHOD)
    private static final ReflectMethod<net.minecraft.core.BlockPosition> TURTLE_HOME_POS = new ReflectMethod<>(EntityTurtle.class, "fA");

    @Remap(classPath = "net.minecraft.world.entity.EntityType", name = "ZOMBIE_VILLAGER", type = Remap.Type.FIELD, remappedName = "bg")
    private static final EntityTypes<EntityZombieVillager> ENTITY_ZOMBIE_VILLAGER_TYPE = EntityTypes.bg;
    @Remap(classPath = "net.minecraft.world.entity.EntityType", name = "EXPERIENCE_ORB", type = Remap.Type.FIELD, remappedName = "A")
    private static final EntityTypes<EntityExperienceOrb> EXPERIENCE_ORB_TYPE = EntityTypes.A;
    @Remap(classPath = "net.minecraft.world.entity.EntityType", name = "PIG", type = Remap.Type.FIELD, remappedName = "an")
    private static final EntityTypes<EntityPig> PIG_TYPE = EntityTypes.an;
    @Remap(classPath = "net.minecraft.world.item.Items", name = "TOTEM_OF_UNDYING", type = Remap.Type.FIELD, remappedName = "sw")
    private static final net.minecraft.world.item.Item TOTEM_OF_UNDYING_ITEM = Items.sw;
    @Remap(classPath = "net.minecraft.world.item.Items", name = "PORKCHOP", type = Remap.Type.FIELD, remappedName = "nJ")
    private static final net.minecraft.world.item.Item PORKCHOP = Items.nJ;
    @Remap(classPath = "net.minecraft.world.item.Items", name = "COOKED_PORKCHOP", type = Remap.Type.FIELD, remappedName = "nK")
    private static final net.minecraft.world.item.Item COOKED_PORKCHOP = Items.nK;
    @Remap(classPath = "net.minecraft.stats.Stats", name = "ITEM_USED", type = Remap.Type.FIELD, remappedName = "c")
    private static final StatisticWrapper<net.minecraft.world.item.Item> ITEM_USED_STATISTIC = StatisticList.c;
    @Remap(classPath = "net.minecraft.advancements.CriteriaTriggers", name = "USED_TOTEM", type = Remap.Type.FIELD, remappedName = "B")
    private static final CriterionTriggerUsedTotem USED_TOTEM_TRIGGER = CriterionTriggers.B;
    @Remap(classPath = "net.minecraft.world.effect.MobEffects", name = "REGENERATION", type = Remap.Type.FIELD, remappedName = "j")
    private static final MobEffectList REGENERATION_EFFECT = MobEffects.j;
    @Remap(classPath = "net.minecraft.world.effect.MobEffects", name = "ABSORPTION", type = Remap.Type.FIELD, remappedName = "v")
    private static final MobEffectList ABSORPTION_EFFECT = MobEffects.v;
    @Remap(classPath = "net.minecraft.world.effect.MobEffects", name = "FIRE_RESISTANCE", type = Remap.Type.FIELD, remappedName = "l")
    private static final MobEffectList FIRE_RESISTANCE_EFFECT = MobEffects.l;
    @Remap(classPath = "net.minecraft.world.entity.ai.memory.MemoryModuleType", name = "WALK_TARGET", type = Remap.Type.FIELD, remappedName = "m")
    private static final MemoryModuleType<MemoryTarget> WALK_TARGET_MEMORY_TYPE = MemoryModuleType.m;
    @Remap(classPath = "net.minecraft.world.entity.ai.memory.MemoryModuleType", name = "TIME_TRYING_TO_REACH_ADMIRE_ITEM", type = Remap.Type.FIELD, remappedName = "Y")
    private static final MemoryModuleType<Integer> TIME_TRYING_TO_REACH_ADMIRE_ITEM_MEMORY_TYPE = MemoryModuleType.Y;
    @Remap(classPath = "net.minecraft.world.entity.ai.memory.MemoryModuleType", name = "ADMIRING_ITEM", type = Remap.Type.FIELD, remappedName = "X")
    private static final MemoryModuleType<Boolean> ADMIRING_ITEM_MEMORY_TYPE = MemoryModuleType.X;
    @Remap(classPath = "net.minecraft.world.entity.ai.memory.MemoryModuleType", name = "ATE_RECENTLY", type = Remap.Type.FIELD, remappedName = "ap")
    private static final MemoryModuleType<Boolean> ATE_RECENTLY_MEMORY_TYPE = MemoryModuleType.ap;
    @Remap(classPath = "net.minecraft.tags.ItemTags", name = "PIGLIN_LOVED", type = Remap.Type.FIELD, remappedName = "N")
    private static final TagKey<net.minecraft.world.item.Item> PIGLIN_LOVED_TAG = TagsItem.N;
    @Remap(classPath = "net.minecraft.world.level.gameevent.GameEvent", name = "EQUIP", type = Remap.Type.FIELD, remappedName = "u")
    private static final GameEvent EQUIP_GAME_EVENT = GameEvent.u;
    @Remap(classPath = "net.minecraft.world.item.enchantment.Enchantments", name = "MENDING", type = Remap.Type.FIELD, remappedName = "K")
    private static final net.minecraft.world.item.enchantment.Enchantment MENDING_ENCHANTMENT = Enchantments.K;
    @Remap(classPath = "net.minecraft.world.level.GameRules", name = "RULE_DOMOBLOOT", type = Remap.Type.FIELD, remappedName = "f")
    private static final GameRules.GameRuleKey<GameRules.GameRuleBoolean> RULE_DOMOBLOOT = GameRules.f;
    @Remap(classPath = "net.minecraft.world.entity.MobSpawnType", name = "SPAWNER", type = Remap.Type.FIELD, remappedName = "c")
    private static final EnumMobSpawn SPAWNER_MOB_SPAWN = EnumMobSpawn.c;

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    /*
     *   Entity methods
     */
    private static final NamespacedKey
            STACK_AMOUNT = new NamespacedKey(plugin, "stackAmount"),
            SPAWN_CAUSE = new NamespacedKey(plugin, "spawnCause"),
            NAME_TAG = new NamespacedKey(plugin, "nameTag"),
            UPGRADE = new NamespacedKey(plugin, "upgrade");

    @Override
    public String getMappingsHash() {
        return ((CraftMagicNumbers) CraftMagicNumbers.INSTANCE).getMappingsVersion();
    }

    @Override
    public <T extends org.bukkit.entity.Entity> T createEntity(Location location, Class<T> type,
                                                               SpawnCause spawnCause,
                                                               Consumer<T> beforeSpawnConsumer,
                                                               Consumer<T> afterSpawnConsumer) {
        CraftWorld world = (CraftWorld) location.getWorld();

        assert world != null;

        net.minecraft.world.entity.Entity nmsEntity = world.createEntity(location, type);
        org.bukkit.entity.Entity bukkitEntity = nmsEntity.getBukkitEntity();

        if (beforeSpawnConsumer != null) {
            //noinspection unchecked
            beforeSpawnConsumer.accept((T) bukkitEntity);
        }

        world.addEntity(nmsEntity, spawnCause.toSpawnReason());

        WStackedEntity.of(bukkitEntity).setSpawnCause(spawnCause);

        if (afterSpawnConsumer != null) {
            //noinspection unchecked
            afterSpawnConsumer.accept((T) bukkitEntity);
        }

        return type.cast(bukkitEntity);
    }

    @Override
    public <T extends org.bukkit.entity.Entity> T spawnEntity(Location location, Class<T> type, SpawnCause spawnCause) {
        CraftWorld world = (CraftWorld) location.getWorld();

        assert world != null;

        net.minecraft.world.entity.Entity nmsEntity = world.createEntity(location, type);
        org.bukkit.entity.Entity bukkitEntity = nmsEntity.getBukkitEntity();

        world.addEntity(nmsEntity, spawnCause.toSpawnReason());

        return type.cast(bukkitEntity);
    }

    @Override
    public Zombie spawnZombieVillager(Villager villager) {
        World world = new World(((CraftWorld) villager.getWorld()).getHandle());

        Entity entityVillager = new Entity(((CraftVillager) villager).getHandle());
        Entity entityZombieVillager = new Entity(ENTITY_ZOMBIE_VILLAGER_TYPE.a(world.getHandle()));

        assert entityZombieVillager != null;
        entityZombieVillager.copyPosition(entityVillager.getHandle());

        entityZombieVillager.setVillagerData(entityVillager.getVillagerData());

        Reputation villagerReputation = entityVillager.getGossips();

        entityZombieVillager.setGossips(villagerReputation.a(DynamicOpsNBT.a).getValue());
        entityZombieVillager.setTradeOffers(entityVillager.getOffers().a());
        entityZombieVillager.setVillagerXp(((CraftVillager) entityVillager.getBukkitEntity()).getVillagerExperience());
        entityZombieVillager.setBaby(entityVillager.isBaby());
        entityZombieVillager.setNoAI(entityVillager.isNoAI());

        if (entityVillager.hasCustomName()) {
            entityZombieVillager.setCustomName(entityVillager.getCustomName());
            entityZombieVillager.setCustomNameVisible(entityVillager.getCustomNameVisible());
        }

        EntityTransformEvent entityTransformEvent = new EntityTransformEvent(entityVillager.getBukkitEntity(),
                Collections.singletonList(entityZombieVillager.getBukkitEntity()),
                EntityTransformEvent.TransformReason.INFECTION);
        Bukkit.getPluginManager().callEvent(entityTransformEvent);

        if (entityTransformEvent.isCancelled())
            return null;

        world.addFreshEntity(entityZombieVillager.getHandle(), CreatureSpawnEvent.SpawnReason.INFECTION);
        BlockPosition blockPosition = new BlockPosition(entityVillager.getX(), entityVillager.getY(), entityVillager.getZ());
        world.levelEvent(null, 1026, blockPosition.getHandle(), 0);

        return (Zombie) entityZombieVillager.getBukkitEntity();
    }

    @Override
    public void setInLove(Animals entity, Player breeder, boolean inLove) {
        Entity nmsEntity = new Entity(((CraftAnimals) entity).getHandle());
        EntityPlayer entityPlayer = ((CraftPlayer) breeder).getHandle();
        if (inLove) {
            nmsEntity.setInLove(entityPlayer);
        } else {
            nmsEntity.resetLove();
        }
    }

    @Override
    public boolean isInLove(Animals entity) {
        return new Entity(((CraftEntity) entity).getHandle()).isInLove();
    }

    @Override
    public boolean isAnimalFood(Animals animal, org.bukkit.inventory.ItemStack itemStack) {
        if (itemStack == null)
            return false;

        Entity nmsEntity = new Entity(((CraftAnimals) animal).getHandle());
        return nmsEntity.isFood(CraftItemStack.asNMSCopy(itemStack));
    }

    @Override
    public int getEntityExp(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();

        if (!(entityLiving instanceof EntityInsentient entityInsentient))
            return 0;

        int defaultEntityExp = ENTITY_EXP.get(entityInsentient);

        for (int i = 0; i < 5; ++i) {
            try {
                int exp = entityInsentient.getExpReward();
                ENTITY_EXP.set(entityInsentient, defaultEntityExp);
                return exp;
            } catch (Exception ignored) {
            }
        }

        return 0;
    }

    @Remap(classPath = "net.minecraft.world.level.GameRules",
            name = "getBoolean",
            type = Remap.Type.METHOD,
            remappedName = "b")
    @Override
    public boolean canDropExp(LivingEntity livingEntity) {
        Entity entityLiving = new Entity(((CraftLivingEntity) livingEntity).getHandle());
        int lastDamageByPlayerTime = LAST_DAMAGE_BY_PLAYER_TIME.get(entityLiving.getHandle());
        boolean isDropExperience = IS_DROP_EXPERIENCE.invoke(entityLiving.getHandle());
        return lastDamageByPlayerTime > 0 && isDropExperience && entityLiving.getWorld().getGameRules().b(RULE_DOMOBLOOT);
    }

    @Override
    public void updateLastDamageTime(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        LAST_DAMAGE_BY_PLAYER_TIME.set(entityLiving, 100);
    }

    @Override
    public void setHealthDirectly(LivingEntity livingEntity, double health) {
        Entity entityLiving = new Entity(((CraftLivingEntity) livingEntity).getHandle());
        entityLiving.setHealth((float) health);
    }

    @Override
    public void setEntityDead(LivingEntity livingEntity, boolean dead) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        ENTITY_REMOVE_REASON.set(entityLiving, dead ? net.minecraft.world.entity.Entity.RemovalReason.b : null);
    }

    @Override
    public int getEggLayTime(Chicken chicken) {
        EntityChicken entityChicken = ((CraftChicken) chicken).getHandle();
        return CHICKEN_EGG_LAY_TIME.get(entityChicken);
    }

    @Override
    public void setNerfedEntity(LivingEntity livingEntity, boolean nerfed) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();

        if (!(entityLiving instanceof EntityInsentient entityInsentient))
            return;

        entityInsentient.aware = !nerfed;

        try {
            entityInsentient.spawnedViaMobSpawner = nerfed;
        } catch (Throwable ignored) {
        }
    }

    @Remap(classPath = "net.minecraft.world.entity.LivingEntity",
            name = "lastHurtByPlayer",
            type = Remap.Type.FIELD,
            remappedName = "bc")
    @Override
    public void setKiller(LivingEntity livingEntity, Player killer) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        entityLiving.bc = killer == null ? null : ((CraftPlayer) killer).getHandle();
    }

    @Override
    public boolean canSpawnOn(org.bukkit.entity.Entity bukkitEntity, Location location) {
        assert location.getWorld() != null;
        World world = new World(((CraftWorld) location.getWorld()).getHandle());
        Entity entity = new Entity(((CraftEntity) bukkitEntity).getHandle());
        EntityTypes<?> entityTypes = entity.getType();
        return EntityPositionTypes.a(entityTypes, (WorldServer) world.getHandle(), SPAWNER_MOB_SPAWN,
                new BlockPosition(location.getX(), location.getY(), location.getZ()).getHandle(),
                world.getRandom());
    }

    @Override
    public Collection<org.bukkit.entity.Entity> getEntitiesAtChunk(ChunkPosition chunkPosition) {
        return new ArrayList<>();
    }

    @Override
    public Collection<org.bukkit.entity.Entity> getNearbyEntities(Location location, int range, Predicate<org.bukkit.entity.Entity> filter) {
        World world = new World(((CraftWorld) location.getWorld()).getHandle());
        List<org.bukkit.entity.Entity> entities = new ArrayList<>();

        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(
                location.getBlockX() - range,
                location.getBlockY() - range,
                location.getBlockZ() - range,
                location.getBlockX() + range,
                location.getBlockY() + range,
                location.getBlockZ() + range
        );

        world.getEntities().a(axisAlignedBB, entity -> {
            if (filter == null || filter.test(entity.getBukkitEntity()))
                entities.add(entity.getBukkitEntity());
        });

        return entities;
    }

    @Override
    public float getItemInMainHandDropChance(EntityEquipment entityEquipment) {
        return entityEquipment.getItemInMainHandDropChance();
    }

    @Override
    public float getItemInOffHandDropChance(EntityEquipment entityEquipment) {
        return entityEquipment.getItemInOffHandDropChance();
    }

    @Override
    public void setItemInMainHand(EntityEquipment entityEquipment, org.bukkit.inventory.ItemStack itemStack) {
        entityEquipment.setItemInMainHand(itemStack);
    }

    @Override
    public void setItemInOffHand(EntityEquipment entityEquipment, org.bukkit.inventory.ItemStack itemStack) {
        entityEquipment.setItemInOffHand(itemStack);
    }

    @Override
    public org.bukkit.inventory.ItemStack getItemInOffHand(EntityEquipment entityEquipment) {
        return entityEquipment.getItemInOffHand();
    }

    @Override
    public boolean shouldArmorBeDamaged(org.bukkit.inventory.ItemStack itemStack) {
        ItemStack nmsItem = new ItemStack(CraftItemStack.asNMSCopy(itemStack));
        return nmsItem != null && nmsItem.isDamageableItem();
    }

    @Override
    public boolean doesStriderHaveSaddle(Strider bukkitStrider) {
        Entity strider = new Entity(((CraftStrider) bukkitStrider).getHandle());
        return strider.isSaddled();
    }

    @Override
    public void removeStriderSaddle(Strider bukkitStrider) {
        try {
            bukkitStrider.setSaddle(false);
        } catch (Throwable ex) {
            Entity strider = new Entity(((CraftStrider) bukkitStrider).getHandle());
            SaddleStorage saddleStorage = strider.getSaddleStorage();
            saddleStorage.setSaddle(false);
        }
    }

    @Override
    public String getEndermanCarried(Enderman enderman) {
        BlockData carriedData = enderman.getCarriedBlock();
        return carriedData == null ? "AIR" : carriedData.getMaterial() + "";
    }

    @Override
    public byte getMooshroomType(MushroomCow mushroomCow) {
        return (byte) mushroomCow.getVariant().ordinal();
    }

    @Override
    public void setTurtleEgg(org.bukkit.entity.Entity turtle) {
        try {
            ((Turtle) turtle).setHasEgg(true);
        } catch (Throwable ex) {
            EntityTurtle entityTurtle = ((CraftTurtle) turtle).getHandle();
            TURTLE_SET_HAS_EGG.invoke(entityTurtle, true);
        }
    }

    @Override
    public Location getTurtleHome(org.bukkit.entity.Entity turtle) {
        try {
            return ((Turtle) turtle).getHome();
        } catch (Throwable ex) {
            Entity entityTurtle = new Entity(((CraftTurtle) turtle).getHandle());
            BlockPosition homePosition = new BlockPosition(TURTLE_HOME_POS.invoke(entityTurtle.getHandle()));
            return new Location(entityTurtle.getWorld().getHandle().getWorld(),
                    homePosition.getX(), homePosition.getY(), homePosition.getZ());
        }
    }

    @Override
    public void setTurtleEggsAmount(Block turtleEggBlock, int amount) {
        TurtleEgg turtleEgg = (TurtleEgg) turtleEggBlock.getBlockData();
        turtleEgg.setEggs(amount);
        turtleEggBlock.setBlockData(turtleEgg, true);
    }

    @Override
    public void handleSweepingEdge(Player attacker, org.bukkit.inventory.ItemStack usedItem, LivingEntity target, double damage) {
        Entity targetLiving = new Entity(((CraftLivingEntity) target).getHandle());
        EntityHuman entityHuman = ((CraftPlayer) attacker).getHandle();
        ItemStack itemStack = new ItemStack(CraftItemStack.asNMSCopy(usedItem));

        // Making sure the player used a sword.
        if (!(itemStack.getItem().getHandle() instanceof ItemSword)) {
            return;
        }

        float sweepDamage = 1.0F + EnchantmentManager.a(entityHuman) * (float) damage;
        List<EntityLiving> nearbyEntities = targetLiving.getWorld().getEntitiesOfClass(EntityLiving.class,
                targetLiving.getBoundingBox().inflate(1.0D, 0.25D, 1.0D));

        for (EntityLiving nearby : nearbyEntities) {
            if (nearby != targetLiving.getHandle() && nearby != entityHuman && !entityHuman.r(nearby)) {
                Entity nearbyMapped = new Entity(nearby);
                if ((!(nearby instanceof EntityArmorStand) || !(nearbyMapped = new Entity(nearby)).isMarker() &&
                        entityHuman.f(nearby) < 9.0D))
                    nearbyMapped.hurt(Entity.playerAttack(entityHuman).sweep(), sweepDamage);

            }
        }
    }

    @Override
    public String getCustomName(org.bukkit.entity.Entity bukkitEntity) {
        Entity entity = new Entity(((CraftEntity) bukkitEntity).getHandle());
        // Much more optimized way than Bukkit's method.
        IChatBaseComponent chatBaseComponent = entity.getCustomName();
        return chatBaseComponent == null ? "" : chatBaseComponent.getString();
    }

    @Override
    public void setCustomName(org.bukkit.entity.Entity entity, String name) {
        entity.setCustomName(name);
    }

    /*
     *   Spawner methods
     */

    @Override
    public boolean handleTotemOfUndying(LivingEntity livingEntity) {
        Entity entityLiving = new Entity(((CraftLivingEntity) livingEntity).getHandle());

        ItemStack totemOfUndying = ItemStack.AIR;

        for (EnumHand enumHand : EnumHand.values()) {
            ItemStack handItem = new ItemStack(entityLiving.getItemInHand(enumHand));
            if (handItem.getItem().getHandle() == TOTEM_OF_UNDYING_ITEM) {
                totemOfUndying = handItem;
                break;
            }
        }

        EntityResurrectEvent event = new EntityResurrectEvent(livingEntity);
        event.setCancelled(totemOfUndying.isEmpty());
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return false;

        if (!totemOfUndying.isEmpty()) {
            totemOfUndying.shrink(1);

            if (entityLiving.getHandle() instanceof EntityPlayer entityPlayer) {
                entityPlayer.b(ITEM_USED_STATISTIC.b(TOTEM_OF_UNDYING_ITEM));
                USED_TOTEM_TRIGGER.a(entityPlayer, totemOfUndying.getHandle());
            }

            entityLiving.setHealth(1.0F);
            entityLiving.removeAllEffects(EntityPotionEffectEvent.Cause.TOTEM);
            entityLiving.addEffect(new MobEffect(REGENERATION_EFFECT, 900, 1), EntityPotionEffectEvent.Cause.TOTEM);
            entityLiving.addEffect(new MobEffect(ABSORPTION_EFFECT, 100, 1), EntityPotionEffectEvent.Cause.TOTEM);
            entityLiving.addEffect(new MobEffect(FIRE_RESISTANCE_EFFECT, 800, 0), EntityPotionEffectEvent.Cause.TOTEM);
            entityLiving.getWorld().broadcastEntityEvent(entityLiving.getHandle(), (byte) 35);
        }

        return true;
    }

    @Override
    public SyncedCreatureSpawner createSyncedSpawner(CreatureSpawner creatureSpawner) {
        org.bukkit.World bukkitWorld = creatureSpawner.getWorld();
        World world = new World(((CraftWorld) bukkitWorld).getHandle());
        BlockPosition blockPosition = new BlockPosition(creatureSpawner.getX(), creatureSpawner.getY(), creatureSpawner.getZ());
        TileEntityMobSpawner tileEntityMobSpawner = (TileEntityMobSpawner) world.getBlockEntity(blockPosition.getHandle()).getHandle();
        return new SyncedCreatureSpawnerImpl(bukkitWorld, tileEntityMobSpawner);
    }

    /*
     *   Item methods
     */

    @Override
    public boolean isRotatable(Block block) {
        World world = new World(((CraftWorld) block.getWorld()).getHandle());
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        IBlockData blockData = world.getBlockState(blockPosition.getHandle());
        return blockData.getBlock() instanceof BlockRotatable;
    }

    @Override
    public StackedItem createItem(Location location, org.bukkit.inventory.ItemStack itemStack, SpawnCause spawnCause, Consumer<StackedItem> itemConsumer) {
        CraftWorld craftWorld = (CraftWorld) location.getWorld();

        EntityItem entityItem = new EntityItem(craftWorld.getHandle(),
                location.getX(), location.getY(), location.getZ(),
                CraftItemStack.asNMSCopy(itemStack));

        Entity mappedEntityItem = new Entity(entityItem);
        mappedEntityItem.setPickupDelay(10);

        try {
            entityItem.canMobPickup = false;
            Executor.sync(() -> entityItem.canMobPickup = true, 20L);
        } catch (Throwable ignored) {
        }

        StackedItem stackedItem = WStackedItem.ofBypass((org.bukkit.entity.Item) entityItem.getBukkitEntity());

        itemConsumer.accept(stackedItem);

        craftWorld.addEntity(entityItem, spawnCause.toSpawnReason());

        return stackedItem;
    }

    @Override
    @SuppressWarnings("all")
    public Enchantment getGlowEnchant() {
        return new Enchantment(NamespacedKey.minecraft("glowing_enchant")) {
            @Override
            public String getName() {
                return "WildStackerGlow";
            }

            @Override
            public int getMaxLevel() {
                return 1;
            }

            @Override
            public int getStartLevel() {
                return 0;
            }

            @Override
            public EnchantmentTarget getItemTarget() {
                return null;
            }

            @Override
            public boolean isTreasure() {
                return false;
            }

            @Override
            public boolean isCursed() {
                return false;
            }

            @Override
            public boolean conflictsWith(Enchantment enchantment) {
                return false;
            }

            @Override
            public boolean canEnchantItem(org.bukkit.inventory.ItemStack itemStack) {
                return true;
            }

            public Component displayName(int i) {
                return null;
            }

            public boolean isTradeable() {
                return false;
            }

            public boolean isDiscoverable() {
                return false;
            }

            public EnchantmentRarity getRarity() {
                return null;
            }

            public float getDamageIncrease(int i, EntityCategory entityCategory) {
                return 0;
            }

            public Set<EquipmentSlot> getActiveSlots() {
                return null;
            }

            public String translationKey() {
                return null;
            }
        };
    }

    /*
     *   World methods
     */

    @Override
    public org.bukkit.inventory.ItemStack getPlayerSkull(org.bukkit.inventory.ItemStack bukkitItem, String texture) {
        ItemStack itemStack = new ItemStack(CraftItemStack.asNMSCopy(bukkitItem));
        NBTTagCompound nbtTagCompound = itemStack.getOrCreateTag();

        NBTTagCompound skullOwner = nbtTagCompound.contains("SkullOwner") ?
                nbtTagCompound.getCompound("SkullOwner") : new NBTTagCompound();

        skullOwner.putString("Id", new UUID(texture.hashCode(), texture.hashCode()).toString());

        NBTTagCompound properties = new NBTTagCompound();

        NBTTagList textures = new NBTTagList();
        NBTTagCompound signature = new NBTTagCompound();
        signature.putString("Value", texture);
        textures.add(signature.getHandle());

        properties.put("textures", textures);

        skullOwner.put("Properties", properties.getHandle());

        nbtTagCompound.put("SkullOwner", skullOwner.getHandle());

        itemStack.setTag(nbtTagCompound.getHandle());

        return CraftItemStack.asBukkitCopy(itemStack.getHandle());
    }

    @Override
    public void awardKillScore(org.bukkit.entity.Entity bukkitDamaged,
                               org.bukkit.entity.Entity damagerEntity) {
        net.minecraft.world.entity.Entity damaged = ((CraftEntity) bukkitDamaged).getHandle();
        net.minecraft.world.entity.Entity damager = ((CraftEntity) damagerEntity).getHandle();

        DamageSource damageSource = null;

        if (damager instanceof EntityHuman entityHuman) {
            damageSource = DamageSource.a(entityHuman);
        } else if (damager instanceof EntityArrow entityArrow) {
            damageSource = DamageSource.a(entityArrow, entityArrow.x());
        } else if (damager instanceof EntityThrownTrident entityThrownTrident) {
            damageSource = DamageSource.a(damager, entityThrownTrident.x());
        } else if (damager instanceof EntityFireballFireball entityFireballFireball) {
            damageSource = DamageSource.a(entityFireballFireball, entityFireballFireball.x());
        }

        if (damageSource == null) {
            if (damager instanceof EntityLiving damagerLiving) {
                damageSource = DamageSource.c(damagerLiving);
            } else {
                return;
            }
        }

        damager.a(damaged, 0, damageSource);
    }

    @Override
    public void playPickupAnimation(LivingEntity livingEntity, org.bukkit.entity.Item item) {
        Entity entityLiving = new Entity(((CraftLivingEntity) livingEntity).getHandle());
        EntityItem entityItem = (EntityItem) ((CraftItem) item).getHandle();
        Entity mappedEntityItem = new Entity(entityItem);
        int entityItemId = entityItem.getBukkitEntity().getEntityId();


        ChunkProviderServer chunkProvider = entityLiving.getWorld().getChunkSource();
        chunkProvider.broadcast(entityItem, new PacketPlayOutCollect(entityItemId,
                entityLiving.getBukkitEntity().getEntityId(), item.getItemStack().getAmount()));
        //Makes sure the entity is still there.
        chunkProvider.broadcast(entityItem, new PacketPlayOutSpawnEntity(entityItem));
        chunkProvider.broadcast(entityItem, new PacketPlayOutEntityMetadata(entityItemId,
                mappedEntityItem.getEntityData(), true));
    }

    @Override
    public void playDeathSound(LivingEntity livingEntity) {
        Entity entityLiving = new Entity(((CraftLivingEntity) livingEntity).getHandle());
        SoundEffect deathSound = GET_SOUND_DEATH.invoke(entityLiving.getHandle());
        if (deathSound != null) {
            float soundVolume = GET_SOUND_VOLUME.invoke(entityLiving.getHandle());
            float soundPitch = GET_SOUND_PITCH.invoke(entityLiving.getHandle());
            entityLiving.playSound(deathSound, soundVolume, soundPitch);
        }
    }

    @Override
    public void playParticle(String particle, Location location, int count, int offsetX, int offsetY, int offsetZ, double extra) {
        assert location.getWorld() != null;
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        world.sendParticles(null, CraftParticle.toNMS(Particle.valueOf(particle)), location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                count, offsetX, offsetY, offsetZ, extra, false);
    }

    @Override
    public void playSpawnEffect(LivingEntity livingEntity) {
        Entity entityLiving = new Entity(((CraftLivingEntity) livingEntity).getHandle());
        if (entityLiving.getHandle() instanceof EntityInsentient)
            entityLiving.spawnAnim();
    }

    @Override
    public Object getBlockData(Material type, short data) {
        return CraftBlockData.fromData(CraftMagicNumbers.getBlock(type, (byte) data));
    }

    @Override
    public void attemptJoinRaid(Player player, org.bukkit.entity.Entity raider) {
        EntityRaider entityRaider = (EntityRaider) ((CraftEntity) raider).getHandle();
        boolean canJoinRaid = RAIDER_CAN_RAID.invoke(entityRaider);
        if (canJoinRaid) {
            Raid villagerRaid = RAIDER_RAID.invoke(entityRaider);
            villagerRaid.a((net.minecraft.world.entity.Entity) ((CraftPlayer) player).getHandle());
        }
    }

    @Override
    public boolean handlePiglinPickup(org.bukkit.entity.Entity bukkitPiglin, org.bukkit.entity.Item bukkitItem) {
        if (!(bukkitPiglin instanceof Piglin))
            return false;

        Entity entityPiglin = new Entity(((CraftPiglin) bukkitPiglin).getHandle());
        Entity entityItem = new Entity(((CraftItem) bukkitItem).getHandle());

        entityPiglin.onItemPickup((EntityItem) entityItem.getHandle());

        BehaviorController<EntityPiglin> behaviorController = entityPiglin.getBrain();

        behaviorController.eraseMemory(WALK_TARGET_MEMORY_TYPE);
        entityPiglin.getNavigation().n();

        entityPiglin.take(entityItem.getHandle(), 1);

        ItemStack itemStack = new ItemStack(entityItem.getItem()).copy();
        itemStack.setCount(1);
        Item item = itemStack.getItem();

        if (item.builtInRegistryHolder().is(PIGLIN_LOVED_TAG)) {
            behaviorController.eraseMemory(TIME_TRYING_TO_REACH_ADMIRE_ITEM_MEMORY_TYPE);
            if (!new ItemStack(entityPiglin.getOffhandItem()).isEmpty()) {
                entityPiglin.spawnAtLocation(entityPiglin.getItemInHand(EnumHand.b));
            }

            entityPiglin.setItemSlot(EnumItemSlot.b, itemStack.getHandle());
            entityPiglin.broadcastBreakEvent(EnumItemSlot.b);

            if (item.getHandle() != PiglinAI.c) {
                entityPiglin.setPersistenceRequired();
            }

            behaviorController.setMemoryWithExpiry(ADMIRING_ITEM_MEMORY_TYPE, true, 120L);
        } else if ((item.getHandle() == PORKCHOP || item.getHandle() == COOKED_PORKCHOP) &&
                !behaviorController.hasMemoryValue(ATE_RECENTLY_MEMORY_TYPE)) {
            behaviorController.setMemoryWithExpiry(ATE_RECENTLY_MEMORY_TYPE, true, 200L);
        } else {
            handleEquipmentPickup((LivingEntity) bukkitPiglin, bukkitItem);
        }

        return true;
    }

    @Override
    public boolean handleEquipmentPickup(LivingEntity livingEntity, org.bukkit.entity.Item bukkitItem) {
        if (livingEntity instanceof Player)
            return false;

        EntityLiving nmsEntityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        Entity entityLiving = new Entity(nmsEntityLiving);

        if (!(entityLiving.getHandle() instanceof EntityInsentient))
            return false;

        Entity entityItem = new Entity(((CraftItem) bukkitItem).getHandle());
        ItemStack itemStack = new ItemStack(entityItem.getItem()).copy();
        itemStack.setCount(1);

        EnumItemSlot equipmentSlotForItem = Entity.getEquipmentSlotForItem(itemStack.getHandle());

        if (equipmentSlotForItem.a() != EnumItemSlot.Function.b) {
            return false;
        }

        ItemStack equipmentItem = new ItemStack(entityLiving.getItemBySlot(equipmentSlotForItem));

        double equipmentDropChance = entityLiving.getArmorDropChances()[equipmentSlotForItem.b()];

        Random random = new Random();
        if (!equipmentItem.isEmpty() && Math.max(random.nextFloat() - 0.1F, 0.0F) < equipmentDropChance) {
            nmsEntityLiving.forceDrops = true;
            entityLiving.spawnAtLocation(equipmentItem.getHandle());
            nmsEntityLiving.forceDrops = false;
        }

        entityLiving.setItemSlot(equipmentSlotForItem, itemStack.getHandle());
        entityLiving.broadcastBreakEvent(equipmentSlotForItem);

        SoundEffect equipSoundEffect = itemStack.getEquipSound();
        if (!itemStack.isEmpty() && equipSoundEffect != null) {
            entityLiving.gameEvent(EQUIP_GAME_EVENT);
            entityLiving.playSound(equipSoundEffect, 1.0F, 1.0F);
        }

        return true;
    }

    @Override
    public void giveExp(Player player, int amount) {
        EntityPlayer nmsEntityPlayer = ((CraftPlayer) player).getHandle();
        Entity entityPlayer = new Entity(nmsEntityPlayer);

        Map.Entry<EnumItemSlot, net.minecraft.world.item.ItemStack> entry =
                EnchantmentManager.b(MENDING_ENCHANTMENT, nmsEntityPlayer);
        ItemStack mendingItem = entry != null ? new ItemStack(entry.getValue()) : ItemStack.AIR;

        if (!mendingItem.isEmpty() && mendingItem.getItem().canBeDepleted()) {
            EntityExperienceOrb nmsOrb = EXPERIENCE_ORB_TYPE.a(entityPlayer.getWorld().getHandle());
            Entity orb = new Entity(nmsOrb);
            orb.setExperienceAmount(amount);

            try {
                // Paper
                nmsOrb.spawnReason = ExperienceOrb.SpawnReason.CUSTOM;
            } catch (Throwable ignored) {
            }

            orb.setPosRaw(entityPlayer.getX(), entityPlayer.getY(), entityPlayer.getZ());
            int repairAmount = Math.min(amount * 2, mendingItem.getDamageValue());
            PlayerItemMendEvent event = CraftEventFactory.callPlayerItemMendEvent(nmsEntityPlayer, nmsOrb,
                    mendingItem.getHandle(), repairAmount);
            repairAmount = event.getRepairAmount();

            orb.discard();

            if (!event.isCancelled()) {
                amount -= (repairAmount / 2);
                mendingItem.setDamageValue(mendingItem.getDamageValue() - repairAmount);
            }
        }

        if (amount > 0) {
            PlayerExpChangeEvent playerExpChangeEvent = new PlayerExpChangeEvent(player, amount);
            Bukkit.getPluginManager().callEvent(playerExpChangeEvent);
            if (playerExpChangeEvent.getAmount() > 0)
                player.giveExp(playerExpChangeEvent.getAmount());
        }
    }

    @Override
    public void enterVehicle(Vehicle vehicle, org.bukkit.entity.Entity entity) {
        vehicle.addPassenger(entity);
    }

    @Override
    public int getPassengersCount(Vehicle vehicle) {
        return (int) vehicle.getPassengers().stream().filter(entity -> !(entity instanceof Player)).count();
    }

    /*
     *   Tag methods
     */

    @Override
    public void updateEntity(LivingEntity sourceBukkit, LivingEntity targetBukkit) {
        Entity source = new Entity(((CraftLivingEntity) sourceBukkit).getHandle());
        Entity target = new Entity(((CraftLivingEntity) targetBukkit).getHandle());

        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        source.addAdditionalSaveData(nbtTagCompound.getHandle());

        nbtTagCompound.putFloat("Health", source.getMaxHealth());
        nbtTagCompound.remove("SaddleItem");
        nbtTagCompound.remove("Saddle");
        nbtTagCompound.remove("ArmorItem");
        nbtTagCompound.remove("ArmorItems");
        nbtTagCompound.remove("HandItems");
        nbtTagCompound.remove("Leash");
        if (targetBukkit instanceof Zombie) {
            //noinspection deprecation
            ((Zombie) targetBukkit).setBaby(nbtTagCompound.contains("IsBaby") && nbtTagCompound.getBoolean("IsBaby"));
        }

        target.readAdditionalSaveData(nbtTagCompound.getHandle());
    }

    @Override
    public String serialize(org.bukkit.inventory.ItemStack itemStack) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new DataOutputStream(outputStream);

        NBTTagCompound tagCompound = new NBTTagCompound();

        ItemStack nmsItem = new ItemStack(CraftItemStack.asNMSCopy(itemStack));

        nmsItem.save(tagCompound.getHandle());

        try {
            NBTCompressedStreamTools.a(tagCompound.getHandle(), dataOutput);
        } catch (Exception ex) {
            return null;
        }

        return new BigInteger(1, outputStream.toByteArray()).toString(32);
    }

    @Override
    public org.bukkit.inventory.ItemStack deserialize(String serialized) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(serialized, 32).toByteArray());

        try {
            net.minecraft.nbt.NBTTagCompound nbtTagCompoundRoot = NBTCompressedStreamTools.a(new DataInputStream(inputStream), NBTReadLimiter.a);

            ItemStack nmsItem = ItemStack.of(nbtTagCompoundRoot);

            return CraftItemStack.asBukkitCopy(nmsItem.getHandle());
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack itemStack, String key, Object value) {
        ItemStack nmsItem = new ItemStack(CraftItemStack.asNMSCopy(itemStack));
        NBTTagCompound tagCompound = nmsItem.getOrCreateTag();

        if (value instanceof Boolean)
            tagCompound.put(key, NBTTagByte.a((boolean) value));
        else if (value instanceof Integer)
            tagCompound.put(key, NBTTagInt.a((int) value));
        else if (value instanceof String)
            tagCompound.putString(key, (String) value);
        else if (value instanceof Double)
            tagCompound.put(key, NBTTagDouble.a((double) value));
        else if (value instanceof Short)
            tagCompound.put(key, NBTTagShort.a((short) value));
        else if (value instanceof Byte)
            tagCompound.put(key, NBTTagByte.a((byte) value));
        else if (value instanceof Float)
            tagCompound.putFloat(key, (float) value);
        else if (value instanceof Long)
            tagCompound.put(key, NBTTagLong.a((long) value));

        return CraftItemStack.asBukkitCopy(nmsItem.getHandle());
    }

    @Override
    public <T> T getTag(org.bukkit.inventory.ItemStack itemStack, String key, Class<T> valueType, Object def) {
        ItemStack nmsItem = ItemStack.ofNullable(CraftItemStack.asNMSCopy(itemStack));

        if (nmsItem == null)
            return valueType.cast(def);

        NBTTagCompound tagCompound = nmsItem.getOrCreateTag();

        if (tagCompound != null) {
            if (!tagCompound.contains(key))
                return valueType.cast(def);
            else if (valueType.equals(Boolean.class))
                return valueType.cast(tagCompound.getBoolean(key));
            else if (valueType.equals(Integer.class))
                return valueType.cast(tagCompound.getInt(key));
            else if (valueType.equals(String.class))
                return valueType.cast(tagCompound.getString(key));
            else if (valueType.equals(Double.class))
                return valueType.cast(tagCompound.getDouble(key));
            else if (valueType.equals(Short.class))
                return valueType.cast(tagCompound.getShort(key));
            else if (valueType.equals(Byte.class))
                return valueType.cast(tagCompound.getByte(key));
            else if (valueType.equals(Float.class))
                return valueType.cast(tagCompound.getFloat(key));
            else if (valueType.equals(Long.class))
                return valueType.cast(tagCompound.getLong(key));
        }

        throw new IllegalArgumentException("Cannot find nbt class type: " + valueType);
    }

    /*
     *   Other methods
     */

    @Remap(classPath = "net.minecraft.network.chat.Component",
            name = "nullToEmpty",
            type = Remap.Type.METHOD,
            remappedName = "a")
    @Override
    public Object getChatMessage(String message) {
        return IChatBaseComponent.a(message);
    }

    /*
     *   Data methods
     */

    @Override
    public void saveEntity(StackedEntity stackedEntity) {
        LivingEntity livingEntity = stackedEntity.getLivingEntity();
        PersistentDataContainer dataContainer = livingEntity.getPersistentDataContainer();

        dataContainer.set(STACK_AMOUNT, PersistentDataType.INTEGER, stackedEntity.getStackAmount());
        dataContainer.set(SPAWN_CAUSE, PersistentDataType.STRING, stackedEntity.getSpawnCause().name());

        if (stackedEntity.hasNameTag())
            dataContainer.set(NAME_TAG, PersistentDataType.BYTE, (byte) 1);

        int upgradeId = ((WStackedEntity) stackedEntity).getUpgradeId();
        if (upgradeId != 0)
            dataContainer.set(UPGRADE, PersistentDataType.INTEGER, upgradeId);
    }

    @Override
    public void loadEntity(StackedEntity stackedEntity) {
        LivingEntity livingEntity = stackedEntity.getLivingEntity();
        PersistentDataContainer dataContainer = livingEntity.getPersistentDataContainer();

        if (dataContainer.has(STACK_AMOUNT, PersistentDataType.INTEGER)) {
            try {
                Integer stackAmount = dataContainer.get(STACK_AMOUNT, PersistentDataType.INTEGER);
                stackedEntity.setStackAmount(stackAmount, false);

                String spawnCause = dataContainer.get(SPAWN_CAUSE, PersistentDataType.STRING);
                if (spawnCause != null)
                    stackedEntity.setSpawnCause(SpawnCause.valueOf(spawnCause));

                if (dataContainer.has(NAME_TAG, PersistentDataType.BYTE))
                    ((WStackedEntity) stackedEntity).setNameTag();

                Integer upgradeId = dataContainer.get(UPGRADE, PersistentDataType.INTEGER);
                if (upgradeId != null && upgradeId > 0)
                    ((WStackedEntity) stackedEntity).setUpgradeId(upgradeId);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void saveItem(StackedItem stackedItem) {
        org.bukkit.entity.Item item = stackedItem.getItem();
        PersistentDataContainer dataContainer = item.getPersistentDataContainer();
        dataContainer.set(STACK_AMOUNT, PersistentDataType.INTEGER, stackedItem.getStackAmount());
    }

    @Override
    public void loadItem(StackedItem stackedItem) {
        org.bukkit.entity.Item item = stackedItem.getItem();
        PersistentDataContainer dataContainer = item.getPersistentDataContainer();
        if (dataContainer.has(STACK_AMOUNT, PersistentDataType.INTEGER)) {
            Integer stackAmount = dataContainer.get(STACK_AMOUNT, PersistentDataType.INTEGER);
            stackedItem.setStackAmount(stackAmount, false);
        }
    }

    @SuppressWarnings({"deprecation", "NullableProblems"})
    private static class SyncedCreatureSpawnerImpl extends CraftBlockEntityState<TileEntityMobSpawner> implements SyncedCreatureSpawner {

        private final World world;
        private final BlockPosition blockPosition;
        private final Location blockLocation;

        SyncedCreatureSpawnerImpl(org.bukkit.World bukkitWorld, TileEntityMobSpawner tileEntityMobSpawner) {
            super(bukkitWorld, tileEntityMobSpawner);
            this.world = new World(((CraftWorld) bukkitWorld).getHandle());
            blockPosition = new TileEntity(tileEntityMobSpawner).getBlockPos();
            blockLocation = new Location(bukkitWorld, blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
        }

        @Override
        public EntityType getSpawnedType() {
            try {
                MinecraftKey key = getMobName();
                EntityType entityType = key == null ? EntityType.PIG : EntityType.fromName(key.getPath());
                return entityType == null ? EntityType.PIG : entityType;
            } catch (Exception ex) {
                return EntityType.PIG;
            }
        }

        @Override
        public void setSpawnedType(EntityType entityType) {
            if (entityType != null && entityType.getName() != null) {
                getSpawner().getSpawner().setEntityId(EntityTypes.a(entityType.getName()).orElse(PIG_TYPE));
            } else {
                throw new IllegalArgumentException("Can't spawn EntityType " + entityType + " from mobspawners!");
            }
        }

        @Override
        public void setCreatureTypeByName(String s) {
            EntityType entityType = EntityType.fromName(s);
            if (entityType != null && entityType != EntityType.UNKNOWN)
                setSpawnedType(entityType);
        }

        @Override
        public String getCreatureTypeName() {
            MinecraftKey key = getMobName();
            return key == null ? "PIG" : key.getPath();
        }

        @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
                name = "spawnDelay",
                type = Remap.Type.FIELD,
                remappedName = "c")
        @Override
        public int getDelay() {
            return getSpawner().getSpawner().getHandle().c;
        }


        @Override
        public void setDelay(int i) {
            getSpawner().getSpawner().getHandle().c = i;
        }

        @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
                name = "minSpawnDelay",
                type = Remap.Type.FIELD,
                remappedName = "h")
        @Override
        public int getMinSpawnDelay() {
            return getSpawner().getSpawner().getHandle().h;
        }

        @Override
        public void setMinSpawnDelay(int i) {
            getSpawner().getSpawner().getHandle().h = i;
        }

        @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
                name = "maxSpawnDelay",
                type = Remap.Type.FIELD,
                remappedName = "i")
        @Override
        public int getMaxSpawnDelay() {
            return getSpawner().getSpawner().getHandle().i;
        }

        @Override
        public void setMaxSpawnDelay(int i) {
            getSpawner().getSpawner().getHandle().i = i;
        }

        @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
                name = "spawnCount",
                type = Remap.Type.FIELD,
                remappedName = "j")
        @Override
        public int getSpawnCount() {
            return getSpawner().getSpawner().getHandle().j;
        }

        @Override
        public void setSpawnCount(int i) {
            getSpawner().getSpawner().getHandle().j = i;
        }

        @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
                name = "maxNearbyEntities",
                type = Remap.Type.FIELD,
                remappedName = "l")
        @Override
        public int getMaxNearbyEntities() {
            return getSpawner().getSpawner().getHandle().l;
        }

        @Override
        public void setMaxNearbyEntities(int i) {
            getSpawner().getSpawner().getHandle().l = i;
        }

        @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
                name = "requiredPlayerRange",
                type = Remap.Type.FIELD,
                remappedName = "m")
        @Override
        public int getRequiredPlayerRange() {
            return getSpawner().getSpawner().getHandle().m;
        }

        @Override
        public void setRequiredPlayerRange(int i) {
            getSpawner().getSpawner().getHandle().m = i;
        }

        @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
                name = "spawnRange",
                type = Remap.Type.FIELD,
                remappedName = "n")
        @Override
        public int getSpawnRange() {
            return getSpawner().getSpawner().getHandle().n;
        }

        @Override
        public void setSpawnRange(int i) {
            getSpawner().getSpawner().getHandle().n = i;
        }

        public boolean isActivated() {
            return false;
        }

        public void resetTimer() {

        }

        public void setSpawnedItem(org.bukkit.inventory.ItemStack itemStack) {

        }

        @Override
        public void updateSpawner(SpawnerUpgrade spawnerUpgrade) {
            MobSpawnerAbstract mobSpawnerAbstract = getSpawner().getSpawner().getHandle();
            mobSpawnerAbstract.h = spawnerUpgrade.getMinSpawnDelay();
            mobSpawnerAbstract.i = spawnerUpgrade.getMaxSpawnDelay();
            mobSpawnerAbstract.j = spawnerUpgrade.getSpawnCount();
            mobSpawnerAbstract.l = spawnerUpgrade.getMaxNearbyEntities();
            mobSpawnerAbstract.m = spawnerUpgrade.getRequiredPlayerRange();
            mobSpawnerAbstract.n = spawnerUpgrade.getSpawnRange();
        }

        @Override
        public SpawnerCachedData readData() {
            MobSpawnerAbstract mobSpawnerAbstract = getSpawner().getSpawner().getHandle();
            return new SpawnerCachedData(
                    mobSpawnerAbstract.h,
                    mobSpawnerAbstract.i,
                    mobSpawnerAbstract.j,
                    mobSpawnerAbstract.l,
                    mobSpawnerAbstract.m,
                    mobSpawnerAbstract.n,
                    mobSpawnerAbstract.c / 20,
                    ""
            );
        }

        @Override
        public boolean update(boolean force, boolean applyPhysics) {
            return blockLocation.getBlock().getState().update(force, applyPhysics);
        }

        TileEntity getSpawner() {
            return world.getBlockEntity(blockPosition.getHandle());
        }

        @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
                name = "nextSpawnData",
                type = Remap.Type.FIELD,
                remappedName = "e")
        private MinecraftKey getMobName() {
            MobSpawnerData mobSpawnerData = new MobSpawnerData(getSpawner().getSpawner().getHandle().e);
            String id = mobSpawnerData.getEntityToSpawn().getString("id");
            return UtilColor.b(id) ? null : new MinecraftKey(id);
        }

    }

}
