package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.wildstacker.utils.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public final class BreakMenuHandler {

    public final Map<Integer, Integer> breakSlots;
    private ItemStack[] contents;
    private String title;

    public BreakMenuHandler(){
        breakSlots = new HashMap<>();
        contents = new ItemStack[0];
        title = "";
    }

    public String getTitle() {
        return title;
    }

    public void openBreakMenu(Player player){
        Inventory inventory = Bukkit.createInventory(null, 9 *3, title);
        inventory.setContents(contents);
        player.openInventory(inventory);
    }

    public boolean isBreakMenu(InventoryView inventory){
        return inventory.getTitle().equals(title);
    }

    public void loadMenu(ConfigurationSection section){
        Inventory inventory = Bukkit.createInventory(null, 9 * section.getInt("rows", 3));
        title = ChatColor.translateAlternateColorCodes('&', section.getString("title", "&lBreak Menu"));

        if(section.contains("fill-items")){
            for(String key : section.getConfigurationSection("fill-items").getKeys(false)){
                ItemStack itemStack = ItemUtil.getFromConfig(section.getConfigurationSection("fill-items." + key));
                for(String slot : section.getString("fill-items." + key + ".slots").split(",")){
                    inventory.setItem(Integer.valueOf(slot), itemStack);
                }
            }
        }

        if(section.contains("break-slots")){
            for(String slot : section.getConfigurationSection("break-slots").getKeys(false)){
                ItemStack itemStack = ItemUtil.getFromConfig(section.getConfigurationSection("break-slots." + slot));
                inventory.setItem(Integer.valueOf(slot), itemStack);
                breakSlots.put(Integer.valueOf(slot), section.getInt("break-slots." + slot + ".amount"));
            }
        }

        contents = inventory.getContents();
    }

}
