package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.utils.chunks.ChunkPosition;
import com.bgsoftware.wildstacker.utils.spawners.SpawnerCachedData;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTReadLimiter;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.ChatMessage;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutCollect;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.stats.StatisticList;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.UtilColor;
import net.minecraft.world.EnumHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityExperienceOrb;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityPositionTypes;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.SaddleStorage;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.gossip.Reputation;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.EntityAnimal;
import net.minecraft.world.entity.animal.EntityChicken;
import net.minecraft.world.entity.animal.EntityTurtle;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.monster.EntityZombieVillager;
import net.minecraft.world.entity.monster.piglin.EntityPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinAI;
import net.minecraft.world.entity.npc.EntityVillager;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.EntityArrow;
import net.minecraft.world.entity.projectile.EntityFireballFireball;
import net.minecraft.world.entity.projectile.EntityThrownTrident;
import net.minecraft.world.entity.raid.EntityRaider;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemSword;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.MobSpawnerAbstract;
import net.minecraft.world.level.MobSpawnerData;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockRotatable;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import net.minecraft.world.level.block.state.IBlockData;
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
import org.bukkit.entity.Item;
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
import java.util.regex.Pattern;

import static com.bgsoftware.wildstacker.nms.NMSMappings_v1_18_R2.*;

@SuppressWarnings("ConstantConditions")
public final class NMSAdapter_v1_18_R2 implements NMSAdapter {

    private static final ReflectField<Integer> ENTITY_EXP = new ReflectField<>(EntityInsentient.class, int.class, "bL");
    private static final ReflectField<Integer> LAST_DAMAGE_BY_PLAYER_TIME = new ReflectField<>(EntityLiving.class, int.class, "bd");
    private static final ReflectMethod<Boolean> IS_DROP_EXPERIENCE = new ReflectMethod<>(EntityLiving.class, "dI");
    private static final ReflectMethod<SoundEffect> GET_SOUND_DEATH = new ReflectMethod<>(EntityLiving.class, "x_");
    private static final ReflectMethod<Float> GET_SOUND_VOLUME = new ReflectMethod<>(EntityLiving.class, "ev");
    private static final ReflectMethod<Float> GET_SOUND_PITCH = new ReflectMethod<>(EntityLiving.class, "ew");
    private static final ReflectField<Entity.RemovalReason> ENTITY_REMOVE_REASON = new ReflectField<>(Entity.class, Entity.RemovalReason.class, "aD");
    private static final ReflectField<Integer> CHICKEN_EGG_LAY_TIME = new ReflectField<>(EntityChicken.class, Integer.class, "cb");
    private static final ReflectMethod<Boolean> RAIDER_CAN_RAID = new ReflectMethod<>(EntityRaider.class, boolean.class, "fO");
    private static final ReflectMethod<Raid> RAIDER_RAID = new ReflectMethod<>(EntityRaider.class, Raid.class, "fN");
    private static final ReflectMethod<Void> TURTLE_SET_HAS_EGG = new ReflectMethod<>(EntityTurtle.class, "v", boolean.class);
    private static final ReflectMethod<BlockPosition> TURTLE_HOME_POS = new ReflectMethod<>(EntityTurtle.class, "fA");

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("§x(?>§[0-9a-f]){6}");

    /*
     *   Entity methods
     */
    private static final NamespacedKey
            STACK_AMOUNT = new NamespacedKey(plugin, "stackAmount"),
            SPAWN_CAUSE = new NamespacedKey(plugin, "spawnCause"),
            NAME_TAG = new NamespacedKey(plugin, "nameTag"),
            UPGRADE = new NamespacedKey(plugin, "upgrade");

    @Override
    public <T extends org.bukkit.entity.Entity> T createEntity(Location location, Class<T> type, SpawnCause spawnCause, Consumer<T> beforeSpawnConsumer, Consumer<T> afterSpawnConsumer) {
        CraftWorld world = (CraftWorld) location.getWorld();

        assert world != null;

        Entity nmsEntity = world.createEntity(location, type);
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

        Entity nmsEntity = world.createEntity(location, type);
        org.bukkit.entity.Entity bukkitEntity = nmsEntity.getBukkitEntity();

        world.addEntity(nmsEntity, spawnCause.toSpawnReason());

        return type.cast(bukkitEntity);
    }

    @Override
    public Zombie spawnZombieVillager(Villager villager) {
        EntityVillager entityVillager = ((CraftVillager) villager).getHandle();
        EntityZombieVillager entityZombieVillager = EntityTypes.bg.a(getWorld(entityVillager));

        assert entityZombieVillager != null;
        entityZombieVillager.s(entityVillager);
        setVillagerData(entityZombieVillager, getVillagerData(entityVillager));

        Reputation villagerReputation = getGossips(entityVillager);

        entityZombieVillager.a(villagerReputation.a(DynamicOpsNBT.a).getValue());
        setTradeOffers(entityZombieVillager, getOffers(entityVillager).a());
        setVillagerXp(entityZombieVillager, getExperience(entityVillager));
        setBaby(entityZombieVillager, isBaby(entityVillager));
        setNoAI(entityZombieVillager, isNoAI(entityVillager));


        if (hasCustomName(entityVillager)) {
            NMSMappings_v1_18_R2.setCustomName(entityZombieVillager, NMSMappings_v1_18_R2.getCustomName(entityVillager));
            NMSMappings_v1_18_R2.setCustomNameVisible(entityZombieVillager, getCustomNameVisible(entityVillager));
        }

        EntityTransformEvent entityTransformEvent = new EntityTransformEvent(entityVillager.getBukkitEntity(),
                Collections.singletonList(entityZombieVillager.getBukkitEntity()),
                EntityTransformEvent.TransformReason.INFECTION);
        Bukkit.getPluginManager().callEvent(entityTransformEvent);

        if (entityTransformEvent.isCancelled())
            return null;

        addFreshEntity(getWorld(entityVillager), entityZombieVillager, CreatureSpawnEvent.SpawnReason.INFECTION);
        levelEvent(getWorld(entityVillager), null, 1026,
                new BlockPosition(NMSMappings_v1_18_R2.getX(entityVillager), NMSMappings_v1_18_R2.getY(entityVillager), NMSMappings_v1_18_R2.getZ(entityVillager)), 0);

        return (Zombie) entityZombieVillager.getBukkitEntity();
    }

    @Override
    public void setInLove(Animals entity, Player breeder, boolean inLove) {
        EntityAnimal nmsEntity = ((CraftAnimals) entity).getHandle();
        EntityPlayer entityPlayer = ((CraftPlayer) breeder).getHandle();
        if (inLove) {
            NMSMappings_v1_18_R2.setInLove(nmsEntity, entityPlayer);
        } else {
            resetLove(nmsEntity);
        }
    }

    @Override
    public boolean isInLove(Animals entity) {
        return NMSMappings_v1_18_R2.isInLove((EntityAnimal) ((CraftEntity) entity).getHandle());
    }

    @Override
    public boolean isAnimalFood(Animals animal, org.bukkit.inventory.ItemStack itemStack) {
        if (itemStack == null)
            return false;

        EntityAnimal nmsEntity = ((CraftAnimals) animal).getHandle();
        return isFood(nmsEntity, CraftItemStack.asNMSCopy(itemStack));
    }

    @Override
    public int getEntityExp(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();

        if (!(entityLiving instanceof EntityInsentient entityInsentient))
            return 0;

        int defaultEntityExp = ENTITY_EXP.get(entityInsentient);
        int exp = entityInsentient.getExpReward();

        ENTITY_EXP.set(entityInsentient, defaultEntityExp);

        return exp;
    }

    @Override
    public boolean canDropExp(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        int lastDamageByPlayerTime = LAST_DAMAGE_BY_PLAYER_TIME.get(entityLiving);
        boolean isDropExperience = IS_DROP_EXPERIENCE.invoke(entityLiving);
        return lastDamageByPlayerTime > 0 && isDropExperience &&
                getGameRules(getWorld(entityLiving)).b(GameRules.f);
    }

    @Override
    public void updateLastDamageTime(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        LAST_DAMAGE_BY_PLAYER_TIME.set(entityLiving, 100);
    }

    @Override
    public void setHealthDirectly(LivingEntity livingEntity, double health) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        setHealth(entityLiving, (float) health);
    }

    @Override
    public void setEntityDead(LivingEntity livingEntity, boolean dead) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        ENTITY_REMOVE_REASON.set(entityLiving, dead ? Entity.RemovalReason.b : null);
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

    @Override
    public void setKiller(LivingEntity livingEntity, Player killer) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        entityLiving.bc = killer == null ? null : ((CraftPlayer) killer).getHandle();
    }

    @Override
    public boolean canSpawnOn(org.bukkit.entity.Entity bukkitEntity, Location location) {
        assert location.getWorld() != null;
        World world = ((CraftWorld) location.getWorld()).getHandle();
        EntityTypes<?> entityTypes = NMSMappings_v1_18_R2.getType(((CraftEntity) bukkitEntity).getHandle());
        return EntityPositionTypes.a(entityTypes, world.getMinecraftWorld(), EnumMobSpawn.c,
                new BlockPosition(location.getX(), location.getY(), location.getZ()), getRandom(world));
    }

    @Override
    public Collection<org.bukkit.entity.Entity> getEntitiesAtChunk(ChunkPosition chunkPosition) {
        return new ArrayList<>();
    }

    @Override
    public Collection<org.bukkit.entity.Entity> getNearbyEntities(Location location, int range, Predicate<org.bukkit.entity.Entity> filter) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        List<org.bukkit.entity.Entity> entities = new ArrayList<>();

        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(
                location.getBlockX() - range,
                location.getBlockY() - range,
                location.getBlockZ() - range,
                location.getBlockX() + range,
                location.getBlockY() + range,
                location.getBlockZ() + range
        );

        getEntities(world).a(axisAlignedBB, entity -> {
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
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        return nmsItem != null && nmsItem.g();
    }

    @Override
    public boolean doesStriderHaveSaddle(Strider strider) {
        return isSaddled(((CraftStrider) strider).getHandle());
    }

    @Override
    public void removeStriderSaddle(Strider strider) {
        try {
            strider.setSaddle(false);
        } catch (Throwable ex) {
            SaddleStorage saddleStorage = getSaddleStorage(((CraftStrider) strider).getHandle());
            setSaddle(saddleStorage, false);
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
            EntityTurtle entityTurtle = ((CraftTurtle) turtle).getHandle();
            BlockPosition homePosition = TURTLE_HOME_POS.invoke(entityTurtle);
            return new Location(getWorld(entityTurtle).getWorld(),
                    getX(homePosition), getY(homePosition), getZ(homePosition));
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
        EntityLiving targetLiving = ((CraftLivingEntity) target).getHandle();
        EntityHuman entityHuman = ((CraftPlayer) attacker).getHandle();

        // Making sure the player used a sword.
        if (usedItem.getType() == Material.AIR || !(getItem(CraftItemStack.asNMSCopy(usedItem)) instanceof ItemSword))
            return;

        float sweepDamage = 1.0F + EnchantmentManager.a(entityHuman) * (float) damage;
        List<EntityLiving> nearbyEntities = getEntitiesOfClass(getWorld(targetLiving), EntityLiving.class,
                inflate(getBoundingBox(targetLiving), 1.0D, 0.25D, 1.0D));

        for (EntityLiving nearby : nearbyEntities) {
            if (nearby != targetLiving && nearby != entityHuman && !entityHuman.r(nearby) &&
                    (!(nearby instanceof EntityArmorStand) || !isMarker((EntityArmorStand) nearby)) &&
                    entityHuman.f(nearby) < 9.0D) {
                hurt(nearby, playerAttack(entityHuman).sweep(), sweepDamage);
            }
        }
    }

    @Override
    public String getCustomName(org.bukkit.entity.Entity entity) {
        // Much more optimized way than Bukkit's method.
        IChatBaseComponent chatBaseComponent = NMSMappings_v1_18_R2.getCustomName(((CraftEntity) entity).getHandle());
        return chatBaseComponent == null ? "" : chatBaseComponent.a();
    }

    @Override
    public void setCustomName(org.bukkit.entity.Entity entity, String name) {
        if (HEX_COLOR_PATTERN.matcher(name).find()) {
            // When hex color is found in the name of the entity, we should use the regular bukkit's method instead.
            entity.setCustomName(name);
        } else {
            // Much more optimized way than Bukkit's method.
            NMSMappings_v1_18_R2.setCustomName(((CraftEntity) entity).getHandle(),
                    name == null || name.isEmpty() ? null : new ChatComponentText(name));
        }
    }

    /*
     *   Spawner methods
     */

    @Override
    public boolean handleTotemOfUndying(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();

        ItemStack totemOfUndying = ItemStack.b;

        for (EnumHand enumHand : EnumHand.values()) {
            ItemStack handItem = entityLiving.b(enumHand);
            if (getItem(handItem) == Items.sw) {
                totemOfUndying = handItem;
                break;
            }
        }

        EntityResurrectEvent event = new EntityResurrectEvent(livingEntity);
        event.setCancelled(isEmpty(totemOfUndying));
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return false;

        if (!isEmpty(totemOfUndying)) {
            shrink(totemOfUndying, 1);

            if (entityLiving instanceof EntityPlayer) {
                ((EntityPlayer) entityLiving).b(StatisticList.c.b(Items.sw));
                CriterionTriggers.B.a((EntityPlayer) entityLiving, totemOfUndying);
            }

            setHealth(entityLiving, 1.0F);
            entityLiving.removeAllEffects(EntityPotionEffectEvent.Cause.TOTEM);
            entityLiving.addEffect(new MobEffect(MobEffects.j, 900, 1), EntityPotionEffectEvent.Cause.TOTEM);
            entityLiving.addEffect(new MobEffect(MobEffects.v, 100, 1), EntityPotionEffectEvent.Cause.TOTEM);
            entityLiving.addEffect(new MobEffect(MobEffects.l, 800, 0), EntityPotionEffectEvent.Cause.TOTEM);
            broadcastEntityEvent(getWorld(entityLiving), entityLiving, (byte) 35);
        }

        return true;
    }

    @Override
    public SyncedCreatureSpawner createSyncedSpawner(CreatureSpawner creatureSpawner) {
        org.bukkit.World bukkitWorld = creatureSpawner.getWorld();
        World world = ((CraftWorld) bukkitWorld).getHandle();
        BlockPosition blockPosition = new BlockPosition(creatureSpawner.getX(), creatureSpawner.getY(), creatureSpawner.getZ());
        TileEntityMobSpawner tileEntityMobSpawner = (TileEntityMobSpawner) getBlockEntity(world, blockPosition);
        return new SyncedCreatureSpawnerImpl(bukkitWorld, tileEntityMobSpawner);
    }

    /*
     *   Item methods
     */

    @Override
    public boolean isRotatable(Block block) {
        World world = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        IBlockData blockData = getBlockState(world, blockPosition);
        return getBlock(blockData) instanceof BlockRotatable;
    }

    @Override
    public StackedItem createItem(Location location, org.bukkit.inventory.ItemStack itemStack, SpawnCause spawnCause, Consumer<StackedItem> itemConsumer) {
        CraftWorld craftWorld = (CraftWorld) location.getWorld();

        EntityItem entityItem = new EntityItem(craftWorld.getHandle(), location.getX(), location.getY(), location.getZ(), CraftItemStack.asNMSCopy(itemStack));

        entityItem.ap = 10;

        try {
            entityItem.canMobPickup = false;
            Executor.sync(() -> entityItem.canMobPickup = true, 20L);
        } catch (Throwable ignored) {
        }

        StackedItem stackedItem = WStackedItem.ofBypass((Item) entityItem.getBukkitEntity());

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
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);
        NBTTagCompound nbtTagCompound = getOrCreateTag(itemStack);

        NBTTagCompound skullOwner = contains(nbtTagCompound, "SkullOwner") ?
                getCompound(nbtTagCompound, "SkullOwner") : new NBTTagCompound();

        putString(skullOwner, "Id", new UUID(texture.hashCode(), texture.hashCode()).toString());

        NBTTagCompound properties = new NBTTagCompound();

        NBTTagList textures = new NBTTagList();
        NBTTagCompound signature = new NBTTagCompound();
        putString(signature, "Value", texture);
        textures.add(signature);

        put(properties, "textures", textures);

        put(skullOwner, "Properties", properties);

        put(nbtTagCompound, "SkullOwner", skullOwner);

        NMSMappings_v1_18_R2.setTag(itemStack, nbtTagCompound);

        return CraftItemStack.asBukkitCopy(itemStack);
    }

    @Override
    public void awardKillScore(org.bukkit.entity.Entity bukkitDamaged,
                               org.bukkit.entity.Entity damagerEntity) {
        Entity damaged = ((CraftEntity) bukkitDamaged).getHandle();
        Entity damager = ((CraftEntity) damagerEntity).getHandle();

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
    public void playPickupAnimation(LivingEntity livingEntity, Item item) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        EntityItem entityItem = (EntityItem) ((CraftItem) item).getHandle();
        ChunkProviderServer chunkProvider = getChunkSource((WorldServer) getWorld(entityLiving));
        broadcast(chunkProvider, entityItem, new PacketPlayOutCollect(getId(entityItem), getId(entityLiving), item.getItemStack().getAmount()));
        //Makes sure the entity is still there.
        broadcast(chunkProvider, entityItem, new PacketPlayOutSpawnEntity(entityItem));
        broadcast(chunkProvider, entityItem, new PacketPlayOutEntityMetadata(getId(entityItem), getEntityData(entityItem), true));
    }

    @Override
    public void playDeathSound(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();

        SoundEffect deathSound = GET_SOUND_DEATH.invoke(entityLiving);
        float soundVolume = GET_SOUND_VOLUME.invoke(entityLiving);
        float soundPitch = GET_SOUND_PITCH.invoke(entityLiving);

        if (deathSound != null)
            playSound(entityLiving, deathSound, soundVolume, soundPitch);
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
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        if (entityLiving instanceof EntityInsentient entityInsentient)
            spawnAnim(entityInsentient);
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
            villagerRaid.a((Entity) ((CraftPlayer) player).getHandle());
        }
    }

    @Override
    public boolean handlePiglinPickup(org.bukkit.entity.Entity bukkitPiglin, Item bukkitItem) {
        if (!(bukkitPiglin instanceof Piglin))
            return false;

        EntityPiglin entityPiglin = ((CraftPiglin) bukkitPiglin).getHandle();
        EntityItem entityItem = (EntityItem) ((CraftItem) bukkitItem).getHandle();

        entityPiglin.a(entityItem);

        BehaviorController<EntityPiglin> behaviorController = getBrain(entityPiglin);

        eraseMemory(behaviorController, MemoryModuleType.m);
        getNavigation(entityPiglin).n();

        take(entityPiglin, entityItem, 1);

        ItemStack itemStack = copy(NMSMappings_v1_18_R2.getItem(entityItem));
        setCount(itemStack, 1);
        net.minecraft.world.item.Item item = getItem(itemStack);

        if (is(TagsItem.N, item)) {
            eraseMemory(behaviorController, MemoryModuleType.Y);
            if (!isEmpty(NMSMappings_v1_18_R2.getOffhandItem(entityPiglin))) {
                entityPiglin.b(entityPiglin.b(EnumHand.b));
            }

            setItemSlot(entityPiglin, EnumItemSlot.b, itemStack);
            entityPiglin.d(EnumItemSlot.b);

            if (item != PiglinAI.c) {
                setPersistenceRequired(entityPiglin);
            }

            behaviorController.a(MemoryModuleType.X, true, 120L);
        } else if ((item == Items.nJ || item == Items.nK) && !hasMemoryValue(behaviorController, MemoryModuleType.ap)) {
            behaviorController.a(MemoryModuleType.ap, true, 200L);
        } else {
            handleEquipmentPickup((LivingEntity) bukkitPiglin, bukkitItem);
        }

        return true;
    }

    @Override
    public boolean handleEquipmentPickup(LivingEntity livingEntity, Item bukkitItem) {
        if (livingEntity instanceof Player)
            return false;

        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();

        if (!(entityLiving instanceof EntityInsentient entityInsentient))
            return false;

        EntityItem entityItem = (EntityItem) ((CraftItem) bukkitItem).getHandle();
        ItemStack itemStack = copy(NMSMappings_v1_18_R2.getItem(entityItem));
        setCount(itemStack, 1);

        EnumItemSlot equipmentSlotForItem = getEquipmentSlotForItem(itemStack);

        if (equipmentSlotForItem.a() != EnumItemSlot.Function.b) {
            return false;
        }

        ItemStack equipmentItem = getItemBySlot(entityInsentient, equipmentSlotForItem);

        double equipmentDropChance = entityInsentient.bT[equipmentSlotForItem.b()];

        Random random = new Random();
        if (!isEmpty(equipmentItem) && Math.max(random.nextFloat() - 0.1F, 0.0F) < equipmentDropChance) {
            entityInsentient.forceDrops = true;
            entityInsentient.b(equipmentItem);
            entityInsentient.forceDrops = false;
        }

        setItemSlot(entityInsentient, equipmentSlotForItem, itemStack);
        entityInsentient.d(equipmentSlotForItem);

        SoundEffect equipSoundEffect = itemStack.M();
        if (!isEmpty(itemStack) && equipSoundEffect != null) {
            entityInsentient.a(GameEvent.u);
            playSound(entityInsentient, equipSoundEffect, 1.0F, 1.0F);
        }

        return true;
    }

    @Override
    public void giveExp(Player player, int amount) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        Map.Entry<EnumItemSlot, ItemStack> entry = EnchantmentManager.b(Enchantments.K, entityPlayer);
        ItemStack mendingItem = entry != null ? entry.getValue() : ItemStack.b;

        if (!isEmpty(mendingItem) && canBeDepleted(getItem(mendingItem))) {
            EntityExperienceOrb orb = EntityTypes.A.a(getWorld(entityPlayer));
            orb.aq = amount;
            try {
                // Paper
                orb.spawnReason = ExperienceOrb.SpawnReason.CUSTOM;
            } catch (Throwable ignored) {
            }
            setPosRaw(orb, NMSMappings_v1_18_R2.getX(entityPlayer), NMSMappings_v1_18_R2.getY(entityPlayer), NMSMappings_v1_18_R2.getZ(entityPlayer));
            int repairAmount = Math.min(amount * 2, getDamageValue(mendingItem));
            PlayerItemMendEvent event = CraftEventFactory.callPlayerItemMendEvent(entityPlayer, orb, mendingItem, repairAmount);
            repairAmount = event.getRepairAmount();
            discard(orb);
            if (!event.isCancelled()) {
                amount -= (repairAmount / 2);
                setDamageValue(mendingItem, getDamageValue(mendingItem) - repairAmount);
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
        EntityLiving source = ((CraftLivingEntity) sourceBukkit).getHandle();
        EntityLiving target = ((CraftLivingEntity) targetBukkit).getHandle();

        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        addAdditionalSaveData(source, nbtTagCompound);

        putFloat(nbtTagCompound, "Health", getMaxHealth(source));
        remove(nbtTagCompound, "SaddleItem");
        remove(nbtTagCompound, "Saddle");
        remove(nbtTagCompound, "ArmorItem");
        remove(nbtTagCompound, "ArmorItems");
        remove(nbtTagCompound, "HandItems");
        remove(nbtTagCompound, "Leash");
        if (targetBukkit instanceof Zombie) {
            //noinspection deprecation
            ((Zombie) targetBukkit).setBaby(contains(nbtTagCompound, "IsBaby") && getBoolean(nbtTagCompound, "IsBaby"));
        }

        readAdditionalSaveData(target, nbtTagCompound);
    }

    @Override
    public String serialize(org.bukkit.inventory.ItemStack itemStack) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new DataOutputStream(outputStream);

        NBTTagCompound tagCompound = new NBTTagCompound();

        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);

        save(nmsItem, tagCompound);

        try {
            NBTCompressedStreamTools.a(tagCompound, dataOutput);
        } catch (Exception ex) {
            return null;
        }

        return new BigInteger(1, outputStream.toByteArray()).toString(32);
    }

    @Override
    public org.bukkit.inventory.ItemStack deserialize(String serialized) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(serialized, 32).toByteArray());

        try {
            NBTTagCompound nbtTagCompoundRoot = NBTCompressedStreamTools.a(new DataInputStream(inputStream), NBTReadLimiter.a);

            ItemStack nmsItem = ItemStack.a(nbtTagCompoundRoot);

            return CraftItemStack.asBukkitCopy(nmsItem);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack itemStack, String key, Object value) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = getOrCreateTag(nmsItem);

        if (value instanceof Boolean)
            put(tagCompound, key, NBTTagByte.a((boolean) value));
        else if (value instanceof Integer)
            put(tagCompound, key, NBTTagInt.a((int) value));
        else if (value instanceof String)
            putString(tagCompound, key, (String) value);
        else if (value instanceof Double)
            put(tagCompound, key, NBTTagDouble.a((double) value));
        else if (value instanceof Short)
            put(tagCompound, key, NBTTagShort.a((short) value));
        else if (value instanceof Byte)
            put(tagCompound, key, NBTTagByte.a((byte) value));
        else if (value instanceof Float)
            putFloat(tagCompound, key, (float) value);
        else if (value instanceof Long)
            put(tagCompound, key, NBTTagLong.a((long) value));

        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public <T> T getTag(org.bukkit.inventory.ItemStack itemStack, String key, Class<T> valueType, Object def) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);

        if (nmsItem == null)
            return valueType.cast(def);

        NBTTagCompound tagCompound = hasTag(nmsItem) ? NMSMappings_v1_18_R2.getTag(nmsItem) : new NBTTagCompound();

        if (tagCompound != null) {
            if (!contains(tagCompound, key))
                return valueType.cast(def);
            else if (valueType.equals(Boolean.class))
                return valueType.cast(getBoolean(tagCompound, key));
            else if (valueType.equals(Integer.class))
                return valueType.cast(getInt(tagCompound, key));
            else if (valueType.equals(String.class))
                return valueType.cast(getString(tagCompound, key));
            else if (valueType.equals(Double.class))
                return valueType.cast(getDouble(tagCompound, key));
            else if (valueType.equals(Short.class))
                return valueType.cast(getShort(tagCompound, key));
            else if (valueType.equals(Byte.class))
                return valueType.cast(getByte(tagCompound, key));
            else if (valueType.equals(Float.class))
                return valueType.cast(getFloat(tagCompound, key));
            else if (valueType.equals(Long.class))
                return valueType.cast(getLong(tagCompound, key));
        }

        throw new IllegalArgumentException("Cannot find nbt class type: " + valueType);
    }

    /*
     *   Other methods
     */

    @Override
    public Object getChatMessage(String message) {
        return new ChatMessage(message);
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
        } else {
            // Old saving method
            EntityLiving entityLiving = ((CraftLivingEntity) stackedEntity.getLivingEntity()).getHandle();
            for (String scoreboardTag : getTags(entityLiving)) {
                if (scoreboardTag.startsWith("ws:")) {
                    String[] tagSections = scoreboardTag.split("=");
                    if (tagSections.length == 2) {
                        try {
                            String key = tagSections[0], value = tagSections[1];
                            if (key.equals("ws:stack-amount")) {
                                stackedEntity.setStackAmount(Integer.parseInt(value), false);
                            } else if (key.equals("ws:stack-cause")) {
                                stackedEntity.setSpawnCause(SpawnCause.valueOf(value));
                            } else if (key.equals("ws:name-tag")) {
                                ((WStackedEntity) stackedEntity).setNameTag();
                            } else if (key.equals("ws:upgrade")) {
                                ((WStackedEntity) stackedEntity).setUpgradeId(Integer.parseInt(value));
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }

    @Override
    public void saveItem(StackedItem stackedItem) {
        Item item = stackedItem.getItem();
        PersistentDataContainer dataContainer = item.getPersistentDataContainer();
        dataContainer.set(STACK_AMOUNT, PersistentDataType.INTEGER, stackedItem.getStackAmount());
    }

    @Override
    public void loadItem(StackedItem stackedItem) {
        Item item = stackedItem.getItem();
        PersistentDataContainer dataContainer = item.getPersistentDataContainer();

        if (dataContainer.has(STACK_AMOUNT, PersistentDataType.INTEGER)) {
            Integer stackAmount = dataContainer.get(STACK_AMOUNT, PersistentDataType.INTEGER);
            stackedItem.setStackAmount(stackAmount, false);
        } else {
            // Old saving method
            EntityItem entityItem = (EntityItem) ((CraftItem) item).getHandle();
            for (String scoreboardTag : getTags(entityItem)) {
                if (scoreboardTag.startsWith("ws:")) {
                    String[] tagSections = scoreboardTag.split("=");
                    if (tagSections.length == 2) {
                        try {
                            String key = tagSections[0], value = tagSections[1];
                            if (key.equals("ws:stack-amount")) {
                                stackedItem.setStackAmount(Integer.parseInt(value), false);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings({"deprecation", "NullableProblems"})
    private static class SyncedCreatureSpawnerImpl extends CraftBlockEntityState<TileEntityMobSpawner> implements SyncedCreatureSpawner {

        private final World world;
        private final BlockPosition blockPosition;
        private final Location blockLocation;

        SyncedCreatureSpawnerImpl(org.bukkit.World bukkitWorld, TileEntityMobSpawner tileEntityMobSpawner) {
            super(bukkitWorld, tileEntityMobSpawner);
            this.world = ((CraftWorld) bukkitWorld).getHandle();
            blockPosition = NMSMappings_v1_18_R2.getBlockPos(tileEntityMobSpawner);
            blockLocation = new Location(bukkitWorld, NMSMappings_v1_18_R2.getX(blockPosition),
                    NMSMappings_v1_18_R2.getY(blockPosition), NMSMappings_v1_18_R2.getZ(blockPosition));
        }

        @Override
        public EntityType getSpawnedType() {
            try {
                MinecraftKey key = getMobName();
                EntityType entityType = key == null ? EntityType.PIG : EntityType.fromName(getPath(key));
                return entityType == null ? EntityType.PIG : entityType;
            } catch (Exception ex) {
                return EntityType.PIG;
            }
        }

        @Override
        public void setSpawnedType(EntityType entityType) {
            if (entityType != null && entityType.getName() != null) {
                setEntityId(NMSMappings_v1_18_R2.getSpawner(getSpawner()),
                        EntityTypes.a(entityType.getName()).orElse(EntityTypes.an));
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
            return key == null ? "PIG" : getPath(key);
        }

        @Override
        public int getDelay() {
            return NMSMappings_v1_18_R2.getSpawner(getSpawner()).c;
        }

        @Override
        public void setDelay(int i) {
            NMSMappings_v1_18_R2.getSpawner(getSpawner()).c = i;
        }

        @Override
        public int getMinSpawnDelay() {
            return NMSMappings_v1_18_R2.getSpawner(getSpawner()).h;
        }

        @Override
        public void setMinSpawnDelay(int i) {
            NMSMappings_v1_18_R2.getSpawner(getSpawner()).h = i;
        }

        @Override
        public int getMaxSpawnDelay() {
            return NMSMappings_v1_18_R2.getSpawner(getSpawner()).i;
        }

        @Override
        public void setMaxSpawnDelay(int i) {
            NMSMappings_v1_18_R2.getSpawner(getSpawner()).i = i;
        }

        @Override
        public int getSpawnCount() {
            return NMSMappings_v1_18_R2.getSpawner(getSpawner()).j;
        }

        @Override
        public void setSpawnCount(int i) {
            NMSMappings_v1_18_R2.getSpawner(getSpawner()).j = i;
        }

        @Override
        public int getMaxNearbyEntities() {
            return NMSMappings_v1_18_R2.getSpawner(getSpawner()).l;
        }

        @Override
        public void setMaxNearbyEntities(int i) {
            NMSMappings_v1_18_R2.getSpawner(getSpawner()).l = i;
        }

        @Override
        public int getRequiredPlayerRange() {
            return NMSMappings_v1_18_R2.getSpawner(getSpawner()).m;
        }

        @Override
        public void setRequiredPlayerRange(int i) {
            NMSMappings_v1_18_R2.getSpawner(getSpawner()).m = i;
        }

        @Override
        public int getSpawnRange() {
            return NMSMappings_v1_18_R2.getSpawner(getSpawner()).n;
        }

        @Override
        public void setSpawnRange(int i) {
            NMSMappings_v1_18_R2.getSpawner(getSpawner()).n = i;
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
            MobSpawnerAbstract mobSpawnerAbstract = NMSMappings_v1_18_R2.getSpawner(getSpawner());
            mobSpawnerAbstract.h = spawnerUpgrade.getMinSpawnDelay();
            mobSpawnerAbstract.i = spawnerUpgrade.getMaxSpawnDelay();
            mobSpawnerAbstract.j = spawnerUpgrade.getSpawnCount();
            mobSpawnerAbstract.l = spawnerUpgrade.getMaxNearbyEntities();
            mobSpawnerAbstract.m = spawnerUpgrade.getRequiredPlayerRange();
            mobSpawnerAbstract.n = spawnerUpgrade.getSpawnRange();
        }

        @Override
        public SpawnerCachedData readData() {
            MobSpawnerAbstract mobSpawnerAbstract = NMSMappings_v1_18_R2.getSpawner(getSpawner());
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

        TileEntityMobSpawner getSpawner() {
            return (TileEntityMobSpawner) NMSMappings_v1_18_R2.getBlockEntity(world, blockPosition);
        }

        private MinecraftKey getMobName() {
            MobSpawnerData mobSpawnerData = NMSMappings_v1_18_R2.getSpawner(getSpawner()).e;
            String id = getString(getEntityToSpawn(mobSpawnerData), "id");
            return UtilColor.b(id) ? null : new MinecraftKey(id);
        }

    }

}
