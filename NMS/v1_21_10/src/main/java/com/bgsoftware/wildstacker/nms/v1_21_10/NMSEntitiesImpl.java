package com.bgsoftware.wildstacker.nms.v1_21_10;

import com.bgsoftware.common.reflection.ReflectConstructor;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.utils.entity.StackCheck;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import com.bgsoftware.wildstacker.utils.pair.Pair;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftCow;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftHappyGhast;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.CopperGolem;
import org.bukkit.entity.Frog;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Salmon;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.ZombieNautilus;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;

public class NMSEntitiesImpl extends com.bgsoftware.wildstacker.nms.v1_21_10.AbstractNMSEntities {

    private static final ReflectConstructor<EntityDeathEvent> OLD_DEATH_EVENT_CONSTRUCTOR =
            new ReflectConstructor<>(LivingEntity.class, List.class, int.class);
    private static final boolean DAMAGESOURCE_CAUSE_SUPPORT = new ReflectMethod<>(DamageSource.class,
            1, EntityDamageEvent.DamageCause.class).isValid();
    private static final ReflectMethod<Void> ENTITY_ADD_ADDITIONAL_SAVE_DATA = new ReflectMethod<>(
            Entity.class, "a", ValueOutput.class);

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    protected Entity createEntity(CraftWorld craftWorld, Location location, Class<? extends org.bukkit.entity.Entity> entityType) {
        return ((CraftEntity) craftWorld.createEntity(location, entityType)).getHandle();
    }

    @Override
    protected void setExperienceOrbValue(ExperienceOrb orb, int value) {
        orb.setValue(value);
    }

    @Override
    protected <T extends Entity> T createEntityType(EntityType<T> entityType, ServerLevel serverLevel, boolean isCommand) {
        return entityType.create(serverLevel, isCommand ? EntitySpawnReason.COMMAND : EntitySpawnReason.CONVERSION);
    }

    @Override
    protected void copyGossipsAndTradeOffers(Villager source, ZombieVillager dest) {
        dest.setGossips(source.getGossips());
        dest.setTradeOffers(source.getOffers());
    }

    @Override
    protected void addEntityToWorld(ServerLevel serverLevel, Entity entity) {
        serverLevel.addFreshEntity(entity, CreatureSpawnEvent.SpawnReason.INFECTION);
    }

    @Override
    protected int getExpReward(Mob mob) {
        return mob.getExpReward((ServerLevel) mob.level(), null);
    }

    @Override
    protected boolean shouldDropExperience(net.minecraft.world.entity.LivingEntity livingEntity) {
        try {
            return livingEntity.shouldDropExperience();
        } catch (Throwable error) {
            return com.bgsoftware.wildstacker.nms.v1_21_10.AbstractNMSEntities.LIVING_ENTITY_SHOULD_DROP_EXPERIENCE.invoke(livingEntity);
        }
    }

    @Override
    protected boolean hasGameRuleDoMobLoot(ServerLevel serverLevel) {
        return serverLevel.getGameRules().get(GameRules.MOB_DROPS);
    }

    @Override
    public void setKiller(LivingEntity bukkitLivingEntity, Player killer) {
        net.minecraft.world.entity.LivingEntity livingEntity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
        livingEntity.lastHurtByPlayer = killer == null ? null : EntityReference.of(((CraftPlayer) killer).getHandle());
    }

    @Override
    protected void removeStriderSaddle(Strider strider) {
        strider.setItemSlot(EquipmentSlot.SADDLE, ItemStack.EMPTY);
    }

    @Override
    protected EntityResurrectEvent createEntityResurrectEvent(LivingEntity bukkitLivingEntity,
                                                              org.bukkit.inventory.EquipmentSlot equipmentSlot) {
        return new EntityResurrectEvent(bukkitLivingEntity, equipmentSlot);
    }

    @Override
    public StackCheckResult areSimilar(EntityTypes entityType, LivingEntity en1, LivingEntity en2) {
        if (StackCheck.CHICKEN_TYPE.isEnabled() && StackCheck.CHICKEN_TYPE.isTypeAllowed(entityType)) {
            if (((org.bukkit.entity.Chicken) en1).getVariant() != ((org.bukkit.entity.Chicken) en2).getVariant())
                return StackCheckResult.CHICKEN_TYPE;
        }

        if (StackCheck.COW_TYPE.isEnabled() && StackCheck.COW_TYPE.isTypeAllowed(entityType)) {
            if (((CraftCow) en1).getVariant() != ((CraftCow) en2).getVariant())
                return StackCheckResult.COW_TYPE;
        }

        if (StackCheck.FROG_TOUNGE_TARGET.isEnabled() && StackCheck.FROG_TOUNGE_TARGET.isTypeAllowed(entityType)) {
            if (!Objects.equals(((Frog) en1).getTongueTarget(), ((Frog) en2).getTongueTarget()))
                return StackCheckResult.FROG_TOUNGE_TARGET;
        }

        if (StackCheck.FROG_TYPE.isEnabled() && StackCheck.FROG_TYPE.isTypeAllowed(entityType)) {
            if (((Frog) en1).getVariant() != ((Frog) en2).getVariant())
                return StackCheckResult.FROG_TYPE;
        }

        if (StackCheck.HAPPY_GHAST_SADDLE.isEnabled() && StackCheck.HAPPY_GHAST_SADDLE.isTypeAllowed(entityType)) {
            if (((CraftHappyGhast) en1).getHandle().isWearingBodyArmor() != ((CraftHappyGhast) en2).getHandle().isWearingBodyArmor())
                return StackCheckResult.HAPPY_GHAST_SADDLE;
        }

        if (StackCheck.PIG_TYPE.isEnabled() && StackCheck.PIG_TYPE.isTypeAllowed(entityType)) {
            if (((Pig) en1).getVariant() != ((Pig) en2).getVariant())
                return StackCheckResult.PIG_TYPE;
        }

        if (StackCheck.SALMON_SIZE.isEnabled() && StackCheck.SALMON_SIZE.isTypeAllowed(entityType)) {
            if (((Salmon) en1).getVariant() != ((Salmon) en2).getVariant())
                return StackCheckResult.SALMON_SIZE;
        }

        if (StackCheck.WOLF_TYPE.isEnabled() && StackCheck.WOLF_TYPE.isTypeAllowed(entityType)) {
            if (((Wolf) en1).getVariant() != ((Wolf) en2).getVariant())
                return StackCheckResult.WOLF_TYPE;
        }

        if (StackCheck.ZOMBIE_NAUTILUS_TYPE.isEnabled() && StackCheck.ZOMBIE_NAUTILUS_TYPE.isTypeAllowed(entityType)) {
            if (((ZombieNautilus) en1).getVariant() != ((ZombieNautilus) en2).getVariant())
                return StackCheckResult.ZOMBIE_NAUTILUS_TYPE;
        }



        return StackCheckResult.SUCCESS;
    }

    @Override
    protected Number getNumericTagValue(NumericTag tag) {
        return tag.box();
    }

    @Override
    protected String getStringTagValue(StringTag tag) {
        return tag.value();
    }

    @Override
    protected CompoundTag getEntityCompoundTag(net.minecraft.world.entity.LivingEntity livingEntity) {
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(livingEntity.problemPath(), LOGGER)) {
            TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, livingEntity.registryAccess());
            ENTITY_ADD_ADDITIONAL_SAVE_DATA.invoke(livingEntity, tagValueOutput);
            return tagValueOutput.buildResult();
        }
    }

    @Override
    protected void awardKillScore(ServerPlayer serverPlayer, Entity damaged, Entity damager) {
        DamageSource damageSource;

        if (damager instanceof net.minecraft.world.entity.player.Player player) {
            damageSource = damaged.damageSources().playerAttack(player);
        } else if (damager instanceof ThrownTrident thrownTrident) {
            damageSource = damaged.damageSources().trident(damager, thrownTrident.getOwner());
        } else if (damager instanceof AbstractArrow abstractArrow) {
            damageSource = damaged.damageSources().arrow(abstractArrow, abstractArrow.getOwner());
        } else if (damager instanceof Fireball fireball) {
            damageSource = damaged.damageSources().fireball(fireball, fireball.getOwner());
        } else if (damager instanceof net.minecraft.world.entity.LivingEntity livingEntity) {
            damageSource = damaged.damageSources().mobAttack(livingEntity);
        } else {
            return;
        }

        serverPlayer.awardKillScore(damaged, damageSource);
    }

    @Override
    protected void simulatePickupItemAnimation(net.minecraft.world.entity.LivingEntity livingEntity, ItemEntity itemEntity, int simulatedItemPickupCount) {
        ItemEntity simulatedEntityItemPickup = new ItemEntity(itemEntity.level(),
                itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                itemEntity.getItem().copyWithCount(simulatedItemPickupCount));

        List<SynchedEntityData.DataValue<?>> entityData = simulatedEntityItemPickup.getEntityData().packDirty();

        ServerChunkCache serverChunkCache = ((ServerLevel) simulatedEntityItemPickup.level()).getChunkSource();
        serverChunkCache.sendToTrackingPlayersAndSelf(livingEntity, getAddEntityPacketForEntity(simulatedEntityItemPickup));
        if (entityData != null) {
            serverChunkCache.sendToTrackingPlayersAndSelf(livingEntity, new ClientboundSetEntityDataPacket(
                    simulatedEntityItemPickup.getId(), entityData));
        }
        serverChunkCache.sendToTrackingPlayersAndSelf(livingEntity, new ClientboundTakeItemEntityPacket(
                simulatedEntityItemPickup.getId(), livingEntity.getId(), simulatedItemPickupCount));
        serverChunkCache.sendToTrackingPlayersAndSelf(livingEntity, new ClientboundRemoveEntitiesPacket(
                simulatedEntityItemPickup.getId()));
    }

    @Override
    protected boolean isItemSword(ItemStack itemStack) {
        return itemStack.is(ItemTags.SWORDS);
    }

    @Override
    protected float getSweepingDamageRatio(ServerPlayer serverPlayer) {
        return (float) serverPlayer.getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO);
    }

    @Override
    protected DamageSource getPlayerAttackDamageSource(net.minecraft.world.entity.LivingEntity entity, ServerPlayer serverPlayer) {
        return entity.damageSources().playerAttack(serverPlayer);
    }

    @Override
    protected float calculateFinalSweepDamage(ServerLevel serverLevel, ItemStack itemStack, net.minecraft.world.entity.LivingEntity entity,
                                              DamageSource damageSource, float sweepDamage) {
        return EnchantmentHelper.modifyDamage(serverLevel, itemStack, entity, damageSource, sweepDamage);
    }

    @Override
    protected void onSweepingEdgeDamage(net.minecraft.world.entity.LivingEntity entity, DamageSource damageSource, float sweepDamage) {
        entity.lastDamageCancelled = false;
        if (DAMAGESOURCE_CAUSE_SUPPORT) {
            damageSource = damageSource.knownCause(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK);
        }
        entity.hurtServer((ServerLevel) entity.level(), damageSource, sweepDamage);
    }

    @Override
    protected Pair<EquipmentSlot, ItemStack> getMendingItem(ServerPlayer serverPlayer) {
        EnchantedItemInUse enchantedItemInUse = EnchantmentHelper.getRandomItemWith(
                        EnchantmentEffectComponents.REPAIR_WITH_XP, serverPlayer, ItemStack::isDamaged)
                .orElse(null);

        ItemStack mendingItem = enchantedItemInUse == null ? ItemStack.EMPTY : enchantedItemInUse.itemStack();
        EquipmentSlot equipmentSlot = enchantedItemInUse == null ? EquipmentSlot.MAINHAND : enchantedItemInUse.inSlot();
        return new Pair<>(equipmentSlot, mendingItem);
    }

    @Override
    protected int calculateDurabilityForMending(ServerLevel serverLevel, ExperienceOrb experienceOrb, ItemStack itemStack, int amount) {
        return EnchantmentHelper.modifyDurabilityToRepairFromXp(serverLevel, itemStack, amount);
    }

    @Override
    protected PlayerItemMendEvent callPlayerItemMendEvent(ServerPlayer serverPlayer, ExperienceOrb experienceOrb,
                                                          ItemStack itemStack, EquipmentSlot equipmentSlot,
                                                          int repairAmount, int consumedExperience) {
        return CraftEventFactory.callPlayerItemMendEvent(serverPlayer, experienceOrb,
                itemStack, equipmentSlot, repairAmount, consumedExperience);
    }

    @Override
    public EntityDeathEvent createDeathEvent(LivingEntity livingEntity,
                                             List<org.bukkit.inventory.ItemStack> drops, int droppedExp,
                                             EntityDamageEvent lastDamage) {
        if (OLD_DEATH_EVENT_CONSTRUCTOR.isValid()) {
            return OLD_DEATH_EVENT_CONSTRUCTOR.newInstance(livingEntity, drops, droppedExp);
        } else {
            return new EntityDeathEvent(livingEntity, lastDamage.getDamageSource(), drops, droppedExp);
        }
    }

    private static ClientboundAddEntityPacket getAddEntityPacketForEntity(Entity entity) {
        BlockPos blockPos = new BlockPos(entity.getBlockX(), entity.getBlockY(), entity.getBlockZ());
        return new ClientboundAddEntityPacket(entity, 0, blockPos);
    }

}
