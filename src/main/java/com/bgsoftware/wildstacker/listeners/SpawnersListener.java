package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.events.SpawnerPlaceEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerPlaceInventoryEvent;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.hooks.CoreProtectHook;
import com.bgsoftware.wildstacker.hooks.EconomyHook;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.EntityUtil;
import com.bgsoftware.wildstacker.utils.Executor;
import com.bgsoftware.wildstacker.utils.ItemUtil;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

        //Try and set a custom entity type
        plugin.getProviders().setSpawnerType(stackedSpawner.getSpawner(), e.getItemInHand(), false);
        int spawnerItemAmount = ItemUtil.getSpawnerItemAmount(e.getItemInHand());

        if(plugin.getSettings().spawnersPlacementPermission && !e.getPlayer().hasPermission("wildstacker.place.*") &&
                !e.getPlayer().hasPermission("wildstacker.place." + stackedSpawner.getSpawnedType().name().toLowerCase())) {
            e.setCancelled(true);
            Locale.SPAWNER_PLACE_BLOCKED.send(e.getPlayer(), "wildstacker.place." + stackedSpawner.getSpawnedType().name().toLowerCase());
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

        int toPlace = 1;

        if(e.getPlayer().isSneaking() && plugin.getSettings().spawnersShiftPlaceStack){
            toPlace = e.getItemInHand().getAmount();
            spawnerItemAmount *= toPlace;
        }

        if(e.getPlayer().getGameMode() != GameMode.CREATIVE){
            int limit = stackedSpawner.getStackLimit();
            //If the spawnerItemAmount is larger than the spawner limit, we want to give to the player the leftovers
            if(limit < spawnerItemAmount){
                ItemStack spawnerItem = plugin.getProviders().getSpawnerItem(stackedSpawner.getSpawner(), spawnerItemAmount - limit);
                //Adding the item to the inventory after the spawner is placed
                spawnerItemAmount = limit;
                Executor.sync(() -> {
                    if (!e.isCancelled())
                        ItemUtil.addItem(spawnerItem, e.getPlayer().getInventory(), e.getPlayer().getLocation());
                }, 1L);
            }
        }

        double amountToCharge = plugin.getSettings().placeChargeAmount * (plugin.getSettings().placeChargeMultiply ? spawnerItemAmount : 1);

        if(EconomyHook.isVaultEnabled() && EconomyHook.getMoneyInBank(e.getPlayer()) < amountToCharge){
            e.setCancelled(true);
            Locale.SPAWNER_PLACE_NOT_ENOUGH_MONEY.send(e.getPlayer(), amountToCharge);
            return;
        }

        //Stacking spawner
        stackedSpawner.setStackAmount(spawnerItemAmount, false);
        CreatureSpawner targetSpawner = stackedSpawner.tryStack();

        if(targetSpawner == null){
            if(isChunkLimit(e.getBlock().getChunk())){
                e.setCancelled(true);
                return;
            }

            if(plugin.getSettings().onlyOneSpawner){
                for(StackedSpawner nearbySpawner : stackedSpawner.getNearbySpawners()){
                    if(nearbySpawner.getStackAmount() >= nearbySpawner.getStackLimit()){
                        e.setCancelled(true);
                        stackedSpawner.remove();
                        return;
                    }
                }
            }

            SpawnerPlaceEvent spawnerPlaceEvent = new SpawnerPlaceEvent(e.getPlayer(), stackedSpawner);
            Bukkit.getPluginManager().callEvent(spawnerPlaceEvent);

            if(spawnerPlaceEvent.isCancelled()) {
                e.setCancelled(true);
                return;
            }
        }

        else{
            e.setCancelled(true);

            if(Bukkit.getPluginManager().isPluginEnabled("CoreProtect"))
                CoreProtectHook.recordBlockChange(e.getPlayer(), targetSpawner.getBlock(), true);

            stackedSpawner = WStackedSpawner.of(targetSpawner);
        }

        //Removing item from player's inventory
        if(e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            ItemStack is = e.getItemInHand().clone();
            is.setAmount(Math.max(0, is.getAmount() - toPlace));
            ItemUtil.setItemInHand(e.getPlayer().getInventory(), e.getItemInHand(), is);
        }

        EconomyHook.withdrawMoney(e.getPlayer(), amountToCharge);

        Locale.SPAWNER_PLACE.send(e.getPlayer(), EntityUtil.getFormattedType(stackedSpawner.getSpawnedType().name()), stackedSpawner.getStackAmount(), amountToCharge);
    }

    //Priority is high so it can be fired before SilkSpawners
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e){
        if(!plugin.getSettings().spawnersStackingEnabled || e.getBlock().getType() != Materials.SPAWNER.toBukkitType())
            return;

        StackedSpawner stackedSpawner = WStackedSpawner.of(e.getBlock());

        e.setCancelled(true);

        int originalAmount = stackedSpawner.getStackAmount();
        int stackAmount = e.getPlayer().isSneaking() && plugin.getSettings().shiftGetWholeSpawnerStack ? originalAmount : 1;

        double amountToCharge = plugin.getSettings().breakChargeAmount * (plugin.getSettings().breakChargeMultiply ? stackAmount : 1);

        if(EconomyHook.isVaultEnabled() && EconomyHook.getMoneyInBank(e.getPlayer()) < amountToCharge){
            e.setCancelled(true);
            Locale.SPAWNER_BREAK_NOT_ENOUGH_MONEY.send(e.getPlayer(), amountToCharge);
            return;
        }

        if(stackedSpawner.tryUnstack(stackAmount)){
            if(Bukkit.getPluginManager().isPluginEnabled("CoreProtect"))
                CoreProtectHook.recordBlockChange(e.getPlayer(), e.getBlock(), false);

            plugin.getProviders().dropOrGiveItem(e.getPlayer(), stackedSpawner.getSpawner(), stackAmount);

            if(stackedSpawner.getStackAmount() <= 0)
                e.getBlock().setType(Material.AIR);

            EconomyHook.withdrawMoney(e.getPlayer(), amountToCharge);

            Locale.SPAWNER_BREAK.send(e.getPlayer(), EntityUtil.getFormattedType(stackedSpawner.getSpawnedType().name()), stackAmount, amountToCharge);
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

            int amount;

            //Explosions can break the whole stack
            if (plugin.getSettings().explosionsBreakSpawnerStack) {
                amount = stackedSpawner.getStackAmount();
                stackedSpawner.tryUnstack(stackedSpawner.getStackAmount());
            } else {
                amount = 1;
                stackedSpawner.tryUnstack(1);
            }

            if(ThreadLocalRandom.current().nextInt(100) < plugin.getSettings().explosionsBreakChance) {
                plugin.getProviders().dropOrGiveItem(e.getEntity(), stackedSpawner.getSpawner(), amount);
            }

            if(stackedSpawner.getStackAmount() <= 0)
                block.setType(Material.AIR);

            //If the amount of the spawner is more than 1, we don't need to destroy it
            e.blockList().remove(block);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerSpawn(SpawnerSpawnEvent e){
        if(!(e.getEntity() instanceof LivingEntity))
            return;

        //DO NOT CANCEL EVENT - CAUSES ENTITIES TO SPAWN LIKE CRAZY
        //e.setCancelled(true);
        //Doing it on the next tick so taco paper won't get weird message
        Executor.sync(() -> e.getEntity().remove(), 1L);

        StackedSpawner stackedSpawner = WStackedSpawner.of(e.getSpawner());

        if(!plugin.getSettings().entitiesStackingEnabled || plugin.getSettings().blacklistedEntities.contains(e.getEntityType().name()) ||
                plugin.getSettings().blacklistedEntities.contains("SPAWNER")) {
            for (int i = 0; i < stackedSpawner.getStackAmount(); i++) {
                StackedEntity stackedEntity = WStackedEntity.of(plugin.getSystemManager().spawnEntityWithoutStacking(e.getLocation(), e.getEntityType().getEntityClass()));
                com.bgsoftware.wildstacker.api.events.SpawnerSpawnEvent spawnerSpawnEvent = new com.bgsoftware.wildstacker.api.events.SpawnerSpawnEvent(stackedEntity, stackedSpawner);
                Bukkit.getPluginManager().callEvent(spawnerSpawnEvent);
            }
        }

        else{
            StackedEntity stackedEntity = WStackedEntity.of(plugin.getSystemManager().spawnEntityWithoutStacking(e.getLocation(), e.getEntityType().getEntityClass()));
            com.bgsoftware.wildstacker.api.events.SpawnerSpawnEvent spawnerSpawnEvent = new com.bgsoftware.wildstacker.api.events.SpawnerSpawnEvent(stackedEntity, stackedSpawner);
            Bukkit.getPluginManager().callEvent(spawnerSpawnEvent);

            stackedEntity.setStackAmount(stackedSpawner.getStackAmount(), true);

            stackedEntity.trySpawnerStack(stackedSpawner);
        }
    }

    //Same as SilkSpawnersSpawnerChangeEvent, but will only work if SilkSpawners is disabled
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerChange(PlayerInteractEvent e){
        if(e.getItem() == null || e.getAction() != Action.RIGHT_CLICK_BLOCK ||
                !Materials.isValidAndSpawnEgg(e.getItem()) || e.getClickedBlock().getType() != Materials.SPAWNER.toBukkitType())
            return;

        StackedSpawner stackedSpawner = WStackedSpawner.of(e.getClickedBlock());

        if(!plugin.getSettings().changeUsingEggs || (plugin.getSettings().eggsStackMultiply &&
                stackedSpawner.getStackAmount() > ItemUtil.countItem(e.getPlayer().getInventory(), e.getItem())) ||
                EntityTypes.fromName(stackedSpawner.getSpawnedType().name()) == ItemUtil.getEntityType(e.getItem())) {
            e.setCancelled(true);
            return;
        }

        Executor.sync(() -> {
            stackedSpawner.updateName();
            if(e.getPlayer().getGameMode() != GameMode.CREATIVE && plugin.getSettings().eggsStackMultiply)
                ItemUtil.removeItem(e.getPlayer().getInventory(), e.getItem(), stackedSpawner.getStackAmount() - 1);
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
                e.getPlayer().openInventory(Bukkit.createInventory(null, 9 * 4, "Add items here (" + EntityUtil.getFormattedType(stackedSpawner.getSpawnedType().name()) + ")"));
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
                    .replace("{1}", EntityUtil.getFormattedType(stackedSpawner.getSpawnedType().name()))
                    .replace("{2}", EntityUtil.getFormattedType(stackedSpawner.getSpawnedType().name()).toUpperCase());
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

        double amountToCharge = plugin.getSettings().breakChargeAmount * (plugin.getSettings().breakChargeMultiply ? removeAmount : 1);

        if(EconomyHook.isVaultEnabled() && EconomyHook.getMoneyInBank(player) < amountToCharge){
            e.setCancelled(true);
            Locale.SPAWNER_BREAK_NOT_ENOUGH_MONEY.send(player, amountToCharge);
            return;
        }

        if(stackedSpawner.tryUnstack(removeAmount)){
            if(Bukkit.getPluginManager().isPluginEnabled("CoreProtect"))
                CoreProtectHook.recordBlockChange(player, spawnerBlock, false);

            EconomyHook.withdrawMoney(player, amountToCharge);

            //noinspection all
            plugin.getProviders().dropOrGiveItem((Player) null, stackedSpawner.getSpawner(), removeAmount);

            if(stackedSpawner.getStackAmount() <= 0)
                spawnerBlock.setType(Material.AIR);
            if(spawnerBlock.getType() == Materials.SPAWNER.toBukkitType())
                Locale.SPAWNER_BREAK.send(player, EntityUtil.getFormattedType(stackedSpawner.getSpawnedType().name()), stackedSpawner.getStackAmount(), amountToCharge);
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
                            amount += ItemUtil.getSpawnerItemAmount(itemStack);
                        else if (itemStack.getType() != Material.AIR)
                            ItemUtil.addItem(itemStack, e.getPlayer().getInventory(), stackedSpawner.getLocation());
                    }
                }

                if(amount != 0) {
                    int limit = stackedSpawner.getStackLimit();
                    int newStackAmount = stackedSpawner.getStackAmount() + amount;

                    if(stackedSpawner.getStackAmount() + amount > limit){
                        ItemStack toAdd = plugin.getProviders().getSpawnerItem(stackedSpawner.getSpawner(),stackedSpawner.getStackAmount() + amount - limit);
                        ItemUtil.addItem(toAdd, e.getPlayer().getInventory(), stackedSpawner.getLocation());
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSpawnerInspect(PlayerInteractEvent e){
        if(e.getItem() == null || !e.getItem().isSimilar(plugin.getSettings().inspectTool) || !plugin.getSystemManager().isStackedSpawner(e.getClickedBlock()))
            return;

        e.setCancelled(true);

        StackedSpawner stackedSpawner = WStackedSpawner.of(e.getClickedBlock());

        Locale.SPAWNER_INFO_HEADER.send(e.getPlayer());
        Locale.SPAWNER_INFO_TYPE.send(e.getPlayer(), EntityUtil.getFormattedType(stackedSpawner.getSpawnedType().name()));
        Locale.SPAWNER_INFO_AMOUNT.send(e.getPlayer(), stackedSpawner.getStackAmount());
        Locale.SPAWNER_INFO_FOOTER.send(e.getPlayer());
    }

    private boolean isChunkLimit(Chunk chunk){
        int chunkLimit = plugin.getSettings().spawnersChunkLimit;

        if(chunkLimit <= 0)
            return false;

        int spawnersInsideChunk = (int) plugin.getNMSAdapter().getTileEntities(chunk, blockState -> blockState instanceof CreatureSpawner).count();

        return spawnersInsideChunk >= chunkLimit;
    }

}
