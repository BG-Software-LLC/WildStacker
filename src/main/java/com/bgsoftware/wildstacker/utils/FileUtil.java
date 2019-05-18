package com.bgsoftware.wildstacker.utils;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.Arrays;

public final class FileUtil {

    private static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    private static boolean legacy;

    static {
        String[] nonLegacyVersions = new String[] {"1.13", "1.14"};
        legacy = Arrays.stream(nonLegacyVersions).noneMatch(ver -> Bukkit.getBukkitVersion().contains(ver));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveResource(String resourcePath){
        try {
            File file = new File(plugin.getDataFolder(), resourcePath);
            if(!file.exists()) {
                String legacyName = resourcePath.replace(".json", "") + "_legacy.json";
                if (legacy && plugin.getResource(legacyName) != null) {
                    plugin.saveResource(legacyName, true);
                    File legacyFile = new File(plugin.getDataFolder(), legacyName);
                    legacyFile.renameTo(file);
                }else{
                    plugin.saveResource(resourcePath, true);
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
