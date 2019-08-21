package com.bgsoftware.wildstacker.utils;

import com.bgsoftware.wildstacker.WildStackerPlugin;

import java.io.File;

public final class FileUtil {

    private static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveResource(String resourcePath){
        try {
            File file = new File(plugin.getDataFolder(), resourcePath);
            if(!file.exists()) {
                String legacyName = resourcePath.replace(".json", "") + "_legacy.json";
                if (ServerVersion.isLegacy() && plugin.getResource(legacyName) != null) {
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
