package com.bgsoftware.wildstacker.hooks.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

public interface IStackedBlockListener {

    void recordBlockChange(OfflinePlayer offlinePlayer, Location location, Material type, byte data, Action action);

    enum Action {

        BLOCK_PLACE,
        BLOCK_BREAK

    }

}
