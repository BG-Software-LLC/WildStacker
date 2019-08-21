package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.utils.items.ItemUtil;
import net.coreprotect.CoreProtect;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;

@SuppressWarnings("deprecation")
public final class CoreProtectHook {

    private static CoreProtect coreProtect;

    static {
        coreProtect = (CoreProtect) Bukkit.getPluginManager().getPlugin("CoreProtect");
    }

    public static void recordBlockChange(OfflinePlayer offlinePlayer, Block block, boolean place) {
        recordBlockChange(offlinePlayer, block.getLocation(), block.getType(), block.getData(), place);
    }

    public static void recordBlockChange(OfflinePlayer offlinePlayer, Location location, Material type, byte data, boolean place) {
        if(coreProtect.getAPI().APIVersion() == 5) {
            if(!place)
                coreProtect.getAPI().logRemoval(offlinePlayer.getName(), location, type, data);
            else
                coreProtect.getAPI().logPlacement(offlinePlayer.getName(), location, type, data);
        }
        else if(coreProtect.getAPI().APIVersion() == 6) {
            if(!place)
                coreProtect.getAPI().logRemoval(offlinePlayer.getName(), location, type,
                        (org.bukkit.block.data.BlockData) ItemUtil.getBlockData(type, data));
            else
                coreProtect.getAPI().logPlacement(offlinePlayer.getName(), location, type,
                        (org.bukkit.block.data.BlockData) ItemUtil.getBlockData(type, data));
        }
    }

}
