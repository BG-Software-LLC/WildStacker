package com.bgsoftware.wildstacker.utils.items;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public final class ItemBuilder {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private ItemStack itemStack;
    private ItemMeta itemMeta;
    private String texture;

    public ItemBuilder(Materials material){
        this(material.toBukkitItem());
    }

    public ItemBuilder(ItemStack itemStack){
        this.itemStack = itemStack.clone();
        this.itemMeta = itemStack.getItemMeta();
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

    public ItemBuilder withLore(List<String> lore){
        itemMeta.setLore(lore.stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList()));
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

    public ItemBuilder replaceName(String regex, String replace){
        if(itemMeta.hasDisplayName())
            withName(itemMeta.getDisplayName().replace(regex, replace));
        return this;
    }

    public ItemBuilder replaceLore(String regex, String replace){
        if(!itemMeta.hasLore())
            return this;

        List<String> loreList = new ArrayList<>();

        for(String line : itemMeta.getLore()){
            loreList.add(line.replace(regex, replace));
        }

        withLore(loreList);
        return this;
    }

    public ItemBuilder replaceAll(String regex, String replace){
        replaceName(regex, replace);
        replaceLore(regex, replace);
        return this;
    }

    public ItemBuilder withEnchant(Enchantment enchant, int level){
        itemMeta.addEnchant(enchant, level, true);
        return this;
    }

    public ItemBuilder withFlags(ItemFlag... itemFlags){
        itemMeta.addItemFlags(itemFlags);
        return this;
    }

    public ItemBuilder asSkullOf(String texture){
        if(itemStack.isSimilar(Materials.PLAYER_HEAD.toBukkitItem()))
            this.texture = texture;
        return this;
    }

    public ItemStack build(){
        return build(1);
    }

    public ItemStack build(int amount){
        itemStack.setItemMeta(itemMeta);
        itemStack.setAmount(amount);
        return texture == null ? itemStack : plugin.getNMSAdapter().getPlayerSkull(itemStack, texture);
    }

    public ItemBuilder copy(){
        try {
            ItemBuilder itemBuilder = new ItemBuilder(Material.AIR);
            itemBuilder.itemStack = itemStack.clone();
            itemBuilder.itemMeta = itemMeta.clone();
            return itemBuilder;
        }catch(Exception ex){
            throw new NullPointerException(ex.getMessage());
        }
    }

}
