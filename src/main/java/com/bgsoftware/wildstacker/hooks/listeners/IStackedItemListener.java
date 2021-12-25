package com.bgsoftware.wildstacker.hooks.listeners;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Item;

public interface IStackedItemListener {

    void recordItemPickup(OfflinePlayer offlinePlayer, Item item, int amount);

}
