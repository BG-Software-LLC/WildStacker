package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.hooks.PluginHooks;
import com.bgsoftware.wildstacker.hooks.ProtocolLibHook;
import com.bgsoftware.wildstacker.listeners.events.EggLayEvent;
import com.bgsoftware.wildstacker.listeners.events.EntityPickupItemEvent;
import com.bgsoftware.wildstacker.listeners.events.ScuteDropEvent;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.entity.EntityStorage;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public final class ItemsListener implements Listener {

    private final WildStackerPlugin plugin;

    public ItemsListener(WildStackerPlugin plugin) {
        this.plugin = plugin;
        if(ServerVersion.isAtLeast(ServerVersion.v1_13))
            plugin.getServer().getPluginManager().registerEvents(new ScuteListener(plugin), plugin);
        if(ServerVersion.isAtLeast(ServerVersion.v1_8))
            plugin.getServer().getPluginManager().registerEvents(new MergeListener(plugin), plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent e){
        if(!plugin.getSettings().itemsStackingEnabled || !plugin.getNMSAdapter().isDroppedItem(e.getEntity()))
            return;

        StackedItem stackedItem = WStackedItem.ofBypass(e.getEntity());

        if(!stackedItem.isCached())
            return;

        int limit = stackedItem.getStackLimit();

        if(stackedItem.getStackAmount() > limit){
            ItemStack cloned = stackedItem.getItemStack().clone();
            cloned.setAmount(cloned.getAmount() - limit);
            stackedItem.setStackAmount(limit, true);
            StackedItem spawnedItem = plugin.getSystemManager().spawnItemWithAmount(e.getEntity().getLocation(), cloned);
            spawnedItem.getItem().setPickupDelay(40);
        }

        stackedItem.runStackAsync(optionalItem -> {
            if(optionalItem.isPresent())
                return;

            Executor.sync(() -> {
                if(EntityStorage.hasMetadata(e.getEntity(), "player-drop"))
                    EntityStorage.removeMetadata(e.getEntity(), "player-drop");
                else if(isChunkLimit(e.getLocation().getChunk()))
                    stackedItem.remove();
            });
        });
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerItemDrop(PlayerDropItemEvent e){
        EntityStorage.setMetadata(e.getItemDrop(), "player-drop", true);
    }

    @EventHandler
    public void onEggLay(EggLayEvent e){
        if(!plugin.getSettings().eggLayMultiply || !EntityUtils.isStackable(e.getChicken()))
            return;

        StackedEntity stackedEntity = WStackedEntity.of(e.getChicken());
        if(stackedEntity.getStackAmount() > 1) {
            ItemStack eggItem = e.getEgg().getItemStack();
            eggItem.setAmount(stackedEntity.getStackAmount());
            e.getEgg().setItemStack(eggItem);
        }
    }

    //This method will be fired even if stacking-drops is disabled.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent e){
        if(ItemUtils.isStackable(e.getEntity()))
            WStackedItem.of(e.getEntity()).remove();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPickup(EntityPickupItemEvent e) {
        StackedItem stackedItem = e.getItem();
        Item item = stackedItem.getItem();

        if(EntityStorage.hasMetadata(item, "pickup")){
            EntityStorage.removeMetadata(item, "pickup");
            stackedItem.remove();

            e.setCancelled(true);

            return;
        }

        //Should run only if the item is 1 (stacked item)
        if(plugin.getSettings().itemsStackingEnabled || (stackedItem.getStackAmount() > stackedItem.getItemStack().getMaxStackSize() ||
                (plugin.getSettings().bucketsStackerEnabled && e.getItem().getItemStack().getType().name().contains("BUCKET")))) {
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

                if(itemStack.getAmount() > maxStackSize)
                    itemStack.setAmount(maxStackSize);

                if(itemStack.getAmount() == stackedItem.getStackAmount()){
                    stackedItem.remove();
                }
                else {
                    stackedItem.setStackAmount(stackAmount - itemStack.getAmount(), true);
                }

                setItemInHand(e.getEntity(), itemStack);
                e.getEntity().getEquipment().setItemInHandDropChance(2.0f);
            }

            if(stackAmount != stackedItem.getStackAmount()) {
                if (e.getPlayer() != null && plugin.getSettings().itemsSoundEnabled) {
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
                plugin.getNMSAdapter().playPickupAnimation(e.getEntity(), item);
            }

            if (stackedItem.getStackAmount() <= 0) {
                item.setPickupDelay(5);
                EntityStorage.setMetadata(item, "pickup", true);

                Executor.sync(() -> {
                    e.getItem().remove();
                    stackedItem.remove();
                }, 2L);
            }
        }
    }

    //This method will be fired even if stacking-drops is disabled.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryPickup(InventoryPickupItemEvent e){
        if(!ItemUtils.isStackable(e.getItem()))
            return;

        StackedItem stackedItem = WStackedItem.of(e.getItem());
        if (stackedItem.getStackAmount() > 1) {
            e.setCancelled(true);
            stackedItem.giveItemStack(e.getInventory());
            Block hopper = e.getItem().getLocation().subtract(0, 1, 0).getBlock();
            hopper.getState().update();
        }
    }

    @EventHandler
    public void g(BlockDropItemEvent e){
        if(!plugin.getSettings().itemsStackingEnabled)
            return;

        Map<ItemStack, Item> itemsMap = new HashMap<>();
        List<Item> itemsToRemove = new ArrayList<>();

        for(Item item : e.getItems()){
            ItemStack clone = item.getItemStack().clone();
            clone.setAmount(1);

            StackedItem stackedItem = WStackedItem.ofBypass(item);

            if(stackedItem.isCached()){
                Item currentItem = itemsMap.get(clone);

                if(currentItem == null){
                    itemsMap.put(clone, item);
                }
                else{
                    itemsToRemove.add(item);
                    currentItem.getItemStack().setAmount(currentItem.getItemStack().getAmount() + item.getItemStack().getAmount());
                }

                plugin.getSystemManager().removeStackObject(stackedItem);
            }
        }

        e.getItems().removeAll(itemsToRemove);
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e){
        if(!plugin.getSettings().itemsNamesToggleEnabled)
            return;

        String commandSyntax = "/" + plugin.getSettings().itemsNamesToggleCommand;

        if(!e.getMessage().equalsIgnoreCase(commandSyntax) && !e.getMessage().startsWith(commandSyntax + " "))
            return;

        e.setCancelled(true);

        if(!PluginHooks.isProtocolLibEnabled){
            e.getPlayer().sendMessage(ChatColor.RED + "The command is enabled but ProtocolLib is not installed. Please contact the administrators of the server to solve the issue.");
            return;
        }

        if(plugin.getSystemManager().hasItemNamesToggledOff(e.getPlayer())){
            Locale.ITEM_NAMES_TOGGLE_ON.send(e.getPlayer());
        }
        else{
            Locale.ITEM_NAMES_TOGGLE_OFF.send(e.getPlayer());
        }

        plugin.getSystemManager().toggleItemNames(e.getPlayer());

        //Refresh item names
        EntityUtils.getNearbyEntities(e.getPlayer().getLocation(), 48, entity ->
                entity instanceof Item && plugin.getNMSAdapter().isCustomNameVisible(entity))
                .whenComplete((nearbyEntities, ex) ->
                        nearbyEntities.forEach(entity -> ProtocolLibHook.updateName(e.getPlayer(), entity)));
    }

    private boolean isChunkLimit(Chunk chunk){
        int chunkLimit = plugin.getSettings().itemsChunkLimit;

        if(chunkLimit <= 0)
            return false;

        return (int) Arrays.stream(chunk.getEntities()).filter(entity -> entity instanceof Item).count() > chunkLimit;
    }

    private void setItemInHand(LivingEntity entity, ItemStack itemStack){
        try{
            //noinspection JavaReflectionMemberAccess
            EntityEquipment.class.getMethod("setItemInMainHand", ItemStack.class).invoke(entity.getEquipment(), itemStack);
        }catch(Exception ex){
            entity.getEquipment().setItemInHand(itemStack);
        }
    }

    private static class MergeListener implements Listener{

        private final WildStackerPlugin plugin;

        private MergeListener(WildStackerPlugin plugin){
            this.plugin = plugin;
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onItemMerge(ItemMergeEvent e){
            if(!plugin.getSettings().itemsStackingEnabled || !ItemUtils.isStackable(e.getEntity()) ||
                    !ItemUtils.isStackable(e.getTarget()))
                return;

            StackedItem stackedItem = WStackedItem.of(e.getEntity());

            if(!stackedItem.isCached())
                return;

            //We are overriding the merge system
            e.setCancelled(true);

            Executor.sync(() -> {
                if(e.getEntity().isValid() && e.getTarget().isValid()){
                    StackedItem targetItem = WStackedItem.of(e.getTarget());
                    stackedItem.runStackAsync(targetItem, null);
                }
            }, 5L);
        }

    }

    private static class ScuteListener implements Listener{

        private final WildStackerPlugin plugin;

        ScuteListener(WildStackerPlugin plugin){
            this.plugin = plugin;
        }

        @EventHandler
        public void onScoutDrop(ScuteDropEvent e){
            if(!plugin.getSettings().scuteMultiply || !EntityUtils.isStackable(e.getTurtle()))
                return;

            StackedEntity stackedEntity = WStackedEntity.of(e.getTurtle());
            ItemStack scuteItem = e.getScute().getItemStack();
            scuteItem.setAmount(stackedEntity.getStackAmount());
            e.getScute().setItemStack(scuteItem);
        }

    }

}
