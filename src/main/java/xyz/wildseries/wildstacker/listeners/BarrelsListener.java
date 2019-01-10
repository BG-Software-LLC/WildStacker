package xyz.wildseries.wildstacker.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.Locale;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.api.events.BarrelPlaceEvent;
import xyz.wildseries.wildstacker.api.objects.StackedBarrel;
import xyz.wildseries.wildstacker.hooks.CoreProtectHook;
import xyz.wildseries.wildstacker.objects.WStackedBarrel;
import xyz.wildseries.wildstacker.utils.EntityUtil;
import xyz.wildseries.wildstacker.utils.ItemUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public final class BarrelsListener implements Listener {

    private WildStackerPlugin plugin;
    private Set<UUID> barrelsToggleCommandPlayers;

    public BarrelsListener(WildStackerPlugin plugin){
        this.plugin = plugin;
        this.barrelsToggleCommandPlayers = new HashSet<>();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBarrelPlace(BlockPlaceEvent e){
        if(!plugin.getSettings().barrelsStackingEnabled || !plugin.getSettings().whitelistedBarrels.contains(e.getItemInHand()))
            return;

        if(plugin.getSettings().barrelsToggleCommand && !barrelsToggleCommandPlayers.contains(e.getPlayer().getUniqueId()))
            return;

        if(plugin.getSettings().barrelsDisabledWorlds.contains(e.getBlockPlaced().getWorld().getName()))
            return;

        if(e.getBlockPlaced().getY() > e.getBlockAgainst().getY() && plugin.getSystemManager().isStackedBarrel(e.getBlockAgainst())){
            e.setCancelled(true);
            return;
        }

        StackedBarrel stackedBarrel = WStackedBarrel.of(e.getBlockPlaced());

        stackedBarrel.setStackAmount(ItemUtil.getSpawnerItemAmount(e.getItemInHand()), true);

        //Stacking barrel
        Block targetBarrel = stackedBarrel.tryStack();

        e.setCancelled(true);

        if(targetBarrel == null) {
            BarrelPlaceEvent barrelPlaceEvent = new BarrelPlaceEvent(e.getPlayer(), stackedBarrel);
            Bukkit.getPluginManager().callEvent(barrelPlaceEvent);

            if(barrelPlaceEvent.isCancelled())
                return;

            Bukkit.getScheduler().runTaskLater(plugin, () -> e.getBlockPlaced().setType(Material.CAULDRON), 1L);
            Locale.BARREL_PLACE.send(e.getPlayer(), ItemUtil.getFormattedType(stackedBarrel.getBarrelItem(1)));
        }
        else {
            stackedBarrel = WStackedBarrel.of(targetBarrel);
            e.getBlockPlaced().setType(Material.AIR);
            Locale.BARREL_UPDATE.send(e.getPlayer(), ItemUtil.getFormattedType(stackedBarrel.getBarrelItem(1)), stackedBarrel.getStackAmount());
        }

        if(Bukkit.getPluginManager().isPluginEnabled("CoreProtect"))
            CoreProtectHook.recordBlockChange(e.getPlayer(), stackedBarrel.getLocation(), stackedBarrel.getType(), (byte) stackedBarrel.getData(), true);

        if(e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            ItemStack inHand = e.getItemInHand().clone();
            inHand.setAmount(inHand.getAmount() - 1);
            e.getPlayer().setItemInHand(inHand);
        }
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

        if(e.getPlayer().getGameMode() != GameMode.CREATIVE)
            e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), dropStack);

        e.setCancelled(true);

        if(!stackedBarrel.tryUnstack(stackedBarrel.getStackAmount())) {
            return;
        }

        if(Bukkit.getPluginManager().isPluginEnabled("CoreProtect"))
            CoreProtectHook.recordBlockChange(e.getPlayer(), stackedBarrel.getLocation(), stackedBarrel.getType(), (byte) stackedBarrel.getData(), false);

        e.getBlock().setType(Material.AIR);

        Locale.BARREL_BREAK.send(e.getPlayer(), stackSize, ItemUtil.getFormattedType(stackedBarrel.getBarrelItem(1)));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBarrelClick(PlayerInteractEvent e){
        if(!plugin.getSettings().barrelsStackingEnabled)
            return;

        if(e.getItem() != null || e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if(!plugin.getSystemManager().isStackedBarrel(e.getClickedBlock()))
            return;

        StackedBarrel stackedBarrel = WStackedBarrel.of(e.getClickedBlock());

        if(e.getPlayer().isSneaking() && plugin.getSettings().barrelsPlaceInventory){
            barrelPlaceInventory.put(e.getPlayer().getUniqueId(), stackedBarrel.getLocation());
            e.getPlayer().openInventory(Bukkit.createInventory(null, 9 * 4, "Add items here (" + EntityUtil.getFormattedType(stackedBarrel.getType().name()) + ")"));
        }

        else {
            stackedBarrel.tryUnstack(1);
            if (Bukkit.getPluginManager().isPluginEnabled("CoreProtect"))
                CoreProtectHook.recordBlockChange(e.getPlayer(), stackedBarrel.getLocation(), stackedBarrel.getType(), (byte) stackedBarrel.getData(), false);
            if (e.getPlayer().getGameMode() != GameMode.CREATIVE)
                e.getClickedBlock().getWorld().dropItemNaturally(e.getClickedBlock().getLocation(), stackedBarrel.getBarrelItem(1));
            if (stackedBarrel.getStackAmount() <= 0)
                e.getClickedBlock().setType(Material.AIR);
        }
    }

    private Map<UUID, Location> barrelPlaceInventory = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryPlaceMove(InventoryClickEvent e){
        if(!barrelPlaceInventory.containsKey(e.getWhoClicked().getUniqueId()))
            return;

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

            for(ItemStack itemStack : e.getInventory().getContents()){
                if(barrelItem.isSimilar(itemStack))
                    amount += itemStack.getAmount();
                else if(itemStack != null && itemStack.getType() != Material.AIR)
                    ItemUtil.addItem(itemStack, e.getPlayer().getInventory(), stackedBarrel.getLocation());
            }

            if(amount != 0) {
                stackedBarrel.setStackAmount(stackedBarrel.getStackAmount() + amount, true);
                Locale.BARREL_UPDATE.send(e.getPlayer(), ItemUtil.getFormattedType(barrelItem), stackedBarrel.getStackAmount());
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

            block.getWorld().dropItemNaturally(block.getLocation(), stackedBarrel.getBarrelItem(amount));
            stackedBarrel.tryUnstack(amount);
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
        if(!plugin.getSettings().barrelsToggleCommand)
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

}
