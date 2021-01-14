package com.bgsoftware.wildstacker.utils.files;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.menu.WildMenu;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.items.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FileUtils {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private FileUtils(){

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveResource(String resourcePath){
        try {
            File file = new File(plugin.getDataFolder(), resourcePath);
            String fileType = resourcePath.endsWith(".json") ? "json" : "yml";

            if(!file.exists()) {
                String legacyName = resourcePath.replace("." + fileType, "") + "_legacy." + fileType;
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

    public static InputStream getResource(String resourcePath){
        String fileType = resourcePath.endsWith(".json") ? "json" : "yml";

        String legacyName = resourcePath.replace("." + fileType, "") + "_legacy." + fileType;
        if (ServerVersion.isLegacy() && plugin.getResource(legacyName) != null) {
            return plugin.getResource(legacyName);
        }else{
            return plugin.getResource(resourcePath);
        }
    }

    public static ItemBuilder getItemStack(String fileName, ConfigurationSection section){
        if(section == null || !section.contains("type"))
            return null;

        Material type;
        short data;

        try{
            type = Material.valueOf(section.getString("type"));
            data = (short) section.getInt("data");
        }catch(IllegalArgumentException ex){
            WildStackerPlugin.log("&c[" + fileName + "] Couldn't convert " + section.getCurrentPath() + " into an itemstack. Check type & data sections!");
            return null;
        }

        ItemBuilder itemBuilder = new ItemBuilder(type, data);

        if(section.contains("name"))
            itemBuilder.withName(ChatColor.translateAlternateColorCodes('&', section.getString("name")));

        if(section.contains("lore"))
            itemBuilder.withLore(section.getStringList("lore"));

        if(section.contains("enchants")){
            for(String _enchantment : section.getConfigurationSection("enchants").getKeys(false)) {
                Enchantment enchantment;

                try {
                    enchantment = Enchantment.getByName(_enchantment);
                } catch (Exception ex) {
                    WildStackerPlugin.log("&c[" + fileName + "] Couldn't convert " + section.getCurrentPath() + ".enchants." + _enchantment + " into an enchantment, skipping...");
                    continue;
                }

                itemBuilder.withEnchant(enchantment, section.getInt("enchants." + _enchantment));
            }
        }

        if(section.contains("flags")){
            for(String flag : section.getStringList("flags"))
                itemBuilder.withFlags(ItemFlag.valueOf(flag));
        }

        if(section.contains("skull"))
            itemBuilder.asSkullOf(section.getString("skull"));

        return itemBuilder;
    }

    public static Map<Character, List<Integer>> loadGUI(WildMenu menu, String fileName, ConfigurationSection section){
        Map<Character, List<Integer>> charSlots = new HashMap<>();

        menu.resetData();

        menu.setTitle(ChatColor.translateAlternateColorCodes('&', section.getString("title", "")));

        List<String> pattern = section.getStringList("pattern");

        menu.setRowsSize(pattern.size());

        for(int row = 0; row < pattern.size(); row++){
            String patternLine = pattern.get(row);
            int slot = row * 9;

            for(int i = 0; i < patternLine.length(); i++){
                char ch = patternLine.charAt(i);
                if(ch != ' '){
                    ItemBuilder itemBuilder = getItemStack(fileName, section.getConfigurationSection("items." + ch));

                    if(itemBuilder != null) {
                        List<String> commands = section.getStringList("commands." + ch);
                        SoundWrapper sound = getSound(section.getConfigurationSection("sounds." + ch));
                        String permission = section.getString("permissions." + ch + ".permission");
                        SoundWrapper noAccessSound = getSound(section.getConfigurationSection("permissions." + ch + ".no-access-sound"));

                        menu.addFillItem(slot, itemBuilder);
                        menu.addCommands(slot, commands);
                        menu.addPermission(slot, permission, noAccessSound);
                        menu.addSound(slot, sound);
                    }

                    if(!charSlots.containsKey(ch))
                        charSlots.put(ch, new ArrayList<>());

                    charSlots.get(ch).add(slot);

                    slot++;
                }
            }
        }

        return charSlots;
    }

    public static SoundWrapper getSound(ConfigurationSection section){
        Sound sound = null;

        try{
            sound = Sound.valueOf(section.getString("type"));
        }catch(Exception ignored){}

        if(sound == null)
            return null;

        return new SoundWrapper(sound, (float) section.getDouble("volume"), (float) section.getDouble("pitch"));
    }

}
