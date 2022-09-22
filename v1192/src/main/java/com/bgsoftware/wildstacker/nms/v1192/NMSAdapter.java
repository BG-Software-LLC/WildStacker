package com.bgsoftware.wildstacker.nms.v1192;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.nms.algorithms.PaperGlowEnchantment;
import com.bgsoftware.wildstacker.nms.algorithms.SpigotGlowEnchantment;
import com.bgsoftware.wildstacker.nms.v1192.spawner.SyncedCreatureSpawnerImpl;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.utils.chunks.ChunkPosition;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.TurtleEgg;
import org.bukkit.craftbukkit.v1_19_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftChicken;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftExperienceOrb;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPiglin;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftRaider;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftStrider;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftTurtle;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_19_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings("ConstantConditions")
public final class NMSAdapter implements com.bgsoftware.wildstacker.nms.NMSAdapter {

    private static final ReflectField<Integer> ENTITY_EXP = new ReflectField<>(
            Mob.class, int.class, Modifier.PROTECTED, 1);
    private static final ReflectField<Entity.RemovalReason> ENTITY_REMOVE_REASON = new ReflectField<>(
            Entity.class, Entity.RemovalReason.class, Modifier.PRIVATE, 1);

    private static final ReflectField<Integer> LAST_DAMAGE_BY_PLAYER_TIME = new ReflectField<>(LivingEntity.class, int.class, "bd");
    private static final ReflectMethod<Boolean> IS_DROP_EXPERIENCE = new ReflectMethod<>(LivingEntity.class, boolean.class, "dM");
    private static final ReflectMethod<SoundEvent> GET_SOUND_DEATH = new ReflectMethod<>(LivingEntity.class, "x_");
    private static final ReflectMethod<Float> GET_SOUND_VOLUME = new ReflectMethod<>(LivingEntity.class, "eC");
    private static final ReflectMethod<Float> GET_SOUND_PITCH = new ReflectMethod<>(LivingEntity.class, "eD");
    private static final ReflectField<Integer> CHICKEN_EGG_LAY_TIME = new ReflectField<>(Chicken.class, Integer.class, "cd");
    private static final ReflectMethod<Boolean> RAIDER_CAN_RAID = new ReflectMethod<>(Raider.class, boolean.class, "fY");
    private static final ReflectMethod<Raid> RAIDER_RAID = new ReflectMethod<>(Raider.class, Raid.class, "fX");
    private static final ReflectMethod<Void> TURTLE_SET_HAS_EGG = new ReflectMethod<>(Turtle.class, "v", boolean.class);
    private static final ReflectMethod<BlockPos> TURTLE_HOME_POS = new ReflectMethod<>(Turtle.class, "fK");

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static final NamespacedKey STACK_AMOUNT = new NamespacedKey(plugin, "stackAmount");
    private static final NamespacedKey SPAWN_CAUSE = new NamespacedKey(plugin, "spawnCause");
    private static final NamespacedKey NAME_TAG = new NamespacedKey(plugin, "nameTag");
    private static final NamespacedKey UPGRADE = new NamespacedKey(plugin, "upgrade");

    /*
     *   Entity methods
     */

    @Override
    public <T extends org.bukkit.entity.Entity> T createEntity(Location location, Class<T> type,
                                                               SpawnCause spawnCause,
                                                               Consumer<T> beforeSpawnConsumer,
                                                               Consumer<T> afterSpawnConsumer) {
        CraftWorld world = (CraftWorld) location.getWorld();

        assert world != null;

        Entity nmsEntity = world.createEntity(location, type);
        org.bukkit.entity.Entity bukkitEntity = nmsEntity.getBukkitEntity();

        if (beforeSpawnConsumer != null) {
            //noinspection unchecked
            beforeSpawnConsumer.accept((T) bukkitEntity);
        }

        world.addEntity(nmsEntity, spawnCause.toSpawnReason());

        if (EntityUtils.isStackable(bukkitEntity))
            WStackedEntity.of(bukkitEntity).setSpawnCause(spawnCause);

        if (afterSpawnConsumer != null) {
            //noinspection unchecked
            afterSpawnConsumer.accept((T) bukkitEntity);
        }

        return type.cast(bukkitEntity);
    }

    @Override
    public org.bukkit.entity.ExperienceOrb spawnExpOrb(Location location, SpawnCause spawnCause, int value) {
        return createEntity(location, org.bukkit.entity.ExperienceOrb.class, spawnCause, bukkitOrb -> {
            ExperienceOrb orb = ((CraftExperienceOrb) bukkitOrb).getHandle();
            orb.value = value;
            try {
                // Paper only
                orb.spawnReason = org.bukkit.entity.ExperienceOrb.SpawnReason.ENTITY_DEATH;
            } catch (Throwable ignored) {
            }
        }, null);
    }

    @Override
    public Zombie spawnZombieVillager(org.bukkit.entity.Villager bukkitVillager) {
        ServerLevel serverLevel = ((CraftWorld) bukkitVillager.getWorld()).getHandle();

        Villager villager = ((CraftVillager) bukkitVillager).getHandle();
        ZombieVillager zombieVillager = EntityType.ZOMBIE_VILLAGER.create(serverLevel);

        if (zombieVillager == null)
            return null;

        zombieVillager.copyPosition(villager);
        zombieVillager.setVillagerData(villager.getVillagerData());
        zombieVillager.setGossips(villager.getGossips().store(NbtOps.INSTANCE).getValue());
        zombieVillager.setTradeOffers(villager.getOffers().createTag());
        zombieVillager.setVillagerXp(villager.getVillagerXp());
        zombieVillager.setBaby(villager.isBaby());
        zombieVillager.setNoAi(villager.isNoAi());

        if (villager.hasCustomName()) {
            zombieVillager.setCustomName(villager.getCustomName());
            zombieVillager.setCustomNameVisible(villager.isCustomNameVisible());
        }

        EntityTransformEvent entityTransformEvent = new EntityTransformEvent(bukkitVillager,
                Collections.singletonList(zombieVillager.getBukkitEntity()),
                EntityTransformEvent.TransformReason.INFECTION);
        Bukkit.getPluginManager().callEvent(entityTransformEvent);

        if (entityTransformEvent.isCancelled())
            return null;

        serverLevel.addFreshEntity(zombieVillager, CreatureSpawnEvent.SpawnReason.INFECTION);
        BlockPos blockPos = new BlockPos(villager.getX(), villager.getY(), villager.getZ());
        serverLevel.levelEvent(null, 1026, blockPos, 0);

        return (Zombie) villager.getBukkitEntity();
    }

    @Override
    public void setInLove(Animals bukkitAnimal, org.bukkit.entity.Player breeder, boolean inLove) {
        Animal animal = ((CraftAnimals) bukkitAnimal).getHandle();
        if (inLove) {
            ServerPlayer serverPlayer = ((CraftPlayer) breeder).getHandle();
            animal.setInLove(serverPlayer);
        } else {
            animal.resetLove();
        }
    }

    @Override
    public boolean isInLove(Animals bukkitAnimal) {
        return ((CraftAnimals) bukkitAnimal).getHandle().isInLove();
    }

    @Override
    public boolean isAnimalFood(Animals bukkitAnimal, org.bukkit.inventory.ItemStack itemStack) {
        return itemStack != null && ((CraftAnimals) bukkitAnimal).getHandle().isFood(CraftItemStack.asNMSCopy(itemStack));
    }

    @Override
    public int getEntityExp(org.bukkit.entity.LivingEntity bukkitLivingEntity) {
        if (!Bukkit.isPrimaryThread())
            return -1;

        LivingEntity livingEntity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();

        if (!(livingEntity instanceof Mob mob))
            return 0;

        int defaultEntityExp = ENTITY_EXP.get(mob);
        int exp = mob.getExpReward();
        ENTITY_EXP.set(mob, defaultEntityExp);

        return exp;
    }

    @Override
    public boolean canDropExp(org.bukkit.entity.LivingEntity bukkitLivingEntity) {
        LivingEntity livingEntity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
        int lastDamageByPlayerTime;
        boolean isDropExperience;

        try {
            lastDamageByPlayerTime = livingEntity.lastHurtByPlayerTime;
            isDropExperience = livingEntity.shouldDropExperience();
        } catch (Throwable error) {
            lastDamageByPlayerTime = LAST_DAMAGE_BY_PLAYER_TIME.get(livingEntity);
            isDropExperience = IS_DROP_EXPERIENCE.invoke(livingEntity);
        }

        return lastDamageByPlayerTime > 0 && isDropExperience &&
                livingEntity.getLevel().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT);
    }

    @Override
    public void updateLastDamageTime(org.bukkit.entity.LivingEntity bukkitLivingEntity) {
        LivingEntity livingEntity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
        try {
            livingEntity.lastHurtByPlayerTime = 100;
        } catch (Throwable error) {
            LAST_DAMAGE_BY_PLAYER_TIME.set(livingEntity, 100);
        }
    }

    @Override
    public void setHealthDirectly(org.bukkit.entity.LivingEntity bukkitLivingEntity, double health) {
        LivingEntity livingEntity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
        livingEntity.setHealth((float) health);
    }

    @Override
    public void setEntityDead(org.bukkit.entity.LivingEntity bukkitLivingEntity, boolean dead) {
        LivingEntity livingEntity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
        ENTITY_REMOVE_REASON.set(livingEntity, dead ? Entity.RemovalReason.DISCARDED : null);
    }

    @Override
    public int getEggLayTime(org.bukkit.entity.Chicken bukkitChicken) {
        Chicken chicken = ((CraftChicken) bukkitChicken).getHandle();
        try {
            return chicken.eggTime;
        } catch (Throwable error) {
            return CHICKEN_EGG_LAY_TIME.get(chicken);
        }
    }

    @Override
    public void setNerfedEntity(org.bukkit.entity.LivingEntity bukkitLivingEntity, boolean nerfed) {
        LivingEntity livingEntity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();

        if (!(livingEntity instanceof Mob mob))
            return;

        mob.aware = !nerfed;

        try {
            // Only in paper
            mob.spawnedViaMobSpawner = nerfed;
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void setKiller(org.bukkit.entity.LivingEntity bukkitLivingEntity, org.bukkit.entity.Player killer) {
        LivingEntity livingEntity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
        livingEntity.lastHurtByPlayer = killer == null ? null : ((CraftPlayer) killer).getHandle();
    }

    @Override
    public boolean canSpawnOn(org.bukkit.entity.Entity bukkitEntity, Location location) {
        if (location.getWorld() == null)
            return false;

        ServerLevel serverLevel = ((CraftWorld) location.getWorld()).getHandle();
        Entity entity = ((CraftEntity) bukkitEntity).getHandle();
        BlockPos blockPos = new BlockPos(location.getX(), location.getY(), location.getZ());

        return SpawnPlacements.checkSpawnRules(entity.getType(), serverLevel, MobSpawnType.SPAWNER,
                blockPos, serverLevel.getRandom());
    }

    @Override
    public Collection<org.bukkit.entity.Entity> getEntitiesAtChunk(ChunkPosition chunkPosition) {
        return new ArrayList<>();
    }

    @Override
    public Collection<org.bukkit.entity.Entity> getNearbyEntities(Location location, int range, Predicate<org.bukkit.entity.Entity> filter) {
        if (location.getWorld() == null)
            return Collections.emptyList();

        ServerLevel serverLevel = ((CraftWorld) location.getWorld()).getHandle();
        List<org.bukkit.entity.Entity> entities = new LinkedList<>();

        AABB searchArea = new AABB(
                location.getBlockX() - range,
                location.getBlockY() - range,
                location.getBlockZ() - range,
                location.getBlockX() + range,
                location.getBlockY() + range,
                location.getBlockZ() + range
        );

        serverLevel.getEntities().get(searchArea, entity -> {
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
    public boolean shouldArmorBeDamaged(org.bukkit.inventory.ItemStack bukkitItem) {
        return bukkitItem != null && CraftItemStack.asNMSCopy(bukkitItem).isDamageableItem();
    }

    @Override
    public boolean doesStriderHaveSaddle(org.bukkit.entity.Strider bukkitStrider) {
        try {
            return bukkitStrider.hasSaddle();
        } catch (Throwable error) {
            Strider strider = ((CraftStrider) bukkitStrider).getHandle();
            return strider.isSaddled();
        }
    }

    @Override
    public void removeStriderSaddle(org.bukkit.entity.Strider bukkitStrider) {
        try {
            bukkitStrider.setSaddle(false);
        } catch (Throwable error) {
            Strider strider = ((CraftStrider) bukkitStrider).getHandle();
            strider.steering.setSaddle(false);
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
    public void setTurtleEgg(org.bukkit.entity.Entity bukkitTurtle) {
        try {
            ((org.bukkit.entity.Turtle) bukkitTurtle).setHasEgg(true);
        } catch (Throwable ex) {
            Turtle turtle = ((CraftTurtle) bukkitTurtle).getHandle();
            TURTLE_SET_HAS_EGG.invoke(turtle, true);
        }
    }

    @Override
    public Location getTurtleHome(org.bukkit.entity.Entity bukkitTurtle) {
        try {
            return ((org.bukkit.entity.Turtle) bukkitTurtle).getHome();
        } catch (Throwable ex) {
            Turtle turtle = ((CraftTurtle) bukkitTurtle).getHandle();
            BlockPos blockPos = TURTLE_HOME_POS.invoke(turtle);
            return new Location(bukkitTurtle.getWorld(), blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }
    }

    @Override
    public void setTurtleEggsAmount(Block turtleEggBlock, int amount) {
        TurtleEgg turtleEgg = (TurtleEgg) turtleEggBlock.getBlockData();
        turtleEgg.setEggs(amount);
        turtleEggBlock.setBlockData(turtleEgg, true);
    }

    @Override
    public void handleSweepingEdge(org.bukkit.entity.Player attacker, org.bukkit.inventory.ItemStack usedItem,
                                   org.bukkit.entity.LivingEntity bukkitLivingEntity, double damage) {
        LivingEntity livingEntity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
        ServerPlayer serverPlayer = ((CraftPlayer) attacker).getHandle();
        ItemStack itemStack = CraftItemStack.asNMSCopy(usedItem);

        // Making sure the player used a sword.
        if (!(itemStack.getItem() instanceof SwordItem))
            return;

        float sweepDamage = 1.0F + EnchantmentHelper.getSweepingDamageRatio(serverPlayer) * (float) damage;
        List<LivingEntity> nearbyEntities = livingEntity.getLevel().getEntitiesOfClass(LivingEntity.class,
                livingEntity.getBoundingBox().inflate(1.0D, 0.25D, 1.0D));

        for (LivingEntity nearby : nearbyEntities) {
            if (nearby != livingEntity && nearby != serverPlayer && !serverPlayer.skipAttackInteraction(nearby) &&
                    (!(nearby instanceof ArmorStand armorStand) || armorStand.isMarker()) &&
                    serverPlayer.distanceToSqr(nearby) < 9.0D) {
                nearby.hurt(DamageSource.playerAttack(serverPlayer).sweep(), sweepDamage);
            }
        }
    }

    @Override
    public String getCustomName(org.bukkit.entity.Entity bukkitEntity) {
        Entity entity = ((CraftEntity) bukkitEntity).getHandle();
        // Much more optimized way than Bukkit's method.
        Component component = entity.getCustomName();
        return component == null ? "" : component.getString();
    }

    @Override
    public void setCustomName(org.bukkit.entity.Entity entity, String name) {
        entity.setCustomName(name);
    }

    /*
     *   Spawner methods
     */

    @Override
    public boolean handleTotemOfUndying(org.bukkit.entity.LivingEntity bukkitLivingEntity) {
        LivingEntity livingEntity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();

        ItemStack totemOfUndying = ItemStack.EMPTY;

        for (InteractionHand interactionHand : InteractionHand.values()) {
            ItemStack handItem = livingEntity.getItemInHand(interactionHand);
            if (handItem.getItem() == Items.TOTEM_OF_UNDYING) {
                totemOfUndying = handItem;
                break;
            }
        }

        EntityResurrectEvent event = new EntityResurrectEvent(bukkitLivingEntity);
        event.setCancelled(totemOfUndying.isEmpty());
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return false;

        if (!totemOfUndying.isEmpty()) {
            totemOfUndying.shrink(1);

            if (livingEntity instanceof ServerPlayer serverPlayer) {
                serverPlayer.awardStat(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
                CriteriaTriggers.USED_TOTEM.trigger(serverPlayer, totemOfUndying);
            }

            livingEntity.setHealth(1.0F);
            livingEntity.removeAllEffects(EntityPotionEffectEvent.Cause.TOTEM);
            livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1), EntityPotionEffectEvent.Cause.TOTEM);
            livingEntity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1), EntityPotionEffectEvent.Cause.TOTEM);
            livingEntity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0), EntityPotionEffectEvent.Cause.TOTEM);
            livingEntity.getLevel().broadcastEntityEvent(livingEntity, (byte) 35);
        }

        return true;
    }

    @Override
    public void sendEntityDieEvent(org.bukkit.entity.LivingEntity bukkitLivingEntity) {
        LivingEntity livingEntity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
        livingEntity.gameEvent(GameEvent.ENTITY_DIE);
    }

    @Override
    public boolean callEntityBreedEvent(org.bukkit.entity.LivingEntity child,
                                        org.bukkit.entity.LivingEntity mother,
                                        org.bukkit.entity.LivingEntity father,
                                        @Nullable org.bukkit.entity.LivingEntity breeder,
                                        @Nullable org.bukkit.inventory.ItemStack bredWith,
                                        int experience) {
        EntityBreedEvent entityBreedEvent = new EntityBreedEvent(child, mother, father, breeder, bredWith, experience);
        Bukkit.getPluginManager().callEvent(entityBreedEvent);
        return !entityBreedEvent.isCancelled();
    }

    @Override
    public SyncedCreatureSpawner createSyncedSpawner(CreatureSpawner creatureSpawner) {
        World bukkitWorld = creatureSpawner.getWorld();
        ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();
        BlockPos blockPos = new BlockPos(creatureSpawner.getX(), creatureSpawner.getY(), creatureSpawner.getZ());
        SpawnerBlockEntity spawnerBlockEntity = (SpawnerBlockEntity) serverLevel.getBlockEntity(blockPos);
        return new SyncedCreatureSpawnerImpl(bukkitWorld, spawnerBlockEntity);
    }

    /*
     *   Item methods
     */

    @Override
    public boolean isRotatable(Block block) {
        BlockState blockState = ((CraftBlock) block).getNMS();
        return blockState.getBlock() instanceof RotatedPillarBlock;
    }

    @Override
    public StackedItem createItem(Location location, org.bukkit.inventory.ItemStack itemStack, SpawnCause spawnCause, Consumer<StackedItem> itemConsumer) {
        CraftWorld craftWorld = (CraftWorld) location.getWorld();

        ItemEntity entityItem = new ItemEntity(craftWorld.getHandle(),
                location.getX(), location.getY(), location.getZ(),
                CraftItemStack.asNMSCopy(itemStack));

        entityItem.pickupDelay = 10;

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
    public Enchantment getGlowEnchant() {
        try {
            return new PaperGlowEnchantment("wildstacker_glowing_enchant");
        } catch (Throwable error) {
            return new SpigotGlowEnchantment("wildstacker_glowing_enchant");
        }
    }

    /*
     *   World methods
     */

    @Override
    public org.bukkit.inventory.ItemStack getPlayerSkull(org.bukkit.inventory.ItemStack bukkitItem, String texture) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);
        CompoundTag compoundTag = itemStack.getOrCreateTag();

        CompoundTag skullOwner = compoundTag.contains("SkullOwner") ?
                compoundTag.getCompound("SkullOwner") : new CompoundTag();

        skullOwner.putString("Id", new UUID(texture.hashCode(), texture.hashCode()).toString());

        CompoundTag properties = new CompoundTag();
        ListTag textures = new ListTag();
        CompoundTag signature = new CompoundTag();

        signature.putString("Value", texture);
        textures.add(signature);

        properties.put("textures", textures);

        skullOwner.put("Properties", properties);

        compoundTag.put("SkullOwner", skullOwner);

        return CraftItemStack.asBukkitCopy(itemStack);
    }

    @Override
    public void awardKillScore(org.bukkit.entity.Entity bukkitDamaged,
                               org.bukkit.entity.Entity damagerEntity) {
        Entity damaged = ((CraftEntity) bukkitDamaged).getHandle();
        Entity damager = ((CraftEntity) damagerEntity).getHandle();

        DamageSource damageSource;

        if (damager instanceof Player player) {
            damageSource = DamageSource.playerAttack(player);
        } else if (damager instanceof AbstractArrow abstractArrow) {
            damageSource = DamageSource.arrow(abstractArrow, abstractArrow.getOwner());
        } else if (damager instanceof ThrownTrident thrownTrident) {
            damageSource = DamageSource.trident(damager, thrownTrident.getOwner());
        } else if (damager instanceof Fireball fireball) {
            damageSource = DamageSource.fireball(fireball, fireball.getOwner());
        } else if (damager instanceof LivingEntity livingEntity) {
            damageSource = DamageSource.mobAttack(livingEntity);
        } else {
            return;
        }

        damager.awardKillScore(damaged, 0, damageSource);
    }

    @Override
    public void playPickupAnimation(org.bukkit.entity.LivingEntity bukkitLivingEntity, org.bukkit.entity.Item item) {
        LivingEntity livingEntity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
        ItemEntity itemEntity = (ItemEntity) ((CraftItem) item).getHandle();

        ServerChunkCache serverChunkCache = (ServerChunkCache) livingEntity.getLevel().getChunkSource();

        ClientboundTakeItemEntityPacket takeItemEntityPacket = new ClientboundTakeItemEntityPacket(
                itemEntity.getId(), livingEntity.getId(), item.getItemStack().getAmount());
        ClientboundAddEntityPacket addEntityPacket = new ClientboundAddEntityPacket(itemEntity);
        ClientboundSetEntityDataPacket setEntityDataPacket = new ClientboundSetEntityDataPacket(
                itemEntity.getId(), itemEntity.getEntityData(), true);

        serverChunkCache.broadcast(itemEntity, takeItemEntityPacket);
        serverChunkCache.broadcast(itemEntity, addEntityPacket);
        serverChunkCache.broadcast(itemEntity, setEntityDataPacket);
    }

    @Override
    public void playDeathSound(org.bukkit.entity.LivingEntity bukkitLivingEntity) {
        LivingEntity livingEntity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
        SoundEvent deathSound;
        float soundVolume;
        float soundPitch;

        try {
            deathSound = livingEntity.getDeathSound();
            soundVolume = livingEntity.getSoundVolume();
            soundPitch = livingEntity.getVoicePitch();
        } catch (Throwable error) {
            deathSound = GET_SOUND_DEATH.invoke(livingEntity);
            soundVolume = GET_SOUND_VOLUME.invoke(livingEntity);
            soundPitch = GET_SOUND_PITCH.invoke(livingEntity);
        }


        if (deathSound != null)
            livingEntity.playSound(deathSound, soundVolume, soundPitch);
    }

    @Override
    public void playParticle(String particle, Location location, int count, int offsetX, int offsetY, int offsetZ, double extra) {
        World world = location.getWorld();
        if (world != null) {
            ServerLevel serverLevel = ((CraftWorld) world).getHandle();
            serverLevel.sendParticles(null,
                    CraftParticle.toNMS(Particle.valueOf(particle)),
                    location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                    count, offsetX, offsetY, offsetZ, extra, false);
        }
    }

    @Override
    public void playSpawnEffect(org.bukkit.entity.LivingEntity bukkitLivingEntity) {
        LivingEntity livingEntity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
        if (livingEntity instanceof Mob mob)
            mob.spawnAnim();
    }

    @Override
    public Object getBlockData(Material type, short data) {
        return CraftBlockData.fromData(CraftMagicNumbers.getBlock(type, (byte) data));
    }

    @Override
    public void attemptJoinRaid(org.bukkit.entity.Player player, org.bukkit.entity.Entity bukkitRaider) {
        Raider raider = ((CraftRaider) bukkitRaider).getHandle();
        boolean hasActiveRaid;
        Raid raid;

        try {
            hasActiveRaid = raider.hasActiveRaid();
            raid = raider.getCurrentRaid();
        } catch (Throwable error) {
            hasActiveRaid = RAIDER_CAN_RAID.invoke(raider);
            raid = RAIDER_RAID.invoke(raider);
        }

        if (hasActiveRaid)
            raid.addHeroOfTheVillage(((CraftPlayer) player).getHandle());
    }

    @Override
    public boolean handlePiglinPickup(org.bukkit.entity.Entity bukkitPiglin, org.bukkit.entity.Item bukkitItem) {
        if (!(bukkitPiglin instanceof org.bukkit.entity.Piglin))
            return false;

        Piglin piglin = ((CraftPiglin) bukkitPiglin).getHandle();
        ItemEntity itemEntity = (ItemEntity) ((CraftItem) bukkitItem).getHandle();

        piglin.onItemPickup(itemEntity);

        Brain<Piglin> brain = piglin.getBrain();

        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        piglin.getNavigation().stop();

        piglin.take(itemEntity, 1);

        ItemStack itemStack = itemEntity.getItem().copy();
        itemStack.setCount(1);
        Item item = itemStack.getItem();

        if (item.builtInRegistryHolder().is(ItemTags.PIGLIN_LOVED)) {
            brain.eraseMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
            if (!piglin.getOffhandItem().isEmpty())
                piglin.spawnAtLocation(piglin.getItemInHand(InteractionHand.OFF_HAND));

            piglin.setItemSlot(EquipmentSlot.OFFHAND, itemStack);
            piglin.setGuaranteedDrop(EquipmentSlot.OFFHAND);

            if (item != PiglinAi.BARTERING_ITEM)
                piglin.setPersistenceRequired();

            brain.setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, true, 120L);
        } else if ((item == Items.PORKCHOP || item == Items.COOKED_PORKCHOP) && !brain.hasMemoryValue(MemoryModuleType.ATE_RECENTLY)) {
            brain.setMemoryWithExpiry(MemoryModuleType.ATE_RECENTLY, true, 200L);
        } else {
            handleEquipmentPickup((org.bukkit.entity.LivingEntity) bukkitPiglin, bukkitItem);
        }

        return true;
    }

    @Override
    public boolean handleEquipmentPickup(org.bukkit.entity.LivingEntity bukkitLivingEntity, org.bukkit.entity.Item bukkitItem) {
        if (bukkitLivingEntity instanceof org.bukkit.entity.Player)
            return false;

        LivingEntity livingEntity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();

        if (!(livingEntity instanceof Mob mob))
            return false;

        ItemEntity itemEntity = (ItemEntity) ((CraftItem) bukkitItem).getHandle();
        ItemStack itemStack = itemEntity.getItem().copy();
        itemStack.setCount(1);

        EquipmentSlot equipmentSlotForItem = LivingEntity.getEquipmentSlotForItem(itemStack);

        if (equipmentSlotForItem.getType() != EquipmentSlot.Type.ARMOR)
            return false;

        ItemStack equipmentItem = mob.getItemBySlot(equipmentSlotForItem);
        double equipmentDropChance = mob.armorDropChances[equipmentSlotForItem.getIndex()];

        Random random = new Random();
        if (!equipmentItem.isEmpty() && Math.max(random.nextFloat() - 0.1F, 0.0F) < equipmentDropChance) {
            mob.forceDrops = true;
            mob.spawnAtLocation(equipmentItem);
            mob.forceDrops = false;
        }

        mob.setItemSlot(equipmentSlotForItem, itemStack);
        mob.setGuaranteedDrop(equipmentSlotForItem);

        SoundEvent equipSound = itemStack.getEquipSound();
        if (!itemStack.isEmpty() && equipSound != null) {
            mob.gameEvent(GameEvent.EQUIP);
            mob.playSound(equipSound, 1.0F, 1.0F);
        }

        return true;
    }

    @Override
    public void giveExp(org.bukkit.entity.Player bukkitPlayer, int amount) {
        ServerPlayer serverPlayer = ((CraftPlayer) bukkitPlayer).getHandle();

        Map.Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.getRandomItemWith(Enchantments.MENDING, serverPlayer);
        ItemStack mendingItem = entry != null ? entry.getValue() : ItemStack.EMPTY;

        if (!mendingItem.isEmpty() && mendingItem.getItem().canBeDepleted()) {
            ExperienceOrb experienceOrb = EntityType.EXPERIENCE_ORB.create(serverPlayer.getLevel());
            experienceOrb.value = amount;

            try {
                // Paper
                experienceOrb.spawnReason = org.bukkit.entity.ExperienceOrb.SpawnReason.ENTITY_DEATH;
            } catch (Throwable ignored) {
            }

            experienceOrb.setPosRaw(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ());
            int repairAmount = Math.min(amount * 2, mendingItem.getDamageValue());
            PlayerItemMendEvent event = CraftEventFactory.callPlayerItemMendEvent(serverPlayer, experienceOrb, mendingItem, repairAmount);
            repairAmount = event.getRepairAmount();

            experienceOrb.discard();

            if (!event.isCancelled()) {
                amount -= (repairAmount / 2);
                mendingItem.setDamageValue(mendingItem.getDamageValue() - repairAmount);
            }
        }

        if (amount > 0) {
            PlayerExpChangeEvent playerExpChangeEvent = new PlayerExpChangeEvent(bukkitPlayer, amount);
            Bukkit.getPluginManager().callEvent(playerExpChangeEvent);
            if (playerExpChangeEvent.getAmount() > 0)
                bukkitPlayer.giveExp(playerExpChangeEvent.getAmount());
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
    public void updateEntity(org.bukkit.entity.LivingEntity sourceBukkit, org.bukkit.entity.LivingEntity targetBukkit) {
        LivingEntity source = ((CraftLivingEntity) sourceBukkit).getHandle();
        LivingEntity target = ((CraftLivingEntity) targetBukkit).getHandle();

        CompoundTag compoundTag = new CompoundTag();
        source.addAdditionalSaveData(compoundTag);

        compoundTag.putFloat("Health", source.getMaxHealth());
        compoundTag.remove("SaddleItem");
        compoundTag.remove("Saddle");
        compoundTag.remove("ArmorItem");
        compoundTag.remove("ArmorItems");
        compoundTag.remove("HandItems");
        compoundTag.remove("Leash");

        if (targetBukkit instanceof Zombie) {
            //noinspection deprecation
            ((Zombie) targetBukkit).setBaby(compoundTag.contains("IsBaby") && compoundTag.getBoolean("IsBaby"));
        }

        target.readAdditionalSaveData(compoundTag);
    }

    @Override
    public String serialize(org.bukkit.inventory.ItemStack bukkitItem) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new DataOutputStream(outputStream);

        CompoundTag compoundTag = new CompoundTag();

        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);
        itemStack.save(compoundTag);

        try {
            NbtIo.write(compoundTag, dataOutput);
        } catch (Exception ex) {
            return null;
        }

        return new BigInteger(1, outputStream.toByteArray()).toString(32);
    }

    @Override
    public org.bukkit.inventory.ItemStack deserialize(String serialized) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(serialized, 32).toByteArray());

        try {
            CompoundTag compoundTag = NbtIo.read(new DataInputStream(inputStream), NbtAccounter.UNLIMITED);
            ItemStack itemStack = ItemStack.of(compoundTag);
            return CraftItemStack.asBukkitCopy(itemStack);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack bukkitItem, String key, Object value) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);
        CompoundTag compoundTag = itemStack.getOrCreateTag();

        if (value instanceof Boolean)
            compoundTag.putBoolean(key, (boolean) value);
        else if (value instanceof Integer)
            compoundTag.putInt(key, (int) value);
        else if (value instanceof String)
            compoundTag.putString(key, (String) value);
        else if (value instanceof Double)
            compoundTag.putDouble(key, (double) value);
        else if (value instanceof Short)
            compoundTag.putShort(key, (short) value);
        else if (value instanceof Byte)
            compoundTag.putByte(key, (byte) value);
        else if (value instanceof Float)
            compoundTag.putFloat(key, (float) value);
        else if (value instanceof Long)
            compoundTag.putLong(key, (long) value);

        return CraftItemStack.asBukkitCopy(itemStack);
    }

    @Override
    public <T> T getTag(org.bukkit.inventory.ItemStack bukkitItem, String key, Class<T> valueType, Object def) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);

        if (itemStack == null || itemStack.isEmpty())
            return valueType.cast(def);

        CompoundTag compoundTag = itemStack.getTag();

        if (compoundTag == null || !compoundTag.contains(key))
            return valueType.cast(def);
        else if (valueType.equals(Boolean.class))
            return valueType.cast(compoundTag.getBoolean(key));
        else if (valueType.equals(Integer.class))
            return valueType.cast(compoundTag.getInt(key));
        else if (valueType.equals(String.class))
            return valueType.cast(compoundTag.getString(key));
        else if (valueType.equals(Double.class))
            return valueType.cast(compoundTag.getDouble(key));
        else if (valueType.equals(Short.class))
            return valueType.cast(compoundTag.getShort(key));
        else if (valueType.equals(Byte.class))
            return valueType.cast(compoundTag.getByte(key));
        else if (valueType.equals(Float.class))
            return valueType.cast(compoundTag.getFloat(key));
        else if (valueType.equals(Long.class))
            return valueType.cast(compoundTag.getLong(key));

        throw new IllegalArgumentException("Cannot find nbt class type: " + valueType);
    }

    /*
     *   Other methods
     */

    @Override
    public Object getChatMessage(String message) {
        return Component.nullToEmpty(message);
    }

    /*
     *   Data methods
     */

    @Override
    public void saveEntity(StackedEntity stackedEntity) {
        org.bukkit.entity.LivingEntity livingEntity = stackedEntity.getLivingEntity();
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
        org.bukkit.entity.LivingEntity livingEntity = stackedEntity.getLivingEntity();
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

}
