package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public final class BreakMenuHandler {

    public final Map<Integer, Integer> breakSlots;
    private Inventory inventory;

    public BreakMenuHandler(){
        breakSlots = new HashMap<>();
        inventory = null;
    }

    public void openBreakMenu(Player player){
        if(inventory != null)
            player.openInventory(inventory);
    }

    public boolean isBreakMenu(Inventory inventory){
        return inventory.equals(this.inventory);
    }

    public void loadMenu(ConfigurationSection section){
        String title = ChatColor.translateAlternateColorCodes('&', section.getString("title", "&lBreak Menu"));
        inventory = Bukkit.createInventory(null, 9 * section.getInt("rows", 3), title);

        if(section.contains("fill-items")){
            for(String key : section.getConfigurationSection("fill-items").getKeys(false)){
                ItemStack itemStack = ItemUtils.getFromConfig(section.getConfigurationSection("fill-items." + key));
                for(String slot : section.getString("fill-items." + key + ".slots").split(",")){
                    inventory.setItem(Integer.valueOf(slot), itemStack);
                }
            }
        }

        if(section.contains("break-slots")){
            for(String slot : section.getConfigurationSection("break-slots").getKeys(false)){
                ItemStack itemStack = ItemUtils.getFromConfig(section.getConfigurationSection("break-slots." + slot));
                inventory.setItem(Integer.valueOf(slot), itemStack);
                breakSlots.put(Integer.valueOf(slot), section.getInt("break-slots." + slot + ".amount"));
            }
        }
    }

}
