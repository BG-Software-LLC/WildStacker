package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.UnstackResult;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.hooks.SlimefunHook;
import com.bgsoftware.wildstacker.hooks.listeners.IStackedBlockListener;
import com.bgsoftware.wildstacker.menu.BarrelsPlaceMenu;
import com.bgsoftware.wildstacker.objects.WStackedBarrel;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.events.EventsCaller;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
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
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public final class BarrelsListener implements Listener {

    private final Set<UUID> barrelsToggleCommandPlayers = new HashSet<>();
    private final Set<UUID> alreadyBarrelsPlacedPlayers = new HashSet<>();
    private final WildStackerPlugin plugin;

    public BarrelsListener(WildStackerPlugin plugin) {
        this.plugin = plugin;
        if (ServerVersion.isAtLeast(ServerVersion.v1_9))
            plugin.getServer().getPluginManager().registerEvents(new CauldronChangeListener(), plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBarrelPlace(BlockPlaceEvent e) {
        if (!plugin.getSettings().barrelsStackingEnabled)
            return;

        if (!isBarrelBlock(e.getBlock()) || SlimefunHook.isSlimefunItem(e.getItemInHand()))
            return;

        if (ItemUtils.isOffHand(e)) {
            e.setCancelled(true);
            return;
        }

        try {
            ItemStack inHand = e.getItemInHand().clone();
            int toPlace = ItemUtils.getSpawnerItemAmount(inHand);

            if (toPlace <= 1 && plugin.getSettings().barrelsToggleCommand && !barrelsToggleCommandPlayers.contains(e.getPlayer().getUniqueId()))
                return;

            if (!plugin.getSettings().barrelsRequiredPermission.isEmpty() &&
                    !e.getPlayer().hasPermission(plugin.getSettings().barrelsRequiredPermission)) {
                e.setCancelled(true);
                Locale.BARREL_NO_PERMISSION.send(e.getPlayer());
                return;
            }

            if (!alreadyBarrelsPlacedPlayers.add(e.getPlayer().getUniqueId())) {
                e.setCancelled(true);
                return;
            }

            Executor.sync(() -> alreadyBarrelsPlacedPlayers.remove(e.getPlayer().getUniqueId()), 2L);

            StackedBarrel stackedBarrel = WStackedBarrel.of(e.getBlockPlaced());

            if (!stackedBarrel.isCached()) {
                stackedBarrel.remove();
                return;
            }

            if (e.getBlockPlaced().getY() > e.getBlockAgainst().getY() && plugin.getSystemManager().isStackedBarrel(e.getBlockAgainst())) {
                e.setCancelled(true);
                stackedBarrel.remove();
                return;
            }

            boolean replaceAir = false;

            if (plugin.getSettings().barrelsShiftPlaceStack) {
                toPlace *= inHand.getAmount();
                replaceAir = true;
            }

            stackedBarrel.setStackAmount(toPlace, false);

            Chunk chunk = e.getBlock().getChunk();
            boolean REPLACE_AIR = replaceAir;

            //Stacking barrel
            Optional<Block> blockOptional = stackedBarrel.runStack();

            e.setCancelled(true);

            if (!blockOptional.isPresent()) {
                if (isChunkLimit(chunk)) {
                    Locale.CHUNK_LIMIT_EXCEEDED.send(e.getPlayer(), ItemUtils.getFormattedType(stackedBarrel.getBarrelItem(1)) + " Barrels");
                    stackedBarrel.remove();
                    return;
                }

                if (!EventsCaller.callBarrelPlaceEvent(e.getPlayer(), stackedBarrel, inHand)) {
                    stackedBarrel.remove();
                    return;
                }

                revokeItem(e.getPlayer(), inHand);

                boolean attemptPlaceDelayed = ServerVersion.isLessThan(ServerVersion.v1_9);

                //Because we cancel the event (tile entity issues), we need to change the block on a tick after that.
                Executor.sync(() -> {
                    e.getBlock().setType(Material.CAULDRON);
                    stackedBarrel.createDisplayBlock();

                    if (attemptPlaceDelayed) {
                        if (e.getBlockPlaced().getType() != Material.CAULDRON)
                            return;

                        stackedBarrel.updateName();

                        Locale.BARREL_PLACE.send(e.getPlayer(), ItemUtils.getFormattedType(stackedBarrel.getBarrelItem(1)));

                        finishBarrelPlace(e.getPlayer(), inHand, stackedBarrel, REPLACE_AIR);
                    }
                }, 1L);

                if (attemptPlaceDelayed)
                    return;

                stackedBarrel.updateName();

                Locale.BARREL_PLACE.send(e.getPlayer(), ItemUtils.getFormattedType(stackedBarrel.getBarrelItem(1)));
            } else {
                revokeItem(e.getPlayer(), inHand);

                StackedBarrel targetBarrel = WStackedBarrel.of(blockOptional.get());
                Locale.BARREL_UPDATE.send(e.getPlayer(), ItemUtils.getFormattedType(targetBarrel.getBarrelItem(1)), targetBarrel.getStackAmount());
            }

            finishBarrelPlace(e.getPlayer(), inHand, stackedBarrel, REPLACE_AIR);
        } catch (Exception ex) {
            alreadyBarrelsPlacedPlayers.remove(e.getPlayer().getUniqueId());
            throw ex;
        }
    }

    private void finishBarrelPlace(Player player, ItemStack inHand, StackedBarrel stackedBarrel, boolean replaceAir) {
        //Removing item from player's inventory
        if (player.getGameMode() != GameMode.CREATIVE && replaceAir)
            ItemUtils.setItemInHand(player.getInventory(), inHand, new ItemStack(Material.AIR));

        plugin.getProviders().notifyStackedBlockListeners(player, stackedBarrel.getLocation(),
                stackedBarrel.getType(), (byte) stackedBarrel.getData(), IStackedBlockListener.Action.BLOCK_PLACE);

        alreadyBarrelsPlacedPlayers.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBarrelBreak(BlockBreakEvent e) {
        if (!plugin.getSettings().barrelsStackingEnabled || e.getBlock().getType() != Material.CAULDRON)
            return;

        if (!plugin.getSystemManager().isStackedBarrel(e.getBlock()))
            return;

        StackedBarrel stackedBarrel = WStackedBarrel.of(e.getBlock());
        int stackSize = stackedBarrel.getStackAmount();

        if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            ItemStack dropStack = EventsCaller.callBarrelDropEvent(stackedBarrel, e.getPlayer(), stackSize);

            if (plugin.getSettings().barrelsAutoPickup) {
                ItemUtils.addItem(dropStack, e.getPlayer().getInventory(), e.getBlock().getLocation());
            } else {
                ItemUtils.dropItem(dropStack, e.getBlock().getLocation());
            }
        }

        e.setCancelled(true);

        if (stackedBarrel.runUnstack(stackedBarrel.getStackAmount(), e.getPlayer()) == UnstackResult.SUCCESS) {
            plugin.getProviders().notifyStackedBlockListeners(e.getPlayer(), stackedBarrel.getLocation(),
                    stackedBarrel.getType(), (byte) stackedBarrel.getData(), IStackedBlockListener.Action.BLOCK_BREAK);

            e.getBlock().setType(Material.AIR);

            Locale.BARREL_BREAK.send(e.getPlayer(), stackSize, ItemUtils.getFormattedType(stackedBarrel.getBarrelItem(1)));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBarrelClick(PlayerInteractEvent e) {
        if (!plugin.getSettings().barrelsStackingEnabled)
            return;

        if (ItemUtils.isOffHand(e) || e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (!plugin.getSystemManager().isStackedBarrel(e.getClickedBlock()))
            return;

        StackedBarrel stackedBarrel = WStackedBarrel.of(e.getClickedBlock());

        if (e.getItem() != null)
            return;

        if (e.getPlayer().isSneaking() && plugin.getSettings().barrelsPlaceInventory) {
            BarrelsPlaceMenu.open(e.getPlayer(), stackedBarrel);
        } else {
            stackedBarrel.runUnstack(1, e.getPlayer());

            plugin.getProviders().notifyStackedBlockListeners(e.getPlayer(), stackedBarrel.getLocation(),
                    stackedBarrel.getType(), (byte) stackedBarrel.getData(), IStackedBlockListener.Action.BLOCK_BREAK);

            if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                ItemStack dropStack = EventsCaller.callBarrelDropEvent(stackedBarrel, e.getPlayer(), 1);

                if (plugin.getSettings().barrelsAutoPickup) {
                    ItemUtils.addItem(dropStack, e.getPlayer().getInventory(), e.getClickedBlock().getLocation());
                } else {
                    ItemUtils.dropItem(dropStack, e.getClickedBlock().getLocation());
                }
            }

            if (stackedBarrel.getStackAmount() <= 0)
                e.getClickedBlock().setType(Material.AIR);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent e) {
        if (!plugin.getSettings().barrelsStackingEnabled)
            return;

        List<Block> blockList = new ArrayList<>(e.blockList());

        for (Block block : blockList) {
            if (!plugin.getSystemManager().isStackedBarrel(block))
                continue;

            e.blockList().remove(block);

            StackedBarrel stackedBarrel = WStackedBarrel.of(block);

            int amount = plugin.getSettings().explosionsBreakBarrelStack ? stackedBarrel.getStackAmount() : 1;
            ItemStack barrelItem = EventsCaller.callBarrelDropEvent(stackedBarrel, null, amount);

            ItemUtils.dropItem(barrelItem, block.getLocation());
            stackedBarrel.runUnstack(amount, e.getEntity());
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked() instanceof ArmorStand) {
            for (StackedBarrel stackedBarrel : plugin.getSystemManager().getStackedBarrels()) {
                if (e.getRightClicked().getLocation().getBlock().getLocation().equals(stackedBarrel.getLocation()))
                    e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        if (!plugin.getSettings().barrelsToggleCommand || !e.getPlayer().hasPermission("wildstacker.toggle"))
            return;

        String commandSyntax = "/" + plugin.getSettings().barrelsToggleCommandSyntax;

        if (!e.getMessage().equalsIgnoreCase(commandSyntax) && !e.getMessage().startsWith(commandSyntax + " "))
            return;

        e.setCancelled(true);

        if (barrelsToggleCommandPlayers.contains(e.getPlayer().getUniqueId())) {
            barrelsToggleCommandPlayers.remove(e.getPlayer().getUniqueId());
            Locale.BARREL_TOGGLE_OFF.send(e.getPlayer());
        } else {
            barrelsToggleCommandPlayers.add(e.getPlayer().getUniqueId());
            Locale.BARREL_TOGGLE_ON.send(e.getPlayer());
        }

    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e) {
        for (Block block : e.getBlocks()) {
            if (plugin.getSystemManager().isStackedBarrel(block)) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        for (Block block : e.getBlocks()) {
            if (plugin.getSystemManager().isStackedBarrel(block)) {
                e.setCancelled(true);
                break;
            }
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

    private boolean isBarrelBlock(Block block) {
        Material type = block.getType();
        return (plugin.getSettings().whitelistedBarrels.size() == 0 ||
                plugin.getSettings().whitelistedBarrels.contains(type)) &&
                !plugin.getSettings().blacklistedBarrels.contains(type) &&
                !plugin.getSettings().barrelsDisabledWorlds.contains(block.getWorld().getName());
    }

    private boolean isChunkLimit(Chunk chunk) {
        int chunkLimit = plugin.getSettings().barrelsChunkLimit;

        if (chunkLimit <= 0)
            return false;

        return plugin.getSystemManager().getStackedBarrels(chunk).size() > chunkLimit;
    }

    private class CauldronChangeListener implements Listener {

        @EventHandler(priority = EventPriority.LOWEST)
        public void onCauldronFill(CauldronLevelChangeEvent e) {
            if (plugin.getSystemManager().isStackedBarrel(e.getBlock())) {
                e.setCancelled(true);
                e.setNewLevel(0);
            }
        }

    }

}
