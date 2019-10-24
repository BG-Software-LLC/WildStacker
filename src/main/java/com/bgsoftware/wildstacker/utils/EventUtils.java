package com.bgsoftware.wildstacker.utils;

import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import org.bukkit.GameMode;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public final class EventUtils {

    public static void cancelEventAsync(StackedObject stackedObject, BlockPlaceEvent e, boolean giveItem){
        cancelEventAsync(stackedObject, e, e.getItemInHand(), giveItem);
    }

    public static void cancelEventAsync(StackedObject stackedObject, BlockPlaceEvent e, ItemStack _inHand, boolean giveItem){
        e.setCancelled(true);
        if(!giveItem && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            ItemStack inHand = _inHand.clone();
            inHand.setAmount(inHand.getAmount() - 1);
            //Using this method as remove() doesn't affect off hand
            ItemUtils.setItemInHand(e.getPlayer().getInventory(), _inHand, inHand);
        }
        stackedObject.remove();
    }

}
