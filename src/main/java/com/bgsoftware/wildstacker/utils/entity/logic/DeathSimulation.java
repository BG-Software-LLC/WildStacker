package com.bgsoftware.wildstacker.utils.entity.logic;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import com.bgsoftware.wildstacker.api.enums.StackSplit;
import com.bgsoftware.wildstacker.api.enums.UnstackResult;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.hooks.listeners.IEntityDeathListener;
import com.bgsoftware.wildstacker.nms.entity.IEntityWrapper;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.bgsoftware.wildstacker.utils.entity.EntityDamageData;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.pair.Pair;
import com.bgsoftware.wildstacker.utils.statistics.StatisticsUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public final class DeathSimulation {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private final static Enchantment SWEEPING_EDGE = Enchantment.getByName("SWEEPING_EDGE");
    @Nullable
    private static final Material CROSSBOW_TYPE = Materials.getMaterialOrNull("CROSSBOW");
    private static boolean sweepingEdgeHandled = false;

    private DeathSimulation() {
    }

    public static EntityDamageData simulateDeath(StackedEntity stackedEntity, EntityDamageEvent damageEvent,
                                                 ItemStack killerTool, @Nullable Player killer, Entity entityKiller,
                                                 boolean creativeMode, boolean fromDeathEvent) {
        if (!plugin.getSettings().entitiesStackingEnabled && stackedEntity.getStackAmount() <= 1)
            return new EntityDamageData(false, Collections.emptyMap());

        LivingEntity livingEntity = stackedEntity.getLivingEntity();

        if (damageEvent.getCause() != EntityDamageEvent.DamageCause.VOID &&
                plugin.getNMSEntities().handleTotemOfUndying(livingEntity)) {
            return new EntityDamageData(true, Collections.emptyMap());
        }

        if (stackedEntity.hasFlag(EntityFlag.ATTACKED_ENTITY))
            return new EntityDamageData(true, Collections.emptyMap());

        Pair<Integer, Double> spreadDamageResult = checkForSpreadDamage(stackedEntity,
                stackedEntity.isInstantKill(damageEvent.getCause()), damageEvent.getFinalDamage(), killerTool);

        int entitiesToKill = spreadDamageResult.getKey();
        double damageToNextStack = spreadDamageResult.getValue();

        int fireTicks = livingEntity.getFireTicks();

        EntityDamageData result = new EntityDamageData(false, 0);

        if (handleFastKill(livingEntity, killer))
            result.setCancelled(true);

        //Villager was killed by a zombie - should be turned into a zombie villager.
        if (checkForZombieVillager(stackedEntity, entityKiller))
            return result;

        int originalAmount = stackedEntity.getStackAmount();

        if (stackedEntity.runUnstack(entitiesToKill, killer == null ? entityKiller : killer) != UnstackResult.SUCCESS)
            return result;

        stackedEntity.setFlag(EntityFlag.ATTACKED_ENTITY, true);

        int unstackAmount = originalAmount - stackedEntity.getStackAmount();

        EntityUtils.setKiller(livingEntity, killer);

        if (plugin.getSettings().keepFireEnabled && livingEntity.getFireTicks() > -1)
            livingEntity.setFireTicks(160);

        if (entityKiller != null)
            giveStatisticsToKiller(entityKiller, unstackAmount, stackedEntity);

        // Handle sweeping edge enchantment
        if (!sweepingEdgeHandled && result.isCancelled() && killerTool != null && killer != null) {
            try {
                sweepingEdgeHandled = true;
                plugin.getNMSEntities().handleSweepingEdge(killer, killerTool, stackedEntity.getLivingEntity(),
                        damageEvent.getDamage());
            } finally {
                sweepingEdgeHandled = false;
            }
        }

        //Decrease durability when next-stack-knockback is false
        if (result.isCancelled() && killerTool != null && !creativeMode && !plugin.getNMSAdapter().isUnbreakable(killerTool))
            reduceKillerToolDurability(killerTool, killer);

        Location dropLocation = livingEntity.getLocation().add(0, 0.5, 0);

        Executor.async(() -> {
            livingEntity.setFireTicks(fireTicks);

            int lootBonusLevel = killerTool == null ? 0 : killerTool.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
            List<ItemStack> drops = stackedEntity.getDrops(lootBonusLevel, plugin.getSettings().multiplyDrops ? unstackAmount : 1);
            int asyncXpResult = stackedEntity.getExp(plugin.getSettings().multiplyExp ? unstackAmount : 1, 0);

            Executor.sync(() -> {
                IEntityWrapper nmsEntity = plugin.getNMSEntities().wrapEntity(livingEntity);

                ((WStackedEntity) stackedEntity).setDeadFlag(true);
                nmsEntity.setHealth(0f, true);

                // Setting the stack amount of the entity to the unstack amount.
                int realStackAmount = stackedEntity.getStackAmount();
                stackedEntity.setStackAmount(unstackAmount, false);

                plugin.getProviders().notifyEntityDeathListeners(stackedEntity,
                        IEntityDeathListener.Type.BEFORE_DEATH_EVENT);

                // We fire the entity_die game event
                plugin.getNMSEntities().sendEntityDieEvent(livingEntity);

                List<ItemStack> finalDrops;
                int finalExp;

                if (!fromDeathEvent) {
                    int droppedExp = asyncXpResult >= 0 ? asyncXpResult :
                            stackedEntity.getExp(plugin.getSettings().multiplyExp ? unstackAmount : 1, 0);
                    EntityDeathEvent entityDeathEvent = new EntityDeathEvent(livingEntity, new ArrayList<>(drops), droppedExp);
                    Bukkit.getPluginManager().callEvent(entityDeathEvent);
                    finalDrops = entityDeathEvent.getDrops();
                    finalExp = entityDeathEvent.getDroppedExp();
                } else {
                    finalDrops = drops;
                    Integer expToDropFlag = stackedEntity.getFlag(EntityFlag.EXP_TO_DROP);
                    finalExp = expToDropFlag == null ? 0 : expToDropFlag;
                    stackedEntity.removeFlag(EntityFlag.EXP_TO_DROP);
                }


                // Restore all values
                nmsEntity.setRemoved(false);
                nmsEntity.setDead(false);

                // Restore health.
                if (realStackAmount > 0) {
                    nmsEntity.setHealth((float) (livingEntity.getMaxHealth() - damageToNextStack), false);
                } else {
                    // We want to set the health to 0 but update it.
                    nmsEntity.setHealth(0f, false);
                }

                // If setting this to ender dragons, the death animation doesn't happen for an unknown reason.
                // Cannot revert to original death event neither. This fixes death animations for all versions.
                if (livingEntity.getType() != EntityType.ENDER_DRAGON)
                    livingEntity.setLastDamageCause(null);

                plugin.getProviders().notifyEntityDeathListeners(stackedEntity,
                        IEntityDeathListener.Type.AFTER_DEATH_EVENT);

                finalDrops.removeIf(itemStack -> itemStack == null || itemStack.getType() == Material.AIR);

                // Multiply items that weren't added in the first place
                // We should call this only when the event was called - aka finalDrops != drops.
                if (plugin.getSettings().multiplyDrops && finalDrops != drops) {
                    subtract(drops, finalDrops).forEach(itemStack -> itemStack.setAmount(itemStack.getAmount() * unstackAmount));
                }

                finalDrops.forEach(itemStack -> ItemUtils.dropItem(itemStack, dropLocation));

                if (finalExp > 0) {
                    if (GeneralUtils.contains(plugin.getSettings().entitiesAutoExpPickup, stackedEntity) && livingEntity.getKiller() != null) {
                        EntityUtils.giveExp(livingEntity.getKiller(), finalExp);
                        if (plugin.getSettings().entitiesExpPickupSound != null)
                            livingEntity.getKiller().playSound(livingEntity.getLocation(),
                                    plugin.getSettings().entitiesExpPickupSound, 0.1F, 0.1F);
                    } else {
                        EntityUtils.spawnExp(livingEntity.getLocation(), finalExp);
                    }
                }

                attemptJoinRaid(killer, livingEntity);

                if (killer != null && killerTool != null && killerTool.getType() == CROSSBOW_TYPE &&
                        ((EntityDamageByEntityEvent) damageEvent).getDamager() instanceof Arrow)
                    plugin.getNMSEntities().awardCrossbowShot(killer, livingEntity);

                ((WStackedEntity) stackedEntity).setDeadFlag(false);

                stackedEntity.removeFlag(EntityFlag.ATTACKED_ENTITY);

                // Restore stacked entity amount
                stackedEntity.setStackAmount(realStackAmount, false);
            });
        });

        return result;
    }

    private static Pair<Integer, Double> checkForSpreadDamage(StackedEntity stackedEntity,
                                                              boolean instantKill, double finalDamage,
                                                              ItemStack damagerTool) {
        int entitiesToKill;
        double damageToNextStack;

        if (plugin.getSettings().spreadDamage && !instantKill) {
            double dealtDamage = finalDamage;

            if (SWEEPING_EDGE != null && damagerTool != null) {
                int sweepingEdgeLevel = damagerTool.getEnchantmentLevel(SWEEPING_EDGE);
                if (sweepingEdgeLevel > 0)
                    dealtDamage = 1 + finalDamage * ((double) sweepingEdgeLevel / (sweepingEdgeLevel + 1));
            }

            double entityHealth = stackedEntity.getHealth();
            double entityMaxHealth = stackedEntity.getLivingEntity().getMaxHealth();

            double leftDamage = Math.max(0, dealtDamage - entityHealth);

            entitiesToKill = Math.min(stackedEntity.getStackAmount(), 1 + (int) (leftDamage / entityMaxHealth));
            damageToNextStack = leftDamage % entityMaxHealth;
        } else {
            entitiesToKill = Math.min(stackedEntity.getStackAmount(),
                    instantKill ? stackedEntity.getStackAmount() : stackedEntity.getDefaultUnstack());
            damageToNextStack = 0;
        }

        return new Pair<>(entitiesToKill, damageToNextStack);
    }

    private static boolean handleFastKill(LivingEntity livingEntity, Player damager) {
        if (plugin.getSettings().entitiesFastKill) {

            if (damager != null) {
                // We make sure the entity has no damage ticks, so it can always be hit.
                livingEntity.setMaximumNoDamageTicks(0);
            }

            // We make sure the entity doesn't get any knockback by setting the velocity to 0.
            Executor.sync(() -> livingEntity.setVelocity(new Vector()), 1L);

            return true;
        }

        return false;
    }

    private static boolean checkForZombieVillager(StackedEntity stackedEntity, Entity entityDamager) {
        LivingEntity livingEntity = stackedEntity.getLivingEntity();

        if (livingEntity.getType() != EntityType.VILLAGER || !(entityDamager instanceof Zombie))
            return false;

        switch (livingEntity.getWorld().getDifficulty()) {
            case NORMAL:
                if (!ThreadLocalRandom.current().nextBoolean())
                    return false;
                break;
            case EASY:
            case PEACEFUL:
                return false;
        }

        Zombie zombieVillager = plugin.getNMSEntities().spawnZombieVillager((Villager) livingEntity);

        if (zombieVillager == null)
            return false;

        Optional.ofNullable(livingEntity.getVehicle()).ifPresent(vehicle ->
                plugin.getNMSEntities().enterVehicle(vehicle, zombieVillager));

        StackedEntity stackedZombie = WStackedEntity.of(zombieVillager);

        if (StackSplit.VILLAGER_INFECTION.isEnabled()) {
            stackedEntity.runUnstack(1, entityDamager);
        } else {
            stackedZombie.setStackAmount(stackedEntity.getStackAmount(), true);
            stackedEntity.remove();
        }

        stackedZombie.updateName();
        stackedZombie.runStackAsync(null);

        return true;
    }

    private static void giveStatisticsToKiller(Entity entityKiller, int unstackAmount, StackedEntity stackedEntity) {
        EntityType victimType = stackedEntity.getType();

        if (entityKiller instanceof Player) {
            Player killer = (Player) entityKiller;
            try {
                StatisticsUtils.incrementStatistic(killer, Statistic.MOB_KILLS, unstackAmount);
                StatisticsUtils.incrementStatistic(killer, Statistic.KILL_ENTITY, victimType, unstackAmount);
            } catch (IllegalArgumentException ignored) {
            }
        }

        plugin.getNMSEntities().awardKillScore(stackedEntity.getLivingEntity(), entityKiller);
    }

    private static void reduceKillerToolDurability(ItemStack damagerTool, Player killer) {
        int damage = ItemUtils.isSword(damagerTool.getType()) ? 1 : ItemUtils.isTool(damagerTool.getType()) ? 2 : 0;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (damage > 0) {
            int unbreakingLevel = damagerTool.getEnchantmentLevel(Enchantment.DURABILITY);
            int damageDecrease = 0;

            for (int i = 0; unbreakingLevel > 0 && i < damage; i++) {
                if (random.nextInt(unbreakingLevel + 1) > 0)
                    damageDecrease++;
            }

            damage -= damageDecrease;

            if (damage > 0) {
                if (damagerTool.getDurability() + damage > damagerTool.getType().getMaxDurability())
                    killer.setItemInHand(new ItemStack(Material.AIR));
                else
                    damagerTool.setDurability((short) (damagerTool.getDurability() + damage));
            }
        }
    }

    private static void attemptJoinRaid(Player killer, LivingEntity livingEntity) {
        if (killer == null || !EntityTypes.fromEntity(livingEntity).isRaider())
            return;

        org.bukkit.entity.Raider raider = (org.bukkit.entity.Raider) livingEntity;

        if (raider.isPatrolLeader()) {
            killer.addPotionEffect(new PotionEffect(
                    PotionEffectType.getByName("BAD_OMEN"),
                    120000,
                    EntityUtils.getBadOmenAmplifier(killer),
                    false
            ));
        }

        plugin.getNMSWorld().attemptJoinRaid(killer, raider);
    }

    private static List<ItemStack> subtract(List<ItemStack> list1, List<ItemStack> list2) {
        List<ItemStack> toReturn = new ArrayList<>(list2);
        toReturn.removeAll(list1);
        return toReturn;
    }

}
