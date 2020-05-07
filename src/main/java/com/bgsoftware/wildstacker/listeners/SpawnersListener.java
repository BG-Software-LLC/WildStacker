package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.enums.UnstackResult;
import com.bgsoftware.wildstacker.api.events.SpawnerPlaceEvent;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.hooks.CoreProtectHook;
import com.bgsoftware.wildstacker.hooks.EconomyHook;
import com.bgsoftware.wildstacker.hooks.PluginHooks;
import com.bgsoftware.wildstacker.key.Key;
import com.bgsoftware.wildstacker.menu.SpawnersBreakMenu;
import com.bgsoftware.wildstacker.menu.SpawnersPlaceMenu;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.entity.EntityStorage;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.pair.Pair;
import com.bgsoftware.wildstacker.utils.threads.Executor;
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

    private static final BlockFace[] blockFaces = new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

    private final Set<UUID> inventoryTweaksToggleCommandPlayers = new HashSet<>();
    private final Map<Entity, UUID> explodableSources = new WeakHashMap<>();

    private final WildStackerPlugin plugin;

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

        plugin.getProviders().handleSpawnerPlace(stackedSpawner.getSpawner(), e.getPlayer().getItemInHand());
        EntityType spawnerType = plugin.getProviders().getSpawnerType(itemInHand);

        stackedSpawner.getSpawner().setDelay(ThreadLocalRandom.current().nextInt(200, 800));

        int spawnerItemAmount = ItemUtils.getSpawnerItemAmount(itemInHand);

        if(plugin.getSettings().spawnersPlacementPermission && !e.getPlayer().hasPermission("wildstacker.place.*") &&
                !e.getPlayer().hasPermission("wildstacker.place." + spawnerType.name().toLowerCase())) {
            Locale.SPAWNER_PLACE_BLOCKED.send(e.getPlayer(), "wildstacker.place." + spawnerType.name().toLowerCase());
            e.setCancelled(true);
            stackedSpawner.remove();
            return;
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
                limitItem = plugin.getProviders().getSpawnerItem(stackedSpawner.getSpawner().getSpawnedType(), spawnerItemAmount - limit);
                //Adding the item to the inventory after the spawner is placed
                spawnerItemAmount = limit;
            }
        }

        Pair<Double, Boolean> chargeInfo = plugin.getSettings().spawnersPlaceCharge.getOrDefault(
                Key.of(spawnerType.name()), new Pair<>(0.0, false));

        double amountToCharge = chargeInfo.getKey() * (chargeInfo.getValue() ? spawnerItemAmount : 1);

        if (amountToCharge > 0 && PluginHooks.isVaultEnabled && EconomyHook.getMoneyInBank(e.getPlayer()) < amountToCharge) {
            Locale.SPAWNER_PLACE_NOT_ENOUGH_MONEY.send(e.getPlayer(), amountToCharge);
            e.setCancelled(true);
            stackedSpawner.remove();
            return;
        }

        stackedSpawner.setStackAmount(spawnerItemAmount, false);

        if(!e.getPlayer().hasPermission("wildstacker.stack.*") &&
                !e.getPlayer().hasPermission("wildstacker.stack." + spawnerType.name().toLowerCase()))
            return;

        final boolean REPLACE_AIR = replaceAir;
        final ItemStack LIMIT_ITEM = limitItem;
        Chunk chunk = e.getBlock().getChunk();

        //Stacking spawner
        Optional<CreatureSpawner> spawnerOptional = stackedSpawner.runStack();

        int stackAmount = stackedSpawner.getStackAmount();

        if(!spawnerOptional.isPresent()){
            if(isChunkLimit(chunk, spawnerType)) {
                e.setCancelled(true);
                Locale.CHUNK_LIMIT_EXCEEDED.send(e.getPlayer(), EntityUtils.getFormattedType(stackedSpawner.getSpawnedType().name()) + " Spawners");
                stackedSpawner.remove();
                return;
            }

            //Next Spawner Placement
            if(!plugin.getSettings().nextSpawnerPlacement && !e.getPlayer().hasPermission("wildstacker.nextplace")) {
                for (BlockFace blockFace : blockFaces) {
                    if (e.getBlockPlaced().getRelative(blockFace).getType() == Materials.SPAWNER.toBukkitType()){
                        Locale.NEXT_SPAWNER_PLACEMENT.send(e.getPlayer());
                        e.setCancelled(true);
                        stackedSpawner.remove();
                        return;
                    }
                }
            }

            if(plugin.getSettings().onlyOneSpawner){
                for(StackedSpawner nearbySpawner : stackedSpawner.getNearbySpawners()){
                    if(nearbySpawner.getStackAmount() >= nearbySpawner.getStackLimit()) {
                        Locale.ONLY_ONE_SPAWNER.send(e.getPlayer());
                        e.setCancelled(true);
                        stackedSpawner.remove();
                        return;
                    }
                }
            }

            SpawnerPlaceEvent spawnerPlaceEvent = new SpawnerPlaceEvent(e.getPlayer(), stackedSpawner, itemInHand);
            Bukkit.getPluginManager().callEvent(spawnerPlaceEvent);

            if(spawnerPlaceEvent.isCancelled()) {
                e.setCancelled(true);
                stackedSpawner.remove();
                return;
            }

            plugin.getDataHandler().insertSpawner(stackedSpawner);
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

        if (amountToCharge > 0 && PluginHooks.isVaultEnabled && EconomyHook.getMoneyInBank(e.getPlayer()) < amountToCharge) {
            Locale.SPAWNER_BREAK_NOT_ENOUGH_MONEY.send(e.getPlayer(), amountToCharge);
            e.setCancelled(true);
            return;
        }

        if(stackedSpawner.runUnstack(stackAmount) == UnstackResult.SUCCESS){
            CoreProtectHook.recordBlockChange(e.getPlayer(), e.getBlock(), false);

            plugin.getProviders().handleSpawnerBreak(stackedSpawner, e.getPlayer(), stackAmount, false);

            EntityType entityType = stackedSpawner.getSpawnedType();

            if(stackedSpawner.getStackAmount() <= 0)
                e.getBlock().setType(Material.AIR);

            if(amountToCharge > 0)
                EconomyHook.withdrawMoney(e.getPlayer(), amountToCharge);

            Locale.SPAWNER_BREAK.send(e.getPlayer(), EntityUtils.getFormattedType(entityType.name()), stackAmount, amountToCharge);
        }
    }

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

            UUID explodeSource = explodableSources.get(e.getEntity());
            Player sourcePlayer = null;

            if(e.getEntity() instanceof TNTPrimed){
                Entity igniter = ((TNTPrimed) e.getEntity()).getSource();
                if (igniter instanceof Player) {
                    sourcePlayer = (Player) igniter;
                }
            }
            else{
                sourcePlayer = explodeSource == null ? null : Bukkit.getPlayer(explodeSource);
            }

            int breakAmount = plugin.getSettings().explosionsBreakSpawnerStack ? stackedSpawner.getStackAmount() : 1;
            breakAmount = (int) Math.round((plugin.getSettings().explosionsAmountPercentage / 100.0) * breakAmount);

            plugin.getProviders().handleSpawnerExplode(stackedSpawner, e.getEntity(), sourcePlayer, breakAmount);

            stackedSpawner.runUnstack(breakAmount);

            if(stackedSpawner.getStackAmount() > 0)
                e.blockList().remove(block);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityExplodeMonitor(EntityExplodeEvent e){
        explodableSources.remove(e.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExplodableLight(PlayerInteractEntityEvent e){
        if(plugin.getSettings().explosionsDropToInventory && e.getRightClicked() instanceof Creeper &&
                e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType() == Material.FLINT_AND_STEEL)
            explodableSources.put(e.getRightClicked(), e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExplodableLight(PlayerInteractEvent e){
        if(plugin.getSettings().explosionsDropToInventory && e.getClickedBlock() != null &&
                e.getClickedBlock().getType() == Material.TNT && e.getItem() != null && e.getItem().getType().equals(Material.FLINT_AND_STEEL)){
            Location location = e.getClickedBlock().getLocation();
            Executor.sync(() -> {
                try{
                    EntityUtils.getNearbyEntities(location, 1, entity -> entity instanceof TNTPrimed)
                            .whenComplete((nearbyEntities,  ex) -> nearbyEntities.stream().findFirst()
                                    .ifPresent(entity -> explodableSources.put(entity, e.getPlayer().getUniqueId())));
                }catch(Throwable ignored){}
            }, 2L);
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

        int minimumEntityLimit = plugin.getSettings().minimumEntitiesLimit.getOrDefault(stackedEntity.getType().name(), 1);

        multipleEntities = multipleEntities || minimumEntityLimit > stackedSpawner.getStackAmount();

        if(multipleEntities) {
            if(stackedSpawner.getStackAmount() > 1) {
                Executor.async(() -> {
                    Set<Location> locationsToSpawn = new HashSet<>();
                    Location location = e.getSpawner().getLocation();
                    ThreadLocalRandom random = ThreadLocalRandom.current();
                    for (int i = 0; i < stackedSpawner.getStackAmount() - 1; i++) {
                        Location locationToSpawn = null;
                        int tries = 0;

                        while ((locationToSpawn == null || !plugin.getNMSAdapter().canSpawnOn(e.getEntity(), locationToSpawn)) && ++tries <= 5){
                            locationToSpawn = new Location(location.getWorld(),
                                    location.getBlockX() + ((random.nextDouble() - random.nextDouble()) * 4.5D),
                                    location.getBlockY(),
                                    location.getBlockZ() + ((random.nextDouble() - random.nextDouble()) * 4.5D)
                            );
                        }

                        locationsToSpawn.add(locationToSpawn);
                    }
                    Executor.sync(() -> {
                        listenToSpawnEvent = false;
                        for (Location toSpawn : locationsToSpawn) {
                            StackedEntity targetEntity = WStackedEntity.of(plugin.getSystemManager().spawnEntityWithoutStacking(toSpawn, e.getEntityType().getEntityClass()));
                            plugin.getNMSAdapter().playSpawnEffect(targetEntity.getLivingEntity());
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerInteract(PlayerInteractEvent e){
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock().getType() != Materials.SPAWNER.toBukkitType() ||
                !plugin.getSettings().spawnersStackingEnabled || ItemUtils.isOffHand(e))
            return;

        StackedSpawner stackedSpawner = WStackedSpawner.of(e.getClickedBlock());

        if(e.getPlayer().isSneaking()){
            if(plugin.getSettings().spawnersBreakMenu){
                SpawnersBreakMenu.open(e.getPlayer(), stackedSpawner.getLocation());
                e.setCancelled(true);
            }else if(plugin.getSettings().spawnersPlaceMenu){
                SpawnersPlaceMenu.open(e.getPlayer(), stackedSpawner);
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
                        EntityType entityType = plugin.getProviders().getSpawnerType(e.getCurrentItem());
                        e.getClickedInventory().setItem(e.getSlot(), ItemUtils.getSpawnerItem(entityType, spawnersAmount / 2));
                        e.getWhoClicked().setItemOnCursor(ItemUtils.getSpawnerItem(entityType, spawnersAmount - (spawnersAmount / 2)));
                    }
                }
                break;
            case PLACE_ALL:
            case PLACE_ONE:
                if (e.getCurrentItem() != null) {
                    action = InventoryAction.SWAP_WITH_CURSOR;
                } else {
                    if (e.getCursor().getType() == Materials.SPAWNER.toBukkitType()) {
                        int cursorAmount = ItemUtils.getSpawnerItemAmount(e.getCursor());
                        if (cursorAmount > 1 && e.getCursor().getAmount() == 1) {
                            e.setCancelled(true);
                            EntityType entityType = plugin.getProviders().getSpawnerType(e.getCursor());
                            e.getWhoClicked().setItemOnCursor(ItemUtils.getSpawnerItem(entityType, cursorAmount - 1));
                            e.getClickedInventory().setItem(e.getSlot(), ItemUtils.getSpawnerItem(entityType, 1));
                        }
                    }
                    break;
                }
            case SWAP_WITH_CURSOR:
                if (e.getCurrentItem().getType() == Materials.SPAWNER.toBukkitType() && e.getCursor().getType() == Materials.SPAWNER.toBukkitType()) {
                    int currentAmount = ItemUtils.getSpawnerItemAmount(e.getCurrentItem()) * e.getCurrentItem().getAmount(),
                            cursorAmount = ItemUtils.getSpawnerItemAmount(e.getCursor()) * e.getCursor().getAmount();
                    EntityType currentType = plugin.getProviders().getSpawnerType(e.getCurrentItem()), cursorType = plugin.getProviders().getSpawnerType(e.getCursor());
                    if (currentType == cursorType) {
                        e.setCancelled(true);
                        e.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));
                        e.getClickedInventory().setItem(e.getSlot(), ItemUtils.getSpawnerItem(currentType, currentAmount + cursorAmount));
                    }
                }
                break;
            case COLLECT_TO_CURSOR:
                if (e.getCursor().getType() == Materials.SPAWNER.toBukkitType()) {
                    int newCursorAmount = ItemUtils.getSpawnerItemAmount(e.getCursor());
                    if (e.getCursor().getAmount() == 1) {
                        e.setCancelled(true);
                        EntityType entityType = plugin.getProviders().getSpawnerType(e.getCursor());
                        for (int i = 0; i < e.getClickedInventory().getSize(); i++) {
                            ItemStack itemStack = e.getClickedInventory().getItem(i);
                            if (itemStack != null && itemStack.getType() == Materials.SPAWNER.toBukkitType()) {
                                if (plugin.getProviders().getSpawnerType(itemStack) == entityType) {
                                    newCursorAmount += ItemUtils.getSpawnerItemAmount(itemStack);
                                    e.getClickedInventory().setItem(i, new ItemStack(Material.AIR));
                                }
                            }
                        }
                        e.getWhoClicked().setItemOnCursor(ItemUtils.getSpawnerItem(entityType, newCursorAmount));
                    }
                }
                break;
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e){
        if(plugin.getSettings().inventoryTweaksCommand.isEmpty())
            return;

        String permission = plugin.getSettings().inventoryTweaksPermission;

        if(!permission.isEmpty() && e.getPlayer().hasPermission(permission))
            return;

        for(String commandSyntax : plugin.getSettings().inventoryTweaksCommand.split(",")) {
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

    private boolean isChunkLimit(Chunk chunk, EntityType entityType){
        int chunkLimit = plugin.getSettings().spawnersChunkLimit;

        if(chunkLimit <= 0)
            return false;

        return plugin.getSystemManager().getStackedSpawners(chunk).stream()
                .filter(stackedSpawner -> !plugin.getSettings().perSpawnerLimit || stackedSpawner.getSpawnedType() == entityType).count() > chunkLimit;
    }

}
