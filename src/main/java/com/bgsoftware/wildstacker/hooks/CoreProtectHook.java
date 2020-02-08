package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("deprecation")
public final class CoreProtectHook {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static Plugin coreProtect;

    public static void setEnabled(boolean enabled){
        coreProtect = enabled ? Bukkit.getPluginManager().getPlugin("CoreProtect") : null;
    }

    public static void recordBlockChange(OfflinePlayer offlinePlayer, Block block, boolean place) {
        recordBlockChange(offlinePlayer, block.getLocation(), block.getType(), block.getData(), place);
    }

    public static void recordBlockChange(OfflinePlayer offlinePlayer, Location location, Material type, byte data, boolean place) {
        if(coreProtect == null)
            return;

        if(!Bukkit.isPrimaryThread()){
            Executor.sync(() -> recordBlockChange(offlinePlayer, location, type, data, place));
            return;
        }

        CoreProtectAPI coreProtectAPI = ((CoreProtect) coreProtect).getAPI();

        if(coreProtectAPI.APIVersion() == 5) {
            if(!place)
                coreProtectAPI.logRemoval(offlinePlayer.getName(), location, type, data);
            else
                coreProtectAPI.logPlacement(offlinePlayer.getName(), location, type, data);
        }
        else if(coreProtectAPI.APIVersion() == 6) {
            if(!place)
                coreProtectAPI.logRemoval(offlinePlayer.getName(), location, type,
                        (org.bukkit.block.data.BlockData) plugin.getNMSAdapter().getBlockData(type, data));
            else
                coreProtectAPI.logPlacement(offlinePlayer.getName(), location, type,
                        (org.bukkit.block.data.BlockData) plugin.getNMSAdapter().getBlockData(type, data));
        }
    }

}
