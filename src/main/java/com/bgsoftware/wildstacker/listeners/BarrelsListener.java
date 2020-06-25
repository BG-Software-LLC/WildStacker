package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.UnstackResult;
import com.bgsoftware.wildstacker.api.events.BarrelPlaceEvent;
import com.bgsoftware.wildstacker.api.events.BarrelPlaceInventoryEvent;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.hooks.CoreProtectHook;
import com.bgsoftware.wildstacker.key.Key;
import com.bgsoftware.wildstacker.objects.WStackedBarrel;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public final class BarrelsListener implements Listener {

    private final Set<UUID> barrelsToggleCommandPlayers = new HashSet<>();
    private final Map<UUID, Location> barrelPlaceInventory = new HashMap<>();
    private final WildStackerPlugin plugin;

    public BarrelsListener(WildStackerPlugin plugin){
        this.plugin = plugin;
        if(ServerVersion.isAtLeast(ServerVersion.v1_9))
            plugin.getServer().getPluginManager().registerEvents(new CauldronChangeListener(), plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBarrelPlace(BlockPlaceEvent e){
        if(!plugin.getSettings().barrelsStackingEnabled)
            return;

        if(!isBarrelBlock(e.getBlock()))
            return;

        if(ItemUtils.isOffHand(e)){
            e.setCancelled(true);
            return;
        }

        if(!plugin.getSettings().barrelsRequiredPermission.isEmpty() &&
                !e.getPlayer().hasPermission(plugin.getSettings().barrelsRequiredPermission)){
            e.setCancelled(true);
            Locale.BARREL_NO_PERMISSION.send(e.getPlayer());
            return;
        }

        ItemStack inHand = e.getItemInHand().clone();
        int toPlace = ItemUtils.getSpawnerItemAmount(inHand);

        if(toPlace > 1 || (plugin.getSettings().barrelsToggleCommand && !barrelsToggleCommandPlayers.contains(e.getPlayer().getUniqueId())))
            return;

        StackedBarrel stackedBarrel = WStackedBarrel.of(e.getBlockPlaced());

        if(stackedBarrel.isBlacklisted() || !stackedBarrel.isWhitelisted() || stackedBarrel.isWorldDisabled()) {
            stackedBarrel.remove();
            return;
        }

        if(e.getBlockPlaced().getY() > e.getBlockAgainst().getY() && plugin.getSystemManager().isStackedBarrel(e.getBlockAgainst())){
            e.setCancelled(true);
            stackedBarrel.remove();
            return;
        }

        boolean replaceAir = false;

        if(plugin.getSettings().barrelsShiftPlaceStack){
            toPlace *= inHand.getAmount();
            replaceAir = true;
        }

        stackedBarrel.setStackAmount(toPlace, false);

        Chunk chunk = e.getBlock().getChunk();
        boolean REPLACE_AIR = replaceAir;

        //Stacking barrel
        Optional<Block> blockOptional = stackedBarrel.runStack();

        if(!blockOptional.isPresent()) {
            if(isChunkLimit(chunk)) {
                Locale.CHUNK_LIMIT_EXCEEDED.send(e.getPlayer(), ItemUtils.getFormattedType(stackedBarrel.getBarrelItem(1)) + " Barrels");
                e.setCancelled(true);
                stackedBarrel.remove();
                return;
            }

            BarrelPlaceEvent barrelPlaceEvent = new BarrelPlaceEvent(e.getPlayer(), stackedBarrel, inHand);
            Bukkit.getPluginManager().callEvent(barrelPlaceEvent);

            if(barrelPlaceEvent.isCancelled()) {
                e.setCancelled(true);
                stackedBarrel.remove();
                return;
            }

            e.getBlockPlaced().setType(Material.CAULDRON);

            if(ServerVersion.isLessThan(ServerVersion.v1_9)){
                Executor.sync(() -> {
                    if(e.getBlockPlaced().getType() != Material.CAULDRON)
                        return;

                    stackedBarrel.updateName();
                    Locale.BARREL_PLACE.send(e.getPlayer(), ItemUtils.getFormattedType(stackedBarrel.getBarrelItem(1)));

                    finishBarrelPlace(e.getPlayer(), inHand, stackedBarrel, REPLACE_AIR);
                }, 1L);
                return;
            }

            stackedBarrel.updateName();
            Locale.BARREL_PLACE.send(e.getPlayer(), ItemUtils.getFormattedType(stackedBarrel.getBarrelItem(1)));
        }
        else {
            e.setCancelled(true);

            revokeItem(e.getPlayer(), inHand);

            StackedBarrel targetBarrel = WStackedBarrel.of(blockOptional.get());
            Locale.BARREL_UPDATE.send(e.getPlayer(), ItemUtils.getFormattedType(targetBarrel.getBarrelItem(1)), targetBarrel.getStackAmount());
        }

        finishBarrelPlace(e.getPlayer(), inHand, stackedBarrel, REPLACE_AIR);
    }

    private void finishBarrelPlace(Player player, ItemStack inHand, StackedBarrel stackedBarrel, boolean replaceAir){
        //Removing item from player's inventory
        if(player.getGameMode() != GameMode.CREATIVE && replaceAir)
            ItemUtils.setItemInHand(player.getInventory(), inHand, new ItemStack(Material.AIR));

        CoreProtectHook.recordBlockChange(player, stackedBarrel.getLocation(), stackedBarrel.getType(), (byte) stackedBarrel.getData(), true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBarrelBreak(BlockBreakEvent e){
        if(!plugin.getSettings().barrelsStackingEnabled || e.getBlock().getType() != Material.CAULDRON)
            return;

        if(!plugin.getSystemManager().isStackedBarrel(e.getBlock()))
            return;

        StackedBarrel stackedBarrel = WStackedBarrel.of(e.getBlock());
        int stackSize = stackedBarrel.getStackAmount();

        ItemStack dropStack = stackedBarrel.getBarrelItem(stackSize);

        if(e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            if(plugin.getSettings().barrelsAutoPickup) {
                ItemUtils.addItem(dropStack, e.getPlayer().getInventory(), e.getBlock().getLocation());
            }
            else {
                if(plugin.getSettings().dropStackedItem) {
                    dropStack = ItemUtils.setSpawnerItemAmount(dropStack, stackSize);
                    dropStack.setAmount(1);

                    ItemMeta itemMeta = dropStack.getItemMeta();
                    itemMeta.setDisplayName(WildStackerPlugin.getPlugin().getSettings().giveItemName
                            .replace("{0}", stackSize + "")
                            .replace("{1}", ItemUtils.getFormattedType(new ItemStack(stackedBarrel.getType())))
                            .replace("{2}", "Barrel")
                    );
                    dropStack.setItemMeta(itemMeta);

                }

                ItemUtils.dropItem(dropStack, e.getBlock().getLocation());
            }
        }

        e.setCancelled(true);

        if(stackedBarrel.runUnstack(stackedBarrel.getStackAmount()) == UnstackResult.SUCCESS){
            CoreProtectHook.recordBlockChange(e.getPlayer(), stackedBarrel.getLocation(), stackedBarrel.getType(), (byte) stackedBarrel.getData(), false);

            e.getBlock().setType(Material.AIR);

            Locale.BARREL_BREAK.send(e.getPlayer(), stackSize, ItemUtils.getFormattedType(stackedBarrel.getBarrelItem(1)));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBarrelClick(PlayerInteractEvent e){
        if(!plugin.getSettings().barrelsStackingEnabled)
            return;

        if(ItemUtils.isOffHand(e) || e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if(!plugin.getSystemManager().isStackedBarrel(e.getClickedBlock()))
            return;

        StackedBarrel stackedBarrel = WStackedBarrel.of(e.getClickedBlock());

        if(e.getItem() != null)
            return;

        if(e.getPlayer().isSneaking() && plugin.getSettings().barrelsPlaceInventory){
            barrelPlaceInventory.put(e.getPlayer().getUniqueId(), stackedBarrel.getLocation());
            e.getPlayer().openInventory(Bukkit.createInventory(null, 9 * 4,
                    plugin.getSettings().barrelsPlaceInventoryTitle.replace("{0}", EntityUtils.getFormattedType(stackedBarrel.getType().name()))));
        }

        else{
            stackedBarrel.runUnstack(1);

            CoreProtectHook.recordBlockChange(e.getPlayer(), stackedBarrel.getLocation(), stackedBarrel.getType(), (byte) stackedBarrel.getData(), false);

            if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                if(plugin.getSettings().barrelsAutoPickup) {
                    ItemUtils.addItem(stackedBarrel.getBarrelItem(1), e.getPlayer().getInventory(), e.getClickedBlock().getLocation());
                }
                else {
                    ItemUtils.dropItem(stackedBarrel.getBarrelItem(1), e.getClickedBlock().getLocation());
                }
            }

            if(stackedBarrel.getStackAmount() <= 0)
                e.getClickedBlock().setType(Material.AIR);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryPlaceMove(InventoryClickEvent e){
        if(!barrelPlaceInventory.containsKey(e.getWhoClicked().getUniqueId()))
            return;

        if(!plugin.getSettings().barrelsRequiredPermission.isEmpty() &&
                !e.getWhoClicked().hasPermission(plugin.getSettings().barrelsRequiredPermission)){
            e.setCancelled(true);
            Locale.BARREL_NO_PERMISSION.send(e.getWhoClicked());
            return;
        }

        StackedBarrel stackedBarrel = WStackedBarrel.of(barrelPlaceInventory.get(e.getWhoClicked().getUniqueId()).getBlock());

        if(e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR && !e.getCurrentItem().isSimilar(stackedBarrel.getBarrelItem(1)))
            e.setCancelled(true);
        if(e.getAction() == InventoryAction.HOTBAR_SWAP)
            e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e){
        if (barrelPlaceInventory.containsKey(e.getPlayer().getUniqueId())) {
            StackedBarrel stackedBarrel = WStackedBarrel.of(barrelPlaceInventory.get(e.getPlayer().getUniqueId()).getBlock());
            ItemStack barrelItem = stackedBarrel.getBarrelItem(1);
            int amount = 0;
            boolean dropAll = false;

            if(!plugin.getSettings().barrelsRequiredPermission.isEmpty() &&
                    !e.getPlayer().hasPermission(plugin.getSettings().barrelsRequiredPermission)){
                Locale.BARREL_NO_PERMISSION.send(e.getPlayer());
                dropAll = true;
            }

            for(ItemStack itemStack : e.getInventory().getContents()){
                if(barrelItem.isSimilar(itemStack) && !dropAll)
                    amount += itemStack.getAmount();
                else if(itemStack != null && itemStack.getType() != Material.AIR)
                    ItemUtils.addItem(itemStack, e.getPlayer().getInventory(), stackedBarrel.getLocation());
            }

            if(amount != 0) {
                int limit = stackedBarrel.getStackLimit();
                int newStackAmount = stackedBarrel.getStackAmount() + amount;

                if(stackedBarrel.getStackAmount() + amount > limit){
                    ItemStack toAdd = barrelItem.clone();
                    toAdd.setAmount(stackedBarrel.getStackAmount() + amount - limit);
                    ItemUtils.addItem(toAdd, e.getPlayer().getInventory(), stackedBarrel.getLocation());
                    newStackAmount = limit;
                }

                BarrelPlaceInventoryEvent barrelPlaceInventoryEvent = new BarrelPlaceInventoryEvent((Player) e.getPlayer(), stackedBarrel, newStackAmount - stackedBarrel.getStackAmount());
                Bukkit.getPluginManager().callEvent(barrelPlaceInventoryEvent);

                if(!barrelPlaceInventoryEvent.isCancelled()) {
                    stackedBarrel.setStackAmount(newStackAmount, true);
                    Locale.BARREL_UPDATE.send(e.getPlayer(), ItemUtils.getFormattedType(barrelItem), stackedBarrel.getStackAmount());
                }
                else{
                    ItemUtils.addItems(e.getInventory().getContents(), e.getPlayer().getInventory(), stackedBarrel.getLocation());
                }
            }

            barrelPlaceInventory.remove(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent e){
        if(!plugin.getSettings().barrelsStackingEnabled)
            return;

        List<Block> blockList = new ArrayList<>(e.blockList());

        for(Block block : blockList){
            if(!plugin.getSystemManager().isStackedBarrel(block))
                continue;

            e.blockList().remove(block);

            StackedBarrel stackedBarrel = WStackedBarrel.of(block);

            int amount = plugin.getSettings().explosionsBreakBarrelStack ? stackedBarrel.getStackAmount() : 1;

            ItemUtils.dropItem(stackedBarrel.getBarrelItem(amount), block.getLocation());
            stackedBarrel.runUnstack(amount);
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractAtEntityEvent e){
        if(e.getRightClicked() instanceof ArmorStand){
            for(StackedBarrel stackedBarrel : plugin.getSystemManager().getStackedBarrels()){
                if(e.getRightClicked().getLocation().getBlock().getLocation().equals(stackedBarrel.getLocation()))
                    e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e){
        if(!plugin.getSettings().barrelsToggleCommand || !e.getPlayer().hasPermission("wildstacker.toggle"))
            return;

        String commandSyntax = "/" + plugin.getSettings().barrelsToggleCommandSyntax;

        if(!e.getMessage().equalsIgnoreCase(commandSyntax) && !e.getMessage().startsWith(commandSyntax + " "))
            return;

        e.setCancelled(true);

        if(barrelsToggleCommandPlayers.contains(e.getPlayer().getUniqueId())){
            barrelsToggleCommandPlayers.remove(e.getPlayer().getUniqueId());
            Locale.BARREL_TOGGLE_OFF.send(e.getPlayer());
        }
        else{
            barrelsToggleCommandPlayers.add(e.getPlayer().getUniqueId());
            Locale.BARREL_TOGGLE_ON.send(e.getPlayer());
        }

    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e){
        for(Block block : e.getBlocks()){
            if(plugin.getSystemManager().isStackedBarrel(block)) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e){
        for(Block block : e.getBlocks()){
            if(plugin.getSystemManager().isStackedBarrel(block)) {
                e.setCancelled(true);
                break;
            }
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

    private boolean isBarrelBlock(Block block){
        Key key = Key.of(ItemUtils.getFromBlock(block));
        return (plugin.getSettings().whitelistedBarrels.isEmpty() ||
                plugin.getSettings().whitelistedBarrels.contains(key)) &&
                !plugin.getSettings().blacklistedBarrels.contains(key) &&
                !plugin.getSettings().barrelsDisabledWorlds.contains(block.getWorld().getName());
    }

    private boolean isChunkLimit(Chunk chunk){
        int chunkLimit = plugin.getSettings().barrelsChunkLimit;

        if(chunkLimit <= 0)
            return false;

        return plugin.getSystemManager().getStackedBarrels(chunk).size() > chunkLimit;
    }

    private class CauldronChangeListener implements Listener{

        @EventHandler(priority = EventPriority.LOWEST)
        public void onCauldronFill(CauldronLevelChangeEvent e){
            if(plugin.getSystemManager().isStackedBarrel(e.getBlock())) {
                e.setCancelled(true);
                e.setNewLevel(0);
            }
        }

    }

}
