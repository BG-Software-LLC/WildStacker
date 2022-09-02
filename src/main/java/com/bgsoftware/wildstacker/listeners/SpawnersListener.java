package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.api.enums.UnstackResult;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.hooks.listeners.IStackedBlockListener;
import com.bgsoftware.wildstacker.menu.SpawnersManageMenu;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.bgsoftware.wildstacker.utils.Random;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.entity.EntitiesGetter;
import com.bgsoftware.wildstacker.utils.entity.EntityStorage;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.events.EventsCaller;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.pair.Pair;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unused")
public final class SpawnersListener implements Listener {

    private static final BlockFace[] blockFaces = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

    private final Set<UUID> inventoryTweaksToggleCommandPlayers = new HashSet<>();
    private final Set<UUID> alreadySpawnersPlacedPlayers = new HashSet<>();
    private final Map<Entity, UUID> explodableSources = new WeakHashMap<>();
    private final Set<Location> paperPreSpawnChecked = new HashSet<>();

    private final WildStackerPlugin plugin;
    private boolean listenToSpawnEvent = true;

    public SpawnersListener(WildStackerPlugin plugin) {
        this.plugin = plugin;

        try {
            Class.forName("com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent");
            plugin.getServer().getPluginManager().registerEvents(new PaperSpawnersListener(), plugin);
        } catch (Exception ignored) {
        }
    }

    public static boolean handleSpawnerBreak(WildStackerPlugin plugin, StackedSpawner stackedSpawner, int breakAmount, Player player, boolean breakMenu) {
        Pair<Double, Boolean> chargeInfo = plugin.getSettings().spawnersBreakCharge
                .getOrDefault(stackedSpawner.getSpawnedType(), new Pair<>(0.0, false));

        double amountToCharge = chargeInfo.getKey() * (chargeInfo.getValue() ? breakAmount : 1);

        if (amountToCharge > 0 && plugin.getProviders().getEconomyProvider().getMoneyInBank(player) < amountToCharge) {
            Locale.SPAWNER_BREAK_NOT_ENOUGH_MONEY.send(player, amountToCharge);
            return false;
        }

        if (stackedSpawner.runUnstack(breakAmount, player) == UnstackResult.SUCCESS) {
            Block block = stackedSpawner.getLocation().getBlock();

            plugin.getProviders().notifyStackedBlockListeners(player, block, IStackedBlockListener.Action.BLOCK_BREAK);

            plugin.getProviders().getSpawnersProvider().handleSpawnerBreak(stackedSpawner, player, breakAmount, breakMenu);

            EntityType entityType = stackedSpawner.getSpawnedType();

            if (stackedSpawner.getStackAmount() <= 0)
                block.setType(Material.AIR);

            if (amountToCharge > 0)
                plugin.getProviders().getEconomyProvider().withdrawMoney(player, amountToCharge);

            Locale.SPAWNER_BREAK.send(player, EntityUtils.getFormattedType(entityType.name()), breakAmount, GeneralUtils.format(amountToCharge));

            return true;
        }

        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!plugin.getSettings().spawnersStackingEnabled || e.getBlockPlaced().getType() != Materials.SPAWNER.toBukkitType())
            return;

        if (!alreadySpawnersPlacedPlayers.add(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            return;
        }

        Executor.sync(() -> alreadySpawnersPlacedPlayers.remove(e.getPlayer().getUniqueId()), 2L);

        try {
            StackedSpawner stackedSpawner = WStackedSpawner.of(e.getBlockPlaced());

            if (!stackedSpawner.isCached())
                return;

            ItemStack itemInHand = e.getItemInHand().clone();

            plugin.getProviders().getSpawnersProvider().handleSpawnerPlace(stackedSpawner.getSpawner(), itemInHand);
            EntityType spawnerType = plugin.getProviders().getSpawnersProvider().getSpawnerType(itemInHand);

            int upgradeId = ItemUtils.getSpawnerUpgrade(itemInHand);
            ((WStackedSpawner) stackedSpawner).setUpgradeId(upgradeId, e.getPlayer(), false);

            SpawnerUpgrade spawnerUpgrade = plugin.getUpgradesManager().getUpgrade(upgradeId);
            if (spawnerUpgrade == null)
                spawnerUpgrade = plugin.getUpgradesManager().getDefaultUpgrade(spawnerType);

            if (spawnerUpgrade.getMinSpawnDelay() >= spawnerUpgrade.getMaxSpawnDelay()) {
                stackedSpawner.getSpawner().setDelay(spawnerUpgrade.getMinSpawnDelay());
            } else {
                stackedSpawner.getSpawner().setDelay(ThreadLocalRandom.current().nextInt(
                        spawnerUpgrade.getMinSpawnDelay(), spawnerUpgrade.getMaxSpawnDelay()
                ));
            }

            int spawnerItemAmount = ItemUtils.getSpawnerItemAmount(itemInHand);

            if (plugin.getSettings().spawnersPlacementPermission && !e.getPlayer().hasPermission("wildstacker.place.*") &&
                    !e.getPlayer().hasPermission("wildstacker.place." + spawnerType.name().toLowerCase())) {
                Locale.SPAWNER_PLACE_BLOCKED.send(e.getPlayer(), "wildstacker.place." + spawnerType.name().toLowerCase());
                e.setCancelled(true);
                stackedSpawner.remove();
                return;
            }

            boolean replaceAir = false;
            ItemStack limitItem = null;
            int heldSlot = e.getPlayer().getInventory().getHeldItemSlot();

            boolean stackPermission = e.getPlayer().hasPermission("wildstacker.stack.*") ||
                    e.getPlayer().hasPermission("wildstacker.stack." + spawnerType.name().toLowerCase());

            if (stackPermission && e.getPlayer().isSneaking() && plugin.getSettings().spawnersShiftPlaceStack) {
                replaceAir = true;
                spawnerItemAmount *= itemInHand.getAmount();
            }

            if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                int limit = stackedSpawner.getStackLimit();
                //If the spawnerItemAmount is larger than the spawner limit, we want to give to the player the leftovers
                if (limit < spawnerItemAmount) {
                    limitItem = plugin.getProviders().getSpawnersProvider().getSpawnerItem(
                            stackedSpawner.getSpawner().getSpawnedType(), spawnerItemAmount - limit,
                            stackedSpawner.getUpgrade());
                    //Adding the item to the inventory after the spawner is placed
                    spawnerItemAmount = limit;
                }
            }

            Pair<Double, Boolean> chargeInfo = plugin.getSettings().spawnersPlaceCharge
                    .getOrDefault(spawnerType, new Pair<>(0.0, false));

            double amountToCharge = chargeInfo.getKey() * (chargeInfo.getValue() ? spawnerItemAmount : 1);

            if (amountToCharge > 0 && plugin.getProviders().getEconomyProvider().getMoneyInBank(e.getPlayer()) < amountToCharge) {
                Locale.SPAWNER_PLACE_NOT_ENOUGH_MONEY.send(e.getPlayer(), amountToCharge);
                e.setCancelled(true);
                stackedSpawner.remove();
                return;
            }

            stackedSpawner.setStackAmount(spawnerItemAmount, false);

            Chunk chunk = e.getBlock().getChunk();

            //Stacking spawner
            Optional<CreatureSpawner> spawnerOptional = stackPermission ?
                    stackedSpawner.runStack() : Optional.empty();

            if (!spawnerOptional.isPresent()) {
                if (isChunkLimit(chunk, spawnerType)) {
                    e.setCancelled(true);
                    Locale.CHUNK_LIMIT_EXCEEDED.send(e.getPlayer(), EntityUtils.getFormattedType(stackedSpawner.getSpawnedType().name()) + " Spawners");
                    stackedSpawner.remove();
                    return;
                }

                //Next Spawner Placement
                if (!plugin.getSettings().nextSpawnerPlacement && !e.getPlayer().hasPermission("wildstacker.nextplace")) {
                    for (BlockFace blockFace : blockFaces) {
                        if (e.getBlockPlaced().getRelative(blockFace).getType() == Materials.SPAWNER.toBukkitType()) {
                            Locale.NEXT_SPAWNER_PLACEMENT.send(e.getPlayer());
                            e.setCancelled(true);
                            stackedSpawner.remove();
                            return;
                        }
                    }
                }

                if (plugin.getSettings().onlyOneSpawner) {
                    for (StackedSpawner nearbySpawner : stackedSpawner.getNearbySpawners()) {
                        if (nearbySpawner.getStackAmount() >= nearbySpawner.getStackLimit()) {
                            Locale.ONLY_ONE_SPAWNER.send(e.getPlayer());
                            e.setCancelled(true);
                            stackedSpawner.remove();
                            return;
                        }
                    }
                }

                if (!EventsCaller.callSpawnerPlaceEvent(e.getPlayer(), stackedSpawner, itemInHand)) {
                    e.setCancelled(true);
                    stackedSpawner.remove();
                    return;
                }

                if (ServerVersion.isLessThan(ServerVersion.v1_9)) {
                    boolean REPLACE_AIR = replaceAir;
                    ItemStack LIMIT_ITEM = limitItem;
                    int SPAWNER_ITEM_AMOUNT = spawnerItemAmount;

                    Executor.sync(() -> {
                        if (e.getBlockPlaced().getType() != Materials.SPAWNER.toBukkitType())
                            return;

                        stackedSpawner.updateName();

                        finishSpawnerPlace(e.getPlayer(), amountToCharge, REPLACE_AIR, itemInHand, LIMIT_ITEM, spawnerType, SPAWNER_ITEM_AMOUNT);
                    }, 1L);

                    return;
                }

                stackedSpawner.updateName();
            } else {
                e.setCancelled(true);

                revokeItem(e.getPlayer(), itemInHand);

                StackedSpawner targetSpawner = WStackedSpawner.of(spawnerOptional.get());

                plugin.getProviders().notifyStackedBlockListeners(e.getPlayer(), targetSpawner.getLocation(),
                        Materials.SPAWNER.toBukkitType(), (byte) 0, IStackedBlockListener.Action.BLOCK_PLACE);

                spawnerItemAmount = targetSpawner.getStackAmount();
            }

            finishSpawnerPlace(e.getPlayer(), amountToCharge, replaceAir, itemInHand, limitItem, spawnerType, spawnerItemAmount);
        } catch (Exception ex) {
            alreadySpawnersPlacedPlayers.remove(e.getPlayer().getUniqueId());
            throw ex;
        }
    }

    private void revokeItem(Player player, ItemStack itemInHand) {
        if (player.getGameMode() != GameMode.CREATIVE) {
            ItemStack inHand = itemInHand.clone();
            inHand.setAmount(inHand.getAmount() - 1);
            //Using this method as remove() doesn't affect off hand
            ItemUtils.setItemInHand(player.getInventory(), itemInHand, inHand);
        }
    }

    private void finishSpawnerPlace(Player player, double amountToCharge, boolean replaceAir, ItemStack itemInHand, ItemStack limitItem, EntityType spawnerType, int spawnerItemAmount) {
        if (amountToCharge > 0)
            plugin.getProviders().getEconomyProvider().withdrawMoney(player, amountToCharge);

        //Removing item from player's inventory
        if (player.getGameMode() != GameMode.CREATIVE && replaceAir)
            ItemUtils.setItemInHand(player.getInventory(), itemInHand, new ItemStack(Material.AIR));

        if (limitItem != null)
            ItemUtils.addItem(limitItem, player.getInventory(), player.getLocation());

        Locale.SPAWNER_PLACE.send(player, EntityUtils.getFormattedType(spawnerType.name()), spawnerItemAmount, GeneralUtils.format(amountToCharge));

        alreadySpawnersPlacedPlayers.remove(player.getUniqueId());
    }

    //Priority is high so it can be fired before SilkSpawners
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (!plugin.getSettings().spawnersStackingEnabled || e.getBlock().getType() != Materials.SPAWNER.toBukkitType())
            return;

        StackedSpawner stackedSpawner = WStackedSpawner.of(e.getBlock());
        CreatureSpawner creatureSpawner = (CreatureSpawner) e.getBlock().getState();

        e.setCancelled(true);

        if (e.getPlayer().getGameMode() != GameMode.CREATIVE && plugin.getSettings().spawnersMineRequireSilk &&
                !ItemUtils.isPickaxeAndHasSilkTouch(e.getPlayer().getItemInHand())) {
            Locale.SPAWNER_BREAK_WITHOUT_SILK.send(e.getPlayer());
            return;
        }

        int originalAmount = stackedSpawner.getStackAmount();
        int stackAmount = e.getPlayer().isSneaking() && plugin.getSettings().shiftGetWholeSpawnerStack ? originalAmount : 1;

        handleSpawnerBreak(plugin, stackedSpawner, stackAmount, e.getPlayer(), false);
    }

    //Priority is high so it can be fired before SilkSpawners
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        if (!plugin.getSettings().spawnersStackingEnabled)
            return;

        List<Block> blockList = new ArrayList<>(e.blockList());

        for (Block block : blockList) {
            //Making sure it's a spawner
            if (block.getType() != Materials.SPAWNER.toBukkitType())
                continue;

            StackedSpawner stackedSpawner = WStackedSpawner.of(block);
            CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();

            UUID explodeSource = explodableSources.get(e.getEntity());
            Player sourcePlayer = null;

            if (e.getEntity() instanceof TNTPrimed) {
                Entity igniter = ((TNTPrimed) e.getEntity()).getSource();
                if (igniter instanceof Player) {
                    sourcePlayer = (Player) igniter;
                }
            } else {
                sourcePlayer = explodeSource == null ? null : Bukkit.getPlayer(explodeSource);
            }

            int breakAmount = plugin.getSettings().explosionsBreakPercentage == -1 ? 1 :
                    (int) Math.round((plugin.getSettings().explosionsBreakPercentage / 100.0) * stackedSpawner.getStackAmount());
            // Should fix issues with amount-percentage being below 100 on low stack sizes.
            breakAmount = Math.max(breakAmount, plugin.getSettings().explosionsBreakMinimum);

            int dropAmount = (int) Math.round((plugin.getSettings().explosionsAmountPercentage / 100.0) * breakAmount);
            // Should fix issues with amount-percentage being below 100 on low stack sizes.
            dropAmount = Math.max(dropAmount, plugin.getSettings().explosionsAmountMinimum);

            plugin.getProviders().getSpawnersProvider().handleSpawnerExplode(stackedSpawner,
                    e.getEntity(), sourcePlayer, dropAmount);

            stackedSpawner.runUnstack(breakAmount, e.getEntity());

            if (stackedSpawner.getStackAmount() > 0)
                e.blockList().remove(block);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityExplodeMonitor(EntityExplodeEvent e) {
        explodableSources.remove(e.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExplodableLight(PlayerInteractEntityEvent e) {
        if (plugin.getSettings().explosionsDropToInventory && e.getRightClicked() instanceof Creeper &&
                e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType() == Material.FLINT_AND_STEEL)
            explodableSources.put(e.getRightClicked(), e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExplodableLight(PlayerInteractEvent e) {
        if (plugin.getSettings().explosionsDropToInventory && e.getClickedBlock() != null &&
                e.getClickedBlock().getType() == Material.TNT && e.getItem() != null && e.getItem().getType().equals(Material.FLINT_AND_STEEL)) {
            Location location = e.getClickedBlock().getLocation();
            Executor.sync(() -> {
                EntitiesGetter.getNearbyEntities(location, 1, entity -> entity instanceof TNTPrimed)
                        .findFirst().ifPresent(entity -> explodableSources.put(entity, e.getPlayer().getUniqueId()));
            }, 2L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExplodableLight(EntityTargetEvent e) {
        if (e.getEntity() instanceof Creeper) {
            if (e.getTarget() instanceof Player)
                explodableSources.put(e.getEntity(), e.getTarget().getUniqueId());
            else
                explodableSources.remove(e.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSpawnerSpawn(SpawnerSpawnEvent e) {
        if (!listenToSpawnEvent || !(e.getEntity() instanceof LivingEntity))
            return;

        if (plugin.getSettings().listenPaperPreSpawnEvent && !paperPreSpawnChecked.remove(e.getSpawner().getLocation()))
            return;

        StackedSpawner stackedSpawner = WStackedSpawner.of(e.getSpawner());

        if (plugin.getSettings().spawnersOverrideEnabled) {
            // We make sure the spawner is still overridden by WildStacker
            // If the spawner was updated again, it will return true, and therefore we must calculate the mobs again.
            if (!plugin.getNMSSpawners().updateStackedSpawner(stackedSpawner))
                return;
        }

        EntityStorage.setMetadata(e.getEntity(), EntityFlag.SPAWN_CAUSE, SpawnCause.SPAWNER);
        StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());

        stackedEntity.updateNerfed();

        ((WStackedEntity) stackedEntity).setUpgradeId(((WStackedSpawner) stackedSpawner).getUpgradeId());

        int minimumEntityRequirement = GeneralUtils.get(plugin.getSettings().minimumRequiredEntities, stackedEntity, 1);

        boolean multipleEntities = !stackedEntity.isCached() ||
                minimumEntityRequirement > stackedSpawner.getStackAmount() ||
                !EventsCaller.callSpawnerStackedEntitySpawnEvent(e.getSpawner());

        if (multipleEntities || stackedSpawner.getStackAmount() > stackedEntity.getStackLimit()) {
            if (stackedSpawner.getStackAmount() > 1)
                spawnEntities(stackedSpawner, stackedEntity, stackedSpawner.getStackAmount(), multipleEntities ? 1 : stackedEntity.getStackLimit());
        } else {
            stackedEntity.setStackAmount(stackedSpawner.getStackAmount(), true);
            stackedEntity.runSpawnerStackAsync(stackedSpawner, null);
        }
    }

    private boolean callSpawnerSpawnEvent(StackedEntity stackedEntity, StackedSpawner stackedSpawner) {
        SpawnerSpawnEvent spawnerSpawnEvent = new SpawnerSpawnEvent(stackedEntity.getLivingEntity(), stackedSpawner.getSpawner());
        Bukkit.getPluginManager().callEvent(new SpawnerSpawnEvent(stackedEntity.getLivingEntity(), stackedSpawner.getSpawner()));
        return !spawnerSpawnEvent.isCancelled() && !stackedEntity.getLivingEntity().isDead() && stackedEntity.getLivingEntity().isValid();
    }

    private void spawnEntities(StackedSpawner stackedSpawner, StackedEntity stackedEntity, int amountToSpawn, int limit) {
        Location location = stackedSpawner.getLocation();
        Entity entity = stackedEntity.getLivingEntity();

        Set<Location> locationsToSpawn = new HashSet<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int entitiesToSpawn = (amountToSpawn / limit) + (amountToSpawn % limit != 0 ? 1 : 0);

        stackedEntity.setStackAmount(limit, true);

        if (entitiesToSpawn <= 1)
            return;

        for (int i = 0; i < entitiesToSpawn - 1; i++) {
            Location locationToSpawn = null;
            int tries = 0;

            while ((locationToSpawn == null || !plugin.getNMSAdapter().canSpawnOn(entity, locationToSpawn)) && ++tries <= 5) {
                locationToSpawn = new Location(location.getWorld(),
                        location.getBlockX() + ((random.nextDouble() - random.nextDouble()) * 4.5D),
                        location.getBlockY(),
                        location.getBlockZ() + ((random.nextDouble() - random.nextDouble()) * 4.5D)
                );
            }

            locationsToSpawn.add(locationToSpawn);
        }

        int leftEntities = amountToSpawn - limit;

        try {
            listenToSpawnEvent = false;

            for (Location toSpawn : locationsToSpawn) {
                if (leftEntities <= 0)
                    break;

                StackedEntity targetEntity = WStackedEntity.of(plugin.getSystemManager().spawnEntityWithoutStacking(toSpawn, entity.getClass()));
                plugin.getNMSAdapter().playSpawnEffect(targetEntity.getLivingEntity());

                if (!callSpawnerSpawnEvent(targetEntity, stackedSpawner)) {
                    targetEntity.remove();
                } else {
                    targetEntity.updateNerfed();
                    targetEntity.setStackAmount(Math.min(leftEntities, limit), true);
                }

                leftEntities -= limit;
            }
        } finally {
            listenToSpawnEvent = true;
        }
    }

    //Same as SilkSpawnersSpawnerChangeEvent, but will only work if SilkSpawners is disabled
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerChange(PlayerInteractEvent e) {
        if (e.getItem() == null || e.getAction() != Action.RIGHT_CLICK_BLOCK ||
                !Materials.isValidAndSpawnEgg(e.getItem()) || e.getClickedBlock().getType() != Materials.SPAWNER.toBukkitType())
            return;

        StackedSpawner stackedSpawner = WStackedSpawner.of(e.getClickedBlock());

        if (!plugin.getSettings().changeUsingEggs) {
            e.setCancelled(true);

            if (EntitiesListener.IMP.handleSpawnerEggUse(e.getItem(), e.getClickedBlock(), e.getBlockFace(), e)) {
                try {
                    EntityType entityType = EntityType.valueOf(ItemUtils.getEntityType(e.getItem()).name());
                    plugin.getNMSAdapter().createEntity(e.getClickedBlock().getRelative(e.getBlockFace())
                            .getLocation().add(0.5, 0, 0.5), entityType.getEntityClass(), SpawnCause.SPAWNER_EGG, null, null);

                    if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                        ItemStack inHand = e.getItem().clone();
                        inHand.setAmount(1);
                        ItemUtils.removeItem(inHand, e);
                    }

                } catch (Exception ignored) {
                }
            }

            return;
        }

        if ((plugin.getSettings().eggsStackMultiply &&
                stackedSpawner.getStackAmount() > ItemUtils.countItem(e.getPlayer().getInventory(), e.getItem())) ||
                EntityTypes.fromName(stackedSpawner.getSpawnedType().name()) == ItemUtils.getEntityType(e.getItem())) {
            e.setCancelled(true);
            return;
        }

        Executor.sync(() -> {
            stackedSpawner.updateName();
            if (e.getPlayer().getGameMode() != GameMode.CREATIVE && plugin.getSettings().eggsStackMultiply)
                ItemUtils.removeItem(e.getPlayer().getInventory(), e.getItem(), stackedSpawner.getStackAmount() - 1);
        }, 2L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock().getType() != Materials.SPAWNER.toBukkitType() ||
                !plugin.getSettings().spawnersStackingEnabled || ItemUtils.isOffHand(e))
            return;

        StackedSpawner stackedSpawner = WStackedSpawner.of(e.getClickedBlock());

        if (plugin.getSettings().manageMenuEnabled && (!plugin.getSettings().sneakingOpenMenu || e.getPlayer().isSneaking())) {
            SpawnersManageMenu.open(e.getPlayer(), stackedSpawner);
            e.setCancelled(true);
        } else if (plugin.getSettings().floatingSpawnerNames) {
            int spawnerAmount = stackedSpawner.getStackAmount();

            if (spawnerAmount < 1 || (spawnerAmount == 1 && !plugin.getSettings().spawnersUnstackedCustomName))
                return;

            String customName = plugin.getSettings().spawnersCustomName;

            if (customName.isEmpty())
                return;

            ((WStackedSpawner) stackedSpawner).setCachedDisplayName(
                    EntityUtils.getFormattedType(stackedSpawner.getSpawnedType().name()));

            customName = plugin.getSettings().spawnersNameBuilder.build(stackedSpawner);
            ((WStackedSpawner) stackedSpawner).setHologramName(customName, true);

            Executor.sync(((WStackedSpawner) stackedSpawner)::removeHologram, 60L);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!plugin.getSettings().inventoryTweaksEnabled)
            return;

        String permission = plugin.getSettings().inventoryTweaksPermission;

        if (!permission.isEmpty() && e.getWhoClicked().hasPermission(permission))
            return;

        if (!plugin.getSettings().inventoryTweaksCommand.isEmpty() &&
                !inventoryTweaksToggleCommandPlayers.contains(e.getWhoClicked().getUniqueId()))
            return;

        InventoryAction action = e.getAction();

        switch (action) {
            case PICKUP_HALF:
                if (e.getCurrentItem().getType() == Materials.SPAWNER.toBukkitType()) {
                    int spawnersAmount = ItemUtils.getSpawnerItemAmount(e.getCurrentItem());
                    if (spawnersAmount > 1 && e.getCurrentItem().getAmount() == 1) {
                        e.setCancelled(true);
                        SpawnerUpgrade spawnerUpgrade = plugin.getUpgradesManager().getUpgrade(ItemUtils.getSpawnerUpgrade(e.getCurrentItem()));
                        EntityType entityType = plugin.getProviders().getSpawnersProvider().getSpawnerType(e.getCurrentItem());
                        e.getClickedInventory().setItem(e.getSlot(), ItemUtils.getSpawnerItem(entityType,
                                spawnersAmount / 2, spawnerUpgrade));
                        e.getWhoClicked().setItemOnCursor(ItemUtils.getSpawnerItem(entityType,
                                spawnersAmount - (spawnersAmount / 2), spawnerUpgrade));
                    }
                }
                break;
            case PLACE_ALL:
            case PLACE_ONE:
                if (e.getCurrentItem() != null) {
                    //noinspection all
                    action = InventoryAction.SWAP_WITH_CURSOR;
                } else {
                    if (e.getCursor().getType() == Materials.SPAWNER.toBukkitType()) {
                        int cursorAmount = ItemUtils.getSpawnerItemAmount(e.getCursor());
                        if (cursorAmount > 1 && e.getCursor().getAmount() == 1) {
                            e.setCancelled(true);
                            SpawnerUpgrade spawnerUpgrade = plugin.getUpgradesManager().getUpgrade(ItemUtils.getSpawnerUpgrade(e.getCurrentItem()));
                            EntityType entityType = plugin.getProviders().getSpawnersProvider().getSpawnerType(e.getCursor());
                            e.getWhoClicked().setItemOnCursor(ItemUtils.getSpawnerItem(entityType,
                                    cursorAmount - 1, spawnerUpgrade));
                            e.getClickedInventory().setItem(e.getSlot(), ItemUtils.getSpawnerItem(
                                    entityType, 1, spawnerUpgrade));
                        }
                    }
                    break;
                }
            case SWAP_WITH_CURSOR:
                if (e.getCurrentItem().getType() == Materials.SPAWNER.toBukkitType() && e.getCursor().getType() == Materials.SPAWNER.toBukkitType()) {
                    int currentAmount = ItemUtils.getSpawnerItemAmount(e.getCurrentItem()) * e.getCurrentItem().getAmount(),
                            cursorAmount = ItemUtils.getSpawnerItemAmount(e.getCursor()) * e.getCursor().getAmount();
                    EntityType currentType = plugin.getProviders().getSpawnersProvider().getSpawnerType(e.getCurrentItem());
                    EntityType cursorType = plugin.getProviders().getSpawnersProvider().getSpawnerType(e.getCursor());
                    if (currentType == cursorType) {
                        e.setCancelled(true);
                        SpawnerUpgrade spawnerUpgrade = plugin.getUpgradesManager().getUpgrade(ItemUtils.getSpawnerUpgrade(e.getCurrentItem()));
                        e.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));
                        e.getClickedInventory().setItem(e.getSlot(), ItemUtils.getSpawnerItem(currentType,
                                currentAmount + cursorAmount, spawnerUpgrade));
                    }
                }
                break;
            case COLLECT_TO_CURSOR:
                if (e.getCursor().getType() == Materials.SPAWNER.toBukkitType()) {
                    int newCursorAmount = ItemUtils.getSpawnerItemAmount(e.getCursor());
                    if (e.getCursor().getAmount() == 1) {
                        e.setCancelled(true);
                        EntityType entityType = plugin.getProviders().getSpawnersProvider().getSpawnerType(e.getCursor());
                        SpawnerUpgrade spawnerUpgrade = plugin.getUpgradesManager().getUpgrade(ItemUtils.getSpawnerUpgrade(e.getCurrentItem()));
                        for (int i = 0; i < e.getClickedInventory().getSize(); i++) {
                            ItemStack itemStack = e.getClickedInventory().getItem(i);
                            if (itemStack != null && itemStack.getType() == Materials.SPAWNER.toBukkitType()) {
                                if (plugin.getProviders().getSpawnersProvider().getSpawnerType(itemStack) == entityType) {
                                    newCursorAmount += ItemUtils.getSpawnerItemAmount(itemStack) * itemStack.getAmount();
                                    e.getClickedInventory().setItem(i, new ItemStack(Material.AIR));
                                }
                            }
                        }
                        e.getWhoClicked().setItemOnCursor(ItemUtils.getSpawnerItem(entityType, newCursorAmount, spawnerUpgrade));
                    }
                }
                break;
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        if (plugin.getSettings().inventoryTweaksCommand.isEmpty())
            return;

        String permission = plugin.getSettings().inventoryTweaksPermission;

        if (!permission.isEmpty() && e.getPlayer().hasPermission(permission))
            return;

        for (String commandSyntax : plugin.getSettings().inventoryTweaksCommand.split(",")) {
            commandSyntax = "/" + commandSyntax;

            if (!e.getMessage().equalsIgnoreCase(commandSyntax) && !e.getMessage().startsWith(commandSyntax + " "))
                continue;

            e.setCancelled(true);

            if (inventoryTweaksToggleCommandPlayers.contains(e.getPlayer().getUniqueId())) {
                inventoryTweaksToggleCommandPlayers.remove(e.getPlayer().getUniqueId());
                Locale.INVENTORY_TWEAKS_TOGGLE_OFF.send(e.getPlayer());
            } else {
                inventoryTweaksToggleCommandPlayers.add(e.getPlayer().getUniqueId());
                Locale.INVENTORY_TWEAKS_TOGGLE_ON.send(e.getPlayer());
            }

            return;
        }
    }

    private boolean isChunkLimit(Chunk chunk, EntityType entityType) {
        int chunkLimit = plugin.getSettings().spawnersChunkLimit;

        if (chunkLimit <= 0)
            return false;

        return plugin.getSystemManager().getStackedSpawners(chunk).stream()
                .filter(stackedSpawner -> !plugin.getSettings().perSpawnerLimit || stackedSpawner.getSpawnedType() == entityType).count() > chunkLimit;
    }

    private final class PaperSpawnersListener implements Listener {

        @EventHandler
        public void onPreSpawnSpawner(PreSpawnerSpawnEvent e) {
            if (!plugin.getSettings().listenPaperPreSpawnEvent)
                return;

            StackedSpawner stackedSpawner = WStackedSpawner.of(e.getSpawnerLocation().getBlock());
            SyncedCreatureSpawner creatureSpawner = (SyncedCreatureSpawner) stackedSpawner.getSpawner();
            Optional<StackedEntity> targetEntityOptional = Optional.empty();

            int spawnMobsCount = Random.nextInt(1, creatureSpawner.readData().getSpawnCount(),
                    stackedSpawner.getStackAmount(), 1.5);

            if (plugin.getSettings().linkedEntitiesEnabled) {
                LivingEntity linkedEntity = stackedSpawner.getLinkedEntity();

                if (linkedEntity != null) {
                    StackedEntity stackedLinkedEntity = WStackedEntity.of(linkedEntity);
                    if (stackedLinkedEntity.canGetStacked(spawnMobsCount) == StackCheckResult.SUCCESS) {
                        targetEntityOptional = Optional.of(stackedLinkedEntity);
                    }
                }
            }

            if (!targetEntityOptional.isPresent()) {
                int mergeRadius = plugin.getSettings().entitiesMergeRadius.getOrDefault(e.getType(), SpawnCause.valueOf(e.getReason()), 0);

                if (mergeRadius <= 0)
                    return;

                targetEntityOptional = EntitiesGetter.getNearbyEntities(e.getSpawnLocation(),
                                mergeRadius, entity -> entity.getType() == e.getType() && EntityUtils.isStackable(entity))
                        .map(WStackedEntity::of)
                        .filter(stackedEntity -> stackedEntity.getUpgrade().equals(stackedSpawner.getUpgrade()) &&
                                stackedEntity.canGetStacked(spawnMobsCount) == StackCheckResult.SUCCESS)
                        .findFirst();
            }

            if (!targetEntityOptional.isPresent()) {
                paperPreSpawnChecked.add(e.getSpawnerLocation());
                return;
            }

            StackedEntity stackedEntity = targetEntityOptional.get();
            stackedEntity.increaseStackAmount(spawnMobsCount, true);
            stackedEntity.spawnStackParticle(true);

            e.setCancelled(true);
            e.setShouldAbortSpawn(true);
        }

    }

}
