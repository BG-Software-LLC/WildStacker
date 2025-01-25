package com.bgsoftware.wildstacker.utils.entity.logic;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import com.bgsoftware.wildstacker.api.enums.StackSplit;
import com.bgsoftware.wildstacker.api.enums.UnstackResult;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.hooks.listeners.IEntityDeathListener;
import com.bgsoftware.wildstacker.nms.entity.IEntityWrapper;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.entity.EntityDamageData;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.events.BiHandlerList;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.pair.Pair;
import com.bgsoftware.wildstacker.utils.statistics.StatisticsUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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
import org.bukkit.event.HandlerList;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public final class DeathSimulation {

    private static final ReflectField<HandlerList> EVENT_DAMAGE_HANDLER_LIST = new ReflectField<HandlerList>(
            EntityDamageEvent.class, HandlerList.class, "handlers").removeFinal();

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    @Nullable
    private static final Material CROSSBOW_TYPE = Materials.getMaterialOrNull("CROSSBOW");
    @Nullable
    private static final PotionEffectType BAD_OMEN = PotionEffectType.getByName("BAD_OMEN");

    private static boolean sweepingEdgeHandled = false;

    private DeathSimulation() {
    }

    public static void injectEntityDamageHandlerList() {
        // Changes HandlerList of EntityDamageEvent to only include WildStacker's listener.
        HandlerList original = EntityDamageEvent.getHandlerList();
        BiHandlerList newHandlerList = new BiHandlerList(original);
        EVENT_DAMAGE_HANDLER_LIST.set(null, newHandlerList);
    }

    public static EntityDamageData simulateDeath(StackedEntity stackedEntity, EntityDamageEvent damageEvent,
                                                 @Nullable Entity directKiller, @Nullable Entity sourceKiller,
                                                 boolean fromDeathEvent) {
        if (!plugin.getSettings().entitiesStackingEnabled && stackedEntity.getStackAmount() <= 1)
            return new EntityDamageData(false, Collections.emptyMap());

        boolean isSourceKillerPlayer = sourceKiller instanceof Player;

        LivingEntity livingEntity = stackedEntity.getLivingEntity();

        if (damageEvent.getCause() != EntityDamageEvent.DamageCause.VOID &&
                plugin.getNMSEntities().handleTotemOfUndying(livingEntity)) {
            return new EntityDamageData(true, Collections.emptyMap());
        }

        if (stackedEntity.hasFlag(EntityFlag.ATTACKED_ENTITY))
            return new EntityDamageData(true, Collections.emptyMap());

        Pair<Integer, Double> spreadDamageResult = checkForSpreadDamage(stackedEntity,
                stackedEntity.isInstantKill(damageEvent.getCause()), damageEvent.getFinalDamage());

        int entitiesToKill = spreadDamageResult.getKey();
        double damageToNextStack = spreadDamageResult.getValue();

        EntityDamageData result = new EntityDamageData(false, 0);

        //Villager was killed by a zombie - should be turned into a zombie villager.
        if (checkForZombieVillager(stackedEntity, directKiller))
            return result;

        if (isSourceKillerPlayer && handleFastKill(livingEntity, (Player) sourceKiller))
            result.setCancelled(true);

        int originalAmount = stackedEntity.getStackAmount();

        if (stackedEntity.runUnstack(entitiesToKill, isSourceKillerPlayer ? sourceKiller : directKiller) != UnstackResult.SUCCESS)
            return result;

        int unstackAmount = originalAmount - stackedEntity.getStackAmount();

        stackedEntity.setFlag(EntityFlag.ATTACKED_ENTITY, true);

        int fireTicks;
        int lootBonusLevel;

        if (!isSourceKillerPlayer) {
            fireTicks = livingEntity.getFireTicks();
            lootBonusLevel = 0;
        } else {
            ItemStack killerTool = ((Player) sourceKiller).getItemInHand();
            boolean isKillerToolEmpty = killerTool == null || killerTool.getType() == Material.AIR;

            if (isKillerToolEmpty) {
                fireTicks = livingEntity.getFireTicks();
                lootBonusLevel = 0;
            } else {
                int fireAspectLevel = killerTool.getEnchantmentLevel(Enchantment.FIRE_ASPECT);
                fireTicks = fireAspectLevel > 0 ? fireAspectLevel * 4 : livingEntity.getFireTicks();

                lootBonusLevel = killerTool.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);

                // Handle sweeping edge enchantment
                if (!sweepingEdgeHandled && result.isCancelled()) {
                    try {
                        sweepingEdgeHandled = true;
                        plugin.getNMSEntities().handleSweepingEdge((Player) sourceKiller,
                                killerTool, stackedEntity.getLivingEntity(), damageEvent.getDamage());
                    } finally {
                        sweepingEdgeHandled = false;
                    }
                }

                //Decrease durability when next-stack-knockback is false
                if (result.isCancelled() && ((Player) sourceKiller).getGameMode() != GameMode.CREATIVE &&
                        !plugin.getNMSAdapter().isUnbreakable(killerTool))
                    reduceKillerToolDurability(killerTool, (Player) sourceKiller);
            }

            EntityUtils.setKiller(livingEntity, (Player) sourceKiller);

            giveStatisticsToKiller(directKiller, (Player) sourceKiller, unstackAmount, stackedEntity);
        }

        if (plugin.getSettings().keepFireEnabled && livingEntity.getFireTicks() > -1)
            livingEntity.setFireTicks(160);

        // We want to cache the killer of the entity
        if (sourceKiller != null)
            stackedEntity.setFlag(EntityFlag.CACHED_KILLER, sourceKiller);

        Location dropLocation = livingEntity.getLocation().add(0, 0.5, 0);

        Executor.async(() -> {
            livingEntity.setFireTicks(fireTicks);

            List<ItemStack> drops = stackedEntity.getDrops(lootBonusLevel, plugin.getSettings().multiplyDrops ? unstackAmount : 1);
            int asyncXpResult = stackedEntity.getExp(plugin.getSettings().multiplyExp ? unstackAmount : 1, 0);

            Executor.sync(() -> {
                // We want to remove the cache of the killer
                stackedEntity.removeFlag(EntityFlag.CACHED_KILLER);

                IEntityWrapper nmsEntity = plugin.getNMSEntities().wrapEntity(livingEntity);

                ((WStackedEntity) stackedEntity).setDeadFlag(true);

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
                    EntityDeathEvent entityDeathEvent = plugin.getNMSEntities().createDeathEvent(
                            livingEntity, new LinkedList<>(drops), droppedExp, damageEvent);

                    try {
                        nmsEntity.setHealth(0f, true);
                        Bukkit.getPluginManager().callEvent(entityDeathEvent);
                    } finally {
                        nmsEntity.setHealth(1f, true);
                    }

                    finalDrops = entityDeathEvent.getDrops();
                    finalExp = entityDeathEvent.getDroppedExp();
                } else {
                    finalDrops = drops;
                    Integer expToDropFlag = stackedEntity.getAndRemoveFlag(EntityFlag.EXP_TO_DROP);
                    finalExp = expToDropFlag == null ? 0 : expToDropFlag;
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

                if (isSourceKillerPlayer) {
                    attemptJoinRaid((Player) sourceKiller, livingEntity);

                    ItemStack killerTool = ((Player) sourceKiller).getItemInHand();

                    if (killerTool.getType() == CROSSBOW_TYPE &&
                            ((EntityDamageByEntityEvent) damageEvent).getDamager() instanceof Arrow)
                        plugin.getNMSEntities().awardCrossbowShot((Player) sourceKiller, livingEntity, killerTool);
                }

                ((WStackedEntity) stackedEntity).setDeadFlag(false);

                stackedEntity.removeFlag(EntityFlag.ATTACKED_ENTITY);

                // Restore stacked entity amount
                stackedEntity.setStackAmount(realStackAmount, false);
            });
        });

        return result;
    }

    private static Pair<Integer, Double> checkForSpreadDamage(StackedEntity stackedEntity,
                                                              boolean instantKill, double finalDamage) {
        int entitiesToKill;
        double damageToNextStack;

        if (plugin.getSettings().spreadDamage && !instantKill) {
            double entityHealth = stackedEntity.getHealth();
            double entityMaxHealth = stackedEntity.getLivingEntity().getMaxHealth();

            double leftDamage = Math.max(0, finalDamage - entityHealth);

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

        stackedEntity.setSpawnCorpse(false);
        if (StackSplit.VILLAGER_INFECTION.isEnabled()) {
            if (stackedEntity.runUnstack(1, entityDamager) == UnstackResult.SUCCESS) {
                IEntityWrapper nmsEntity = plugin.getNMSEntities().wrapEntity(livingEntity);
                nmsEntity.setHealth((float) livingEntity.getMaxHealth(), false);
                stackedEntity.updateName();
            }
        } else {
            stackedZombie.setStackAmount(stackedEntity.getStackAmount(), true);
            stackedEntity.remove();
        }

        if (!stackedEntity.hasNameTag())
            stackedZombie.removeFlag(EntityFlag.NAME_TAG);

        stackedZombie.updateName();
        stackedZombie.runStackAsync(null);

        return true;
    }

    private static void giveStatisticsToKiller(Entity directKiller, Player sourceKiller, int unstackAmount, StackedEntity stackedEntity) {
        EntityType victimType = stackedEntity.getType();

        try {
            StatisticsUtils.incrementStatistic(sourceKiller, Statistic.MOB_KILLS, unstackAmount);
            StatisticsUtils.incrementStatistic(sourceKiller, Statistic.KILL_ENTITY, victimType, unstackAmount);
        } catch (IllegalArgumentException ignored) {
        }

        plugin.getNMSEntities().awardKillScore(sourceKiller, stackedEntity.getLivingEntity(), directKiller);
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

        if (raider.isPatrolLeader() && ServerVersion.isLessThan(ServerVersion.v1_21)) {
            killer.addPotionEffect(new PotionEffect(
                    BAD_OMEN,
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
