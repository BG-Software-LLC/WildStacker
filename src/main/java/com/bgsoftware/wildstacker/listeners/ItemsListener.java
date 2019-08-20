package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.hooks.ProtocolLibHook;
import com.bgsoftware.wildstacker.listeners.events.EggLayEvent;
import com.bgsoftware.wildstacker.listeners.events.EntityPickupItemEvent;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.utils.Executor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

@SuppressWarnings("unused")
public final class ItemsListener implements Listener {

    private WildStackerPlugin plugin;

    public ItemsListener(WildStackerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent e){
        if(!plugin.getSettings().itemsStackingEnabled)
            return;

        StackedItem stackedItem = WStackedItem.of(e.getEntity());

        if(stackedItem.isBlacklisted() || !stackedItem.isWhitelisted() || stackedItem.isWorldDisabled())
            return;

        int limit = stackedItem.getStackLimit();

        if(stackedItem.getStackAmount() > limit){
            ItemStack cloned = stackedItem.getItemStack().clone();
            cloned.setAmount(cloned.getAmount() - limit);
            stackedItem.setStackAmount(limit, true);
            Item spawnedItem = e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), cloned);
            spawnedItem.setPickupDelay(40);
        }

        stackedItem.tryStack();

        //Chunk Limit
        Executor.sync(() -> {
            if(isChunkLimit(e.getLocation().getChunk()))
                stackedItem.remove();
        }, 2L);
    }

    @EventHandler
    public void onEggLay(EggLayEvent e){
        StackedEntity stackedEntity = WStackedEntity.of(e.getChicken());
        ItemStack eggItem = e.getEgg().getItemStack();
        eggItem.setAmount(stackedEntity.getStackAmount());
        e.getEgg().setItemStack(eggItem);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemMerge(ItemMergeEvent e){
        if(!plugin.getSettings().itemsStackingEnabled)
            return;

        StackedItem stackedItem = WStackedItem.of(e.getEntity());

        if(stackedItem.isBlacklisted() || !stackedItem.isWhitelisted() || stackedItem.isWorldDisabled())
            return;

        //We are overriding the merge system
        e.setCancelled(true);

        Executor.sync(() -> {
            if(e.getEntity().isValid() && e.getTarget().isValid()){
                StackedItem targetItem = WStackedItem.of(e.getTarget());
                stackedItem.tryStackInto(targetItem);
            }
        }, 5L);
    }

    //This method will be fired even if stacking-drops is disabled.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent e){
        WStackedItem.of(e.getEntity()).remove();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPickup(EntityPickupItemEvent e) {
        StackedItem stackedItem = WStackedItem.of(e.getItem());

        //Should run only if the item is 1 (stacked item)
        if(stackedItem.getStackAmount() > 1 || e.getItem().getItemStack().getType().name().contains("BUCKET")) {
            e.setCancelled(true);

            //Causes too many issues
            if(e.getEntityType().name().equals("DOLPHIN"))
                return;

            int stackAmount = stackedItem.getStackAmount();

            if(e.getInventory() != null) {
                stackedItem.giveItemStack(e.getInventory());
            }else{
                ItemStack itemStack = stackedItem.getItemStack();
                int maxStackSize = plugin.getSettings().itemsFixStackEnabled || itemStack.getType().name().contains("SHULKER_BOX") ? itemStack.getMaxStackSize() : 64;
                if(itemStack.getAmount() > maxStackSize){
                    itemStack.setAmount(maxStackSize);
                    stackedItem.setStackAmount(stackAmount - maxStackSize, true);
                }
                setItemInHand(e.getEntity(), itemStack);
            }

            if(e.getPlayer() != null){
                if(stackAmount != stackedItem.getStackAmount() && plugin.getSettings().itemsSoundEnabled){
                    Sound pickUpItem;

                    //Different name on 1.12
                    try {
                        pickUpItem = Sound.valueOf("ITEM_PICKUP");
                    } catch (IllegalArgumentException ex) {
                        pickUpItem = Sound.valueOf("ENTITY_ITEM_PICKUP");
                    }

                    e.getPlayer().playSound(e.getPlayer().getLocation(), pickUpItem,
                            plugin.getSettings().itemsSoundVolume, plugin.getSettings().itemsSoundPitch);
                }
                
                //Pick up animation
                plugin.getNMSAdapter().playPickupAnimation(e.getPlayer(), e.getItem());
            }

            if (stackedItem.getStackAmount() <= 0) {
                e.getItem().remove();
                stackedItem.remove();
            }
        }
    }

    //This method will be fired even if stacking-drops is disabled.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryPickup(InventoryPickupItemEvent e){
        StackedItem stackedItem = WStackedItem.of(e.getItem());
        if(stackedItem.getStackAmount() > 1) {
            e.setCancelled(true);
            stackedItem.giveItemStack(e.getInventory());
            if (stackedItem.getStackAmount() <= 0) {
                stackedItem.remove();
            }

            Block hopper = e.getItem().getLocation().subtract(0, 1, 0).getBlock();
            hopper.getState().update();
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e){
        if(!plugin.getSettings().itemsNamesToggleEnabled)
            return;

        String commandSyntax = "/" + plugin.getSettings().itemsNamesToggleCommand;

        if(!e.getMessage().equalsIgnoreCase(commandSyntax) && !e.getMessage().startsWith(commandSyntax + " "))
            return;

        e.setCancelled(true);

        if(!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")){
            e.getPlayer().sendMessage(ChatColor.RED + "The command is enabled but ProtocolLib is not installed. Please contact the administrators of the server to solve the issue.");
            return;
        }

        if(ProtocolLibHook.itemsDisabledNames.contains(e.getPlayer().getUniqueId())){
            ProtocolLibHook.itemsDisabledNames.remove(e.getPlayer().getUniqueId());
            Locale.ITEM_NAMES_TOGGLE_ON.send(e.getPlayer());
        }
        else{
            ProtocolLibHook.itemsDisabledNames.add(e.getPlayer().getUniqueId());
            Locale.ITEM_NAMES_TOGGLE_OFF.send(e.getPlayer());
        }

        //Refresh item names
        for(Entity entity : e.getPlayer().getNearbyEntities(50, 256, 50)){
            if(entity instanceof Item && entity.isCustomNameVisible()){
                ProtocolLibHook.updateName(e.getPlayer(), entity);
            }
        }
    }

    private boolean isChunkLimit(Chunk chunk){
        int chunkLimit = plugin.getSettings().itemsChunkLimit;

        if(chunkLimit <= 0)
            return false;

        int itemsInsideChunk = (int) Arrays.stream(chunk.getEntities()).filter(entity -> entity instanceof Item).count();
        return itemsInsideChunk >= chunkLimit;
    }

    private void setItemInHand(LivingEntity entity, ItemStack itemStack){
        try{
            //noinspection JavaReflectionMemberAccess
            EntityEquipment.class.getMethod("setItemInMainHand", ItemStack.class).invoke(entity.getEquipment(), itemStack);
        }catch(Exception ex){
            entity.getEquipment().setItemInHand(itemStack);
        }
    }

}
