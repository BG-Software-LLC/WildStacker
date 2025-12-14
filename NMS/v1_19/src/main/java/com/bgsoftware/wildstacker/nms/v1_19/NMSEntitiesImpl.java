package com.bgsoftware.wildstacker.nms.v1_19;

import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.utils.entity.StackCheck;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import com.bgsoftware.wildstacker.utils.pair.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerItemMendEvent;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NMSEntitiesImpl extends com.bgsoftware.wildstacker.nms.v1_19.AbstractNMSEntities {

    @Override
    protected Entity createEntity(CraftWorld craftWorld, Location location, Class<? extends org.bukkit.entity.Entity> entityType) {
        return craftWorld.createEntity(location, entityType);
    }

    @Override
    protected void setExperienceOrbValue(ExperienceOrb orb, int value) {
        orb.value = value;
    }

    @Override
    protected <T extends Entity> T createEntityType(EntityType<T> entityType, ServerLevel serverLevel, boolean isCommand) {
        return entityType.create(serverLevel);
    }

    @Override
    protected void copyGossipsAndTradeOffers(Villager source, ZombieVillager dest) {
        dest.setGossips(source.getGossips().store(NbtOps.INSTANCE));
        dest.setTradeOffers(source.getOffers().createTag());
    }

    @Override
    protected void addEntityToWorld(ServerLevel serverLevel, Entity entity) {
        serverLevel.addFreshEntity(entity, CreatureSpawnEvent.SpawnReason.INFECTION);
    }

    @Override
    protected int getExpReward(Mob mob) {
        return mob.getExpReward();
    }

    @Override
    protected boolean shouldDropExperience(LivingEntity livingEntity) {
        try {
            return livingEntity.shouldDropExperience();
        } catch (Throwable error) {
            return com.bgsoftware.wildstacker.nms.v1_19.AbstractNMSEntities.LIVING_ENTITY_SHOULD_DROP_EXPERIENCE.invoke(livingEntity);
        }
    }

    @Override
    protected boolean hasGameRuleDoMobLoot(ServerLevel serverLevel) {
        return serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT);
    }

    @Override
    public void setKiller(org.bukkit.entity.LivingEntity bukkitLivingEntity, Player killer) {
        LivingEntity livingEntity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
        livingEntity.lastHurtByPlayer = killer == null ? null : ((CraftPlayer) killer).getHandle();
    }

    @Override
    protected void removeStriderSaddle(Strider strider) {
        strider.steering.setSaddle(false);
    }

    @Override
    protected EntityResurrectEvent createEntityResurrectEvent(org.bukkit.entity.LivingEntity bukkitLivingEntity,
                                                              org.bukkit.inventory.EquipmentSlot equipmentSlot) {
        return new EntityResurrectEvent(bukkitLivingEntity, equipmentSlot);
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
    protected Number getNumericTagValue(NumericTag tag) {
        return tag.getAsNumber();
    }

    @Override
    protected String getStringTagValue(StringTag tag) {
        return tag.getAsString();
    }

    @Override
    protected CompoundTag getEntityCompoundTag(LivingEntity livingEntity) {
        CompoundTag compoundTag = new CompoundTag();
        livingEntity.addAdditionalSaveData(compoundTag);
        return compoundTag;
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
        } else if (damager instanceof LivingEntity livingEntity) {
            damageSource = damaged.damageSources().mobAttack(livingEntity);
        } else {
            return;
        }

        serverPlayer.awardKillScore(damaged, 0, damageSource);
    }

    @Override
    protected void simulatePickupItemAnimation(LivingEntity livingEntity, ItemEntity itemEntity, int simulatedItemPickupCount) {
        ItemEntity simulatedEntityItemPickup = new ItemEntity(itemEntity.level,
                itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                itemEntity.getItem().copyWithCount(simulatedItemPickupCount));

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
    }

    @Override
    protected boolean isItemSword(ItemStack itemStack) {
        return itemStack.is(ItemTags.SWORDS);
    }

    @Override
    protected float getSweepingDamageRatio(ServerPlayer serverPlayer) {
        return EnchantmentHelper.getSweepingDamageRatio(serverPlayer);
    }

    @Override
    protected DamageSource getPlayerAttackDamageSource(LivingEntity entity, ServerPlayer serverPlayer) {
        return entity.damageSources().playerAttack(serverPlayer).sweep();
    }

    @Override
    protected float calculateFinalSweepDamage(ServerLevel serverLevel, ItemStack itemStack, LivingEntity entity,
                                              DamageSource damageSource, float sweepDamage) {
        return 0;
    }

    @Override
    protected void onSweepingEdgeDamage(LivingEntity entity, DamageSource damageSource, float sweepDamage) {
        entity.lastDamageCancelled = false;
        entity.hurt(damageSource, sweepDamage);
    }

    @Override
    protected Pair<EquipmentSlot, ItemStack> getMendingItem(ServerPlayer serverPlayer) {
        Map.Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.getRandomItemWith(Enchantments.MENDING, serverPlayer, ItemStack::isDamaged);
        ItemStack mendingItem = entry != null ? entry.getValue() : ItemStack.EMPTY;
        EquipmentSlot equipmentSlot = entry != null ? entry.getKey() : EquipmentSlot.MAINHAND;
        return new Pair<>(equipmentSlot, mendingItem);
    }

    @Override
    protected int calculateDurabilityForMending(ServerLevel serverLevel, ExperienceOrb experienceOrb, ItemStack itemStack, int amount) {
        return experienceOrb.xpToDurability(amount);
    }

    @Override
    protected PlayerItemMendEvent callPlayerItemMendEvent(ServerPlayer serverPlayer, ExperienceOrb experienceOrb,
                                                          ItemStack itemStack, EquipmentSlot equipmentSlot,
                                                          int repairAmount, int consumedExperience) {
        return CraftEventFactory.callPlayerItemMendEvent(serverPlayer, experienceOrb, itemStack, equipmentSlot,
                repairAmount, experienceOrb::xpToDurability);
    }

}
