package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.enums.StackSplit;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.listeners.events.EventsListener;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.bgsoftware.wildstacker.utils.Random;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.entity.EntitiesGetter;
import com.bgsoftware.wildstacker.utils.entity.EntityDamageData;
import com.bgsoftware.wildstacker.utils.entity.EntityStorage;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.entity.FutureEntityTracker;
import com.bgsoftware.wildstacker.utils.entity.logic.DeathSimulation;
import com.bgsoftware.wildstacker.utils.events.BiHandlerList;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Beehive;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockShearEntityEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEnterBlockEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.entity.SheepRegrowWoolEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class EntitiesListener implements Listener {

    private static final Map<EntityDamageEvent.DamageModifier, ? extends Function<? super Double, Double>> damageModifiersFunctions =
            Maps.newEnumMap(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, Functions.constant(-0.0D)));
    private final static Map<Location, Integer[]> beesAmount = new HashMap<>();
    private final static Map<Location, Integer> turtleEggsAmounts = new HashMap<>();
    private final static Material TURTLE_EGG = Materials.getMaterialOrNull("TURTLE_EGG");

    public static EntitiesListener IMP;

    private final FutureEntityTracker<Integer> slimeSplitTracker = new FutureEntityTracker<>();
    private final FutureEntityTracker<Integer> mushroomTracker = new FutureEntityTracker<>();
    private final FutureEntityTracker<SpawnEggTrackedData> spawnEggTracker = new FutureEntityTracker<>();
    private final Map<EntityDamageEvent, EntityDamageData> damageResults = new IdentityHashMap<>();
    private final WildStackerPlugin plugin;

    private boolean duplicateCow = false;

    public boolean secondPickupEventCall = false;
    @Nullable
    public Cancellable secondPickupEvent = null;

    public EntitiesListener(WildStackerPlugin plugin) {
        this.plugin = plugin;
        EntitiesListener.IMP = this;

        if (ServerVersion.isAtLeast(ServerVersion.v1_13))
            plugin.getServer().getPluginManager().registerEvents(new TransformListener(), plugin);
        if (ServerVersion.isAtLeast(ServerVersion.v1_14))
            plugin.getServer().getPluginManager().registerEvents(new TurtleListener(), plugin);
        if (ServerVersion.isAtLeast(ServerVersion.v1_15))
            plugin.getServer().getPluginManager().registerEvents(new BeeListener(), plugin);

        try {
            Class.forName("org.bukkit.event.block.BlockShearEntityEvent");
            plugin.getServer().getPluginManager().registerEvents(new BlockShearEntityListener(), plugin);
        } catch (Exception ignored) {
        }

        EventsListener.addEntityPickupListener(this::onEntityPickup, EventPriority.HIGHEST);

        // We register the event in a delay so it will be the last listener to be called.
        // We want to restore the event after all the other ones will be called on the wanted data.
        Executor.sync(() -> plugin.getServer().getPluginManager().registerEvents(
                new EntityDamageRestoreListener(), plugin), 10L);
    }

    /*
     *  Event handlers
     */

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeathMonitor(EntityDeathEvent e) {
        if (EntityStorage.hasMetadata(e.getEntity(), EntityFlag.CORPSE) &&
                !EntityStorage.hasMetadata(e.getEntity(), EntityFlag.DEAD_ENTITY)) {
            try {
                e.getDrops().clear();
                e.setDroppedExp(0);
            } catch (Throwable ex) {
                WildStackerPlugin.log("Seems like the array of EntityDeathEvent is not an ArrayList, but a " + e.getDrops().getClass());
                ex.printStackTrace();
            }
        }
    }

    private boolean onEntityPickup(Cancellable event, StackedItem stackedItem, LivingEntity livingEntity, int remaining) {
        return EntityStorage.hasMetadata(livingEntity, EntityFlag.CORPSE);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCustomEntityDeath(EntityDeathEvent e) {
        //Checks if the entity is not a corpse.
        if (EntityStorage.hasMetadata(e.getEntity(), EntityFlag.CORPSE) || !EntityUtils.isStackable(e.getEntity()))
            return;

        StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());

        if (stackedEntity.hasFlag(EntityFlag.DEAD_ENTITY)) {
            return;
        }

        stackedEntity.setDrops(e.getDrops()); // Fixing issues with plugins changing the drops in this event.
        stackedEntity.setFlag(EntityFlag.EXP_TO_DROP, e.getDroppedExp());

        //Calling the onEntityLastDamage function with default parameters.
        handleEntityDamage(createDamageEvent(e.getEntity(), EntityDamageEvent.DamageCause.CUSTOM,
                e.getEntity().getHealth(), null), true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDeadEntityDamage(EntityDamageEvent e) {
        if (EntityUtils.isStackable(e.getEntity()) && WStackedEntity.of(e.getEntity()).hasFlag(EntityFlag.DEAD_ENTITY)) {
            e.setDamage(0);
        }
        if (EntityStorage.hasMetadata(e.getEntity(), EntityFlag.CORPSE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityNerfDamage(EntityDamageByEntityEvent e) {
        if (!EntityUtils.isStackable(e.getDamager()))
            return;

        StackedEntity stackedEntity = WStackedEntity.of(e.getDamager());

        if (stackedEntity.isNerfed()) {
            e.setCancelled(true);
            e.setDamage(0);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityNerfTeleport(EntityTeleportEvent e) {
        if (!plugin.getSettings().nerfedEntitiesTeleport && EntityUtils.isStackable(e.getEntity()) &&
                WStackedEntity.of(e.getEntity()).isNerfed())
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void handleEntityDamage(EntityDamageEvent e) {
        handleEntityDamage(e, false);
    }

    private static void recallDamageEvent(EntityDamageEvent damageEvent) {
        BiHandlerList entityDamageHandlerList = (BiHandlerList) EntityDamageEvent.getHandlerList();
        try {
            entityDamageHandlerList.setMode(BiHandlerList.Mode.ORIGINAL);
            // Call the event again.
            Bukkit.getPluginManager().callEvent(damageEvent);
        } finally {
            entityDamageHandlerList.setMode(BiHandlerList.Mode.NEW);
        }
    }

    private void handleEntityDamage(EntityDamageEvent damageEvent, boolean fromDeathEvent) {
        // Making sure the entity is stackable
        if (!plugin.getSettings().entitiesStackingEnabled || !EntityUtils.isStackable(damageEvent.getEntity())) {
            // If not, we still want to re-call the damage event
            recallDamageEvent(damageEvent);
            return;
        }

        LivingEntity livingEntity = (LivingEntity) damageEvent.getEntity();
        StackedEntity stackedEntity = WStackedEntity.of(livingEntity);

        // If the entity is already considered as "dead", then we don't deal any damage and return.
        if (stackedEntity.hasFlag(EntityFlag.DEAD_ENTITY)) {
            damageEvent.setDamage(0);
            return;
        }

        // We call a fake event and cancel the original one, if needed.

        EntityDamageData entityDamageData = null;

        if (!fromDeathEvent) {
            damageEvent.setCancelled(true);
            entityDamageData = new EntityDamageData(damageEvent);
            recallDamageEvent(damageEvent);
        }

        if (!damageEvent.isCancelled())
            damageEvent.getEntity().setLastDamageCause(damageEvent);

        try {
            EntityDamageData damageResult = handleEntityDamageInternal(damageEvent, stackedEntity, fromDeathEvent);

            // We want to restore the original values of the event.
            // If we were called from the death event, we restore it now.
            // Otherwise, the values will be restored in #onEntityDamageMonitor
            // Reminder: The original event in that case is cancelled, therefore no other plugins should touch it.
            if (fromDeathEvent) {
                restoreDamageResult(damageResult, damageEvent);
            } else {
                this.damageResults.put(damageEvent, damageResult);
            }
        } finally {
            if (entityDamageData != null)
                entityDamageData.applyToEvent(damageEvent);
        }
    }

    private EntityDamageData handleEntityDamageInternal(EntityDamageEvent damageEvent, StackedEntity stackedEntity, boolean fromDeathEvent) {
        if (damageEvent.isCancelled())
            return new EntityDamageData(true, 0);

        Entity directDamager = EntityUtils.getDamagerFromEvent(damageEvent, true, true);
        Entity sourceDamager = EntityUtils.getDamagerFromEvent(damageEvent, true, false);

        boolean shouldSimulateDeath;

        if (stackedEntity.getHealth() - damageEvent.getFinalDamage() <= 0) {
            shouldSimulateDeath = true;
        } else if (sourceDamager instanceof Player) {
            ItemStack damagerTool = ((Player) sourceDamager).getItemInHand();
            // In case the entity has enough health to deal with the damage, we check for one shot.
            boolean hasAvoidOneShot = stackedEntity.getAndRemoveFlag(EntityFlag.AVOID_ONE_SHOT) != null;
            shouldSimulateDeath = !hasAvoidOneShot &&
                    plugin.getSettings().entitiesOneShotEnabled &&
                    GeneralUtils.contains(plugin.getSettings().entitiesOneShotWhitelist, stackedEntity) &&
                    plugin.getSettings().entitiesOneShotTools.contains(damagerTool.getType().toString());
        } else {
            shouldSimulateDeath = false;
        }

        if (!shouldSimulateDeath) {
            // The entity will not be killed, therefore the damage result should be identical to the event's results.
            return new EntityDamageData(damageEvent);
        }

        return DeathSimulation.simulateDeath(stackedEntity, damageEvent,
                directDamager, sourceDamager, fromDeathEvent).setShouldEntityDie();
    }

    private void restoreDamageResult(EntityDamageData damageResult, EntityDamageEvent damageEvent) {
        damageEvent.setCancelled(damageResult.isCancelled());

        if (ServerVersion.isEquals(ServerVersion.v1_8) && damageResult.isShouldEntityDie()) {
            // In 1.8, EntityLiving#die does not check for dead flag, causing the entity to actually die.
            // Therefore, we set the health to 0.1 and later restore it.
            plugin.getNMSEntities().setHealthDirectly((LivingEntity) damageEvent.getEntity(), 0.01f, true);
            damageEvent.setDamage(0);
        } else {
            damageResult.applyToEvent(damageEvent);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityTeleport(EntityTeleportEvent e) {
        if (EntityUtils.isStackable(e.getEntity()) && WStackedEntity.of(e.getEntity()).hasFlag(EntityFlag.DEAD_ENTITY))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSlimeSplit(SlimeSplitEvent event) {
        if (!EntityUtils.isStackable(event.getEntity()) ||
                !EntityStorage.hasMetadata(event.getEntity(), EntityFlag.ORIGINAL_AMOUNT))
            return;

        int stackAmount = EntityStorage.removeMetadata(event.getEntity(), EntityFlag.ORIGINAL_AMOUNT, -1);

        if (stackAmount > 1 && event.getCount() > 0) {
            this.slimeSplitTracker.startTracking(stackAmount, event.getCount());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent e) {
        if (duplicateCow && e.getEntityType() == EntityType.COW) {
            duplicateCow = false;
            e.setCancelled(true);
            e.getEntity().getWorld().spawnEntity(e.getLocation(), EntityType.COW);
            return;
        }

        handleEntitySpawn(e.getEntity(), e.getSpawnReason());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerEggUse(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        handleSpawnerEggUse(e.getItem(), e.getClickedBlock(), e.getBlockFace(), e);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntitySpawnFromEgg(CreatureSpawnEvent e) {
        if (!EntityUtils.isStackable(e.getEntity()))
            return;

        Optional<SpawnEggTrackedData> spawnEggDataOptional = spawnEggTracker.getTrackedData();

        if (!spawnEggDataOptional.isPresent() || e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)
            return;

        SpawnEggTrackedData spawnEggData = spawnEggDataOptional.get();

        if (spawnEggData.entityType != null && EntityTypes.fromEntity(e.getEntity()) != spawnEggData.entityType)
            return;

        int stackAmount = spawnEggData.stackAmount;

        EntityStorage.setMetadata(e.getEntity(), EntityFlag.SPAWN_CAUSE, SpawnCause.valueOf(e.getSpawnReason()));
        StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());
        stackedEntity.setStackAmount(stackAmount, false);
        if (stackAmount > 1) {
            // Remove the name tag from the custom name of the egg
            e.getEntity().setCustomName(null);
            Executor.sync(stackedEntity::updateName, 1L);
        }

        if (spawnEggData.upgradeId != 0)
            ((WStackedEntity) stackedEntity).setUpgradeId(spawnEggData.upgradeId);

        spawnEggTracker.resetTracker();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCatchFishWithBucket(PlayerInteractEntityEvent e) {
        ItemStack inHand = e.getPlayer().getItemInHand();

        //noinspection deprecation
        if (!(e.getRightClicked() instanceof Fish) || ServerVersion.isLegacy() || inHand == null || inHand.getType() != Material.WATER_BUCKET)
            return;

        StackedEntity stackedEntity = WStackedEntity.of(e.getRightClicked());

        if (stackedEntity.getStackAmount() <= 1)
            return;

        Material fishBucketType = null;

        try {
            switch (EntityTypes.fromEntity((LivingEntity) e.getRightClicked())) {
                case COD:
                    fishBucketType = Material.valueOf("COD_BUCKET");
                    break;
                case PUFFERFISH:
                    fishBucketType = Material.valueOf("PUFFERFISH_BUCKET");
                    break;
                case SALMON:
                    fishBucketType = Material.valueOf("SALMON_BUCKET");
                    break;
                case TROPICAL_FISH:
                    fishBucketType = Material.valueOf("TROPICAL_FISH_BUCKET");
                    break;
            }
        } catch (Exception ignored) {
        }

        if (fishBucketType == null)
            return;

        e.setCancelled(true);

        ItemStack fishBucketItem = ItemUtils.setSpawnerItemAmount(new ItemStack(fishBucketType), stackedEntity.getStackAmount());

        stackedEntity.remove();

        EquipmentSlot usedHand = ItemUtils.getHand(e);
        ItemUtils.removeItemFromHand(e.getPlayer(), 1, usedHand);

        ItemUtils.addItem(fishBucketItem, e.getPlayer().getInventory(), e.getRightClicked().getLocation());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSlimeSplitAsyncCatch(SlimeSplitEvent e) {
        //Fixes a async catch exception with auto-clear task
        if (!Bukkit.isPrimaryThread())
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSheepDye(SheepDyeWoolEvent e) {
        if (!plugin.getSettings().entitiesStackingEnabled || !StackSplit.SHEEP_DYE.isEnabled())
            return;

        StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());

        DyeColor originalColor = e.getEntity().getColor();

        Executor.sync(() -> {
            if (stackedEntity.getStackAmount() > 1) {
                e.getEntity().setColor(originalColor);
                stackedEntity.decreaseStackAmount(1, true);
                StackedEntity duplicate = stackedEntity.spawnDuplicate(1);
                ((Sheep) duplicate.getLivingEntity()).setColor(e.getColor());
                duplicate.runStackAsync(null);
            } else {
                stackedEntity.runStackAsync(null);
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerShearEntity(PlayerShearEntityEvent e) {
        handleEntityShear(e, e.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWoolRegrow(SheepRegrowWoolEvent e) {
        if (!plugin.getSettings().entitiesStackingEnabled)
            return;

        StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());

        if (!e.getEntity().isSheared())
            return;

        Executor.sync(() -> {
            if (stackedEntity.getStackAmount() > 1) {
                e.getEntity().setSheared(true);
                stackedEntity.decreaseStackAmount(1, true);
                StackedEntity duplicated = stackedEntity.spawnDuplicate(1);
                ((Sheep) duplicated.getLivingEntity()).setSheared(false);
                duplicated.runStackAsync(null);
            } else {
                stackedEntity.runStackAsync(null);
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityNameTag(PlayerInteractEntityEvent e) {
        ItemStack inHand = e.getPlayer().getInventory().getItemInHand();

        if (inHand == null || inHand.getType() != Material.NAME_TAG || !inHand.hasItemMeta() || !inHand.getItemMeta().hasDisplayName()
                || e.getRightClicked() instanceof EnderDragon || !EntityUtils.isStackable(e.getRightClicked()))
            return;

        StackedEntity stackedEntity = WStackedEntity.of(e.getRightClicked());

        if (plugin.getSettings().entitiesStackingEnabled && StackSplit.NAME_TAG.isEnabled()) {
            Executor.sync(() -> {
                if (stackedEntity.getStackAmount() > 1) {
                    stackedEntity.setCustomName("");
                    stackedEntity.decreaseStackAmount(1, true);
                    StackedEntity duplicated = stackedEntity.spawnDuplicate(1);
                    duplicated.setCustomName(inHand.getItemMeta().getDisplayName());
                    ((WStackedEntity) duplicated).setNameTag();
                } else {
                    ((WStackedEntity) stackedEntity).setNameTag();
                    stackedEntity.runStackAsync(null);
                }
            }, 2L);
        } else {
            ((WStackedEntity) stackedEntity).setNameTag();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityFeed(PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof Animals) || ItemUtils.isOffHand(e))
            return;

        ItemStack inHand = e.getPlayer().getItemInHand();

        if (!plugin.getNMSEntities().isAnimalFood((Animals) e.getRightClicked(), inHand))
            return;

        if (!EntityUtils.canBeBred((Animals) e.getRightClicked()))
            return;

        StackedEntity stackedEntity = WStackedEntity.of(e.getRightClicked());
        int inventoryItemsAmount = plugin.getSettings().smartBreedingConsumeEntireInventory ?
                ItemUtils.countItem(e.getPlayer().getInventory(), inHand) : inHand.getAmount();

        if (stackedEntity.getStackAmount() > 1) {
            int itemsAmountToRemove;

            if (plugin.getSettings().smartBreedingEnabled) {
                int breedableAmount = e.getPlayer().getGameMode() == GameMode.CREATIVE ?
                        stackedEntity.getStackAmount() :
                        Math.min(stackedEntity.getStackAmount(), inventoryItemsAmount);

                if (breedableAmount % 2 != 0)
                    breedableAmount--;

                if (breedableAmount < 2)
                    return;

                // Setting the entity to be in love-mode.
                plugin.getNMSEntities().setInLove((Animals) e.getRightClicked(), e.getPlayer(), true);

                itemsAmountToRemove = breedableAmount;

                ItemStack inHandCopy = inHand.clone();

                Executor.sync(() -> {
                    // Spawning the baby after 2.5 seconds
                    int babiesAmount = itemsAmountToRemove / 2;

                    // Making sure the entities are not in a love-mode anymore.
                    plugin.getNMSEntities().setInLove((Animals) e.getRightClicked(), e.getPlayer(), false);

                    // Resetting the breeding of the entity to 5 minutes
                    ((Animals) e.getRightClicked()).setAge(6000);

                    // Calculate exp to drop
                    int expToDrop = Random.nextInt(1, 7, babiesAmount);

                    LivingEntity childEntity;

                    if (EntityTypes.fromEntity(stackedEntity.getLivingEntity()) == EntityTypes.TURTLE) {
                        // Turtles should lay an egg instead of spawning a baby.
                        plugin.getNMSEntities().setTurtleEgg(stackedEntity.getLivingEntity());
                        stackedEntity.setFlag(EntityFlag.BREEDABLE_AMOUNT, babiesAmount);
                        childEntity = null;
                    } else {
                        StackedEntity duplicated = ((WStackedEntity) stackedEntity).spawnDuplicate(babiesAmount, SpawnCause.BREEDING, _childEntity -> {
                            return plugin.getNMSEntities().callEntityBreedEvent((LivingEntity) _childEntity, (LivingEntity) e.getRightClicked(),
                                    (LivingEntity) e.getRightClicked(), e.getPlayer(), inHandCopy, expToDrop);
                        });
                        childEntity = duplicated == null ? null : duplicated.getLivingEntity();
                    }

                    if (childEntity != null) {
                        ((Animals) childEntity).setBaby();
                        EntityUtils.spawnExp(stackedEntity.getLocation(), expToDrop);
                    }
                }, 50L);
            } else if (StackSplit.ENTITY_BREED.isEnabled()) {
                stackedEntity.decreaseStackAmount(1, true);
                StackedEntity duplicated = stackedEntity.spawnDuplicate(1, SpawnCause.BREEDING);
                plugin.getNMSEntities().setInLove((Animals) duplicated.getLivingEntity(), e.getPlayer(), true);
                itemsAmountToRemove = 1;
            } else {
                return;
            }

            e.setCancelled(true);

            if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                int inHandItemsAmount = inHand.getAmount();
                EquipmentSlot usedHand = ItemUtils.getHand(e);

                if (itemsAmountToRemove >= inHandItemsAmount) {
                    ItemUtils.setItemInHand(e.getPlayer().getInventory(), usedHand, null);
                    if (itemsAmountToRemove > inHandItemsAmount)
                        ItemUtils.removeItemFromHand(e.getPlayer().getInventory(), inHand, itemsAmountToRemove - inHandItemsAmount);
                } else {
                    ItemStack newItem = inHand.clone();
                    newItem.setAmount(inHandItemsAmount - itemsAmountToRemove);
                    ItemUtils.setItemInHand(e.getPlayer().getInventory(), usedHand, newItem);
                }

            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBreed(CreatureSpawnEvent e) {
        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BREEDING && plugin.getSettings().stackAfterBreed) {
            EntitiesGetter.getNearbyEntities(e.getEntity().getLocation(), 5, entity -> EntityUtils.isStackable(entity) &&
                            (!(entity instanceof Animals) || !plugin.getNMSEntities().isInLove((Animals) entity)))
                    .forEach(entity -> WStackedEntity.of(entity).runStackAsync(null));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMobAgro(EntityTargetLivingEntityEvent e) {
        if (!(e.getEntity() instanceof LivingEntity) || !(e.getTarget() instanceof Player))
            return;

        EntityTypes entityType = EntityTypes.fromEntity((LivingEntity) e.getEntity());

        switch (entityType) {
            case BEE:
                if (!StackSplit.BEE_AGRO.isEnabled())
                    return;
                break;
            case IRON_GOLEM:
                if (!StackSplit.IRON_GOLEM_AGRO.isEnabled())
                    return;
                break;
            case WOLF:
                if (!StackSplit.WOLF_AGRO.isEnabled())
                    return;
                break;
            case PIGLIN:
            case ZOMBIE_PIGMAN:
                if (!StackSplit.PIGMAN_AGRO.isEnabled())
                    return;
                break;
            default:
                return;
        }

        Creature creature = (Creature) e.getEntity();
        StackedEntity stackedEntity = WStackedEntity.of(creature);

        if (stackedEntity.getStackAmount() > 1) {
            for (int i = 0; i < stackedEntity.getStackAmount() - 1; i++) {
                Executor.sync(() -> {
                    if (creature.getTarget() != null) {
                        StackedEntity duplicate = stackedEntity.spawnDuplicate(1);
                        ((Creature) duplicate.getLivingEntity()).setTarget(creature.getTarget());
                        ((WStackedEntity) duplicate).setStackFlag(entity -> ((Creature) entity).getTarget() == null);
                        stackedEntity.setStackAmount(stackedEntity.getStackAmount() - 1, true);
                    }
                }, i * 20);
            }
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent e) {
        if (!EntityUtils.isStackable(e.getEntered()))
            return;

        StackedEntity stackedEntity = WStackedEntity.of(e.getEntered());

        if (stackedEntity.getStackAmount() <= 1 || !StackSplit.ENTER_VEHICLE.isEnabled() ||
                stackedEntity.hasFlag(EntityFlag.ADD_TO_VEHICLE))
            return;

        e.setCancelled(true);

        if (!plugin.getSettings().entitiesFillVehicles &&
                plugin.getNMSEntities().getPassengersCount(e.getVehicle()) >= 1 && stackedEntity.getStackAmount() > 1)
            return;

        stackedEntity.decreaseStackAmount(1, true);
        StackedEntity duplicated = stackedEntity.spawnDuplicate(1);

        try {
            stackedEntity.setFlag(EntityFlag.ADD_TO_VEHICLE, true);
            plugin.getNMSEntities().enterVehicle(e.getVehicle(), duplicated.getLivingEntity());
        } finally {
            stackedEntity.removeFlag(EntityFlag.ADD_TO_VEHICLE);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleExit(VehicleExitEvent e) {
        if (!EntityUtils.isStackable(e.getExited()))
            return;

        StackedEntity stackedEntity = WStackedEntity.of(e.getExited());
        stackedEntity.runStackAsync(null);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCreatureSpawnMonitor(CreatureSpawnEvent event) {
        if (event.isCancelled())
            handleEntityCacheClear(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawnerSpawnMonitor(SpawnerSpawnEvent event) {
        if (event.isCancelled() && event.getEntity() instanceof LivingEntity)
            handleEntityCacheClear((LivingEntity) event.getEntity());
    }

    // When a lightning hits pigs, the EntityTransformEvent is not called for no reason.
    // This trick should do the job.
    // For reference, https://github.com/BG-Software-LLC/WildStacker/issues/481
    @EventHandler(priority = EventPriority.MONITOR)
    public void onLightningStrike(LightningStrikeEvent event) {
        if (event.getLightning().isEffect())
            return;

        List<Integer> nearbyEntitiesAmounts = event.getWorld().getNearbyEntities(event.getLightning().getLocation(), 2, 2, 2)
                .stream().filter(entity -> entity instanceof Pig).map(entity -> WStackedEntity.of(entity).getStackAmount())
                .collect(Collectors.toList());

        if (!nearbyEntitiesAmounts.isEmpty()) {
            Executor.sync(() -> {
                for (Entity entity : event.getWorld().getNearbyEntities(event.getLightning().getLocation(), 2, 2, 2)) {
                    if (entity instanceof PigZombie) {
                        this.handleEntityTransform(entity, "LIGHTNING", nearbyEntitiesAmounts.remove(0), SpawnCause.LIGHTNING);
                    }
                }
            }, 2L);
        }
    }

    /*
     *  General methods
     */

    public boolean handleSpawnerEggUse(ItemStack usedItem, Block clickedBlock, BlockFace blockFace, PlayerInteractEvent event) {
        if (!plugin.getSettings().entitiesStackingEnabled || usedItem == null ||
                plugin.getSettings().blacklistedEntities.contains(SpawnCause.SPAWNER_EGG) ||
                (!Materials.isValidAndSpawnEgg(usedItem) && !Materials.isFishBucket(usedItem)))
            return false;

        SpawnEggTrackedData trackedData = new SpawnEggTrackedData();
        trackedData.stackAmount = ItemUtils.getSpawnerItemAmount(usedItem);
        trackedData.upgradeId = ItemUtils.getSpawnerUpgrade(usedItem);

        if (Materials.isValidAndSpawnEgg(usedItem)) {
            trackedData.entityType = ItemUtils.getEntityType(usedItem);

            if (trackedData.entityType == null) {
                this.spawnEggTracker.resetTracker();
                return false;
            }

            EntityType nmsEntityType = ItemUtils.getNMSEntityType(usedItem);
            if (nmsEntityType != null) {
                event.setCancelled(true);
                trackedData.entityType = EntityTypes.fromName(nmsEntityType.name());
                Location toSpawn = clickedBlock.getRelative(blockFace).getLocation().add(0.5, 0, 0.5);
                if (toSpawn.getBlock().getType() != Material.AIR)
                    toSpawn = toSpawn.add(0, 1, 0);
                plugin.getSystemManager().spawnEntityWithoutStacking(toSpawn, nmsEntityType.getEntityClass(), SpawnCause.SPAWNER_EGG);
            }
        }

        this.spawnEggTracker.startTracking(trackedData, 1);

        return true;
    }

    // Handle entity removed from world.
    public void handleEntityRemove(Entity entity) {
        if (EntityUtils.isStackable(entity)) {
            StackedEntity stackedEntity = plugin.getDataHandler().CACHED_ENTITIES.remove(entity.getUniqueId());
            if (stackedEntity != null)
                plugin.getSystemManager().saveEntity(stackedEntity);
        } else if (entity instanceof Item) {
            StackedItem stackedItem = plugin.getDataHandler().CACHED_ITEMS.remove(entity.getUniqueId());
            if (stackedItem != null)
                plugin.getSystemManager().saveItem(stackedItem);
        }
        Executor.sync(() -> EntityStorage.clearMetadata(entity), 100L);
    }

    private void handleEntitySpawn(LivingEntity entity, CreatureSpawnEvent.SpawnReason spawnReason) {
        if (!plugin.getSettings().entitiesStackingEnabled)
            return;

        if (!EntityUtils.isStackable(entity) || EntityStorage.hasMetadata(entity, EntityFlag.CORPSE))
            return;

        EntitiesGetter.handleEntitySpawn(entity);

        SpawnCause spawnCause = SpawnCause.valueOf(spawnReason);

        EntityStorage.setMetadata(entity, EntityFlag.SPAWN_CAUSE, spawnCause);
        StackedEntity stackedEntity = WStackedEntity.of(entity);

        // In case the entity with a name already, we want to treat is as he was nametagged
        if (entity.getCustomName() != null) {
            if ((stackedEntity.getStackAmount() <= 1 && stackedEntity.isDefaultUpgrade()) ||
                    !entity.getCustomName().equals(EntityUtils.getEntityName(stackedEntity)))
                ((WStackedEntity) stackedEntity).setNameTag();
        }

        if (stackedEntity.getType() == EntityType.COW) {
            mushroomTracker.getTrackedData().ifPresent(mushroomCount -> {
                stackedEntity.setStackAmount(mushroomCount, true);
                mushroomTracker.decreaseTrackCount();
            });
        }

        if (spawnReason == CreatureSpawnEvent.SpawnReason.SLIME_SPLIT) {
            this.slimeSplitTracker.getTrackedData().ifPresent(originalStackAmount -> {
                stackedEntity.setStackAmount(originalStackAmount, true);
                this.slimeSplitTracker.decreaseTrackCount();
            });
        }

        if (spawnCause == SpawnCause.BEEHIVE && EntityTypes.fromEntity(entity) == EntityTypes.BEE) {
            org.bukkit.entity.Bee bee = (org.bukkit.entity.Bee) entity;
            Integer[] beesAmount = EntitiesListener.beesAmount.get(bee.getHive());
            if (beesAmount != null) {
                for (int i = beesAmount.length - 1; i >= 0; i--) {
                    if (beesAmount[i] != -1) {
                        stackedEntity.setStackAmount(beesAmount[i], true);
                        beesAmount[i] = -1;
                        break;
                    }
                    if (i == 0)
                        EntitiesListener.beesAmount.remove(bee.getHive());
                }
            }
        } else if (spawnCause == SpawnCause.EGG && EntityTypes.fromEntity(entity) == EntityTypes.TURTLE) {
            Location homeLocation = plugin.getNMSEntities().getTurtleHome(entity);
            Integer cachedEggs = homeLocation == null ? null : turtleEggsAmounts.remove(homeLocation);
            if (cachedEggs != null && cachedEggs > 1) {
                int newBabiesAmount = Random.nextInt(1, 4, cachedEggs);
                int stackLimit = stackedEntity.getStackLimit();

                if (newBabiesAmount > stackLimit) {
                    int amountOfNewEntities = newBabiesAmount / stackLimit;
                    for (int i = 0; i < amountOfNewEntities; ++i) {
                        stackedEntity.spawnDuplicate(stackLimit, SpawnCause.EGG);
                    }
                    newBabiesAmount -= (stackLimit * amountOfNewEntities);
                }

                if (newBabiesAmount > 0)
                    WStackedEntity.of(entity).setStackAmount(newBabiesAmount, true);
            }
        }

        if (!stackedEntity.isCached())
            return;

        //Chunk Limit
        Executor.sync(() -> {
            if (isChunkLimit(entity.getLocation().getChunk()))
                stackedEntity.remove();
        }, 5L);

        if (stackedEntity.getAndRemoveFlag(EntityFlag.BYPASS_STACKING) != null) {
            return;
        }

        //EpicSpawners has it's own event
        if (stackedEntity.getSpawnCause() == SpawnCause.EPIC_SPAWNERS)
            return;

        if (!plugin.getSettings().spawnersStackingEnabled && plugin.getProviders().handleEntityStackingInsideEvent() &&
                spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER)
            return;

        Consumer<Optional<LivingEntity>> entityConsumer = entityOptional -> {
            if (!entityOptional.isPresent())
                stackedEntity.updateNerfed();
        };

        boolean stackWithDelay = Optional.ofNullable((Boolean) EntityStorage.removeMetadata(entity, EntityFlag.DELAY_STACK)).orElse(false);

        //Need to add a delay so eggs will get removed from inventory
        if (stackWithDelay || spawnCause == SpawnCause.SPAWNER_EGG || spawnCause == SpawnCause.CUSTOM ||
                spawnCause == SpawnCause.COMMAND || entity.getType() == EntityType.WITHER ||
                entity.getType() == EntityType.IRON_GOLEM || entity.getType() == EntityType.SNOWMAN ||
                EntityTypes.fromEntity(entity).isSlime() || plugin.getProviders().handleEntityStackingWithDelay())
            Executor.sync(() -> stackedEntity.runStackAsync(entityConsumer), 1L);
        else
            stackedEntity.runStackAsync(entityConsumer);
    }

    private void handleEntityCacheClear(LivingEntity livingEntity) {
        // Removing the entity from cache.
        plugin.getDataHandler().CACHED_ENTITIES.remove(livingEntity.getUniqueId());
        EntityStorage.clearMetadata(livingEntity);
    }

    private void handleEntityShear(Cancellable cancellable, Entity entity) {
        if (!plugin.getSettings().entitiesStackingEnabled || !EntityUtils.isStackable(entity))
            return;

        StackedEntity stackedEntity = WStackedEntity.of(entity);

        if (entity instanceof MushroomCow) {
            if (StackSplit.MUSHROOM_SHEAR.isEnabled()) {
                /* When mushroom cows are sheared, they are removed from the world and a new cow is spawned.
                Therefore, we need to do two things. First, spawn a duplicate of the mushroom cow so it doesn't disappear.
                Secondly, we want to spawn a new cow. This is because the original cow is considered invalid when the
                spawn event is called. The mushroomSpawn is used to determine when the cow is spawned and to spawn another. */
                if (stackedEntity.getStackAmount() > 1) {
                    stackedEntity.spawnDuplicate(stackedEntity.getStackAmount() - 1, SpawnCause.SHEARED);
                }
                duplicateCow = true;
            } else {
                int mushroomAmount = 5 * (stackedEntity.getStackAmount() - 1);
                ItemStack mushroomItem = new ItemStack(Material.RED_MUSHROOM, mushroomAmount);
                ItemUtils.dropItem(mushroomItem, entity.getLocation());
                mushroomTracker.startTracking(stackedEntity.getStackAmount(), 1);
            }
        } else if (entity instanceof Sheep) {
            int stackAmount = stackedEntity.getStackAmount();
            if (StackSplit.SHEEP_SHEAR.isEnabled()) {
                Executor.sync(() -> {
                    if (stackAmount > 1) {
                        ((Sheep) entity).setSheared(false);
                        stackedEntity.setStackAmount(stackAmount - 1, true);
                        StackedEntity duplicate = stackedEntity.spawnDuplicate(1, SpawnCause.SHEARED);
                        ((Sheep) duplicate.getLivingEntity()).setSheared(true);
                        duplicate.runStackAsync(null);
                    } else {
                        stackedEntity.runStackAsync(null);
                    }
                }, 0L);
            } else if (stackAmount > 1) {
                int woolAmount = ThreadLocalRandom.current().nextInt(2 * (stackAmount - 1)) + stackAmount - 1;
                ItemStack woolItem = Materials.getWool((((Sheep) entity).getColor()));
                woolItem.setAmount(woolAmount);
                ItemUtils.dropItem(woolItem, entity.getLocation());
            }
        }
    }

    private void handleEntityTransform(Entity entityBukkit, Entity transformedEntityBukkit, String reason) {
        StackedEntity stackedEntity = WStackedEntity.of(entityBukkit);

        if (stackedEntity.getStackAmount() > 1) {
            if (this.handleEntityTransform(transformedEntityBukkit, reason, stackedEntity.getStackAmount(), stackedEntity.getSpawnCause()))
                stackedEntity.remove();
        }
    }

    private boolean handleEntityTransform(Entity transformedEntityBukkit, String reason, int originalStackAmount, SpawnCause defaultCause) {
        if (!plugin.getSettings().entitiesFilteredTransforms.contains(reason))
            return false;

        StackedEntity transformedEntity = WStackedEntity.of(transformedEntityBukkit);
        boolean multipleEntities = !transformedEntity.isCached();

        SpawnCause spawnCause;

        try {
            spawnCause = SpawnCause.valueOf(reason);
        } catch (IllegalArgumentException ex) {
            spawnCause = defaultCause;
        }

        if (multipleEntities) {
            for (int i = 0; i < originalStackAmount - 1; i++) {
                plugin.getSystemManager().spawnEntityWithoutStacking(transformedEntityBukkit.getLocation(),
                        transformedEntityBukkit.getType().getEntityClass(), spawnCause);
            }
        } else {
            StackedEntity transformed = WStackedEntity.of(transformedEntityBukkit);
            transformed.setStackAmount(originalStackAmount, true);
            transformed.setSpawnCause(spawnCause);
            transformed.updateNerfed();
            Executor.sync(() -> transformed.runStackAsync(null), 1L);
        }

        return true;
    }

    private boolean isChunkLimit(Chunk chunk) {
        int chunkLimit = plugin.getSettings().entitiesChunkLimit;

        if (chunkLimit <= 0)
            return false;

        return (int) Arrays.stream(chunk.getEntities()).filter(EntityUtils::isStackable).count() > chunkLimit;
    }

    private static EntityDamageEvent createDamageEvent(Entity entity, EntityDamageEvent.DamageCause damageCause, double damage, Entity damager) {
        Map<EntityDamageEvent.DamageModifier, Double> damageModifiers = Maps.newEnumMap(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, damage));
        if (damager == null) {
            return new EntityDamageEvent(entity, damageCause, damageModifiers, damageModifiersFunctions);
        } else {
            return new EntityDamageByEntityEvent(damager, entity, damageCause, damageModifiers, damageModifiersFunctions);
        }
    }

    private class TransformListener implements Listener {

        @EventHandler
        public void onEntityTransform(org.bukkit.event.entity.EntityTransformEvent e) {
            handleEntityTransform(e.getEntity(), e.getTransformedEntity(), e.getTransformReason().name());
        }

    }

    private class BeeListener implements Listener {

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onBee(EntityEnterBlockEvent e) {
            if (e.getBlock().getState() instanceof Beehive && EntityUtils.isStackable(e.getEntity())) {
                Beehive beehive = (Beehive) e.getBlock().getState();
                StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());

                if (!stackedEntity.isCached())
                    return;

                Integer[] arr = beesAmount.computeIfAbsent(e.getBlock().getLocation(), l -> {
                    Integer[] newArr = new Integer[beehive.getEntityCount()];
                    Arrays.fill(newArr, -1);
                    return newArr;
                });

                for (int i = 0; i < arr.length; i++) {
                    if (arr[i] == -1) {
                        arr[i] = stackedEntity.getStackAmount();
                        break;
                    }
                }

                plugin.getSystemManager().removeStackObject(stackedEntity);
            }
        }

    }

    private class TurtleListener implements Listener {

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onTurtleEggLay(EntityChangeBlockEvent e) {
            if (!plugin.getSettings().smartBreedingEnabled)
                return;

            if (!(e.getEntity() instanceof org.bukkit.entity.Turtle) || e.getTo() != TURTLE_EGG)
                return;

            StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());

            Integer breedableAmount = stackedEntity.getAndRemoveFlag(EntityFlag.BREEDABLE_AMOUNT);
            if (breedableAmount == null || breedableAmount <= 1)
                return;

            Executor.sync(() -> plugin.getNMSWorld().setTurtleEggsAmount(e.getBlock(), 1), 1L);
            turtleEggsAmounts.put(e.getBlock().getLocation(), breedableAmount);
        }

    }

    private class BlockShearEntityListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onBlockShearEntity(BlockShearEntityEvent e) {
            handleEntityShear(e, e.getEntity());
        }

    }

    private class EntityDamageRestoreListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false /* Not ignoring cancel status as the event should be cancelled. */)
        public void onEntityDamageRestore(EntityDamageEvent e) {
            Optional.ofNullable(damageResults.remove(e)).ifPresent(result -> restoreDamageResult(result, e));
        }

    }

    private static class SpawnEggTrackedData {

        private int stackAmount;
        private int upgradeId;
        private EntityTypes entityType;

        public SpawnEggTrackedData() {

        }

    }

}
