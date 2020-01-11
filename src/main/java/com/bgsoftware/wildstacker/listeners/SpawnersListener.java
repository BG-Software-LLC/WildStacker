package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.enums.UnstackResult;
import com.bgsoftware.wildstacker.api.events.SpawnerPlaceEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerPlaceInventoryEvent;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.database.Query;
import com.bgsoftware.wildstacker.database.SQLHelper;
import com.bgsoftware.wildstacker.hooks.CoreProtectHook;
import com.bgsoftware.wildstacker.hooks.EconomyHook;
import com.bgsoftware.wildstacker.hooks.PluginHooks;
import com.bgsoftware.wildstacker.key.Key;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.Pair;
import com.bgsoftware.wildstacker.utils.entity.EntityStorage;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import com.bgsoftware.wildstacker.utils.threads.StackService;
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
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unused")
public final class SpawnersListener implements Listener {

    private static final BlockFace[] blockFaces = new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
    private WildStackerPlugin plugin;

    public SpawnersListener(WildStackerPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e){
        if(!plugin.getSettings().spawnersStackingEnabled || e.getBlockPlaced().getType() != Materials.SPAWNER.toBukkitType())
            return;

        StackedSpawner stackedSpawner = WStackedSpawner.of(e.getBlockPlaced());

        if(stackedSpawner.isBlacklisted() || !stackedSpawner.isWhitelisted() || stackedSpawner.isWorldDisabled())
            return;

        ItemStack itemInHand = e.getItemInHand().clone();
        EntityType spawnerType = plugin.getProviders().getSpawnerType(itemInHand);

        plugin.getProviders().setSpawnerType(stackedSpawner.getSpawner(), itemInHand, true);
        stackedSpawner.getSpawner().setDelay(ThreadLocalRandom.current().nextInt(200, 800));

        int spawnerItemAmount = ItemUtils.getSpawnerItemAmount(itemInHand);

        if(plugin.getSettings().spawnersPlacementPermission && !e.getPlayer().hasPermission("wildstacker.place.*") &&
                !e.getPlayer().hasPermission("wildstacker.place." + spawnerType.name().toLowerCase())) {
            Locale.SPAWNER_PLACE_BLOCKED.send(e.getPlayer(), "wildstacker.place." + spawnerType.name().toLowerCase());
            e.setCancelled(true);
            return;
        }

        //Next Spawner Placement
        if(!plugin.getSettings().nextSpawnerPlacement && !e.getPlayer().hasPermission("wildstacker.nextplace")) {
            for (BlockFace blockFace : blockFaces) {
                if (e.getBlockPlaced().getRelative(blockFace).getType() == Materials.SPAWNER.toBukkitType()){
                    Locale.NEXT_SPAWNER_PLACEMENT.send(e.getPlayer());
                    e.setCancelled(true);
                    return;
                }
            }
        }

        boolean replaceAir = false;
        ItemStack limitItem = null;
        int heldSlot = e.getPlayer().getInventory().getHeldItemSlot();

        if(e.getPlayer().isSneaking() && plugin.getSettings().spawnersShiftPlaceStack){
            replaceAir = true;
            spawnerItemAmount *= itemInHand.getAmount();
        }

        if(e.getPlayer().getGameMode() != GameMode.CREATIVE){
            int limit = stackedSpawner.getStackLimit();
            //If the spawnerItemAmount is larger than the spawner limit, we want to give to the player the leftovers
            if(limit < spawnerItemAmount){
                limitItem = plugin.getProviders().getSpawnerItem(stackedSpawner.getSpawner(), spawnerItemAmount - limit);
                //Adding the item to the inventory after the spawner is placed
                spawnerItemAmount = limit;
            }
        }

        Pair<Double, Boolean> chargeInfo = plugin.getSettings().spawnersPlaceCharge.getOrDefault(
                Key.of(spawnerType.name()), new Pair<>(0.0, false));

        double amountToCharge = chargeInfo.getKey() * (chargeInfo.getValue() ? spawnerItemAmount : 1);

        if (PluginHooks.isVaultEnabled && EconomyHook.getMoneyInBank(e.getPlayer()) < amountToCharge) {
            Locale.SPAWNER_PLACE_NOT_ENOUGH_MONEY.send(e.getPlayer(), amountToCharge);
            e.setCancelled(true);
            return;
        }

        //Stacking spawner
        stackedSpawner.setStackAmount(spawnerItemAmount, false);

        final boolean REPLACE_AIR = replaceAir;
        final ItemStack LIMIT_ITEM = limitItem;
        Chunk chunk = e.getBlock().getChunk();

        StackService.runOnMain(stackedSpawner);
        stackedSpawner.runStackAsync(spawnerOptional -> {
            int stackAmount = stackedSpawner.getStackAmount();

            if(!spawnerOptional.isPresent()){
                if(isChunkLimit(chunk, spawnerType)) {
                    e.setCancelled(true);
                    return;
                }

                if(plugin.getSettings().onlyOneSpawner){
                    for(StackedSpawner nearbySpawner : stackedSpawner.getNearbySpawners()){
                        if(nearbySpawner.getStackAmount() >= nearbySpawner.getStackLimit()) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                }

                SpawnerPlaceEvent spawnerPlaceEvent = new SpawnerPlaceEvent(e.getPlayer(), stackedSpawner, itemInHand);
                Bukkit.getPluginManager().callEvent(spawnerPlaceEvent);

                if(spawnerPlaceEvent.isCancelled()) {
                    e.setCancelled(true);
                    return;
                }

                Location location = stackedSpawner.getLocation();

                SQLHelper.runIfConditionNotExist("SELECT * FROM spawners WHERE location ='" + SQLHelper.getLocation(location) + "';", () ->
                        Query.SPAWNER_INSERT.getStatementHolder()
                                .setLocation(location)
                                .setInt(stackedSpawner.getStackAmount())
                                .execute(false)
                );
            }
            else{
                e.setCancelled(true);

                revokeItem(e.getPlayer(), itemInHand);

                StackedSpawner targetSpawner = WStackedSpawner.of(spawnerOptional.get());

                CoreProtectHook.recordBlockChange(e.getPlayer(), targetSpawner.getLocation(), Materials.SPAWNER.toBukkitType(), (byte) 0, true);

                stackAmount = targetSpawner.getStackAmount();
            }

            if(amountToCharge > 0)
                EconomyHook.withdrawMoney(e.getPlayer(), amountToCharge);

            //Removing item from player's inventory
            if(e.getPlayer().getGameMode() != GameMode.CREATIVE && REPLACE_AIR)
                ItemUtils.setItemInHand(e.getPlayer().getInventory(), itemInHand, new ItemStack(Material.AIR));

            if(LIMIT_ITEM != null)
                ItemUtils.addItem(LIMIT_ITEM, e.getPlayer().getInventory(), e.getPlayer().getLocation());

            Locale.SPAWNER_PLACE.send(e.getPlayer(), EntityUtils.getFormattedType(spawnerType.name()), stackAmount, amountToCharge);
        });

        if(e.isCancelled()){
            stackedSpawner.remove();
        }
    }

    private void revokeItem(Player player, ItemStack itemInHand){
        if(player.getGameMode() != GameMode.CREATIVE) {
            ItemStack inHand = itemInHand.clone();
            inHand.setAmount(inHand.getAmount() - 1);
            //Using this method as remove() doesn't affect off hand
            ItemUtils.setItemInHand(player.getInventory(), itemInHand, inHand);
        }
    }

    //Priority is high so it can be fired before SilkSpawners
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e){
        if(!plugin.getSettings().spawnersStackingEnabled || e.getBlock().getType() != Materials.SPAWNER.toBukkitType())
            return;

        StackedSpawner stackedSpawner = WStackedSpawner.of(e.getBlock());
        CreatureSpawner creatureSpawner = (CreatureSpawner) e.getBlock().getState();

        e.setCancelled(true);

        int originalAmount = stackedSpawner.getStackAmount();
        int stackAmount = e.getPlayer().isSneaking() && plugin.getSettings().shiftGetWholeSpawnerStack ? originalAmount : 1;

        Pair<Double, Boolean> chargeInfo = plugin.getSettings().spawnersBreakCharge.getOrDefault(
                Key.of(stackedSpawner.getSpawnedType().name()), new Pair<>(0.0, false));

        double amountToCharge = chargeInfo.getKey() * (chargeInfo.getValue() ? stackAmount : 1);

        if (PluginHooks.isVaultEnabled && EconomyHook.getMoneyInBank(e.getPlayer()) < amountToCharge) {
            Locale.SPAWNER_BREAK_NOT_ENOUGH_MONEY.send(e.getPlayer(), amountToCharge);
            e.setCancelled(true);
            return;
        }

        if(stackedSpawner.runUnstack(stackAmount) == UnstackResult.SUCCESS){
            CoreProtectHook.recordBlockChange(e.getPlayer(), e.getBlock(), false);

            plugin.getProviders().dropOrGiveItem(e.getPlayer(), creatureSpawner, stackAmount, false);

            EntityType entityType = stackedSpawner.getSpawnedType();

            if(stackedSpawner.getStackAmount() <= 0)
                e.getBlock().setType(Material.AIR);

            if(amountToCharge > 0)
                EconomyHook.withdrawMoney(e.getPlayer(), amountToCharge);

            Locale.SPAWNER_BREAK.send(e.getPlayer(), EntityUtils.getFormattedType(entityType.name()), stackAmount, amountToCharge);
        }
    }

    private Map<Entity, UUID> explodableSources = new WeakHashMap<>();

    //Priority is high so it can be fired before SilkSpawners
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent e){
        if(!plugin.getSettings().spawnersStackingEnabled)
            return;

        List<Block> blockList = new ArrayList<>(e.blockList());

        for(Block block : blockList){
            //Making sure it's a spawner
            if(block.getType() != Materials.SPAWNER.toBukkitType())
                continue;

            StackedSpawner stackedSpawner = WStackedSpawner.of(block);
            CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();

            int amount;

            //Explosions can break the whole stack
            if (plugin.getSettings().explosionsBreakSpawnerStack) {
                amount = stackedSpawner.getStackAmount();
                stackedSpawner.runUnstack(stackedSpawner.getStackAmount());
            } else {
                amount = 1;
                stackedSpawner.runUnstack(1);
            }

            if(ThreadLocalRandom.current().nextInt(100) < plugin.getSettings().explosionsBreakChance) {
                amount = (int) Math.round((plugin.getSettings().explosionsAmountPercentage / 100.0) * amount);
                plugin.getProviders().dropOrGiveItem(e.getEntity(), creatureSpawner, amount, explodableSources.get(e.getEntity()));
            }

            if(stackedSpawner.getStackAmount() <= 0)
                block.setType(Material.AIR);

            //If the amount of the spawner is more than 1, we don't need to destroy it
            e.blockList().remove(block);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityExplodeMonitor(EntityExplodeEvent e){
        explodableSources.remove(e.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExplodableLight(PlayerInteractAtEntityEvent e){
        if(plugin.getSettings().explosionsDropToInventory && e.getRightClicked() instanceof Creeper &&
                e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType() == Material.FLINT_AND_STEEL)
            explodableSources.put(e.getRightClicked(), e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExplodableLight(PlayerInteractEvent e){
        if(plugin.getSettings().explosionsDropToInventory && e.getClickedBlock() != null &&
                e.getClickedBlock().getType() == Material.TNT && e.getItem() != null && e.getItem().getType().equals(Material.FLINT_AND_STEEL)){
            Location location = e.getClickedBlock().getLocation();
            Executor.sync(() -> location.getWorld().getNearbyEntities(location, 1, 1, 1).stream()
                    .filter(entity -> entity instanceof TNTPrimed).findFirst()
                    .ifPresent(entity -> explodableSources.put(entity, e.getPlayer().getUniqueId())), 2L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExplodableLight(EntityTargetEvent e){
        if(e.getEntity() instanceof Creeper){
            if(e.getTarget() instanceof Player)
                explodableSources.put(e.getEntity(), e.getTarget().getUniqueId());
            else
                explodableSources.remove(e.getEntity());
        }
    }

    private boolean listenToSpawnEvent = true;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSpawnerSpawn(SpawnerSpawnEvent e){
        if(!listenToSpawnEvent || !(e.getEntity() instanceof LivingEntity))
            return;

        EntityStorage.setMetadata(e.getEntity(), "spawn-cause", SpawnCause.SPAWNER);
        StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());

        stackedEntity.updateNerfed();

        boolean multipleEntities = !plugin.getSettings().entitiesStackingEnabled;

        if(!multipleEntities){
            multipleEntities = !stackedEntity.isWhitelisted() || stackedEntity.isBlacklisted() || stackedEntity.isWorldDisabled();
        }

        StackedSpawner stackedSpawner = WStackedSpawner.of(e.getSpawner());

        if(multipleEntities) {
            if(stackedSpawner.getStackAmount() > 1) {
                Executor.async(() -> {
                    Set<Location> locationsToSpawn = new HashSet<>();
                    Location location = e.getSpawner().getLocation();
                    ThreadLocalRandom random = ThreadLocalRandom.current();
                    for (int i = 0; i < stackedSpawner.getStackAmount() - 1; i++) {
                        locationsToSpawn.add(new Location(location.getWorld(),
                                location.getBlockX() + ((random.nextDouble() - random.nextDouble()) * 4.5D),
                                location.getBlockY(),
                                location.getBlockZ() + ((random.nextDouble() - random.nextDouble()) * 4.5D)
                        ));
                    }
                    Executor.sync(() -> {
                        listenToSpawnEvent = false;
                        for (Location toSpawn : locationsToSpawn) {
                            StackedEntity targetEntity = WStackedEntity.of(plugin.getSystemManager().spawnEntityWithoutStacking(toSpawn, e.getEntityType().getEntityClass()));
                            if(!callSpawnerSpawnEvent(targetEntity, stackedSpawner))
                                stackedEntity.remove();
                            targetEntity.updateNerfed();
                        }
                        listenToSpawnEvent = true;
                    });
                });
            }
        }

        else{
            stackedEntity.setStackAmount(stackedSpawner.getStackAmount(), true);
            stackedEntity.runSpawnerStackAsync(stackedSpawner, null);
        }
    }

    private boolean callSpawnerSpawnEvent(StackedEntity stackedEntity, StackedSpawner stackedSpawner){
        SpawnerSpawnEvent spawnerSpawnEvent = new SpawnerSpawnEvent(stackedEntity.getLivingEntity(), stackedSpawner.getSpawner());
        Bukkit.getPluginManager().callEvent(new SpawnerSpawnEvent(stackedEntity.getLivingEntity(), stackedSpawner.getSpawner()));

        if(spawnerSpawnEvent.isCancelled() || stackedEntity.getLivingEntity().isDead() || !stackedEntity.getLivingEntity().isValid())
            return false;

        //noinspection deprecation
        Bukkit.getPluginManager().callEvent(new com.bgsoftware.wildstacker.api.events.SpawnerSpawnEvent(stackedEntity, stackedSpawner));

        return true;
    }

    //Same as SilkSpawnersSpawnerChangeEvent, but will only work if SilkSpawners is disabled
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerChange(PlayerInteractEvent e){
        if(e.getItem() == null || e.getAction() != Action.RIGHT_CLICK_BLOCK ||
                !Materials.isValidAndSpawnEgg(e.getItem()) || e.getClickedBlock().getType() != Materials.SPAWNER.toBukkitType())
            return;

        StackedSpawner stackedSpawner = WStackedSpawner.of(e.getClickedBlock());

        if(!plugin.getSettings().changeUsingEggs || (plugin.getSettings().eggsStackMultiply &&
                stackedSpawner.getStackAmount() > ItemUtils.countItem(e.getPlayer().getInventory(), e.getItem())) ||
                EntityTypes.fromName(stackedSpawner.getSpawnedType().name()) == ItemUtils.getEntityType(e.getItem())) {
            e.setCancelled(true);
            return;
        }

        Executor.sync(() -> {
            stackedSpawner.updateName();
            if(e.getPlayer().getGameMode() != GameMode.CREATIVE && plugin.getSettings().eggsStackMultiply)
                ItemUtils.removeItem(e.getPlayer().getInventory(), e.getItem(), stackedSpawner.getStackAmount() - 1);
        }, 2L);
    }

    private Map<UUID, Location> clickedSpawners = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerInteract(PlayerInteractEvent e){
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock().getType() != Materials.SPAWNER.toBukkitType())
            return;

        if(clickedSpawners.containsKey(e.getPlayer().getUniqueId()))
            return;

        StackedSpawner stackedSpawner = WStackedSpawner.of(e.getClickedBlock());

        if(e.getPlayer().isSneaking()){
            if(plugin.getSettings().spawnersBreakMenu){
                clickedSpawners.put(e.getPlayer().getUniqueId(), stackedSpawner.getLocation());
                plugin.getBreakMenuHandler().openBreakMenu(e.getPlayer());
                e.setCancelled(true);
            }else if(plugin.getSettings().spawnersPlaceMenu){
                clickedSpawners.put(e.getPlayer().getUniqueId(), stackedSpawner.getLocation());
                e.getPlayer().openInventory(Bukkit.createInventory(null, 9 * 4,
                        plugin.getSettings().spawnersPlaceMenuTitle.replace("{0}", EntityUtils.getFormattedType(stackedSpawner.getSpawnedType().name()))));
                e.setCancelled(true);
            }
        }

        else{
            if(!plugin.getSettings().floatingSpawnerNames || stackedSpawner.getStackAmount() <= 1)
                return;

            String customName = plugin.getSettings().hologramCustomName;

            if (customName.isEmpty())
                return;

            int amount = stackedSpawner.getStackAmount();

            customName = customName
                    .replace("{0}", Integer.toString(amount))
                    .replace("{1}", EntityUtils.getFormattedType(stackedSpawner.getSpawnedType().name()))
                    .replace("{2}", EntityUtils.getFormattedType(stackedSpawner.getSpawnedType().name()).toUpperCase());
            plugin.getProviders().changeLine(stackedSpawner, customName, true);

            Executor.sync(() -> plugin.getProviders().deleteHologram(stackedSpawner), 60L);
        }
    }

    @EventHandler
    public void onBreakMenuClick(InventoryClickEvent e){
        if(e.getInventory() == null || !plugin.getBreakMenuHandler().isBreakMenu(e.getInventory()))
            return;

        e.setCancelled(true);

        if(!(e.getWhoClicked() instanceof Player))
            return;

        Player player = (Player) e.getWhoClicked();

        if(!clickedSpawners.containsKey(player.getUniqueId())){
            player.closeInventory();
            return;
        }

        if(!plugin.getBreakMenuHandler().breakSlots.containsKey(e.getRawSlot()))
            return;

        int removeAmount = plugin.getBreakMenuHandler().breakSlots.get(e.getRawSlot());

        Block spawnerBlock = clickedSpawners.get(player.getUniqueId()).getBlock();
        StackedSpawner stackedSpawner;

        try {
            stackedSpawner = WStackedSpawner.of(spawnerBlock);
        }catch(IllegalArgumentException ex){
            //IllegelArgumentException can be thrown if the block no longer exist.
            player.closeInventory();
            return;
        }


        removeAmount = Math.min(stackedSpawner.getStackAmount(), removeAmount);

        Pair<Double, Boolean> chargeInfo = plugin.getSettings().spawnersBreakCharge.getOrDefault(
                Key.of(stackedSpawner.getSpawnedType().name()), new Pair<>(0.0, false));

        double amountToCharge = chargeInfo.getKey() * (chargeInfo.getValue() ? removeAmount : 1);

        if (PluginHooks.isVaultEnabled && EconomyHook.getMoneyInBank(player) < amountToCharge) {
            Locale.SPAWNER_BREAK_NOT_ENOUGH_MONEY.send(player, amountToCharge);
            e.setCancelled(true);
            return;
        }

        if(stackedSpawner.runUnstack(removeAmount) == UnstackResult.SUCCESS){
            CoreProtectHook.recordBlockChange(player, spawnerBlock, false);

            if(amountToCharge > 0)
                EconomyHook.withdrawMoney(player, amountToCharge);

            //noinspection all
            plugin.getProviders().dropOrGiveItem((Player) null, stackedSpawner.getSpawner(), removeAmount, false);

            if(stackedSpawner.getStackAmount() <= 0)
                spawnerBlock.setType(Material.AIR);
            if(spawnerBlock.getType() == Materials.SPAWNER.toBukkitType())
                Locale.SPAWNER_BREAK.send(player, EntityUtils.getFormattedType(stackedSpawner.getSpawnedType().name()), stackedSpawner.getStackAmount(), amountToCharge);
            player.closeInventory();
        }

    }

    @EventHandler
    public void onPlaceMenuClick(InventoryClickEvent e){
        if(!clickedSpawners.containsKey(e.getWhoClicked().getUniqueId()) || e.getInventory().getSize() != 9*4)
            return;

        StackedSpawner stackedSpawner = WStackedSpawner.of(clickedSpawners.get(e.getWhoClicked().getUniqueId()).getBlock());

        if(e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR && (e.getCurrentItem().getType() != Materials.SPAWNER.toBukkitType() ||
                plugin.getProviders().getSpawnerType(e.getCurrentItem()) != stackedSpawner.getSpawnedType()))
            e.setCancelled(true);
        if(e.getAction() == InventoryAction.HOTBAR_SWAP)
            e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e){
        if(clickedSpawners.containsKey(e.getPlayer().getUniqueId())){
            //Place inventory
            if(e.getInventory().getType() == InventoryType.CHEST && e.getInventory().getSize() == 9 * 4){
                StackedSpawner stackedSpawner = WStackedSpawner.of(clickedSpawners.get(e.getPlayer().getUniqueId()).getBlock());
                int amount = 0;

                for(ItemStack itemStack : e.getInventory().getContents()){
                    if(itemStack != null) {
                        if (itemStack.getType() == Materials.SPAWNER.toBukkitType() &&
                                plugin.getProviders().getSpawnerType(itemStack) == stackedSpawner.getSpawnedType())
                            amount += ItemUtils.getSpawnerItemAmount(itemStack) * itemStack.getAmount();
                        else if (itemStack.getType() != Material.AIR)
                            ItemUtils.addItem(itemStack, e.getPlayer().getInventory(), stackedSpawner.getLocation());
                    }
                }

                if(amount != 0) {
                    int limit = stackedSpawner.getStackLimit();
                    int newStackAmount = stackedSpawner.getStackAmount() + amount;

                    if(stackedSpawner.getStackAmount() + amount > limit){
                        ItemStack toAdd = plugin.getProviders().getSpawnerItem(stackedSpawner.getSpawner(),stackedSpawner.getStackAmount() + amount - limit);
                        ItemUtils.addItem(toAdd, e.getPlayer().getInventory(), stackedSpawner.getLocation());
                        newStackAmount = limit;
                    }

                    SpawnerPlaceInventoryEvent spawnerPlaceInventoryEvent = new SpawnerPlaceInventoryEvent((Player) e.getPlayer(), stackedSpawner, newStackAmount - stackedSpawner.getStackAmount());
                    Bukkit.getPluginManager().callEvent(spawnerPlaceInventoryEvent);

                    if(!spawnerPlaceInventoryEvent.isCancelled()) {
                        stackedSpawner.setStackAmount(newStackAmount, true);
                        Locale.SPAWNER_UPDATE.send(e.getPlayer(), stackedSpawner.getStackAmount());
                    }
                }
            }

            clickedSpawners.remove(e.getPlayer().getUniqueId());
        }
    }

    private boolean isChunkLimit(Chunk chunk, EntityType entityType){
        int chunkLimit = plugin.getSettings().spawnersChunkLimit;

        if(chunkLimit <= 0)
            return false;

        return plugin.getSystemManager().getStackedSpawners(chunk).stream()
                .filter(stackedSpawner -> !plugin.getSettings().perSpawnerLimit || stackedSpawner.getSpawnedType() == entityType).count() > chunkLimit;
    }

}
