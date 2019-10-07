package com.bgsoftware.wildstacker.utils;

import com.bgsoftware.wildstacker.api.objects.StackedObject;
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
            inHand.setAmount(1);
            e.getPlayer().getInventory().removeItem(inHand);
        }
        stackedObject.remove();
    }

}
