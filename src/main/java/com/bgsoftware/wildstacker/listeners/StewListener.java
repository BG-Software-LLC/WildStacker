package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public final class StewListener implements Listener {

    private WildStackerPlugin plugin;

    public StewListener(WildStackerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStewEat(PlayerItemConsumeEvent e) {
        if (e.getItem().getType().name().contains("STEW") || e.getItem().getType().name().contains("SOUP")) {
            ItemStack inHand = e.getPlayer().getItemInHand().clone();
            inHand.setAmount(inHand.getAmount() - 1);

            Bukkit.getScheduler().runTask(plugin, () -> {
                e.getPlayer().setItemInHand(inHand);
                e.getPlayer().getInventory().addItem(new ItemStack(Material.BOWL));
                ItemUtils.stackStew(e.getItem(), e.getPlayer().getInventory());
            });
        }
    }

}
