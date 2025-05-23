package com.bgsoftware.wildstacker.nms.v1_19;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.listeners.EntitiesListener;
import com.bgsoftware.wildstacker.nms.NMSEntities;
import com.bgsoftware.wildstacker.nms.entity.IEntityWrapper;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.entity.StackCheck;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftChicken;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftExperienceOrb;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftItem;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftStrider;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftTurtle;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Item;
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

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class NMSEntitiesImpl implements NMSEntities {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static final ReflectField<Integer> ENTITY_EXP = new ReflectField<>(
            Mob.class, int.class, Modifier.PROTECTED, 1);
    private static final ReflectField<Entity.RemovalReason> ENTITY_REMOVE_REASON = new ReflectField<>(
            Entity.class, Entity.RemovalReason.class, Modifier.PRIVATE, 1);

    private static final ReflectField<Integer> LAST_DAMAGE_BY_PLAYER_TIME = new ReflectField<>(LivingEntity.class, int.class, "aY");
    private static final ReflectField<Boolean> ENTITY_LIVING_DEAD = new ReflectField<>(LivingEntity.class, boolean.class, "aZ");
    private static final ReflectMethod<Boolean> IS_DROP_EXPERIENCE = new ReflectMethod<>(LivingEntity.class, boolean.class, "dV");
    private static final ReflectMethod<SoundEvent> GET_SOUND_DEATH = new ReflectMethod<>(LivingEntity.class, "x_");
    private static final ReflectMethod<Float> GET_SOUND_VOLUME = new ReflectMethod<>(LivingEntity.class, "eN");
    private static final ReflectMethod<Float> GET_SOUND_PITCH = new ReflectMethod<>(LivingEntity.class, "eO");
    private static final ReflectField<Integer> CHICKEN_EGG_LAY_TIME = new ReflectField<>(Chicken.class, Integer.class, "bY");
    private static final ReflectMethod<Void> TURTLE_SET_HAS_EGG = new ReflectMethod<>(Turtle.class, "w", boolean.class);
    private static final ReflectMethod<BlockPos> TURTLE_HOME_POS = new ReflectMethod<>(Turtle.class, "fS");
    private static final ReflectMethod<Void> MOB_PICK_UP_ITEM = new ReflectMethod<>(Mob.class, "b", ItemEntity.class);
    private static final ReflectMethod<SynchedEntityData.DataItem<?>> ENTITY_DATA_GET_ITEM = new ReflectMethod<>(SynchedEntityData.class, "b", EntityDataAccessor.class);


    @Override
    public <T extends org.bukkit.entity.Entity> T createEntity(Location location, Class<T> type,
                                                               SpawnCause spawnCause,
                                                               Predicate<org.bukkit.entity.Entity> beforeSpawnConsumer,
                                                               Consumer<org.bukkit.entity.Entity> afterSpawnConsumer) {
        CraftWorld world = (CraftWorld) location.getWorld();

        assert world != null;

        Entity nmsEntity = world.createEntity(location, type);
        //noinspection unchecked
        T bukkitEntity = (T) nmsEntity.getBukkitEntity();

        if (beforeSpawnConsumer != null && !beforeSpawnConsumer.test(bukkitEntity)) {
            return null;
        }

        world.addEntity(nmsEntity, spawnCause.toSpawnReason());

        if (EntityUtils.isStackable(bukkitEntity))
            WStackedEntity.of(bukkitEntity).setSpawnCause(spawnCause);

        if (afterSpawnConsumer != null) {
            afterSpawnConsumer.accept(bukkitEntity);
        }

        return type.cast(bukkitEntity);
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

        StackedItem stackedItem = WStackedItem.ofBypass((Item) entityItem.getBukkitEntity());

        itemConsumer.accept(stackedItem);

        craftWorld.addEntity(entityItem, spawnCause.toSpawnReason());

        return stackedItem;
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
            return true;
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
        zombieVillager.setGossips(villager.getGossips().store(NbtOps.INSTANCE));
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
        BlockPos blockPos = new BlockPos(villager.getBlockX(), villager.getBlockY(), villager.getBlockZ());
        serverLevel.levelEvent(null, 1026, blockPos, 0);

        return (Zombie) zombieVillager.getBukkitEntity();
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
    public void setHealthDirectly(org.bukkit.entity.LivingEntity bukkitLivingEntity, double health, boolean preventUpdate) {
        LivingEntity livingEntity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
        SynchedEntityData entityData = livingEntity.getEntityData();
        livingEntity.setHealth((float) health);
        if (preventUpdate) {
            SynchedEntityData.DataItem<?> dataItem = ENTITY_DATA_GET_ITEM.invoke(entityData, LivingEntity.DATA_HEALTH_ID);
            if (dataItem != null)
                dataItem.setDirty(false);
        } else {
            // We make sure health is marked dirty
            entityData.markDirty(LivingEntity.DATA_HEALTH_ID);
        }
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
    public String getEndermanCarried(Enderman enderman) {
        BlockData carriedData = enderman.getCarriedBlock();
        return carriedData == null ? "AIR" : carriedData.getMaterial() + "";
    }

    @Override
    public byte getMooshroomType(MushroomCow mushroomCow) {
        return (byte) mushroomCow.getVariant().ordinal();
    }

    @Override
    public boolean doesStriderHaveSaddle(org.bukkit.entity.Entity bukkitStrider) {
        try {
            return ((org.bukkit.entity.Strider) bukkitStrider).hasSaddle();
        } catch (Throwable error) {
            Strider strider = ((CraftStrider) bukkitStrider).getHandle();
            return strider.isSaddled();
        }
    }

    @Override
    public void removeStriderSaddle(org.bukkit.entity.Entity bukkitStrider) {
        try {
            ((org.bukkit.entity.Strider) bukkitStrider).setSaddle(false);
        } catch (Throwable error) {
            Strider strider = ((CraftStrider) bukkitStrider).getHandle();
            strider.steering.setSaddle(false);
        }
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
    public StackCheckResult areSimilar(EntityTypes entityType, org.bukkit.entity.LivingEntity en1,
                                       org.bukkit.entity.LivingEntity en2) {
        if (StackCheck.FROG_TOUNGE_TARGET.isEnabled() && StackCheck.FROG_TOUNGE_TARGET.isTypeAllowed(entityType)) {
            if (!Objects.equals(((Frog) en1).getTongueTarget(), ((Frog) en2).getTongueTarget()))
                return StackCheckResult.FROG_TOUNGE_TARGET;
        }

        if (StackCheck.FROG_TYPE.isEnabled() && StackCheck.FROG_TYPE.isTypeAllowed(entityType)) {
            if (((Frog) en1).getVariant() != ((Frog) en2).getVariant())
                return StackCheckResult.FROG_TYPE;
        }

        return StackCheckResult.SUCCESS;
    }

    @Override
    public boolean checkEntityAttributes(org.bukkit.entity.LivingEntity bukkitEntity, Map<String, Object> attributes) {
        LivingEntity livingEntity = ((CraftLivingEntity) bukkitEntity).getHandle();
        CompoundTag entityCompound = new CompoundTag();
        livingEntity.addAdditionalSaveData(entityCompound);

        for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
            Tag tag = entityCompound.get(attribute.getKey());
            if (tag instanceof NumericTag) {
                if (!Objects.equals(attribute.getValue(), ((NumericTag) tag).getAsNumber()))
                    return false;
            } else if (tag instanceof StringTag) {
                if (!Objects.equals(attribute.getValue(), tag.getAsString()))
                    return false;
            } else {
                return false;
            }
        }

        return true;
    }

    @Override
    public void awardKillScore(org.bukkit.entity.Player playerKiller,
                               org.bukkit.entity.Entity bukkitDamaged,
                               org.bukkit.entity.Entity directDamager) {
        ServerPlayer serverPlayer = ((CraftPlayer) playerKiller).getHandle();
        Entity damaged = ((CraftEntity) bukkitDamaged).getHandle();
        Entity damager = ((CraftEntity) directDamager).getHandle();

        DamageSource damageSource;

        if (damager instanceof Player player) {
            damageSource = damaged.damageSources().playerAttack(player);
        } else if (damager instanceof ThrownTrident thrownTrident) {
            damageSource = damaged.damageSources().trident(damager, thrownTrident.getOwner());
        } else if (damager instanceof AbstractArrow abstractArrow) {
            damageSource = damaged.damageSources().arrow(abstractArrow, abstractArrow.getOwner());
        } else if (damager instanceof Fireball fireball) {
            damageSource = damaged.damageSources().fireball(fireball, fireball.getOwner());
        } else if (damager instanceof LivingEntity livingEntity) {
            damageSource = damaged.damageSources().mobAttack(livingEntity);
        } else {
            return;
        }

        serverPlayer.awardKillScore(damaged, 0, damageSource);
    }

    @Override
    public void awardPickupScore(org.bukkit.entity.Player player, Item pickItem) {
        // Do nothing.
    }

    @Override
    public void awardCrossbowShot(org.bukkit.entity.Player player, org.bukkit.entity.LivingEntity target,
                                  org.bukkit.inventory.ItemStack unused) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        LivingEntity targetEntity = ((CraftLivingEntity) target).getHandle();
        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverPlayer, Arrays.asList(targetEntity));
    }

    @Override
    public void playPickupAnimation(org.bukkit.entity.LivingEntity bukkitLivingEntity, Item item) {
        LivingEntity livingEntity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
        ItemEntity itemEntity = (ItemEntity) ((CraftItem) item).getHandle();

        ServerChunkCache serverChunkCache = (ServerChunkCache) livingEntity.getLevel().getChunkSource();

        ClientboundTakeItemEntityPacket takeItemEntityPacket = new ClientboundTakeItemEntityPacket(
                itemEntity.getId(), livingEntity.getId(), item.getItemStack().getAmount());
        ClientboundAddEntityPacket addEntityPacket = new ClientboundAddEntityPacket(itemEntity);

        serverChunkCache.broadcast(itemEntity, takeItemEntityPacket);
        serverChunkCache.broadcast(itemEntity, addEntityPacket);

        List<SynchedEntityData.DataValue<?>> packedData = itemEntity.getEntityData().packDirty();
        if (packedData != null) {
            ClientboundSetEntityDataPacket setEntityDataPacket = new ClientboundSetEntityDataPacket(itemEntity.getId(), packedData);
            serverChunkCache.broadcast(itemEntity, setEntityDataPacket);
        }
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
    public void playSpawnEffect(org.bukkit.entity.LivingEntity bukkitLivingEntity) {
        LivingEntity livingEntity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
        if (livingEntity instanceof Mob mob)
            mob.spawnAnim();
    }

    @Override
    public void handleItemPickup(org.bukkit.entity.LivingEntity bukkitLivingEntity, StackedItem stackedItem, int remaining) {
        LivingEntity livingEntity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
        boolean isPlayerPickup = livingEntity instanceof Player;

        if (!isPlayerPickup && !(livingEntity instanceof Mob))
            return;

        ItemEntity itemEntity = (ItemEntity) ((CraftItem) stackedItem.getItem()).getHandle();

        int stackAmount = stackedItem.getStackAmount();
        int maxStackSize = itemEntity.getItem().getMaxStackSize();
        boolean retryPickup = false;

        ItemEntity pickupItem;
        if (stackAmount <= maxStackSize) {
            // If the stack size is not larger than vanilla, we can safely pickup the original item.
            pickupItem = itemEntity;
        } else {
            // In case the stack size is larger than vanilla's max stack size, we want to simulate pickup
            // of a max stack size item instead. In case it's a player picking up the item, we want the item to have
            // the real count.
            ItemStack itemStack = itemEntity.getItem().copy();

            if (isPlayerPickup || livingEntity instanceof Fox) {
                if (plugin.getSettings().itemsFixStackEnabled) {
                    itemStack.setCount(maxStackSize);
                    retryPickup = true;
                } else {
                    itemStack.setCount(stackAmount);
                }
            } else {
                itemStack.setCount(maxStackSize);
            }

            pickupItem = new ItemEntity(itemEntity.level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), itemStack);
        }

        int originalItemCount = pickupItem.getItem().getCount();
        int originalPickupDelay = itemEntity.pickupDelay;
        boolean isDifferentPickupItem = pickupItem != itemEntity;
        boolean actualItemDupe = originalItemCount != stackAmount;

        try {
            if (isDifferentPickupItem) itemEntity.setNeverPickUp();
            EntitiesListener.IMP.secondPickupEventCall = true;
            EntitiesListener.IMP.secondPickupEvent = null;
            if (isPlayerPickup) {
                pickupItem.playerTouch((Player) livingEntity);
            } else {
                MOB_PICK_UP_ITEM.invoke(livingEntity, pickupItem);
            }
        } finally {
            if (isDifferentPickupItem) itemEntity.pickupDelay = originalPickupDelay;
            EntitiesListener.IMP.secondPickupEventCall = false;
            EntitiesListener.IMP.secondPickupEvent = null;
        }

        int pickupCount = originalItemCount - (pickupItem.isAlive() ? pickupItem.getItem().getCount() : 0);

        if (pickupCount > 0) {
            stackedItem.decreaseStackAmount(pickupCount, true);
            if (retryPickup) {
                handleItemPickup(bukkitLivingEntity, stackedItem, 0);
            } else {
                itemEntity.setDefaultPickUpDelay();
            }
        }

        if (!actualItemDupe && isDifferentPickupItem) {
            livingEntity.onItemPickup(itemEntity);

            int simulatedItemPickupCount = Math.min(pickupCount, maxStackSize);

            if (pickupCount < originalItemCount) {
                // Need to simulate item pickup
                ItemEntity simulatedEntityItemPickup = new ItemEntity(pickupItem.level,
                        pickupItem.getX(), pickupItem.getY(), pickupItem.getZ(),
                        pickupItem.getItem().copyWithCount(simulatedItemPickupCount));

                List<SynchedEntityData.DataValue<?>> entityData = simulatedEntityItemPickup.getEntityData().packDirty();

                ServerChunkCache serverChunkCache = ((ServerLevel) simulatedEntityItemPickup.level).getChunkSource();
                serverChunkCache.broadcastAndSend(livingEntity, simulatedEntityItemPickup.getAddEntityPacket());
                if (entityData != null) {
                    serverChunkCache.broadcastAndSend(livingEntity, new ClientboundSetEntityDataPacket(
                            simulatedEntityItemPickup.getId(), entityData));
                }
                serverChunkCache.broadcastAndSend(livingEntity, new ClientboundTakeItemEntityPacket(
                        simulatedEntityItemPickup.getId(), livingEntity.getId(), simulatedItemPickupCount));
                serverChunkCache.broadcastAndSend(livingEntity, new ClientboundRemoveEntitiesPacket(
                        simulatedEntityItemPickup.getId()));
            } else {
                livingEntity.take(itemEntity, simulatedItemPickupCount);
            }

            if (!pickupItem.isAlive())
                itemEntity.discard();
        }
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
                nearby.hurt(nearby.damageSources().playerAttack(serverPlayer).sweep(), sweepDamage);
            }
        }
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
            PlayerItemMendEvent event = new PlayerItemMendEvent(serverPlayer.getBukkitEntity(),
                    CraftItemStack.asBukkitCopy(mendingItem),
                    (org.bukkit.entity.ExperienceOrb) experienceOrb.getBukkitEntity(),
                    repairAmount);
            Bukkit.getPluginManager().callEvent(event);
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
    public void enterVehicle(org.bukkit.entity.Entity vehicle, org.bukkit.entity.Entity entity) {
        vehicle.addPassenger(entity);
    }

    @Override
    public int getPassengersCount(Vehicle vehicle) {
        return (int) vehicle.getPassengers().stream().filter(entity -> !(entity instanceof Player)).count();
    }

    @Override
    public String getCustomName(org.bukkit.entity.Entity entity) {
        return getCustomName(entity, false);
    }

    @Override
    public String getCustomName(org.bukkit.entity.Entity bukkitEntity, boolean withColors) {
        if (withColors) {
            try {
                net.kyori.adventure.text.Component component = bukkitEntity.customName();
                return component == null ? "" : LegacyComponentSerializer.legacySection().serialize(component);
            } catch (Throwable ignored) {
                return bukkitEntity.getCustomName();
            }
        }

        Entity entity = ((CraftEntity) bukkitEntity).getHandle();
        // Much more optimized way than Bukkit's method.
        Component component = entity.getCustomName();
        return component == null ? "" : component.getString();
    }

    @Override
    public void setCustomName(org.bukkit.entity.Entity entity, String name) {
        entity.setCustomName(name);
    }

    @Override
    public boolean isCustomNameVisible(org.bukkit.entity.Entity entity) {
        return entity.isCustomNameVisible();
    }

    @Override
    public void setCustomNameVisible(org.bukkit.entity.Entity entity, boolean visible) {
        entity.setCustomNameVisible(visible);
    }

    @Override
    public boolean isDroppedItem(Item item) {
        return ((CraftItem) item).getHandle() instanceof ItemEntity;
    }

    @Override
    public IEntityWrapper wrapEntity(org.bukkit.entity.LivingEntity bukkitLivingEntity) {
        return new IEntityWrapper() {
            @Override
            public void setHealth(float health, boolean preventUpdate) {
                NMSEntitiesImpl.this.setHealthDirectly(bukkitLivingEntity, health, preventUpdate);
            }

            @Override
            public void setRemoved(boolean removed) {
                LivingEntity livingEntity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
                ENTITY_REMOVE_REASON.set(livingEntity, removed ? Entity.RemovalReason.DISCARDED : null);
            }

            @Override
            public void setDead(boolean dead) {
                LivingEntity entity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
                ENTITY_LIVING_DEAD.set(entity, dead);
                entity.deathTime = 0;
            }
        };
    }

    @Override
    public SpawnCause getEntitySpawnCause(org.bukkit.entity.LivingEntity livingEntity) {
        try {
            return SpawnCause.valueOf(livingEntity.getEntitySpawnReason());
        } catch (Throwable error) {
            return null;
        }
    }

}
