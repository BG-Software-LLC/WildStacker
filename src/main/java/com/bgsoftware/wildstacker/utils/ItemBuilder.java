package com.bgsoftware.wildstacker.utils;

import com.bgsoftware.wildstacker.utils.legacy.Materials;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public final class ItemBuilder{

    private ItemStack itemStack;
    private ItemMeta itemMeta;

    public ItemBuilder(Materials material){
        this(material.toBukkitItem());
    }

    public ItemBuilder(ItemStack itemStack){
        this(itemStack.getType(), itemStack.getDurability());
    }

    public ItemBuilder(Material type){
        this(type, 0);
    }

    public ItemBuilder(Material type, int damage){
        itemStack = new ItemStack(type, 1, (short) damage);
        itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder withName(String name){
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        return this;
    }

    public ItemBuilder withLore(String firstLine, ConfigurationSection configurationSection){
        List<String> loreList = new ArrayList<>();

        firstLine = ChatColor.translateAlternateColorCodes('&', firstLine);
        loreList.add(firstLine);

        for(String section : configurationSection.getKeys(false)){
            section = section + ": " + configurationSection.get(section).toString();
            loreList.add(ChatColor.getLastColors(firstLine) + ChatColor.translateAlternateColorCodes('&', section));
        }

        if(loreList.size() > 16){
            loreList = loreList.subList(0, 16);
            loreList.add(ChatColor.getLastColors(firstLine) + "...");
        }

        itemMeta.setLore(loreList);
        return this;
    }

    public ItemBuilder withLore(String firstLine, List<String> listLine){
        List<String> loreList = new ArrayList<>();

        firstLine = ChatColor.translateAlternateColorCodes('&', firstLine);
        loreList.add(firstLine);

        for(String line : listLine){
            loreList.add(ChatColor.getLastColors(firstLine) + ChatColor.translateAlternateColorCodes('&', line));
        }

        if(loreList.size() > 10){
            for(int i = 10; i < loreList.size(); i++){
                loreList.remove(loreList.get(i));
            }
            loreList.add(ChatColor.getLastColors(firstLine) + "...");
        }

        itemMeta.setLore(loreList);
        return this;
    }

    public ItemBuilder withLore(String... lore){
        List<String> loreList = new ArrayList<>();

        for(String line : lore){
            loreList.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        itemMeta.setLore(loreList);
        return this;
    }

    public ItemStack build(){
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

}
