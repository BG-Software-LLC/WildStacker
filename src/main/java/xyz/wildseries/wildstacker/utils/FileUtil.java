package xyz.wildseries.wildstacker.utils;

import xyz.wildseries.wildstacker.WildStackerPlugin;

import java.io.File;

public final class FileUtil {

    private static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    public static void saveResource(String resourcePath){
        saveResource(resourcePath, null);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveResource(String resourcePath, String destination){
        try {
            File file = new File(plugin.getDataFolder(), resourcePath);
            plugin.saveResource(resourcePath, true);

            if(destination != null){
                File dest = new File(plugin.getDataFolder(), destination);
//                if (!dest.exists())
//                    dest.createNewFile();

                file.renameTo(dest);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
